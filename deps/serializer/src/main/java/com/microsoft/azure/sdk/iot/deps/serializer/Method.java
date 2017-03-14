// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * Representation of a single Direct Method Access collection with a Json serializer and deserializer.
 */
public class Method 
{
    private static final int MAX_MAP_LEVEL = 5;

    /* Codes_SRS_METHOD_21_015: [The toJson shall include name as `methodName` in the json.] */
    @Expose(serialize = true, deserialize = false)
    @SerializedName("methodName")
    private String name;

    /* Codes_SRS_METHOD_21_016: [The toJson shall include responseTimeout in seconds as `responseTimeoutInSeconds` in the json.] */
    @Expose(serialize = true, deserialize = false)
    @SerializedName("responseTimeoutInSeconds")
    private Long responseTimeout;

    /* Codes_SRS_METHOD_21_031: [The toJson shall include connectTimeout in seconds as `connectTimeoutInSeconds` in the json.] */
    @Expose(serialize = true, deserialize = false)
    @SerializedName("connectTimeoutInSeconds")
    private Long connectTimeout;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("status")
    private Integer status;

    /* Codes_SRS_METHOD_21_018: [The class toJson include payload as `payload` in the json.] */
    @SerializedName("payload")
    private Object payload;

    /**
     * CONSTRUCTOR
     * Create a Method instance with provided values.
     */
    public Method()
    {
        /* Codes_SRS_METHOD_21_029: [The constructor shall create an instance of the method.] */
        /* Codes_SRS_METHOD_21_030: [The constructor shall initialize all data in the collection as null.] */
        this.name = null;
        this.responseTimeout = null;
        this.connectTimeout = null;
        this.status = null;
        this.payload = null;
    }

    /**
     * CONSTRUCTOR
     * Create a Method instance with provided values.
     *
     * @param name - method name [required].
     * @param responseTimeout - maximum interval of time, in seconds, that the Direct Method will wait for answer.
     * @param connectTimeout - maximum interval of time, in seconds, that the Direct Method will wait for the connection.
     * @param payload - Object that contains the payload defined by the user.
     * @throws IllegalArgumentException This exception is thrown if the one of the provided information do not fits the requirements.
     */
    public Method(String name, Long responseTimeout, Long connectTimeout, Object payload) throws IllegalArgumentException
    {
        /* Codes_SRS_METHOD_21_001: [The constructor shall create an instance of the method.] */
        this();

        /* Codes_SRS_METHOD_21_003: [All Strings are case sensitive.] */
        /* Codes_SRS_METHOD_21_004: [If the `name` is null, empty, contains more than 128 chars, or illegal char (`$`, `.`, space), the constructor shall throw IllegalArgumentException.] */
        validateKey(name);

        if(responseTimeout != null)
        {
            /* Codes_SRS_METHOD_21_005: [If the responseTimeout is a negative number, the constructor shall throw IllegalArgumentException.] */
            validateTimeout(responseTimeout);
        }

        if(connectTimeout != null)
        {
            /* Codes_SRS_METHOD_21_033: [If the connectTimeout is a negative number, the constructor shall throw IllegalArgumentException.] */
            validateTimeout(connectTimeout);
        }

        /* Codes_SRS_METHOD_21_002: [The constructor shall update the method collection using the provided information.] */
        this.name = name;
        this.responseTimeout = responseTimeout;
        this.connectTimeout = connectTimeout;
        this.payload = payload;
    }

    /**
     * CONSTRUCTOR
     * Create a Method instance with provided values.
     *
     * @param payload - Object that contains the payload defined by the user.
     */
    public Method(Object payload)
    {
        /* Codes_SRS_METHOD_21_020: [The constructor shall create an instance of the method.] */
        this();

        /* Codes_SRS_METHOD_21_021: [The constructor shall update the method collection using the provided information.] */
        this.payload = payload;
    }

