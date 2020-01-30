// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.device;

import com.fasterxml.jackson.core.type.TypeReference;
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
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinPropertyUpdate;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinPropertyUpdate.DigitalTwinPropertyUpdateBuilder;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinReportProperty;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.dto.DigitalTwinCommand;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.dto.DigitalTwinCommand.CommandRequest;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.dto.DigitalTwinComponentRegistrationMessage;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.dto.DigitalTwinComponentRegistrationMessage.ModelInformation;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.dto.DigitalTwinComponentRegistrationMessage.ModelInformation.ModelInformationBuilder;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.dto.JsonRawValue;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Supplier;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK_EMPTY;
import static com.microsoft.azure.sdk.iot.device.Message.DEFAULT_IOTHUB_MESSAGE_CHARSET;
import static com.microsoft.azure.sdk.iot.device.MessageType.DEVICE_TELEMETRY;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinComponent.STATUS_CODE_INVALID;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinComponent.STATUS_CODE_NOT_IMPLEMENTED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_COMPONENTS_ALREADY_BOUND;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_COMPONENTS_NOT_BOUND;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.deserialize;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.serialize;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.TwinPropertyJsonSerializer.DIGITAL_TWIN_COMPONENT_NAME_PREFIX;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.TwinPropertyJsonSerializer.serializeReportProperty;
import static io.reactivex.rxjava3.core.BackpressureStrategy.BUFFER;
import static java.util.Collections.singleton;

/**
 * Convenience layer handle to bind Digital Twin interface handles to the IoTHub transport.
 */
@Slf4j
public final class DigitalTwinDeviceClient {
    private static final int NON_DIGITAL_TWIN_COMMAND_CODE = STATUS_CODE_NOT_IMPLEMENTED;
    private static final int COMPONENT_NOT_FOUND_CODE = STATUS_CODE_NOT_IMPLEMENTED;
    private static final int INVALID_COMMAND_PAYLOAD_CODE = STATUS_CODE_INVALID;
    private static final int INVALID_METHOD_PAYLOAD_CODE = STATUS_CODE_INVALID;
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String NON_DIGITAL_TWIN_COMMAND_MESSAGE_PATTERN = "\"Non digital twin command [%s].\"";
    private static final String COMPONENT_NOT_FOUND_MESSAGE_PATTERN = "\"Component [%s] not found.\"";
    private static final String INVALID_METHOD_PAYLOAD_MESSAGE_PATTERN = "\"Invalid method payload: %s.\"";
    private static final String INVALID_METHOD_PAYLOAD_TYPE_MESSAGE = "\"Method data is not byte array.\"";
    private static final String DIGITAL_TWIN_MODEL_DISCOVERY_COMPONENT_NAME = "urn_azureiot_ModelDiscovery_ModelInformation";
    private static final String DIGITAL_TWIN_MODEL_DISCOVERY_INTERFACE_ID = "urn:azureiot:ModelDiscovery:ModelInformation:1";
    private static final String PROPERTY_DIGITAL_TWIN_INTERFACE_ID = "$.ifid";
    private static final String DIGITAL_TWIN_MODEL_DISCOVERY_MESSAGE_SCHEMA = "modelInformation";
    private static final Pattern COMMAND_PARSER = Pattern.compile(String.format(
            "^\\%s(.+)\\*(.+)$",
            DIGITAL_TWIN_COMPONENT_NAME_PREFIX
    ));
    static final String PROPERTY_DIGITAL_TWIN_COMPONENT = "$.ifname";
    static final String PROPERTY_MESSAGE_SCHEMA = "$.schema";
    static final String PROPERTY_COMMAND_NAME = "iothub-command-name";
    static final String PROPERTY_REQUEST_ID = "iothub-command-request-id";
    static final String PROPERTY_STATUS = "iothub-command-statuscode";

    private final DeviceClientManager deviceClientManager;
    private final String deviceCapabilityModelId;
    private final Map<String, AbstractDigitalTwinComponent> components;
    private final Object lock;

