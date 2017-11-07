/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.contract;

import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.http.ContractAPIHttp;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceConnectionException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.RequestData;

public abstract class ProvisioningDeviceClientContract
{
    /**
     * Static method to create contracts with the service over the specified protocol
     *
     * @param provisioningDeviceClientConfig@return Implementation of the relevant contract for the requested protocol
     * @throws ProvisioningDeviceClientException This exception is thrown if the contract implementation could not be instantiated.
     */
    public static ProvisioningDeviceClientContract createProvisioningContract(ProvisioningDeviceClientConfig provisioningDeviceClientConfig) throws ProvisioningDeviceClientException
    {
        if (provisioningDeviceClientConfig == null)
        {
            throw new ProvisioningDeviceClientException("config cannot be null");
        }
        switch (provisioningDeviceClientConfig.getProtocol())
        {
            case MQTT:
                return null;

            case MQTT_WS:
                return null;

            case AMQPS:
                return null;

            case AMQPS_WS:
                return null;

            case HTTPS:
                return new ContractAPIHttp(provisioningDeviceClientConfig.getScopeId(), provisioningDeviceClientConfig.getProvisioningServiceGlobalEndpoint());

            default:
                throw new ProvisioningDeviceClientException("Unknown protocol");
        }
    }

    public abstract void open(RequestData requestData) throws ProvisioningDeviceConnectionException;
    public abstract void requestNonceForTPM(RequestData requestData, ResponseCallback responseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException;
    public abstract void authenticateWithProvisioningService(RequestData requestData, ResponseCallback responseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException;
    public abstract void getRegistrationStatus(RequestData requestData, ResponseCallback responseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException;
    public abstract void close() throws ProvisioningDeviceConnectionException;
}
