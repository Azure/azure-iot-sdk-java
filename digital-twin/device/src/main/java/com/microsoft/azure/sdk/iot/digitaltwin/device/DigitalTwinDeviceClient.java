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
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.dto.DigitalTwinInterfaceRegistrationMessage;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.dto.DigitalTwinInterfaceRegistrationMessage.ModelInformation;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.dto.DigitalTwinInterfaceRegistrationMessage.ModelInformation.ModelInformationBuilder;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.dto.JsonRawValue;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Supplier;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
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
import static com.microsoft.azure.sdk.iot.digitaltwin.device.model.dto.DigitalTwinSdkInformation.DIGITAL_TWIN_SDK_INFORMATION_PROPERTIES;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.deserialize;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.serialize;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.TwinPropertyJsonSerializer.DIGITAL_TWIN_INTERFACE_INSTANCE_NAME_PREFIX;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.TwinPropertyJsonSerializer.serializeReportProperty;
import static io.reactivex.rxjava3.core.BackpressureStrategy.BUFFER;
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
    private static final String PROPERTY_DIGITAL_TWIN_INTERFACE_ID = "$.ifid";
    private static final String DIGITAL_TWIN_MODEL_DISCOVERY_MESSAGE_SCHEMA = "modelInformation";
    private static final Pattern COMMAND_PARSER = Pattern.compile(String.format(
            "^\\%s(.+)\\*(.+)$",
            DIGITAL_TWIN_INTERFACE_INSTANCE_NAME_PREFIX
    ));
    static final String PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE = "$.ifname";
    static final String PROPERTY_MESSAGE_SCHEMA = "$.schema";
    static final String PROPERTY_COMMAND_NAME = "iothub-command-name";
    static final String PROPERTY_REQUEST_ID = "iothub-command-request-id";
    static final String PROPERTY_STATUS = "iothub-command-statuscode";

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
     * @param deviceClient An {@link DeviceClient} that has been already created and bound to a specific connection string (or transport, or DPS handle, or whatever mechanism is preferred).
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
     *
     * @param deviceCapabilityModelId     Device Capability Model Id
     * @param digitalTwinInterfaceClients An list of {@link AbstractDigitalTwinInterfaceClient}s to register with the service.
     * @return if this async function is accepted or not
     */
    public Flowable<DigitalTwinClientResult> registerInterfacesAsync(@NonNull final String deviceCapabilityModelId,
            @NonNull final List<? extends AbstractDigitalTwinInterfaceClient> digitalTwinInterfaceClients) {
        synchronized (lock) {
            if (registrationStatus == REGISTERING) {
                return Flowable.just(DIGITALTWIN_CLIENT_ERROR_REGISTRATION_PENDING);
            } else if (registrationStatus == REGISTERED) {
                return Flowable.just(DIGITALTWIN_CLIENT_ERROR_INTERFACE_ALREADY_REGISTERED);
            } else {
                registrationStatus = REGISTERING;
            }
        }

        return connectAsync(deviceClient)
                .flatMap(new Function<DigitalTwinClientResult, Flowable<DigitalTwinClientResult>>() {
                    @Override
                    public Flowable<DigitalTwinClientResult> apply(DigitalTwinClientResult result) {
                        return sendRegistrationMessageAsync(deviceCapabilityModelId, digitalTwinInterfaceClients);
                    }
                }).flatMap(new Function<DigitalTwinClientResult, Flowable<DigitalTwinClientResult>>() {
                    @Override
                    public Flowable<DigitalTwinClientResult> apply(DigitalTwinClientResult result) {
                        if (result == DIGITALTWIN_CLIENT_OK) {
                            return subscribeCommandAsync();
                        } else {
                            return Flowable.just(result);
                        }
                    }
                }).flatMap(new Function<DigitalTwinClientResult, Flowable<DigitalTwinClientResult>>() {
                    @Override
                    public Flowable<DigitalTwinClientResult> apply(DigitalTwinClientResult result) {
                        if (result == DIGITALTWIN_CLIENT_OK) {
                            return subscribeTwinAsync();
                        } else {
                            return Flowable.just(result);
                        }
                    }
                }).flatMap(new Function<DigitalTwinClientResult, Flowable<DigitalTwinClientResult>>() {
                    @Override
                    public Flowable<DigitalTwinClientResult> apply(DigitalTwinClientResult result) {
                        if (result == DIGITALTWIN_CLIENT_OK) {
                            return reportSdkInformationAsync();
                        } else {
                            return Flowable.just(result);
                        }
                    }
                }).map(new Function<DigitalTwinClientResult, DigitalTwinClientResult>() {
                    @Override
                    public DigitalTwinClientResult apply(DigitalTwinClientResult result) throws Throwable {
                        if (result == DIGITALTWIN_CLIENT_OK) {
                            return getTwinAsync();
                        } else {
                            return result;
                        }
                    }
                }).map(new Function<DigitalTwinClientResult, DigitalTwinClientResult>() {
                    @Override
                    public DigitalTwinClientResult apply(DigitalTwinClientResult result) {
                        if (result == DIGITALTWIN_CLIENT_OK) {
                            return notifyComponents(digitalTwinInterfaceClients);
                        } else {
                            return result;
                        }
                    }
                }).map(new Function<DigitalTwinClientResult, DigitalTwinClientResult>() {
                    @Override
                    public DigitalTwinClientResult apply(DigitalTwinClientResult result) {
                        return onRegistrationResult(result);
                    }
                }).doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        onRegistrationResult(DIGITALTWIN_CLIENT_ERROR);
                    }
                }).doOnCancel(new Action() {
                    @Override
                    public void run() {
                        onRegistrationResult(DIGITALTWIN_CLIENT_ERROR);
                    }
                });
    }

    private Flowable<DigitalTwinClientResult> connectAsync(final DeviceClient deviceClient) {
        return Flowable.fromSupplier(new Supplier<DigitalTwinClientResult>() {
            @Override
            public DigitalTwinClientResult get() throws Throwable {
                log.debug("Connecting device client...");
                deviceClient.open();
                log.debug("Device client connected.");
                return DIGITALTWIN_CLIENT_OK;
            }
        });
    }

    private Flowable<DigitalTwinClientResult> sendRegistrationMessageAsync(final String deviceCapabilityModelId, final List<? extends AbstractDigitalTwinInterfaceClient> components) {
        return Flowable.create(new FlowableOnSubscribe<DigitalTwinClientResult>() {
            @Override
            public void subscribe(FlowableEmitter<DigitalTwinClientResult> emitter) throws Throwable {
                ModelInformationBuilder modelInformationBuilder = ModelInformation.builder();
                modelInformationBuilder.dcmId(deviceCapabilityModelId);
                for (AbstractDigitalTwinInterfaceClient component : components) {
                    String interfaceInstanceName = component.getDigitalTwinInterfaceInstanceName();
                    String interfaceId = component.getDigitalTwinInterfaceId();
                    modelInformationBuilder.interfaceInstance(interfaceInstanceName, interfaceId);
                    digitalTwinInterfaceClients.put(interfaceInstanceName, component);
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
                log.debug("Sending registration message...");
                deviceClient.sendEventAsync(registerInterfacesMessage, createIotHubEventCallback(emitter), emitter);
            }
        }, BUFFER);
    }

    private Flowable<DigitalTwinClientResult> subscribeCommandAsync() {
        return Flowable.create(new FlowableOnSubscribe<DigitalTwinClientResult>() {
            @Override
            public void subscribe(FlowableEmitter<DigitalTwinClientResult> emitter) throws Throwable {
                log.debug("Subscribing command...");
                DeviceMethodCallback deviceMethodCallback = new DigitalTwinCommandDispatcher();
                deviceClient.subscribeToDeviceMethod(
                        deviceMethodCallback,
                        emitter,
                        createIotHubEventCallback(emitter),
                        emitter
                );
            }
        }, BUFFER);
    }

    private Flowable<DigitalTwinClientResult> subscribeTwinAsync() {
        return Flowable.create(new FlowableOnSubscribe<DigitalTwinClientResult>() {
            @Override
            public void subscribe(FlowableEmitter<DigitalTwinClientResult> emitter) throws Throwable {
                log.debug("Subscribing twin...");
                TwinPropertyCallBack twinPropertyCallBack = new DigitalTwinPropertyDispatcher();
                deviceClient.startDeviceTwin(
                        createIotHubEventCallback(emitter),
                        emitter,
                        twinPropertyCallBack,
                        emitter
                );
            }
        }, BUFFER);
    }

    private Flowable<DigitalTwinClientResult> reportSdkInformationAsync() {
        return reportPropertiesAsync(
                DIGITAL_TWIN_SDK_INFORMATION_INTERFACE_INSTANCE,
                DIGITAL_TWIN_SDK_INFORMATION_PROPERTIES
        );
    }

    private DigitalTwinClientResult getTwinAsync() throws Throwable {
        log.debug("Getting DeviceTwin...");
        deviceClient.getDeviceTwin();
        return DIGITALTWIN_CLIENT_OK;
    }

    private DigitalTwinClientResult notifyComponents(@NonNull List<? extends AbstractDigitalTwinInterfaceClient> digitalTwinInterfaceClients) {
        log.debug("Notifying interface instances...");
        for (AbstractDigitalTwinInterfaceClient digitalTwinInterfaceClient : digitalTwinInterfaceClients) {
            onDigitalTwinInterfaceClientRegistered(digitalTwinInterfaceClient);
        }
        log.debug("Notify interface instances succeed.");
        return DIGITALTWIN_CLIENT_OK;
    }

    private static boolean isSuccess(IotHubStatusCode statusCode) {
        return statusCode == OK || statusCode == OK_EMPTY;
    }

    private DigitalTwinClientResult onRegistrationResult(DigitalTwinClientResult result) {
        synchronized (lock) {
            if (result != DIGITALTWIN_CLIENT_OK) {
                log.debug("Registration failed.");
                digitalTwinInterfaceClients.clear();
                registrationStatus = UNREGISTERED;
            } else {
                log.debug("Registration succeed.");
                registrationStatus = REGISTERED;
            }
        }
        return result;
    }

    Flowable<DigitalTwinClientResult> sendTelemetryAsync(
            @NonNull final String digitalTwinInterfaceInstanceName,
            @NonNull final String telemetryName,
            @NonNull final String payload) {
        return Flowable.create(new FlowableOnSubscribe<DigitalTwinClientResult>() {
            @Override
            public void subscribe(FlowableEmitter<DigitalTwinClientResult> emitter) throws Throwable {
                log.debug("Sending TelemetryAsync...");
                SimpleEntry body = new SimpleEntry<>(telemetryName, new JsonRawValue(payload));
                Message message = new Message(serialize(body));
                message.setProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE, digitalTwinInterfaceInstanceName);
                message.setProperty(PROPERTY_MESSAGE_SCHEMA, telemetryName);
                IotHubEventCallback callback = createIotHubEventCallback(emitter);
                deviceClient.sendEventAsync(message, callback, callback);
                log.debug("SendTelemetryAsync succeed.");
            }
        }, BUFFER);
    }

    Flowable<DigitalTwinClientResult> reportPropertiesAsync(
            @NonNull final String digitalTwinInterfaceInstanceName,
            @NonNull final List<DigitalTwinReportProperty> digitalTwinReportProperties) {
        return Flowable.create(new FlowableOnSubscribe<DigitalTwinClientResult>() {
            @Override
            public void subscribe(FlowableEmitter<DigitalTwinClientResult> emitter) throws Throwable {
                log.debug("Reporting PropertiesAsync...");
                // TODO Known gap, SDK API with ambiguous Object
                Property property = serializeReportProperty(digitalTwinInterfaceInstanceName, digitalTwinReportProperties);
                deviceClient.sendReportedProperties(singleton(property));
                // TODO TODO Known gap, SDK API accepts no callback, there is no guarantee it's delivered
                log.debug("ReportPropertiesAsync succeed.");
                notifyEmitter(emitter, DIGITALTWIN_CLIENT_OK);
            }
        }, BUFFER);
    }

    Flowable<DigitalTwinClientResult> updateAsyncCommandStatusAsync(
            @NonNull final String digitalTwinInterfaceInstanceName,
            @NonNull final DigitalTwinAsyncCommandUpdate digitalTwinAsyncCommandUpdate) {
        return Flowable.create(new FlowableOnSubscribe<DigitalTwinClientResult>() {
            @Override
            public void subscribe(FlowableEmitter<DigitalTwinClientResult> emitter) throws Throwable {
                log.debug("Updating AsyncCommandStatus...");
                Message message = new Message(digitalTwinAsyncCommandUpdate.getPayload());
                message.setProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE, digitalTwinInterfaceInstanceName);
                message.setProperty(PROPERTY_COMMAND_NAME, digitalTwinAsyncCommandUpdate.getCommandName());
                message.setProperty(PROPERTY_REQUEST_ID, digitalTwinAsyncCommandUpdate.getRequestId());
                message.setProperty(PROPERTY_STATUS, String.valueOf(digitalTwinAsyncCommandUpdate.getStatusCode()));
                IotHubEventCallback asyncCommandCallback = createIotHubEventCallback(emitter);
                deviceClient.sendEventAsync(message, asyncCommandCallback, asyncCommandCallback);
                log.debug("UpdateAsyncCommandStatus succeed.");
            }
        }, BUFFER);
    }

    private void onDigitalTwinInterfaceClientRegistered(AbstractDigitalTwinInterfaceClient digitalTwinInterfaceClient) {
        digitalTwinInterfaceClient.setDigitalTwinDeviceClient(this);
        digitalTwinInterfaceClient.onRegistered();
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
                String interfaceInstanceName = componentMatcher.group(1);
                AbstractDigitalTwinInterfaceClient digitalTwinInterfaceClient = digitalTwinInterfaceClients.get(interfaceInstanceName);
                if (digitalTwinInterfaceClient == null) {
                    return new DeviceMethodData(INTERFACE_INSTANCE_NOT_FOUND_CODE, String.format(INTERFACE_INSTANCE_NOT_FOUND_MESSAGE_PATTERN, interfaceInstanceName));
                }
                DigitalTwinCommandRequestBuilder digitalTwinCommandRequestBuilder = DigitalTwinCommandRequest.builder()
                                                                                                             .commandName(componentMatcher.group(2));
                if (methodData instanceof byte[]) {
                    try {
                        DigitalTwinCommand digitalTwinCommand = deserialize((byte[]) methodData, DigitalTwinCommand.class);
                        CommandRequest commandRequest = digitalTwinCommand.getCommandRequest();
                        DigitalTwinCommandRequest digitalTwinCommandRequest = digitalTwinCommandRequestBuilder.requestId(commandRequest.getRequestId())
                                                                                                              .payload(commandRequest.getValue().getRawJson())
                                                                                                              .build();
                        DigitalTwinCommandResponse digitalTwinCommandResponse = digitalTwinInterfaceClient.onCommandReceived(digitalTwinCommandRequest);
                        return new DeviceMethodData(digitalTwinCommandResponse.getStatus(), digitalTwinCommandResponse.getPayload());
                    } catch (Exception e) {
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
                        digitalTwinInterfaceClient.onPropertyUpdate(propertyUpdateBuilder.build());
                    }
                } catch (Exception e) {
                    log.debug("Property ignored: value is not JSON.", e);
                }
            } else {
                log.debug("Ignored none digital twin property[{}].", name);
            }
        }
    }

}
