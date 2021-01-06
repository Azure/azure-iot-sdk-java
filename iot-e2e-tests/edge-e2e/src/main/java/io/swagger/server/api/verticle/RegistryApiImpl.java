package io.swagger.server.api.verticle;

import glue.RegistryGlue;
import io.swagger.server.api.model.ConnectResponse;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public class RegistryApiImpl implements RegistryApi
{
    public static RegistryGlue _registryGlue = new RegistryGlue();

    @Override
    public void registryConnectPut(String connectionString, Handler<AsyncResult<ConnectResponse>> handler)
    {
        _registryGlue.connect(connectionString, handler);
    }

    @Override
    public void registryConnectionIdDisconnectPut(String connectionId, Handler<AsyncResult<Void>> handler)
    {
        _registryGlue.disconnect(connectionId, handler);
    }

    @Override
    public void registryConnectionIdModuleTwinDeviceIdModuleIdGet(String connectionId, String deviceId, String moduleId, Handler<AsyncResult<Object>> handler)
    {
        _registryGlue.getModuleTwin(connectionId, deviceId, moduleId, handler);
    }

    @Override
    public void registryConnectionIdModuleTwinDeviceIdModuleIdPatch(String connectionId, String deviceId, String moduleId, Object props, Handler<AsyncResult<Void>> handler)
    {
        _registryGlue.sendModuleTwinPatch(connectionId, deviceId, moduleId, props, handler);
    }
}
