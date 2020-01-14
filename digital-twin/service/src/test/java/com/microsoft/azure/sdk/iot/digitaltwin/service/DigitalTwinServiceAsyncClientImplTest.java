// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.sdk.iot.digitaltwin.service.credentials.SasTokenProviderWithSharedAccessKey;
import com.microsoft.azure.sdk.iot.digitaltwin.service.credentials.StaticSasTokenProvider;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.DigitalTwins;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.implementation.DigitalTwinsImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.implementation.IotHubGatewayServiceAPIs20190701PreviewImpl;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.DigitalTwinInterfaces;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.DigitalTwinInterfacesPatch;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.DigitalTwinInterfacesPatchInterfacesValuePropertiesValue;
import com.microsoft.azure.sdk.iot.digitaltwin.service.generated.models.DigitalTwinInvokeInterfaceCommandHeaders;
import com.microsoft.azure.sdk.iot.digitaltwin.service.models.DigitalTwinCommandResponse;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceResponseWithHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import retrofit2.Retrofit;
import rx.Observable;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DigitalTwinServiceAsyncClientImpl.class, RestClient.Builder.class, RestClient.class, ServiceResponseWithHeaders.class})
public class DigitalTwinServiceAsyncClientImplTest {
    private static final String IOTHUB_CONNECTION_STRING = "HostName=abc.azure-devices.net;SharedAccessKeyName=SHAREDACCESSKEY;SharedAccessKey=123456789=";
    private static final String DIGITAL_TWIN_ID = "someDigitalTwinId";
    private static final String MODEL_ID = "someModelId";
    private static final String EMPTY_STRING = "";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private DigitalTwinServiceAsyncClientImpl testee;
    @Mock
    private DigitalTwins digitalTwin;
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

