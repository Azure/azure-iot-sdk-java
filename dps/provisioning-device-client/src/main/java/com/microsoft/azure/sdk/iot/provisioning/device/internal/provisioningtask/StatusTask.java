/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.provisioningtask;

import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.ResponseParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientAuthorization;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.dps.security.DPSSecurityClient;

import javax.net.ssl.SSLContext;
import java.util.concurrent.Callable;

public class StatusTask implements Callable
{
    private static final int WAIT_FOR_STATUS_RESPONSE = 100;
    private ProvisioningDeviceClientContract provisioningDeviceClientContract;
    private DPSSecurityClient dpsSecurityClient;
    private String operationId;
    private ProvisioningDeviceClientAuthorization provisioningDeviceClientAuthorization;

    class ResponseCallbackImpl implements ResponseCallback
    {
        @Override
        public void run(byte[] responseData, Object context) throws ProvisioningDeviceClientException
        {
            if (context instanceof ResponseData)
            {
                ResponseData data = (ResponseData) context;
                data.responseData = responseData;
                data.dpsRegistrationState = ContractState.DPS_REGISTRATION_RECEIVED;
            }
            else
            {
                throw new ProvisioningDeviceClientException("Context mismatch for DPS registration");
            }
        }
    }

    StatusTask(DPSSecurityClient dpsSecurityClient, ProvisioningDeviceClientContract provisioningDeviceClientContract, String operationId, ProvisioningDeviceClientAuthorization provisioningDeviceClientAuthorization) throws ProvisioningDeviceClientException
    {
        if (provisioningDeviceClientContract == null)
        {
            throw new ProvisioningDeviceClientException("transport cannot be null");
        }

        if (dpsSecurityClient == null)
        {
            throw new ProvisioningDeviceClientException("security client cannot be null");
        }

        this.dpsSecurityClient = dpsSecurityClient;
        this.provisioningDeviceClientContract = provisioningDeviceClientContract;
        this.operationId = operationId;
        this.provisioningDeviceClientAuthorization = provisioningDeviceClientAuthorization;
    }

    public ResponseParser getRegistrationStatus(String operationId, ProvisioningDeviceClientAuthorization provisioningDeviceClientAuthorization) throws ProvisioningDeviceClientException
    {
        try
        {
            String registrationId = this.dpsSecurityClient.getRegistrationId();
            ResponseParser responseParser = null;

            SSLContext sslContext = provisioningDeviceClientAuthorization.getSslContext();
            ResponseData responseData = new ResponseData();
            provisioningDeviceClientContract.getRegistrationStatus(operationId, registrationId, provisioningDeviceClientAuthorization.getSasToken(), provisioningDeviceClientAuthorization.getSslContext(), new ResponseCallbackImpl(), responseData);
            while (responseData.responseData == null || responseData.dpsRegistrationState != ContractState.DPS_REGISTRATION_RECEIVED)
            {
                Thread.sleep(WAIT_FOR_STATUS_RESPONSE);
            }
            if (responseData.responseData != null && responseData.dpsRegistrationState == ContractState.DPS_REGISTRATION_RECEIVED)
            {
                responseParser = ResponseParser.createFromJson(new String(responseData.responseData));
                return responseParser;
            }
            else
            {
                throw new ProvisioningDeviceClientException("Did not receive DPS Status information");
            }
        }
        catch (InterruptedException e)
        {
            throw new ProvisioningDeviceClientException(e.getMessage());
        }
    }

    @Override
    public ResponseParser call() throws Exception
    {
        // To edit later to move all the DPS operations here from transport and replace with transport send, receive....
       return this.getRegistrationStatus(this.operationId, this.provisioningDeviceClientAuthorization);
       // for tpm remember to extract sastoken out of auth key provided by service
    }
}
