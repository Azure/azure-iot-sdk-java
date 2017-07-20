// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * Representation of a single error message collection with a Json deserializer.
 */
public class ErrorMessageParser
{
    static class ErrorMessage
    {
        @SerializedName("Message")
        String message;
        @SerializedName("ExceptionMessage")
        String exception;
    }

    /**
     * Parse the fullErrorMessage from the IoTHub to find the root cause
     * of the problem, after that, return a string with the best information
     * about the issue
     *
     * @param fullErrorMessage is the full error message from the IoTHub
     * @return a String with the best information extracted from the error message
     */
    public static String bestErrorMessage(String fullErrorMessage)
    {
        /* Codes_SRS_ERROR_MESSAGE_PARSER_21_008: [If the fullErrorMessage is null or empty, the bestErrorMessage shall return an empty String.] */
        if((fullErrorMessage == null) || fullErrorMessage.isEmpty())
        {
            return "";
        }

        Gson gson = new GsonBuilder().create();

        String rootMessage = fullErrorMessage;
        String rootException = null;
        ErrorMessage subMessage;
        /* Codes_SRS_ERROR_MESSAGE_PARSER_21_001: [The bestErrorMessage shall parse the fullErrorMessage as json with format {"Message":"ErrorCode:[error]","ExceptionMessage":"Tracking ID:[tracking id]-TimeStamp:[dateTime]"}.] */
        try
        {
            subMessage = gson.fromJson(rootMessage, ErrorMessage.class);
        }
        catch (JsonSyntaxException e)
        {
            /* Codes_SRS_ERROR_MESSAGE_PARSER_21_002: [If the bestErrorMessage failed to parse the fullErrorMessage as json, it shall return the fullErrorMessage as is.] */
            subMessage = new ErrorMessage();
        }

        /* Codes_SRS_ERROR_MESSAGE_PARSER_21_003: [If the fullErrorMessage contains inner Messages, the bestErrorMessage shall parse the inner message.] */
        while((subMessage != null) && (subMessage.message != null))
        {
            rootMessage = subMessage.message;
            /* Codes_SRS_ERROR_MESSAGE_PARSER_21_007: [If the inner message do not have rootException, the bestErrorMessage shall use the parent rootException.] */
            if(subMessage.exception != null)
            {
                rootException = subMessage.exception;
            }
            try
            {
                /* Codes_SRS_ERROR_MESSAGE_PARSER_21_004: [The bestErrorMessage shall use the most inner message as the root cause.] */
                subMessage = gson.fromJson(rootMessage.substring(rootMessage.indexOf('{')), ErrorMessage.class);
            }
            catch (StringIndexOutOfBoundsException | JsonSyntaxException e)
            {
                break;
            }
        }

        /* Codes_SRS_ERROR_MESSAGE_PARSER_21_005: [The bestErrorMessage shall return a String with the rootMessage and rootException.] */
        /* Codes_SRS_ERROR_MESSAGE_PARSER_21_006: [If the fullErrorMessage do not have rootException, the bestErrorMessage shall return only the rootMessage.] */
        if(rootException != null)
        {
            rootMessage = rootMessage + " " + rootException;
        }

        return rootMessage;
    }
}
