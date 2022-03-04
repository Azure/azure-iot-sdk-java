package io.swagger.server.api.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import io.swagger.server.api.MainApiException;

@SuppressWarnings("ALL")
public class ServiceApiVerticle extends AbstractVerticle {
    final static Logger LOGGER = LoggerFactory.getLogger(ServiceApiVerticle.class); 
    
    final static String PUT_SERVICE_CONNECT_SERVICE_ID = "PUT_service_connect";
    final static String PUT_SERVICE_CONNECTIONID_DEVICEMETHOD_DEVICEID_SERVICE_ID = "PUT_service_connectionId_deviceMethod_deviceId";
    final static String PUT_SERVICE_CONNECTIONID_DISCONNECT__SERVICE_ID = "PUT_service_connectionId_disconnect_";
    final static String PUT_SERVICE_CONNECTIONID_MODULEMETHOD_DEVICEID_MODULEID_SERVICE_ID = "PUT_service_connectionId_moduleMethod_deviceId_moduleId";
    
    final ServiceApi service;

    public ServiceApiVerticle() {
        try {
            Class<?> serviceImplClass = getClass().getClassLoader().loadClass("io.swagger.server.api.verticle.ServiceApiImpl");
            service = (ServiceApi)serviceImplClass.newInstance();
        } catch (Exception e) {
            logUnexpectedError("ServiceApiVerticle constructor", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() throws Exception {
        
        //Consumer for PUT_service_connect
        vertx.eventBus().<JsonObject> consumer(PUT_SERVICE_CONNECT_SERVICE_ID).handler(message -> {
            try {
                String connectionString = message.body().getString("connectionString");
                service.serviceConnectPut(connectionString, result -> {
                    if (result.succeeded())
                        message.reply(new JsonObject(Json.encode(result.result())).encodePrettily());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_service_connect");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_service_connect", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for PUT_service_connectionId_deviceMethod_deviceId
        vertx.eventBus().<JsonObject> consumer(PUT_SERVICE_CONNECTIONID_DEVICEMETHOD_DEVICEID_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                String deviceId = message.body().getString("deviceId");
                Object methodInvokeParameters = message.body().getJsonObject("methodInvokeParameters");
                service.serviceConnectionIdDeviceMethodDeviceIdPut(connectionId, deviceId, methodInvokeParameters, result -> {
                    if (result.succeeded())
                        message.reply(new JsonObject(Json.encode(result.result())).encodePrettily());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_service_connectionId_deviceMethod_deviceId");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_service_connectionId_deviceMethod_deviceId", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for PUT_service_connectionId_disconnect_
        vertx.eventBus().<JsonObject> consumer(PUT_SERVICE_CONNECTIONID_DISCONNECT__SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                service.serviceConnectionIdDisconnectPut(connectionId, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_service_connectionId_disconnect_");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_service_connectionId_disconnect_", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for PUT_service_connectionId_moduleMethod_deviceId_moduleId
        vertx.eventBus().<JsonObject> consumer(PUT_SERVICE_CONNECTIONID_MODULEMETHOD_DEVICEID_MODULEID_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                String deviceId = message.body().getString("deviceId");
                String moduleId = message.body().getString("moduleId");
                Object methodInvokeParameters = message.body().getJsonObject("methodInvokeParameters");
                service.serviceConnectionIdModuleMethodDeviceIdModuleIdPut(connectionId, deviceId, moduleId, methodInvokeParameters, result -> {
                    if (result.succeeded())
                        message.reply(new JsonObject(Json.encode(result.result())).encodePrettily());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_service_connectionId_moduleMethod_deviceId_moduleId");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_service_connectionId_moduleMethod_deviceId_moduleId", e);
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
