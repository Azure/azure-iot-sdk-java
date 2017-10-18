/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.provisioningtask;

import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientAuthenticationException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.RegisterRequestParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.RegisterResponseTPMParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.ResponseParser;
import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.UrlPathBuilder;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceSecurityException;
import com.microsoft.azure.sdk.iot.dps.security.DPSSecurityClient;
import com.microsoft.azure.sdk.iot.dps.security.DPSSecurityClientKey;
import com.microsoft.azure.sdk.iot.dps.security.DPSSecurityClientX509;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import static com.microsoft.azure.sdk.iot.provisioning.device.internal.provisioningtask.ContractState.DPS_REGISTRATION_RECEIVED;

public class RegisterTask implements Callable
{
    private static final int MAX_WAIT_FOR_REGISTRATION_RESPONSE = 100;
    private static final int DEFAULT_EXPIRY_TIME = 3600; // 1 Hour
    private ResponseCallback responseCallback = null;
    private ProvisioningDeviceClientContract provisioningDeviceClientContract = null;
    private Authorization authorization = null;
    private DPSSecurityClient dpsSecurityClient = null;
    private ProvisioningDeviceClientConfig provisioningDeviceClientConfig = null;

    private class ResponseCallbackImpl implements ResponseCallback
    {
        @Override
        public void run(byte[] responseData, Object context) throws ProvisioningDeviceClientException
        {
            if (context instanceof ResponseData)
            {
                ResponseData data = (ResponseData) context;
                data.setResponseData(responseData);
                data.setContractState(DPS_REGISTRATION_RECEIVED);
            }
            else
            {
                throw new ProvisioningDeviceClientException("Context mismatch for DPS registration");
            }
        }
    }

    /**
     * Constructor for the task to perform registration with service
     * @param provisioningDeviceClientConfig Config client registered with. Cannot be {@code null}.
     * @param dpsSecurityClient Security client holding HSM details. Cannot be {@code null}.
     * @param provisioningDeviceClientContract Lower level contract with the service over multiple protocols. Cannot be {@code null}.
     * @param authorization An object that holds the state of the service retrieved data. Cannot be {@code null}.
     * @throws ProvisioningDeviceClientException When any of the provided parameters are invalid.
     */
    RegisterTask(ProvisioningDeviceClientConfig provisioningDeviceClientConfig, DPSSecurityClient dpsSecurityClient,
                 ProvisioningDeviceClientContract provisioningDeviceClientContract, Authorization authorization)
            throws ProvisioningDeviceClientException
    {
        //SRS_RegisterTask_25_002: [ Constructor throw ProvisioningDeviceClientException if provisioningDeviceClientConfig , dpsSecurityClient, authorization or provisioningDeviceClientContract is null.]
        if (provisioningDeviceClientContract == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("provisioningDeviceClientContract cannot be null"));
        }

        if (dpsSecurityClient == null)
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

