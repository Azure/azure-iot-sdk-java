/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.hsm.parser;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.deps.util.Base64;

import javax.crypto.Mac;

public class SignRequest
{
    private static final String KEY_ID_NAME = "keyId";
    @Expose(serialize = true, deserialize = false)
    @SerializedName(KEY_ID_NAME)
    private String keyId;

    private transient Mac algo;

    private static final String ALGO_NAME = "algo";
    @Expose(serialize = true, deserialize = false)
    @SerializedName(ALGO_NAME)
    private String algoString;

    private static final String DATA_NAME = "data";
    @Expose(serialize = true, deserialize = false)
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
        return data.getBytes();
    }

    public void setData(byte[] data)
    {
        // Codes_SRS_HTTPHSMSIGNREQUEST_34_003: [This function shall save the provided data after base64 encoding it.]
        this.data = Base64.encodeBase64StringLocal(data);
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
