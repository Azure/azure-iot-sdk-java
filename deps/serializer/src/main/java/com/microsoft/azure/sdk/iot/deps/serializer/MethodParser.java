// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Representation of a single Direct Method Access collection with a Json serializer and deserializer.
 */
public class MethodParser
{
    protected enum Operation
    {
        invoke,
        response,
        payload,
        none
    }

    @Expose(serialize = false, deserialize = false)
    private Operation operation;

    /* Codes_SRS_METHODPARSER_21_015: [The toJson shall include name as `methodName` in the json.] */
    private static final String METHOD_NAME_TAG = "methodName";
    @Expose(serialize = true, deserialize = false)
    @SerializedName(METHOD_NAME_TAG)
    private String name;

    /* Codes_SRS_METHODPARSER_21_016: [The toJson shall include responseTimeout in seconds as `responseTimeoutInSeconds` in the json.] */
    private static final String RESPONSE_TIMEOUT_IN_SECONDS_TAG = "responseTimeoutInSeconds";
    @Expose(serialize = true, deserialize = false)
    @SerializedName(RESPONSE_TIMEOUT_IN_SECONDS_TAG)
    private Long responseTimeout;

    /* Codes_SRS_METHODPARSER_21_031: [The toJson shall include connectTimeout in seconds as `connectTimeoutInSeconds` in the json.] */
    private static final String CONNECT_TIMEOUT_IN_SECONDS_TAG = "connectTimeoutInSeconds";
    @Expose(serialize = true, deserialize = false)
    @SerializedName(CONNECT_TIMEOUT_IN_SECONDS_TAG)
    private Long connectTimeout;

    /* Codes_SRS_METHODPARSER_21_024: [The class toJson include status as `status` in the json.] */
    private static final String STATUS_TAG = "status";
    @Expose(serialize = false, deserialize = true)
    @SerializedName(STATUS_TAG)
    private Integer status;

    /* Codes_SRS_METHODPARSER_21_018: [The class toJson include payload as `payload` in the json.] */
    private static final String PAYLOAD_TAG = "payload";
    @SerializedName(PAYLOAD_TAG)
    private Object payload;

    /**
     * CONSTRUCTOR
     * Create a MethodParser instance with provided values.
     */
    public MethodParser()
    {
        /* Codes_SRS_METHODPARSER_21_029: [The constructor shall create an instance of the MethodParser.] */
        /* Codes_SRS_METHODPARSER_21_030: [The constructor shall initialize all data in the collection as null.] */
        this.name = null;
        this.responseTimeout = null;
        this.connectTimeout = null;
        this.status = null;
        this.payload = null;

        /* Codes_SRS_METHODPARSER_21_022: [The constructor shall initialize the method operation as `none`.] */
        this.operation = Operation.none;
    }

    /**
     * CONSTRUCTOR
     * Create a MethodParser instance with provided values.
     *
     * @param name - method name [required].
     * @param responseTimeout - maximum interval of time, in seconds, that the Direct Method will wait for answer. It can be {@code null}.
     * @param connectTimeout - maximum interval of time, in seconds, that the Direct Method will wait for the connection. It can be {@code null}.
     * @param payload - Object that contains the payload defined by the user. It can be {@code null}.
     * @throws IllegalArgumentException This exception is thrown if the one of the provided information do not fits the requirements.
     */
    public MethodParser(String name, Long responseTimeout, Long connectTimeout, Object payload) throws IllegalArgumentException
    {
        /* Codes_SRS_METHODPARSER_21_001: [The constructor shall create an instance of the MethodParser.] */
        this();

        /* Codes_SRS_METHODPARSER_21_003: [All Strings are case sensitive.] */
        /* Codes_SRS_METHODPARSER_21_004: [If the `name` is null, empty, contains more than 128 chars, or illegal char (`$`, `.`, space), the constructor shall throw IllegalArgumentException.] */
        validateKey(name);

        if(responseTimeout != null)
        {
            /* Codes_SRS_METHODPARSER_21_005: [If the responseTimeout is a negative number, the constructor shall throw IllegalArgumentException.] */
            validateTimeout(responseTimeout);
        }

        if(connectTimeout != null)
        {
            /* Codes_SRS_METHODPARSER_21_033: [If the connectTimeout is a negative number, the constructor shall throw IllegalArgumentException.] */
            validateTimeout(connectTimeout);
        }

        /* Codes_SRS_METHODPARSER_21_002: [The constructor shall update the method collection using the provided information.] */
        this.name = name;
        this.responseTimeout = responseTimeout;
        this.connectTimeout = connectTimeout;
        this.payload = payload;

        /* Codes_SRS_METHODPARSER_21_023: [The constructor shall initialize the method operation as `invoke`.] */
        this.operation = Operation.invoke;
    }

