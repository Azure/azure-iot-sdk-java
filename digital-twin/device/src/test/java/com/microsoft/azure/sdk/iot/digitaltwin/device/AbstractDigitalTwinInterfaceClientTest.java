// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.device;

import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinAsyncCommandUpdate;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandRequest;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinPropertyUpdate;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinReportProperty;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinInterfaceClient.COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinInterfaceClient.STATUS_CODE_NOT_IMPLEMENTED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_INTERFACE_NOT_REGISTERED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DigitalTwinDeviceClient.class})
public class AbstractDigitalTwinInterfaceClientTest {
    private static final String DIGITAL_TWIN_INTERFACE_INSTANCE = "DIGITAL_TWIN_INTERFACE_INSTANCE";
    private static final String DIGITAL_TWIN_INTERFACE_ID = "DIGITAL_TWIN_INTERFACE_ID";
    private static final String TELEMETRY_NAME = "TELEMETRY_NAME";
    private static final String TELEMETRY_PAYLOAD = "TELEMETRY_PAYLOAD";
    private static final int OPERATION_LATENCY = 500;

    private AbstractDigitalTwinInterfaceClient testee;
    @Mock
    private DigitalTwinDeviceClient digitalTwinDeviceClient;
    @Mock
    private Object context;
    @Mock
    private List<DigitalTwinReportProperty> properties;
    @Mock
    private DigitalTwinAsyncCommandUpdate asyncCommandUpdate;
    @Mock
    private DigitalTwinPropertyUpdate propertyUpdate;
    @Mock
    private DigitalTwinCommandRequest commandRequest;
    @Mock
    private DigitalTwinCallback operationCallback;

