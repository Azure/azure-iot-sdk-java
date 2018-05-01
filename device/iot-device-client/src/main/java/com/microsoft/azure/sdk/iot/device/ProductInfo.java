/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;

/**
 * Represents the complete user agent string to be included in all D2C communication.
 */
public class ProductInfo
{
    private String extra;

    public ProductInfo()
    {
        //Codes_SRS_PRODUCTINFO_34_001: [This function shall set the extra field to an empty string.]
        this.extra = "";
    }

    public String getUserAgentString()
    {
        if (this.extra == null || this.extra.equals(""))
        {
            //Codes_SRS_PRODUCTINFO_34_002: [If the saved extra field is an empty string, this function shall return just the user agent string defined in TransportUtils.]
            return TransportUtils.USER_AGENT_STRING;
        }

        //Codes_SRS_PRODUCTINFO_34_003: [If the saved extra field is not an empty string, this function shall return the user agent string defined in TransportUtils with the extra string appended.]
        return TransportUtils.USER_AGENT_STRING + " " + this.extra;
    }

    /**
     * Sets the extra text to be included in the user agent string
     * @param extra the extra text to be included in the user agent string
     */
    public void setExtra(String extra)
    {
        //Codes_SRS_PRODUCTINFO_34_004: [This function shall save the provided extra.]
        this.extra = extra;
    }
}