    /**
     * Create Digital Twin device client instance based on a pre-existing {@link DeviceClient}.
     * This constructor is used when initially bringing up Digital Twin, when the Digital Twin maps to an an IoTHub device (as opposed to an IoTHub module).
     * DigitalTwinDeviceClient also guarantee thread safety at the Digital Twin layer.
     * Prior to invoking this function, applications *MUST* specify all options on the {@link DeviceClient} that are required.
     * Callers MUST NOT directly access {@link DeviceClient} after.
     *
     * @param deviceClient An {@link DeviceClient} that has been already created and bound to a specific connection string (or transport, or DPS handle, or whatever mechanism is preferred).
     * @param deviceCapabilityModelId     Device Capability Model Id
     *
     */
    public DigitalTwinDeviceClient(@NonNull DeviceClient deviceClient, @NonNull String deviceCapabilityModelId) {
        this.deviceClientManager = new DeviceClientManager(deviceClient);
        this.deviceCapabilityModelId = deviceCapabilityModelId;
        this.components = new ConcurrentHashMap<>();
        this.lock = new Object();
    }

    /**
     * Bind all digital twin components with Digital Twin device client.
     * @param digitalTwinComponents An list of {@link AbstractDigitalTwinComponent}s to bind with the service.
     * @return Result of this sync function.
     */
    public DigitalTwinClientResult bindComponents(@NonNull final List<? extends AbstractDigitalTwinComponent> digitalTwinComponents) {
        synchronized (lock) {
            if (!components.isEmpty()) {
                return DIGITALTWIN_CLIENT_ERROR_COMPONENTS_ALREADY_BOUND;
            }

            for (AbstractDigitalTwinComponent digitalTwinComponent : digitalTwinComponents) {
                digitalTwinComponent.setDigitalTwinDeviceClient(this);
                components.put(digitalTwinComponent.getDigitalTwinComponentName(), digitalTwinComponent);
            }
        }
        return DIGITALTWIN_CLIENT_OK;
    }

    /**
     * Sends registration message message to DigitalTwin Service.
     * The call occurs asynchronously. The call returns immediately. The application can either subscribe to handle the result or block to get the result.
     *
     * @return Result of this async function.
     */
    public Single<DigitalTwinClientResult> registerComponentsAsync() {
        synchronized (lock) {
            if (components.isEmpty()) {
                return Single.just(DIGITALTWIN_CLIENT_ERROR_COMPONENTS_NOT_BOUND);
            }
        }
        return ensureConnectAsync()
                .flatMap(new Function<DigitalTwinClientResult, Flowable<DigitalTwinClientResult>>() {
                    @Override
                    public Flowable<DigitalTwinClientResult> apply(DigitalTwinClientResult result) {
                        return sendRegistrationMessageAsync(result);
                    }
                }).singleOrError();
    }

    /**
     * Sends registration message to DigitalTwin Service.
     * The call occurs asynchronously. The call returns immediately. The application can either subscribe to handle the result or block to get the result.
     *
     * @return Result of this sync function.
     */
    public DigitalTwinClientResult registerComponents() {
        return registerComponentsAsync().blockingGet();
    }

    /**
     * Subscribe commands with the DigitalTwin Service.
     * The call occurs asynchronously. The call returns immediately. The application can either subscribe to handle the result or block to get the result.* @return Result of this sync function.
     *
     * @return Result of this async function.
     */
    public Single<DigitalTwinClientResult> subscribeForCommandsAsync() {
        return ensureConnectAsync()
                .flatMap(new Function<DigitalTwinClientResult, Flowable<DigitalTwinClientResult>>() {
                    @Override
                    public Flowable<DigitalTwinClientResult> apply(DigitalTwinClientResult result) {
                        return subscribeCommandsAsync(result);
                    }
                }).singleOrError();
    }

    /**
     * Subscribe commands with the DigitalTwin Service.
     * The call will be blocked and will return the result once command subscription is processed.
     *
     * @return Result of this sync function.
     */
    public DigitalTwinClientResult subscribeForCommands() {
        return subscribeForCommandsAsync().blockingGet();
    }

