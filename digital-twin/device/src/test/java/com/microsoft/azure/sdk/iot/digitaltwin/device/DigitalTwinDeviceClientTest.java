package com.microsoft.azure.sdk.iot.digitaltwin.device;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private static final int REGISTRATION_LATENCY = 3000;

    private DigitalTwinDeviceClient testee;
    private List<? extends AbstractDigitalTwinInterfaceClient> digitalTwinInterfaceClients;
    @Mock
    private DeviceClient deviceClient;
    @Mock
    private AbstractDigitalTwinInterfaceClient digitalTwinInterfaceClient1;
    @Mock
    private AbstractDigitalTwinInterfaceClient digitalTwinInterfaceClient2;
    @Mock
    private DigitalTwinCallback digitalTwinInterfaceRegistrationCallback;
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
    public void registerInterfacesTest() throws InterruptedException {
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERING);
        Thread.sleep(REGISTRATION_LATENCY);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(digitalTwinInterfaceRegistrationCallback).onResult(eq(DIGITALTWIN_CLIENT_OK), eq(context));
        verify(digitalTwinInterfaceClient1).setDigitalTwinDeviceClient(eq(testee));
        verify(digitalTwinInterfaceClient2).setDigitalTwinDeviceClient(eq(testee));
        verify(digitalTwinInterfaceClient1).onRegistered();
        verify(digitalTwinInterfaceClient2).onRegistered();
    }

    @Test
    public void registerInterfaceTwicePendingTest() throws InterruptedException {
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERING);

        DigitalTwinCallback digitalTwinInterfaceRegistrationCallback2 = mock(DigitalTwinCallback.class);
        DigitalTwinClientResult digitalTwinClientResult2 = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback2,
                context
        );
        assertThat(digitalTwinClientResult2).isEqualTo(DIGITALTWIN_CLIENT_ERROR_REGISTRATION_PENDING);

        Thread.sleep(REGISTRATION_LATENCY);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(digitalTwinInterfaceRegistrationCallback).onResult(eq(DIGITALTWIN_CLIENT_OK), eq(context));
        verify(digitalTwinInterfaceRegistrationCallback2, never()).onResult(any(DigitalTwinClientResult.class), any());
    }

    @Test
    public void registerInterfaceTwiceAfterTest() throws InterruptedException {
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERING);
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);

        Thread.sleep(REGISTRATION_LATENCY);
        verify(digitalTwinInterfaceRegistrationCallback).onResult(eq(DIGITALTWIN_CLIENT_OK), eq(context));
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);

        DigitalTwinCallback digitalTwinInterfaceRegistrationCallback2 = mock(DigitalTwinCallback.class);
        DigitalTwinClientResult digitalTwinClientResult2 = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback2,
                context
        );
        assertThat(digitalTwinClientResult2).isEqualTo(DIGITALTWIN_CLIENT_ERROR_INTERFACE_ALREADY_REGISTERED);
        verify(digitalTwinInterfaceRegistrationCallback2, never()).onResult(any(DigitalTwinClientResult.class), any());
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
    }

    @Test
    public void registerInterfaceSendRegistrationMessageFailedTest() throws InterruptedException, IOException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
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

        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERING);
        Thread.sleep(REGISTRATION_LATENCY);
        verify(digitalTwinInterfaceRegistrationCallback).onResult(eq(DIGITALTWIN_CLIENT_ERROR), eq(context));
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient1, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient2, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient1, never()).onRegistered();
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
        verify(deviceClient, never()).subscribeToDeviceMethod(any(DeviceMethodCallback.class), any(), any(IotHubEventCallback.class), any());
    }

    @Test
    public void registerInterfaceSendRegistrationMessageThrowExceptionTest() throws InterruptedException, IOException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw new Exception("SendRegistrationMessageThrowException");
            }
        }).when(deviceClient)
          .sendEventAsync(any(Message.class), any(IotHubEventCallback.class), any());

        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERING);
        Thread.sleep(REGISTRATION_LATENCY);
        verify(digitalTwinInterfaceRegistrationCallback).onResult(eq(DIGITALTWIN_CLIENT_ERROR), eq(context));
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient1, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient2, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient1, never()).onRegistered();
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
        verify(deviceClient, never()).subscribeToDeviceMethod(any(DeviceMethodCallback.class), any(), any(IotHubEventCallback.class), any());
    }

    @Test
    public void registerInterfaceCommandSubscriptionFailedTest() throws IOException, InterruptedException {
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
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERING);
        Thread.sleep(REGISTRATION_LATENCY);
        verify(digitalTwinInterfaceRegistrationCallback).onResult(eq(DIGITALTWIN_CLIENT_ERROR), eq(context));
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient1, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient2, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient1, never()).onRegistered();
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
        verify(deviceClient, never()).startDeviceTwin(any(IotHubEventCallback.class), any(), any(TwinPropertyCallBack.class), any());
    }

    @Test
    public void registerInterfaceCommandSubscriptionThrowExceptionTest() throws IOException, InterruptedException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw new Exception("CommandSubscriptionThrowException");
            }
        }).when(deviceClient)
          .subscribeToDeviceMethod(any(DeviceMethodCallback.class), any(), any(IotHubEventCallback.class), any());
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERING);
        Thread.sleep(REGISTRATION_LATENCY);
        verify(digitalTwinInterfaceRegistrationCallback).onResult(eq(DIGITALTWIN_CLIENT_ERROR), eq(context));
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient1, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient2, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient1, never()).onRegistered();
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
        verify(deviceClient, never()).startDeviceTwin(any(IotHubEventCallback.class), any(), any(TwinPropertyCallBack.class), any());
    }

    @Test
    public void registerInterfaceTwinSubscriptionFailedTest() throws IOException, InterruptedException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
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
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERING);
        Thread.sleep(REGISTRATION_LATENCY);
        verify(digitalTwinInterfaceRegistrationCallback).onResult(eq(DIGITALTWIN_CLIENT_ERROR), eq(context));
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient1, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient2, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient1, never()).onRegistered();
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
        verify(deviceClient, never()).sendReportedProperties(anySetOf(Property.class));
    }

    @Test
    public void registerInterfaceTwinSubscriptionThrowExceptionTest() throws IOException, InterruptedException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw new Exception("TwinSubscriptionThrowException");
            }
        }).when(deviceClient)
          .startDeviceTwin(any(IotHubEventCallback.class), any(), any(TwinPropertyCallBack.class), any()); ;
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERING);
        Thread.sleep(REGISTRATION_LATENCY);
        verify(digitalTwinInterfaceRegistrationCallback).onResult(eq(DIGITALTWIN_CLIENT_ERROR), eq(context));
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient1, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient2, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient1, never()).onRegistered();
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
        verify(deviceClient, never()).sendReportedProperties(anySetOf(Property.class));
    }

    @Test
    public void registerInterfaceReportSdkInformationThrowExceptionTest() throws IOException, InterruptedException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw new Exception("ReportSdkInformationThrowException");
            }
        }).when(deviceClient)
          .sendReportedProperties(anySetOf(Property.class));
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERING);
        Thread.sleep(REGISTRATION_LATENCY);
        verify(digitalTwinInterfaceRegistrationCallback).onResult(eq(DIGITALTWIN_CLIENT_ERROR), eq(context));
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient1, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient2, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient1, never()).onRegistered();
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
        verify(deviceClient, never()).getDeviceTwin();
    }

    @Test
    public void registerInterfaceGetTwinThrowExceptionTest() throws IOException, InterruptedException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw new Exception("GetTwinThrowException");
            }
        }).when(deviceClient)
          .getDeviceTwin();
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERING);
        Thread.sleep(REGISTRATION_LATENCY);
        verify(digitalTwinInterfaceRegistrationCallback).onResult(eq(DIGITALTWIN_CLIENT_ERROR), eq(context));
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient1, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient2, never()).setDigitalTwinDeviceClient(any(DigitalTwinDeviceClient.class));
        verify(digitalTwinInterfaceClient1, never()).onRegistered();
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
    }

    @Test
    public void registerInterfaceOnRegistered1ThrowExceptionTest() throws InterruptedException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw new Exception("InterfaceClient1OnRegisteredThrowExceptionThrowException");
            }
        }).when(digitalTwinInterfaceClient1)
          .onRegistered();
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERING);
        Thread.sleep(REGISTRATION_LATENCY);
        verify(digitalTwinInterfaceRegistrationCallback).onResult(eq(DIGITALTWIN_CLIENT_ERROR), eq(context));
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient2, never()).onRegistered();
    }

    @Test
    public void registerInterfaceOnRegistered2ThrowExceptionTest() throws InterruptedException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw new Exception("InterfaceClient2OnRegisteredThrowExceptionThrowException");
            }
        }).when(digitalTwinInterfaceClient2)
          .onRegistered();
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERING);
        Thread.sleep(REGISTRATION_LATENCY);
        verify(digitalTwinInterfaceRegistrationCallback).onResult(eq(DIGITALTWIN_CLIENT_ERROR), eq(context));
        assertThat(testee.getRegistrationStatus()).isEqualTo(UNREGISTERED);
        verify(digitalTwinInterfaceClient1).onRegistered();
    }

    @Test
    public void sendTelemetryAsyncTest() throws InterruptedException {
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        DigitalTwinCallback telemetryCallback = mock(DigitalTwinCallback.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.sendTelemetryAsync(DIGITAL_TWIN_INTERFACE_INSTANCE_1, TELEMETRY_NAME, TEST_PAYLOAD, telemetryCallback, context);
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(REGISTRATION_LATENCY);
        verify(deviceClient).sendEventAsync(messageCaptor.capture(), any(IotHubEventCallback.class), any());
        verify(telemetryCallback).onResult(eq(DIGITALTWIN_CLIENT_OK), eq(context));
        Message message = messageCaptor.getValue();
        assertThat(message.getProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE)).isEqualTo(DIGITAL_TWIN_INTERFACE_INSTANCE_1);
        assertThat(message.getProperty(PROPERTY_MESSAGE_SCHEMA)).isEqualTo(TELEMETRY_NAME);
        String expectedPayload = String.format("{\"%s\":%s}", TELEMETRY_NAME, TEST_PAYLOAD);
        assertThat(new String(message.getBytes(), UTF_8)).isEqualTo(expectedPayload);
    }

    @Test
    public void sendTelemetryAsyncFailedTest() throws InterruptedException {
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
        DigitalTwinCallback telemetryCallback = mock(DigitalTwinCallback.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.sendTelemetryAsync(DIGITAL_TWIN_INTERFACE_INSTANCE_1, TELEMETRY_NAME, TEST_PAYLOAD, telemetryCallback, context);
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(REGISTRATION_LATENCY);
        verify(telemetryCallback).onResult(eq(DIGITALTWIN_CLIENT_ERROR), eq(context));
        verify(deviceClient).sendEventAsync(messageCaptor.capture(), any(IotHubEventCallback.class), any());
        Message message = messageCaptor.getValue();
        assertThat(message.getProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE)).isEqualTo(DIGITAL_TWIN_INTERFACE_INSTANCE_1);
        assertThat(message.getProperty(PROPERTY_MESSAGE_SCHEMA)).isEqualTo(TELEMETRY_NAME);
        String expectedPayload = String.format("{\"%s\":%s}", TELEMETRY_NAME, TEST_PAYLOAD);
        assertThat(new String(message.getBytes(), UTF_8)).isEqualTo(expectedPayload);
    }

    @Test
    public void sendTelemetryAsyncThrowExceptionTest() throws InterruptedException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw new Exception("SendEventAsyncThrowException");
            }
        }).when(deviceClient)
          .sendEventAsync(any(Message.class), any(IotHubEventCallback.class), any());
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        DigitalTwinCallback telemetryCallback = mock(DigitalTwinCallback.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.sendTelemetryAsync(
                DIGITAL_TWIN_INTERFACE_INSTANCE_1,
                TELEMETRY_NAME,
                TEST_PAYLOAD,
                telemetryCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
        Thread.sleep(REGISTRATION_LATENCY);
        verify(telemetryCallback, never()).onResult(any(DigitalTwinClientResult.class), eq(context));
        verify(deviceClient).sendEventAsync(messageCaptor.capture(), any(IotHubEventCallback.class), any());
        Message message = messageCaptor.getValue();
        assertThat(message.getProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE)).isEqualTo(DIGITAL_TWIN_INTERFACE_INSTANCE_1);
        assertThat(message.getProperty(PROPERTY_MESSAGE_SCHEMA)).isEqualTo(TELEMETRY_NAME);
        String expectedPayload = String.format("{\"%s\":%s}", TELEMETRY_NAME, TEST_PAYLOAD);
        assertThat(new String(message.getBytes(), UTF_8)).isEqualTo(expectedPayload);
    }

    @Test
    public void updateAsyncCommandStatusAsyncTest() throws InterruptedException {
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        DigitalTwinCallback telemetryCallback = mock(DigitalTwinCallback.class);
        DigitalTwinAsyncCommandUpdate asyncCommandUpdate = DigitalTwinAsyncCommandUpdate.builder()
                                                                                        .commandName(COMMAND_NAME)
                                                                                        .requestId(REQUEST_ID)
                                                                                        .statusCode(STATUS_CODE_COMPLETED)
                                                                                        .payload(TEST_PAYLOAD)
                                                                                        .build();
        DigitalTwinClientResult digitalTwinClientResult = testee.updateAsyncCommandStatusAsync(
                DIGITAL_TWIN_INTERFACE_INSTANCE_1,
                asyncCommandUpdate,
                telemetryCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(REGISTRATION_LATENCY);
        verify(deviceClient).sendEventAsync(messageCaptor.capture(), any(IotHubEventCallback.class), any());
        verify(telemetryCallback).onResult(eq(DIGITALTWIN_CLIENT_OK), eq(context));
        Message message = messageCaptor.getValue();
        assertThat(message.getProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE)).isEqualTo(DIGITAL_TWIN_INTERFACE_INSTANCE_1);
        assertThat(message.getProperty(PROPERTY_COMMAND_NAME)).isEqualTo(COMMAND_NAME);
        assertThat(message.getProperty(PROPERTY_REQUEST_ID)).isEqualTo(REQUEST_ID);
        assertThat(message.getProperty(PROPERTY_STATUS)).isEqualTo(String.valueOf(STATUS_CODE_COMPLETED));
        assertThat(new String(message.getBytes(), UTF_8)).isEqualTo(TEST_PAYLOAD);
    }

    @Test
    public void updateAsyncCommandStatusAsyncFailedTest() throws InterruptedException {
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
          .sendEventAsync(any(Message.class), any(IotHubEventCallback.class), any()); ;
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        DigitalTwinCallback telemetryCallback = mock(DigitalTwinCallback.class);
        DigitalTwinAsyncCommandUpdate asyncCommandUpdate = DigitalTwinAsyncCommandUpdate.builder()
                                                                                        .commandName(COMMAND_NAME)
                                                                                        .requestId(REQUEST_ID)
                                                                                        .statusCode(STATUS_CODE_COMPLETED)
                                                                                        .payload(TEST_PAYLOAD)
                                                                                        .build();
        DigitalTwinClientResult digitalTwinClientResult = testee.updateAsyncCommandStatusAsync(
                DIGITAL_TWIN_INTERFACE_INSTANCE_1,
                asyncCommandUpdate,
                telemetryCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(REGISTRATION_LATENCY);
        verify(deviceClient).sendEventAsync(messageCaptor.capture(), any(IotHubEventCallback.class), any());
        verify(telemetryCallback).onResult(eq(DIGITALTWIN_CLIENT_ERROR), eq(context));
        Message message = messageCaptor.getValue();
        assertThat(message.getProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE)).isEqualTo(DIGITAL_TWIN_INTERFACE_INSTANCE_1);
        assertThat(message.getProperty(PROPERTY_COMMAND_NAME)).isEqualTo(COMMAND_NAME);
        assertThat(message.getProperty(PROPERTY_REQUEST_ID)).isEqualTo(REQUEST_ID);
        assertThat(message.getProperty(PROPERTY_STATUS)).isEqualTo(String.valueOf(STATUS_CODE_COMPLETED));
        assertThat(new String(message.getBytes(), UTF_8)).isEqualTo(TEST_PAYLOAD);
    }

    @Test
    public void updateAsyncCommandStatusAsyncThrowExceptionTest() throws InterruptedException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw new Exception("UpdateAsyncCommandStatusAsyncThrowException");
            }
        }).when(deviceClient)
          .sendEventAsync(any(Message.class), any(IotHubEventCallback.class), any());
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        DigitalTwinCallback telemetryCallback = mock(DigitalTwinCallback.class);
        DigitalTwinAsyncCommandUpdate asyncCommandUpdate = DigitalTwinAsyncCommandUpdate.builder()
                                                                                        .commandName(COMMAND_NAME)
                                                                                        .requestId(REQUEST_ID)
                                                                                        .statusCode(STATUS_CODE_COMPLETED)
                                                                                        .payload(TEST_PAYLOAD)
                                                                                        .build();
        DigitalTwinClientResult digitalTwinClientResult = testee.updateAsyncCommandStatusAsync(
                DIGITAL_TWIN_INTERFACE_INSTANCE_1,
                asyncCommandUpdate,
                telemetryCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
        Thread.sleep(REGISTRATION_LATENCY);
        verify(deviceClient).sendEventAsync(messageCaptor.capture(), any(IotHubEventCallback.class), any());
        verify(telemetryCallback, never()).onResult(any(DigitalTwinClientResult.class), eq(context));
        Message message = messageCaptor.getValue();
        assertThat(message.getProperty(PROPERTY_DIGITAL_TWIN_INTERFACE_INSTANCE)).isEqualTo(DIGITAL_TWIN_INTERFACE_INSTANCE_1);
        assertThat(message.getProperty(PROPERTY_COMMAND_NAME)).isEqualTo(COMMAND_NAME);
        assertThat(message.getProperty(PROPERTY_REQUEST_ID)).isEqualTo(REQUEST_ID);
        assertThat(message.getProperty(PROPERTY_STATUS)).isEqualTo(String.valueOf(STATUS_CODE_COMPLETED));
        assertThat(new String(message.getBytes(), UTF_8)).isEqualTo(TEST_PAYLOAD);
    }

    @Test
    public void commandDispatcherTest() throws InterruptedException, IOException {
        ArgumentCaptor<DeviceMethodCallback> methodCallbackCaptor = ArgumentCaptor.forClass(DeviceMethodCallback.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(REGISTRATION_LATENCY);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(deviceClient).subscribeToDeviceMethod(methodCallbackCaptor.capture(), eq(context), any(IotHubEventCallback.class), eq(context));
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
    public void commandDispatcherWithoutInterfaceInstanceNameTest() throws InterruptedException, IOException {
        ArgumentCaptor<DeviceMethodCallback> methodCallbackCaptor = ArgumentCaptor.forClass(DeviceMethodCallback.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(REGISTRATION_LATENCY);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(deviceClient).subscribeToDeviceMethod(methodCallbackCaptor.capture(), eq(context), any(IotHubEventCallback.class), eq(context));
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
    public void commandDispatcherWithUnknownInterfaceInstanceNameTest() throws InterruptedException, IOException {
        ArgumentCaptor<DeviceMethodCallback> methodCallbackCaptor = ArgumentCaptor.forClass(DeviceMethodCallback.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(REGISTRATION_LATENCY);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(deviceClient).subscribeToDeviceMethod(methodCallbackCaptor.capture(), eq(context), any(IotHubEventCallback.class), eq(context));
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
    public void propertyDispatcherReportedTest() throws InterruptedException, IOException {
        ArgumentCaptor<TwinPropertyCallBack> twinPropertyCallbackCaptor = ArgumentCaptor.forClass(TwinPropertyCallBack.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(REGISTRATION_LATENCY);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), eq(context), twinPropertyCallbackCaptor.capture(), eq(context));
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
    public void propertyDispatcherDesiredTest() throws InterruptedException, IOException {
        ArgumentCaptor<TwinPropertyCallBack> twinPropertyCallbackCaptor = ArgumentCaptor.forClass(TwinPropertyCallBack.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(REGISTRATION_LATENCY);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), eq(context), twinPropertyCallbackCaptor.capture(), eq(context));
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
    public void propertyDispatcherWithoutInterfaceInstanceNameTest() throws InterruptedException, IOException {
        ArgumentCaptor<TwinPropertyCallBack> twinPropertyCallbackCaptor = ArgumentCaptor.forClass(TwinPropertyCallBack.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(REGISTRATION_LATENCY);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), eq(context), twinPropertyCallbackCaptor.capture(), eq(context));
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
    public void propertyDispatcherNoneObjectTest() throws InterruptedException, IOException {
        ArgumentCaptor<TwinPropertyCallBack> twinPropertyCallbackCaptor = ArgumentCaptor.forClass(TwinPropertyCallBack.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(REGISTRATION_LATENCY);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), eq(context), twinPropertyCallbackCaptor.capture(), eq(context));
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
    public void propertyDispatcherNoneJsonTest() throws InterruptedException, IOException {
        ArgumentCaptor<TwinPropertyCallBack> twinPropertyCallbackCaptor = ArgumentCaptor.forClass(TwinPropertyCallBack.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(REGISTRATION_LATENCY);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), eq(context), twinPropertyCallbackCaptor.capture(), eq(context));
        Property property = mock(Property.class);
        when(property.getKey()).thenReturn(String.format("%s%s", DIGITAL_TWIN_INTERFACE_INSTANCE_NAME_PREFIX, DIGITAL_TWIN_INTERFACE_INSTANCE_1));
        when(property.getValue()).thenReturn(REQUEST_ID);
        TwinPropertyCallBack twinPropertyCallBack = twinPropertyCallbackCaptor.getValue();
        twinPropertyCallBack.TwinPropertyCallBack(property, context);
        verify(digitalTwinInterfaceClient1, never()).onPropertyUpdate(any(DigitalTwinPropertyUpdate.class));
        verify(digitalTwinInterfaceClient2, never()).onPropertyUpdate(any(DigitalTwinPropertyUpdate.class));
    }

    @Test
    public void propertyDispatcherWithUnknownInterfaceInstanceNameTest() throws InterruptedException, IOException {
        ArgumentCaptor<TwinPropertyCallBack> twinPropertyCallbackCaptor = ArgumentCaptor.forClass(TwinPropertyCallBack.class);
        DigitalTwinClientResult digitalTwinClientResult = testee.registerInterfacesAsync(
                DIGITAL_TWIN_DCM_ID,
                digitalTwinInterfaceClients,
                digitalTwinInterfaceRegistrationCallback,
                context
        );
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(REGISTRATION_LATENCY);
        assertThat(testee.getRegistrationStatus()).isEqualTo(REGISTERED);
        verify(deviceClient).startDeviceTwin(any(IotHubEventCallback.class), eq(context), twinPropertyCallbackCaptor.capture(), eq(context));
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
