package io.swagger.server.api.verticle;

import glue.WrapperGlue;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public class WrapperApiImpl implements WrapperApi  {
    private WrapperGlue _wrapperGlue= new WrapperGlue();

    @Override
    public void wrapperCleanupPut(Handler<AsyncResult<Void>> handler)
    {
        this._wrapperGlue.Cleanup(handler);
    }

    @Override
    public void wrapperMessagePut(Object message, Handler<AsyncResult<Void>> handler)
    {
        this._wrapperGlue.outputMessage(message, handler);
    }

    @Override
    public void wrapperSessionGet(Handler<AsyncResult<Void>> handler)
    {
        handler.handle(Future.succeededFuture());
    }

    @Override
    public void wrapperSessionPut(Handler<AsyncResult<Void>> handler)
    {
        handler.handle(Future.succeededFuture());
    }
}
