// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

import java.util.Map;

/**
 * INNER TWIN CLASS
 *
 * Twin Properties representation
 *
 * This class is part of the Twin. It is necessary to generate the properties json.
 */
public class TwinProperties
{
    private TwinProperty desired = new TwinProperty();
    private TwinProperty reported = new TwinProperty();

    private static final String DESIRED_TAG = "desired";
    private static final String REPORTED_TAG = "reported";

    protected void enableDesiredMetadata()
    {
        this.desired.enableMetadata();
    }

    protected void enableReportedMetadata()
    {
        this.reported.enableMetadata();
    }


    protected JsonElement updateDesired(Map<String, Object> property)
    {
        /* Codes_SRS_TWIN_21_021: [The updateDesiredProperty shall add all provided properties to the Desired property.] */
        return this.desired.update(property);
    }

    protected JsonElement updateReported(Map<String, Object> property)
    {
        /* Codes_SRS_TWIN_21_025: [The updateReportedProperty shall add all provided properties to the Reported property.] */
        return this.reported.update(property);
    }

    protected void updateDesired(String json, TwinPropertiesChangeCallback onDesiredCallback)
    {
        this.desired.update(json, onDesiredCallback);
    }

    protected void updateReported(String json, TwinPropertiesChangeCallback onDesiredCallback)
    {
        this.reported.update(json, onDesiredCallback);
    }


    protected Integer getDesiredVersion()
    {
        return this.desired.getVersion();
    }

    protected Integer getReportedVersion()
    {
        return this.reported.getVersion();
    }


    protected Map<String, Object> getDesiredPropertyMap()
    {
        return this.desired.getPropertyMap();
    }

    protected Map<String, Object> getReportedPropertyMap()
    {
        return this.reported.getPropertyMap();
    }


    protected String toJson()
    {
        return toJsonElement().toString();
    }


    protected JsonElement toJsonElement()
    {
        /* Codes_SRS_TWIN_21_017: [The toJsonElement shall return a JsonElement with information in the Twin using json format.] */
        JsonObject propertiesJson = new JsonObject();

        /* Codes_SRS_TWIN_21_087: [**The toJsonElement shall include the `desired` property in the json even if it has no content.] */
        JsonElement desiredElement = this.desired.toJsonElement();
        propertiesJson.add(DESIRED_TAG, desiredElement);

        /* Codes_SRS_TWIN_21_088: [**The toJsonElement shall include the `reported` property in the json even if it has no content.] */
        JsonElement reportedElement = this.reported.toJsonElement();
        propertiesJson.add(REPORTED_TAG, reportedElement);

        return (JsonElement) propertiesJson;
    }

    protected void update(Map<String, Object> jsonTree,
                       TwinPropertiesChangeCallback onDesiredCallback, TwinPropertiesChangeCallback onReportedCallback)
            throws IllegalArgumentException
    {
        for(Map.Entry<String, Object> entry : jsonTree.entrySet())
        {
            if(entry.getKey().equals(DESIRED_TAG))
            {
                desired.update((LinkedTreeMap<String, Object>) entry.getValue(), onDesiredCallback);
            }
            else if(entry.getKey().equals(REPORTED_TAG))
            {
                reported.update((LinkedTreeMap<String, Object>) entry.getValue(), onReportedCallback);
            }
            else
            {
                throw new IllegalArgumentException("Invalid Property");
            }
        }
    }
}
