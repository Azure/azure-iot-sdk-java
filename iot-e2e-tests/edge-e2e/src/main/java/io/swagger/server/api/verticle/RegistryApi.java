package io.swagger.server.api.verticle;

import io.swagger.server.api.model.ConnectResponse;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface RegistryApi  {
    //PUT_registry_connect
    void registryConnectPut(String connectionString, Handler<AsyncResult<ConnectResponse>> handler);
    
    //PUT_registry_connectionId_disconnect_
    void registryConnectionIdDisconnectPut(String connectionId, Handler<AsyncResult<Void>> handler);
    
    //GET_registry_connectionId_moduleTwin_deviceId_moduleId
    void registryConnectionIdModuleTwinDeviceIdModuleIdGet(String connectionId, String deviceId, String moduleId, Handler<AsyncResult<Object>> handler);
    
    //PATCH_registry_connectionId_moduleTwin_deviceId_moduleId
    void registryConnectionIdModuleTwinDeviceIdModuleIdPatch(String connectionId, String deviceId, String moduleId, Object props, Handler<AsyncResult<Void>> handler);
    
}
