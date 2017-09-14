/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.dps.device.privateapi.dpstask;

import com.microsoft.azure.sdk.iot.dps.device.privateapi.dpsparser.DPSResponseParser;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.DPSAuthorization;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.DPSTransport;
import com.microsoft.azure.sdk.iot.dps.device.privateapi.exceptions.DPSClientException;
import com.microsoft.azure.sdk.iot.dps.security.DPSSecurityClient;

import javax.net.ssl.SSLContext;
import java.util.concurrent.Callable;

import static com.microsoft.azure.sdk.iot.dps.device.privateapi.dpstask.DPSRestState.DPS_REGISTRATION_RECEIVED;

public class DPSStatusTask implements Callable
{
    private static final int WAIT_FOR_STATUS_RESPONSE = 100;
    private DPSTransport dpsTransport;
    private DPSSecurityClient dpsSecurityClient;
    private String operationId;
    private DPSAuthorization dpsAuthorization;

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

    DPSStatusTask(DPSSecurityClient dpsSecurityClient, DPSTransport dpsTransport, String operationId, DPSAuthorization dpsAuthorization) throws DPSClientException
    {
        if (dpsTransport == null)
        {
            throw new DPSClientException("transport cannot be null");
        }

        if (dpsSecurityClient == null)
        {
            throw new DPSClientException("security client cannot be null");
        }

        this.dpsSecurityClient = dpsSecurityClient;
        this.dpsTransport = dpsTransport;
        this.operationId = operationId;
        this.dpsAuthorization = dpsAuthorization;
    }

    public DPSResponseParser getRegistrationStatus(String operationId, DPSAuthorization dpsAuthorization) throws DPSClientException
    {
        try
        {
            String registrationId = this.dpsSecurityClient.getRegistrationId();
            DPSResponseParser dpsResponseParser = null;

            SSLContext sslContext = dpsAuthorization.getSslContext();
            DPSRestResponseData dpsRestResponseData = new DPSRestResponseData();
            dpsTransport.getRegistrationStatus(operationId, registrationId, dpsAuthorization.getSasToken(), dpsAuthorization.getSslContext(), new DPSRestResponseCallbackImpl(), dpsRestResponseData);
            while (dpsRestResponseData.responseData == null || dpsRestResponseData.dpsRegistrationState != DPS_REGISTRATION_RECEIVED)
            {
                Thread.sleep(WAIT_FOR_STATUS_RESPONSE);
            }
            if (dpsRestResponseData.responseData != null && dpsRestResponseData.dpsRegistrationState == DPS_REGISTRATION_RECEIVED)
            {
                dpsResponseParser = DPSResponseParser.createFromJson(new String(dpsRestResponseData.responseData));
                return dpsResponseParser;
            }
            else
            {
                throw new DPSClientException("Did not receive DPS Status information");
            }
        }
        catch (InterruptedException e)
        {
            throw new DPSClientException(e.getMessage());
        }
    }

    @Override
    public DPSResponseParser call() throws Exception
    {
        // To edit later to move all the DPS operations here from transport and replace with transport send, receive....
       return this.getRegistrationStatus(this.operationId, this.dpsAuthorization);
       // for tpm remember to extract sastoken out of auth key provided by service
    }
}
