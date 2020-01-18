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
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinReportProperty;
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
import static com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinComponent.STATUS_CODE_COMPLETED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinComponent.STATUS_CODE_NOT_IMPLEMENTED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_COMPONENTS_ALREADY_BOUND;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_COMPONENTS_NOT_BOUND;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient.PROPERTY_COMMAND_NAME;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient.PROPERTY_DIGITAL_TWIN_COMPONENT;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient.PROPERTY_MESSAGE_SCHEMA;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient.PROPERTY_REQUEST_ID;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient.PROPERTY_STATUS;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.TwinPropertyJsonSerializer.DIGITAL_TWIN_COMPONENT_NAME_PREFIX;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(PowerMockRunner.class)
@PrepareForTest({DeviceClient.class})
public class DigitalTwinDeviceClientTest {
    private static final String DIGITAL_TWIN_COMPONENT_NAME_1 = "DIGITAL_TWIN_COMPONENT_NAME_1";
    private static final String DIGITAL_TWIN_COMPONENT_NAME_2 = "DIGITAL_TWIN_COMPONENT_NAME_2";
    private static final String DIGITAL_TWIN_INTERFACE_ID_1 = "DIGITAL_TWIN_INTERFACE_ID_1";
    private static final String DIGITAL_TWIN_INTERFACE_ID_2 = "DIGITAL_TWIN_INTERFACE_ID_2";
    private static final String DIGITAL_TWIN_DCM_ID = "DIGITAL_TWIN_DCM_ID";
    private static final String TELEMETRY_NAME = "TELEMETRY_NAME";
    private static final String TEST_PAYLOAD = "{\"number\":123,\"str\":\"abc\",\"boolean\":true}";
    private static final String COMMAND_NAME = "DIGITAL_TWIN_COMMAND_NAME";
    private static final String REQUEST_ID = "DIGITAL_TWIN_REQUEST_ID";
    private static final byte[] COMMAND_PAYLOAD = String.format("{\"commandRequest\":{\"requestId\":\"%s\", \"value\":%s}}", REQUEST_ID, TEST_PAYLOAD).getBytes(UTF_8);
    private static final String DIGITAL_TWIN_COMPONENT_NAME_UNKNOWN = "UNKNOWN_COMPONENT_NAME";
    private static final int DESIRED_VERSION = 1234;
    private static final int DELAY_IN_MS = 100;
    private static final List<DigitalTwinReportProperty> TEST_PROPERTIES = asList(
            DigitalTwinReportProperty.builder()
                                     .propertyName("PROPERTY_INT")
                .propertyValue("123")
                .build(),
            DigitalTwinReportProperty.builder()
                                     .propertyName("PROPERTY_STRING")
                                     .propertyValue("\"STRING_VALUE\"")
                                     .build()
    );

    private DigitalTwinDeviceClient testee;
    private List<? extends AbstractDigitalTwinComponent> digitalTwinComponents;
    @Mock
    private DeviceClient deviceClient;
    @Mock
    private AbstractDigitalTwinComponent digitalTwinComponent1;
    @Mock
    private AbstractDigitalTwinComponent digitalTwinComponent2;
    @Mock
    private Consumer<Throwable> errorConsumer;
    @Mock
    private Object context;