    /**
     * Subscribe properties with the DigitalTwin Service.
     * The call occurs asynchronously. The call returns immediately. The application can either subscribe to handle the result or block to get the result.* @return Result of this sync function.
     *
     * @return Result of this async function.
     */
    public Single<DigitalTwinClientResult> subscribeForPropertiesAsync() {
        return ensureConnectAsync()
                .flatMap(new Function<DigitalTwinClientResult, Flowable<DigitalTwinClientResult>>() {
                    @Override
                    public Flowable<DigitalTwinClientResult> apply(DigitalTwinClientResult result) {
                        return subscribePropertiesAsync(result);
                    }
                }).singleOrError();
    }

    /**
     * Subscribe properties with the DigitalTwin Service.
     * The call will be blocked and will return the result once properties subscription is processed.
     *
     * @return Result of this sync function.
     */
    public DigitalTwinClientResult subscribeForProperties() {
        return subscribeForPropertiesAsync().blockingGet();
    }

    /**
     * Trigger properties sync up with the DigitalTwin Service.
     * The call occurs asynchronously. The call returns immediately. The application can either subscribe to handle the result or block to get the result.* @return Result of this sync function.
     *
     * @return Result of this async function.
     */
    public Single<DigitalTwinClientResult> syncupPropertiesAsync() {
        return ensureConnectAsync()
                .map(new Function<DigitalTwinClientResult, DigitalTwinClientResult>() {
                    @Override
                    public DigitalTwinClientResult apply(DigitalTwinClientResult result) throws IOException {
                        // TODO Known gap, SDK API accepts no callback, there is no guarantee it's delivered
                        log.debug("Getting DeviceTwin...");
                        deviceClientManager.getDeviceTwin();
                        return DIGITALTWIN_CLIENT_OK;
                    }
                }).singleOrError();
    }

    /**
     * Trigger properties sync up with the DigitalTwin Service.
     * The call will be blocked and will return the result once properties sync up is processed.
     *
     * @return Result of this sync function.
     */
    public DigitalTwinClientResult syncupProperties() {
        return syncupPropertiesAsync().blockingGet();
    }

    /**
     * Application should call this function to notify all components they are now ready to use. All bound component function {@link AbstractDigitalTwinComponent#ready()} will be triggered.
     *
     * @return Failure if no component was bound, success otherwise.
     */
    public DigitalTwinClientResult ready() {
        synchronized (lock) {
            if (components.isEmpty()) {
                return DIGITALTWIN_CLIENT_ERROR_COMPONENTS_NOT_BOUND;
            }

            for (AbstractDigitalTwinComponent component : components.values()) {
                component.ready();
            }
        }
        return DIGITALTWIN_CLIENT_OK;
    }

    Flowable<DigitalTwinClientResult> sendTelemetryAsync(
            @NonNull final String digitalTwinComponentName,
            @NonNull final String payload) {
        return ensureConnectAsync()
                .flatMap(new Function<DigitalTwinClientResult, Flowable<DigitalTwinClientResult>>() {
                    @Override
                    public Flowable<DigitalTwinClientResult> apply(DigitalTwinClientResult result) {
                        return Flowable.create(new FlowableOnSubscribe<DigitalTwinClientResult>() {
                            @Override
                            public void subscribe(FlowableEmitter<DigitalTwinClientResult> emitter) throws Throwable {
                                log.debug("Sending TelemetryAsync...");
                                Message message = new Message(payload);
                                message.setProperty(PROPERTY_DIGITAL_TWIN_COMPONENT, digitalTwinComponentName);
                                message.setContentTypeFinal(CONTENT_TYPE_APPLICATION_JSON);
                                message.setContentEncoding(DEFAULT_IOTHUB_MESSAGE_CHARSET.name());
                                IotHubEventCallback callback = createIotHubEventCallback(emitter);
                                deviceClientManager.sendEventAsync(message, callback, callback);
                                log.debug("SendTelemetryAsync succeeded.");
                            }
                        }, BUFFER);
                    }
                });
    }

