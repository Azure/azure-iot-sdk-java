package io.swagger.server.api.verticle;

import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import io.vertx.core.json.JsonObject;

public class WrappedDeviceTwinDevice extends DeviceTwinDevice
{
    public WrappedDeviceTwinDevice(String deviceId, String moduleId)
    {
        super(deviceId, moduleId);
    }

    private JsonObject mapToJson(TwinCollection map)
    {
        return new JsonObject(map.toJsonElement().toString());
    }

    public JsonObject toJsonObject()
    {
        JsonObject twinObj = new JsonObject()
                .put("properties", new JsonObject()
                    .put("desired", mapToJson(this.getDesiredMap()))
                    .put("reported", mapToJson(this.getReportedMap())));

        return twinObj;
    }

}
