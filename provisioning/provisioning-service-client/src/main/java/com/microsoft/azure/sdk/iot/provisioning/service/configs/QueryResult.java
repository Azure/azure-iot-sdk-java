// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.*;
import com.microsoft.azure.sdk.iot.provisioning.service.Tools;

/**
 * Representation of a single Device Provisioning Service query response with a JSON deserializer.
 *
 * <p> It is the result of any query for the provisioning service. This class will parse the result and
 *     return it in a best format possible. For the known formats in {@link QueryResultType}, you can
 *     just cast the items. In case of <b>unknown</b> type, the items will contain a list of {@code Strings}
 *     and you shall parse it by your own.
 *
 * <p> The provisioning service query result is composed by 2 system properties and a body. This class exposes
 *     it with 3 getters, {@link #getType()}, {@link #getContinuationToken()}, and {@link #getItems()}.
 *
 * <p> The system properties are:
 * <dl>
 *     <dt><b>type:</b>
 *     <dd>Identify the type of the content in the body. You can use it to cast the objects
 *         in the items list. See {@link QueryResultType} for the possible types and classes
 *         to cast.
 *     <dt><b>continuationToken:</b>
 *     <dd>Contains the token the uniquely identify the next page of information. The
 *         service will return the next page of this query when you send a new query with
 *         this token,
 * </dl>
 *
 * <p> And the body is a JSON list of the specific <b>type</b>. For instance, if the system
 *     property type is IndividualEnrollment, the body will look like:
 * <pre>
 * {@code
 * [
 *     {
 *         "registrationId":"validRegistrationId-1",
 *         "deviceId":"ContosoDevice-1",
 *         "attestation":{
 *             "type":"tpm",
 *             "tpm":{
 *                 "endorsementKey":"validEndorsementKey"
 *             }
 *         },
 *         "iotHubHostName":"ContosoIoTHub.azure-devices.net",
 *         "provisioningStatus":"enabled"
 *     },
 *     {
 *         "registrationId":"validRegistrationId-2",
 *         "deviceId":"ContosoDevice-2",
 *         "attestation":{
 *             "type":"tpm",
 *            "tpm":{
 *                 "endorsementKey":"validEndorsementKey"
 *             }
 *         },
 *         "iotHubHostName":"ContosoIoTHub.azure-devices.net",
 *         "provisioningStatus":"enabled"
 *     }
 * ]
 * }
 * </pre>
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 */
public class QueryResult
{
    // the query type
    private transient static final String TYPE_TAG = "type";
    private QueryResultType type;

    // the list of items in the query result
    private transient static final String ITEMS_TAG = "items";
    private Object[] items;

    // the continuation token for the query
    private transient static final String CONTINUATION_TOKEN_TAG = "continuationToken";
    private String continuationToken;

