package io.swagger.server.api.verticle;

import io.swagger.server.api.model.ConnectResponse;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface ServiceApi  {
    //PUT_service_connect
    void serviceConnectPut(String connectionString, Handler<AsyncResult<ConnectResponse>> handler);
    
    //PUT_service_connectionId_deviceMethod_deviceId
    void serviceConnectionIdDeviceMethodDeviceIdPut(String connectionId, String deviceId, Object methodInvokeParameters, Handler<AsyncResult<Object>> handler);
    
    //PUT_service_connectionId_disconnect_
    void serviceConnectionIdDisconnectPut(String connectionId, Handler<AsyncResult<Void>> handler);
    
    //PUT_service_connectionId_moduleMethod_deviceId_moduleId
    void serviceConnectionIdModuleMethodDeviceIdModuleIdPut(String connectionId, String deviceId, String moduleId, Object methodInvokeParameters, Handler<AsyncResult<Object>> handler);
    
}
