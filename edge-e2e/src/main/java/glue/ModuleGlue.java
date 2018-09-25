package glue;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack;
import com.microsoft.azure.sdk.iot.device.edge.MethodRequest;
import com.microsoft.azure.sdk.iot.device.edge.MethodResult;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import io.swagger.server.api.model.Certificate;
import io.swagger.server.api.model.ConnectResponse;
import io.swagger.server.api.model.MethodRequestResponse;
import io.swagger.server.api.MainApiException;
import io.swagger.server.api.model.RoundtripMethodCallBody;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;


public class ModuleGlue
{

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

        IotHubClientProtocol protocol = this.transportFromString(transportType);
        if (protocol == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid transport")));
            return;
        }

        try
        {
            ModuleClient client = ModuleClient.createFromEnvironment(protocol);
            client.open();

            this._clientCount++;
            String connectionId = "moduleClient_" + this._clientCount;
            this._map.put(connectionId, client);

            ConnectResponse cr = new ConnectResponse();
            cr.setConnectionId(connectionId);
            handler.handle(Future.succeededFuture(cr));
        }
        catch (IOException | ModuleClientException  e)
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
            ModuleClient client = new ModuleClient(connectionString, protocol);

            String cert = caCertificate.getCert();
            if (cert != null && cert != "")
            {
                client.setOption("SetCertificateAuthority", cert);
            }
            client.open();

            this._clientCount++;
            String connectionId = "moduleClient_" + this._clientCount;
            this._map.put(connectionId, client);

