package glue;

import com.microsoft.azure.sdk.iot.device.ClientOptions;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.ModuleClient;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.edge.MethodRequest;
import com.microsoft.azure.sdk.iot.device.edge.MethodResult;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.hsm.UnixDomainSocketChannel;
import com.microsoft.azure.sdk.iot.device.twin.DesiredPropertiesUpdateCallback;
import com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse;
import com.microsoft.azure.sdk.iot.device.twin.GetTwinCallback;
import com.microsoft.azure.sdk.iot.device.twin.MethodCallback;
import com.microsoft.azure.sdk.iot.device.twin.Property;
import com.microsoft.azure.sdk.iot.device.twin.SubscribeToDesiredPropertiesCallback;
import com.microsoft.azure.sdk.iot.device.twin.Twin;
import com.microsoft.azure.sdk.iot.device.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.device.twin.UpdateReportedPropertiesCallback;
import io.swagger.server.api.MainApiException;
import io.swagger.server.api.model.Certificate;
import io.swagger.server.api.model.ConnectResponse;
import io.swagger.server.api.model.RoundtripMethodCallBody;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import samples.com.microsoft.azure.sdk.iot.UnixDomainSocketSample;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLStreamHandlerFactory;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;


@SuppressWarnings("ALL")
public class ModuleGlue
{
    private static final long OPEN_RETRY_TIMEOUT = 3 * 60 * 1000;
    private static final String UNIX_SCHEME = "unix";

    private IotHubClientProtocol transportFromString(String protocolStr)
    {
        IotHubClientProtocol protocol = null;

        if (protocolStr.equals("amqp"))
        {
            protocol = IotHubClientProtocol.AMQPS;
        }
        else if (protocolStr.equals("mqtt"))
        {
            protocol = IotHubClientProtocol.MQTT;
        }
        else if (protocolStr.equals("amqpws"))
        {
            protocol = IotHubClientProtocol.AMQPS_WS;
        }
        else if (protocolStr.equals("mqttws"))
        {
            protocol = IotHubClientProtocol.MQTT_WS;
        }
        else if (protocolStr.equals("http"))
        {
            protocol = IotHubClientProtocol.HTTPS;
        }
        return protocol;
    }

    HashMap<String, ModuleClient> _map = new HashMap<>();
    int _clientCount = 0;

    public void connectFromEnvironment(String transportType, Handler<AsyncResult<ConnectResponse>> handler)
    {
        System.out.printf("ConnectFromEnvironment called with transport %s%n", transportType);

        //This is the default URL stream handler factory
        URLStreamHandlerFactory fac = protocol -> {
            if (protocol.equals("http"))
            {
                return new sun.net.www.protocol.http.Handler();
            }
            else if(protocol.equals("https"))
            {
                return new sun.net.www.protocol.https.Handler();
            }

            return null;
        };

        IotHubClientProtocol protocol = this.transportFromString(transportType);
        if (protocol == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid transport")));
            return;
        }

        try
        {
            UnixDomainSocketChannel unixDomainSocketChannel = new UnixDomainSocketSample.UnixDomainSocketChannelImpl();
            ModuleClient client = ModuleClient.createFromEnvironment(unixDomainSocketChannel, protocol);
            client.open(false);

            this._clientCount++;
            String connectionId = "moduleClient_" + this._clientCount;
            this._map.put(connectionId, client);

            ConnectResponse cr = new ConnectResponse();
            cr.setConnectionId(connectionId);
            handler.handle(Future.succeededFuture(cr));
        } catch (ModuleClientException | IOException e)
        {
            handler.handle(Future.failedFuture(e));
        }
    }

    private ModuleClient getClient(String connectionId)
    {
        if (this._map.containsKey(connectionId))
        {
            return this._map.get(connectionId);
        }
        else
        {
            return null;
        }
    }