    /**
     * CONSTRUCTOR
     * Create a MethodParser instance with provided values.
     *
     * @param payload - Object that contains the payload defined by the user. It can be {@code null}.
     */
    public MethodParser(Object payload)
    {
        /* Codes_SRS_METHODPARSER_21_020: [The constructor shall create an instance of the MethodParser.] */
        this();

        /* Codes_SRS_METHODPARSER_21_021: [The constructor shall update the method collection using the provided information.] */
        this.payload = payload;

        /* Codes_SRS_METHODPARSER_21_034: [The constructor shall set the method operation as `payload`.] */
        this.operation = Operation.payload;
    }

    /**
     * Set the Method collection with the provided information in the json.
     *
     * @param json - Json with the information to change the collection.
     *                  - If contains `methodName`, it is a full method including `methodName`, `responseTimeoutInSeconds`, `connectTimeoutInSeconds`, and `payload`.
     *                  - If contains `status`, it is a response with `status` and `payload`.
     *                  - Otherwise, it is only `payload`.
     * @throws IllegalArgumentException This exception is thrown if the one of the provided information do not fits the requirements.
     */
    public synchronized void fromJson(String json) throws IllegalArgumentException
    {
        MethodParser newMethodParser;

        if((json == null) || json.isEmpty())
        {
            /* Codes_SRS_METHODPARSER_21_008: [If the provided json is null, empty, or not valid, the fromJson shall throws IllegalArgumentException.] */
            throw new IllegalArgumentException("Invalid json");
        }

        /* Codes_SRS_METHODPARSER_21_007: [The json can contain values `null`, `"null"`, and `""`, which represents null, the string null, and empty string respectively.] */
        Gson gson = new GsonBuilder().serializeNulls().create();

        /* Codes_SRS_METHODPARSER_21_006: [The fromJson shall parse the json and fill the method collection.] */
        if(json.contains(METHOD_NAME_TAG))
        {
            if(json.contains(STATUS_TAG))
            {
                /* Codes_SRS_METHODPARSER_21_008: [If the provided json is null, empty, or not valid, the fromJson shall throws IllegalArgumentException.] */
                throw new IllegalArgumentException("Invoke method name and Status reported in the same json");
            }
            /**
             * Codes_SRS_METHODPARSER_21_009: [If the json contains the `methodName` identification, the fromJson shall parse the full method, and set the operation as `invoke`.]
             *  Ex:
             *  {
             *      "methodName": "reboot",
             *      "responseTimeoutInSeconds": 200,
             *      "connectTimeoutInSeconds": 5,
             *      "payload":
             *      {
             *          "input1": "someInput",
             *          "input2": "anotherInput"
             *      }
             *  }
             */
            try
            {
                newMethodParser = gson.fromJson(json, MethodParser.class);
            }
            catch (Exception malformed)
            {
                /* Codes_SRS_METHODPARSER_21_008: [If the provided json is null, empty, or not valid, the fromJson shall throws IllegalArgumentException.] */
                throw new IllegalArgumentException("Malformed json:" + malformed);
            }

            this.name = newMethodParser.name;
            this.responseTimeout = newMethodParser.responseTimeout;
            this.connectTimeout = newMethodParser.connectTimeout;
            this.status = null;
            this.payload = newMethodParser.payload;
            this.operation = Operation.invoke;
        }
        else if(json.contains(STATUS_TAG))
        {
            /**
             * Codes_SRS_METHODPARSER_21_011: [If the json contains the `status` identification, the fromJson shall parse both status and payload, and set the operation as `response`.]
             *  Ex:
             *  {
             *      "status": 201,
             *      "payload": {"AnyValidPayload" : "" }
             *  }
             */
            try
            {
                newMethodParser = gson.fromJson(json, MethodParser.class);
            }
            catch (Exception malformed)
            {
                /* Codes_SRS_METHODPARSER_21_008: [If the provided json is null, empty, or not valid, the fromJson shall throws IllegalArgumentException.] */
                throw new IllegalArgumentException("Malformed json:" + malformed);
            }
            this.name = null;
            this.responseTimeout = null;
            this.connectTimeout = null;
            this.status = newMethodParser.status;
            this.payload = newMethodParser.payload;
            this.operation = Operation.response;
        }
        else
        {
            try
            {
                /**
                 * Codes_SRS_METHODPARSER_21_010: [If the json contains any payload without `methodName` or `status` identification, the fromJson shall parse only the payload, and set the operation as `payload`]
                 *  Ex:
                 *  {
                 *      "input1": "someInput",
                 *      "input2": "anotherInput"
                 *  }
                 */
                this.name = null;
                this.responseTimeout = null;
                this.connectTimeout = null;
                this.status = null;
                this.payload = gson.fromJson(json, Object.class);
                this.operation = Operation.payload;
            }
            catch (Exception malformed)
            {
                /* Codes_SRS_METHODPARSER_21_008: [If the provided json is null, empty, or not valid, the fromJson shall throws IllegalArgumentException.] */
                throw new IllegalArgumentException("Malformed json:" + malformed);
            }
        }
    }

