package com.microsoft.azure.sdk.iot.device.DeviceTwin;// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;

public class DeviceMethodMessage extends Message
{
    private String version;
    private String requestId;
    private String status;
    private DeviceOperations operationType;
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

    public void setVersion(String version)
    {
        /*
        **Codes_SRS_DEVICETWINMESSAGE_25_003: [**The function shall set the version.**]**
         */
        this.version = version;
    }

    public String getVersion()
    {
        /*
        **Codes_SRS_DEVICETWINMESSAGE_25_004: [**The function shall return the value of the version either set by the setter or the default (null) if unset so far.**]**
         */
        return  this.version;
    }

    public void setRequestId(String id)
    {
        /*
        **Codes_SRS_DEVICETWINMESSAGE_25_005: [**The function shall save the request id.**]**
         */
        this.requestId = id;
    }

    public String getRequestId()
    {
        /*
        **Codes_SRS_DEVICETWINMESSAGE_25_006: [**The function shall return the value of the request id either set by the setter or the default (null) if unset so far.**]**
         */
        return this.requestId;
    }

    public void setStatus(String status)
    {
        /*
        **Codes_SRS_DEVICETWINMESSAGE_25_007: [**The function shall save the status.**]**
         */
        this.status = status;
    }

    public String getStatus()
    {
        /*
        **Codes_SRS_DEVICETWINMESSAGE_25_008: [**The function shall return the value of the status either set by the setter or the default (null) if unset so far.**]**
         */
        return this.status;
    }

    public void setDeviceOperationType(DeviceOperations type)
    {
        /*
        **Codes_SRS_DEVICETWINMESSAGE_25_009: [**The function shall save the device twin operation type.**]**
         */
        this.operationType = type;
    }

    public DeviceOperations getDeviceOperationType()
    {
        /*
        **Codes_SRS_DEVICETWINMESSAGE_25_010: [**The function shall return the operation type either set by the setter or the default (DEVICE_OPERATION_UNKNOWN) if unset so far.**]**
         */
        return this.operationType;
    }
}
