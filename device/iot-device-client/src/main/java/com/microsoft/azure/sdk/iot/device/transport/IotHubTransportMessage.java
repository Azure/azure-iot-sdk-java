// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.IotHubMethod;
import com.microsoft.azure.sdk.iot.device.Message;

/**
 * Extends Message, adding transport artifacts.
 */
public class IotHubTransportMessage extends Message
{
    /// <summary>
    /// [Required in IoTHubTransportManager] Used to specify the method invoked for the message (POST, GET).
    /// </summary>
    private IotHubMethod iotHubMethod;

    /// <summary>
    /// [Required in IoTHubTransportManager] Used to specify the URI path of this message.
    /// </summary>
    private String uriPath;

    /**
     * Constructor.
     * @param body The body of the new Message instance. It is internally serialized to a byte array using UTF-8 encoding.
     */
    public IotHubTransportMessage(String body)
    {
        /* Codes_SRS_IOTHUBTRANSPORTMESSAGE_21_001: [The constructor shall call the supper class with the body. This function do not evaluates this parameter.] */
        super(body);
    }

    /**
     * Setter for the IoT Hub method
     * @param iotHubMethod The enum containing the IoT Hub method.
     */
    public void setIotHubMethod(IotHubMethod iotHubMethod)
    {
        /* Codes_SRS_IOTHUBTRANSPORTMESSAGE_21_002: [The setIotHubMethod shall store the iotHubMethod. This function do not evaluates this parameter.] */
        this.iotHubMethod = iotHubMethod;
    }

    /**
     * Setter for the URI path
     * @param uriPath The string with the URI path.
     */
    public void setUriPath(String uriPath)
    {
        /* Codes_SRS_IOTHUBTRANSPORTMESSAGE_21_003: [The setUriPath shall store the uriPath. This function do not evaluates this parameter.] */
        this.uriPath = uriPath;
    }

    /**
     * Getter for the IoT Hub method
     * @return the IoT Hub method (POST, GET).
     */
    public IotHubMethod getIotHubMethod()
    {
        /* Codes_SRS_IOTHUBTRANSPORTMESSAGE_21_004: [The getIotHubMethod shall return the stored iotHubMethod.] */
        return this.iotHubMethod;
    }

    /**
     * Getter for the URI path
     * @return the string with the URI path.
     */
    public String getUriPath()
    {
        /* Codes_SRS_IOTHUBTRANSPORTMESSAGE_21_005: [The getUriPath shall return the stored uriPath.] */
        return uriPath;
    }
}
