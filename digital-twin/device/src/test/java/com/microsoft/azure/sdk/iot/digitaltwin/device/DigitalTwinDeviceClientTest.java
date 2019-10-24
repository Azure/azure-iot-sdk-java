// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.device;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinAsyncCommandUpdate;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandRequest;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinPropertyUpdate;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.ERROR;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinInterfaceClient.STATUS_CODE_COMPLETED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinInterfaceClient.STATUS_CODE_NOT_IMPLEMENTED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_INTERFACE_ALREADY_REGISTERED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_REGISTRATION_PENDING;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient.PROPERTY_COMMAND_NAME;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient.PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient.PROPERTY_MESSAGE_SCHEMA;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient.PROPERTY_REQUEST_ID;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient.PROPERTY_STATUS;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.RegistrationStatus.REGISTERED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.RegistrationStatus.REGISTERING;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.RegistrationStatus.UNREGISTERED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.TwinPropertyJsonSerializer.DIGITAL_TWIN_INTERFACE_INSTANCE_NAME_PREFIX;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(PowerMockRunner.class)
@PrepareForTest({DeviceClient.class})
public class DigitalTwinDeviceClientTest {
    private static final String DIGITAL_TWIN_INTERFACE_INSTANCE_1 = "DIGITAL_TWIN_INTERFACE_INSTANCE_1";
    private static final String DIGITAL_TWIN_INTERFACE_INSTANCE_2 = "DIGITAL_TWIN_INTERFACE_INSTANCE_2";
    private static final String DIGITAL_TWIN_INTERFACE_ID_1 = "DIGITAL_TWIN_INTERFACE_ID_1";
    private static final String DIGITAL_TWIN_INTERFACE_ID_2 = "DIGITAL_TWIN_INTERFACE_ID_2";
    private static final String DIGITAL_TWIN_DCM_ID = "DIGITAL_TWIN_DCM_ID";
    private static final String TELEMETRY_NAME = "TELEMETRY_NAME";
    private static final String TEST_PAYLOAD = "{\"number\":123,\"str\":\"abc\",\"boolean\":true}";
    private static final String COMMAND_NAME = "DIGITAL_TWIN_COMMAND_NAME";
    private static final String REQUEST_ID = "DIGITAL_TWIN_REQUEST_ID";
    private static final byte[] COMMAND_PAYLOAD = String.format("{\"commandRequest\":{\"requestId\":\"%s\", \"value\":%s}}", REQUEST_ID, TEST_PAYLOAD).getBytes(UTF_8);
    private static final String DIGITAL_TWIN_INTERFACE_INSTANCE_UNKNOWN = "UNKNOWN_INTERFACE_INSTANCE_NAME";
    private static final int DESIRED_VERSION = 1234;
    private static final int DELAY_IN_MS = 100;

    private DigitalTwinDeviceClient testee;
    private List<? extends AbstractDigitalTwinInterfaceClient> digitalTwinInterfaceClients;
    @Mock
    private DeviceClient deviceClient;
    @Mock
    private AbstractDigitalTwinInterfaceClient digitalTwinInterfaceClient1;
    @Mock
    private AbstractDigitalTwinInterfaceClient digitalTwinInterfaceClient2;
    @Mock
    private Consumer<Throwable> errorConsumer;
    @Mock
    private Object context;

