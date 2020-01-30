// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.device;

import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinAsyncCommandUpdate;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandRequest;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinPropertyUpdate;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinReportProperty;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.functions.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinComponent.COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinComponent.STATUS_CODE_NOT_IMPLEMENTED;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_ERROR_COMPONENTS_NOT_BOUND;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(PowerMockRunner.class)
@PrepareForTest({DigitalTwinDeviceClient.class})
public class AbstractDigitalTwinComponentTest {
    private static final String DIGITAL_TWIN_COMPONENT_NAME = "DIGITAL_TWIN_COMPONENT_NAME";
    private static final String DIGITAL_TWIN_INTERFACE_ID = "DIGITAL_TWIN_INTERFACE_ID";
    private static final String TELEMETRY_PAYLOAD = "{\"TELEMETRY_NAME\": \"TELEMETRY_VALUE\"}";
    private static final int OPERATION_LATENCY = 500;

    private AbstractDigitalTwinComponent testee;
    @Mock
    private DigitalTwinDeviceClient digitalTwinDeviceClient;
    @Mock
    private List<DigitalTwinReportProperty> properties;
    @Mock
    private DigitalTwinAsyncCommandUpdate asyncCommandUpdate;
    @Mock
    private DigitalTwinPropertyUpdate propertyUpdate;
    @Mock
    private DigitalTwinCommandRequest commandRequest;
    @Mock
    private Consumer<Throwable> errorConsumer;

