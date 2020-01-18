package com.microsoft.azure.sdk.iot.digitaltwin.device;

import io.reactivex.rxjava3.core.Flowable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.SdkInformationComponent.SDK_INFORMATION_COMPONENT_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DigitalTwinDeviceClient.class})
public class SdkInformationComponentTest {

    @Mock
    private DigitalTwinDeviceClient digitalTwinDeviceClient;

    @Test
    public void singletonTest() {
        assertThat(SdkInformationComponent.getInstance()).isSameAs(SdkInformationComponent.getInstance());
        assertThat(SdkInformationComponent.getInstance().getSdkInformationProperties()).isNotEmpty();
    }

    @Test
    public void readyTest() {
        SdkInformationComponent sdkInformationComponent = SdkInformationComponent.getInstance();
        when(digitalTwinDeviceClient.reportPropertiesAsync(eq(SDK_INFORMATION_COMPONENT_NAME), same(sdkInformationComponent.getSdkInformationProperties()))).thenReturn(Flowable.just(DIGITALTWIN_CLIENT_OK));
        sdkInformationComponent.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        sdkInformationComponent.ready();
        verify(digitalTwinDeviceClient).reportPropertiesAsync(eq(SDK_INFORMATION_COMPONENT_NAME), same(sdkInformationComponent.getSdkInformationProperties()));
    }

    @Test
    public void readyWithReportFailureTest() {
        SdkInformationComponent sdkInformationComponent = SdkInformationComponent.getInstance();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                return Flowable.error(new Exception("Report SDK info failed."));
            }
        }).when(digitalTwinDeviceClient)
          .reportPropertiesAsync(eq(SDK_INFORMATION_COMPONENT_NAME), same(sdkInformationComponent.getSdkInformationProperties()));
        sdkInformationComponent.setDigitalTwinDeviceClient(digitalTwinDeviceClient);
        sdkInformationComponent.ready();
    }
}
