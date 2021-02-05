package io.swagger.server.api.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import io.swagger.server.api.MainApiException;

@SuppressWarnings("ALL")
public class EventhubApiVerticle extends AbstractVerticle {
    final static Logger LOGGER = LoggerFactory.getLogger(EventhubApiVerticle.class); 
    
    final static String PUT_EVENTHUB_CONNECT_SERVICE_ID = "PUT_eventhub_connect";
    final static String GET_EVENTHUB_CONNECTIONID_DEVICETELEMETRY_DEVICEID_SERVICE_ID = "GET_eventhub_connectionId_deviceTelemetry_deviceId";
    final static String PUT_EVENTHUB_CONNECTIONID_DISCONNECT__SERVICE_ID = "PUT_eventhub_connectionId_disconnect_";
    final static String PUT_EVENTHUB_CONNECTIONID_ENABLETELEMETRY_SERVICE_ID = "PUT_eventhub_connectionId_enableTelemetry";
    
    final EventhubApi service;

    public EventhubApiVerticle() {
        try {
            Class serviceImplClass = getClass().getClassLoader().loadClass("io.swagger.server.api.verticle.EventhubApiImpl");
            service = (EventhubApi)serviceImplClass.newInstance();
        } catch (Exception e) {
            logUnexpectedError("EventhubApiVerticle constructor", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() throws Exception {
        
        //Consumer for PUT_eventhub_connect
        vertx.eventBus().<JsonObject> consumer(PUT_EVENTHUB_CONNECT_SERVICE_ID).handler(message -> {
            try {
                String connectionString = message.body().getString("connectionString");
                service.eventhubConnectPut(connectionString, result -> {
                    if (result.succeeded())
                        message.reply(new JsonObject(Json.encode(result.result())).encodePrettily());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_eventhub_connect");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_eventhub_connect", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for GET_eventhub_connectionId_deviceTelemetry_deviceId
        vertx.eventBus().<JsonObject> consumer(GET_EVENTHUB_CONNECTIONID_DEVICETELEMETRY_DEVICEID_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                String deviceId = message.body().getString("deviceId");
                service.eventhubConnectionIdDeviceTelemetryDeviceIdGet(connectionId, deviceId, result -> {
                    if (result.succeeded())
                        message.reply(new JsonObject(Json.encode(result.result())).encodePrettily());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "GET_eventhub_connectionId_deviceTelemetry_deviceId");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("GET_eventhub_connectionId_deviceTelemetry_deviceId", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for PUT_eventhub_connectionId_disconnect_
        vertx.eventBus().<JsonObject> consumer(PUT_EVENTHUB_CONNECTIONID_DISCONNECT__SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                service.eventhubConnectionIdDisconnectPut(connectionId, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_eventhub_connectionId_disconnect_");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_eventhub_connectionId_disconnect_", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for PUT_eventhub_connectionId_enableTelemetry
        vertx.eventBus().<JsonObject> consumer(PUT_EVENTHUB_CONNECTIONID_ENABLETELEMETRY_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                service.eventhubConnectionIdEnableTelemetryPut(connectionId, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_eventhub_connectionId_enableTelemetry");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_eventhub_connectionId_enableTelemetry", e);
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