    @Before
    public void setUp() {
        when(digitalTwinDeviceClient.sendTelemetryAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(TELEMETRY_PAYLOAD)))
                .thenReturn(Flowable.just(DIGITALTWIN_CLIENT_OK));
        when(digitalTwinDeviceClient.reportPropertiesAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(properties)))
                .thenReturn(Flowable.just(DIGITALTWIN_CLIENT_OK));
        when(digitalTwinDeviceClient.updateAsyncCommandStatusAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(asyncCommandUpdate)))
                .thenReturn(Flowable.just(DIGITALTWIN_CLIENT_OK));
        testee = new DigitalTwinComponent();
    }


    @Test
    public void sendTelemetryTest() {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        DigitalTwinClientResult digitalTwinClientResult = testee.sendTelemetry(TELEMETRY_PAYLOAD);
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(digitalTwinDeviceClient).sendTelemetryAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(TELEMETRY_PAYLOAD));
    }

    @Test
    public void sendTelemetryAsyncTest() {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        DigitalTwinClientResult digitalTwinClientResult = testee.sendTelemetryAsync(TELEMETRY_PAYLOAD).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(digitalTwinDeviceClient).sendTelemetryAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(TELEMETRY_PAYLOAD));
    }

    @Test
    public void sendTelemetryAsyncFailureTest() {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        when(digitalTwinDeviceClient.sendTelemetryAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(TELEMETRY_PAYLOAD)))
                .thenReturn(Flowable.just(DIGITALTWIN_CLIENT_ERROR));
        DigitalTwinClientResult digitalTwinClientResult = testee.sendTelemetryAsync(TELEMETRY_PAYLOAD).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
        verify(digitalTwinDeviceClient).sendTelemetryAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(TELEMETRY_PAYLOAD));
    }

    @Test
    public void sendTelemetryAsyncExceptionTest() throws Throwable {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        Exception exception = new Exception("SendTelemetryAsyncException");
        when(digitalTwinDeviceClient.sendTelemetryAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(TELEMETRY_PAYLOAD)))
                .thenReturn(Flowable.<DigitalTwinClientResult>error(exception));
        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.sendTelemetryAsync(TELEMETRY_PAYLOAD)
                                                                    .doOnError(errorConsumer)
                                                                    .blockingGet();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Send telemetry async throw exception", e);
        }
        verify(errorConsumer).accept(exception);
        verify(digitalTwinDeviceClient).sendTelemetryAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(TELEMETRY_PAYLOAD));
    }

    @Test
    public void sendTelemetryAsyncWithoutRegisterTest() {
        DigitalTwinClientResult digitalTwinClientResult = testee.sendTelemetryAsync(TELEMETRY_PAYLOAD).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR_COMPONENTS_NOT_BOUND);
        verify(digitalTwinDeviceClient, never()).sendTelemetryAsync(anyString(), anyString());
    }

    @Test
    public void reportPropertiesTest() {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        DigitalTwinClientResult digitalTwinClientResult = testee.reportProperties(properties);
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(digitalTwinDeviceClient).reportPropertiesAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(properties));
    }

    @Test
    public void reportPropertiesAsyncTest() {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        DigitalTwinClientResult digitalTwinClientResult = testee.reportPropertiesAsync(properties).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        verify(digitalTwinDeviceClient).reportPropertiesAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(properties));
    }

    @Test
    public void reportPropertiesAsyncFailureTest() {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        when(digitalTwinDeviceClient.reportPropertiesAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(properties))).thenReturn(Flowable.just(DIGITALTWIN_CLIENT_ERROR));
        DigitalTwinClientResult digitalTwinClientResult = testee.reportPropertiesAsync(properties).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
        verify(digitalTwinDeviceClient).reportPropertiesAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(properties));
    }

    @Test
    public void reportPropertiesAsyncExceptionTest() throws Throwable {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        Exception exception = new Exception("ReportPropertiesAsyncException");
        when(digitalTwinDeviceClient.reportPropertiesAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(properties)))
                .thenReturn(Flowable.<DigitalTwinClientResult>error(exception));
        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.reportPropertiesAsync(properties)
                                                                    .doOnError(errorConsumer)
                                                                    .blockingGet();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Report properties async throw exception", e);
        }
        verify(errorConsumer).accept(exception);
        verify(digitalTwinDeviceClient).reportPropertiesAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(properties));
    }

    @Test
    public void reportPropertiesAsyncWithoutRegisterTest() throws InterruptedException {
        DigitalTwinClientResult digitalTwinClientResult = testee.reportPropertiesAsync(properties).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR_COMPONENTS_NOT_BOUND);
        Thread.sleep(OPERATION_LATENCY);
        verify(digitalTwinDeviceClient, never()).reportPropertiesAsync(anyString(), anyListOf(DigitalTwinReportProperty.class));
    }

    @Test
    public void updateAsyncCommandStatusTest() throws InterruptedException {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        DigitalTwinClientResult digitalTwinClientResult = testee.updateAsyncCommandStatus(asyncCommandUpdate);
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(OPERATION_LATENCY);
        verify(digitalTwinDeviceClient).updateAsyncCommandStatusAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(asyncCommandUpdate));
    }

    @Test
    public void updateAsyncCommandStatusAsyncTest() throws InterruptedException {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        DigitalTwinClientResult digitalTwinClientResult = testee.updateAsyncCommandStatusAsync(asyncCommandUpdate).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_OK);
        Thread.sleep(OPERATION_LATENCY);
        verify(digitalTwinDeviceClient).updateAsyncCommandStatusAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(asyncCommandUpdate));
    }

    @Test
    public void updateAsyncCommandStatusAsyncFailureTest() {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        when(digitalTwinDeviceClient.updateAsyncCommandStatusAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(asyncCommandUpdate)))
                .thenReturn(Flowable.just(DIGITALTWIN_CLIENT_ERROR));
        DigitalTwinClientResult digitalTwinClientResult = testee.updateAsyncCommandStatusAsync(asyncCommandUpdate).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR);
        verify(digitalTwinDeviceClient).updateAsyncCommandStatusAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(asyncCommandUpdate));
    }

    @Test
    public void updateAsyncCommandStatusAsyncExceptionTest() throws Throwable {
        testee.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        Exception exception = new Exception("UpdateAsyncCommandStatusAsyncException");
        when(digitalTwinDeviceClient.updateAsyncCommandStatusAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(asyncCommandUpdate)))
                .thenReturn(Flowable.<DigitalTwinClientResult>error(exception));
        try {
            DigitalTwinClientResult digitalTwinClientResult = testee.updateAsyncCommandStatusAsync(asyncCommandUpdate)
                                                                    .doOnError(errorConsumer)
                                                                    .blockingGet();
            log.debug("Unexpected result: {}", digitalTwinClientResult);
            fail("Should throw exception.");
        } catch (Throwable e) {
            log.debug("Update async command status async throw exception", e);
        }
        verify(errorConsumer).accept(exception);
        verify(digitalTwinDeviceClient).updateAsyncCommandStatusAsync(eq(DIGITAL_TWIN_COMPONENT_NAME), eq(asyncCommandUpdate));
    }

    @Test
    public void updateAsyncCommandStatusAsyncWithoutRegisterTest() throws InterruptedException {
        DigitalTwinClientResult digitalTwinClientResult = testee.updateAsyncCommandStatusAsync(asyncCommandUpdate).blockingGet();
        assertThat(digitalTwinClientResult).isEqualTo(DIGITALTWIN_CLIENT_ERROR_COMPONENTS_NOT_BOUND);
        Thread.sleep(OPERATION_LATENCY);
        verify(digitalTwinDeviceClient, never()).updateAsyncCommandStatusAsync(anyString(), any(DigitalTwinAsyncCommandUpdate.class));
    }

    @Test
    public void getterTest() {
        assertThat(testee.getDigitalTwinInterfaceId()).isEqualTo(DIGITAL_TWIN_INTERFACE_ID);
        assertThat(testee.getDigitalTwinComponentName()).isEqualTo(DIGITAL_TWIN_COMPONENT_NAME);
    }

    @Test
    public void onPropertyUpdateTest() {
        testee.onPropertyUpdate(propertyUpdate);
    }

    @Test
    public void onCommandReceivedTest() {
        DigitalTwinCommandResponse commandResponse = testee.onCommandReceived(commandRequest);
        assertThat(commandResponse.getStatus()).isEqualTo(STATUS_CODE_NOT_IMPLEMENTED);
        assertThat(commandResponse.getPayload()).isEqualTo(String.format(COMMAND_NOT_IMPLEMENTED_MESSAGE_PATTERN, DIGITAL_TWIN_COMPONENT_NAME));
    }

    private class DigitalTwinComponent extends AbstractDigitalTwinComponent {
        DigitalTwinComponent() {
            super(DIGITAL_TWIN_COMPONENT_NAME, DIGITAL_TWIN_INTERFACE_ID);
        }
    }

}
