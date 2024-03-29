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
@SuppressWarnings("unused") // A number of private members are unused but may be filled in or used by serialization
public class ErrorResponse
{
    private static final String MESSAGE_NAME = "message";
    @Expose
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
        return this.message;
    }
}
