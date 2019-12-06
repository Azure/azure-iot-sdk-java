// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service;

import com.microsoft.azure.sdk.iot.digitaltwin.service.credentials.SasTokenProviderWithSharedAccessKey;
import com.microsoft.azure.sdk.iot.digitaltwin.service.credentials.StaticSasTokenProvider;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.implementation.DigitalTwinsImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.implementation.IotHubGatewayServiceAPIs20190701PreviewImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwinCommandResponse;
import com.microsoft.rest.RestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import retrofit2.Retrofit;
import rx.Observable;

import java.io.IOException;

import static com.microsoft.azure.sdk.iot.digitaltwin.service.DigitalTwinServiceAsyncClientImplTest.createPropertyPatch;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DigitalTwinServiceAsyncClientImpl.class, RestClient.class, RestClient.Builder.class, DigitalTwinsImpl.class, IotHubGatewayServiceAPIs20190701PreviewImpl.class})
public class DigitalTwinServiceClientImplTest {
    private static final String IOTHUB_CONNECTION_STRING = "HostName=abc.azure-devices.net;SharedAccessKeyName=SHAREDACCESSKEY;SharedAccessKey=123456789=";
    private static final String DIGITAL_TWIN_ID = "someDigitalTwinId";
    private static final String MODEL_ID = "someModelId";

    private DigitalTwinServiceClientImpl testee;
    @Mock
    private DigitalTwinServiceAsyncClientImpl digitalTwinServiceAsyncClient;
    @Mock (answer = RETURNS_DEEP_STUBS)
    private RestClient.Builder builder;
    @Mock
    private RestClient restClient;

    @Before
    public void setUp() throws Exception {
        whenNew(RestClient.Builder.class).withNoArguments().thenReturn(builder);
        when(builder.build()).thenReturn(restClient);
        whenNew(IotHubGatewayServiceAPIs20190701PreviewImpl.class).withAnyArguments().thenReturn(mock(IotHubGatewayServiceAPIs20190701PreviewImpl.class));
        whenNew(DigitalTwinsImpl.class).withArguments(any(Retrofit.class), any(IotHubGatewayServiceAPIs20190701PreviewImpl.class)).thenReturn(mock(DigitalTwinsImpl.class));

        testee = DigitalTwinServiceClientImpl.buildFromConnectionString().connectionString(IOTHUB_CONNECTION_STRING).build();
        testee.setDigitalTwinServiceAsyncClient(digitalTwinServiceAsyncClient);
    }

    @Test
    public void buildServiceClintWithConnectionString() {
        // act
        DigitalTwinServiceClientImpl serviceAsyncClient = DigitalTwinServiceClientImpl.buildFromConnectionString()
                                                                                      .connectionString(IOTHUB_CONNECTION_STRING)
                                                                                      .build();

        // assert
        assertThat(serviceAsyncClient).isNotNull();
    }

    @Test
    public void buildServiceClientWithStaticSasTokenProviderAndHttpsEndpoint() {
        // arrange
        StaticSasTokenProvider sasTokenProvider = mock(StaticSasTokenProvider.class);
        String httpsEndpoint = "https://somewhere.com";

        // act
        DigitalTwinServiceClientImpl serviceAsyncClient = DigitalTwinServiceClientImpl.buildFromSasProvider()
                                                                                      .sasTokenProvider(sasTokenProvider)
                                                                                      .httpsEndpoint(httpsEndpoint)
                                                                                      .build();

        // assert
        assertThat(serviceAsyncClient).isNotNull();
    }

    @Test
    public void buildServiceClientSasTokenProviderWithSharedAccessKeyAndHttpsEndpoint() {
        // arrange
        SasTokenProviderWithSharedAccessKey sasTokenProviderWithSharedAccessKey = mock(SasTokenProviderWithSharedAccessKey.class);
        String httpsEndpoint = "https://somewhere.com";

        // act
        DigitalTwinServiceClientImpl serviceAsyncClient = DigitalTwinServiceClientImpl.buildFromSasProvider()
                                                                                      .sasTokenProvider(sasTokenProviderWithSharedAccessKey)
                                                                                      .httpsEndpoint(httpsEndpoint)
                                                                                      .build();

        // assert
        assertThat(serviceAsyncClient).isNotNull();
    }

    @Test (expected = NullPointerException.class)
    public void buildServiceClientWithSasTokenProviderWithoutHttpsEndpoint() {
        // arrange
        StaticSasTokenProvider sasTokenProvider = mock(StaticSasTokenProvider.class);

        // act
        DigitalTwinServiceClientImpl serviceAsyncClient = DigitalTwinServiceClientImpl.buildFromSasProvider()
                                                                                      .sasTokenProvider(sasTokenProvider)
                                                                                      .build();
    }

    @Test (expected = NullPointerException.class)
    public void buildServiceClientWithConnectionStringNoBuilderArgs() {
        DigitalTwinServiceClientImpl serviceAsyncClient = DigitalTwinServiceClientImpl.buildFromConnectionString().build();
    }