        //SRS_RegisterTask_25_001: [ Constructor shall save provisioningDeviceClientConfig , dpsSecurityClient, provisioningDeviceClientContract and authorization.]
        this.provisioningDeviceClientConfig = provisioningDeviceClientConfig;
        this.dpsSecurityClient = dpsSecurityClient;
        this.provisioningDeviceClientContract = provisioningDeviceClientContract;
        this.authorization = authorization;
        this.responseCallback = new ResponseCallbackImpl();
    }

    private ResponseParser authenticateWithX509(String registrationId) throws ProvisioningDeviceClientException
    {
        //SRS_RegisterTask_25_003: [ If the provided security client is for X509 then, this method shall throw ProvisioningDeviceClientException if registration id is null. ]
        if (registrationId == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("registration id cannot be null"));
        }

        try
        {
            //SRS_RegisterTask_25_005: [ If the provided security client is for X509 then, this method shall build the required Json input using parser. ]
            byte[] payload = new RegisterRequestParser(registrationId).toJson().getBytes();

            DPSSecurityClientX509 dpsSecurityClientX509 = (DPSSecurityClientX509) dpsSecurityClient;

            //SRS_RegisterTask_25_004: [ If the provided security client is for X509 then, this method shall save the SSL context to Authorization if it is not null and throw ProvisioningDeviceClientException otherwise. ]

            SSLContext sslContext = dpsSecurityClientX509.getSSLContext();
            if (sslContext == null)
            {
                throw new ProvisioningDeviceSecurityException("Retrieved Null SSL context from security client");
            }
            authorization.setSslContext(sslContext);

            //SRS_RegisterTask_25_006: [ If the provided security client is for X509 then, this method shall trigger authenticateWithDPS on the contract API and wait for response and return it. ]
            ResponseData dpsRegistrationData = new ResponseData();
            this.provisioningDeviceClientContract.authenticateWithDPS(payload, registrationId, sslContext, null, responseCallback, dpsRegistrationData);

            if (dpsRegistrationData.getResponseData() == null || dpsRegistrationData.getContractState() != DPS_REGISTRATION_RECEIVED)
            {
                Thread.sleep(MAX_WAIT_FOR_REGISTRATION_RESPONSE);
            }

            if (dpsRegistrationData.getResponseData() != null && dpsRegistrationData.getContractState() == DPS_REGISTRATION_RECEIVED)
            {
                return ResponseParser.createFromJson(new String(dpsRegistrationData.getResponseData()));
            }
            else
            {
                //SRS_RegisterTask_25_007: [ If the provided security client is for X509 then, this method shall throw ProvisioningDeviceClientException if null response is received. ]
                throw new ProvisioningDeviceClientException("Did not receive DPS registration successfully");
            }
        }
        catch (InterruptedException e)
        {
            throw new ProvisioningDeviceClientException(e);
        }
    }

    private String constructSasToken(String registrationId, int expiryTime) throws ProvisioningDeviceClientException, UnsupportedEncodingException
    {
        //"SharedAccessSignature sr=%s&sig=%s&se=%s&skn=", token_scope, STRING_c_str(urlEncodedSignature), expire_token);
        if (expiryTime <= 0)
        {
            throw new IllegalArgumentException("expiry time cannot be negative or zero");
        }
        String tokenScope = new UrlPathBuilder(provisioningDeviceClientConfig.getDpsScopeId()).generateSasTokenUrl(registrationId);
        if (tokenScope == null || tokenScope.isEmpty())
        {
            throw new ProvisioningDeviceClientException("Could not construct token scope");
        }
        DPSSecurityClientKey dpsSecurityClientKey = (DPSSecurityClientKey) dpsSecurityClient;
        Long expiryTimeUTC = System.currentTimeMillis() / 1000 + expiryTime;
        byte[] token = dpsSecurityClientKey.signData(tokenScope.concat("\n" + String.valueOf(expiryTimeUTC)).getBytes());
        if (token == null || token.length == 0)
        {
            throw new ProvisioningDeviceSecurityException("Security client could not sign data successfully");
        }

        byte[] base64Signature = Base64.encodeBase64Local(token);
        String base64UrlEncodedSignature = URLEncoder.encode(new String(base64Signature), StandardCharsets.UTF_8.displayName());
        //SRS_RegisterTask_25_015: [ If the provided security client is for Key then, this method shall build the SasToken of the format SharedAccessSignature sr=<tokenScope>&sig=<signature>&se=<expiryTime>&skn= and save it to authorization]
        return String.format("SharedAccessSignature sr=%s&sig=%s&se=%s&skn=", tokenScope, base64UrlEncodedSignature, expiryTimeUTC);
    }

    private ResponseParser processWithNonce(ResponseData responseDataForNonce,
                                            DPSSecurityClientKey dpsSecurityClientKey,
                                            byte[] payload)
            throws IOException, InterruptedException, ProvisioningDeviceClientException
    {

        RegisterResponseTPMParser registerResponseTPMParser = RegisterResponseTPMParser.createFromJson(new String(responseDataForNonce.getResponseData()));

        if (registerResponseTPMParser.getAuthenticationKey() != null)
        {
            //SRS_RegisterTask_25_018: [ If the provided security client is for Key then, this method shall import the Base 64 encoded Authentication Key into the HSM using the security client and pass the exception to the user on failure. ]
            byte[] base64DecodedAuthKey = Base64.decodeBase64Local(registerResponseTPMParser.getAuthenticationKey().getBytes());
            dpsSecurityClientKey.importKey(base64DecodedAuthKey);

            /*SRS_RegisterTask_25_014: [ If the provided security client is for Key then, this method shall construct SasToken by doing the following

            1. Build a tokenScope of format <scopeid>/registrations/<registrationId>
            2. Sign the HSM with the string of format <tokenScope>/n<expiryTime> and receive a token
            3. Encode the token to Base64 format and UrlEncode it to generate the signature. ]*/

            String sasToken = this.constructSasToken(dpsSecurityClientKey.getRegistrationId(), DEFAULT_EXPIRY_TIME);

            //SRS_RegisterTask_25_016: [ If the provided security client is for Key then, this method shall trigger authenticateWithDPS on the contract API using the sasToken generated and wait for response and return it. ]
            ResponseData responseDataForSasTokenAuth = new ResponseData();
            this.provisioningDeviceClientContract.authenticateWithDPS(payload, dpsSecurityClientKey.getRegistrationId(),
                                                                      authorization.getSslContext(),
                                                                      sasToken, responseCallback,
                                                                      responseDataForSasTokenAuth);
            if (responseDataForSasTokenAuth.getResponseData() == null ||
                    responseDataForSasTokenAuth.getContractState() != DPS_REGISTRATION_RECEIVED)
            {
                Thread.sleep(MAX_WAIT_FOR_REGISTRATION_RESPONSE);
            }

            if (responseDataForSasTokenAuth.getResponseData() != null &&
                    responseDataForSasTokenAuth.getContractState() == DPS_REGISTRATION_RECEIVED)
            {
                this.authorization.setSasToken(sasToken);
                return ResponseParser.createFromJson(new String(responseDataForSasTokenAuth.getResponseData()));
            }
            else
            {
                //SRS_RegisterTask_25_017: [ If the provided security client is for Key then, this method shall throw ProvisioningDeviceClientException if null response to authenticateWithDPS is received. ]
                throw new ProvisioningDeviceClientAuthenticationException("Service did not authorize SasToken");
            }
        }
        else
        {
            //SRS_RegisterTask_25_013: [ If the provided security client is for Key then, this method shall throw ProvisioningDeviceClientException if Authentication Key received is null. ]
            throw new ProvisioningDeviceClientAuthenticationException("Service did not send authentication key");
        }
    }
    private ResponseParser authenticateWithSasToken(String registrationId) throws ProvisioningDeviceClientException
    {
        //SRS_RegisterTask_25_008: [ If the provided security client is for Key then, this method shall throw ProvisioningDeviceClientException if registration id or endorsement key or storage root key are null. ]
        if (registrationId == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("registration id cannot be null"));
        }

        try
        {
            DPSSecurityClientKey dpsSecurityClientKey = (DPSSecurityClientKey) dpsSecurityClient;
            if (dpsSecurityClientKey.getDeviceEk() == null || dpsSecurityClientKey.getDeviceSRK() == null)
            {
                throw new ProvisioningDeviceSecurityException(new IllegalArgumentException("Ek or SRK cannot be null"));
            }

            //SRS_RegisterTask_25_010: [ If the provided security client is for Key then, this method shall build the required Json input with base 64 encoded endorsement key, storage root key and on failure pass the exception back to the user. ]
            String base64EncodedEk = new String(Base64.encodeBase64Local(dpsSecurityClientKey.getDeviceEk()));
            String base64EncodedSrk = new String(Base64.encodeBase64Local(dpsSecurityClientKey.getDeviceSRK()));
            byte[] payload = new RegisterRequestParser(registrationId, base64EncodedEk, base64EncodedSrk).toJson().getBytes();

            //SRS_RegisterTask_25_009: [ If the provided security client is for Key then, this method shall save the SSL context to Authorization if it is not null and throw ProvisioningDeviceClientException otherwise. ]
            SSLContext sslContext = dpsSecurityClientKey.getSSLContext();
            if (sslContext == null)
            {
                throw new ProvisioningDeviceSecurityException("Null SSL Context received from security client");
            }
            authorization.setSslContext(sslContext);

            //SRS_RegisterTask_25_011: [ If the provided security client is for Key then, this method shall trigger requestNonceWithDPSTPM on the contract API and wait for Authentication Key and decode it from Base64. Also this method shall pass the exception back to the user if it fails. ]
            ResponseData nonceResponseData = new ResponseData();
            this.provisioningDeviceClientContract.requestNonceWithDPSTPM(payload, registrationId, sslContext, responseCallback, nonceResponseData);

            if (nonceResponseData.getResponseData() == null || nonceResponseData.getContractState() != DPS_REGISTRATION_RECEIVED)
            {
                Thread.sleep(MAX_WAIT_FOR_REGISTRATION_RESPONSE);
            }

            if (nonceResponseData.getResponseData() != null && nonceResponseData.getContractState() == DPS_REGISTRATION_RECEIVED)
            {
                return processWithNonce(nonceResponseData, dpsSecurityClientKey, payload);
            }
            else
            {
                //SRS_RegisterTask_25_012: [ If the provided security client is for Key then, this method shall throw ProvisioningDeviceClientException if null response is received. ]
                throw new ProvisioningDeviceClientException("Did not receive DPS registration nonce successfully");
            }
        }
        catch (IOException | InterruptedException e)
        {
            throw new ProvisioningDeviceClientException(e);
        }
    }

    private ResponseParser authenticateWithDPS() throws ProvisioningDeviceClientException
    {
        if (this.dpsSecurityClient instanceof DPSSecurityClientX509)
        {
            return this.authenticateWithX509(this.dpsSecurityClient.getRegistrationId());
        }
        else if (this.dpsSecurityClient instanceof DPSSecurityClientKey)
        {
            return this.authenticateWithSasToken(this.dpsSecurityClient.getRegistrationId());
        }
        else
        {
            throw new ProvisioningDeviceSecurityException("Unknown Security client received");
        }
    }

    /**
     * Callable call by the thread which handles Authentication and registration of a given device with the service
     * @return ResponseParser holding the state of the service post registration
     * @throws Exception if registration fails.
     */
    @Override
    public ResponseParser call() throws Exception
    {
        return this.authenticateWithDPS();
    }
}
