package io.swagger.server.api.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import io.swagger.server.api.model.Certificate;
import io.swagger.server.api.MainApiException;
import io.swagger.server.api.model.RoundtripMethodCallBody;

public class DeviceApiVerticle extends AbstractVerticle {
    final static Logger LOGGER = LoggerFactory.getLogger(DeviceApiVerticle.class); 
    
    final static String PUT_DEVICE_CONNECT_TRANSPORTTYPE_SERVICE_ID = "PUT_device_connect_transportType";
    final static String PUT_DEVICE_CONNECTIONID_DISCONNECT_SERVICE_ID = "PUT_device_connectionId_disconnect";
    final static String PUT_DEVICE_CONNECTIONID_ENABLEMETHODS_SERVICE_ID = "PUT_device_connectionId_enableMethods";
    final static String PUT_DEVICE_CONNECTIONID_ROUNDTRIPMETHODCALL_METHODNAME_SERVICE_ID = "PUT_device_connectionId_roundtripMethodCall_methodName";
    
    final DeviceApi service;

    public DeviceApiVerticle() {
        try {
            Class serviceImplClass = getClass().getClassLoader().loadClass("io.swagger.server.api.verticle.DeviceApiImpl");
            service = (DeviceApi)serviceImplClass.newInstance();
        } catch (Exception e) {
            logUnexpectedError("DeviceApiVerticle constructor", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() throws Exception {
        
        //Consumer for PUT_device_connect_transportType
        vertx.eventBus().<JsonObject> consumer(PUT_DEVICE_CONNECT_TRANSPORTTYPE_SERVICE_ID).handler(message -> {
            try {
                String transportType = message.body().getString("transportType");
                String connectionString = message.body().getString("connectionString");
                Certificate caCertificate = Json.mapper.readValue(message.body().getJsonObject("caCertificate").encode(), Certificate.class);
                service.deviceConnectTransportTypePut(transportType, connectionString, caCertificate, result -> {
                    if (result.succeeded())
                        message.reply(new JsonObject(Json.encode(result.result())).encodePrettily());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_device_connect_transportType");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_device_connect_transportType", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for PUT_device_connectionId_disconnect
        vertx.eventBus().<JsonObject> consumer(PUT_DEVICE_CONNECTIONID_DISCONNECT_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                service.deviceConnectionIdDisconnectPut(connectionId, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_device_connectionId_disconnect");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_device_connectionId_disconnect", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for PUT_device_connectionId_enableMethods
        vertx.eventBus().<JsonObject> consumer(PUT_DEVICE_CONNECTIONID_ENABLEMETHODS_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                service.deviceConnectionIdEnableMethodsPut(connectionId, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_device_connectionId_enableMethods");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_device_connectionId_enableMethods", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for PUT_device_connectionId_roundtripMethodCall_methodName
        vertx.eventBus().<JsonObject> consumer(PUT_DEVICE_CONNECTIONID_ROUNDTRIPMETHODCALL_METHODNAME_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                String methodName = message.body().getString("methodName");
                RoundtripMethodCallBody requestAndResponse = Json.mapper.readValue(message.body().getJsonObject("requestAndResponse").encode(), RoundtripMethodCallBody.class);
                service.deviceConnectionIdRoundtripMethodCallMethodNamePut(connectionId, methodName, requestAndResponse, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_device_connectionId_roundtripMethodCall_methodName");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_device_connectionId_roundtripMethodCall_methodName", e);
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
