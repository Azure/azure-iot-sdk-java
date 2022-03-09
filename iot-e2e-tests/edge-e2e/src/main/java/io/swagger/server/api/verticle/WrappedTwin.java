package io.swagger.server.api.verticle;

import com.microsoft.azure.sdk.iot.service.twin.Pair;
import com.microsoft.azure.sdk.iot.service.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import io.vertx.core.json.JsonObject;

import java.util.Set;

public class WrappedTwin
{
    final Twin twin;

    public WrappedTwin(Twin twin)
    {
        this.twin = twin;
    }

    private JsonObject mapToJson(TwinCollection map)
    {
        return new JsonObject(map.toJsonElement().toString());
    }

    public JsonObject toJsonObject()
    {

        return new JsonObject()
                .put("properties", new JsonObject()
                    .put("desired", mapToJson(setToMap(this.twin.getDesiredProperties())))
                    .put("reported", mapToJson(setToMap(this.twin.getReportedProperties()))));
    }

    private TwinCollection setToMap(Set<Pair> set)
    {
        TwinCollection map = new TwinCollection();

        if (set != null)
        {
            for (Pair pair : set)
            {
                map.put(pair.getKey(), pair.getValue());
            }
        }

        return map;
    }

}