    /**
     * Create a Method instance with the provided information in the json.
     *
     * @param json - Json with the information to change the collection.
     *                  - If contains `methodName`, it is a full method including `methodName`, `responseTimeoutInSeconds`, `connectTimeoutInSeconds`, and `payload`.
     *                  - If contains `status`, it is a response with `status` and `payload`.
     *                  - Otherwise, it is only `payload`.
     * @throws IllegalArgumentException This exception is thrown if the one of the provided information do not fits the requirements.
     */
    public void fromJson(String json) throws IllegalArgumentException
    {
        /* Codes_SRS_METHOD_21_006: [The fromJson shall create an instance of the method.] */
        
        if((json == null) || json.isEmpty())
        {
            /* Codes_SRS_METHOD_21_008: [If the provided json is null, empty, or not valid, the fromJson shall throws IllegalArgumentException.] */
            throw new IllegalArgumentException("Invalid json");
        }
        Gson gson = new GsonBuilder().create();

        /* Codes_SRS_METHOD_21_007: [The fromJson shall parse the json and fill the status and payload.] */
        try
        {
            /* Codes_SRS_METHOD_21_009: [If the json contains the `methodName` identification, the fromJson shall parser the full method.]
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
            /** Codes_SRS_METHOD_21_011: [If the json contains the `status` and `payload` identification, the fromJson shall parser both status and payload.]
             *  Ex:
             *  {
             *      "status": 201,
             *      "payload": {"AnyValidPayload" : "" }
             *  }
             */
            Method newMethod = gson.fromJson(json, Method.class);
            if((newMethod.name == null) && (newMethod.status == null))
            {
                throw new Exception();
            }
            this.name = newMethod.name;
            this.responseTimeout = newMethod.responseTimeout;
            this.connectTimeout = newMethod.connectTimeout;
            this.status = newMethod.status;
            this.payload = newMethod.payload;
        }
        catch (Exception e)
        {
            // It is just payload.
            try
            {
                /**
                 * Codes_SRS_METHOD_21_010: [If the json contains any payload without status identification, the fromJson shall parser only the payload.]
                 *  Ex:
                 *  {
                 *      "input1": "someInput",
                 *      "input2": "anotherInput"
                 *  }
                 */
                this.payload = gson.fromJson(json, Object.class);
            }
            catch (Exception malformed)
            {
                /* Codes_SRS_METHOD_21_008: [If the provided json is null, empty, or not valid, the fromJson shall throws IllegalArgumentException.] */
                throw new IllegalArgumentException("Malformed json:" + malformed);
            }
        }

        if((this.name != null) && ((this.status != null) || this.name.isEmpty()))
        {
            /* Codes_SRS_METHOD_21_008: [If the provided json is null, empty, or not valid, the fromJson shall throws IllegalArgumentException.] */
            throw new IllegalArgumentException("Name and Status reported in the same json");
        }
    }

    /**
     * Return an Integer with the response status.
     *
     * @return An integer with the status of the response (it can be null). 
     */
    public Integer getStatus()
    {
        /* Codes_SRS_METHOD_21_012: [The getStatus shall return an Integer with the status in the parsed json.] */
        return this.status;
    }

    /**
     * Return an Object with the payload.
     *
     * @return An Object with the payload(it can be null). 
     */
    public Object getPayload()
    {
        /* Codes_SRS_METHOD_21_013: [The getPayload shall return an Object with the Payload in the parsed json.] */
        return this.payload;
    }

    /**
     * Create a String with a json content that represents all the information in the method collection.
     *
     * @return String with the json content.
     * @throws IllegalArgumentException This exception is thrown if the one of the provided information do not fits the requirements.
     */
    public String toJson()
    {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();

        /* Codes_SRS_METHOD_21_014: [The toJson shall create a String with the full information in the method collection using json format.] */
        /* Codes_SRS_METHOD_21_015: [The toJson shall include name as `methodName` in the json.] */
        /* Codes_SRS_METHOD_21_016: [The toJson shall include responseTimeout in seconds as `responseTimeoutInSeconds` in the json.] */
        /* Codes_SRS_METHOD_21_017: [If the responseTimeout is null, the toJson shall not include the `responseTimeoutInSeconds` in the json.] */
        /* Codes_SRS_METHOD_21_031: [The toJson shall include connectTimeout in seconds as `connectTimeoutInSeconds` in the json.] */
        /* Codes_SRS_METHOD_21_032: [If the connectTimeout is null, the toJson shall not include the `connectTimeoutInSeconds` in the json.] */
        /* Codes_SRS_METHOD_21_018: [The class toJson include payload as `payload` in the json.] */
        /* Codes_SRS_METHOD_21_019: [If the payload is null, the toJson shall not include `payload` for parameters in the json.] */
        /* Codes_SRS_METHOD_21_024: [The class toJson include status as `status` in the json.] */
        /* Codes_SRS_METHOD_21_025: [If the status is null, the toJson shall not include `status` for parameters in the json.] */
        /**
         *  Codes_SRS_METHOD_21_026: [If the method contains a name, the toJson shall include the full method information in the json.]
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
        
        if(name == null)
        {
            /** Codes_SRS_METHOD_21_027: [If the method contains the status, the toJson shall parser both status and payload.
             *  Ex:
             *  {
             *      "status": 201,
             *      "payload": {"AnyValidPayload" : "" }
             *  }
             */
            if(status == null)
            {
                /**
                 * Codes_SRS_METHOD_21_028: [If the method do not contains name or status, the toJson shall parser only the payload.]
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
                else
                {
                    return gson.toJson(payload);
                }
            }
        }

        return gson.toJson(this);
    }
    
    /**
     * Validation helper, make sure that the key fits the requirements.
     *
     * @param key is a key string. It can be name or an Json key in a Map.
     * @throws IllegalArgumentException This exception is thrown if the provided Key do not fits the requirements.
     */
    private void validateKey(String key) throws IllegalArgumentException
    {
        /* Codes_SRS_METHOD_21_004: [If the `name` is null, empty, contains more than 128 chars, or illegal char (`$`, `.`, space), the constructor shall throw IllegalArgumentException.] */
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
        /* Codes_SRS_METHOD_21_005: [If the responseTimeout is a negative number, the constructor shall throw IllegalArgumentException.] */
        /* Codes_SRS_METHOD_21_033: [If the connectTimeout is a negative number, the constructor shall throw IllegalArgumentException.] */
        if(timeout<0)
        {
            throw new IllegalArgumentException("Negative timeout");
        }
    }

}
