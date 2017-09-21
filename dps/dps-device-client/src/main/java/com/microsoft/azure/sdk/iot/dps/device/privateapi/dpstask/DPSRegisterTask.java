/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.dps.device.privateapi.dpstask;

import com.microsoft.azure.sdk.iot.dps.device.privateapi.dpsparser.DPSRegisterRequestParser;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.dpsparser.DPSRegisterResponseTPMParser;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.dpsparser.DPSResponseParser;
import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.dps.device.DPSConfig;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.DPSAuthorization;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.DPSGenerateUrl;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.DPSTransport;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.exceptions.DPSAuthenticationException;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.exceptions.DPSClientException;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.exceptions.DPSHubException;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.exceptions.DPSSecurityException;
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

import static com.microsoft.azure.sdk.iot.dps.device.privateapi.dpstask.DPSRestState.DPS_REGISTRATION_RECEIVED;

public class DPSRegisterTask implements Callable
{
    private static final int WAIT_FOR_REGISTRATION_RESPONSE = 100;
    private static final int DEFAULT_EXPIRY_TIME = 3600; // 1 Hour
    private DPSRestResponseCallbackImpl dpsAuthorizationCallback = null;
    private DPSTransport dpsTransport = null;
    private DPSAuthorization dpsAuthorization = null;
    private DPSSecurityClient dpsSecurityClient = null;
    private DPSConfig dpsConfig = null;

    class DPSRestResponseCallbackImpl implements DPSRestResponseCallback
    {
        @Override
        public void run(byte[] responseData, Object context) throws DPSClientException
        {
            if (context instanceof DPSRestResponseData)
            {
                DPSRestResponseData data = (DPSRestResponseData) context;
                data.responseData = responseData;
                data.dpsRegistrationState = DPS_REGISTRATION_RECEIVED;
            }
            else
            {
                throw new DPSClientException("Context mismatch for DPS registration");
            }
        }
    }

    // consider to use auth call back if needed
    DPSRegisterTask(DPSConfig dpsConfig, DPSSecurityClient dpsSecurityClient, DPSTransport dpsTransport, DPSAuthorization dpsAuthorization) throws DPSClientException
    {
        if (dpsTransport == null)
        {
            throw new DPSClientException("transport cannot be null");
        }

        if (dpsSecurityClient == null)
        {
            throw new DPSClientException("security client cannot be null");
        }

        if (dpsConfig == null)
        {
            throw new DPSClientException("dps config cannot be null");
        }

        this.dpsConfig = dpsConfig;
        this.dpsSecurityClient = dpsSecurityClient;
        this.dpsTransport = dpsTransport;
        this.dpsAuthorization = dpsAuthorization; // this may not work fix the broken link with dice/tpm
        this.dpsAuthorizationCallback = new DPSRestResponseCallbackImpl();
    }

    private DPSResponseParser authenticateWithX509(String registrationId) throws DPSClientException
    {
        try
        {
            if (dpsSecurityClient.getSecurityType() == SecurityType.X509)
            {
                byte[] payload = new DPSRegisterRequestParser(registrationId).toJson().getBytes();
                if (dpsSecurityClient instanceof DPSSecurityClientX509)
                {
                    DPSSecurityClientX509 dpsSecurityClientX509 = (DPSSecurityClientX509) dpsSecurityClient;
                    SSLContext sslContext = dpsSecurityClientX509.getSSLContext();
                    dpsAuthorization.setSslContext(sslContext);
                    DPSRestResponseData dpsRegistrationData = new DPSRestResponseData();
                    this.dpsTransport.authenticateWithDPS(payload, registrationId, sslContext, null, dpsAuthorizationCallback, dpsRegistrationData);
                    while (dpsRegistrationData.responseData == null || dpsRegistrationData.dpsRegistrationState != DPS_REGISTRATION_RECEIVED)
                    {
                        Thread.sleep(WAIT_FOR_REGISTRATION_RESPONSE);
                    }
                    if (dpsRegistrationData.responseData != null && dpsRegistrationData.dpsRegistrationState == DPS_REGISTRATION_RECEIVED)
                    {
                        return DPSResponseParser.createFromJson(new String(dpsRegistrationData.responseData));
                    }
                    else
                    {
                        throw new DPSClientException("Did not receive DPS registration successfully");
                    }
                }
                else
                {
                    throw new DPSAuthenticationException("Unknown security type received");
                }
            }
            else
            {
                throw new DPSAuthenticationException("Unknown security type received");
            }
        }
        catch (InterruptedException e)
        {
            throw new DPSClientException(e.getMessage());
        }
    }

