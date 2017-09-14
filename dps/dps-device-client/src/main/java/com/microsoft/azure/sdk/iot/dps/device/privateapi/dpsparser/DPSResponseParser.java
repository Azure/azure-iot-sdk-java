/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.dps.device.privateapi.dpsparser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

public class DPSResponseParser
{
    private static final String OPERATION_ID = "operationId";
    @SerializedName(OPERATION_ID)
    private String operationId;

    private static final String STATUS = "status";
    @SerializedName(STATUS)
    private String status;

    private static final String REGISTRATION_STATUS = "registrationStatus";
    @SerializedName(REGISTRATION_STATUS)
    private DPSOperationRegistrationStatusParser registrationStatus;

    private DPSResponseParser()
    {
    }

    public static DPSResponseParser createFromJson(String json) throws IllegalArgumentException, JsonParseException
    {
        if((json == null) || json.isEmpty())
        {
            throw new IllegalArgumentException("Json is null or empty");
        }

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        DPSResponseParser dpsResponseParser = null;

        try
        {
            dpsResponseParser = gson.fromJson(json, DPSResponseParser.class);
        }
        catch (JsonSyntaxException malformed)
        {
            throw new IllegalArgumentException("Malformed json:" + malformed);
        }

        if (dpsResponseParser.operationId == null || dpsResponseParser.status == null)
        {
            throw new IllegalArgumentException("Json does not contain Operation Id or Status");
        }

        return dpsResponseParser;
    }


    public String getOperationId()
    {
        return operationId;
    }

    public String getStatus()
    {
        return status;
    }

    public DPSOperationRegistrationStatusParser getRegistrationStatus()
    {
        return registrationStatus;
    }
}
