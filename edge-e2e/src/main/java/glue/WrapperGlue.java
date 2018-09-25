package glue;

import io.swagger.server.api.verticle.ModuleApiImpl;
import io.swagger.server.api.verticle.RegistryApiImpl;
import io.swagger.server.api.verticle.ServiceApiImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public class WrapperGlue
{
    public void Cleanup(Handler<AsyncResult<Void>> handler)
    {
        ModuleApiImpl._moduleGlue.Cleanup();
        RegistryApiImpl._registryGlue.Cleanup();
        ServiceApiImpl._serviceGlue.Cleanup();
        handler.handle(Future.succeededFuture());
    }

    public void outputMessage(Object message, Handler<AsyncResult<Void>> handler)
    {
        System.out.println("testscript:" + ((JsonObject)message).getString("message"));
        handler.handle(Future.succeededFuture());
    }
}
