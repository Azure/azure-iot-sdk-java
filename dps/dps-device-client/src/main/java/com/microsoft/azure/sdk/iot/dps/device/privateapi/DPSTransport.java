/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.dps.device.privateapi;

import com.microsoft.azure.sdk.iot.dps.device.DPSConfig;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.dpstask.DPSRestResponseCallback;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.exceptions.DPSClientException;
import com.microsoft.azure.sdk.iot.dps.device.DPSTransportProtocol;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.exceptions.DPSHubException;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.exceptions.DPSTransportException;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.transport.http.DpsRestAPIHttp;

import javax.net.ssl.SSLContext;

public abstract class DPSTransport
{
    DPSTransportProtocol protocol;
    public static DPSTransport createDPSTransport(DPSConfig dpsConfig) throws DPSTransportException
    {
        if (dpsConfig.getProtocol() == DPSTransportProtocol.HTTPS)
        {
            return new DpsRestAPIHttp(dpsConfig.getDpsScopeId(), dpsConfig.getDpsURI());
        }

        return null;
    }

    public abstract void requestNonceWithDPSTPM(byte[] payload, String registrationId, SSLContext sslContext, DPSRestResponseCallback dpsRestResponseCallback, Object dpsAuthorizationCallbackContext) throws DPSClientException, DPSTransportException, DPSHubException;
    public abstract void authenticateWithDPS(byte[] payload, String registrationId, SSLContext sslContext, String authorization, DPSRestResponseCallback dpsRestResponseCallback, Object dpsAuthorizationCallbackContext) throws DPSClientException, DPSTransportException, DPSHubException;
    public abstract void getRegistrationStatus(String operationId, String registrationId, String dpsAuthorization, SSLContext sslContext, DPSRestResponseCallback dpsRestResponseCallback, Object dpsAuthorizationCallbackContext) throws DPSClientException, DPSTransportException, DPSHubException;

}
