package io.swagger.server.api.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import io.swagger.server.api.MainApiException;

public class RegistryApiVerticle extends AbstractVerticle {
    final static Logger LOGGER = LoggerFactory.getLogger(RegistryApiVerticle.class); 
    
    final static String PUT_REGISTRY_CONNECT_SERVICE_ID = "PUT_registry_connect";
    final static String PUT_REGISTRY_CONNECTIONID_DISCONNECT__SERVICE_ID = "PUT_registry_connectionId_disconnect_";
    final static String GET_REGISTRY_CONNECTIONID_MODULETWIN_DEVICEID_MODULEID_SERVICE_ID = "GET_registry_connectionId_moduleTwin_deviceId_moduleId";
    final static String PATCH_REGISTRY_CONNECTIONID_MODULETWIN_DEVICEID_MODULEID_SERVICE_ID = "PATCH_registry_connectionId_moduleTwin_deviceId_moduleId";
    
    final RegistryApi service;

    public RegistryApiVerticle() {
        try {
            Class serviceImplClass = getClass().getClassLoader().loadClass("io.swagger.server.api.verticle.RegistryApiImpl");
            service = (RegistryApi)serviceImplClass.newInstance();
        } catch (Exception e) {
            logUnexpectedError("RegistryApiVerticle constructor", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() throws Exception {
        
        //Consumer for PUT_registry_connect
        vertx.eventBus().<JsonObject> consumer(PUT_REGISTRY_CONNECT_SERVICE_ID).handler(message -> {
            try {
                String connectionString = message.body().getString("connectionString");
                service.registryConnectPut(connectionString, result -> {
                    if (result.succeeded())
                        message.reply(new JsonObject(Json.encode(result.result())).encodePrettily());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_registry_connect");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_registry_connect", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for PUT_registry_connectionId_disconnect_
        vertx.eventBus().<JsonObject> consumer(PUT_REGISTRY_CONNECTIONID_DISCONNECT__SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                service.registryConnectionIdDisconnectPut(connectionId, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_registry_connectionId_disconnect_");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_registry_connectionId_disconnect_", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for GET_registry_connectionId_moduleTwin_deviceId_moduleId
        vertx.eventBus().<JsonObject> consumer(GET_REGISTRY_CONNECTIONID_MODULETWIN_DEVICEID_MODULEID_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                String deviceId = message.body().getString("deviceId");
                String moduleId = message.body().getString("moduleId");
                service.registryConnectionIdModuleTwinDeviceIdModuleIdGet(connectionId, deviceId, moduleId, result -> {
                    if (result.succeeded())
                        message.reply(new JsonObject(Json.encode(result.result())).encodePrettily());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "GET_registry_connectionId_moduleTwin_deviceId_moduleId");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("GET_registry_connectionId_moduleTwin_deviceId_moduleId", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
        //Consumer for PATCH_registry_connectionId_moduleTwin_deviceId_moduleId
        vertx.eventBus().<JsonObject> consumer(PATCH_REGISTRY_CONNECTIONID_MODULETWIN_DEVICEID_MODULEID_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                String deviceId = message.body().getString("deviceId");
                String moduleId = message.body().getString("moduleId");
                Object props = message.body().getJsonObject("props");
                service.registryConnectionIdModuleTwinDeviceIdModuleIdPatch(connectionId, deviceId, moduleId, props, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PATCH_registry_connectionId_moduleTwin_deviceId_moduleId");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PATCH_registry_connectionId_moduleTwin_deviceId_moduleId", e);
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