            ConnectResponse cr = new ConnectResponse();
            cr.setConnectionId(connectionId);
            handler.handle(Future.succeededFuture(cr));
        }
        catch (IOException | ModuleClientException | URISyntaxException e)
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
            }
            catch (ModuleClientException e)
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
            this._deviceTwinPropertyCallback.setHandler(null);
            this._deviceTwinStatusCallback.setHandler(null);
            try
            {
                client.closeNow();
            }
            catch (IOException e)
            {
                // ignore it, but keep it as an open connection so we can close it again later.
                System.out.printf("Exception on close: %s%n", e.toString());
            }
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

    private static class ModuleTwinPropertyCallBack implements TwinPropertyCallBack
    {
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

        @Override
        public void TwinPropertyCallBack(Property property, Object context)
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
    }

    private ModuleTwinPropertyCallBack _deviceTwinPropertyCallback = new ModuleTwinPropertyCallBack();

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
                if ((status == IotHubStatusCode.OK) || (status == IotHubStatusCode.OK_EMPTY))
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

    private IotHubEventCallbackImpl _deviceTwinStatusCallback = new IotHubEventCallbackImpl();


    public void enableTwin(String connectionId, final Handler<AsyncResult<Void>> handler)
    {
        final ModuleClient client = getClient(connectionId);
        if (client == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid connection id")));
        }
        else
        {
            try
            {
                // After we start the twin, we want to subscribe to twin properties.  This lambda will do that for us.
                this._deviceTwinStatusCallback.setHandler(res -> {
                    System.out.printf("startTwin completed - failed = %s%n", (res.failed() ? "true" : "false"));

                    if (res.failed())
                    {
                        handler.handle(res);
                    }
                    else
                    {
                        try
                        {
                            client.subscribeToTwinDesiredProperties(null);
                        }
                        catch (IOException e)
                        {
                            this._deviceTwinStatusCallback.setHandler(null);
                            handler.handle(Future.failedFuture(e));
                            return;
                        }
                        handler.handle(Future.succeededFuture());
                    }
                    this._deviceTwinStatusCallback.setHandler(null);
                });
                System.out.println("calling startTwin");
                client.startTwin(this._deviceTwinStatusCallback, null, this._deviceTwinPropertyCallback, null);
            }
            catch (IOException e)
            {
                handler.handle(Future.failedFuture((e)));
            }
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
            System.out.printf("calling sendEventAsync%n");
            client.sendEventAsync(msg, callback, null);
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
            }
            catch (Exception e)
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

    private static class DeviceMethodCallbackImpl implements DeviceMethodCallback
    {
        public Handler<AsyncResult<Void>> _handler;
        public String _requestBody;
        public String _responseBody;
        public String _methodName;
        public int _statusCode;
        public ModuleClient _client;

        public void reset()
        {
            this._methodName = null;
            this._handler = null;
        }

        @Override
        public DeviceMethodData call(String methodName, Object methodData, Object context)
        {
            System.out.printf("method %s called%n", methodName);
            System.out.printf("methodData: %s%n", methodData.toString());
            if (methodName.equals(this._methodName))
            {
                String methodDataString;
                try
                {
                    methodDataString = Json.mapper.readValue(new String((byte[]) methodData), String.class);
                }
                catch (IOException e)
                {
                    this._handler.handle(Future.failedFuture(e));
                    this.reset();
                    return new DeviceMethodData(500, "exception parsing methodData");
                }

                if (methodDataString.equals(this._requestBody))
                {
                    System.out.printf("Method data looks corrent.  Returning result: %s%n", _responseBody);
                    this._handler.handle(Future.succeededFuture());
                    this.reset();
                    return new DeviceMethodData(this._statusCode, this._responseBody);
                }
                else
                {
                    System.out.printf("method data does not match.  Expected %s%n", this._requestBody);
                    this._handler.handle(Future.failedFuture("methodData does not match"));
                    this.reset();
                    return new DeviceMethodData(500, "methodData not received as expected");
                }
            }
            else
            {
                this._handler.handle(Future.failedFuture("unexpected call: " + methodName));
                this.reset();
                return new DeviceMethodData(404, "method " + methodName + " not handled");
            }
        }
    }
    DeviceMethodCallbackImpl _methodCallback = new DeviceMethodCallbackImpl();

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
                client.subscribeToMethod(this._methodCallback, null, callback, null);
            }
            catch (IOException e)
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
            _methodCallback._handler = handler;
            _methodCallback._requestBody = (String)(((LinkedHashMap)requestAndResponse.getRequestPayload()).get("payload"));
            _methodCallback._responseBody = Json.encode(requestAndResponse.getResponsePayload());
            _methodCallback._statusCode = requestAndResponse.getStatusCode();
            _methodCallback._client = client;
            _methodCallback._methodName = methodName;
        }
    }

    private JsonObject makeMethodResultThatEncodesCorrectly(MethodResult result)
    {
        // Our JSON encoder doesn't like the way the MethodClass implements getPayload and getPayloadObject.  It
        // produces JSON that had both fields and the we want to return payloadObject, but we want to return it
        // in the field called "payload".  The easiest workaroudn is to make an empty JsonObject and copy the
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
            JsonObject params = (JsonObject)methodInvokeParameters;
            String methodName = params.getString("methodName");
            String payload = params.getString("payload");
            int responseTimeout = params.getInteger("responseTimeoutInSeconds", 0);
            int connectionTimeout = params.getInteger("connectTimeoutInSeconds", 0);
            MethodRequest request = new MethodRequest(methodName, payload, responseTimeout, connectionTimeout);
            try
            {
                MethodResult result = client.invokeMethod(deviceId, moduleId, request);
                handler.handle(Future.succeededFuture(makeMethodResultThatEncodesCorrectly(result)));
            }
            catch (ModuleClientException e)
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
            this._deviceTwinPropertyCallback.setHandler(res -> {
                if (res.succeeded())
                {
                    JsonObject obj = (JsonObject)res.result();
                    Object desiredProps = obj.getJsonObject("properties").getJsonObject("desired");
                    handler.handle(Future.succeededFuture(desiredProps));
                }
                else
                {
                    handler.handle(res);
                }
            });

        }
    }

    public void getTwin(String connectionId, Handler<AsyncResult<Object>> handler)
    {
        System.out.printf("getTwin with %s%n", connectionId);

        ModuleClient client = getClient(connectionId);
        if (client == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid connection id")));
        }
        else
        {
            this._deviceTwinPropertyCallback.setHandler(handler);
            try
            {
                client.getTwin();
            }
            catch(IOException e)
            {
                this._deviceTwinPropertyCallback.setHandler(null);
                handler.handle(Future.failedFuture(e));
            }
        }
    }

    private Set<Property> objectToPropSet(JsonObject props)
    {
        Set<Property> propSet = new HashSet<Property>();
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
            Set<Property> propSet = objectToPropSet((JsonObject)props);
            this._deviceTwinStatusCallback.setHandler(handler);
            try
            {
                client.sendReportedProperties(propSet);
            }
            catch(IOException e)
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
