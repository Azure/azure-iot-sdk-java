/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 *  Class for the representation of TpmRegistration
 *  https://docs.microsoft.com/en-us/rest/api/iot-dps/RuntimeRegistration/RegisterDevice#definitions_tpmregistrationresult
 */
public class TpmRegistrationResultParser
{
    private static final String AUTHENTICATION_KEY = "authenticationKey";
    @SerializedName(AUTHENTICATION_KEY)
    private String authenticationKey;

    //empty constructor for Gson
    TpmRegistrationResultParser()
    {
    }

    /**
     * Getter for the Authentication Key
     * @return Getter for the Authentication Key
     */
    public String getAuthenticationKey()
    {
        //SRS_TpmRegistrationResultParser_25_001: [ This method returns the authentication key. ]
        return authenticationKey;
    }

    /**
     * Creates the object TpmRegistrationResultParser if JSON Input is provided
     * @param json JSON input to be parsed
     * @return TpmRegistrationResultParser object
     * @throws IllegalArgumentException If JSON could not be parsed.
     */
    static public TpmRegistrationResultParser createFromJson(String json) throws IllegalArgumentException
    {
        if((json == null) || json.isEmpty())
        {
            //SRS_TpmRegistrationResultParser_25_002: [ The constructor shall throw IllegalArgumentException if the provided Json is null or empty. ]
            throw new IllegalArgumentException("JSON is null or empty");
        }

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        TpmRegistrationResultParser tpmRegistrationResultParserParser = null;

        try
        {
            //SRS_TpmRegistrationResultParser_25_003: [ The constructor shall build this object from the provided Json. ]
            tpmRegistrationResultParserParser = gson.fromJson(json, TpmRegistrationResultParser.class);
        }
        catch (JsonSyntaxException malformed)
        {
            //SRS_TpmRegistrationResultParser_25_004: [ The constructor shall throw IllegalArgumentException if the provided Json could not be parsed. ]
            throw new IllegalArgumentException("Malformed JSON", malformed);
        }

        return tpmRegistrationResultParserParser;
    }
}
