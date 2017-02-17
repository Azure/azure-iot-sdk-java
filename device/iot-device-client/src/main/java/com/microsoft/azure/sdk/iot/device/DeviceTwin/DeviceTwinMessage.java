// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.DeviceTwin;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;

import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceTwinOperations.DEVICE_TWIN_OPERATION_UNKNOWN;

public class DeviceTwinMessage extends Message
{
    private String version;
    private String requestId;
    private String status;
    private DeviceTwinOperations operationType;


  public DeviceTwinMessage(byte[] data)
  {
      /*
      **Codes_SRS_DEVICETWINMESSAGE_25_001: [**The constructor shall save the message body by calling super with the body as parameter.**]**
      **Codes_SRS_DEVICETWINMESSAGE_25_002: [**If the message body is null, the constructor shall throw an IllegalArgumentException thrown by base constructor.**]**
       */
      super(data);
      this.setMessageType(MessageType.DeviceTwin);
      this.version = null;
      this.requestId = null;
      this.status = null;
      this.operationType = DEVICE_TWIN_OPERATION_UNKNOWN;
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

    public void setDeviceTwinOperationType(DeviceTwinOperations type)
    {
        /*
        **Codes_SRS_DEVICETWINMESSAGE_25_009: [**The function shall save the device twin operation type.**]**
         */
        this.operationType = type;
    }

    public DeviceTwinOperations getDeviceTwinOperationType()
    {
        /*
        **Codes_SRS_DEVICETWINMESSAGE_25_010: [**The function shall return the operation type either set by the setter or the default (DEVICE_TWIN_OPERATION_UNKNOWN) if unset so far.**]**
         */
        return this.operationType;
    }

}
