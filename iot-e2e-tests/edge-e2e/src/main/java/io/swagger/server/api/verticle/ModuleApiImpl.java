package io.swagger.server.api.verticle;

import glue.ModuleGlue;
import io.swagger.server.api.model.Certificate;
import io.swagger.server.api.model.ConnectResponse;
import io.swagger.server.api.model.RoundtripMethodCallBody;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;


public class ModuleApiImpl implements ModuleApi
{
    public static ModuleGlue _moduleGlue = new ModuleGlue();

    @Override
    public void moduleConnectFromEnvironmentTransportTypePut(String transportType, Handler<AsyncResult<ConnectResponse>> handler)
    {
        _moduleGlue.connectFromEnvironment(transportType, handler);
    }

    @Override
    public void moduleConnectTransportTypePut(String transportType, String connectionString, Certificate caCertificate, Handler<AsyncResult<ConnectResponse>> handler)
    {
        _moduleGlue.connect(transportType, connectionString, caCertificate, handler);
    }


    @Override
    public void moduleConnectionIdDeviceMethodDeviceIdPut(String connectionId, String deviceId, Object methodInvokeParameters, Handler<AsyncResult<Object>> handler)
    {
        _moduleGlue.invokeDeviceMethod(connectionId, deviceId, methodInvokeParameters, handler);
    }

    @Override
    public void moduleConnectionIdDisconnectPut(String connectionId, Handler<AsyncResult<Void>> handler)
    {
        _moduleGlue.disconnect(connectionId, handler);
    }

    @Override
    public void moduleConnectionIdEnableInputMessagesPut(String connectionId, Handler<AsyncResult<Void>> handler)
    {
        _moduleGlue.enableInputMessages(connectionId, handler);
    }

    @Override
    public void moduleConnectionIdEnableMethodsPut(String connectionId, Handler<AsyncResult<Void>> handler)
    {
        _moduleGlue.enableMethods(connectionId, handler);
    }

    @Override
    public void moduleConnectionIdEnableTwinPut(String connectionId, Handler<AsyncResult<Void>> handler)
    {
        _moduleGlue.enableTwin(connectionId, handler);
    }

    @Override
    public void moduleConnectionIdEventPut(String connectionId, String eventBody, Handler<AsyncResult<Void>> handler)
    {
        _moduleGlue.sendEvent(connectionId, eventBody, handler);
    }

    @Override
    public void moduleConnectionIdInputMessageInputNameGet(String connectionId, String inputName, Handler<AsyncResult<String>> handler)
    {
        _moduleGlue.waitForInputMessage(connectionId, inputName, handler);
    }

    @Override
    public void moduleConnectionIdModuleMethodDeviceIdModuleIdPut(String connectionId, String deviceId, String moduleId, Object methodInvokeParameters, Handler<AsyncResult<Object>> handler)
    {
        _moduleGlue.invokeModuleMethod(connectionId, deviceId, moduleId, methodInvokeParameters, handler);
    }

    @Override
    public void moduleConnectionIdOutputEventOutputNamePut(String connectionId, String outputName, String eventBody, Handler<AsyncResult<Void>> handler)
    {
        _moduleGlue.sendOutputEvent(connectionId, outputName, eventBody, handler);
    }

    @Override
    public void moduleConnectionIdRoundtripMethodCallMethodNamePut(String connectionId, String methodName, RoundtripMethodCallBody requestAndResponse, Handler<AsyncResult<Void>> handler)
    {
        _moduleGlue.roundtripMethodCall(connectionId, methodName, requestAndResponse, handler);
    }

    @Override
    public void moduleConnectionIdTwinDesiredPropPatchGet(String connectionId, Handler<AsyncResult<Object>> handler)
    {
        _moduleGlue.waitForDesiredPropertyPatch(connectionId, handler);
    }

    @Override
    public void moduleConnectionIdTwinGet(String connectionId, Handler<AsyncResult<Object>> handler)
    {
        _moduleGlue.getTwin(connectionId, handler);
    }

    @Override
    public void moduleConnectionIdTwinPatch(String connectionId, Object props, Handler<AsyncResult<Void>> handler)
    {
        _moduleGlue.sendTwinPatch(connectionId, props, handler);
    }

}
