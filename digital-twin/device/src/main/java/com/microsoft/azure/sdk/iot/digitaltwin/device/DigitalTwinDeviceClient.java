package com.microsoft.azure.sdk.iot.digitaltwin.device;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinAsyncCommandUpdate;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandRequest;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandRequest.DigitalTwinCommandRequestBuilder;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinInterfaceRegistrationMessage;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinInterfaceRegistrationMessage.ModelInformation;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinInterfaceRegistrationMessage.ModelInformation.ModelInformationBuilder;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinPropertyUpdate;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinPropertyUpdate.DigitalTwinPropertyUpdateBuilder;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinReportProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK_EMPTY;
import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_TELEMETRY;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinInterfaceClient.STATUS_CODE_INVALID;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinInterfaceClient.STATUS_CODE_NOT_IMPLEMENTED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_INTERFACE_ALREADY_REGISTERED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_REGISTRATION_PENDING;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.RegistrationStatus.REGISTERED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.RegistrationStatus.REGISTERING;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.RegistrationStatus.UNREGISTERED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinSdkInformation.DIGITAL_TWIN_SDK_INFORMATION_PROPERTIES;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.CommandJsonSerializer.deserializeCommandRequest;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.deserialize;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.serialize;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.TelemetryJsonSerializer.serializeTelemetry;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.TwinPropertyJsonSerializer.DIGITAL_TWIN_INTERFACE_INSTANCE_NAME_PREFIX;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.TwinPropertyJsonSerializer.serializeReportProperty;
import static java.util.Collections.singleton;
import static lombok.AccessLevel.PACKAGE;

/**
 * Convenience layer handle to bind Digital Twin interface handles to the IoTHub transport.
 */
@Slf4j
public final class DigitalTwinDeviceClient {
    private static final int NONE_DIGITAL_TWIN_COMMAND_CODE = STATUS_CODE_NOT_IMPLEMENTED;
    private static final int INTERFACE_INSTANCE_NOT_FOUND_CODE = STATUS_CODE_NOT_IMPLEMENTED;
    private static final int INVALID_COMMAND_PAYLOAD_CODE = STATUS_CODE_INVALID;
    private static final int INVALID_METHOD_PAYLOAD_CODE = STATUS_CODE_INVALID;
    private static final String NONE_DIGITAL_TWIN_COMMAND_MESSAGE_PATTERN = "\"None digital twin command [%s].\"";
    private static final String INTERFACE_INSTANCE_NOT_FOUND_MESSAGE_PATTERN = "\"Interface instance [%s] not found.\"";
    private static final String INVALID_METHOD_PAYLOAD_MESSAGE_PATTERN = "\"Invalid method payload: %s.\"";
    private static final String INVALID_METHOD_PAYLOAD_TYPE_MESSAGE = "\"Method data is not byte array.\"";
    private static final String DIGITAL_TWIN_MODEL_DISCOVERY_INTERFACE_INSTANCE = "urn_azureiot_ModelDiscovery_ModelInformation";
    private static final String DIGITAL_TWIN_MODEL_DISCOVERY_INTERFACE_ID = "urn:azureiot:ModelDiscovery:ModelInformation:1";
    private static final String DIGITAL_TWIN_SDK_INFORMATION_INTERFACE_INSTANCE = "urn_azureiot_Client_SDKInformation";
    private static final String DIGITAL_TWIN_SDK_INFORMATION_INTERFACE_ID = "urn:azureiot:Client:SDKInformation:1";
    private static final String PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE = "$.ifname";
    private static final String PROPERTY_DIGITAL_TWIN_INTERFACE_ID = "$.ifid";
    private static final String PROPERTY_MESSAGE_SCHEMA = "$.schema";
    private static final String PROPERTY_COMMAND_NAME = "iothub-command-name";
    private static final String PROPERTY_REQUEST_ID = "iothub-command-request-id";
    private static final String PROPERTY_STATUS = "iothub-command-statuscode";
    private static final String TOKEN_INTERFACE_INSTANCE_NAME = "interfaceInstanceName";
    private static final String TOKEN_COMMAND_NAME = "command";
    private static final Pattern COMMAND_PARSER = Pattern.compile(String.format(
            "^\\%s(?<%s>.*)\\*(?<%s>.*)$",
            DIGITAL_TWIN_INTERFACE_INSTANCE_NAME_PREFIX,
            TOKEN_INTERFACE_INSTANCE_NAME,
            TOKEN_COMMAND_NAME
    ));

