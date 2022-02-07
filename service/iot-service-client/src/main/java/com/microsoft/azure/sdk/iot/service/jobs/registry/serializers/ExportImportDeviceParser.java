// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.jobs.registry.serializers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.service.registry.serializers.AuthenticationParser;
import com.microsoft.azure.sdk.iot.service.twin.TwinCollection;
import lombok.Getter;
import lombok.Setter;

public class ExportImportDeviceParser
{
    private static final String ID_NAME = "id";
    @Expose
    @SerializedName(ID_NAME)
    @Getter
    @Setter
    private String id;

    private static final String MODULE_ID_NAME = "moduleId";
    @Expose
    @SerializedName(MODULE_ID_NAME)
    @Getter
    @Setter
    private String moduleId;

    private static final String E_TAG_NAME = "eTag";
    @Expose
    @SerializedName(E_TAG_NAME)
    @Getter
    @Setter
    private String eTag;

    private static final String IMPORT_MODE_NAME = "importMode";
    @Expose
    @SerializedName(IMPORT_MODE_NAME)
    @Getter
    @Setter
    private String importMode;

    private static final String STATUS_NAME = "status";
    @Expose
    @SerializedName(STATUS_NAME)
    @Getter
    @Setter
    private String status;

    private static final String STATUS_REASON_NAME = "statusReason";
    @Expose
    @SerializedName(STATUS_REASON_NAME)
    @Getter
    @Setter
    private String statusReason;

    private static final String AUTHENTICATION_NAME = "authentication";
    @Expose
    @SerializedName(AUTHENTICATION_NAME)
    @Getter
    @Setter
    private AuthenticationParser authentication;
    
    private static final String TAGS_NAME = "tags";
    @Expose
    @SerializedName(TAGS_NAME)
    @Getter
    @Setter
    private TwinCollection tags;

    private final transient static Gson gson = new Gson();

    /**
     * Converts this into json and returns it
     * @return the json representation of this
     */
    public String toJson()
    {
        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_001: [The parser shall save the ExportImportDeviceParser's authentication type to the returned json representation]
        return gson.toJson(this);
    }

    /**
     * Empty constructor: Used only to keep GSON happy.
     */
    public ExportImportDeviceParser()
    {
    }

    /**
     * Constructor for an ExportImportDeviceParser that is built using the provided json
     * @param json the json string to build the ExportImportDeviceParser out of
     */
    public ExportImportDeviceParser(String json)
    {
        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_011: [If the provided json is null, empty, or cannot be parsed into an ExportImportDeviceParser object, an IllegalArgumentException shall be thrown.]
        if (json == null || json.isEmpty())
        {
            throw new IllegalArgumentException("The provided json cannot be null or empty");
        }

        ExportImportDeviceParser deviceParser;
        try
        {
            //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_005: [This constructor shall take the provided json and convert it into a new ExportImportDeviceParser and return it.]
            deviceParser = gson.fromJson(json, ExportImportDeviceParser.class);
        }
        catch (JsonSyntaxException e)
        {
            //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_011: [If the provided json is null, empty, or cannot be parsed into an ExportImportDeviceParser object, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The provided json could not be parsed");
        }

        if (deviceParser.getId() == null)
        {
            //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_008: [If the provided json is missing the id field, or its value is empty, an IllegalArgumentException shall be thrown]
            throw new IllegalArgumentException("The id field must be present in the provided json");
        }

        if (deviceParser.getAuthentication() == null)
        {
            //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_009: [If the provided json is missing the authentication field, or its value is empty, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The authentication field must be present in the provided json");
        }

        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_002: [The parser shall look for the authentication Type of the serialized export import device and save it to the returned ExportImportDeviceParser instance]
        this.authentication = deviceParser.authentication;
        this.id = deviceParser.id;
        this.moduleId = deviceParser.moduleId;
        this.importMode = deviceParser.importMode;
        this.eTag = deviceParser.eTag;
        this.statusReason = deviceParser.statusReason;
        this.status = deviceParser.status;
        this.tags = deviceParser.tags;
    }
}
