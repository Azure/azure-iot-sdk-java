/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.hsm.parser;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.crypto.Mac;

import java.nio.charset.StandardCharsets;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;

// This suppression below is addressing warnings of fields used for serialization.
@SuppressWarnings({"FieldCanBeLocal", "unused"}) // A number of private members are unused but may be filled in or used by serialization
public class SignRequest
{
    private static final String KEY_ID_NAME = "keyId";
    @Expose(deserialize = false)
    @SerializedName(KEY_ID_NAME)
    private String keyId;

    private transient Mac algo;

    private static final String ALGO_NAME = "algo";
    @Expose(deserialize = false)
    @SerializedName(ALGO_NAME)
    private String algoString;

    private static final String DATA_NAME = "data";
    @Expose(deserialize = false)
    @SerializedName(DATA_NAME)
    private String data;

    public void setKeyId(String keyId)
    {
        // Codes_SRS_HTTPHSMSIGNREQUEST_34_001: [This function shall save the provided keyId.]
        this.keyId = keyId;
    }

    public byte[] getData()
    {
        // Codes_SRS_HTTPHSMSIGNREQUEST_34_002: [This function shall return the saved data.]
        return data.getBytes(StandardCharsets.UTF_8);
    }

    public void setData(byte[] data)
    {
        // Codes_SRS_HTTPHSMSIGNREQUEST_34_003: [This function shall save the provided data after base64 encoding it.]
        this.data = encodeBase64String(data);
    }

    public void setAlgo(Mac algo)
    {
        // Codes_SRS_HTTPHSMSIGNREQUEST_34_004: [This function shall save the provided algo.]
        this.algo = algo;

        this.algoString = algo.getAlgorithm().toUpperCase();
    }

    public String toJson()
    {
        return new GsonBuilder().create().toJson(this);
    }

    //empty constructor for Gson to use
    public SignRequest() {}
}
