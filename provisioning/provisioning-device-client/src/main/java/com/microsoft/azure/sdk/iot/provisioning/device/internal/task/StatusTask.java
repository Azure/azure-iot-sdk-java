/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.task;

import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.RegistrationOperationStatusParser;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceSecurityException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityClientException;

import javax.net.ssl.SSLContext;
import java.util.concurrent.Callable;

public class StatusTask implements Callable
{
    private static final int MAX_WAIT_FOR_STATUS_RESPONSE = 100;
    private ProvisioningDeviceClientContract provisioningDeviceClientContract;
    private SecurityProvider securityProvider;
    private String operationId;
    private Authorization authorization;

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
                throw new ProvisioningDeviceClientException("Context mismatch for DPS registration");
            }
        }
    }

    /**
     * Task to query Status information from the service
     * @param securityProvider security client for the HSM on which this device is registering on. Cannot be {@code null}
     * @param provisioningDeviceClientContract Contract of the transport with the lower layers. Cannot be {@code null}
     * @param operationId Id retrieved from the service.  Cannot be {@code null} or empty
     * @param authorization Object holding auth info.  Cannot be {@code null}
     * @throws ProvisioningDeviceClientException is thrown if any of the parameters are invalid.
     */
    StatusTask(SecurityProvider securityProvider, ProvisioningDeviceClientContract provisioningDeviceClientContract,
               String operationId, Authorization authorization) throws ProvisioningDeviceClientException
    {
        //SRS_StatusTask_25_002: [ Constructor shall throw ProvisioningDeviceClientException if operationId , securityProvider, authorization or provisioningDeviceClientContract is null. ]
        if (provisioningDeviceClientContract == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("provisioningDeviceClientContract cannot be null"));
        }

        if (securityProvider == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("security client cannot be null"));
        }

        if (operationId == null || operationId.isEmpty())
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("operationId cannot be null or empty"));
        }

        if (authorization == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("authorization cannot be null"));
        }

        //SRS_StatusTask_25_001: [ Constructor shall save operationId , securityProvider, provisioningDeviceClientContract and authorization. ]
        this.securityProvider = securityProvider;
        this.provisioningDeviceClientContract = provisioningDeviceClientContract;
        this.operationId = operationId;
        this.authorization = authorization;
    }

    private RegistrationOperationStatusParser getRegistrationStatus(String operationId, Authorization authorization) throws ProvisioningDeviceClientException
    {
        try
        {
            //SRS_StatusTask_25_003: [ This method shall throw ProvisioningDeviceClientException if registration id is null or empty. ]
            String registrationId = this.securityProvider.getRegistrationId();
            if (registrationId == null || registrationId.isEmpty())
            {
                throw new ProvisioningDeviceSecurityException("registrationId cannot be null or empty");
            }

            //SRS_StatusTask_25_004: [ This method shall retrieve the SSL context from Authorization and throw ProvisioningDeviceClientException if it is null. ]
            SSLContext sslContext = authorization.getSslContext();
            if (sslContext == null)
            {
                throw new ProvisioningDeviceSecurityException("SSL context cannot be null");
            }

            RequestData requestData = new RequestData( registrationId, operationId, authorization.getSslContext(), authorization.getSasToken());
            //SRS_StatusTask_25_005: [ This method shall trigger getRegistrationState on the contract API and wait for response and return it. ]
            ResponseData responseData = new ResponseData();
            provisioningDeviceClientContract.getRegistrationStatus(requestData, new ResponseCallbackImpl(), responseData);
            if (responseData.getResponseData() == null || responseData.getContractState() != ContractState.DPS_REGISTRATION_RECEIVED)
            {
                Thread.sleep(MAX_WAIT_FOR_STATUS_RESPONSE);
            }
            if (responseData.getResponseData() != null && responseData.getContractState() == ContractState.DPS_REGISTRATION_RECEIVED)
            {
                return RegistrationOperationStatusParser.createFromJson(new String(responseData.getResponseData()));
            }
            else
            {
                //SRS_StatusTask_25_006: [ This method shall throw ProvisioningDeviceClientException if null response or no response is received in maximum time of 90 seconds. ]
                throw new ProvisioningDeviceClientException("Did not receive DPS Status information");
            }
        }
        catch (InterruptedException | SecurityClientException e)
        {
            throw new ProvisioningDeviceClientException(e);
        }
    }

    /**
     * Implementation of callable for this task. This task queries for status
     * with the service
     * @return RegistrationOperationStatusParser object holding the information received from service
     * @throws Exception If any of the underlying calls fail
     */
    @Override
    public RegistrationOperationStatusParser call() throws Exception
    {
       return this.getRegistrationStatus(this.operationId, this.authorization);
    }
}
