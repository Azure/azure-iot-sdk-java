package io.swagger.server.api.verticle;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

public interface WrapperApi  {
    //PUT_wrapper_cleanup
    void wrapperCleanupPut(Handler<AsyncResult<Void>> handler);
    
    //PUT_wrapper_message
    void wrapperMessagePut(Object msg, Handler<AsyncResult<Void>> handler);
    
    //GET_wrapper_session
    void wrapperSessionGet(Handler<AsyncResult<Void>> handler);
    
    //PUT_wrapper_session
    void wrapperSessionPut(Handler<AsyncResult<Void>> handler);
    
}
