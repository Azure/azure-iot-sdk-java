package io.swagger.server.api.verticle;

import io.swagger.server.api.model.Certificate;
import io.swagger.server.api.model.ConnectResponse;
import io.swagger.server.api.model.RoundtripMethodCallBody;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface DeviceApi  {
    //PUT_device_connect_transportType
    void deviceConnectTransportTypePut(String transportType, String connectionString, Certificate caCertificate, Handler<AsyncResult<ConnectResponse>> handler);

    //PUT_device_connectionId_disconnect
    void deviceConnectionIdDisconnectPut(String connectionId, Handler<AsyncResult<Void>> handler);

    //PUT_device_connectionId_enableMethods
    void deviceConnectionIdEnableMethodsPut(String connectionId, Handler<AsyncResult<Void>> handler);

    //PUT_device_connectionId_roundtripMethodCall_methodName
    void deviceConnectionIdRoundtripMethodCallMethodNamePut(String connectionId, String methodName, RoundtripMethodCallBody requestAndResponse, Handler<AsyncResult<Void>> handler);
}