    /**
     * CONSTRUCTOR
     *
     * <p> This constructor creates an instance of the QueryResult.
     *
     * @param type the {@code String} with type of the content in the body. It cannot be {@code null}
     * @param body the {@code String} with the body in a JSON list format. It cannot be {@code null}, or empty, if the type is different than `unknown`.
     * @param continuationToken the {@code String} with the continuation token. It can be {@code null}.
     * @throws IllegalArgumentException If one of the provided parameters is invalid.
     */
    public QueryResult(String type, String body, String continuationToken)
    {
        /* SRS_QUERY_RESULT_21_001: [The constructor shall throw IllegalArgumentException if the provided type is null, empty, or not parsed to QueryResultType.] */
        QueryResultType queryResultType = QueryResultType.fromString(type);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();

        /* SRS_QUERY_RESULT_21_002: [The constructor shall throw IllegalArgumentException if the provided body is null or empty and the type is not `unknown`.] */
        if((queryResultType != QueryResultType.UNKNOWN) && Tools.isNullOrEmpty(body))
        {
            throw new IllegalArgumentException("body cannot be null or empty");
        }

        /* SRS_QUERY_RESULT_21_003: [The constructor shall throw JsonSyntaxException if the JSON is invalid.] */
        switch(queryResultType)
        {
            case ENROLLMENT:
                /* SRS_QUERY_RESULT_21_004: [If the type is `enrollment`, the constructor shall parse the body as IndividualEnrollment[].] */
                this.items = gson.fromJson(body, IndividualEnrollment[].class);
                break;
            case ENROLLMENT_GROUP:
                /* SRS_QUERY_RESULT_21_005: [If the type is `enrollmentGroup`, the constructor shall parse the body as EnrollmentGroup[].] */
                this.items = gson.fromJson(body, EnrollmentGroup[].class);
                break;
            case DEVICE_REGISTRATION:
                /* SRS_QUERY_RESULT_21_006: [If the type is `deviceRegistration`, the constructor shall parse the body as DeviceRegistrationState[].] */
                this.items = gson.fromJson(body, DeviceRegistrationState[].class);
                break;
            default:
                if(body == null)
                {
                    /* SRS_QUERY_RESULT_21_007: [If the type is `unknown`, and the body is null, the constructor shall set `items` as null.] */
                    this.items = null;
                }
                else
                {
                    try
                    {
                        /* SRS_QUERY_RESULT_21_008: [If the type is `unknown`, the constructor shall try to parse the body as JsonObject[].] */
                        this.items = gson.fromJson(body, JsonObject[].class);
                    }
                    catch (JsonSyntaxException e1)
                    {
                        try
                        {
                            /* SRS_QUERY_RESULT_21_009: [If the type is `unknown`, and the constructor failed to parse the body as JsonObject[], it shall try to parse the body as JsonPrimitive[].] */
                            this.items = gson.fromJson(body, JsonPrimitive[].class);
                        }
                        catch (JsonSyntaxException e2)
                        {
                            /* SRS_QUERY_RESULT_21_010: [If the type is `unknown`, and the constructor failed to parse the body as JsonObject[] and JsonPrimitive[], it shall return the body as a single string in the items.] */
                            this.items = new String[1];
                            this.items[0] = body;
                        }
                    }
                }
                break;
        }

        /* SRS_QUERY_RESULT_21_011: [The constructor shall store the provided parameters `type` and `continuationToken`.] */
        this.type = queryResultType;
        this.continuationToken = continuationToken;
    }

    /**
     * Getter for the type.
     *
     * @return The {@code QueryResultType} with the type of the items Objects.
     */
    public QueryResultType getType()
    {
        /* SRS_QUERY_RESULT_21_012: [The getType shall return the stored type.] */
        return this.type;
    }

    /**
     * Getter for the continuationToken.
     *
     * @return The {@code String} with the unique token that identify the next page of this query.
     */
    public String getContinuationToken()
    {
        /* SRS_QUERY_RESULT_21_013: [The getContinuationToken shall return the stored continuationToken.] */
        return this.continuationToken;
    }

    /**
     * Getter for the items.
     *
     * @return The {@code Object[]} with the results of the query. You can cast it using the type.
     * @see QueryResultType
     */
    public Object[] getItems()
    {
        /* SRS_QUERY_RESULT_21_014: [The getItems shall return the stored items.] */
        return this.items;
    }

    /**
     * Creates a pretty print JSON with the content of this class and subclasses.
     *
     * <p>The result of this function is <b>not</b> a valid JSON for the provisioning service, it is just
     *    to provide a way to print its content.
     *
     * @return The {@code String} with the pretty print JSON.
     */
    @Override
    public String toString()
    {
        /* SRS_QUERY_RESULT_21_015: [The toString shall return a String with the information in this class in a pretty print JSON.] */
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        return gson.toJson(this);
    }

    /**
     * Empty constructor
     *
     * <p>
     *     Used only by the tools that will deserialize this class.
     * </p>
     */
    @SuppressWarnings("unused")
    protected QueryResult()
    {
        /* SRS_QUERY_RESULT_21_016: [The EnrollmentGroup shall provide an empty constructor to make GSON happy.] */
    }
}
