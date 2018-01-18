/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.task;

import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.UrlPathBuilder;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientAuthenticationException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceSecurityException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.RegistrationOperationStatusParser;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderX509;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import static com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ContractState.DPS_REGISTRATION_RECEIVED;

public class RegisterTask implements Callable
{
    private static final int MAX_WAIT_FOR_REGISTRATION_RESPONSE = 90*1000; // 90 seconds
    private static final int SLEEP_INTERVAL_WHEN_WAITING_FOR_RESPONSE = 4*1000; //4 seconds
    private static final int DEFAULT_EXPIRY_TIME_IN_SECS = 3600; // 1 Hour
    private static final String SASTOKEN_FORMAT = "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=";
    private ResponseCallback responseCallback = null;
    private ProvisioningDeviceClientContract provisioningDeviceClientContract = null;
    private Authorization authorization = null;
    private SecurityProvider securityProvider = null;
    private ProvisioningDeviceClientConfig provisioningDeviceClientConfig = null;

    private class ResponseCallbackImpl implements ResponseCallback
    {
        @Override
        public void run(ResponseData responseData, Object context) throws ProvisioningDeviceClientException
        {
            if (context instanceof ResponseData)
            {
                ResponseData data = (ResponseData) context;
                data.setResponseData(responseData.getResponseData());
                data.setContractState(responseData.getContractState());
                data.setWaitForStatusInMS(responseData.getWaitForStatusInMS());
            }
            else
            {
                throw new ProvisioningDeviceClientException(new IllegalArgumentException("Context mismatch for DPS registration"));
            }
        }
    }

    /**
     * Constructor for the task to perform registration with service
     * @param provisioningDeviceClientConfig Config client registered with. Cannot be {@code null}.
     * @param securityProvider Security client holding HSM details. Cannot be {@code null}.
     * @param provisioningDeviceClientContract Lower level contract with the service over multiple protocols. Cannot be {@code null}.
     * @param authorization An object that holds the state of the service retrieved data. Cannot be {@code null}.
     * @throws ProvisioningDeviceClientException When any of the provided parameters are invalid.
     */
    RegisterTask(ProvisioningDeviceClientConfig provisioningDeviceClientConfig, SecurityProvider securityProvider,
                 ProvisioningDeviceClientContract provisioningDeviceClientContract, Authorization authorization)
            throws ProvisioningDeviceClientException
    {
        //SRS_RegisterTask_25_002: [ Constructor throw ProvisioningDeviceClientException if provisioningDeviceClientConfig , securityProvider, authorization or provisioningDeviceClientContract is null.]
        if (provisioningDeviceClientContract == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("provisioningDeviceClientContract cannot be null"));
        }

