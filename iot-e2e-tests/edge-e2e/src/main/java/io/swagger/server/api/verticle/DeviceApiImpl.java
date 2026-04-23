package io.swagger.server.api.verticle;

import com.microsoft.azure.sdk.iot.device.ClientOptions;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.twin.DirectMethodPayload;
import com.microsoft.azure.sdk.iot.device.twin.MethodCallback;
import io.swagger.server.api.MainApiException;
import io.swagger.server.api.model.Certificate;
import io.swagger.server.api.model.ConnectResponse;
import io.swagger.server.api.model.RoundtripMethodCallBody;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.LinkedHashMap;

@SuppressWarnings("ALL")
public class DeviceApiImpl implements DeviceApi
{
    private final HashMap<String, DeviceClient> _map = new HashMap<>();
    private int _clientCount = 0;

    // State for roundtrip method call
    private Handler<AsyncResult<Void>> _methodHandler;
    private String _methodName;
    private String _methodRequestBody;
    private String _methodResponseBody;
    private int _methodStatusCode;

    private IotHubClientProtocol transportFromString(String protocolStr)
    {
        if ("amqp".equals(protocolStr))       return IotHubClientProtocol.AMQPS;
        if ("mqtt".equals(protocolStr))       return IotHubClientProtocol.MQTT;
        if ("amqpws".equals(protocolStr))     return IotHubClientProtocol.AMQPS_WS;
        if ("mqttws".equals(protocolStr))     return IotHubClientProtocol.MQTT_WS;
        if ("http".equals(protocolStr))       return IotHubClientProtocol.HTTPS;
        return null;
    }

    private DeviceClient getClient(String connectionId)
    {
        return _map.getOrDefault(connectionId, null);
    }

    private com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse handleMethodInvocation(String methodName, DirectMethodPayload methodData, Object context)
    {
        System.out.printf("device method %s called%n", methodName);
        if (methodName.equals(this._methodName))
        {
            String methodDataString;
            try
            {
                methodDataString = Json.mapper.readValue(new String((byte[]) methodData, StandardCharsets.UTF_8), String.class);
            }
            catch (IOException e)
            {
                this._methodHandler.handle(Future.failedFuture(e));
                resetMethodState();
                return new com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse(500, "exception parsing methodData");
            }
            if (methodDataString.equals(this._methodRequestBody) ||
                Json.encode(methodDataString).equals(this._methodRequestBody))
            {
                this._methodHandler.handle(Future.succeededFuture());
                resetMethodState();
                return new com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse(this._methodStatusCode, this._methodResponseBody);
            }
            else
            {
                this._methodHandler.handle(Future.failedFuture("methodData does not match"));
                resetMethodState();
                return new com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse(500, "methodData not received as expected");
            }
        }
        else
        {
            if (this._methodHandler != null)
            {
                this._methodHandler.handle(Future.failedFuture("unexpected call: " + methodName));
                resetMethodState();
            }
            return new com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse(404, "method " + methodName + " not handled");
        }
    }

    private void resetMethodState()
    {
        _methodHandler = null;
        _methodName = null;
        _methodRequestBody = null;
        _methodResponseBody = null;
        _methodStatusCode = 0;
    }

    @Override
    public void deviceConnectTransportTypePut(String transportType, String connectionString, Certificate caCertificate, Handler<AsyncResult<ConnectResponse>> handler)
    {
        System.out.printf("DeviceApiImpl.connect called with transport %s%n", transportType);
        IotHubClientProtocol protocol = transportFromString(transportType);
        if (protocol == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid transport")));
            return;
        }
        try
        {
            String cert = caCertificate != null ? caCertificate.getCert() : null;
            ClientOptions.ClientOptionsBuilder optionsBuilder = ClientOptions.builder();
            if (cert != null && !cert.isEmpty())
            {
                optionsBuilder.sslContext(IotHubSSLContext.getSSLContextFromFile(cert));
            }
            DeviceClient client = new DeviceClient(connectionString, protocol, optionsBuilder.build());
            client.open(false);
            this._clientCount++;
            String connectionId = "deviceClient_" + this._clientCount;
            this._map.put(connectionId, client);
            ConnectResponse cr = new ConnectResponse();
            cr.setConnectionId(connectionId);
            handler.handle(Future.succeededFuture(cr));
        }
        catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException | IotHubClientException e)
        {
            handler.handle(Future.failedFuture(e));
        }
    }

    @Override
    public void deviceConnectionIdDisconnectPut(String connectionId, Handler<AsyncResult<Void>> handler)
    {
        System.out.printf("DeviceApiImpl.disconnect called for %s%n", connectionId);
        DeviceClient client = getClient(connectionId);
        if (client != null)
        {
            client.close();
            _map.remove(connectionId);
        }
        handler.handle(Future.succeededFuture());
    }

    @Override
    public void deviceConnectionIdEnableMethodsPut(String connectionId, Handler<AsyncResult<Void>> handler)
    {
        System.out.printf("DeviceApiImpl.enableMethods called for %s%n", connectionId);
        DeviceClient client = getClient(connectionId);
        if (client == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid connection id")));
            return;
        }
        try
        {
            client.subscribeToMethods(
                new MethodCallback()
                {
                    @Override
                    public com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse onMethodInvoked(String methodName, DirectMethodPayload methodData, Object context)
                    {
                        return handleMethodInvocation(methodName, methodData, context);
                    }
                },
                null);
            handler.handle(Future.succeededFuture());
        }
        catch (IllegalStateException | InterruptedException | IotHubClientException e)
        {
            handler.handle(Future.failedFuture(e));
        }
    }

    @Override
    public void deviceConnectionIdRoundtripMethodCallMethodNamePut(String connectionId, String methodName, RoundtripMethodCallBody requestAndResponse, Handler<AsyncResult<Void>> handler)
    {
        System.out.printf("DeviceApiImpl.roundtripMethodCall called for %s, method %s%n", connectionId, methodName);
        DeviceClient client = getClient(connectionId);
        if (client == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid connection id")));
            return;
        }
        _methodHandler = handler;
        _methodRequestBody = (String) (((LinkedHashMap) requestAndResponse.getRequestPayload()).get("payload"));
        _methodResponseBody = Json.encode(requestAndResponse.getResponsePayload());
        _methodStatusCode = requestAndResponse.getStatusCode();
        _methodName = methodName;
    }
}
