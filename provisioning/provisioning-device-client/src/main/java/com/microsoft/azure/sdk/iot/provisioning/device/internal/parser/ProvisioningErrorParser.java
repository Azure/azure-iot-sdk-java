/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.parser;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class ProvisioningErrorParser
{
    private static final String ERROR_CODE = "errorCode";
    @SerializedName(ERROR_CODE)
    private int errorCode;

    private static final String TRACKING_ID = "trackingId";
    @SerializedName(TRACKING_ID)
    private String trackingId;

    private static final String MESSAGE = "message";
    @SerializedName(MESSAGE)
    private String message;

    private static final String INFO = "info";
    @SerializedName(INFO)
    private Map<String, String> info;

    //empty constructor for Gson
    ProvisioningErrorParser()
    {
    }

    public static ProvisioningErrorParser createFromJson(String json)
    {
        //Codes_SRS_PROVISIONING_ERROR_PARSER_34_001: [This function shall create a ProvisioningErrorParser instance from the provided json]
        return new GsonBuilder().create().fromJson(json, ProvisioningErrorParser.class);
    }

    /**
     * Creates a descriptive error message based on the json that constructed this object
     * @return the error message to throw
     */
    public String getExceptionMessage()
    {
        //Codes_SRS_PROVISIONING_ERROR_PARSER_34_002: [This function shall return a string containing the saved error code, message, and tracking id]
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Service error: " + ((errorCode == 0) ? "null" : errorCode) + " - " + ((message == null) ? "null" : message) + " - TrackingId: " + ((trackingId == null) ? "null" : trackingId));
        errorMessage.append("\n");
        if (info != null)
        {
            for (String key : info.keySet())
            {
                errorMessage.append(key + " : " + info.get(key));
            }
        }

        return errorMessage.toString();
    }
}
