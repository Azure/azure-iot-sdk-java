/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.hsm.parser;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Json parser for the response received from an HSM unit upon a failed sign request
 */
public class ErrorResponse
{
    private static final String MESSAGE_NAME = "message";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(MESSAGE_NAME)
    private String message;

    public static ErrorResponse fromJson(String json)
    {
        return new GsonBuilder().create().fromJson(json, ErrorResponse.class);
    }

    public ErrorResponse()
    {
        //empty constructor for gson
    }

    /**
     * Get the message
     * @return the message
     */
    public String getMessage()
    {
        // Codes_SRS_HTTPHSMERRORRESPONSE_34_001: [This function shall return the saved message.]
        return this.message;
    }
}
