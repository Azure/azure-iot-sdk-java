package io.swagger.server.api.verticle;

import io.swagger.server.api.model.Certificate;
import io.swagger.server.api.model.ConnectResponse;
import io.swagger.server.api.model.RoundtripMethodCallBody;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

@SuppressWarnings("ALL")
public class DeviceApiImpl implements DeviceApi
{
    // These implementations are empty on purpose for future expansion.
    //
    // The short story is that there is a minor piece of functionality that is not yet being tested.  Specifically, we are not yet
    // testing that a Java leaf device that is sitting behind IoTEdge (using GateWayHostName=) can handle a method call
    // (from the service or from a module).  We still test if the Java SDK can invoke a method on a leaf device (using the Node SDK for the leaf device),
    // but we don't test if it works with a Java leaf device.
    //
    // This oversight was discovered late, and, because of time, we had to skip this test for most SDKs.  Once this implementation is filled in, we can switch the test
    // runner so this gets tested.
    //
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