    Flowable<DigitalTwinClientResult> reportPropertiesAsync(
            @NonNull final String digitalTwinComponentName,
            @NonNull final List<DigitalTwinReportProperty> digitalTwinReportProperties) {
        return ensureConnectAsync()
                .flatMap(new Function<DigitalTwinClientResult, Flowable<DigitalTwinClientResult>>() {
                    @Override
                    public Flowable<DigitalTwinClientResult> apply(DigitalTwinClientResult result) {
                        return Flowable.create(new FlowableOnSubscribe<DigitalTwinClientResult>() {
                            @Override
                            public void subscribe(FlowableEmitter<DigitalTwinClientResult> emitter) throws Throwable {
                                log.debug("Reporting PropertiesAsync...");
                                // TODO Known gap, SDK API with ambiguous Object
                                Property property = serializeReportProperty(digitalTwinComponentName, digitalTwinReportProperties);
                                deviceClientManager.sendReportedProperties(singleton(property));
                                // TODO Known gap, SDK API accepts no callback, there is no guarantee it's delivered
                                log.debug("ReportPropertiesAsync succeeded.");
                                notifyEmitter(emitter, DIGITALTWIN_CLIENT_OK);
                            }
                        }, BUFFER);
                    }
                });
    }

    Flowable<DigitalTwinClientResult> updateAsyncCommandStatusAsync(
            @NonNull final String digitalTwinComponentName,
            @NonNull final DigitalTwinAsyncCommandUpdate digitalTwinAsyncCommandUpdate) {
        return ensureConnectAsync()
                .flatMap(new Function<DigitalTwinClientResult, Flowable<DigitalTwinClientResult>>() {
                    @Override
                    public Flowable<DigitalTwinClientResult> apply(DigitalTwinClientResult result) {
                        return Flowable.create(new FlowableOnSubscribe<DigitalTwinClientResult>() {
                            @Override
                            public void subscribe(FlowableEmitter<DigitalTwinClientResult> emitter) {
                                log.debug("Updating AsyncCommandStatus...");
                                Message message = new Message(digitalTwinAsyncCommandUpdate.getPayload());
                                message.setProperty(PROPERTY_DIGITAL_TWIN_COMPONENT, digitalTwinComponentName);
                                message.setProperty(PROPERTY_COMMAND_NAME, digitalTwinAsyncCommandUpdate.getCommandName());
                                message.setProperty(PROPERTY_REQUEST_ID, digitalTwinAsyncCommandUpdate.getRequestId());
                                message.setProperty(PROPERTY_STATUS, String.valueOf(digitalTwinAsyncCommandUpdate.getStatusCode()));
                                message.setContentTypeFinal(CONTENT_TYPE_APPLICATION_JSON);
                                message.setContentEncoding(DEFAULT_IOTHUB_MESSAGE_CHARSET.name());
                                IotHubEventCallback asyncCommandCallback = createIotHubEventCallback(emitter);
                                deviceClientManager.sendEventAsync(message, asyncCommandCallback, asyncCommandCallback);
                                log.debug("UpdateAsyncCommandStatus succeeded.");
                            }
                        }, BUFFER);
                    }
                });
    }

    private Flowable<DigitalTwinClientResult> ensureConnectAsync() {
        return Flowable.fromSupplier(new Supplier<DigitalTwinClientResult>() {
            @Override
            public DigitalTwinClientResult get() throws Exception {
                deviceClientManager.open();
                return DIGITALTWIN_CLIENT_OK;
            }
        });
    }