    public void connect(String transportType, String connectionString, Certificate caCertificate, Handler<AsyncResult<ConnectResponse>> handler)
    {
        System.out.printf("Connect called with transport %s%n", transportType);

        IotHubClientProtocol protocol = this.transportFromString(transportType);
        if (protocol == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid transport")));
            return;
        }

        try
        {
            String cert = caCertificate.getCert();
            ClientOptions.ClientOptionsBuilder clientOptionsBuilder = ClientOptions.builder();
            if (cert != null && !cert.isEmpty())
            {
                clientOptionsBuilder.sslContext(IotHubSSLContext.getSSLContextFromFile(cert));
            }

            ModuleClient client = new ModuleClient(connectionString, protocol, clientOptionsBuilder.build());

            client.open(false);

            this._clientCount++;
            String connectionId = "moduleClient_" + this._clientCount;
            this._map.put(connectionId, client);

            ConnectResponse cr = new ConnectResponse();
            cr.setConnectionId(connectionId);
            handler.handle(Future.succeededFuture(cr));
        }
        catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e)
        {
            handler.handle(Future.failedFuture(e));
        }
    }


    public void invokeDeviceMethod(String connectionId, String deviceId, Object methodInvokeParameters, Handler<AsyncResult<Object>> handler)
    {
        ModuleClient client = getClient(connectionId);
        if (client == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid connection id")));
        }
        else
        {
            JsonObject params = (JsonObject) methodInvokeParameters;
            String methodName = params.getString("methodName");
            String payload = params.getString("payload");
            int responseTimeout = params.getInteger("responseTimeoutInSeconds", 0);
            int connectionTimeout = params.getInteger("connectTimeoutInSeconds", 0);
            MethodRequest request = new MethodRequest(methodName, payload, responseTimeout, connectionTimeout);
            try
            {
                MethodResult result = client.invokeMethod(deviceId, request);
                handler.handle(Future.succeededFuture(makeMethodResultThatEncodesCorrectly(result)));
            } catch (ModuleClientException e)
            {
                handler.handle(Future.failedFuture(e));
            }
        }
    }

    private void _closeConnection(String connectionId)
    {
        System.out.printf("Disconnect for %s%n", connectionId);
        ModuleClient client = getClient(connectionId);
        if (client != null)
        {
            client.close();
            this._map.remove(connectionId);
        }
    }

    public void disconnect(String connectionId, Handler<AsyncResult<Void>> handler)
    {
        this._closeConnection(connectionId);
        handler.handle(Future.succeededFuture());
    }

    public void enableInputMessages(String connectionId, Handler<AsyncResult<Void>> handler)
    {
        ModuleClient client = getClient(connectionId);
        if (client == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid connection id")));
        }
        else
        {
            handler.handle(Future.succeededFuture());
        }
    }

    private JsonObject _props = null;
    private Handler<AsyncResult<Object>> _handler;
    private Timer _timer = null;

    public void setHandler(Handler<AsyncResult<Object>> handler)
    {
        if (handler == null)
        {
            this._props = null;
        }
        else
        {
            this._props = new JsonObject()
            {
                {
                    put("desired", new JsonObject());
                    put("reported", new JsonObject());
                }
            };
        }
        this._handler = handler;
    }

    public void onPropertyChanged(Property property, Object context)
    {
        System.out.println(
            "onProperty callback for " + (property.getIsReported() ? "reported" : "desired") +
                " property " + property.getKey() +
                " to " + property.getValue() +
                ", Properties version:" + property.getVersion());
        if (this._props == null)
        {
            System.out.println("nobody is listening for desired properties.  ignoring.");
        }
        else
        {
            if (property.getIsReported())
            {
                this._props.getJsonObject("reported").getMap().put(property.getKey(), property.getValue());
            }
            else
            {
                this._props.getJsonObject("desired").getMap().put(property.getKey(), property.getValue());
            }
            System.out.println(this._props.toString());
            System.out.println("scheduling timer");
            this.rescheduleHandler();
        }
    }

    private void rescheduleHandler()
    {
        if (_handler == null)
        {
            return;
        }
        // call _handler 2 seconds after the last designed property change
        if (this._timer != null)
        {
            this._timer.cancel();
            this._timer = null;
        }
        this._timer = new Timer();
        this._timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                _timer = null;
                if (_handler != null && _props != null)
                {
                    System.out.println("It's been 2 seconds since last desired property arrived.  Calling handler");
                    JsonObject twin = new JsonObject()
                    {
                        {
                            put("properties", _props);
                        }
                    };
                    System.out.println(twin.toString());
                    _handler.handle(Future.succeededFuture(twin));
                    _handler = null;
                }
            }
        }, 2000);
    }

    private static class IotHubEventCallbackImpl implements IotHubEventCallback
    {
        Handler<AsyncResult<Void>> _handler = null;

        public void setHandler(Handler<AsyncResult<Void>> handler)
        {
            this._handler = handler;
        }

        @Override
        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("IoT Hub responded to operation with status " + status.name());
            Handler<AsyncResult<Void>> handler = this._handler;
            this._handler = null;
            if (handler != null)
            {
                if (status == IotHubStatusCode.OK)
                {
                    handler.handle(Future.succeededFuture());
                }
                else
                {
                    handler.handle(Future.failedFuture(new MainApiException(500, "operation failed")));
                }
            }
        }
    }

    private final IotHubEventCallbackImpl _deviceTwinStatusCallback = new IotHubEventCallbackImpl();

    public void enableTwin(String connectionId, final Handler<AsyncResult<Void>> handler)
    {
        final ModuleClient client = getClient(connectionId);
        if (client == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid connection id")));
        }
        else
        {
            // After we start the twin, we want to subscribe to twin properties.  This lambda will do that for us.
            this._deviceTwinStatusCallback.setHandler(res -> {
                System.out.printf("startTwinAsync completed - failed = %s%n", (res.failed() ? "true" : "false"));

                if (res.failed())
                {
                    handler.handle(res);
                }
                else
                {
                }
                this._deviceTwinStatusCallback.setHandler(null);
            });
            System.out.println("calling subscribeToDesiredPropertiesAsync");
            client.subscribeToDesiredPropertiesAsync(
                new SubscribeToDesiredPropertiesCallback()
                {
                    @Override
                    public void onSubscriptionAcknowledged(IotHubStatusCode statusCode, Object context)
                    {
                        handler.handle(Future.succeededFuture());
                    }
                },
                null,
                new DesiredPropertiesUpdateCallback()
                {
                    @Override
                    public void onDesiredPropertiesUpdate(Twin twin, Object context)
                    {
                        TwinCollection desiredProperties = twin.getDesiredProperties();
                        for (String key : desiredProperties.keySet())
                        {
                            onPropertyChanged(new Property(key, desiredProperties.get(key)), null);
                        }
                    }
                },
                null);
        }
    }

    private static class EventCallback implements IotHubEventCallback
    {
        Handler<AsyncResult<Void>> _handler;

        public EventCallback(Handler<AsyncResult<Void>> handler)
        {
            this._handler = handler;
        }

        public synchronized void execute(IotHubStatusCode status, Object context)
        {
            System.out.printf("EventCallback called with status %s%n", status.toString());
            this._handler.handle(Future.succeededFuture());
        }
    }

    private void sendEventHelper(String connectionId, Message msg, Handler<AsyncResult<Void>> handler)
    {
        System.out.println("inside sendEventHelper");

        ModuleClient client = getClient(connectionId);
        if (client == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid connection id")));
        }
        else
        {
            EventCallback callback = new EventCallback(handler);
            System.out.printf("calling sendTelemetryAsync%n");
            client.sendTelemetryAsync(msg, callback, null);
        }
    }

    public void sendEvent(String connectionId, String eventBody, Handler<AsyncResult<Void>> handler)
    {
        System.out.printf("moduleConnectionIdEventPut called for %s%n", connectionId);
        System.out.println(eventBody);
        this.sendEventHelper(connectionId, new Message(eventBody), handler);
    }

    protected static class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        ModuleClient _client;
        Handler<AsyncResult<String>> _handler;
        String _inputName;

        public MessageCallback(ModuleClient client, String inputName, Handler<AsyncResult<String>> handler)
        {
            this._client = client;
            this._inputName = inputName;
            this._handler = handler;
        }

        public synchronized IotHubMessageResult execute(Message msg, Object context)
        {
            System.out.println("MessageCallback called");
            this._client.setMessageCallback(this._inputName, null, null);
            String result = new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET);
            System.out.printf("result = %s%n", result);
            try
            {
                if (this._handler != null)
                {
                    this._handler.handle(Future.succeededFuture(result));
                }
            } catch (Exception e)
            {
                System.out.printf("Ignoring exception %s%n", e.toString());

            }
            return IotHubMessageResult.COMPLETE;
        }
    }

    public void waitForInputMessage(String connectionId, String inputName, Handler<AsyncResult<String>> handler)
    {
        System.out.printf("waitForInputMessage with %s, %s%n", connectionId, inputName);

        ModuleClient client = getClient(connectionId);
        if (client == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid connection id")));
        }
        else
        {
            MessageCallback callback = new MessageCallback(client, inputName, handler);
            System.out.printf("calling setMessageCallback%n");
            client.setMessageCallback(inputName, callback, null);
        }
    }

    public Handler<AsyncResult<Void>> _methodHandler;
    public String _methodRequestBody;
    public String _methodResponseBody;
    public String _methodName;
    public int _methodStatusCode;
    public ModuleClient _client;

    public void reset()
    {
        this._methodName = null;
        this._handler = null;
    }

    public DirectMethodResponse handleMethodInvocation(String methodName, Object methodData, Object context)
    {
        System.out.printf("method %s called%n", methodName);
        if (methodName.equals(this._methodName))
        {
            String methodDataString;
            try
            {
                methodDataString = Json.mapper.readValue(new String((byte[]) methodData, StandardCharsets.UTF_8), String.class);
            } catch (IOException e)
            {
                this._handler.handle(Future.failedFuture(e));
                this.reset();
                return new DirectMethodResponse(500, "exception parsing methodData");
            }
            System.out.printf("methodData: %s%n", methodDataString);

            if (methodDataString.equals(this._methodRequestBody) ||
                Json.encode(methodDataString).equals(this._methodRequestBody))
            {
                System.out.printf("Method data looks correct.  Returning result: %s%n", _methodResponseBody);
                this._handler.handle(Future.succeededFuture());
                this.reset();
                return new DirectMethodResponse(this._methodStatusCode, this._methodResponseBody);
            }
            else
            {
                System.out.printf("method data does not match.  Expected %s%n", this._methodRequestBody);
                this._handler.handle(Future.failedFuture("methodData does not match"));
                this.reset();
                return new DirectMethodResponse(500, "methodData not received as expected");
            }
        }
        else
        {
            this._handler.handle(Future.failedFuture("unexpected call: " + methodName));
            this.reset();
            return new DirectMethodResponse(404, "method " + methodName + " not handled");
        }
    }

    public void enableMethods(String connectionId, Handler<AsyncResult<Void>> handler)
    {
        ModuleClient client = getClient(connectionId);
        if (client == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid connection id")));
        }
        else
        {
            IotHubEventCallbackImpl callback = new IotHubEventCallbackImpl();
            callback.setHandler(handler);
            try
            {
                client.subscribeToMethodsAsync(new MethodCallback()
                {
                    @Override
                    public DirectMethodResponse call(String methodName, Object methodData, Object context)
                    {
                        return handleMethodInvocation(methodName, methodData, context);
                    }
                }, null, callback, null);
            }
            catch (IllegalStateException e)
            {
                handler.handle(Future.failedFuture(e));
            }

        }
    }

    public void roundtripMethodCall(String connectionId, String methodName, RoundtripMethodCallBody requestAndResponse, Handler<AsyncResult<Void>> handler)
    {
        ModuleClient client = getClient(connectionId);
        if (client == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid connection id")));
        }
        else
        {
            _methodHandler = handler;
            _methodRequestBody = (String) (((LinkedHashMap) requestAndResponse.getRequestPayload()).get("payload"));
            _methodResponseBody = Json.encode(requestAndResponse.getResponsePayload());
            _methodStatusCode = requestAndResponse.getStatusCode();
            _client = client;
            _methodName = methodName;
        }
    }

    private JsonObject makeMethodResultThatEncodesCorrectly(MethodResult result)
    {
        // Our JSON encoder doesn't like the way the MethodClass implements getPayload and getPayloadObject.  It
        // produces JSON that had both fields and the we want to return payloadObject, but we want to return it
        // in the field called "payload".  The easiest workaround is to make an empty JsonObject and copy the
        // values over manually.  I'm sure there's a better way, but this is test code.
        JsonObject fixedObject = new JsonObject();
        fixedObject.put("status", result.getStatus());
        fixedObject.put("payload", result.getPayloadObject());
        return fixedObject;
    }


    public void invokeModuleMethod(String connectionId, String deviceId, String moduleId, Object methodInvokeParameters, Handler<AsyncResult<Object>> handler)
    {
        ModuleClient client = getClient(connectionId);
        if (client == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid connection id")));
        }
        else
        {
            JsonObject params = (JsonObject) methodInvokeParameters;
            String methodName = params.getString("methodName");
            String payload = params.getString("payload");
            int responseTimeout = params.getInteger("responseTimeoutInSeconds", 0);
            int connectionTimeout = params.getInteger("connectTimeoutInSeconds", 0);
            MethodRequest request = new MethodRequest(methodName, payload, responseTimeout, connectionTimeout);
            try
            {
                MethodResult result = client.invokeMethod(deviceId, moduleId, request);
                handler.handle(Future.succeededFuture(makeMethodResultThatEncodesCorrectly(result)));
            } catch (ModuleClientException e)
            {
                handler.handle(Future.failedFuture(e));
            }
        }
    }

    public void sendOutputEvent(String connectionId, String outputName, String eventBody, Handler<AsyncResult<Void>> handler)
    {
        System.out.printf("sendOutputEvent called for %s, %s%n", connectionId, outputName);
        System.out.println(eventBody);
        Message msg = new Message(eventBody);
        msg.setOutputName(outputName);
        this.sendEventHelper(connectionId, msg, handler);
    }

    public void waitForDesiredPropertyPatch(String connectionId, Handler<AsyncResult<Object>> handler)
    {
        System.out.printf("waitForDesiredPropertyPatch with %s%n", connectionId);

        ModuleClient client = getClient(connectionId);
        if (client == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid connection id")));
        }
        else
        {
            this._handler = res -> {
                if (res.succeeded())
                {
                    JsonObject obj = (JsonObject) res.result();
                    Object desiredProps = obj.getJsonObject("properties").getJsonObject("desired");
                    handler.handle(Future.succeededFuture(desiredProps));
                }
                else
                {
                    handler.handle(res);
                }
            };
        }
    }

    public void getTwin(String connectionId, Handler<AsyncResult<Object>> handler)
    {
        System.out.printf("getTwinAsync with %s%n", connectionId);

        ModuleClient client = getClient(connectionId);
        if (client == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid connection id")));
        }
        else
        {
            this._handler = handler;
            try
            {
                client.getTwinAsync(
                    new GetTwinCallback()
                    {
                        @Override
                        public void onTwinReceived(Twin twin, Object callbackContext)
                        {
                            TwinCollection desiredProperties = twin.getDesiredProperties();
                            for (String key : desiredProperties.keySet())
                            {
                                onPropertyChanged(new Property(key, desiredProperties.get(key)), null);
                            }
                        }
                    },
                    null);
            }
            catch (IllegalStateException e)
            {
                handler.handle(Future.failedFuture(e));
            }
        }
    }

    private Set<Property> objectToPropSet(JsonObject props)
    {
        Set<Property> propSet = new HashSet<>();
        for (String key : props.fieldNames())
        {
            // TODO: we may need to make this function recursive.
            propSet.add(new Property(key, props.getMap().get(key)));
        }
        return propSet;
    }

    public void sendTwinPatch(String connectionId, Object props, Handler<AsyncResult<Void>> handler)
    {
        System.out.printf("sendTwinPatch called for %s%n", connectionId);
        System.out.println(props.toString());

        ModuleClient client = getClient(connectionId);
        if (client == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid connection id")));
        }
        else
        {
            TwinCollection reportedProperties = new TwinCollection();
            Set<Property> propSet = objectToPropSet((JsonObject) props);
            for (Property property : propSet)
            {
                reportedProperties.put(property.getKey(), property.getValue());
            }
            this._deviceTwinStatusCallback.setHandler(handler);
            try
            {
                final CountDownLatch sendReportedPropertiesLatch = new CountDownLatch(1);
                UpdateReportedPropertiesCallback sendReportedPropertiesResponseCallback = new UpdateReportedPropertiesCallback()
                {
                    @Override
                    public void onPropertiesUpdated(IotHubStatusCode statusCode, TransportException e, Object callbackContext)
                    {
                        sendReportedPropertiesLatch.countDown();
                    }
                };

                client.updateReportedPropertiesAsync(reportedProperties, sendReportedPropertiesResponseCallback, null);
                sendReportedPropertiesLatch.await();
            }
            catch (IllegalStateException | InterruptedException e)
            {
                this._deviceTwinStatusCallback.setHandler(null);
                handler.handle(Future.failedFuture(e));
            }
        }
    }

    public void Cleanup()
    {
        Set<String> keys = this._map.keySet();
        if (!keys.isEmpty())
        {
            for (String key : keys)
            {
                this._closeConnection(key);
            }
        }
    }
}
