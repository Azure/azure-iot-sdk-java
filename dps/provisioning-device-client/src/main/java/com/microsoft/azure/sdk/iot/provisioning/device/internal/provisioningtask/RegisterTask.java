/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.provisioningtask;

import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.RegisterRequestParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.RegisterResponseTPMParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.ResponseParser;
import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientAuthorization;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.UrlPathBuilder;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientAuthenticationException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceHubException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceSecurityException;
import com.microsoft.azure.sdk.iot.dps.security.DPSSecurityClient;
import com.microsoft.azure.sdk.iot.dps.security.DPSSecurityClientKey;
import com.microsoft.azure.sdk.iot.dps.security.DPSSecurityClientX509;
import com.microsoft.azure.sdk.iot.dps.security.SecurityType;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import static com.microsoft.azure.sdk.iot.provisioning.device.internal.provisioningtask.ContractState.DPS_REGISTRATION_RECEIVED;

public class RegisterTask implements Callable
{
    private static final int WAIT_FOR_REGISTRATION_RESPONSE = 100;
    private static final int DEFAULT_EXPIRY_TIME = 3600; // 1 Hour
    private ResponseCallbackImpl dpsAuthorizationCallback = null;
    private ProvisioningDeviceClientContract provisioningDeviceClientContract = null;
    private ProvisioningDeviceClientAuthorization provisioningDeviceClientAuthorization = null;
    private DPSSecurityClient dpsSecurityClient = null;
    private ProvisioningDeviceClientConfig provisioningDeviceClientConfig = null;

    class ResponseCallbackImpl implements ResponseCallback
    {
        @Override
        public void run(byte[] responseData, Object context) throws ProvisioningDeviceClientException
        {
            if (context instanceof ResponseData)
            {
                ResponseData data = (ResponseData) context;
                data.responseData = responseData;
                data.dpsRegistrationState = DPS_REGISTRATION_RECEIVED;
            }
            else
            {
                throw new ProvisioningDeviceClientException("Context mismatch for DPS registration");
            }
        }
    }

    // consider to use auth call back if needed
    RegisterTask(ProvisioningDeviceClientConfig provisioningDeviceClientConfig, DPSSecurityClient dpsSecurityClient, ProvisioningDeviceClientContract provisioningDeviceClientContract, ProvisioningDeviceClientAuthorization provisioningDeviceClientAuthorization) throws ProvisioningDeviceClientException
    {
        if (provisioningDeviceClientContract == null)
        {
            throw new ProvisioningDeviceClientException("transport cannot be null");
        }

        if (dpsSecurityClient == null)
        {
            throw new ProvisioningDeviceClientException("security client cannot be null");
        }

        if (provisioningDeviceClientConfig == null)
        {
            throw new ProvisioningDeviceClientException("dps config cannot be null");
        }

        this.provisioningDeviceClientConfig = provisioningDeviceClientConfig;
        this.dpsSecurityClient = dpsSecurityClient;
        this.provisioningDeviceClientContract = provisioningDeviceClientContract;
        this.provisioningDeviceClientAuthorization = provisioningDeviceClientAuthorization; // this may not work fix the broken link with dice/tpm
        this.dpsAuthorizationCallback = new ResponseCallbackImpl();
    }

    private ResponseParser authenticateWithX509(String registrationId) throws ProvisioningDeviceClientException
    {
        try
        {
            if (dpsSecurityClient.getSecurityType() == SecurityType.X509)
            {
                byte[] payload = new RegisterRequestParser(registrationId).toJson().getBytes();
                if (dpsSecurityClient instanceof DPSSecurityClientX509)
                {
                    DPSSecurityClientX509 dpsSecurityClientX509 = (DPSSecurityClientX509) dpsSecurityClient;
                    SSLContext sslContext = dpsSecurityClientX509.getSSLContext();
                    provisioningDeviceClientAuthorization.setSslContext(sslContext);
                    ResponseData dpsRegistrationData = new ResponseData();
                    this.provisioningDeviceClientContract.authenticateWithDPS(payload, registrationId, sslContext, null, dpsAuthorizationCallback, dpsRegistrationData);
                    while (dpsRegistrationData.responseData == null || dpsRegistrationData.dpsRegistrationState != DPS_REGISTRATION_RECEIVED)
                    {
                        Thread.sleep(WAIT_FOR_REGISTRATION_RESPONSE);
                    }
                    if (dpsRegistrationData.responseData != null && dpsRegistrationData.dpsRegistrationState == DPS_REGISTRATION_RECEIVED)
                    {
                        return ResponseParser.createFromJson(new String(dpsRegistrationData.responseData));
                    }
                    else
                    {
                        throw new ProvisioningDeviceClientException("Did not receive DPS registration successfully");
                    }
                }
                else
                {
                    throw new ProvisioningDeviceClientAuthenticationException("Unknown security type received");
                }
            }
            else
            {
                throw new ProvisioningDeviceClientAuthenticationException("Unknown security type received");
            }
        }
        catch (InterruptedException e)
        {
            throw new ProvisioningDeviceClientException(e.getMessage());
        }
    }

