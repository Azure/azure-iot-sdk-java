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

public class ModuleApiVerticle extends AbstractVerticle {
    final static Logger LOGGER = LoggerFactory.getLogger(ModuleApiVerticle.class);

    final static String PUT_MODULE_CONNECTFROMENVIRONMENT_TRANSPORTTYPE_SERVICE_ID = "PUT_module_connectFromEnvironment_transportType";
    final static String PUT_MODULE_CONNECT_TRANSPORTTYPE_SERVICE_ID = "PUT_module_connect_transportType";
    final static String PUT_MODULE_CONNECTIONID_DEVICEMETHOD_DEVICEID_SERVICE_ID = "PUT_module_connectionId_deviceMethod_deviceId";
    final static String PUT_MODULE_CONNECTIONID_DISCONNECT_SERVICE_ID = "PUT_module_connectionId_disconnect";
    final static String PUT_MODULE_CONNECTIONID_ENABLEINPUTMESSAGES_SERVICE_ID = "PUT_module_connectionId_enableInputMessages";
    final static String PUT_MODULE_CONNECTIONID_ENABLEMETHODS_SERVICE_ID = "PUT_module_connectionId_enableMethods";
    final static String PUT_MODULE_CONNECTIONID_ENABLETWIN_SERVICE_ID = "PUT_module_connectionId_enableTwin";
    final static String PUT_MODULE_CONNECTIONID_EVENT_SERVICE_ID = "PUT_module_connectionId_event";
    final static String GET_MODULE_CONNECTIONID_INPUTMESSAGE_INPUTNAME_SERVICE_ID = "GET_module_connectionId_inputMessage_inputName";
    final static String PUT_MODULE_CONNECTIONID_MODULEMETHOD_DEVICEID_MODULEID_SERVICE_ID = "PUT_module_connectionId_moduleMethod_deviceId_moduleId";
    final static String PUT_MODULE_CONNECTIONID_OUTPUTEVENT_OUTPUTNAME_SERVICE_ID = "PUT_module_connectionId_outputEvent_outputName";
    final static String PUT_MODULE_CONNECTIONID_ROUNDTRIPMETHODCALL_METHODNAME_SERVICE_ID = "PUT_module_connectionId_roundtripMethodCall_methodName";
    final static String GET_MODULE_CONNECTIONID_TWINDESIREDPROPPATCH_SERVICE_ID = "GET_module_connectionId_twinDesiredPropPatch";
    final static String GET_MODULE_CONNECTIONID_TWIN_SERVICE_ID = "GET_module_connectionId_twin";
    final static String PATCH_MODULE_CONNECTIONID_TWIN_SERVICE_ID = "PATCH_module_connectionId_twin";

    final ModuleApi service;