    private static final String DIGITAL_TWIN_MODEL_DISCOVERY_MESSAGE_SCHEMA = "modelInformation";

    private final DeviceClient deviceClient;
    private final Map<String, AbstractDigitalTwinInterfaceClient> digitalTwinInterfaceClients;
    private final Object lock;
    @Getter(PACKAGE)
    private RegistrationStatus registrationStatus;

    /**
     * Create Digital Twin device client instance based on a pre-existing {@link DeviceClient}.
     * This constructor is used when initially bringing up Digital Twin, when the Digital Twin maps to an an IoTHub device (as opposed to an IoTHub module).
     * DigitalTwinDeviceClient also guarantee thread safety at the Digital Twin layer.
     * Prior to invoking this function, applications *MUST* specify all options on the {@link DeviceClient} that are required.
     * Callers MUST NOT directly access {@link DeviceClient} after.
     *
     * @param deviceClient  An {@link DeviceClient} that has been already created and bound to a specific connection string (or transport, or DPS handle, or whatever mechanism is preferred).
     */
    public DigitalTwinDeviceClient(@NonNull DeviceClient deviceClient) {
        this.deviceClient = deviceClient;
        this.registrationStatus = UNREGISTERED;
        this.digitalTwinInterfaceClients = new HashMap<>();
        this.lock = new Object();
    }

