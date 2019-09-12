package com.microsoft.azure.sdk.iot.digitaltwin.device;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.List;

import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.ERROR;
import static com.microsoft.azure.sdk.iot.device.IotHubStatusCode.OK;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_INTERFACE_ALREADY_REGISTERED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_REGISTRATION_PENDING;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.RegistrationStatus.REGISTERED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.RegistrationStatus.REGISTERING;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.RegistrationStatus.UNREGISTERED;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
                        iotHubEventCallback.execute(OK, context);
                    }
                }).start();
                return null;
            }
        }).when(deviceClient)
          .sendEventAsync(any(Message.class), any(IotHubEventCallback.class), any());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
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
    public void registerInterfaceSendEventFailedTest() throws InterruptedException {
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
    }

    @Test
    public void registerInterfaceSendEventThrowExceptionTest() throws InterruptedException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Exception {
                throw new Exception("SendEventThrowException");
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
    }
}
