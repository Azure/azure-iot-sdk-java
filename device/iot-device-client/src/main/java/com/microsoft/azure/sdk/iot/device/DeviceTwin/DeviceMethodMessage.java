// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.DeviceTwin;

import com.microsoft.azure.sdk.iot.device.MessageType;

public class DeviceMethodMessage extends DeviceTwinMessage
{
    private String methodName;

    public DeviceMethodMessage(byte[] data)
    {
        /*
        **Codes_SRS_DEVICEMETHODMESSAGE_25_001: [**The constructor shall save the message body by calling super with the body as parameter.**]**
        **Codes_SRS_DEVICEMETHODMESSAGE_25_002: [**If the message body is null, the constructor shall throw an IllegalArgumentException thrown by base constructor.**]**
         */
        super(data);
        this.setMessageType(MessageType.DeviceMethods);
        this.methodName = null;
    }

    /*
    **Codes_SRS_DEVICEMETHODMESSAGE_25_003: [**This method shall set the methodName.**]**
     */
    public void setMethodName(String methodName)
    {
        if (methodName == null)
        {
            /*
            **Codes_SRS_DEVICEMETHODMESSAGE_25_004: [**This method shall throw IllegalArgumentException if the methodName is null.**]**
             */
            throw new IllegalArgumentException("Method name cannot be null");
        }
        this.methodName = methodName;
    }

    public String getMethodName()
    {
        /*
        **Codes_SRS_DEVICEMETHODMESSAGE_25_005: [**The method shall return the methodName either set by the setter or the default (null) if unset so far.**]**
         */
        return methodName;
    }
}