    /**
     * Registers the specified {@link AbstractDigitalTwinInterfaceClient} with the DigitalTwin Service. It registers specified dtInterfaces with the Digital Twin Service.
     * This registration occurs asynchronously. While registration is in progress, {@link AbstractDigitalTwinInterfaceClient}'s that are being registered nor will they be able to receive commands.
     * It must not be called multiple times.  If a given Digital Twin device needs to have its handles re-registered, it needs to create a new one.
     * @param deviceCapabilityModelId Device Capability Model Id
     * @param digitalTwinInterfaceClients An list of {@link AbstractDigitalTwinInterfaceClient}s to register with the service.
     * @param digitalTwinInterfaceRegistrationCallback User specified callback that will be invoked on registration completion or failure. Callers should not begin sending Digital Twin telemetry until this callback is invoked.
     * @param context User context that is provided to the callback.
     * @return if this async function is accepted or not
     */
    public DigitalTwinClientResult registerInterfacesAsync(
            @NonNull final String deviceCapabilityModelId,
            @NonNull final List<? extends AbstractDigitalTwinInterfaceClient> digitalTwinInterfaceClients,
            @NonNull final DigitalTwinCallback digitalTwinInterfaceRegistrationCallback,
            final Object context) {
        synchronized (lock) {
            if (registrationStatus == REGISTERING) {
                return DIGITALTWIN_CLIENT_ERROR_REGISTRATION_PENDING;
            } else if (registrationStatus == REGISTERED) {
                return DIGITALTWIN_CLIENT_ERROR_INTERFACE_ALREADY_REGISTERED;
            } else {
                registrationStatus = REGISTERING;
            }
        }
        // TODO SDK missing async open function, starting new thread to handle async
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    registerInterfaces(deviceCapabilityModelId, digitalTwinInterfaceClients, digitalTwinInterfaceRegistrationCallback, context);
                } catch (Exception e) {
                    log.debug("RegisterInterfaces failed.", e);
                    onRegistrationFailed(digitalTwinInterfaceRegistrationCallback, context);
                }
            }
        }).start();
        return DIGITALTWIN_CLIENT_OK;
    }

    private void registerInterfaces(
            @NonNull final String deviceCapabilityModelId,
            @NonNull final List<? extends AbstractDigitalTwinInterfaceClient> digitalTwinInterfaceClients,
            @NonNull final DigitalTwinCallback digitalTwinInterfaceRegistrationCallback,
            final Object context) throws IOException {
        ModelInformationBuilder modelInformationBuilder = ModelInformation.builder();
        modelInformationBuilder.dcmId(deviceCapabilityModelId);
        for (AbstractDigitalTwinInterfaceClient digitalTwinInterfaceClient : digitalTwinInterfaceClients) {
            String interfaceInstanceName = digitalTwinInterfaceClient.getDigitalTwinInterfaceInstanceName();
            String interfaceId = digitalTwinInterfaceClient.getDigitalTwinInterfaceId();
            modelInformationBuilder.interfaceInstance(interfaceInstanceName, interfaceId);
            this.digitalTwinInterfaceClients.put(interfaceInstanceName, digitalTwinInterfaceClient);
        }
        ModelInformation modelInformation = modelInformationBuilder.interfaceInstance(DIGITAL_TWIN_MODEL_DISCOVERY_INTERFACE_INSTANCE, DIGITAL_TWIN_MODEL_DISCOVERY_INTERFACE_ID)
                                                                   .interfaceInstance(DIGITAL_TWIN_SDK_INFORMATION_INTERFACE_INSTANCE, DIGITAL_TWIN_SDK_INFORMATION_INTERFACE_ID)
                                                                   .build();
        String payload = serialize(new DigitalTwinInterfaceRegistrationMessage(modelInformation));
        Message registerInterfacesMessage = new Message(payload);
        registerInterfacesMessage.setMessageType(DEVICE_TELEMETRY);
        registerInterfacesMessage.setProperty(PROPERTY_MESSAGE_SCHEMA, DIGITAL_TWIN_MODEL_DISCOVERY_MESSAGE_SCHEMA);
        registerInterfacesMessage.setProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE, DIGITAL_TWIN_MODEL_DISCOVERY_INTERFACE_INSTANCE);
        registerInterfacesMessage.setProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_ID, DIGITAL_TWIN_MODEL_DISCOVERY_INTERFACE_ID);
        log.debug("Connecting device client...");
        deviceClient.open();
        final AtomicBoolean twinEnabled = new AtomicBoolean();
        final DigitalTwinCallback reportSdkInformationCallback = new DigitalTwinCallback() {
            @Override
            public void onResult(DigitalTwinClientResult result, Object context) {
                log.debug("ReportSdkInformation finished with code: {}.", result);
                if (result != DIGITALTWIN_CLIENT_OK) {
                    onRegistrationFailed(digitalTwinInterfaceRegistrationCallback, context);
                    return;
                }
                try {
                    log.debug("Getting DeviceTwin...");
                    deviceClient.getDeviceTwin();
                    log.debug("Get DeviceTwin succeed.");
                } catch (IOException e) {
                    log.debug("GetTwin failed.", e);
                    onRegistrationFailed(digitalTwinInterfaceRegistrationCallback, context);
                    return;
                }
                try {
                    log.debug("Notifying interface instances...");
                    for (AbstractDigitalTwinInterfaceClient digitalTwinInterfaceClient : digitalTwinInterfaceClients) {
                        onDigitalTwinInterfaceClientRegistered(digitalTwinInterfaceClient);
                    }
                    log.debug("Notify interface instances succeed.");
                    onRegistrationSucceed(digitalTwinInterfaceRegistrationCallback, context);
                } catch (Exception e) {
                    log.debug("Notify DigitalTwinInterfaceClient registered failed.", e);
                    onRegistrationFailed(digitalTwinInterfaceRegistrationCallback, context);
                }
            }
        };
        final IotHubEventCallback enableTwinCallback = new IotHubEventCallback() {
            @Override
            public void execute(IotHubStatusCode iotHubStatusCode, Object callbackContext) {
                if (twinEnabled.compareAndSet(false, true)) {
                    log.debug("SubscribeTwin finished with code: {}.", iotHubStatusCode);
                    if (isFailure(iotHubStatusCode)) {
                        log.debug("SubscribeTwin failed with code: {}.", iotHubStatusCode);
                        onRegistrationFailed(digitalTwinInterfaceRegistrationCallback, context);
                        return;
                    }
                    log.debug("Reporting SdkInformation...");
                    reportPropertiesAsync(
                            DIGITAL_TWIN_SDK_INFORMATION_INTERFACE_INSTANCE,
                            DIGITAL_TWIN_SDK_INFORMATION_PROPERTIES,
                            reportSdkInformationCallback,
                            context
                    );
                }
            }
        };
        final IotHubEventCallback enableCommandCallback = new IotHubEventCallback() {
            @Override
            public void execute(IotHubStatusCode iotHubStatusCode, Object callbackContext) {
                log.debug("SubscribeCommand finished with code: {}.", iotHubStatusCode);
                if (isFailure(iotHubStatusCode)) {
                    onRegistrationFailed(digitalTwinInterfaceRegistrationCallback, context);
                    return;
                }
                TwinPropertyCallBack twinPropertyCallBack = new DigitalTwinPropertyDispatcher();
                try {
                    log.debug("Subscribing Twin...");
                    deviceClient.startDeviceTwin(
                            enableTwinCallback,
                            context,
                            twinPropertyCallBack,
                            context);
                    log.debug("Start DeviceTwin succeed.");
                } catch (IOException e) {
                    log.debug("SubscribeTwin failed.", e);
                    onRegistrationFailed(digitalTwinInterfaceRegistrationCallback, context);
                }
            }
        };
        final IotHubEventCallback registrationMessageSendingCallback = new IotHubEventCallback() {
            @Override
            public void execute(IotHubStatusCode iotHubStatusCode, Object o) {
                log.debug("RegistrationMessageSending finished with code: {}.", iotHubStatusCode);
                if (isFailure(iotHubStatusCode)) {
                    onRegistrationFailed(digitalTwinInterfaceRegistrationCallback, context);
                    return;
                }
                DeviceMethodCallback deviceMethodCallback = new DigitalTwinCommandDispatcher();
                try {
                    log.debug("Subscribing command...");
                    deviceClient.subscribeToDeviceMethod(
                            deviceMethodCallback,
                            context,
                            enableCommandCallback,
                            context
                    );
                } catch (Exception e) {
                    log.error("SubscribeCommand failed.", e);
                    onRegistrationFailed(digitalTwinInterfaceRegistrationCallback, context);
                }
            }
        };
        deviceClient.sendEventAsync(
                registerInterfacesMessage,
                registrationMessageSendingCallback,
                context
        );
    }

    private boolean isFailure(IotHubStatusCode statusCode) {
        return statusCode != OK && statusCode != OK_EMPTY;
    }

    private void onRegistrationFailed(@NonNull final DigitalTwinCallback digitalTwinInterfaceRegistrationCallback, final Object context) {
        synchronized (lock) {
            if (registrationStatus == REGISTERING) {
                digitalTwinInterfaceClients.clear();
                registrationStatus = UNREGISTERED;
            } else {
                return;
            }
        }
        log.debug("Registration failed.");
        digitalTwinInterfaceRegistrationCallback.onResult(DIGITALTWIN_CLIENT_ERROR, context);
    }

    private void onRegistrationSucceed(@NonNull final DigitalTwinCallback digitalTwinInterfaceRegistrationCallback, final Object context) {
        synchronized (lock) {
            if (registrationStatus == REGISTERING) {
                registrationStatus = REGISTERED;
            } else {
                return;
            }
        }
        log.debug("Registration succeed.");
        digitalTwinInterfaceRegistrationCallback.onResult(DIGITALTWIN_CLIENT_OK, context);
    }

    DigitalTwinClientResult sendTelemetryAsync(
            @NonNull final String digitalTwinInterfaceInstanceName,
            @NonNull final String telemetryName,
            @NonNull final String payload,
            @NonNull final DigitalTwinCallback digitalTwinTelemetryConfirmationCallback,
            final Object context) {
        try {
            Message message = new Message(serializeTelemetry(telemetryName, payload));
            message.setProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE, digitalTwinInterfaceInstanceName);
            message.setProperty(PROPERTY_MESSAGE_SCHEMA, telemetryName);
            IotHubEventCallback telemetryCallback = createIotHubEventCallback(digitalTwinTelemetryConfirmationCallback);
            deviceClient.sendEventAsync(message, telemetryCallback, context);
            return DIGITALTWIN_CLIENT_OK;
        } catch (IOException e) {
            log.debug("SendTelemetryAsync failed.", e);
            return DIGITALTWIN_CLIENT_ERROR;
        }
    }

    DigitalTwinClientResult reportPropertiesAsync(
            @NonNull final String digitalTwinInterfaceInstanceName,
            @NonNull final List<DigitalTwinReportProperty> digitalTwinReportProperties,
            @NonNull final DigitalTwinCallback digitalTwinReportedPropertyUpdatedCallback,
            final Object context) {
        try {
            // TODO Known gap, SDK API with ambiguous Object
            Property property = serializeReportProperty(digitalTwinInterfaceInstanceName, digitalTwinReportProperties);
            deviceClient.sendReportedProperties(singleton(property));
            // TODO TODO Known gap, SDK API accepts no callback, there is no guarantee it's delivered
            digitalTwinReportedPropertyUpdatedCallback.onResult(DIGITALTWIN_CLIENT_OK, context);
        } catch (Exception e) {
            log.debug("ReportPropertyAsync failed.", e);
            digitalTwinReportedPropertyUpdatedCallback.onResult(DIGITALTWIN_CLIENT_ERROR, context);
        }
        return DIGITALTWIN_CLIENT_OK;
    }

    DigitalTwinClientResult updateAsyncCommandStatusAsync(
            @NonNull final String digitalTwinInterfaceInstanceName,
            @NonNull final DigitalTwinAsyncCommandUpdate digitalTwinAsyncCommandUpdate,
            @NonNull final DigitalTwinCallback digitalTwinUpdateAsyncCommandStatusCallback,
            final Object context) {
        Message message = new Message(digitalTwinAsyncCommandUpdate.getPayload());
        message.setProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE, digitalTwinInterfaceInstanceName);
        message.setProperty(PROPERTY_COMMAND_NAME, digitalTwinAsyncCommandUpdate.getCommandName());
        message.setProperty(PROPERTY_REQUEST_ID, digitalTwinAsyncCommandUpdate.getRequestId());
        message.setProperty(PROPERTY_STATUS, String.valueOf(digitalTwinAsyncCommandUpdate.getStatusCode()));
        IotHubEventCallback asyncCommandCallback = createIotHubEventCallback(digitalTwinUpdateAsyncCommandStatusCallback);
        deviceClient.sendEventAsync(message, asyncCommandCallback, context);
        return DIGITALTWIN_CLIENT_OK;
    }

    private void onDigitalTwinInterfaceClientRegistered(AbstractDigitalTwinInterfaceClient digitalTwinInterfaceClient) {
        digitalTwinInterfaceClient.setDigitalTwinDeviceClient(this);
        digitalTwinInterfaceClient.onRegistered();
    }

    private static IotHubEventCallback createIotHubEventCallback(final DigitalTwinCallback digitalTwinCallback) {
        return new IotHubEventCallback() {
            @Override
            public void execute(IotHubStatusCode iotHubStatusCode, Object context) {
                if (iotHubStatusCode == OK || iotHubStatusCode == OK_EMPTY) {
                    digitalTwinCallback.onResult(DIGITALTWIN_CLIENT_OK, context);
                } else {
                    digitalTwinCallback.onResult(DIGITALTWIN_CLIENT_ERROR, context);
                }
            }
        };
    }

    private class DigitalTwinCommandDispatcher implements DeviceMethodCallback {
        @Override
        public DeviceMethodData call(String methodName, Object methodData, Object context) {
            Matcher componentMatcher = COMMAND_PARSER.matcher(methodName);
            if (componentMatcher.matches()) {
                String interfaceInstanceName = componentMatcher.group(TOKEN_INTERFACE_INSTANCE_NAME);
                AbstractDigitalTwinInterfaceClient digitalTwinInterfaceClient = digitalTwinInterfaceClients.get(interfaceInstanceName);
                if (digitalTwinInterfaceClient == null) {
                    return new DeviceMethodData(INTERFACE_INSTANCE_NOT_FOUND_CODE, String.format(INTERFACE_INSTANCE_NOT_FOUND_MESSAGE_PATTERN, interfaceInstanceName));
                }
                DigitalTwinCommandRequestBuilder digitalTwinCommandRequestBuilder = DigitalTwinCommandRequest.builder()
                        .commandName(componentMatcher.group(TOKEN_COMMAND_NAME));
                if (methodData instanceof byte[]) {
                    try {
                        deserializeCommandRequest(digitalTwinCommandRequestBuilder, (byte[]) methodData);
                        DigitalTwinCommandResponse digitalTwinCommandResponse = digitalTwinInterfaceClient.onCommandReceived(digitalTwinCommandRequestBuilder.build());
                        return new DeviceMethodData(digitalTwinCommandResponse.getStatus(), digitalTwinCommandResponse.getPayload());
                    } catch (IOException e) {
                        return new DeviceMethodData(INVALID_METHOD_PAYLOAD_CODE, String.format(INVALID_METHOD_PAYLOAD_MESSAGE_PATTERN, e.getMessage()));
                    }
                } else {
                    return new DeviceMethodData(INVALID_COMMAND_PAYLOAD_CODE, INVALID_METHOD_PAYLOAD_TYPE_MESSAGE);
                }
            } else {
                return new DeviceMethodData(NONE_DIGITAL_TWIN_COMMAND_CODE, String.format(NONE_DIGITAL_TWIN_COMMAND_MESSAGE_PATTERN, methodName));
            }
        }
    }

    private class DigitalTwinPropertyDispatcher implements TwinPropertyCallBack {
        @Override
        public void TwinPropertyCallBack(Property property, Object context) {
            String name = property.getKey();
            if (name.startsWith(DIGITAL_TWIN_INTERFACE_INSTANCE_NAME_PREFIX)) {
                String interfaceInstanceName = name.substring(DIGITAL_TWIN_INTERFACE_INSTANCE_NAME_PREFIX.length());
                AbstractDigitalTwinInterfaceClient digitalTwinInterfaceClient = digitalTwinInterfaceClients.get(interfaceInstanceName);
                if (digitalTwinInterfaceClient == null) {
                    log.debug("Property ignored: Digital twin interface instance[{}] not found.", interfaceInstanceName);
                    return;
                }
                String payload = property.getValue().toString();
                try {
                    JsonNode node = deserialize(payload);
                    if (node.isObject()) {
                        Iterator<Entry<String, JsonNode>> fields = node.fields();
                        while(fields.hasNext()) {
                            Entry<String, JsonNode> field = fields.next();
                            DigitalTwinPropertyUpdateBuilder propertyUpdateBuilder = DigitalTwinPropertyUpdate.builder()
                                                                                                              .propertyName(field.getKey());
                            if (!property.getIsReported()) {
                                propertyUpdateBuilder.desiredVersion(property.getVersion())
                                                     .propertyDesired(field.getValue().toString());
                            }
                            digitalTwinInterfaceClient.onPropertyUpdate(propertyUpdateBuilder.build());
                        }
                    } else {
                        log.debug("Property ignored: value is not complex type.");
                    }
                } catch (IOException e) {
                    log.debug("Property ignored: value is not JSON.");
                }
            } else {
                log.debug("Ignored none digital twin property[{}].", name);
            }
        }
    }

}
