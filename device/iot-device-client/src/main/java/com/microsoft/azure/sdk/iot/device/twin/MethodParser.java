// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.microsoft.azure.sdk.iot.device.twin.ParserUtility.getJsonObjectValue;
import static com.microsoft.azure.sdk.iot.device.twin.ParserUtility.resolveJsonElement;

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

    /* Codes_SRS_METHODPARSER_21_015: [The toJsonElement shall include name as `methodName` in the json.] */
    private static final String METHOD_NAME_TAG = "methodName";
    @Expose(deserialize = false)
    @SerializedName(METHOD_NAME_TAG)
    @Getter
    @Setter
    private String name;

    /* Codes_SRS_METHODPARSER_21_016: [The toJsonElement shall include responseTimeout in seconds as `responseTimeoutInSeconds` in the json.] */
    private static final String RESPONSE_TIMEOUT_IN_SECONDS_TAG = "responseTimeoutInSeconds";
    @Expose(deserialize = false)
    @SerializedName(RESPONSE_TIMEOUT_IN_SECONDS_TAG)
    @Getter
    @Setter
    private Long responseTimeout;

    /* Codes_SRS_METHODPARSER_21_031: [The toJsonElement shall include connectTimeout in seconds as `connectTimeoutInSeconds` in the json.] */
    private static final String CONNECT_TIMEOUT_IN_SECONDS_TAG = "connectTimeoutInSeconds";
    @Expose(deserialize = false)
    @SerializedName(CONNECT_TIMEOUT_IN_SECONDS_TAG)
    @Getter
    @Setter
    private Long connectTimeout;

    /* Codes_SRS_METHODPARSER_21_024: [The class toJsonElement include status as `status` in the json.] */
    private static final String STATUS_TAG = "status";
    @Expose(serialize = false)
    @SerializedName(STATUS_TAG)
    @Getter
    @Setter
    private Integer status;

    /* Codes_SRS_METHODPARSER_21_018: [The class toJsonElement include payload as `payload` in the json.] */
    private static final String PAYLOAD_TAG = "payload";
    @SerializedName(PAYLOAD_TAG)
    @Getter
    @Setter
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

        if (responseTimeout != null)
        {
            /* Codes_SRS_METHODPARSER_21_005: [If the responseTimeout is a negative number, the constructor shall throw IllegalArgumentException.] */
            validateTimeout(responseTimeout);
        }

        if (connectTimeout != null)
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

        if ((json == null) || json.isEmpty())
        {
            /* Codes_SRS_METHODPARSER_21_008: [If the provided json is null, empty, or not valid, the fromJson shall throws IllegalArgumentException.] */
            throw new IllegalArgumentException("Invalid json.");
        }

        JsonParser jsonParser = new JsonParser();
        try
        {
            JsonElement jsonElement = jsonParser.parse(json);
            if (jsonElement instanceof JsonPrimitive || jsonElement instanceof JsonArray)
            {
                /*
                   Basic JSON String or Array
                   Ex:
                       "this is payload."
                       1
                       true
                       2.0e10
                       2.6
                 */
                this.operation = Operation.payload;
                this.payload = resolveJsonElement(jsonElement);
            }
            else if (jsonElement instanceof JsonObject)
            {
                JsonObject jsonObject = (JsonObject) jsonElement;
                JsonElement statusTagNode = jsonObject.get(STATUS_TAG);
                JsonElement methodNameNode = jsonObject.get(METHOD_NAME_TAG);
                if (methodNameNode == null)
                {
                    if (statusTagNode == null)
                    {
                        /*
                           If the json contains any payload without `methodName` or `status` identification, the fromJson shall parse only the payload, and set the operation as `payload`.
                           Ex:
                           {
                               "input1": "someInput",
                               "input2": "anotherInput"
                           }
                         */
                        operation = Operation.payload;
                        payload = getJsonObjectValue(jsonObject);
                    }
                    else
                    {
                        /*
                           If the json contains the `status` identification, the fromJson shall parse both status and payload, and set the operation as `response`.
                           Ex:
                           {
                               "status": 201,
                               "payload": {"AnyValidPayload" : "" }
                           }
                         */
                        operation = Operation.response;
                        if (statusTagNode.isJsonPrimitive())
                        {
                            status = statusTagNode.getAsInt();
                        }
                        JsonElement payloadNode = jsonObject.get(PAYLOAD_TAG);
                        if (payloadNode != null)
                        {
                            payload = resolveJsonElement(payloadNode);
                        }
                    }
                }
                else
                {
                    if (statusTagNode == null)
                    {
                        /*
                           If the json contains the `methodName` identification, the fromJson shall parse the full method, and set the operation as `invoke`.
                           Ex:
                           {
                               "methodName": "reboot",
                               "responseTimeoutInSeconds": 200,
                               "connectTimeoutInSeconds": 5,
                               "payload":
                               {
                                   "input1": "someInput",
                                   "input2": "anotherInput"
                               }
                           }
                         */
                        operation = Operation.invoke;
                        name = methodNameNode.getAsString();
                        JsonElement responseTimeoutNode = jsonObject.get(RESPONSE_TIMEOUT_IN_SECONDS_TAG);
                        if (responseTimeoutNode != null)
                        {
                            responseTimeout = responseTimeoutNode.getAsLong();

                        }
                        JsonElement connetionTimeoutNode = jsonObject.get(CONNECT_TIMEOUT_IN_SECONDS_TAG);
                        if (connetionTimeoutNode != null)
                        {
                            connectTimeout = connetionTimeoutNode.getAsLong();
                        }
                        JsonElement payloadNode = jsonObject.get(PAYLOAD_TAG);
                        if (payloadNode != null)
                        {
                            payload = resolveJsonElement(payloadNode);
                        }
                    }
                    else
                    {
                        /* Codes_SRS_METHODPARSER_21_008: [If the provided json is null, empty, or not valid, the fromJson shall throws IllegalArgumentException.] */
                        throw new IllegalArgumentException("Invoke method name and Status reported in the same json");
                    }
                }
            }
            else
            {
                // JSON null, since string is not empty, it shouldn't reach here
                throw new IllegalArgumentException("Invalid json.");
            }
        }
        catch (Exception ex)
        {
            /* Codes_SRS_METHODPARSER_21_008: [If the provided json is null, empty, or not valid, the fromJson shall throws IllegalArgumentException.] */
            throw new IllegalArgumentException("Malformed json.", ex);
        }
    }

    /**
     * Return an Object with the payload.
     *
     * @return An Object with the payload. It can be {@code null}.
     */
    public Object getPayload()
    {
        /* Codes_SRS_METHODPARSER_21_013: [The getPayload shall return an Object with the Payload in the parsed json.] */
        if (payload instanceof JsonElement && ((JsonElement) payload).isJsonPrimitive() && ((JsonPrimitive) payload).isString())
        {
            return ((JsonPrimitive) payload).getAsString();
        }
        return payload;
    }

    /**
     * Create a String with a json content that represents all the information in the method collection.
     *
     * @return String with the json content.
     * @throws IllegalArgumentException This exception is thrown if the one of the provided information do not fits the requirements.
     */
    public String toJson() throws IllegalArgumentException
    {
        /* Codes_SRS_METHODPARSER_21_014: [The toJson shall create a String with the full information in the method collection using json format, by using the toJsonElement.] */
        return toJsonElement().toString();
    }

    // Unchecked casts of Maps to Map<String, Object> are safe as long as service is returning valid twin json payloads. Since all json keys are Strings, all maps must be Map<String, Object>
    @SuppressWarnings("unchecked")
    private JsonElement jsonizePayload(Object payload)
    {
        if (payload == null)
        {
            return JsonNull.INSTANCE;
        }
        else if (payload instanceof JsonElement)
        {
            return (JsonElement) payload;
        }
        else if (payload instanceof Map)
        {
            JsonObject jsonObject = new JsonObject();
            Set<Entry<String, Object>> entrySet = ((Map<String, Object>) payload).entrySet();
            for (Entry<String, Object> entry : entrySet)
            {
                jsonObject.add(entry.getKey(), jsonizePayload(entry.getValue()));
            }
            return jsonObject;
        }
        else
        {
            JsonParser parser = new JsonParser();
            try
            {
                String json = payload.toString();
                JsonElement jsonElement = parser.parse(json);
                if (jsonElement.isJsonNull())
                {
                    return new JsonPrimitive(json);
                }
                else
                {
                    return jsonElement;
                }
            }
            catch (JsonSyntaxException e)
            {
                return new Gson().toJsonTree(payload);
            }
        }
    }

    /**
     * Create a JsonElement with a content that represents all the information in the method collection.
     *
     * @return JsonElement with the content.
     * @throws IllegalArgumentException This exception is thrown if the one of the provided information do not fits the requirements.
     */
    public JsonElement toJsonElement() throws IllegalArgumentException
    {
        /* Codes_SRS_METHODPARSER_21_015: [The toJsonElement shall include name as `methodName` in the json.] */
        /* Codes_SRS_METHODPARSER_21_016: [The toJsonElement shall include responseTimeout in seconds as `responseTimeoutInSeconds` in the json.] */
        /* Codes_SRS_METHODPARSER_21_017: [If the responseTimeout is null, the toJsonElement shall not include the `responseTimeoutInSeconds` in the json.] */
        /* Codes_SRS_METHODPARSER_21_031: [The toJsonElement shall include connectTimeout in seconds as `connectTimeoutInSeconds` in the json.] */
        /* Codes_SRS_METHODPARSER_21_032: [If the connectTimeout is null, the toJsonElement shall not include the `connectTimeoutInSeconds` in the json.] */
        /* Codes_SRS_METHODPARSER_21_018: [The class toJsonElement include payload as `payload` in the json.] */
        /* Codes_SRS_METHODPARSER_21_019: [If the payload is null, the toJsonElement shall include `payload` with value `null`.] */
        /* Codes_SRS_METHODPARSER_21_024: [The class toJsonElement include status as `status` in the json.] */
        /* Codes_SRS_METHODPARSER_21_025: [If the status is null, the toJsonElement shall include `status` as `null`.] */
        if (operation == Operation.invoke)
        {
            /*
               If the method operation is `invoke`, the toJsonElement shall include the full method information in the json.
               Ex:
               {
                   "methodName": "reboot",
                   "responseTimeoutInSeconds": 200,
                   "connectTimeoutInSeconds": 5,
                   "payload":
                   {
                       "input1": "someInput",
                       "input2": "anotherInput"
                   }
               }
             */
            if ((name == null) || name.isEmpty())
            {
                throw new IllegalArgumentException("cannot invoke method with null name");
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(METHOD_NAME_TAG, name);
            if (responseTimeout != null)
            {
                jsonObject.addProperty(RESPONSE_TIMEOUT_IN_SECONDS_TAG, responseTimeout);
            }
            if (connectTimeout != null)
            {
                jsonObject.addProperty(CONNECT_TIMEOUT_IN_SECONDS_TAG, connectTimeout);
            }
            jsonObject.add(PAYLOAD_TAG, jsonizePayload(payload));
            return jsonObject;
        }
        else if (operation == Operation.response)
        {
            /*
               If the method operation is `response`, the toJsonElement shall parse both status and payload.
               Ex:
               {
                   "status": 201,
                   "payload": {"AnyValidPayload" : "" }
               }
             */
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(STATUS_TAG, status);
            jsonObject.add(PAYLOAD_TAG, jsonizePayload(payload));
            return jsonObject;
        }
        else if (operation == Operation.payload)
        {
            /*
               If the method operation is `payload`, the toJsonElement shall parse only the payload.
               Ex:
               {
                   "input1": "someInput",
                   "input2": "anotherInput"
               }
             */
            return jsonizePayload(payload);
        } else
        {
            /* Codes_SRS_METHODPARSER_21_036: [If the method operation is `none`, the toJsonElement shall throw IllegalArgumentException.] */
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
        if ((key == null) || (key.isEmpty()))
        {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }

        if (key.length()>128)
        {
            throw new IllegalArgumentException("Key cannot be longer than 128 characters");
        }

        if (key.contains("$") || key.contains(".") ||key.contains(" "))
        {
            throw new IllegalArgumentException("Key cannot contain '$', '.', or space");
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
        if (timeout<0)
        {
            throw new IllegalArgumentException("Negative timeout");
        }
    }
}