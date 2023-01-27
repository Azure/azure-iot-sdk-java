package io.swagger.server.api.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import io.swagger.server.api.MainApiException;

@SuppressWarnings("ALL")
public class WrapperApiVerticle extends AbstractVerticle {
    final static Logger LOGGER = LoggerFactory.getLogger(WrapperApiVerticle.class); 
    
    final static String PUT_WRAPPER_CLEANUP_SERVICE_ID = "PUT_wrapper_cleanup";
    final static String PUT_WRAPPER_MESSAGE_SERVICE_ID = "PUT_wrapper_message";
    final static String GET_WRAPPER_SESSION_SERVICE_ID = "GET_wrapper_session";
    final static String PUT_WRAPPER_SESSION_SERVICE_ID = "PUT_wrapper_session";
    
    final WrapperApi service;

    public WrapperApiVerticle() {
        try {
            Class<?> serviceImplClass = getClass().getClassLoader().loadClass("io.swagger.server.api.verticle.WrapperApiImpl");
            service = (WrapperApi)serviceImplClass.newInstance();
        } catch (Exception e) {
            logUnexpectedError("WrapperApiVerticle constructor", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() throws Exception {
        
        //Consumer for PUT_wrapper_cleanup
        vertx.eventBus().<JsonObject> consumer(PUT_WRAPPER_CLEANUP_SERVICE_ID).handler(message -> {
            try {
                service.wrapperCleanupPut(result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_wrapper_cleanup");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_wrapper_cleanup", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for PUT_wrapper_message
        vertx.eventBus().<JsonObject> consumer(PUT_WRAPPER_MESSAGE_SERVICE_ID).handler(message -> {
            try {
                Object msg = message.body().getJsonObject("msg");
                service.wrapperMessagePut(msg, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_wrapper_message");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_wrapper_message", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for GET_wrapper_session
        vertx.eventBus().<JsonObject> consumer(GET_WRAPPER_SESSION_SERVICE_ID).handler(message -> {
            try {
                service.wrapperSessionGet(result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "GET_wrapper_session");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("GET_wrapper_session", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for PUT_wrapper_session
        vertx.eventBus().<JsonObject> consumer(PUT_WRAPPER_SESSION_SERVICE_ID).handler(message -> {
            try {
                service.wrapperSessionPut(result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_wrapper_session");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_wrapper_session", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
    }
    
    private void manageError(Message<JsonObject> message, Throwable cause, String serviceName) {
        int code = MainApiException.INTERNAL_SERVER_ERROR.getStatusCode();
        String statusMessage = MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage();
        if (cause instanceof MainApiException) {
            code = ((MainApiException)cause).getStatusCode();
            statusMessage = ((MainApiException)cause).getStatusMessage();
        } else {
            logUnexpectedError(serviceName, cause); 
        }
            
        message.fail(code, statusMessage);
    }
    
    private void logUnexpectedError(String serviceName, Throwable cause) {
        LOGGER.error("Unexpected error in "+ serviceName, cause);
    }
}