    @Before
    public void setUp() {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                final DigitalTwinCallback callback = invocation.getArgumentAt(3, DigitalTwinCallback.class);
                final Object context = invocation.getArgumentAt(4, Object.class);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(DIGITALTWIN_CLIENT_OK, context);
                    }
                }).start();
                return DIGITALTWIN_CLIENT_OK;
            }
        }).when(digitalTwinDeviceClient)
          .sendTelemetryAsync(eq(DIGITAL_TWIN_INTERFACE_INSTANCE), eq(TELEMETRY_NAME), eq(TELEMETRY_PAYLOAD), eq(operationCallback), eq(context));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                final DigitalTwinCallback callback = invocation.getArgumentAt(2, DigitalTwinCallback.class);
                final Object context = invocation.getArgumentAt(3, Object.class);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(DIGITALTWIN_CLIENT_OK, context);
                    }
                }).start();
                return DIGITALTWIN_CLIENT_OK;
            }
        }).when(digitalTwinDeviceClient)
          .reportPropertiesAsync(eq(DIGITAL_TWIN_INTERFACE_INSTANCE), eq(properties), eq(operationCallback), eq(context));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                final DigitalTwinCallback callback = invocation.getArgumentAt(2, DigitalTwinCallback.class);
                final Object context = invocation.getArgumentAt(3, Object.class);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(DIGITALTWIN_CLIENT_OK, context);
                    }
                }).start();
                return DIGITALTWIN_CLIENT_OK;
            }
        }).when(digitalTwinDeviceClient)
          .updateAsyncCommandStatusAsync(eq(DIGITAL_TWIN_INTERFACE_INSTANCE), eq(asyncCommandUpdate), eq(operationCallback), eq(context));
        testee = new DigitalTwinInterfaceClient();
    }

    @Test
    public void sendTelemetryAsyncTest() throws InterruptedException {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        testee.onRegistered();
        DigitalTwinClientResult digitalTwinClientResult = testee.sendTelemetryAsync(TELEMETRY_NAME, TELEMETRY_PAYLOAD, operationCallback, context);
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(OPERATION_LATENCY);
        verify(operationCallback).onResult(eq(DIGITALTWIN_CLIENT_OK), eq(context));
        verify(digitalTwinDeviceClient).sendTelemetryAsync(eq(DIGITAL_TWIN_INTERFACE_INSTANCE), eq(TELEMETRY_NAME), eq(TELEMETRY_PAYLOAD), eq(operationCallback), eq(context));
    }

    @Test
    public void sendTelemetryAsyncFailureTest() throws InterruptedException {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        testee.onRegistered();
        when(digitalTwinDeviceClient.sendTelemetryAsync(eq(DIGITAL_TWIN_INTERFACE_INSTANCE), eq(TELEMETRY_NAME), eq(TELEMETRY_PAYLOAD), eq(operationCallback), eq(context))).thenReturn(
                DIGITALTWIN_CLIENT_ERROR);
        DigitalTwinClientResult digitalTwinClientResult = testee.sendTelemetryAsync(TELEMETRY_NAME, TELEMETRY_PAYLOAD, operationCallback, context);
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
        Thread.sleep(OPERATION_LATENCY);
        verify(operationCallback, never()).onResult(any(DigitalTwinClientResult.class), any());
        verify(digitalTwinDeviceClient).sendTelemetryAsync(eq(DIGITAL_TWIN_INTERFACE_INSTANCE), eq(TELEMETRY_NAME), eq(TELEMETRY_PAYLOAD), eq(operationCallback), eq(context));
    }

    @Test
    public void sendTelemetryAsyncCallbackFailureTest() throws InterruptedException {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        testee.onRegistered();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                final DigitalTwinCallback callback = invocation.getArgumentAt(3, DigitalTwinCallback.class);
                final Object context = invocation.getArgumentAt(4, Object.class);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(DIGITALTWIN_CLIENT_ERROR, context);
                    }
                }).start();
                return DIGITALTWIN_CLIENT_OK;
            }
        }).when(digitalTwinDeviceClient)
          .sendTelemetryAsync(eq(DIGITAL_TWIN_INTERFACE_INSTANCE), eq(TELEMETRY_NAME), eq(TELEMETRY_PAYLOAD), eq(operationCallback), eq(context));
        DigitalTwinClientResult digitalTwinClientResult = testee.sendTelemetryAsync(TELEMETRY_NAME, TELEMETRY_PAYLOAD, operationCallback, context);
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(OPERATION_LATENCY);
        verify(operationCallback).onResult(eq(DIGITALTWIN_CLIENT_ERROR), eq(context));
        verify(digitalTwinDeviceClient).sendTelemetryAsync(eq(DIGITAL_TWIN_INTERFACE_INSTANCE), eq(TELEMETRY_NAME), eq(TELEMETRY_PAYLOAD), eq(operationCallback), eq(context));
    }

    @Test
    public void sendTelemetryAsyncWithoutRegisterTest() throws InterruptedException {
        DigitalTwinClientResult digitalTwinClientResult = testee.sendTelemetryAsync(TELEMETRY_NAME, TELEMETRY_PAYLOAD, operationCallback, context);
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR_INTERFACE_NOT_REGISTERED);
        Thread.sleep(OPERATION_LATENCY);
        verify(operationCallback, never()).onResult(any(DigitalTwinClientResult.class), any());
        verify(digitalTwinDeviceClient, never()).sendTelemetryAsync(anyString(), anyString(), anyString(), any(DigitalTwinCallback.class), any());
    }

    @Test
    public void reportPropertiesAsyncTest() throws InterruptedException {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        testee.onRegistered();
        DigitalTwinClientResult digitalTwinClientResult = testee.reportPropertiesAsync(properties, operationCallback, context);
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(OPERATION_LATENCY);
        verify(operationCallback).onResult(eq(DIGITALTWIN_CLIENT_OK), eq(context));
        verify(digitalTwinDeviceClient).reportPropertiesAsync(eq(DIGITAL_TWIN_INTERFACE_INSTANCE), eq(properties), eq(operationCallback), eq(context));
    }

    @Test
    public void reportPropertiesAsyncFailureTest() throws InterruptedException {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        testee.onRegistered();
        when(digitalTwinDeviceClient.reportPropertiesAsync(eq(DIGITAL_TWIN_INTERFACE_INSTANCE), eq(properties), eq(operationCallback), eq(context))).thenReturn(DIGITALTWIN_CLIENT_ERROR);
        DigitalTwinClientResult digitalTwinClientResult = testee.reportPropertiesAsync(properties, operationCallback, context);
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
        Thread.sleep(OPERATION_LATENCY);
        verify(operationCallback, never()).onResult(any(DigitalTwinClientResult.class), any());
        verify(digitalTwinDeviceClient).reportPropertiesAsync(eq(DIGITAL_TWIN_INTERFACE_INSTANCE), eq(properties), eq(operationCallback), eq(context));
    }

    @Test
    public void reportPropertiesAsyncCallbackFailureTest() throws InterruptedException {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        testee.onRegistered();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                final DigitalTwinCallback callback = invocation.getArgumentAt(2, DigitalTwinCallback.class);
                final Object context = invocation.getArgumentAt(3, Object.class);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(DIGITALTWIN_CLIENT_ERROR, context);
                    }
                }).start();
                return DIGITALTWIN_CLIENT_OK;
            }
        }).when(digitalTwinDeviceClient)
          .reportPropertiesAsync(eq(DIGITAL_TWIN_INTERFACE_INSTANCE), eq(properties), eq(operationCallback), eq(context));
        DigitalTwinClientResult digitalTwinClientResult = testee.reportPropertiesAsync(properties, operationCallback, context);
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(OPERATION_LATENCY);
        verify(operationCallback).onResult(eq(DIGITALTWIN_CLIENT_ERROR), eq(context));
        verify(digitalTwinDeviceClient).reportPropertiesAsync(eq(DIGITAL_TWIN_INTERFACE_INSTANCE), eq(properties), eq(operationCallback), eq(context));
    }

    @Test
    public void reportPropertiesAsyncWithoutRegisterTest() throws InterruptedException {
        DigitalTwinClientResult digitalTwinClientResult = testee.reportPropertiesAsync(properties, operationCallback, context);
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR_INTERFACE_NOT_REGISTERED);
        Thread.sleep(OPERATION_LATENCY);
        verify(operationCallback, never()).onResult(any(DigitalTwinClientResult.class), any());
        verify(digitalTwinDeviceClient, never()).reportPropertiesAsync(anyString(), anyListOf(DigitalTwinReportProperty.class), any(DigitalTwinCallback.class), any());
    }

    @Test
    public void updateAsyncCommandStatusAsyncTest() throws InterruptedException {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        testee.onRegistered();
        DigitalTwinClientResult digitalTwinClientResult = testee.updateAsyncCommandStatusAsync(asyncCommandUpdate, operationCallback, context);
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(OPERATION_LATENCY);
        verify(operationCallback).onResult(eq(DIGITALTWIN_CLIENT_OK), eq(context));
        verify(digitalTwinDeviceClient).updateAsyncCommandStatusAsync(eq(DIGITAL_TWIN_INTERFACE_INSTANCE), eq(asyncCommandUpdate), eq(operationCallback), eq(context));
    }

    @Test
    public void updateAsyncCommandStatusAsyncFailureTest() throws InterruptedException {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        testee.onRegistered();
        when(digitalTwinDeviceClient.updateAsyncCommandStatusAsync(
                eq(DIGITAL_TWIN_INTERFACE_INSTANCE),
                eq(asyncCommandUpdate),
                eq(operationCallback),
                eq(context)
        )).thenReturn(DIGITALTWIN_CLIENT_ERROR);
        DigitalTwinClientResult digitalTwinClientResult = testee.updateAsyncCommandStatusAsync(asyncCommandUpdate, operationCallback, context);
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
        Thread.sleep(OPERATION_LATENCY);
        verify(operationCallback, never()).onResult(any(DigitalTwinClientResult.class), any());
        verify(digitalTwinDeviceClient).updateAsyncCommandStatusAsync(eq(DIGITAL_TWIN_INTERFACE_INSTANCE), eq(asyncCommandUpdate), eq(operationCallback), eq(context));
    }

    @Test
    public void updateAsyncCommandStatusAsyncCallbackFailureTest() throws InterruptedException {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        testee.onRegistered();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                final DigitalTwinCallback callback = invocation.getArgumentAt(2, DigitalTwinCallback.class);
                final Object context = invocation.getArgumentAt(3, Object.class);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(DIGITALTWIN_CLIENT_ERROR, context);
                    }
                }).start();
                return DIGITALTWIN_CLIENT_OK;
            }
        }).when(digitalTwinDeviceClient)
          .updateAsyncCommandStatusAsync(eq(DIGITAL_TWIN_INTERFACE_INSTANCE), eq(asyncCommandUpdate), eq(operationCallback), eq(context));
        DigitalTwinClientResult digitalTwinClientResult = testee.updateAsyncCommandStatusAsync(asyncCommandUpdate, operationCallback, context);
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(OPERATION_LATENCY);
        verify(operationCallback).onResult(eq(DIGITALTWIN_CLIENT_ERROR), eq(context));
        verify(digitalTwinDeviceClient).updateAsyncCommandStatusAsync(eq(DIGITAL_TWIN_INTERFACE_INSTANCE), eq(asyncCommandUpdate), eq(operationCallback), eq(context));
    }

    @Test
    public void updateAsyncCommandStatusAsyncWithoutRegisterTest() throws InterruptedException {
        DigitalTwinClientResult digitalTwinClientResult = testee.updateAsyncCommandStatusAsync(asyncCommandUpdate, operationCallback, context);
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR_INTERFACE_NOT_REGISTERED);
        Thread.sleep(OPERATION_LATENCY);
        verify(operationCallback, never()).onResult(any(DigitalTwinClientResult.class), any());
        verify(digitalTwinDeviceClient, never()).updateAsyncCommandStatusAsync(anyString(), any(DigitalTwinAsyncCommandUpdate.class), any(DigitalTwinCallback.class), any());
    }

    @Test
    public void getterTest() {
        assertThat(testee.getDigitalTwinInterfaceId()).isEqualTo(DIGITAL_TWIN_INTERFACE_ID);
        assertThat(testee.getDigitalTwinInterfaceInstanceName()).isEqualTo(DIGITAL_TWIN_INTERFACE_INSTANCE);
    }

    @Test
    public void onPropertyUpdateTest() {
        testee.onPropertyUpdate(propertyUpdate);
    }

    @Test
    public void onCommandReceivedTest() {
        DigitalTwinCommandResponse commandResponse = testee.onCommandReceived(commandRequest);
        assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_NOT_IMPLEMENTED);
        assertThat(commandResponse.getPayload()).isEqualTo(String.format(COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN, DIGITAL_TWIN_INTERFACE_INSTANCE));
    }

    private class DigitalTwinInterfaceClient extends AbstractDigitalTwinInterfaceClient {
        DigitalTwinInterfaceClient() {
            super(DIGITAL_TWIN_INTERFACE_INSTANCE, DIGITAL_TWIN_INTERFACE_ID);
        }
    }

}
