package io.swagger.server.api.verticle;

import io.swagger.server.api.model.Certificate;
import io.swagger.server.api.model.ConnectResponse;
import io.swagger.server.api.model.RoundtripMethodCallBody;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public class DeviceApiImpl implements DeviceApi
{
    @Override
    public void deviceConnectTransportTypePut(String transportType, String connectionString, Certificate caCertificate, Handler<AsyncResult<ConnectResponse>> handler)
    {

    }

    @Override
    public void deviceConnectionIdDisconnectPut(String connectionId, Handler<AsyncResult<Void>> handler)
    {

    }

    @Override
    public void deviceConnectionIdEnableMethodsPut(String connectionId, Handler<AsyncResult<Void>> handler)
    {

    }

    @Override
    public void deviceConnectionIdRoundtripMethodCallMethodNamePut(String connectionId, String methodName, RoundtripMethodCallBody requestAndResponse, Handler<AsyncResult<Void>> handler)
    {

    }
}