    /**
     * Return an Integer with the response status.
     *
     * @return An integer with the status of the response. It can be {@code null}.
     * @throws IllegalArgumentException This exception is thrown if the operation is not type of `response`.
     */
    public Integer getStatus() throws IllegalArgumentException
    {
        /* Codes_SRS_METHODPARSER_21_035: [If the operation is not `response`, the getStatus shall throws IllegalArgumentException.] */
        if(operation != Operation.response)
        {
            throw new IllegalArgumentException("No response to report status");
        }

        /* Codes_SRS_METHODPARSER_21_012: [The getStatus shall return an Integer with the status in the parsed json.] */
        return this.status;
    }

    /**
     * Return an Object with the payload.
     *
     * @return An Object with the payload. It can be {@code null}.
     */
    public Object getPayload()
    {
        /* Codes_SRS_METHODPARSER_21_013: [The getPayload shall return an Object with the Payload in the parsed json.] */
        return this.payload;
    }

    /**
     * Create a String with a json content that represents all the information in the method collection.
     *
     * @return String with the json content.
     * @throws IllegalArgumentException This exception is thrown if the one of the provided information do not fits the requirements.
     */
    public String toJson() throws IllegalArgumentException
    {
        /* Codes_SRS_METHODPARSER_21_014: [The toJson shall create a String with the full information in the method collection using json format.] */
        /* Codes_SRS_METHODPARSER_21_015: [The toJson shall include name as `methodName` in the json.] */
        /* Codes_SRS_METHODPARSER_21_016: [The toJson shall include responseTimeout in seconds as `responseTimeoutInSeconds` in the json.] */
        /* Codes_SRS_METHODPARSER_21_017: [If the responseTimeout is null, the toJson shall not include the `responseTimeoutInSeconds` in the json.] */
        /* Codes_SRS_METHODPARSER_21_031: [The toJson shall include connectTimeout in seconds as `connectTimeoutInSeconds` in the json.] */
        /* Codes_SRS_METHODPARSER_21_032: [If the connectTimeout is null, the toJson shall not include the `connectTimeoutInSeconds` in the json.] */
        /* Codes_SRS_METHODPARSER_21_018: [The class toJson include payload as `payload` in the json.] */
        /* Codes_SRS_METHODPARSER_21_019: [If the payload is null, the toJson shall include `payload` with value `null`.] */
        /* Codes_SRS_METHODPARSER_21_024: [The class toJson include status as `status` in the json.] */
        /* Codes_SRS_METHODPARSER_21_025: [If the status is null, the toJson shall include `status` as `null`.] */
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().serializeNulls().create();
        JsonObject jsonProperty = new JsonObject();

        switch(operation)
        {
            case invoke:
                /**
                 *  Codes_SRS_METHODPARSER_21_026: [If the method operation is `invoke`, the toJson shall include the full method information in the json.]
                 *  Ex:
                 *  {
                 *      "methodName": "reboot",
                 *      "responseTimeoutInSeconds": 200,
                 *      "connectTimeoutInSeconds": 5,
                 *      "payload":
                 *      {
                 *          "input1": "someInput",
                 *          "input2": "anotherInput"
                 *      }
                 *  }
                 */
                if ((name == null) || name.isEmpty())
                {
                    throw new IllegalArgumentException("cannot invoke method with null name");
                }
                jsonProperty.addProperty(METHOD_NAME_TAG, name);
                if(responseTimeout != null)
                {
                    jsonProperty.addProperty(RESPONSE_TIMEOUT_IN_SECONDS_TAG, responseTimeout);
                }
                if(connectTimeout != null)
                {
                    jsonProperty.addProperty(CONNECT_TIMEOUT_IN_SECONDS_TAG, connectTimeout);
                }
                jsonProperty.add(PAYLOAD_TAG, gson.toJsonTree(payload));
                return jsonProperty.toString();

            case response:
                /** Codes_SRS_METHODPARSER_21_027: [If the method operation is `response`, the toJson shall parse both status and payload.]
                 *  Ex:
                 *  {
                 *      "status": 201,
                 *      "payload": {"AnyValidPayload" : "" }
                 *  }
                 */
                jsonProperty.addProperty(STATUS_TAG, status);
                jsonProperty.add(PAYLOAD_TAG, gson.toJsonTree(payload));
                return jsonProperty.toString();

            case payload:
                /**
                 * Codes_SRS_METHODPARSER_21_028: [If the method operation is `payload`, the toJson shall parse only the payload.]
                 *  Ex:
                 *  {
                 *      "input1": "someInput",
                 *      "input2": "anotherInput"
                 *  }
                 */
                if (payload instanceof Map)
                {
                    return gson.toJson(payload, Map.class);
                }
                return gson.toJson(payload);

            default:
                /* Codes_SRS_METHODPARSER_21_036: [If the method operation is `none`, the toJson shall throw IllegalArgumentException.] */
                throw new IllegalArgumentException("There is no content to parser");
        }
    }
    
