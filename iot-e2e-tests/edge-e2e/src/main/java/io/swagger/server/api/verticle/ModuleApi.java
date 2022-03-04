package io.swagger.server.api.verticle;

import io.swagger.server.api.model.Certificate;
import io.swagger.server.api.model.ConnectResponse;
import io.swagger.server.api.model.RoundtripMethodCallBody;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface ModuleApi  {
    //PUT_module_connectFromEnvironment_transportType
    void moduleConnectFromEnvironmentTransportTypePut(String transportType, Handler<AsyncResult<ConnectResponse>> handler);
    
    //PUT_module_connect_transportType
    void moduleConnectTransportTypePut(String transportType, String connectionString, Certificate caCertificate, Handler<AsyncResult<ConnectResponse>> handler);
    
    //PUT_module_connectionId_deviceMethod_deviceId
    void moduleConnectionIdDeviceMethodDeviceIdPut(String connectionId, String deviceId, Object methodInvokeParameters, Handler<AsyncResult<Object>> handler);
    
    //PUT_module_connectionId_disconnect
    void moduleConnectionIdDisconnectPut(String connectionId, Handler<AsyncResult<Void>> handler);
    
    //PUT_module_connectionId_enableInputMessages
    void moduleConnectionIdEnableInputMessagesPut(String connectionId, Handler<AsyncResult<Void>> handler);
    
    //PUT_module_connectionId_enableMethods
    void moduleConnectionIdEnableMethodsPut(String connectionId, Handler<AsyncResult<Void>> handler);
    
    //PUT_module_connectionId_enableTwin
    void moduleConnectionIdEnableTwinPut(String connectionId, Handler<AsyncResult<Void>> handler);
    
    //PUT_module_connectionId_event
    void moduleConnectionIdEventPut(String connectionId, String eventBody, Handler<AsyncResult<Void>> handler);
    
    //GET_module_connectionId_inputMessage_inputName
    void moduleConnectionIdInputMessageInputNameGet(String connectionId, String inputName, Handler<AsyncResult<String>> handler);
    
    //PUT_module_connectionId_moduleMethod_deviceId_moduleId
    void moduleConnectionIdModuleMethodDeviceIdModuleIdPut(String connectionId, String deviceId, String moduleId, Object methodInvokeParameters, Handler<AsyncResult<Object>> handler);
    
    //PUT_module_connectionId_outputEvent_outputName
    void moduleConnectionIdOutputEventOutputNamePut(String connectionId, String outputName, String eventBody, Handler<AsyncResult<Void>> handler);
    
    //PUT_module_connectionId_roundtripMethodCall_methodName
    void moduleConnectionIdRoundtripMethodCallMethodNamePut(String connectionId, String methodName, RoundtripMethodCallBody requestAndResponse, Handler<AsyncResult<Void>> handler);
    
    //GET_module_connectionId_twinDesiredPropPatch
    void moduleConnectionIdTwinDesiredPropPatchGet(String connectionId, Handler<AsyncResult<Object>> handler);
    
    //GET_module_connectionId_twin
    void moduleConnectionIdTwinGet(String connectionId, Handler<AsyncResult<Object>> handler);
    
    //PATCH_module_connectionId_twin
    void moduleConnectionIdTwinPatch(String connectionId, Object props, Handler<AsyncResult<Void>> handler);
    
}
