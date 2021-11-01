package io.swagger.server.api.verticle;

import com.microsoft.azure.sdk.iot.service.devicetwin.Twin;
import com.microsoft.azure.sdk.iot.service.devicetwin.TwinCollection;
import io.vertx.core.json.JsonObject;

public class WrappedTwin extends Twin
{
    public WrappedTwin(String deviceId, String moduleId)
    {
        super(deviceId, moduleId);
    }

    private JsonObject mapToJson(TwinCollection map)
    {
        return new JsonObject(map.toJsonElement().toString());
    }

    public JsonObject toJsonObject()
    {

        return new JsonObject()
                .put("properties", new JsonObject()
                    .put("desired", mapToJson(this.getDesiredMap()))
                    .put("reported", mapToJson(this.getReportedMap())));
    }

}
