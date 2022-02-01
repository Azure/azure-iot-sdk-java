package glue;

import com.microsoft.azure.sdk.iot.service.twin.DirectMethodRequestOptions;
import com.microsoft.azure.sdk.iot.service.twin.DirectMethodsClient;
import com.microsoft.azure.sdk.iot.service.twin.MethodResult;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import io.swagger.server.api.MainApiException;
import io.swagger.server.api.model.ConnectResponse;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

@SuppressWarnings("ALL")
public class ServiceGlue
{
    HashMap<String, DirectMethodsClient> _map = new HashMap<>();
    int _clientCount = 0;

    public void connect(String connectionString, Handler<AsyncResult<ConnectResponse>> handler)
    {
        System.out.printf("connect called%n");
        DirectMethodsClient client = new DirectMethodsClient(connectionString);

        this._clientCount++;
        String connectionId = "serviceClient_" + this._clientCount;
        this._map.put(connectionId, client);

        ConnectResponse cr = new ConnectResponse();
        cr.setConnectionId(connectionId);
        handler.handle(Future.succeededFuture(cr));
    }

    private DirectMethodsClient getClient(String connectionId)
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


    private void _closeConnection(String connectionId)
    {
        System.out.printf("Disconnect for %s%n", connectionId);
        DirectMethodsClient client = getClient(connectionId);
        if (client != null)
        {
            this._map.remove(connectionId);
        }
    }

    public void disconnect(String connectionId, Handler<AsyncResult<Void>> handler)
    {
        _closeConnection(connectionId);
        handler.handle(Future.succeededFuture());
    }

    private void invokeMethodCommon(String connectionId, String deviceId, String moduleId, Object methodInvokeParameters, Handler<AsyncResult<Object>> handler)
    {
        System.out.printf("invoking method on %s with deviceId = %s moduleId = %s%n", connectionId, deviceId, moduleId);
        System.out.println(methodInvokeParameters);

        DirectMethodsClient client = getClient(connectionId);
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
            MethodResult result = null;
            System.out.printf("invoking%n");
            try
            {
                DirectMethodRequestOptions options =
                    DirectMethodRequestOptions.builder()
                        .payload(payload)
                        .methodConnectTimeout(connectionTimeout)
                        .methodResponseTimeout(responseTimeout)
                        .build();

                if (moduleId == null)
                {
                    result = client.invoke(deviceId, methodName, options);
                }
                else
                {
                    result = client.invoke(deviceId, moduleId, methodName, options);
                }
            }
            catch (IotHubException e)
            {
                handler.handle(Future.failedFuture(e));
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            System.out.printf("invoke returned%n");
            System.out.println(result);
            handler.handle(Future.succeededFuture(result));
        }
    }

    public void invokeDeviceMethod(String connectionId, String deviceId, Object methodInvokeParameters, Handler<AsyncResult<Object>> handler)
    {
        invokeMethodCommon(connectionId, deviceId, null, methodInvokeParameters, handler);

    }

    public void invokeModuleMethod(String connectionId, String deviceId, String moduleId, Object methodInvokeParameters, Handler<AsyncResult<Object>> handler)
    {
        invokeMethodCommon(connectionId, deviceId, moduleId, methodInvokeParameters, handler);
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
