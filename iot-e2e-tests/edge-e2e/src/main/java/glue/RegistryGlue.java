package glue;

import com.microsoft.azure.sdk.iot.service.twin.TwinClient;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import io.swagger.server.api.MainApiException;
import io.swagger.server.api.model.ConnectResponse;
import io.swagger.server.api.verticle.WrappedTwin;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("ALL")
public class RegistryGlue
{
    HashMap<String, TwinClient> _map = new HashMap<>();
    int _clientCount = 0;

    public void connect(String connectionString, Handler<AsyncResult<ConnectResponse>> handler)
    {
        System.out.printf("Connect called%n");
        TwinClient client = new TwinClient(connectionString);

        this._clientCount++;
        String connectionId = "registryClient_" + this._clientCount;
        this._map.put(connectionId, client);

        ConnectResponse cr = new ConnectResponse();
        cr.setConnectionId(connectionId);
        handler.handle(Future.succeededFuture(cr));
    }

    private TwinClient getClient(String connectionId)
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
        TwinClient client = getClient(connectionId);
        if (client != null)
        {
            this._map.remove(connectionId);
        }
    }

    public void disconnect(String connectionId, Handler<AsyncResult<Void>> handler)
    {
        this._closeConnection(connectionId);
        handler.handle(Future.succeededFuture());
    }

    public void getModuleTwin(String connectionId, String deviceId, String moduleId, Handler<AsyncResult<Object>> handler)
    {
        System.out.printf("getModuleTwin called for %s with deviceId = %s and moduleId = %s%n", connectionId, deviceId, moduleId);

        TwinClient client = getClient(connectionId);
        if (client == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid connection id")));
        }
        else
        {
            Twin twin = null;
            try
            {
                twin = client.get(deviceId, moduleId);
            } catch (IOException | IotHubException e)
            {
                handler.handle(Future.failedFuture(e));
            }

            WrappedTwin wrappedTwin = new WrappedTwin(twin);

            handler.handle(Future.succeededFuture(wrappedTwin.toJsonObject()));
        }
    }

    public void sendModuleTwinPatch(String connectionId, String deviceId, String moduleId, Object props, Handler<AsyncResult<Void>> handler)
    {
        System.out.printf("sendModuleTwinPatch called for %s with deviceId = %s and moduleId = %s%n", connectionId, deviceId, moduleId);
        System.out.println(props.toString());

        TwinClient client = getClient(connectionId);
        if (client == null)
        {
            handler.handle(Future.failedFuture(new MainApiException(500, "invalid connection id")));
        }
        else
        {
            Twin twin = new Twin(deviceId, moduleId);
            Map<String, Object> desiredProps = ((JsonObject) props).getJsonObject("properties").getJsonObject("desired").getMap();
            twin.getDesiredProperties().putAll(desiredProps);

            try
            {
                client.patch(twin);
            }
            catch (IotHubException | IOException e)
            {
                handler.handle(Future.failedFuture(e));
            }

            handler.handle(Future.succeededFuture());
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
