/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.hsm.parser;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The json parser for the response from an HSM that contains the certificates to be trusted
 */
public class TrustBundleResponse
{
    private static final String CERTIFICATE_NAME = "certificate";
    @Expose(serialize = false, deserialize = true)
    @SerializedName(CERTIFICATE_NAME)
    private String certificates;

    /**
     * Constructor that deserializes from json
     * @param json the json representation of the TrustBundleResponse
     * @return  the created trustBundleResponse instance
     */
    public static TrustBundleResponse fromJson(String json)
    {
        //Codes_SRS_TRUSTBUNDLERESPONSE_34_003: [This constructor shall create a new TrustBundleResponse from json.]
        TrustBundleResponse response = new GsonBuilder().create().fromJson(json, TrustBundleResponse.class);

        if (response == null || response.certificates == null || response.certificates.isEmpty())
        {
            //Codes_SRS_TRUSTBUNDLERESPONSE_34_002: [If the provided json does not contain any certificates, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("The provided json did not contain any certificates");
        }

        return response;
    }

    /**
     * Empty constructor, only for gson. Don't delete
     */
    public TrustBundleResponse()
    {
    }

    /**
     * Retreive the certificates that the HSM dictates to trust
     * @return one to many concatenated certificate strings to trust
     */
    public String getCertificates()
    {
        //Codes_SRS_TRUSTBUNDLERESPONSE_34_001: [This function shall return the saved certificates string.]
        return this.certificates;
    }
}
