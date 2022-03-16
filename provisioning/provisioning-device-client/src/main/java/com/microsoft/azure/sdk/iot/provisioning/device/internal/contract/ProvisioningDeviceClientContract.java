/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.contract;

import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.http.ContractAPIHttp;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.amqp.ContractAPIAmqp;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.mqtt.ContractAPIMqtt;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceConnectionException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.RequestData;

public abstract class ProvisioningDeviceClientContract
{
    private int retryValue = DEFAULT_RETRY_AFTER_VALUE;
    protected static final String RETRY_AFTER = "retry-after";
    private static final Integer DEFAULT_RETRY_AFTER_VALUE = 2;
    private static final Integer MAX_PROV_GET_THROTTLE_TIME = 5;

    protected void setRetrieveRetryAfterValue(String protocolRetryValue)
    {
        if (protocolRetryValue != null && !protocolRetryValue.isEmpty())
        {
            retryValue = Integer.parseInt(protocolRetryValue);
            // ensure the value is between the tolerances
            if (retryValue < DEFAULT_RETRY_AFTER_VALUE || retryValue > MAX_PROV_GET_THROTTLE_TIME)
            {
                retryValue = DEFAULT_RETRY_AFTER_VALUE;
            }
        }
    }

    /**
     * Static method to create contracts with the service over the specified protocol
     * @param provisioningDeviceClientConfig Config used for provisioning
     * @return Implementation of the relevant contract for the requested protocol
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
                return new ContractAPIMqtt(provisioningDeviceClientConfig);

            case MQTT_WS:
                provisioningDeviceClientConfig.setUsingWebSocket(true);
                return new ContractAPIMqtt(provisioningDeviceClientConfig);

            case AMQPS:
                return new ContractAPIAmqp(provisioningDeviceClientConfig);

            case AMQPS_WS:
                provisioningDeviceClientConfig.setUsingWebSocket(true);
                return new ContractAPIAmqp(provisioningDeviceClientConfig);

            case HTTPS:
                return new ContractAPIHttp(provisioningDeviceClientConfig);

            default:
                throw new ProvisioningDeviceClientException("Unknown protocol");
        }
    }

    public abstract void open(RequestData requestData) throws ProvisioningDeviceConnectionException;
    public abstract void requestNonceForTPM(RequestData requestData, ResponseCallback responseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException;
    public abstract void authenticateWithProvisioningService(RequestData requestData, ResponseCallback responseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException;
    public abstract void getRegistrationStatus(RequestData requestData, ResponseCallback responseCallback, Object dpsAuthorizationCallbackContext) throws ProvisioningDeviceClientException;
    public abstract void close() throws ProvisioningDeviceConnectionException;
    public abstract String getConnectionId();
    public abstract String getHostName();

    /**
     * Method to get the DPS retry after value
     * @return integer value of the number of milliseconds to wait to call dps service
     */
    public int getRetryValue()
    {
        return this.retryValue*1000;
    }
}