    private String constructSasToken(String registrationId, int expiryTime) throws DPSSecurityException, UnsupportedEncodingException
    {
        //"SharedAccessSignature sr=%s&sig=%s&se=%s&skn=", token_scope, STRING_c_str(urlEncodedSignature), expire_token);
        if (expiryTime <= 0)
        {
            throw new IllegalArgumentException("expiry time cannot be negative or zero");
        }
        String tokenScope = new DPSGenerateUrl(dpsConfig.getDpsScopeId()).generateSasTokenUrl(registrationId);
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
            throw new DPSSecurityException("Unknown security type received");
        }
    }

    private DPSResponseParser authenticateWithSasToken(String registrationId) throws DPSClientException
    {
        try
        {
            if (dpsSecurityClient instanceof DPSSecurityClientKey)
            {
                DPSSecurityClientKey dpsSecurityClientKey = (DPSSecurityClientKey) dpsSecurityClient;
                String ek = new String(Base64.encodeBase64Local(dpsSecurityClientKey.getDeviceEk()));
                System.out.println("Base64 encoded ek - " + ek);
                String srk = new String(Base64.encodeBase64Local(dpsSecurityClientKey.getDeviceSRK()));

                byte[] payload = new DPSRegisterRequestParser(registrationId, ek, srk).toJson().getBytes();
                SSLContext sslContext = dpsSecurityClientKey.getSSLContext();
                dpsAuthorization.setSslContext(sslContext);
                DPSRestResponseData dpsRegistrationData = new DPSRestResponseData();
                this.dpsTransport.requestNonceWithDPSTPM(payload, registrationId, sslContext, dpsAuthorizationCallback, dpsRegistrationData);

                while (dpsRegistrationData.responseData == null || dpsRegistrationData.dpsRegistrationState != DPS_REGISTRATION_RECEIVED)
                {
                    Thread.sleep(WAIT_FOR_REGISTRATION_RESPONSE);
                }
                if (dpsRegistrationData.responseData != null && dpsRegistrationData.dpsRegistrationState == DPS_REGISTRATION_RECEIVED)
                {
                    DPSRegisterResponseTPMParser dpsRegisterResponseTPMParser = DPSRegisterResponseTPMParser.createFromJson(new String(dpsRegistrationData.responseData));
                    if (dpsRegisterResponseTPMParser.getAuthenticationKey() != null)
                    {
                        System.out.println("Auth key received as " + dpsRegisterResponseTPMParser.getAuthenticationKey());
                        dpsSecurityClientKey.importKey(Base64.decodeBase64Local(dpsRegisterResponseTPMParser.getAuthenticationKey().getBytes()));
                        // construct sas-token signing with <idscope/registration/regid> {url encoded} /expirytime and retrieve the sastoken
                        String sasToken = this.constructSasToken(registrationId, DEFAULT_EXPIRY_TIME);
                        System.out.println("SasToken - " + sasToken);
                        DPSRestResponseData dpsRegistrationDataAuthorization = new DPSRestResponseData();
                        this.dpsTransport.authenticateWithDPS(payload, registrationId, sslContext, sasToken, dpsAuthorizationCallback, dpsRegistrationDataAuthorization);
                        while (dpsRegistrationDataAuthorization.responseData == null || dpsRegistrationDataAuthorization.dpsRegistrationState != DPS_REGISTRATION_RECEIVED)
                        {
                            Thread.sleep(WAIT_FOR_REGISTRATION_RESPONSE);
                        }
                        if (dpsRegistrationDataAuthorization.responseData != null && dpsRegistrationDataAuthorization.dpsRegistrationState == DPS_REGISTRATION_RECEIVED)
                        {
                            this.dpsAuthorization.setSasToken(sasToken);
                            return DPSResponseParser.createFromJson(new String(dpsRegistrationDataAuthorization.responseData));
                        }
                        else
                        {
                            throw new DPSHubException("DPS hub did not authorize SasToken");
                        }
                    }
                    else
                    {
                        throw new DPSHubException("DPS hub did not send authentication key");
                    }
                }
                else
                {
                    throw new DPSClientException("Did not receive DPS registration successfully");
                }

            }
            else
            {
                throw new DPSSecurityException("Unknown security type received");
            }
        }
        catch (IOException | InterruptedException e)
        {
            throw new DPSClientException(e.getMessage());
        }
    }

    private DPSResponseParser authenticateWithDPS() throws DPSClientException
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
            throw new DPSSecurityException("Unknown Security type received");
        }
    }

    // consider changing this to transport calls
    @Override
    public DPSResponseParser call() throws Exception
    {
        return this.authenticateWithDPS();
    }
}