        if (securityProvider == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("security client cannot be null"));
        }

        if (provisioningDeviceClientConfig == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("provisioningDeviceClientConfig cannot be null"));
        }

        if (authorization == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("authorization cannot be null"));
        }

        //SRS_RegisterTask_25_001: [ Constructor shall save provisioningDeviceClientConfig , securityProvider, provisioningDeviceClientContract and authorization.]
        this.provisioningDeviceClientConfig = provisioningDeviceClientConfig;
        this.securityProvider = securityProvider;
        this.provisioningDeviceClientContract = provisioningDeviceClientContract;
        this.authorization = authorization;
        this.responseCallback = new ResponseCallbackImpl();
    }

    private RegistrationOperationStatusParser authenticateWithX509(String registrationId) throws ProvisioningDeviceClientException
    {
        //SRS_RegisterTask_25_003: [ If the provided security client is for X509 then, this method shall throw ProvisioningDeviceClientException if registration id is null. ]
        if (registrationId == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("registration id cannot be null"));
        }

        try
        {
            SecurityProviderX509 dpsSecurityProviderX509 = (SecurityProviderX509) securityProvider;

            //SRS_RegisterTask_25_004: [ If the provided security client is for X509 then, this method shall save the SSL context to Authorization if it is not null and throw ProvisioningDeviceClientException otherwise. ]

            SSLContext sslContext = dpsSecurityProviderX509.getSSLContext();
            if (sslContext == null)
            {
                throw new ProvisioningDeviceSecurityException("Retrieved Null SSL context from security client");
            }
            authorization.setSslContext(sslContext);

            RequestData requestData = new RequestData( registrationId,  sslContext, null);

            //SRS_RegisterTask_25_006: [ If the provided security client is for X509 then, this method shall trigger authenticateWithProvisioningService on the contract API and wait for response and return it. ]
            ResponseData dpsRegistrationData = new ResponseData();
            this.provisioningDeviceClientContract.authenticateWithProvisioningService(requestData, responseCallback, dpsRegistrationData);

            waitForResponse(dpsRegistrationData);

            if (dpsRegistrationData.getResponseData() != null && dpsRegistrationData.getContractState() == DPS_REGISTRATION_RECEIVED)
            {
                return RegistrationOperationStatusParser.createFromJson(new String(dpsRegistrationData.getResponseData()));
            }
            else
            {
                //SRS_RegisterTask_25_007: [ If the provided security client is for X509 then, this method shall throw ProvisioningDeviceClientException if null response is received. ]
                throw new ProvisioningDeviceClientException("Did not receive DPS registration successfully");
            }
        }
        catch (InterruptedException | SecurityProviderException e)
        {
            throw new ProvisioningDeviceClientException(e);
        }
    }

    private String constructSasToken(String registrationId, int expiryTime) throws ProvisioningDeviceClientException, UnsupportedEncodingException, SecurityProviderException
    {
        if (expiryTime <= 0)
        {
            throw new IllegalArgumentException("expiry time cannot be negative or zero");
        }
        String tokenScope = new UrlPathBuilder(provisioningDeviceClientConfig.getIdScope()).generateSasTokenUrl(registrationId);
        if (tokenScope == null || tokenScope.isEmpty())
        {
            throw new ProvisioningDeviceClientException("Could not construct token scope");
        }
        SecurityProviderTpm securityClientTpm = (SecurityProviderTpm) securityProvider;
        Long expiryTimeUTC = System.currentTimeMillis() / 1000 + expiryTime;
        byte[] token = securityClientTpm.signWithIdentity(tokenScope.concat("\n" + String.valueOf(expiryTimeUTC)).getBytes());
        if (token == null || token.length == 0)
        {
            throw new ProvisioningDeviceSecurityException("Security client could not sign data successfully");
        }

        byte[] base64Signature = Base64.encodeBase64Local(token);
        String base64UrlEncodedSignature = URLEncoder.encode(new String(base64Signature), StandardCharsets.UTF_8.displayName());
        //SRS_RegisterTask_25_015: [ If the provided security client is for Key then, this method shall build the SasToken of the format SharedAccessSignature sr=<tokenScope>&sig=<signature>&se=<expiryTime>&skn= and save it to authorization]
        return String.format(SASTOKEN_FORMAT, tokenScope, base64UrlEncodedSignature, expiryTimeUTC);
    }

    private RegistrationOperationStatusParser processWithNonce(byte[] base64DecodedAuthKey,
                                                               SecurityProviderTpm securityClientTpm,
                                                               RequestData requestData)
            throws IOException, InterruptedException, ProvisioningDeviceClientException,SecurityProviderException

    {
        if (base64DecodedAuthKey != null)
        {
            //SRS_RegisterTask_25_018: [ If the provided security client is for Key then, this method shall import the Base 64 encoded Authentication Key into the HSM using the security client and pass the exception to the user on failure. ]
            securityClientTpm.activateIdentityKey(base64DecodedAuthKey);

            /*SRS_RegisterTask_25_014: [ If the provided security client is for Key then, this method shall construct SasToken by doing the following

            1. Build a tokenScope of format <scope>/registrations/<registrationId>
            2. Sign the HSM with the string of format <tokenScope>/n<expiryTime> and receive a token
            3. Encode the token to Base64 format and UrlEncode it to generate the signature. ]*/

            String sasToken = null;
            try
            {
                sasToken = this.constructSasToken(securityClientTpm.getRegistrationId(), DEFAULT_EXPIRY_TIME_IN_SECS);
            }
            catch (SecurityProviderException e)
            {
                throw new ProvisioningDeviceSecurityException(e);
            }

            requestData.setSasToken(sasToken);

            //SRS_RegisterTask_25_016: [ If the provided security client is for Key then, this method shall trigger authenticateWithProvisioningService on the contract API using the sasToken generated and wait for response and return it. ]
            ResponseData responseDataForSasTokenAuth = new ResponseData();
            this.provisioningDeviceClientContract.authenticateWithProvisioningService(requestData, responseCallback,
                                                                                      responseDataForSasTokenAuth);
            waitForResponse(responseDataForSasTokenAuth);

            if (responseDataForSasTokenAuth.getResponseData() != null &&
                    responseDataForSasTokenAuth.getContractState() == DPS_REGISTRATION_RECEIVED)
            {
                this.authorization.setSasToken(sasToken);
                return RegistrationOperationStatusParser.createFromJson(new String(responseDataForSasTokenAuth.getResponseData()));
            }
            else
            {
                //SRS_RegisterTask_25_017: [ If the provided security client is for Key then, this method shall throw ProvisioningDeviceClientException if null response to authenticateWithProvisioningService is received. ]
                throw new ProvisioningDeviceClientAuthenticationException("Service did not authorize SasToken");
            }
        }
        else
        {
            //SRS_RegisterTask_25_013: [ If the provided security client is for Key then, this method shall throw ProvisioningDeviceClientException if Authentication Key received is null. ]
            throw new ProvisioningDeviceClientAuthenticationException("Service did not send authentication key");
        }
    }

    private RegistrationOperationStatusParser authenticateWithSasToken(String registrationId) throws ProvisioningDeviceClientException
    {
        //SRS_RegisterTask_25_008: [ If the provided security client is for Key then, this method shall throw ProvisioningDeviceClientException if registration id or endorsement key or storage root key are null. ]
        if (registrationId == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("registration id cannot be null"));
        }

        try
        {
            SecurityProviderTpm securityClientTpm = (SecurityProviderTpm) securityProvider;
            if (securityClientTpm.getEndorsementKey() == null || securityClientTpm.getStorageRootKey() == null)
            {
                throw new ProvisioningDeviceSecurityException(new IllegalArgumentException("Ek or SRK cannot be null"));
            }

            //SRS_RegisterTask_25_009: [ If the provided security client is for Key then, this method shall save the SSL context to Authorization if it is not null and throw ProvisioningDeviceClientException otherwise. ]
            SSLContext sslContext = securityClientTpm.getSSLContext();
            if (sslContext == null)
            {
                throw new ProvisioningDeviceSecurityException("Null SSL Context received from security client");
            }
            authorization.setSslContext(sslContext);

            RequestData requestData = new RequestData(securityClientTpm.getEndorsementKey(), securityClientTpm.getStorageRootKey(), registrationId, sslContext, null);

            //SRS_RegisterTask_25_011: [ If the provided security client is for Key then, this method shall trigger requestNonceForTPM on the contract API and wait for Authentication Key and decode it from Base64. Also this method shall pass the exception back to the user if it fails. ]
            ResponseData nonceResponseData = new ResponseData();
            this.provisioningDeviceClientContract.requestNonceForTPM(requestData, responseCallback, nonceResponseData);

            waitForResponse(nonceResponseData);

            if (nonceResponseData.getContractState() == DPS_REGISTRATION_RECEIVED)
            {
                return processWithNonce(nonceResponseData.getResponseData(), securityClientTpm, requestData);
            }
            else
            {
                //SRS_RegisterTask_25_012: [ If the provided security client is for Key then, this method shall throw ProvisioningDeviceClientException if null response is received. ]
                throw new ProvisioningDeviceClientException("Did not receive DPS registration nonce successfully");
            }
        }
        catch (IOException | InterruptedException | SecurityProviderException e)
        {
            throw new ProvisioningDeviceClientException(e);
        }
    }

    private RegistrationOperationStatusParser authenticateWithDPS() throws ProvisioningDeviceClientException
    {
        try
        {
            if (this.securityProvider instanceof SecurityProviderX509)
            {
                return this.authenticateWithX509(this.securityProvider.getRegistrationId());
            }
            else if (this.securityProvider instanceof SecurityProviderTpm)
            {
                return this.authenticateWithSasToken(this.securityProvider.getRegistrationId());
            }
            else
            {
                throw new ProvisioningDeviceSecurityException("Unknown Security client received");
            }
        }
        catch (SecurityProviderException e)
        {
            throw new ProvisioningDeviceSecurityException(e);
        }
    }

    /**
     * Callable call by the thread which handles Authentication and registration of a given device with the service
     * @return RegistrationOperationStatusParser holding the state of the service post registration
     * @throws Exception if registration fails.
     */
    @Override
    public RegistrationOperationStatusParser call() throws Exception
    {
        return this.authenticateWithDPS();
    }

    /**
     * Busy waits for the provided responseData to be populated or for a timeout to occur
     * @param responseData the responseData object to periodically check for response data
     * @throws InterruptedException if an interrupted exception is thrown while this thread sleeps
     */
    private void waitForResponse(ResponseData responseData) throws InterruptedException
    {
        long millisecondsElapsed = 0;
        long waitTimeStart = System.currentTimeMillis();
        while (responseData.getContractState() != DPS_REGISTRATION_RECEIVED
                && millisecondsElapsed < MAX_WAIT_FOR_REGISTRATION_RESPONSE)
        {
            Thread.sleep(SLEEP_INTERVAL_WHEN_WAITING_FOR_RESPONSE);
            millisecondsElapsed = System.currentTimeMillis() - waitTimeStart;
        }
    }
}