    @Before
    public void setUp() throws IOException {
        testee = new DigitalTwinDeviceClient(deviceClient);
        digitalTwinInterfaceClients = asList(digitalTwinInterfaceClient1, digitalTwinInterfaceClient2);
        when(digitalTwinInterfaceClient1.getDigitalTwinInterfaceInstanceName()).thenReturn(DIGITAL_TWIN_INTERFACE_INSTANCE_1);
        when(digitalTwinInterfaceClient2.getDigitalTwinInterfaceInstanceName()).thenReturn(DIGITAL_TWIN_INTERFACE_INSTANCE_2);
        when(digitalTwinInterfaceClient1.getDigitalTwinInterfaceId()).thenReturn(DIGITAL_TWIN_INTERFACE_ID_1);
        when(digitalTwinInterfaceClient2.getDigitalTwinInterfaceId()).thenReturn(DIGITAL_TWIN_INTERFACE_ID_2);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                final IotHubEventCallback iotHubEventCallback = invocation.getArgumentAt(1, IotHubEventCallback.class);
                final Object context = invocation.getArgumentAt(2, Object.class);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(DELAY_IN_MS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        iotHubEventCallback.execute(OK, context);
                    }
                }).start();
                return null;
            }
        }).when(deviceClient)
          .sendEventAsync(any(Message.class), any(IotHubEventCallback.class), any());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                final IotHubEventCallback iotHubEventCallback = invocation.getArgumentAt(2, IotHubEventCallback.class);
                final Object context = invocation.getArgumentAt(3, Object.class);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(DELAY_IN_MS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        iotHubEventCallback.execute(OK, context);
                    }
                }).start();
                return null;
            }
        }).when(deviceClient)
          .subscribeToDeviceMethod(any(DeviceMethodCallback.class), any(), any(IotHubEventCallback.class), any());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                final IotHubEventCallback iotHubEventCallback = invocation.getArgumentAt(0, IotHubEventCallback.class);
                final Object context = invocation.getArgumentAt(1, Object.class);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(DELAY_IN_MS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        iotHubEventCallback.execute(OK, context);
                    }
                }).start();
                return null;
            }
        }).when(deviceClient)
          .startDeviceTwin(any(IotHubEventCallback.class), any(), any(TwinPropertyCallBack.class), any());
    }

    @Test
    public void registerInterfacesTest() {
        Single<DigitalTwinClientResult> single = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERING);
        DigitalTwinClientResult digitalTwinClientResult = single.blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(digitalTwinInterfaceClient1).setDigitalTwinDeviceClient(eq(testee));
        verify(digitalTwinInterfaceClient2).setDigitalTwinDeviceClient(eq(testee));
        verify(digitalTwinInterfaceClient1).onRegistered();
        verify(digitalTwinInterfaceClient2).onRegistered();
    }

    @Test
    public void registerInterfaceTwicePendingTest() {
        Single<DigitalTwinClientResult> single = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERING);
        DigitalTwinClientResult digitalTwinClientResult2 = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients).blockingGet();
        assertThat(digitalTwinClientResult2).isEqualTo(DIGITALTWIN_CLIENT_ERROR_REGISTRATION_PENDING);

        DigitalTwinClientResult digitalTwinClientResult = single.blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
    }

    @Test
    public void registerInterfaceTwiceAfterTest() {
        Single<DigitalTwinClientResult> single = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERING);
        DigitalTwinClientResult digitalTwinClientResult = single.blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);

        DigitalTwinClientResult digitalTwinClientResult2 = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients).blockingGet();
        assertThat(digitalTwinClientResult2).isEqualTo(DIGITALTWIN_CLIENT_ERROR_INTERFACE_ALREADY_REGISTERED);
    }

    @Test
    public void registerInterfaceOpenThrowExceptionTest() throws Throwable {
        final Exception exception = new Exception("OpenThrowException");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw exception;
            }
        }).when(deviceClient)
          .open();

        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients)
                                                                    .doOnError(errorConsumer)
                                                                    .blockingGet();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Open throw exception", e);
        }

        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(errorConsumer).accept(eq(exception));
        verify(digitalTwinInterfaceClient1, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient2, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient1, never()).onRegistered();
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
        verify(deviceClient, never()).sendEventAsync(any(Message.class), any(IotHubEventCallback.class), any());
    }

    @Test
    public void registerInterfaceSendRegistrationMessageFailedTest() throws IOException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                final IotHubEventCallback iotHubEventCallback = invocation.getArgumentAt(1, IotHubEventCallback.class);
                final Object context = invocation.getArgumentAt(2, Object.class);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(DELAY_IN_MS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        iotHubEventCallback.execute(ERROR, context);
                    }
                }).start();
                return null;
            }
        }).when(deviceClient)
          .sendEventAsync(any(Message.class), any(IotHubEventCallback.class), any());

        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient1, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient2, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient1, never()).onRegistered();
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
        verify(deviceClient, never()).subscribeToDeviceMethod(any(DeviceMethodCallback.class), any(), any(IotHubEventCallback.class), any());
    }

    @Test
    public void registerInterfaceSendRegistrationMessageThrowExceptionTest() throws Throwable {
        final Exception exception = new Exception("SendRegistrationMessageThrowException");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw exception;
            }
        }).when(deviceClient)
          .sendEventAsync(any(Message.class), any(IotHubEventCallback.class), any());

        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients)
                                                                    .doOnError(errorConsumer)
                                                                    .blockingGet();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Send registration message throw exception", e);
        }

        verify(errorConsumer).accept(eq(exception));
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient1, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient2, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient1, never()).onRegistered();
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
        verify(deviceClient, never()).subscribeToDeviceMethod(any(DeviceMethodCallback.class), any(), any(IotHubEventCallback.class), any());
    }

    @Test
    public void registerInterfaceCommandSubscriptionFailedTest() throws IOException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                final IotHubEventCallback iotHubEventCallback = invocation.getArgumentAt(2, IotHubEventCallback.class);
                final Object context = invocation.getArgumentAt(3, Object.class);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(DELAY_IN_MS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        iotHubEventCallback.execute(ERROR, context);
                    }
                }).start();
                return null;
            }
        }).when(deviceClient)
          .subscribeToDeviceMethod(any(DeviceMethodCallback.class), any(), any(IotHubEventCallback.class), any());
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient1, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient2, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient1, never()).onRegistered();
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
        verify(deviceClient, never()).startDeviceTwin(any(IotHubEventCallback.class), any(), any(TwinPropertyCallBack.class), any());
    }

    @Test
    public void registerInterfaceCommandSubscriptionThrowExceptionTest() throws Throwable {
        final Exception exception = new Exception("CommandSubscriptionThrowException");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw exception;
            }
        }).when(deviceClient)
          .subscribeToDeviceMethod(any(DeviceMethodCallback.class), any(), any(IotHubEventCallback.class), any());

        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients)
                                                                    .doOnError(errorConsumer)
                                                                    .blockingGet();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Subscribe command throw exception.", e);
        }

        verify(errorConsumer).accept(eq(exception));
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient1, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient2, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient1, never()).onRegistered();
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
        verify(deviceClient, never()).startDeviceTwin(any(IotHubEventCallback.class), any(), any(TwinPropertyCallBack.class), any());
    }

    @Test
    public void registerInterfaceTwinSubscriptionFailedTest() throws IOException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                final IotHubEventCallback iotHubEventCallback = invocation.getArgumentAt(0, IotHubEventCallback.class);
                final Object context = invocation.getArgumentAt(1, Object.class);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(DELAY_IN_MS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        iotHubEventCallback.execute(ERROR, context);
                    }
                }).start();
                return null;
            }
        }).when(deviceClient)
          .startDeviceTwin(any(IotHubEventCallback.class), any(), any(TwinPropertyCallBack.class), any());
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient1, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient2, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient1, never()).onRegistered();
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
        verify(deviceClient, never()).sendReportedProperties(anySetOf(Property.class));
    }

    @Test
    public void registerInterfaceTwinSubscriptionThrowExceptionTest() throws Throwable {
        final Exception exception = new Exception("TwinSubscriptionThrowException");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw exception;
            }
        }).when(deviceClient)
          .startDeviceTwin(any(IotHubEventCallback.class), any(), any(TwinPropertyCallBack.class), any());

        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients)
                                                                    .doOnError(errorConsumer)
                                                                    .blockingGet();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Subscribe Twin throw exception.", e);
        }

        verify(errorConsumer).accept(eq(exception));
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient1, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient2, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient1, never()).onRegistered();
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
        verify(deviceClient, never()).sendReportedProperties(anySetOf(Property.class));
    }

    @Test
    public void registerInterfaceReportSdkInformationThrowExceptionTest() throws Throwable {
        final Exception exception = new Exception("ReportSdkInformationThrowException");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw exception;
            }
        }).when(deviceClient)
          .sendReportedProperties(anySetOf(Property.class));

        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients)
                                                                    .doOnError(errorConsumer)
                                                                    .blockingGet();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Report Sdk information throw exception.", e);
        }

        verify(errorConsumer).accept(eq(exception));
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient1, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient2, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient1, never()).onRegistered();
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
        verify(deviceClient, never()).getDeviceTwin();
    }

    @Test
    public void registerInterfaceGetTwinThrowExceptionTest() throws Throwable {
        final Exception exception = new Exception("GetTwinThrowException");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw exception;
            }
        }).when(deviceClient)
          .getDeviceTwin();

        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients)
                                                                    .doOnError(errorConsumer)
                                                                    .blockingGet();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Get twin throw exception.", e);
        }

        verify(errorConsumer).accept(eq(exception));
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient1, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient2, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient1, never()).onRegistered();
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
    }

    @Test
    public void registerInterfaceOnRegistered1ThrowExceptionTest() throws Throwable {
        final Exception exception = new Exception("InterfaceClient1OnRegisteredThrowExceptionThrowException");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw exception;
            }
        }).when(digitalTwinInterfaceClient1)
          .onRegistered();

        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients)
                                                                    .doOnError(errorConsumer)
                                                                    .blockingGet();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Interface client 1 throw exception when on registered called.", e);
        }

        verify(errorConsumer).accept(eq(exception));
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
    }

    @Test
    public void registerInterfaceOnRegistered2ThrowExceptionTest() throws Throwable {
        final Exception exception = new Exception("InterfaceClient2OnRegisteredThrowExceptionThrowException");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw exception;
            }
        }).when(digitalTwinInterfaceClient2)
          .onRegistered();

        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients)
                                                                    .doOnError(errorConsumer)
                                                                    .blockingGet();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Interface client 2 throw exception when on registered called.", e);
        }

        verify(errorConsumer).accept(eq(exception));
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient1).onRegistered();
    }

    @Test
    public void sendTelemetryAsyncTest() {
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.sendTelemetryAsync(DIGITAL_TWIN_INTERFACE_INSTANCE_1, TELEMETRY_NAME, TEST_PAYLOAD).blockingSingle();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).sendEventAsync(messageCaptor.capture(), any(IotHubEventCallback.class), any());
        Message message = messageCaptor.getValue();
        assertThat(message.getProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE)).isEqualTo(DIGITAL_TWIN_INTERFACE_INSTANCE_1);
        assertThat(message.getProperty(PROPERTY_MESSAGE_SCHEMA)).isEqualTo(TELEMETRY_NAME);
        String expectedPayload = String.format("{\"%s\":%s}", TELEMETRY_NAME, TEST_PAYLOAD);
        assertThat(new String(message.getBytes(), UTF_8)).isEqualTo(expectedPayload);
    }

    @Test
    public void sendTelemetryAsyncFailedTest() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                final IotHubEventCallback iotHubEventCallback = invocation.getArgumentAt(1, IotHubEventCallback.class);
                final Object context = invocation.getArgumentAt(2, Object.class);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        iotHubEventCallback.execute(ERROR, context);
                    }
                }).start();
                return null;
            }
        }).when(deviceClient)
          .sendEventAsync(any(Message.class), any(IotHubEventCallback.class), any());
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.sendTelemetryAsync(DIGITAL_TWIN_INTERFACE_INSTANCE_1, TELEMETRY_NAME, TEST_PAYLOAD).blockingSingle();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
        verify(deviceClient).sendEventAsync(messageCaptor.capture(), any(IotHubEventCallback.class), any());
        Message message = messageCaptor.getValue();
        assertThat(message.getProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE)).isEqualTo(DIGITAL_TWIN_INTERFACE_INSTANCE_1);
        assertThat(message.getProperty(PROPERTY_MESSAGE_SCHEMA)).isEqualTo(TELEMETRY_NAME);
        String expectedPayload = String.format("{\"%s\":%s}", TELEMETRY_NAME, TEST_PAYLOAD);
        assertThat(new String(message.getBytes(), UTF_8)).isEqualTo(expectedPayload);
    }

    @Test
    public void sendTelemetryAsyncThrowExceptionTest() throws Throwable {
        final Exception exception = new Exception("SendEventAsyncThrowException");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw exception;
            }
        }).when(deviceClient)
          .sendEventAsync(any(Message.class), any(IotHubEventCallback.class), any());

        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.sendTelemetryAsync(DIGITAL_TWIN_INTERFACE_INSTANCE_1, TELEMETRY_NAME, TEST_PAYLOAD)
                                                                    .doOnError(errorConsumer)
                                                                    .blockingSingle();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Send event throw exception.", e);
        }
        verify(errorConsumer).accept(eq(exception));
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(deviceClient).sendEventAsync(messageCaptor.capture(), any(IotHubEventCallback.class), any());
        Message message = messageCaptor.getValue();
        assertThat(message.getProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE)).isEqualTo(DIGITAL_TWIN_INTERFACE_INSTANCE_1);
        assertThat(message.getProperty(PROPERTY_MESSAGE_SCHEMA)).isEqualTo(TELEMETRY_NAME);
        String expectedPayload = String.format("{\"%s\":%s}", TELEMETRY_NAME, TEST_PAYLOAD);
        assertThat(new String(message.getBytes(), UTF_8)).isEqualTo(expectedPayload);
    }

    @Test
    public void updateAsyncCommandStatusAsyncTest() {
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        DigitalTwinAsyncCommandUpdate asyncCommandUpdate = DigitalTwinAsyncCommandUpdate.builder()
                                                                                        .commandName(COMMAND_NAME)
                                                                                        .requestId(REQUEST_ID)
                                                                                        .statusCode(STATUS_CODE_COMPLETED)
                                                                                        .payload(TEST_PAYLOAD)
                                                                                        .build();
        DigitalTwinClientResult digitalTwinClientResult = testee.updateAsyncCommandStatusAsync(DIGITAL_TWIN_INTERFACE_INSTANCE_1, asyncCommandUpdate).blockingSingle();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).sendEventAsync(messageCaptor.capture(), any(IotHubEventCallback.class), any());
        Message message = messageCaptor.getValue();
        assertThat(message.getProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE)).isEqualTo(DIGITAL_TWIN_INTERFACE_INSTANCE_1);
        assertThat(message.getProperty(PROPERTY_COMMAND_NAME)).isEqualTo(COMMAND_NAME);
        assertThat(message.getProperty(PROPERTY_REQUEST_ID)).isEqualTo(REQUEST_ID);
        assertThat(message.getProperty(PROPERTY_STATUS)).isEqualTo(String.valueOf(STATUS_CODE_COMPLETED));
        assertThat(new String(message.getBytes(), UTF_8)).isEqualTo(TEST_PAYLOAD);
    }

    @Test
    public void updateAsyncCommandStatusAsyncFailedTest() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                final IotHubEventCallback iotHubEventCallback = invocation.getArgumentAt(1, IotHubEventCallback.class);
                final Object context = invocation.getArgumentAt(2, Object.class);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        iotHubEventCallback.execute(ERROR, context);
                    }
                }).start();
                return null;
            }
        }).when(deviceClient)
          .sendEventAsync(any(Message.class), any(IotHubEventCallback.class), any());
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        DigitalTwinAsyncCommandUpdate asyncCommandUpdate = DigitalTwinAsyncCommandUpdate.builder()
                                                                                        .commandName(COMMAND_NAME)
                                                                                        .requestId(REQUEST_ID)
                                                                                        .statusCode(STATUS_CODE_COMPLETED)
                                                                                        .payload(TEST_PAYLOAD)
                                                                                        .build();
        DigitalTwinClientResult digitalTwinClientResult = testee.updateAsyncCommandStatusAsync(DIGITAL_TWIN_INTERFACE_INSTANCE_1, asyncCommandUpdate).blockingSingle();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
        verify(deviceClient).sendEventAsync(messageCaptor.capture(), any(IotHubEventCallback.class), any());
        Message message = messageCaptor.getValue();
        assertThat(message.getProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE)).isEqualTo(DIGITAL_TWIN_INTERFACE_INSTANCE_1);
        assertThat(message.getProperty(PROPERTY_COMMAND_NAME)).isEqualTo(COMMAND_NAME);
        assertThat(message.getProperty(PROPERTY_REQUEST_ID)).isEqualTo(REQUEST_ID);
        assertThat(message.getProperty(PROPERTY_STATUS)).isEqualTo(String.valueOf(STATUS_CODE_COMPLETED));
        assertThat(new String(message.getBytes(), UTF_8)).isEqualTo(TEST_PAYLOAD);
    }

    @Test
    public void updateAsyncCommandStatusAsyncThrowExceptionTest() throws Throwable {
        final Exception exception = new Exception("UpdateAsyncCommandStatusAsyncThrowException");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw exception;
            }
        }).when(deviceClient)
          .sendEventAsync(any(Message.class), any(IotHubEventCallback.class), any());

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        try {
            DigitalTwinAsyncCommandUpdate asyncCommandUpdate = DigitalTwinAsyncCommandUpdate.builder()
                                                                                            .commandName(COMMAND_NAME)
                                                                                            .requestId(REQUEST_ID)
                                                                                            .statusCode(STATUS_CODE_COMPLETED)
                                                                                            .payload(TEST_PAYLOAD)
                                                                                            .build();
            DigitalTwinClientResult digitalTwinClientResult = testee.updateAsyncCommandStatusAsync(DIGITAL_TWIN_INTERFACE_INSTANCE_1, asyncCommandUpdate)
                                                                    .doOnError(errorConsumer)
                                                                    .blockingSingle();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Update async command status throw exception when on registered called.", e);
        }
        verify(errorConsumer).accept(eq(exception));

        verify(deviceClient).sendEventAsync(messageCaptor.capture(), any(IotHubEventCallback.class), any());
        Message message = messageCaptor.getValue();
        assertThat(message.getProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE)).isEqualTo(DIGITAL_TWIN_INTERFACE_INSTANCE_1);
        assertThat(message.getProperty(PROPERTY_COMMAND_NAME)).isEqualTo(COMMAND_NAME);
        assertThat(message.getProperty(PROPERTY_REQUEST_ID)).isEqualTo(REQUEST_ID);
        assertThat(message.getProperty(PROPERTY_STATUS)).isEqualTo(String.valueOf(STATUS_CODE_COMPLETED));
        assertThat(new String(message.getBytes(), UTF_8)).isEqualTo(TEST_PAYLOAD);
    }

    @Test
    public void commandDispatcherTest() throws Exception {
        ArgumentCaptor<DeviceMethodCallback> methodCallbackCaptor = ArgumentCaptor.forClass(DeviceMethodCallback.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(deviceClient).subscribeToDeviceMethod(methodCallbackCaptor.capture(), any(), any(IotHubEventCallback.class), any());
        DeviceMethodCallback methodCallback = methodCallbackCaptor.getValue();
        methodCallback.call(
                String.format("%s%s*%s", DIGITAL_TWIN_INTERFACE_INSTANCE_NAME_PREFIX, DIGITAL_TWIN_INTERFACE_INSTANCE_1, COMMAND_NAME),
                COMMAND_PAYLOAD,
                context
        );
        ArgumentCaptor<DigitalTwinCommandRequest> commandRequestCaptor = ArgumentCaptor.forClass(DigitalTwinCommandRequest.class);
        verify(digitalTwinInterfaceClient1).onCommandReceived(commandRequestCaptor.capture());
        DigitalTwinCommandRequest commandRequest = commandRequestCaptor.getValue();
        assertThat(commandRequest.getCommandName()).isEqualTo(COMMAND_NAME);
        assertThat(commandRequest.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(commandRequest.getPayload()).isEqualTo(TEST_PAYLOAD);
    }

    @Test
    public void commandDispatcherWithoutInterfaceInstanceNameTest() throws IOException {
        ArgumentCaptor<DeviceMethodCallback> methodCallbackCaptor = ArgumentCaptor.forClass(DeviceMethodCallback.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(deviceClient).subscribeToDeviceMethod(methodCallbackCaptor.capture(), any(), any(IotHubEventCallback.class), any());
        DeviceMethodCallback methodCallback = methodCallbackCaptor.getValue();
        DeviceMethodData methodData = methodCallback.call(
                COMMAND_NAME,
                COMMAND_PAYLOAD,
                context
        );
        verify(digitalTwinInterfaceClient1, never()).onCommandReceived(any(DigitalTwinCommandRequest.class));
        verify(digitalTwinInterfaceClient2, never()).onCommandReceived(any(DigitalTwinCommandRequest.class));
        assertThat(methodData.getStatus()).isEqualTo(STATUS_CODE_NOT_IMPLEMENTED);
    }

    @Test
    public void commandDispatcherWithUnknownInterfaceInstanceNameTest() throws IOException {
        ArgumentCaptor<DeviceMethodCallback> methodCallbackCaptor = ArgumentCaptor.forClass(DeviceMethodCallback.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(deviceClient).subscribeToDeviceMethod(methodCallbackCaptor.capture(), any(), any(IotHubEventCallback.class), any());
        DeviceMethodCallback methodCallback = methodCallbackCaptor.getValue();
        DeviceMethodData methodData = methodCallback.call(
                String.format("%s%s*%s", DIGITAL_TWIN_INTERFACE_INSTANCE_NAME_PREFIX, DIGITAL_TWIN_INTERFACE_INSTANCE_UNKNOWN, COMMAND_NAME),
                COMMAND_PAYLOAD,
                context
        );
        verify(digitalTwinInterfaceClient1, never()).onCommandReceived(any(DigitalTwinCommandRequest.class));
        verify(digitalTwinInterfaceClient2, never()).onCommandReceived(any(DigitalTwinCommandRequest.class));
        assertThat(methodData.getStatus()).isEqualTo(STATUS_CODE_NOT_IMPLEMENTED);
    }

    @Test
    public void propertyDispatcherReportedTest() throws IOException {
        ArgumentCaptor<TwinPropertyCallBack> twinPropertyCallbackCaptor = ArgumentCaptor.forClass(TwinPropertyCallBack.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), any(), twinPropertyCallbackCaptor.capture(), any());
        Property property = mock(Property.class);
        JsonElement propertyValue = new JsonParser().parse(TEST_PAYLOAD);
        when(property.getKey()).thenReturn(String.format("%s%s", DIGITAL_TWIN_INTERFACE_INSTANCE_NAME_PREFIX, DIGITAL_TWIN_INTERFACE_INSTANCE_1));
        when(property.getIsReported()).thenReturn(true);
        when(property.getValue()).thenReturn(propertyValue);
        TwinPropertyCallBack twinPropertyCallBack = twinPropertyCallbackCaptor.getValue();
        twinPropertyCallBack.TwinPropertyCallBack(property, context);
        ArgumentCaptor<DigitalTwinPropertyUpdate> propertyUpdateCaptor = ArgumentCaptor.forClass(DigitalTwinPropertyUpdate.class);
        verify(digitalTwinInterfaceClient1, atLeastOnce()).onPropertyUpdate(propertyUpdateCaptor.capture());
        List<DigitalTwinPropertyUpdate> propertyUpdates = propertyUpdateCaptor.getAllValues();
        Map<String, String> actualProperties = new HashMap<>();
        for (DigitalTwinPropertyUpdate propertyUpdate : propertyUpdates) {
            assertThat(propertyUpdate.getPropertyDesired()).isNull();
            assertThat(propertyUpdate.getDesiredVersion()).isNull();
            actualProperties.put(propertyUpdate.getPropertyName(), propertyUpdate.getPropertyReported());
        }
        Map<String, String> expectedProperties = new HashMap<>();
        Set<Entry<String, JsonElement>> entries = propertyValue.getAsJsonObject().entrySet();
        for (Entry<String, JsonElement> entry : entries) {
            expectedProperties.put(entry.getKey(), entry.getValue().toString());
        }
        assertThat(actualProperties).containsAllEntriesOf(expectedProperties);
    }

    @Test
    public void propertyDispatcherDesiredTest() throws IOException {
        ArgumentCaptor<TwinPropertyCallBack> twinPropertyCallbackCaptor = ArgumentCaptor.forClass(TwinPropertyCallBack.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), any(), twinPropertyCallbackCaptor.capture(), any());
        Property property = mock(Property.class);
        JsonElement propertyValue = new JsonParser().parse(TEST_PAYLOAD);
        when(property.getKey()).thenReturn(String.format("%s%s", DIGITAL_TWIN_INTERFACE_INSTANCE_NAME_PREFIX, DIGITAL_TWIN_INTERFACE_INSTANCE_1));
        when(property.getIsReported()).thenReturn(false);
        when(property.getValue()).thenReturn(propertyValue);
        when(property.getVersion()).thenReturn(DESIRED_VERSION);
        TwinPropertyCallBack twinPropertyCallBack = twinPropertyCallbackCaptor.getValue();
        twinPropertyCallBack.TwinPropertyCallBack(property, context);
        ArgumentCaptor<DigitalTwinPropertyUpdate> propertyUpdateCaptor = ArgumentCaptor.forClass(DigitalTwinPropertyUpdate.class);
        verify(digitalTwinInterfaceClient1, atLeastOnce()).onPropertyUpdate(propertyUpdateCaptor.capture());
        List<DigitalTwinPropertyUpdate> propertyUpdates = propertyUpdateCaptor.getAllValues();
        Map<String, String> actualProperties = new HashMap<>();
        for (DigitalTwinPropertyUpdate propertyUpdate : propertyUpdates) {
            assertThat(propertyUpdate.getPropertyReported()).isNull();
            assertThat(propertyUpdate.getDesiredVersion()).isEqualTo(DESIRED_VERSION);
            actualProperties.put(propertyUpdate.getPropertyName(), propertyUpdate.getPropertyDesired());
        }
        Map<String, String> expectedProperties = new HashMap<>();
        Set<Entry<String, JsonElement>> entries = propertyValue.getAsJsonObject().entrySet();
        for (Entry<String, JsonElement> entry : entries) {
            expectedProperties.put(entry.getKey(), entry.getValue().toString());
        }
        assertThat(actualProperties).containsAllEntriesOf(expectedProperties);
    }

    @Test
    public void propertyDispatcherWithoutInterfaceInstanceNameTest() throws IOException {
        ArgumentCaptor<TwinPropertyCallBack> twinPropertyCallbackCaptor = ArgumentCaptor.forClass(TwinPropertyCallBack.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), any(), twinPropertyCallbackCaptor.capture(), any());
        Property property = mock(Property.class);
        JsonElement propertyValue = new JsonParser().parse(TEST_PAYLOAD);
        when(property.getKey()).thenReturn(DIGITAL_TWIN_INTERFACE_INSTANCE_1);
        when(property.getValue()).thenReturn(propertyValue);
        TwinPropertyCallBack twinPropertyCallBack = twinPropertyCallbackCaptor.getValue();
        twinPropertyCallBack.TwinPropertyCallBack(property, context);
        verify(digitalTwinInterfaceClient1, never()).onPropertyUpdate(any(DigitalTwinPropertyUpdate.class));
        verify(digitalTwinInterfaceClient2, never()).onPropertyUpdate(any(DigitalTwinPropertyUpdate.class));
    }

    @Test
    public void propertyDispatcherNonObjectTest() throws IOException {
        ArgumentCaptor<TwinPropertyCallBack> twinPropertyCallbackCaptor = ArgumentCaptor.forClass(TwinPropertyCallBack.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), any(), twinPropertyCallbackCaptor.capture(), any());
        Property property = mock(Property.class);
        JsonElement propertyValue = new JsonPrimitive(true);
        when(property.getKey()).thenReturn(String.format("%s%s", DIGITAL_TWIN_INTERFACE_INSTANCE_NAME_PREFIX, DIGITAL_TWIN_INTERFACE_INSTANCE_1));
        when(property.getValue()).thenReturn(propertyValue);
        TwinPropertyCallBack twinPropertyCallBack = twinPropertyCallbackCaptor.getValue();
        twinPropertyCallBack.TwinPropertyCallBack(property, context);
        verify(digitalTwinInterfaceClient1, never()).onPropertyUpdate(any(DigitalTwinPropertyUpdate.class));
        verify(digitalTwinInterfaceClient2, never()).onPropertyUpdate(any(DigitalTwinPropertyUpdate.class));
    }

    @Test
    public void propertyDispatcherNonJsonTest() throws IOException {
        ArgumentCaptor<TwinPropertyCallBack> twinPropertyCallbackCaptor = ArgumentCaptor.forClass(TwinPropertyCallBack.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), any(), twinPropertyCallbackCaptor.capture(), any());
        Property property = mock(Property.class);
        when(property.getKey()).thenReturn(String.format("%s%s", DIGITAL_TWIN_INTERFACE_INSTANCE_NAME_PREFIX, DIGITAL_TWIN_INTERFACE_INSTANCE_1));
        when(property.getValue()).thenReturn(REQUEST_ID);
        TwinPropertyCallBack twinPropertyCallBack = twinPropertyCallbackCaptor.getValue();
        twinPropertyCallBack.TwinPropertyCallBack(property, context);
        verify(digitalTwinInterfaceClient1, never()).onPropertyUpdate(any(DigitalTwinPropertyUpdate.class));
        verify(digitalTwinInterfaceClient2, never()).onPropertyUpdate(any(DigitalTwinPropertyUpdate.class));
    }

    @Test
    public void propertyDispatcherWithUnknownInterfaceInstanceNameTest() throws IOException {
        ArgumentCaptor<TwinPropertyCallBack> twinPropertyCallbackCaptor = ArgumentCaptor.forClass(TwinPropertyCallBack.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(DIGITAL_TWIN_DCM_ID, digitalTwinInterfaceClients).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), any(), twinPropertyCallbackCaptor.capture(), any());
        Property property = mock(Property.class);
        JsonElement propertyValue = new JsonParser().parse(TEST_PAYLOAD);
        when(property.getKey()).thenReturn(String.format("%s%s", DIGITAL_TWIN_INTERFACE_INSTANCE_NAME_PREFIX, DIGITAL_TWIN_INTERFACE_INSTANCE_UNKNOWN));
        when(property.getValue()).thenReturn(propertyValue);
        TwinPropertyCallBack twinPropertyCallBack = twinPropertyCallbackCaptor.getValue();
        twinPropertyCallBack.TwinPropertyCallBack(property, context);
        verify(digitalTwinInterfaceClient1, never()).onPropertyUpdate(any(DigitalTwinPropertyUpdate.class));
        verify(digitalTwinInterfaceClient2, never()).onPropertyUpdate(any(DigitalTwinPropertyUpdate.class));
    }
}