    @SuppressWarnings("unused")
    private Flowable<DigitalTwinClientResult> sendRegistrationMessageAsync(DigitalTwinClientResult digitalTwinClientResult) {
        return Flowable.create(new FlowableOnSubscribe<DigitalTwinClientResult>() {
            @Override
            public void subscribe(FlowableEmitter<DigitalTwinClientResult> emitter) throws Throwable {
                ModelInformationBuilder modelInformationBuilder = ModelInformation.builder();
                modelInformationBuilder.dcmId(deviceCapabilityModelId);
                synchronized (lock) {
                    for (AbstractDigitalTwinComponent component : components.values()) {
                        String componentName = component.getDigitalTwinComponentName();
                        String interfaceId = component.getDigitalTwinInterfaceId();
                        modelInformationBuilder.component(componentName, interfaceId);
                    }
                }
                modelInformationBuilder.component(DIGITAL_TWIN_MODEL_DISCOVERY_COMPONENT_NAME, DIGITAL_TWIN_MODEL_DISCOVERY_INTERFACE_ID);
                ModelInformation modelInformation = modelInformationBuilder.build();
                String payload = serialize(new DigitalTwinComponentRegistrationMessage(modelInformation));
                Message registerInterfacesMessage = new Message(payload);
                registerInterfacesMessage.setMessageType(DEVICE_TELEMETRY);
                registerInterfacesMessage.setProperty(PROPERTY_MESSAGE_SCHEMA, DIGITAL_TWIN_MODEL_DISCOVERY_MESSAGE_SCHEMA);
                registerInterfacesMessage.setProperty(PROPERTY_DIGITAL_TWIN_COMPONENT, DIGITAL_TWIN_MODEL_DISCOVERY_COMPONENT_NAME);
                registerInterfacesMessage.setProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_ID, DIGITAL_TWIN_MODEL_DISCOVERY_INTERFACE_ID);
                registerInterfacesMessage.setContentTypeFinal(CONTENT_TYPE_APPLICATION_JSON);
                registerInterfacesMessage.setContentEncoding(DEFAULT_IOTHUB_MESSAGE_CHARSET.name());
                log.debug("Sending registration message...");
                deviceClientManager.sendEventAsync(registerInterfacesMessage, createIotHubEventCallback(emitter), emitter);
            }
        }, BUFFER);
    }

    @SuppressWarnings("unused")
    private Flowable<DigitalTwinClientResult> subscribeCommandsAsync(DigitalTwinClientResult digitalTwinClientResult) {
        return Flowable.create(new FlowableOnSubscribe<DigitalTwinClientResult>() {
            @Override
            public void subscribe(FlowableEmitter<DigitalTwinClientResult> emitter) throws Throwable {
                log.debug("Subscribing command...");
                DeviceMethodCallback deviceMethodCallback = new DigitalTwinCommandDispatcher();
                deviceClientManager.subscribeToDeviceMethod(
                        deviceMethodCallback,
                        emitter,
                        createIotHubEventCallback(emitter),
                        emitter
                );
            }
        }, BUFFER);
    }

    @SuppressWarnings("unused")
    private Flowable<DigitalTwinClientResult> subscribePropertiesAsync(DigitalTwinClientResult digitalTwinClientResult) {
        return Flowable.create(new FlowableOnSubscribe<DigitalTwinClientResult>() {
            @Override
            public void subscribe(FlowableEmitter<DigitalTwinClientResult> emitter) throws Throwable {
                log.debug("Subscribing twin...");
                TwinPropertyCallBack twinPropertyCallBack = new DigitalTwinPropertyDispatcher();
                deviceClientManager.startDeviceTwin(
                        createIotHubEventCallback(emitter),
                        emitter,
                        twinPropertyCallBack,
                        emitter
                );
            }
        }, BUFFER);
    }

    private static boolean isSuccess(IotHubStatusCode statusCode) {
        return statusCode == OK || statusCode == OK_EMPTY;
    }

    private static IotHubEventCallback createIotHubEventCallback(final FlowableEmitter<DigitalTwinClientResult> emitter) {
        return new IotHubEventCallback() {
            @Override
            public void execute(IotHubStatusCode statusCode, Object context) {
                if (isSuccess(statusCode)) {
                    notifyEmitter(emitter, DIGITALTWIN_CLIENT_OK);
                } else {
                    notifyEmitter(emitter, DIGITALTWIN_CLIENT_ERROR);
                }
            }
        };
    }

    private static void notifyEmitter(FlowableEmitter<DigitalTwinClientResult> emitter, DigitalTwinClientResult result) {
        if (!emitter.isCancelled()) {
            emitter.onNext(result);
            emitter.onComplete();
        }
    }

    private class DigitalTwinCommandDispatcher implements DeviceMethodCallback {
        @Override
        public DeviceMethodData call(String methodName, Object methodData, Object context) {
            Matcher componentMatcher = COMMAND_PARSER.matcher(methodName);
            if (componentMatcher.matches()) {
                String componentName = componentMatcher.group(1);
                AbstractDigitalTwinComponent digitalTwinComponent = components.get(componentName);
                if (digitalTwinComponent == null) {
                    return new DeviceMethodData(COMPONENT_NOT_FOUND_CODE, String.format(COMPONENT_NOT_FOUND_MESSAGE_PATTERN, componentName));
                }
                DigitalTwinCommandRequestBuilder digitalTwinCommandRequestBuilder = DigitalTwinCommandRequest.builder()
                                                                                                             .commandName(componentMatcher.group(2));
                if (methodData instanceof byte[]) {
                    try {
                        DigitalTwinCommand digitalTwinCommand = deserialize((byte[]) methodData, DigitalTwinCommand.class);
                        CommandRequest commandRequest = digitalTwinCommand.getCommandRequest();
                        String payload = commandRequest.getValue() != null ? commandRequest.getValue().getRawJson() : null;
                        DigitalTwinCommandRequest digitalTwinCommandRequest = digitalTwinCommandRequestBuilder.requestId(commandRequest.getRequestId())
                                                                                                              .payload(payload)
                                                                                                              .build();
                        DigitalTwinCommandResponse digitalTwinCommandResponse = digitalTwinComponent.onCommandReceived(digitalTwinCommandRequest);
                        return new DeviceMethodData(digitalTwinCommandResponse.getStatus(), digitalTwinCommandResponse.getPayload());
                    } catch (Exception e) {
                        return new DeviceMethodData(INVALID_METHOD_PAYLOAD_CODE, String.format(INVALID_METHOD_PAYLOAD_MESSAGE_PATTERN, e.getMessage()));
                    }
                } else {
                    return new DeviceMethodData(INVALID_COMMAND_PAYLOAD_CODE, INVALID_METHOD_PAYLOAD_TYPE_MESSAGE);
                }
            } else {
                return new DeviceMethodData(NON_DIGITAL_TWIN_COMMAND_CODE, String.format(NON_DIGITAL_TWIN_COMMAND_MESSAGE_PATTERN, methodName));
            }
        }
    }

    private class DigitalTwinPropertyDispatcher implements TwinPropertyCallBack {
        @Override
        public void TwinPropertyCallBack(Property property, Object context) {
            String name = property.getKey();
            if (name.startsWith(DIGITAL_TWIN_COMPONENT_NAME_PREFIX)) {
                String componentName = name.substring(DIGITAL_TWIN_COMPONENT_NAME_PREFIX.length());
                AbstractDigitalTwinComponent digitalTwinComponent = components.get(componentName);
                if (digitalTwinComponent == null) {
                    log.debug("Property ignored: Digital twin component[{}] not found.", componentName);
                    return;
                }
                String payload = property.getValue().toString();
                try {
                    TypeReference<Map<String, JsonRawValue>> typeRef = new TypeReference<Map<String, JsonRawValue>>() {};
                    Map<String, JsonRawValue> properties = deserialize(payload, typeRef);
                    for (Entry<String, JsonRawValue> value : properties.entrySet()) {
                        DigitalTwinPropertyUpdateBuilder propertyUpdateBuilder = DigitalTwinPropertyUpdate.builder()
                                                                                                          .propertyName(value.getKey());
                        if (property.getIsReported()) {
                            propertyUpdateBuilder.propertyReported(value.getValue().getRawJson());
                        } else {
                            propertyUpdateBuilder.desiredVersion(property.getVersion())
                                                 .propertyDesired(value.getValue().getRawJson());
                        }
                        digitalTwinComponent.onPropertyUpdate(propertyUpdateBuilder.build());
                    }
                } catch (Exception e) {
                    log.debug("Property ignored: value is not JSON.", e);
                }
            } else {
                log.debug("Ignored non digital twin property[{}].", name);
            }
        }
    }

}