    @Before
    public void setUp() throws IOException {
        testee = new DigitalTwinDeviceClient(deviceClient, DIGITAL_TWIN_DCM_ID);
        digitalTwinComponents = asList(digitalTwinComponent1, digitalTwinComponent2);
        when(digitalTwinComponent1.getDigitalTwinComponentName()).thenReturn(DIGITAL_TWIN_COMPONENT_NAME_1);
        when(digitalTwinComponent2.getDigitalTwinComponentName()).thenReturn(DIGITAL_TWIN_COMPONENT_NAME_2);
        when(digitalTwinComponent1.getDigitalTwinInterfaceId()).thenReturn(DIGITAL_TWIN_INTERFACE_ID_1);
        when(digitalTwinComponent2.getDigitalTwinInterfaceId()).thenReturn(DIGITAL_TWIN_INTERFACE_ID_2);
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
    public void bindComponentsTest() throws IOException {
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(digitalTwinComponent1).setDigitalTwinDeviceClient(eq(testee));
        verify(digitalTwinComponent2).setDigitalTwinDeviceClient(eq(testee));
        verify(deviceClient, never()).open();
    }

    @Test
    public void bindComponentsTwiceTest() throws IOException {
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(digitalTwinComponent1).setDigitalTwinDeviceClient(eq(testee));
        verify(digitalTwinComponent2).setDigitalTwinDeviceClient(eq(testee));
        verify(deviceClient, never()).open();

        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_ERROR_COMPONENTS_ALREADY_BOUND);
    }

