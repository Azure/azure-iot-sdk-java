// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;

public class ExportImportDeviceParser
{
    private static final String ID_NAME = "id";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(ID_NAME)
    private String id;

    private static final String E_TAG_NAME = "eTag";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(E_TAG_NAME)
    private String eTag;

    private static final String IMPORT_MODE_NAME = "importMode";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(IMPORT_MODE_NAME)
    private String importMode;

    private static final String STATUS_NAME = "status";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(STATUS_NAME)
    private String status;

    private static final String STATUS_REASON_NAME = "statusReason";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(STATUS_REASON_NAME)
    private String statusReason;

    private static final String AUTHENTICATION_NAME = "authentication";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(AUTHENTICATION_NAME)
    private AuthenticationParser authentication;
    
    private static final String TAGS_NAME = "tags";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(TAGS_NAME)
    private TwinCollection tags;

    private transient static Gson gson = new Gson();

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

        if (deviceParser.getIdFinal() == null)
        {
            //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_008: [If the provided json is missing the id field, or its value is empty, an IllegalArgumentException shall be thrown]
            throw new IllegalArgumentException("The id field must be present in the provided json");
        }

        if (deviceParser.getAuthenticationFinal() == null)
        {
            //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_009: [If the provided json is missing the authentication field, or its value is empty, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The authentication field must be present in the provided json");
        }

        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_002: [The parser shall look for the authentication Type of the serialized export import device and save it to the returned ExportImportDeviceParser instance]
        this.authentication = deviceParser.authentication;
        this.id = deviceParser.id;
        this.importMode = deviceParser.importMode;
        this.eTag = deviceParser.eTag;
        this.statusReason = deviceParser.statusReason;
        this.status = deviceParser.status;
        this.tags = deviceParser.tags;
    }

    /**
     * Getter for id
     *
     * @deprecated as of Deps version 0.7.1, please use {@link #getIdFinal()}
     *
     * @return The value of id
     */
    @Deprecated
    public String getId()
    {
        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_013: [This method shall return the value of this object's Id.]
        return id;
    }

    /**
     * Getter for id
     *
     * @return The value of id
     */
    public final String getIdFinal()
    {
        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_013: [This method shall return the value of this object's Id.]
        return id;
    }


    /**
     * Setter for id
     * @param id the value to set id to
     * @throws IllegalArgumentException if id is null
     */
    public void setId(String id) throws IllegalArgumentException
    {
        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_007: [If the provided id is null, an IllegalArgumentException shall be thrown.]
        if (id == null)
        {
            throw new IllegalArgumentException("Argument 'id' cannot be null");
        }

        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_022: [This method shall set the value of this object's Id equal to the provided value.]
        this.id = id;
    }

    /**
     * Getter for eTag
     *
     * @return The value of eTag
     */
    public String getETag()
    {
        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_015: [This method shall return the value of this object's eTag.]
        return eTag;
    }

    /**
     * Getter for importMode
     *
     * @return The value of importMode
     */
    public String getImportMode()
    {
        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_017: [This method shall return the value of this object's importMode.]
        return importMode;
    }

    /**
     * Getter for status
     *
     * @return The value of status
     */
    public String getStatus()
    {
        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_019: [This method shall return the value of this object's status.]
        return status;
    }

    /**
     * Getter for statusReason
     *
     * @return The value of statusReason
     */
    public String getStatusReason()
    {
        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_021: [This method shall return the value of this object's statusReason.]
        return statusReason;
    }

    /**
     * Setter for eTag
     *
     * @param eTag the value to set eTag to
     */
    public void setETag(String eTag)
    {
        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_014: [This method shall set the value of this object's eTag equal to the provided value.]
        this.eTag = eTag;
    }

    /**
     * Setter for ImportMode
     *
     * @param importMode the value to set ImportMode to
     */
    public void setImportMode(String importMode)
    {
        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_016: [This method shall set the value of this object's importMode equal to the provided value.]
        this.importMode = importMode;
    }

    /**
     * Setter for Status
     *
     * @param status the value to set Status to
     */
    public void setStatus(String status)
    {
        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_018: [This method shall set the value of this object's status equal to the provided value.]
        this.status = status;
    }

    /**
     * Setter for StatusReason
     *
     * @param statusReason the value to set StatusReason to
     */
    public void setStatusReason(String statusReason)
    {
        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_020: [This method shall set the value of this object's statusReason equal to the provided value.]
        this.statusReason = statusReason;
    }

    /**
     * Getter for authentication
     *
     * @deprecated as of Deps version 0.7.1, please use {@link #getAuthenticationFinal()}
     *
     * @return The value of authentication
     */
    @Deprecated
    public AuthenticationParser getAuthentication()
    {
        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_012: [This method shall return the value of this object's AuthenticationParser.]
        return authentication;
    }

    /**
     * Getter for authentication
     *
     * @return The value of authentication
     */
    public final AuthenticationParser getAuthenticationFinal()
    {
        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_012: [This method shall return the value of this object's AuthenticationParser.]
        return authentication;
    }

    /**
     * Setter for authentication
     * @param authentication the authentication to set
     * @throws IllegalArgumentException if authentication is null
     */
    public void setAuthentication(AuthenticationParser authentication) throws IllegalArgumentException
    {
        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_006: [If the provided authentication is null, an IllegalArgumentException shall be thrown.]
        if (authentication == null)
        {
            throw new IllegalArgumentException("Authentication cannot be null");
        }

        //Codes_SRS_EXPORTIMPORTDEVICE_PARSER_34_023: [This method shall set the value of this object's AuthenticationParser equal to the provided value.]
        this.authentication = authentication;
    }

	/**
	 * @return the tags
	 */
	public TwinCollection getTags() {
		return tags;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(TwinCollection tags) {
		this.tags = tags;
	}
}
