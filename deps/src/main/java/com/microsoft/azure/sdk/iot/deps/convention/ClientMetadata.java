// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.convention;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.twin.TwinMetadata;
import com.microsoft.azure.sdk.iot.deps.util.Tools;
import lombok.Getter;

import java.util.Date;
import java.util.Map;

/**
 * Representation of a single metadata item for the {@code ClientPropertyCollection}.
 *
 * <p> The metadata is a set of pairs lastUpdated/lastUpdatedVersion for each
 * property and sub-property in the client. It is optionally provided by
 * the service and the clients can only ready it.
 *
 * <p> This class store the Date and Version for each entity in the {@code ClientPropertyCollection}.
 *
 * <p> For instance, the following is a valid TwinCollection with its metadata.
 * <pre>
 * {@code
 *  "$metadata":{
 *      "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *      "$lastUpdatedVersion":4,
 *      "MaxSpeed":{
 *          "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *          "$lastUpdatedVersion":3,
 *          "$lastUpdatedBy": "newconfig",
 *          "$lastUpdatedByDigest": "637570574076206429",
 *          "Value":{
 *              "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *              "$lastUpdatedVersion":5
 *          },
 *          "NewValue":{
 *              "$lastUpdated":"2017-09-21T02:07:44.238Z",
 *              "$lastUpdatedVersion":5
 *          }
 *      }
 *  }
 * }
 * </pre>
 *
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-device-twins">Understand and use device twins in IoT Hub</a>
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iothub/devicetwinapi">Device Twin Api</a>
 */
// Unchecked casts of Maps to Map<String, Object> are safe as long as service is returning valid twin json payloads. Since all json keys are Strings, all maps must be Map<String, Object>
@SuppressWarnings("unchecked")
public class ClientMetadata extends TwinMetadata {
    public ClientMetadata(ClientMetadata clientMetadata) {
        super((TwinMetadata)clientMetadata);
    }

    public ClientMetadata(String lastUpdated, Integer lastUpdatedVersion, String lastUpdatedBy, String lastUpdatedByDigest) {
        super(lastUpdated, lastUpdatedVersion, lastUpdatedBy, lastUpdatedByDigest);
    }

    public static ClientMetadata tryExtractFromMap(Object metadata) {
        return (ClientMetadata) TwinMetadata.tryExtractFromMap(metadata);
    }
}