    @Test
    public void registerComponentsAsyncTest() throws IOException {
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerComponentsAsync().blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).open();
        verify(deviceClient).sendEventAsync(any(Message.class), any(IotHubEventCallback.class), any());
    }

    @Test
    public void registerComponentsTest() {
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerComponents();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
    }

    @Test
    public void registerComponentsAsyncTwiceParallelTest() throws IOException {
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Single<DigitalTwinClientResult> single1 = testee.registerComponentsAsync();
        Single<DigitalTwinClientResult> single2 = testee.registerComponentsAsync();
        assertThat(single1.blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(single2.blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).open();
        verify(deviceClient, times(2)).sendEventAsync(any(Message.class), any(IotHubEventCallback.class), any());
    }

    @Test
    public void registerComponentsAsyncTwiceSequentialTest() {
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.registerComponentsAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.registerComponentsAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient, times(2)).sendEventAsync(any(Message.class), any(IotHubEventCallback.class), any());
    }

    @Test
    public void registerComponentsWithoutBindingTest() {
        DigitalTwinClientResult digitalTwinClientResult = testee.registerComponents();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR_COMPONENTS_NOT_BOUND);
    }

    @Test
    public void registerComponentsAsyncOpenThrowExceptionTest() throws Throwable {
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        final Exception exception = new Exception("OpenThrowException");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw exception;
            }
        }).when(deviceClient)
          .open();

        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.registerComponentsAsync()
                                                                    .doOnError(errorConsumer)
                                                                    .blockingGet();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Open threw exception", e);
        }

        verify(errorConsumer).accept(eq(exception));
        verify(deviceClient, never()).sendEventAsync(any(Message.class), any(IotHubEventCallback.class), any());
    }

    @Test
    public void registerComponentsAsyncFailedTest() throws IOException {
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
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

        DigitalTwinClientResult digitalTwinClientResult = testee.registerComponentsAsync().blockingGet();
        verify(deviceClient).open();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
    }

    @Test
    public void registerComponentsAsyncThrowExceptionTest() throws Throwable {
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        final Exception exception = new Exception("SendRegistrationMessageThrowException");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw exception;
            }
        }).when(deviceClient)
          .sendEventAsync(any(Message.class), any(IotHubEventCallback.class), any());

        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.registerComponentsAsync()
                                                                    .doOnError(errorConsumer)
                                                                    .blockingGet();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Send registration message threw exception", e);
        }

        verify(deviceClient).open();
        verify(errorConsumer).accept(eq(exception));
    }

    @Test
    public void subscribeForCommandsAsyncTest() throws IOException {
        assertThat(testee.subscribeForCommandsAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).open();
        verify(deviceClient).subscribeToDeviceMethod(any(DeviceMethodCallback.class), any(), any(IotHubEventCallback.class), any());
    }

    @Test
    public void subscribeForCommandsTest() throws IOException {
        assertThat(testee.subscribeForCommands()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).open();
        verify(deviceClient).subscribeToDeviceMethod(any(DeviceMethodCallback.class), any(), any(IotHubEventCallback.class), any());
    }

    @Test
    public void subscribeForCommandsAsyncOpenThrowExceptionTest() throws Throwable {
        final Exception exception = new Exception("OpenThrowException");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw exception;
            }
        }).when(deviceClient)
          .open();

        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.subscribeForCommandsAsync()
                                                                    .doOnError(errorConsumer)
                                                                    .blockingGet();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Open threw exception", e);
        }

        verify(errorConsumer).accept(eq(exception));
        verify(deviceClient, never()).subscribeToDeviceMethod(any(DeviceMethodCallback.class), any(), any(IotHubEventCallback.class), any());
    }

    @Test
    public void subscribeForCommandsAsyncFailedTest() throws IOException {
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
        DigitalTwinClientResult digitalTwinClientResult = testee.subscribeForCommandsAsync().blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
        verify(deviceClient).open();
    }

    @Test
    public void subscribeForCommandsAsyncThrowExceptionTest() throws Throwable {
        final Exception exception = new Exception("CommandSubscriptionThrowException");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw exception;
            }
        }).when(deviceClient)
          .subscribeToDeviceMethod(any(DeviceMethodCallback.class), any(), any(IotHubEventCallback.class), any());

        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.subscribeForCommandsAsync()
                                                                    .doOnError(errorConsumer)
                                                                    .blockingGet();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Subscribe command threw exception.", e);
        }

        verify(errorConsumer).accept(eq(exception));
        verify(deviceClient).open();
    }

    @Test
    public void subscribeForPropertiesAsyncTest() throws IOException {
        assertThat(testee.subscribeForPropertiesAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).open();
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), any(), any(TwinPropertyCallBack.class), any());
    }

    @Test
    public void subscribeForPropertiesTest() throws IOException {
        assertThat(testee.subscribeForProperties()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).open();
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), any(), any(TwinPropertyCallBack.class), any());
    }

    @Test
    public void subscribeForPropertiesAsyncOpenThrowExceptionTest() throws Throwable {
        final Exception exception = new Exception("OpenThrowException");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw exception;
            }
        }).when(deviceClient)
          .open();

        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.subscribeForPropertiesAsync()
                                                                    .doOnError(errorConsumer)
                                                                    .blockingGet();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Open threw exception", e);
        }

        verify(errorConsumer).accept(eq(exception));
        verify(deviceClient, never()).startDeviceTwin(any(IotHubEventCallback.class), any(), any(TwinPropertyCallBack.class), any());
    }

    @Test
    public void subscribeForPropertiesAsyncFailedTest() throws IOException {
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
        DigitalTwinClientResult digitalTwinClientResult = testee.subscribeForPropertiesAsync().blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
        verify(deviceClient).open();
    }

    @Test
    public void subscribeForPropertiesAsyncThrowExceptionTest() throws Throwable {
        final Exception exception = new Exception("TwinSubscriptionThrowException");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw exception;
            }
        }).when(deviceClient)
          .startDeviceTwin(any(IotHubEventCallback.class), any(), any(TwinPropertyCallBack.class), any());

        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.subscribeForPropertiesAsync()
                                                                    .doOnError(errorConsumer)
                                                                    .blockingGet();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Subscribe Twin threw exception.", e);
        }

        verify(errorConsumer).accept(eq(exception));
        verify(deviceClient).open();
    }

    @Test
    public void readyTest() {
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.ready()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(digitalTwinComponent1).ready();
        verify(digitalTwinComponent2).ready();
    }

    @Test
    public void readyWithoutBindingTest() {
        assertThat(testee.ready()).isEqualTo(DIGITALTWIN_CLIENT_ERROR_COMPONENTS_NOT_BOUND);
        verify(digitalTwinComponent1, never()).ready();
        verify(digitalTwinComponent2, never()).ready();
    }

    @Test
    public void syncupPropertiesAsyncTest() throws IOException {
        assertThat(testee.syncupPropertiesAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).open();
        verify(deviceClient).getDeviceTwin();
    }

    @Test
    public void syncupPropertiesTest() throws IOException {
        assertThat(testee.syncupProperties()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).open();
        verify(deviceClient).getDeviceTwin();
    }

    @Test
    public void syncupPropertiesAsyncThrowExceptionTest() throws Throwable {
        final Exception exception = new Exception("GetTwinThrowException");
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw exception;
            }
        }).when(deviceClient)
          .getDeviceTwin();

        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.syncupPropertiesAsync()
                                                                    .doOnError(errorConsumer)
                                                                    .blockingGet();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Get twin threw exception.", e);
        }

        verify(errorConsumer).accept(eq(exception));
        verify(deviceClient).open();
    }

    @Test
    public void sendTelemetryAsyncTest() {
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.sendTelemetryAsync(DIGITAL_TWIN_COMPONENT_NAME_1, TELEMETRY_NAME, TEST_PAYLOAD).blockingSingle();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).sendEventAsync(messageCaptor.capture(), any(IotHubEventCallback.class), any());
        Message message = messageCaptor.getValue();
        assertThat(message.getProperty(PROPERTY_DIGITAL_TWIN_COMPONENT)).isEqualTo(DIGITAL_TWIN_COMPONENT_NAME_1);
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
        DigitalTwinClientResult digitalTwinClientResult = testee.sendTelemetryAsync(DIGITAL_TWIN_COMPONENT_NAME_1, TELEMETRY_NAME, TEST_PAYLOAD).blockingSingle();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
        verify(deviceClient).sendEventAsync(messageCaptor.capture(), any(IotHubEventCallback.class), any());
        Message message = messageCaptor.getValue();
        assertThat(message.getProperty(PROPERTY_DIGITAL_TWIN_COMPONENT)).isEqualTo(DIGITAL_TWIN_COMPONENT_NAME_1);
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
            DigitalTwinClientResult digitalTwinClientResult = testee.sendTelemetryAsync(DIGITAL_TWIN_COMPONENT_NAME_1, TELEMETRY_NAME, TEST_PAYLOAD)
                                                                    .doOnError(errorConsumer)
                                                                    .blockingSingle();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Send event threw exception.", e);
        }
        verify(errorConsumer).accept(eq(exception));
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(deviceClient).sendEventAsync(messageCaptor.capture(), any(IotHubEventCallback.class), any());
        Message message = messageCaptor.getValue();
        assertThat(message.getProperty(PROPERTY_DIGITAL_TWIN_COMPONENT)).isEqualTo(DIGITAL_TWIN_COMPONENT_NAME_1);
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
        DigitalTwinClientResult digitalTwinClientResult = testee.updateAsyncCommandStatusAsync(DIGITAL_TWIN_COMPONENT_NAME_1, asyncCommandUpdate).blockingSingle();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).sendEventAsync(messageCaptor.capture(), any(IotHubEventCallback.class), any());
        Message message = messageCaptor.getValue();
        assertThat(message.getProperty(PROPERTY_DIGITAL_TWIN_COMPONENT)).isEqualTo(DIGITAL_TWIN_COMPONENT_NAME_1);
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
        DigitalTwinClientResult digitalTwinClientResult = testee.updateAsyncCommandStatusAsync(DIGITAL_TWIN_COMPONENT_NAME_1, asyncCommandUpdate).blockingSingle();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
        verify(deviceClient).sendEventAsync(messageCaptor.capture(), any(IotHubEventCallback.class), any());
        Message message = messageCaptor.getValue();
        assertThat(message.getProperty(PROPERTY_DIGITAL_TWIN_COMPONENT)).isEqualTo(DIGITAL_TWIN_COMPONENT_NAME_1);
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
            DigitalTwinClientResult digitalTwinClientResult = testee.updateAsyncCommandStatusAsync(DIGITAL_TWIN_COMPONENT_NAME_1, asyncCommandUpdate)
                                                                    .doOnError(errorConsumer)
                                                                    .blockingSingle();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Update async command status threw exception when on registered called.", e);
        }
        verify(errorConsumer).accept(eq(exception));

        verify(deviceClient).sendEventAsync(messageCaptor.capture(), any(IotHubEventCallback.class), any());
        Message message = messageCaptor.getValue();
        assertThat(message.getProperty(PROPERTY_DIGITAL_TWIN_COMPONENT)).isEqualTo(DIGITAL_TWIN_COMPONENT_NAME_1);
        assertThat(message.getProperty(PROPERTY_COMMAND_NAME)).isEqualTo(COMMAND_NAME);
        assertThat(message.getProperty(PROPERTY_REQUEST_ID)).isEqualTo(REQUEST_ID);
        assertThat(message.getProperty(PROPERTY_STATUS)).isEqualTo(String.valueOf(STATUS_CODE_COMPLETED));
        assertThat(new String(message.getBytes(), UTF_8)).isEqualTo(TEST_PAYLOAD);
    }

    @Test
    public void reportPropertiesAsyncTest() throws IOException {
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.reportPropertiesAsync(DIGITAL_TWIN_COMPONENT_NAME_1, TEST_PROPERTIES).blockingSingle()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).open();
        verify(deviceClient).sendReportedProperties(anySetOf(Property.class));
    }

    @Test
    public void commandDispatcherTest() throws Exception {
        ArgumentCaptor<DeviceMethodCallback> methodCallbackCaptor = ArgumentCaptor.forClass(DeviceMethodCallback.class);
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.registerComponentsAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.subscribeForCommandsAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).subscribeToDeviceMethod(methodCallbackCaptor.capture(), any(), any(IotHubEventCallback.class), any());
        DeviceMethodCallback methodCallback = methodCallbackCaptor.getValue();
        methodCallback.call(
                String.format("%s%s*%s", DIGITAL_TWIN_COMPONENT_NAME_PREFIX, DIGITAL_TWIN_COMPONENT_NAME_1, COMMAND_NAME),
                COMMAND_PAYLOAD,
                context
        );
        ArgumentCaptor<DigitalTwinCommandRequest> commandRequestCaptor = ArgumentCaptor.forClass(DigitalTwinCommandRequest.class);
        verify(digitalTwinComponent1).onCommandReceived(commandRequestCaptor.capture());
        DigitalTwinCommandRequest commandRequest = commandRequestCaptor.getValue();
        assertThat(commandRequest.getCommandName()).isEqualTo(COMMAND_NAME);
        assertThat(commandRequest.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(commandRequest.getPayload()).isEqualTo(TEST_PAYLOAD);
    }

    @Test
    public void commandDispatcherWithoutComponentNameTest() throws IOException {
        ArgumentCaptor<DeviceMethodCallback> methodCallbackCaptor = ArgumentCaptor.forClass(DeviceMethodCallback.class);
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.registerComponentsAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.subscribeForCommandsAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).subscribeToDeviceMethod(methodCallbackCaptor.capture(), any(), any(IotHubEventCallback.class), any());
        DeviceMethodCallback methodCallback = methodCallbackCaptor.getValue();
        DeviceMethodData methodData = methodCallback.call(
                COMMAND_NAME,
                COMMAND_PAYLOAD,
                context
        );
        verify(digitalTwinComponent1, never()).onCommandReceived(any(DigitalTwinCommandRequest.class));
        verify(digitalTwinComponent2, never()).onCommandReceived(any(DigitalTwinCommandRequest.class));
        assertThat(methodData.getStatus()).isEqualTo(STATUS_CODE_NOT_IMPLEMENTED);
    }

    @Test
    public void commandDispatcherWithUnknownComponentNameTest() throws IOException {
        ArgumentCaptor<DeviceMethodCallback> methodCallbackCaptor = ArgumentCaptor.forClass(DeviceMethodCallback.class);
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.registerComponentsAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.subscribeForCommandsAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).subscribeToDeviceMethod(methodCallbackCaptor.capture(), any(), any(IotHubEventCallback.class), any());
        DeviceMethodCallback methodCallback = methodCallbackCaptor.getValue();
        DeviceMethodData methodData = methodCallback.call(
                String.format("%s%s*%s", DIGITAL_TWIN_COMPONENT_NAME_PREFIX, DIGITAL_TWIN_COMPONENT_NAME_UNKNOWN, COMMAND_NAME),
                COMMAND_PAYLOAD,
                context
        );
        verify(digitalTwinComponent1, never()).onCommandReceived(any(DigitalTwinCommandRequest.class));
        verify(digitalTwinComponent2, never()).onCommandReceived(any(DigitalTwinCommandRequest.class));
        assertThat(methodData.getStatus()).isEqualTo(STATUS_CODE_NOT_IMPLEMENTED);
    }

    @Test
    public void propertyDispatcherReportedTest() throws IOException {
        ArgumentCaptor<TwinPropertyCallBack> twinPropertyCallbackCaptor = ArgumentCaptor.forClass(TwinPropertyCallBack.class);
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.registerComponentsAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.subscribeForPropertiesAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), any(), twinPropertyCallbackCaptor.capture(), any());
        Property property = mock(Property.class);
        JsonElement propertyValue = new JsonParser().parse(TEST_PAYLOAD);
        when(property.getKey()).thenReturn(String.format("%s%s", DIGITAL_TWIN_COMPONENT_NAME_PREFIX, DIGITAL_TWIN_COMPONENT_NAME_1));
        when(property.getIsReported()).thenReturn(true);
        when(property.getValue()).thenReturn(propertyValue);
        TwinPropertyCallBack twinPropertyCallBack = twinPropertyCallbackCaptor.getValue();
        twinPropertyCallBack.TwinPropertyCallBack(property, context);
        ArgumentCaptor<DigitalTwinPropertyUpdate> propertyUpdateCaptor = ArgumentCaptor.forClass(DigitalTwinPropertyUpdate.class);
        verify(digitalTwinComponent1, atLeastOnce()).onPropertyUpdate(propertyUpdateCaptor.capture());
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
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.registerComponentsAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.subscribeForPropertiesAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), any(), twinPropertyCallbackCaptor.capture(), any());
        Property property = mock(Property.class);
        JsonElement propertyValue = new JsonParser().parse(TEST_PAYLOAD);
        when(property.getKey()).thenReturn(String.format("%s%s", DIGITAL_TWIN_COMPONENT_NAME_PREFIX, DIGITAL_TWIN_COMPONENT_NAME_1));
        when(property.getIsReported()).thenReturn(false);
        when(property.getValue()).thenReturn(propertyValue);
        when(property.getVersion()).thenReturn(DESIRED_VERSION);
        TwinPropertyCallBack twinPropertyCallBack = twinPropertyCallbackCaptor.getValue();
        twinPropertyCallBack.TwinPropertyCallBack(property, context);
        ArgumentCaptor<DigitalTwinPropertyUpdate> propertyUpdateCaptor = ArgumentCaptor.forClass(DigitalTwinPropertyUpdate.class);
        verify(digitalTwinComponent1, atLeastOnce()).onPropertyUpdate(propertyUpdateCaptor.capture());
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
    public void propertyDispatcherWithoutComponentNameTest() throws IOException {
        ArgumentCaptor<TwinPropertyCallBack> twinPropertyCallbackCaptor = ArgumentCaptor.forClass(TwinPropertyCallBack.class);
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.registerComponentsAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.subscribeForPropertiesAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), any(), twinPropertyCallbackCaptor.capture(), any());
        Property property = mock(Property.class);
        JsonElement propertyValue = new JsonParser().parse(TEST_PAYLOAD);
        when(property.getKey()).thenReturn(DIGITAL_TWIN_COMPONENT_NAME_1);
        when(property.getValue()).thenReturn(propertyValue);
        TwinPropertyCallBack twinPropertyCallBack = twinPropertyCallbackCaptor.getValue();
        twinPropertyCallBack.TwinPropertyCallBack(property, context);
        verify(digitalTwinComponent1, never()).onPropertyUpdate(any(DigitalTwinPropertyUpdate.class));
        verify(digitalTwinComponent2, never()).onPropertyUpdate(any(DigitalTwinPropertyUpdate.class));
    }

    @Test
    public void propertyDispatcherNonObjectTest() throws IOException {
        ArgumentCaptor<TwinPropertyCallBack> twinPropertyCallbackCaptor = ArgumentCaptor.forClass(TwinPropertyCallBack.class);
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.registerComponentsAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.subscribeForPropertiesAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), any(), twinPropertyCallbackCaptor.capture(), any());
        Property property = mock(Property.class);
        JsonElement propertyValue = new JsonPrimitive(true);
        when(property.getKey()).thenReturn(String.format("%s%s", DIGITAL_TWIN_COMPONENT_NAME_PREFIX, DIGITAL_TWIN_COMPONENT_NAME_1));
        when(property.getValue()).thenReturn(propertyValue);
        TwinPropertyCallBack twinPropertyCallBack = twinPropertyCallbackCaptor.getValue();
        twinPropertyCallBack.TwinPropertyCallBack(property, context);
        verify(digitalTwinComponent1, never()).onPropertyUpdate(any(DigitalTwinPropertyUpdate.class));
        verify(digitalTwinComponent2, never()).onPropertyUpdate(any(DigitalTwinPropertyUpdate.class));
    }

    @Test
    public void propertyDispatcherNonJsonTest() throws IOException {
        ArgumentCaptor<TwinPropertyCallBack> twinPropertyCallbackCaptor = ArgumentCaptor.forClass(TwinPropertyCallBack.class);
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.registerComponentsAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.subscribeForPropertiesAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), any(), twinPropertyCallbackCaptor.capture(), any());
        Property property = mock(Property.class);
        when(property.getKey()).thenReturn(String.format("%s%s", DIGITAL_TWIN_COMPONENT_NAME_PREFIX, DIGITAL_TWIN_COMPONENT_NAME_1));
        when(property.getValue()).thenReturn(REQUEST_ID);
        TwinPropertyCallBack twinPropertyCallBack = twinPropertyCallbackCaptor.getValue();
        twinPropertyCallBack.TwinPropertyCallBack(property, context);
        verify(digitalTwinComponent1, never()).onPropertyUpdate(any(DigitalTwinPropertyUpdate.class));
        verify(digitalTwinComponent2, never()).onPropertyUpdate(any(DigitalTwinPropertyUpdate.class));
    }

    @Test
    public void propertyDispatcherWithUnknownComponentNameTest() throws IOException {
        ArgumentCaptor<TwinPropertyCallBack> twinPropertyCallbackCaptor = ArgumentCaptor.forClass(TwinPropertyCallBack.class);
        assertThat(testee.bindComponents(digitalTwinComponents)).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.registerComponentsAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.subscribeForPropertiesAsync().blockingGet()).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), any(), twinPropertyCallbackCaptor.capture(), any());
        Property property = mock(Property.class);
        JsonElement propertyValue = new JsonParser().parse(TEST_PAYLOAD);
        when(property.getKey()).thenReturn(String.format("%s%s", DIGITAL_TWIN_COMPONENT_NAME_PREFIX, DIGITAL_TWIN_COMPONENT_NAME_UNKNOWN));
        when(property.getValue()).thenReturn(propertyValue);
        TwinPropertyCallBack twinPropertyCallBack = twinPropertyCallbackCaptor.getValue();
        twinPropertyCallBack.TwinPropertyCallBack(property, context);
        verify(digitalTwinComponent1, never()).onPropertyUpdate(any(DigitalTwinPropertyUpdate.class));
        verify(digitalTwinComponent2, never()).onPropertyUpdate(any(DigitalTwinPropertyUpdate.class));
    }
}
