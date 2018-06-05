/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.auth;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class HttpHsmErrorResponse
{
    private static final String MESSAGE_NAME = "message";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(MESSAGE_NAME)
    private String message;

    public static HttpHsmErrorResponse fromJson(String json)
    {
        return new GsonBuilder().create().fromJson(json, HttpHsmErrorResponse.class);
    }

    public String getMessage()
    {
        // Codes_SRS_HTTPHSMERRORRESPONSE_34_001: [This function shall return the saved message.]
        return this.message;
    }
}
