package io.swagger.server.api.verticle;

import io.swagger.server.api.model.ConnectResponse;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface EventhubApi  {
    //PUT_eventhub_connect
    void eventhubConnectPut(String connectionString, Handler<AsyncResult<ConnectResponse>> handler);
    
    //GET_eventhub_connectionId_deviceTelemetry_deviceId
    void eventhubConnectionIdDeviceTelemetryDeviceIdGet(String connectionId, String deviceId, Handler<AsyncResult<String>> handler);
    
    //PUT_eventhub_connectionId_disconnect_
    void eventhubConnectionIdDisconnectPut(String connectionId, Handler<AsyncResult<Void>> handler);
    
    //PUT_eventhub_connectionId_enableTelemetry
    void eventhubConnectionIdEnableTelemetryPut(String connectionId, Handler<AsyncResult<Void>> handler);
    
}
