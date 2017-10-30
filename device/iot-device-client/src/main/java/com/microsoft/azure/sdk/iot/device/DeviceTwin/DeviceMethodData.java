// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.DeviceTwin;

public class DeviceMethodData
{
    private int status;
    private String responseMessage;

    public DeviceMethodData(int status, String responseMessage)
    {
        /*
        **Codes_SRS_DEVICEMETHODDATA_25_001: [**The constructor shall save the status and response message provided by user.**]**
         */
        this.status = status;
        this.responseMessage = responseMessage;
    }

    public int getStatus()
    {
        /*
        **Codes_SRS_DEVICEMETHODDATA_25_003: [**This method shall return the status previously set.**]**
         */
        return status;
    }

    public String getResponseMessage()
    {
        /*
        **Codes_SRS_DEVICEMETHODDATA_25_004: [**This method shall return the response message previously set.**]**
         */
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage)
    {
        /*
        **Codes_SRS_DEVICEMETHODDATA_25_005: [**This method shall save the response message provided by the user.**]**
         */
        this.responseMessage = responseMessage;
    }

    public void setStatus(int status)
    {
        /*
        **Codes_SRS_DEVICEMETHODDATA_25_007: [**The method shall set the status.**]**
         */
        this.status = status;
    }
}