    public ModuleApiVerticle() {
        try {
            Class serviceImplClass = getClass().getClassLoader().loadClass("io.swagger.server.api.verticle.ModuleApiImpl");
            service = (ModuleApi)serviceImplClass.newInstance();
        } catch (Exception e) {
            logUnexpectedError("ModuleApiVerticle constructor", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() throws Exception {

        //Consumer for PUT_module_connectFromEnvironment_transportType
        vertx.eventBus().<JsonObject> consumer(PUT_MODULE_CONNECTFROMENVIRONMENT_TRANSPORTTYPE_SERVICE_ID).handler(message -> {
            try {
                String transportType = message.body().getString("transportType");
                service.moduleConnectFromEnvironmentTransportTypePut(transportType, result -> {
                    if (result.succeeded())
                        message.reply(new JsonObject(Json.encode(result.result())).encodePrettily());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_module_connectFromEnvironment_transportType");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_module_connectFromEnvironment_transportType", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        //Consumer for PUT_module_connect_transportType
        vertx.eventBus().<JsonObject> consumer(PUT_MODULE_CONNECT_TRANSPORTTYPE_SERVICE_ID).handler(message -> {
            try {
                String transportType = message.body().getString("transportType");
                String connectionString = message.body().getString("connectionString");
                Certificate caCertificate = Json.mapper.readValue(message.body().getJsonObject("caCertificate").encode(), Certificate.class);
                service.moduleConnectTransportTypePut(transportType, connectionString, caCertificate, result -> {
                    if (result.succeeded())
                        message.reply(new JsonObject(Json.encode(result.result())).encodePrettily());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_module_connect_transportType");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_module_connect_transportType", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        //Consumer for PUT_module_connectionId_deviceMethod_deviceId
        vertx.eventBus().<JsonObject> consumer(PUT_MODULE_CONNECTIONID_DEVICEMETHOD_DEVICEID_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                String deviceId = message.body().getString("deviceId");
                Object methodInvokeParameters = message.body().getJsonObject("methodInvokeParameters");
                service.moduleConnectionIdDeviceMethodDeviceIdPut(connectionId, deviceId, methodInvokeParameters, result -> {
                    if (result.succeeded())
                        message.reply(new JsonObject(Json.encode(result.result())).encodePrettily());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_module_connectionId_deviceMethod_deviceId");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_module_connectionId_deviceMethod_deviceId", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        //Consumer for PUT_module_connectionId_disconnect
        vertx.eventBus().<JsonObject> consumer(PUT_MODULE_CONNECTIONID_DISCONNECT_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                service.moduleConnectionIdDisconnectPut(connectionId, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_module_connectionId_disconnect");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_module_connectionId_disconnect", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        //Consumer for PUT_module_connectionId_enableInputMessages
        vertx.eventBus().<JsonObject> consumer(PUT_MODULE_CONNECTIONID_ENABLEINPUTMESSAGES_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                service.moduleConnectionIdEnableInputMessagesPut(connectionId, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_module_connectionId_enableInputMessages");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_module_connectionId_enableInputMessages", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        //Consumer for PUT_module_connectionId_enableMethods
        vertx.eventBus().<JsonObject> consumer(PUT_MODULE_CONNECTIONID_ENABLEMETHODS_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                service.moduleConnectionIdEnableMethodsPut(connectionId, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_module_connectionId_enableMethods");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_module_connectionId_enableMethods", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        //Consumer for PUT_module_connectionId_enableTwin
        vertx.eventBus().<JsonObject> consumer(PUT_MODULE_CONNECTIONID_ENABLETWIN_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                service.moduleConnectionIdEnableTwinPut(connectionId, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_module_connectionId_enableTwin");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_module_connectionId_enableTwin", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        //Consumer for PUT_module_connectionId_event
        vertx.eventBus().<JsonObject> consumer(PUT_MODULE_CONNECTIONID_EVENT_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                String eventBody = message.body().getString("eventBody");
                service.moduleConnectionIdEventPut(connectionId, eventBody, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_module_connectionId_event");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_module_connectionId_event", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        //Consumer for GET_module_connectionId_inputMessage_inputName
        vertx.eventBus().<JsonObject> consumer(GET_MODULE_CONNECTIONID_INPUTMESSAGE_INPUTNAME_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                String inputName = message.body().getString("inputName");
                service.moduleConnectionIdInputMessageInputNameGet(connectionId, inputName, result -> {
                    if (result.succeeded())
                        message.reply(result.result());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "GET_module_connectionId_inputMessage_inputName");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("GET_module_connectionId_inputMessage_inputName", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        //Consumer for PUT_module_connectionId_moduleMethod_deviceId_moduleId
        vertx.eventBus().<JsonObject> consumer(PUT_MODULE_CONNECTIONID_MODULEMETHOD_DEVICEID_MODULEID_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                String deviceId = message.body().getString("deviceId");
                String moduleId = message.body().getString("moduleId");
                Object methodInvokeParameters = message.body().getJsonObject("methodInvokeParameters");
                service.moduleConnectionIdModuleMethodDeviceIdModuleIdPut(connectionId, deviceId, moduleId, methodInvokeParameters, result -> {
                    if (result.succeeded())
                        message.reply(new JsonObject(Json.encode(result.result())).encodePrettily());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_module_connectionId_moduleMethod_deviceId_moduleId");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_module_connectionId_moduleMethod_deviceId_moduleId", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        //Consumer for PUT_module_connectionId_outputEvent_outputName
        vertx.eventBus().<JsonObject> consumer(PUT_MODULE_CONNECTIONID_OUTPUTEVENT_OUTPUTNAME_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                String outputName = message.body().getString("outputName");
                String eventBody = message.body().getString("eventBody");
                service.moduleConnectionIdOutputEventOutputNamePut(connectionId, outputName, eventBody, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_module_connectionId_outputEvent_outputName");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_module_connectionId_outputEvent_outputName", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        //Consumer for PUT_module_connectionId_roundtripMethodCall_methodName
        vertx.eventBus().<JsonObject> consumer(PUT_MODULE_CONNECTIONID_ROUNDTRIPMETHODCALL_METHODNAME_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                String methodName = message.body().getString("methodName");
                RoundtripMethodCallBody requestAndResponse = Json.mapper.readValue(message.body().getJsonObject("requestAndResponse").encode(), RoundtripMethodCallBody.class);
                service.moduleConnectionIdRoundtripMethodCallMethodNamePut(connectionId, methodName, requestAndResponse, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PUT_module_connectionId_roundtripMethodCall_methodName");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PUT_module_connectionId_roundtripMethodCall_methodName", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        //Consumer for GET_module_connectionId_twinDesiredPropPatch
        vertx.eventBus().<JsonObject> consumer(GET_MODULE_CONNECTIONID_TWINDESIREDPROPPATCH_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                service.moduleConnectionIdTwinDesiredPropPatchGet(connectionId, result -> {
                    if (result.succeeded())
                        message.reply(new JsonObject(Json.encode(result.result())).encodePrettily());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "GET_module_connectionId_twinDesiredPropPatch");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("GET_module_connectionId_twinDesiredPropPatch", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        //Consumer for GET_module_connectionId_twin
        vertx.eventBus().<JsonObject> consumer(GET_MODULE_CONNECTIONID_TWIN_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                service.moduleConnectionIdTwinGet(connectionId, result -> {
                    if (result.succeeded())
                        message.reply(new JsonObject(Json.encode(result.result())).encodePrettily());
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "GET_module_connectionId_twin");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("GET_module_connectionId_twin", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        //Consumer for PATCH_module_connectionId_twin
        vertx.eventBus().<JsonObject> consumer(PATCH_MODULE_CONNECTIONID_TWIN_SERVICE_ID).handler(message -> {
            try {
                String connectionId = message.body().getString("connectionId");
                Object props = message.body().getJsonObject("props");
                service.moduleConnectionIdTwinPatch(connectionId, props, result -> {
                    if (result.succeeded())
                        message.reply(null);
                    else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "PATCH_module_connectionId_twin");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("PATCH_module_connectionId_twin", e);
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