        testee = DigitalTwinServiceAsyncClientImpl.buildFromConnectionString().connectionString(IOTHUB_CONNECTION_STRING).build();
        testee.setDigitalTwin(digitalTwin);
    }

    @Test
    public void buildServiceAsyncClintWithConnectionString() {
        // act
        DigitalTwinServiceAsyncClientImpl serviceAsyncClient = DigitalTwinServiceAsyncClientImpl.buildFromConnectionString()
                                                                                                .connectionString(IOTHUB_CONNECTION_STRING)
                                                                                                .build();

        // assert
        assertThat(serviceAsyncClient).isNotNull();
    }

    @Test
    public void buildServiceAsyncClintWithConnectionStringSasToken() {
        // act
        String connectionStringWithSasToken = "HostName=abc.azure-devices.net;SharedAccessSignature=SharedAccessSignature sr=IOTHUBURI&sig=SIGNATURE&se=EXPIRY&skn=SHAREDACCESSKEYNAME";
        DigitalTwinServiceAsyncClientImpl serviceAsyncClient = DigitalTwinServiceAsyncClientImpl.buildFromConnectionString()
                .connectionString(connectionStringWithSasToken)
                .build();

        // assert
        assertThat(serviceAsyncClient).isNotNull();
    }

    @Test
    public void buildServiceAsyncClientWithStaticSasTokenProviderAndHttpsEndpoint() {
        // arrange
        StaticSasTokenProvider sasTokenProvider = mock(StaticSasTokenProvider.class);
        String httpsEndpoint = "https://somewhere.com";

        // act
        DigitalTwinServiceAsyncClientImpl serviceAsyncClient = DigitalTwinServiceAsyncClientImpl.buildFromSasProvider()
                .sasTokenProvider(sasTokenProvider)
                .httpsEndpoint(httpsEndpoint)
                .build();

        // assert
        assertThat(serviceAsyncClient).isNotNull();
    }

    @Test
    public void buildServiceAsyncClientSasTokenProviderWithSharedAccessKeyAndHttpsEndpoint() {
        // arrange
        SasTokenProviderWithSharedAccessKey sasTokenProviderWithSharedAccessKey = mock(SasTokenProviderWithSharedAccessKey.class);
        String httpsEndpoint = "https://somewhere.com";

        // act
        DigitalTwinServiceAsyncClientImpl serviceAsyncClient = DigitalTwinServiceAsyncClientImpl.buildFromSasProvider()
                .sasTokenProvider(sasTokenProviderWithSharedAccessKey)
                .httpsEndpoint(httpsEndpoint)
                .build();

        // assert
        assertThat(serviceAsyncClient).isNotNull();
    }

    @Test (expected = NullPointerException.class)
    public void buildServiceAsyncClientWithSasTokenProviderWithoutHttpsEndpoint() {
        // arrange
        StaticSasTokenProvider sasTokenProvider = mock(StaticSasTokenProvider.class);

        // act
        DigitalTwinServiceAsyncClientImpl serviceAsyncClient = DigitalTwinServiceAsyncClientImpl.buildFromSasProvider()
                .sasTokenProvider(sasTokenProvider)
                .build();
    }

    @Test (expected = NullPointerException.class)
    public void buildServiceAsyncClientWithConnectionStringNoBuilderArgs() {
        DigitalTwinServiceAsyncClientImpl serviceAsyncClient = DigitalTwinServiceAsyncClientImpl.buildFromConnectionString().build();
    }

    @Test (expected = NullPointerException.class)
    public void buildServiceAsyncClientWithSasTokenProviderNoBuilderArgs() {
        DigitalTwinServiceAsyncClientImpl serviceAsyncClient = DigitalTwinServiceAsyncClientImpl.buildFromSasProvider().build();
    }

    @Test
    public void getDigitalTwinCallsDigitalTwinsGetInterfaces() throws JsonProcessingException {
        // arrange
        DigitalTwinInterfaces digitalTwinInterfaces = new DigitalTwinInterfaces();
        when(digitalTwin.getInterfacesAsync(anyString())).thenReturn(Observable.just(digitalTwinInterfaces));
        String expectedDigitalTwin = objectMapper.writeValueAsString(digitalTwinInterfaces);

        // act
        String actualDigitalTwin = testee.getDigitalTwin(DIGITAL_TWIN_ID).toBlocking().single();

        // assert
        assertThat(actualDigitalTwin).isEqualTo(expectedDigitalTwin);
        verify(digitalTwin).getInterfacesAsync(eq(DIGITAL_TWIN_ID));
    }

    @Test
    public void getModelCallsDigitalTwinsGetModel() throws JsonProcessingException {
        // arrange
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.put("someModel", "someValue");
        when(digitalTwin.getDigitalTwinModelAsync(anyString())).thenReturn(Observable.just(objectNode));
        String expectedModel = objectMapper.writeValueAsString(objectNode);

        // act
        String actualModel = testee.getModel(MODEL_ID).toBlocking().single();

        // assert
        assertThat(actualModel).isEqualTo(expectedModel);
        verify(digitalTwin).getDigitalTwinModelAsync(eq(MODEL_ID));
    }

    @Test
    public void getModelWithExpandCallsDigitalTwinGetModelWithExpand() throws JsonProcessingException {
        // arrange
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.put("someModel", "someValue");
        boolean expand = true;
        when(digitalTwin.getDigitalTwinModelAsync(anyString(), anyBoolean())).thenReturn(Observable.just(objectNode));
        String expectedModel = objectMapper.writeValueAsString(objectNode);

        // act
        String actualModel = testee.getModel(MODEL_ID, expand).toBlocking().single();

        // assert
        assertThat(actualModel).isEqualTo(expectedModel);
        verify(digitalTwin).getDigitalTwinModelAsync(eq(MODEL_ID), eq(expand));
    }

    @Test
    public void updatePropertiesCallsDigitalTwinUpdateInterfaces() throws IOException {
        // arrange
        DigitalTwinInterfaces digitalTwinInterfaces = new DigitalTwinInterfaces();
        String interfaceInstanceName = "someInterfaceInstanceName";
        String propertyName = "somePropertyName";
        String propertyValue = "somePropertyValue";
        String propertyPatch = createPropertyPatch(propertyName, propertyValue);

        when(digitalTwin.updateInterfacesAsync(anyString(), any(DigitalTwinInterfacesPatch.class))).thenReturn(Observable.just(digitalTwinInterfaces));
        String expectedDigitalTwin = objectMapper.writeValueAsString(digitalTwinInterfaces);
        ArgumentCaptor<DigitalTwinInterfacesPatch> argumentCaptor = ArgumentCaptor.forClass(DigitalTwinInterfacesPatch.class);

        // act
        String actualDigitalTwin = testee.updateDigitalTwinProperties(DIGITAL_TWIN_ID, interfaceInstanceName, propertyPatch).toBlocking().single();

        // assert
        assertThat(actualDigitalTwin).isEqualTo(expectedDigitalTwin);
        verify(digitalTwin).updateInterfacesAsync(anyString(), argumentCaptor.capture());
        DigitalTwinInterfacesPatch digitalTwinInterfacesPatch = argumentCaptor.getValue();
        assertThat(digitalTwinInterfacesPatch.interfaces()).containsOnlyKeys(interfaceInstanceName);
        Map<String, DigitalTwinInterfacesPatchInterfacesValuePropertiesValue> properties = digitalTwinInterfacesPatch.interfaces().get(interfaceInstanceName).properties();
        assertThat(properties).containsOnlyKeys(propertyName);
        assertThat(properties.get(propertyName).desired().value()).isEqualTo(propertyValue);
    }

    @Test
    public void invokeCommandWithoutArgsCallsDigitalTwinInvokeInterfaceCommand() throws Exception {
        // arrange
        String interfaceInstanceName = "someInterfaceInstance";
        String commandName = "someCommandName";
        DigitalTwinCommandResponse expectedResult = mock(DigitalTwinCommandResponse.class);
        ServiceResponseWithHeaders responseWithHeaders = mock(ServiceResponseWithHeaders.class);
        DigitalTwinInvokeInterfaceCommandHeaders commandHeaders = mock(DigitalTwinInvokeInterfaceCommandHeaders.class);
        when(responseWithHeaders.headers()).thenReturn(commandHeaders);
        when(responseWithHeaders.body()).thenReturn(mock(String.class));
        when(commandHeaders.xMsCommandStatuscode()).thenReturn(mock(Integer.class));
        when(commandHeaders.xMsRequestId()).thenReturn(mock(String.class));
        whenNew(DigitalTwinCommandResponse.class).withAnyArguments().thenReturn(expectedResult);
        when(digitalTwin.invokeInterfaceCommandWithServiceResponseAsync(anyString(), anyString(), anyString(), anyString(), eq(null), eq(null))).thenReturn(Observable.just(responseWithHeaders));

        // act
        DigitalTwinCommandResponse actualResult = testee.invokeCommand(DIGITAL_TWIN_ID, interfaceInstanceName, commandName).toBlocking().single();

        // assert
        assertThat(actualResult).isEqualTo(expectedResult);
        verify(digitalTwin).invokeInterfaceCommandWithServiceResponseAsync(eq(DIGITAL_TWIN_ID), eq(interfaceInstanceName), eq(commandName), eq(EMPTY_STRING), eq(null), eq(null));
    }

    @Test
    public void invokeCommandWithArgsCallsDigitalTwinInvokeInterfaceCommand() throws Exception {
        // arrange
        String interfaceInstanceName = "someInterfaceInstance";
        String commandName = "someCommandName";
        String arguments = "\"someArgs\"";
        DigitalTwinCommandResponse expectedResult = mock(DigitalTwinCommandResponse.class);
        ServiceResponseWithHeaders responseWithHeaders = mock(ServiceResponseWithHeaders.class);
        DigitalTwinInvokeInterfaceCommandHeaders commandHeaders = mock(DigitalTwinInvokeInterfaceCommandHeaders.class);
        when(responseWithHeaders.headers()).thenReturn(commandHeaders);
        when(responseWithHeaders.body()).thenReturn(mock(String.class));
        when(commandHeaders.xMsCommandStatuscode()).thenReturn(mock(Integer.class));
        when(commandHeaders.xMsRequestId()).thenReturn(mock(String.class));
        whenNew(DigitalTwinCommandResponse.class).withAnyArguments().thenReturn(expectedResult);
        when(digitalTwin.invokeInterfaceCommandWithServiceResponseAsync(anyString(), anyString(), anyString(), anyString(), eq(null), eq(null))).thenReturn(Observable.just(responseWithHeaders));

        // act
        DigitalTwinCommandResponse actualResult = testee.invokeCommand(DIGITAL_TWIN_ID, interfaceInstanceName, commandName, arguments).toBlocking().single();

        // assert
        assertThat(actualResult).isEqualTo(expectedResult);
        verify(digitalTwin).invokeInterfaceCommandWithServiceResponseAsync(eq(DIGITAL_TWIN_ID), eq(interfaceInstanceName), eq(commandName), eq(arguments), eq(null), eq(null));
    }

    static String createPropertyPatch(String propertyName, String propertyValue) {
        return "    { "
                + "      \"properties\": {"
                + "          \"" + propertyName + "\": {"
                + "              \"desired\": {"
                + "                  \"value\": \"" + propertyValue + "\""
                + "              }"
                + "          }"
                + "     } "
                + "  }";
    }
}
