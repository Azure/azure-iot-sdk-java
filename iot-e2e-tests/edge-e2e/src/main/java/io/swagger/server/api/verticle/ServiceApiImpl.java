package io.swagger.server.api.verticle;

import glue.ServiceGlue;
import io.swagger.server.api.model.ConnectResponse;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;


public class ServiceApiImpl implements ServiceApi
{
    public static ServiceGlue _serviceGlue = new ServiceGlue();

    @Override
    public void serviceConnectPut(String connectionString, Handler<AsyncResult<ConnectResponse>> handler)
    {
        this._serviceGlue.connect(connectionString, handler);
    }

    @Override
    public void serviceConnectionIdDeviceMethodDeviceIdPut(String connectionId, String deviceId, Object methodInvokeParameters, Handler<AsyncResult<Object>> handler)
    {
        this._serviceGlue.invokeDeviceMethod(connectionId, deviceId, methodInvokeParameters, handler);
    }

    @Override
    public void serviceConnectionIdDisconnectPut(String connectionId, Handler<AsyncResult<Void>> handler)
    {
        this._serviceGlue.disconnect(connectionId, handler);
    }

    @Override
    public void serviceConnectionIdModuleMethodDeviceIdModuleIdPut(String connectionId, String deviceId, String moduleId, Object methodInvokeParameters, Handler<AsyncResult<Object>> handler)
    {
        this._serviceGlue.invokeModuleMethod(connectionId, deviceId, moduleId, methodInvokeParameters, handler);
    }
}