    @Test (expected = NullPointerException.class)
    public void buildServiceClientWithSasTokenProviderNoBuilderArgs() {
        DigitalTwinServiceClientImpl serviceClient = DigitalTwinServiceClientImpl.buildFromSasProvider().build();
    }

    @Test
    public void getDigitalTwinCallsServiceAsyncClientGetDigitalTwin() {
        // arrange
        String expectedDigitalTwin = mock(String.class);
        when(digitalTwinServiceAsyncClient.getDigitalTwin(anyString())).thenReturn(Observable.just(expectedDigitalTwin));

        // act
        String actualDigitalTwin = testee.getDigitalTwin(DIGITAL_TWIN_ID);

        // assert
        assertThat(actualDigitalTwin).isEqualTo(expectedDigitalTwin);
        verify(digitalTwinServiceAsyncClient).getDigitalTwin(eq(DIGITAL_TWIN_ID));
    }

    @Test
    public void getModelCallsServiceAsyncClientGetModel() {
        // arrange
        String expectedModel = "someModel";
        when(digitalTwinServiceAsyncClient.getModel(anyString())).thenReturn(Observable.just(expectedModel));

        // act
        String actualModel = testee.getModel(MODEL_ID);

        // assert
        assertThat(actualModel).isEqualTo(expectedModel);
        verify(digitalTwinServiceAsyncClient).getModel(eq(MODEL_ID));
    }

    @Test
    public void getModelWithExpandCallsServiceAsyncClientGetModelWithExpand() {
        // arrange
        String expectedModel = "someModel";
        boolean expand = true;
        when(digitalTwinServiceAsyncClient.getModel(anyString(), anyBoolean())).thenReturn(Observable.just(expectedModel));

        // act
        String actualModel = testee.getModel(MODEL_ID, expand);

        // assert
        assertThat(actualModel).isEqualTo(expectedModel);
        verify(digitalTwinServiceAsyncClient).getModel(eq(MODEL_ID), eq(expand));
    }

    @Test
    public void updatePropertiesCallsServiceAsyncClientUpdateProperties() throws IOException {
        // arrange
        String expectedDigitalTwin = mock(String.class);
        String interfaceInstanceName = "someInterfaceInstanceName";
        String propertyName = "somePropertyName";
        String propertyValue = "somePropertyValue";
        String propertyPatch = createPropertyPatch(propertyName, propertyValue);
        when(digitalTwinServiceAsyncClient.updateDigitalTwinProperties(anyString(), anyString(), anyString())).thenReturn(Observable.just(expectedDigitalTwin));

        // act
        String actualDigitalTwin = testee.updateDigitalTwinProperties(DIGITAL_TWIN_ID, interfaceInstanceName, propertyPatch);

        // assert
        assertThat(actualDigitalTwin).isEqualTo(expectedDigitalTwin);
        verify(digitalTwinServiceAsyncClient).updateDigitalTwinProperties(eq(DIGITAL_TWIN_ID), eq(interfaceInstanceName), eq(propertyPatch));
    }

    @Test
    public void invokeCommandWithoutArgsCallsServiceAsyncClientInvokeCommandWithArgs() {
        // arrange
        String interfaceInstanceName = "someInterfaceInstance";
        String commandName = "someCommandName";
        DigitalTwinCommandResponse expectedResult = mock(DigitalTwinCommandResponse.class);
        when(digitalTwinServiceAsyncClient.invokeCommand(anyString(), anyString(), anyString(), anyString())).thenReturn(Observable.just(expectedResult));

        // act
        DigitalTwinCommandResponse actualResult = testee.invokeCommand(DIGITAL_TWIN_ID, interfaceInstanceName, commandName);

        // assert
        assertThat(actualResult).isEqualTo(expectedResult);
        verify(digitalTwinServiceAsyncClient).invokeCommand(eq(DIGITAL_TWIN_ID), eq(interfaceInstanceName), eq(commandName), eq(null));
    }

    @Test
    public void invokeCommandWithArgumentCallsServiceAsyncClientInvokeCommandWithArgs() {
        // arrange
        String interfaceInstanceName = "someInterfaceInstance";
        String commandName = "someCommandName";
        String arguments = "someArgs";
        DigitalTwinCommandResponse expectedResult = mock(DigitalTwinCommandResponse.class);
        when(digitalTwinServiceAsyncClient.invokeCommand(anyString(), anyString(), anyString(), anyString())).thenReturn(Observable.just(expectedResult));

        // act
        DigitalTwinCommandResponse actualResult = testee.invokeCommand(DIGITAL_TWIN_ID, interfaceInstanceName, commandName, arguments);

        // assert
        assertThat(actualResult).isEqualTo(expectedResult);
        verify(digitalTwinServiceAsyncClient).invokeCommand(eq(DIGITAL_TWIN_ID), eq(interfaceInstanceName), eq(commandName), eq(arguments));
    }
}