    private String constructSasToken(String registrationId, int expiryTime) throws ProvisioningDeviceSecurityException, UnsupportedEncodingException
    {
        //"SharedAccessSignature sr=%s&sig=%s&se=%s&skn=", token_scope, STRING_c_str(urlEncodedSignature), expire_token);
        if (expiryTime <= 0)
        {
            throw new IllegalArgumentException("expiry time cannot be negative or zero");
        }
        String tokenScope = new UrlPathBuilder(provisioningDeviceClientConfig.getDpsScopeId()).generateSasTokenUrl(registrationId);
        if (dpsSecurityClient instanceof DPSSecurityClientKey)
        {
            DPSSecurityClientKey dpsSecurityClientKey = (DPSSecurityClientKey) dpsSecurityClient;
            Long expiryTimeUTC = System.currentTimeMillis() / 1000 + expiryTime;
            byte[] token = dpsSecurityClientKey.signData(tokenScope.concat("\n" + String.valueOf(expiryTimeUTC)).getBytes());
            byte[] base64Signature = Base64.encodeBase64Local(token);
            String base64UrlEncodedSignature = URLEncoder.encode(new String(base64Signature), StandardCharsets.UTF_8.displayName());
            return String.format("SharedAccessSignature sr=%s&sig=%s&se=%s&skn=", tokenScope, base64UrlEncodedSignature, expiryTimeUTC);
        }
        else
        {
            throw new ProvisioningDeviceSecurityException("Unknown security type received");
        }
    }

    private ResponseParser authenticateWithSasToken(String registrationId) throws ProvisioningDeviceClientException
    {
        try
        {
            if (dpsSecurityClient instanceof DPSSecurityClientKey)
            {
                DPSSecurityClientKey dpsSecurityClientKey = (DPSSecurityClientKey) dpsSecurityClient;
                String ek = new String(Base64.encodeBase64Local(dpsSecurityClientKey.getDeviceEk()));
                System.out.println("Base64 encoded ek - " + ek);
                String srk = new String(Base64.encodeBase64Local(dpsSecurityClientKey.getDeviceSRK()));

                byte[] payload = new RegisterRequestParser(registrationId, ek, srk).toJson().getBytes();
                SSLContext sslContext = dpsSecurityClientKey.getSSLContext();
                provisioningDeviceClientAuthorization.setSslContext(sslContext);
                ResponseData dpsRegistrationData = new ResponseData();
                this.provisioningDeviceClientContract.requestNonceWithDPSTPM(payload, registrationId, sslContext, dpsAuthorizationCallback, dpsRegistrationData);

                while (dpsRegistrationData.responseData == null || dpsRegistrationData.dpsRegistrationState != DPS_REGISTRATION_RECEIVED)
                {
                    Thread.sleep(WAIT_FOR_REGISTRATION_RESPONSE);
                }
                if (dpsRegistrationData.responseData != null && dpsRegistrationData.dpsRegistrationState == DPS_REGISTRATION_RECEIVED)
                {
                    RegisterResponseTPMParser registerResponseTPMParser = RegisterResponseTPMParser.createFromJson(new String(dpsRegistrationData.responseData));
                    if (registerResponseTPMParser.getAuthenticationKey() != null)
                    {
                        System.out.println("Auth key received as " + registerResponseTPMParser.getAuthenticationKey());
                        dpsSecurityClientKey.importKey(Base64.decodeBase64Local(registerResponseTPMParser.getAuthenticationKey().getBytes()));
                        // construct sas-token signing with <idscope/registration/regid> {url encoded} /expirytime and retrieve the sastoken
                        String sasToken = this.constructSasToken(registrationId, DEFAULT_EXPIRY_TIME);
                        System.out.println("SasToken - " + sasToken);
                        ResponseData dpsRegistrationDataAuthorization = new ResponseData();
                        this.provisioningDeviceClientContract.authenticateWithDPS(payload, registrationId, sslContext, sasToken, dpsAuthorizationCallback, dpsRegistrationDataAuthorization);
                        while (dpsRegistrationDataAuthorization.responseData == null || dpsRegistrationDataAuthorization.dpsRegistrationState != DPS_REGISTRATION_RECEIVED)
                        {
                            Thread.sleep(WAIT_FOR_REGISTRATION_RESPONSE);
                        }
                        if (dpsRegistrationDataAuthorization.responseData != null && dpsRegistrationDataAuthorization.dpsRegistrationState == DPS_REGISTRATION_RECEIVED)
                        {
                            this.provisioningDeviceClientAuthorization.setSasToken(sasToken);
                            return ResponseParser.createFromJson(new String(dpsRegistrationDataAuthorization.responseData));
                        }
                        else
                        {
                            throw new ProvisioningDeviceHubException("DPS hub did not authorize SasToken");
                        }
                    }
                    else
                    {
                        throw new ProvisioningDeviceHubException("DPS hub did not send authentication key");
                    }
                }
                else
                {
                    throw new ProvisioningDeviceClientException("Did not receive DPS registration successfully");
                }

            }
            else
            {
                throw new ProvisioningDeviceSecurityException("Unknown security type received");
            }
        }
        catch (IOException | InterruptedException e)
        {
            throw new ProvisioningDeviceClientException(e.getMessage());
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
            throw new ProvisioningDeviceSecurityException("Unknown Security type received");
        }
    }

    // consider changing this to transport calls
    @Override
    public ResponseParser call() throws Exception
    {
        return this.authenticateWithDPS();
    }
}