    /**
     * Validation helper, make sure that the key fits the requirements.
     *
     * @param key is a key string. It can be name or an Json key in a Map.
     * @throws IllegalArgumentException This exception is thrown if the provided Key do not fits the requirements.
     */
    private void validateKey(String key) throws IllegalArgumentException
    {
        /* Codes_SRS_METHODPARSER_21_004: [If the `name` is null, empty, contains more than 128 chars, or illegal char (`$`, `.`, space), the constructor shall throw IllegalArgumentException.] */
        if((key == null) || (key.isEmpty()))
        {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }

        if(key.length()>128)
        {
            throw new IllegalArgumentException("Key cannot be longer than 128 characters");
        }

        if(key.contains("$") || key.contains(".") ||key.contains(" "))
        {
            throw new IllegalArgumentException("Key cannot contain \'$\', \'.\', or space");
        }
    }

    /**
     * Validation helper, make sure that timeout values fit the requirements.
     *
     * @param timeout is the timeout value in seconds.
     * @throws IllegalArgumentException This exception is thrown if the provided timeout value do not fits the requirements.
     */
    private void validateTimeout(Long timeout) throws IllegalArgumentException
    {
        /* Codes_SRS_METHODPARSER_21_005: [If the responseTimeout is a negative number, the constructor shall throw IllegalArgumentException.] */
        /* Codes_SRS_METHODPARSER_21_033: [If the connectTimeout is a negative number, the constructor shall throw IllegalArgumentException.] */
        if(timeout<0)
        {
            throw new IllegalArgumentException("Negative timeout");
        }
    }

}
