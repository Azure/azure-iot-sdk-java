/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.twin.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.transport.ExponentialBackoffWithJitter;
import com.microsoft.azure.sdk.iot.device.transport.RetryPolicy;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for InternalClient.java
 * Methods: 89%
 * Lines: 93%
 */
public class InternalClientTest
{
    private static final Object NULL_OBJECT = null;

    @Mocked
    MessageSentCallback mockedMessageSentCallback;

    @Mocked
    ClientConfiguration mockConfig;

    @Mocked
    IotHubConnectionString mockIotHubConnectionString;

    @Mocked
    DeviceIO mockDeviceIO;

    @Mocked
    FileUpload mockFileUpload;

    @Mocked
    IotHubSasTokenAuthenticationProvider mockIotHubSasTokenAuthenticationProvider;

    @Mocked
    IotHubAuthenticationProvider mockIotHubAuthenticationProvider;

    @Mocked
    SecurityProvider mockSecurityProvider;

    @Mocked
    IotHubConnectionStatusChangeCallback mockedIotHubConnectionStatusChangeCallback;

    @Mocked
    ProductInfo mockedProductInfo;

    @Mocked
    ProxySettings mockProxySettings;

    /* Tests_SRS_INTERNALCLIENT_21_004: [If the connection string is null or empty, the function shall throw an IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullConnectionStringThrows() throws URISyntaxException, IOException
    {
        //arrange
        final IotHubConnectionString mockIotHubConnectionString = null;
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        // act
        Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class}, mockIotHubConnectionString, protocol);
    }

    //Tests_SRS_INTERNALCLIENT_34_065: [The provided uri and device id will be used to create an iotHubConnectionString that will be saved in config.]
    //Tests_SRS_INTERNALCLIENT_34_066: [The provided security provider will be saved in config.]
    //Tests_SRS_INTERNALCLIENT_34_067: [The constructor shall initialize the IoT Hub transport for the protocol specified, creating a instance of the deviceIO.]
    @Test
    public void createFromSecurityProviderUsesUriAndDeviceIdAndSavesSecurityProviderAndCreatesDeviceIO() throws URISyntaxException, SecurityProviderException, IOException
    {
        //arrange
        final String expectedUri = "some uri";
        final String expectedDeviceId = "some device id";
        final IotHubClientProtocol expectedProtocol = IotHubClientProtocol.HTTPS;
        new StrictExpectations()
        {
            {
                new IotHubConnectionString(expectedUri, expectedDeviceId, null, null);
                result = mockIotHubConnectionString;
            }
        };

        //act
        new DeviceClient(expectedUri, expectedDeviceId, mockSecurityProvider, expectedProtocol);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(ClientConfiguration.class, new Class[] {IotHubConnectionString.class, SecurityProvider.class, IotHubClientProtocol.class, ClientOptions.class}, mockIotHubConnectionString, mockSecurityProvider, (IotHubClientProtocol) any, null);
                times = 1;

                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                        new Class[] {ClientConfiguration.class},
                        any);
                times = 1;
            }
        };
    }

    //Tests_SRS_INTERNALCLIENT_34_065: [The provided uri and device id will be used to create an iotHubConnectionString that will be saved in config.]
    //Tests_SRS_INTERNALCLIENT_34_066: [The provided security provider will be saved in config.]
    //Tests_SRS_INTERNALCLIENT_34_067: [The constructor shall initialize the IoT Hub transport for the protocol specified, creating a instance of the deviceIO.]
    @Test
    public void createFromSecurityProviderWithClientOptionsUsesUriAndDeviceIdAndSavesSecurityProviderAndCreatesDeviceIO() throws URISyntaxException, SecurityProviderException, IOException
    {
        //arrange
        final String expectedUri = "some uri";
        final String expectedDeviceId = "some device id";
        final IotHubClientProtocol expectedProtocol = IotHubClientProtocol.HTTPS;
        new StrictExpectations()
        {
            {
                new IotHubConnectionString(expectedUri, expectedDeviceId, null, null);
                result = mockIotHubConnectionString;
            }
        };

        final ClientOptions clientOptions = ClientOptions.builder().build();
        //act
        final DeviceClient dc = new DeviceClient(expectedUri, expectedDeviceId, mockSecurityProvider, expectedProtocol, clientOptions);

        new Verifications()
        {
            {
                Deencapsulation.newInstance(ClientConfiguration.class, new Class[] {IotHubConnectionString.class, SecurityProvider.class, IotHubClientProtocol.class, ClientOptions.class}, mockIotHubConnectionString, mockSecurityProvider, (IotHubClientProtocol) any, clientOptions);
                times = 1;
            }
        };
    }

    //Tests_SRS_INTERNALCLIENT_34_073: [If the provided securityProvider is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void createFromSecurityProviderThrowForNullSecurityProvider() throws URISyntaxException, SecurityProviderException, IOException
    {
        //arrange
        final String expectedUri = "some uri";
        final String expectedDeviceId = "some device id";
        final IotHubClientProtocol expectedProtocol = IotHubClientProtocol.HTTPS;

        //act
        Deencapsulation.newInstance(InternalClient.class, new Class[] {String.class, String.class, SecurityProvider.class, IotHubClientProtocol.class}, expectedUri, expectedDeviceId, null, expectedProtocol, 1, 1);
    }

    //Tests_SRS_INTERNALCLIENT_34_074: [If the provided uri is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void createFromSecurityProviderThrowForNullUri() throws URISyntaxException, SecurityProviderException, IOException
    {
        //arrange
        final String expectedUri = "some uri";
        final String expectedDeviceId = "some device id";
        final IotHubClientProtocol expectedProtocol = IotHubClientProtocol.HTTPS;

        //act
        Deencapsulation.newInstance(InternalClient.class, new Class[] {String.class, String.class, SecurityProvider.class, IotHubClientProtocol.class}, null, expectedDeviceId, mockSecurityProvider, expectedProtocol, 1, 1);
    }

    //Tests_SRS_INTERNALCLIENT_34_075: [If the provided deviceId is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void createFromSecurityProviderThrowForNullDeviceId() throws URISyntaxException, SecurityProviderException, IOException
    {
        //arrange
        final String expectedUri = "some uri";
        final String expectedDeviceId = "some device id";
        final IotHubClientProtocol expectedProtocol = IotHubClientProtocol.HTTPS;

        //act
        Deencapsulation.newInstance(InternalClient.class, new Class[] {String.class, String.class, SecurityProvider.class, IotHubClientProtocol.class}, expectedUri, null, mockSecurityProvider, expectedProtocol, 1, 1);
    }

    //Tests_SRS_INTERNALCLIENT_34_072: [If the provided protocol is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void createFromSecurityProviderThrowForNullProtocol() throws URISyntaxException, SecurityProviderException, IOException
    {
        //arrange
        final String expectedUri = "some uri";
        final String expectedDeviceId = "some device id";

        //act
        Deencapsulation.newInstance(InternalClient.class, new Class[] {String.class, String.class, SecurityProvider.class, IotHubClientProtocol.class}, expectedUri, expectedDeviceId, mockSecurityProvider, null, 1, 1);
    }
    
    /* Tests_SRS_INTERNALCLIENT_21_005: [If protocol is null, the function shall throw an IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullProtocolThrows() throws URISyntaxException, IOException
    {
        //arrange
        final IotHubClientProtocol protocol = null;

        // act
        Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class}, mockIotHubConnectionString, protocol);
    }

    /* Tests_SRS_INTERNALCLIENT_21_002: [The constructor shall initialize the IoT Hub transport for the protocol specified, creating a instance of the deviceIO.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorBadDeviceIOThrows() throws URISyntaxException, IOException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        // act
        Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class}, mockIotHubConnectionString, protocol);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubConnectionString.class, mockIotHubConnectionString);
                times = 1;
                Deencapsulation.newInstance(ClientConfiguration.class, any, ClientConfiguration.AuthType.SAS_TOKEN);
                times = 1;
                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                        new Class[] {ClientConfiguration.class, IotHubClientProtocol.class},
                        any, protocol);
                times = 1;
            }
        };
    }

    /* Tests_SRS_INTERNALCLIENT_21_003: [The constructor shall save the connection configuration using the object ClientConfiguration.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorBadDeviceClientConfigThrows() throws URISyntaxException, IOException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        // act
        Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class}, mockIotHubConnectionString, protocol);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(ClientConfiguration.class, any, ClientConfiguration.AuthType.SAS_TOKEN);
                times = 1;
                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                        new Class[] {ClientConfiguration.class, IotHubClientProtocol.class},
                        any, protocol);
                times = 0;
            }
        };
    }

    /* Tests_SRS_INTERNALCLIENT_21_006: [The open shall open the deviceIO connection.] */
    @Test
    public void openOpensDeviceIOSuccess() throws IOException, URISyntaxException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, ClientOptions.class}, mockIotHubConnectionString, protocol, null);

        // act
        Deencapsulation.invoke(client, "open", false);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                        new Class[] {ClientConfiguration.class},
                        any);
                times = 1;

                Deencapsulation.invoke(mockDeviceIO, "open", false);
                times = 1;
            }
        };
    }

    /* Tests_SRS_INTERNALCLIENT_21_008: [The close shall close the deviceIO connection.] */
    @Test
    public void closeClosesTransportSuccess() throws IOException, URISyntaxException, IotHubClientException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, ClientOptions.class}, mockIotHubConnectionString, protocol, null);
        client.open(false);

        // act
        Deencapsulation.invoke(client, "close");

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.close();
                times = 1;
            }
        };
    }

    /* Tests_SRS_INTERNALCLIENT_21_009: [If the closing a connection via deviceIO is not successful, the close shall throw IOException.] */
    @Test
    public void closeBadCloseTransportThrows() throws IOException, URISyntaxException, IotHubClientException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.close();
                result = new IOException();
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, ClientOptions.class}, mockIotHubConnectionString, protocol, null);
        client.open(false);

        // act
        try
        {
            Deencapsulation.invoke(client, "close");
        }
        catch (Exception expected)
        {
            // Don't do anything, throw expected.
        }

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.close();
                times = 1;
            }
        };
    }

    /* Tests_SRS_INTERNALCLIENT_21_010: [The sendEventAsync shall asynchronously send the message using the deviceIO connection.] */
    @Test
    public void sendEventAsyncSendsSuccess(
            @Mocked final Message mockMessage,
            @Mocked final MessageSentCallback mockCallback)
            throws IOException, URISyntaxException, IotHubClientException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final Map<String, Object> context = new HashMap<>();
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, ClientOptions.class}, mockIotHubConnectionString, protocol, null);
        Deencapsulation.setField(client, "config", mockConfig);
        client.open(false);

        // act
        Deencapsulation.invoke(client, "sendEventAsync", mockMessage, mockCallback, context);

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.sendEventAsync(mockMessage, mockCallback, context, mockConfig.getDeviceId());
                times = 1;
            }
        };
    }

    //Tests_SRS_INTERNALCLIENT_34_045: [This function shall set the provided message's connection device id to the config's saved device id.]
    @Test
    public void sendEventAsyncSetsConnectionDeviceId(
            @Mocked final Message mockMessage,
            @Mocked final MessageSentCallback mockCallback)
            throws IOException, URISyntaxException, IotHubClientException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final Map<String, Object> context = new HashMap<>();
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, ClientOptions.class}, mockIotHubConnectionString, protocol, null);
        Deencapsulation.setField(client, "config", mockConfig);
        client.open(false);
        final String expectedDeviceId = "some device";
        new NonStrictExpectations()
        {
            {
                mockConfig.getDeviceId();
                result = expectedDeviceId;
            }
        };

        // act
        Deencapsulation.invoke(client, "sendEventAsync", mockMessage, mockCallback, context);

        // assert
        new Verifications()
        {
            {
                mockMessage.setConnectionDeviceId(expectedDeviceId);
                times = 1;
            }
        };
    }

    /* Tests_SRS_INTERNALCLIENT_21_011: [If starting to send via deviceIO is not successful, the sendEventAsync shall bypass the threw exception.] */
    // Tests_SRS_INTERNALCLIENT_12_001: [The function shall call deviceIO.sendEventAsync with the client's config parameter to enable multiplexing.]
    @Test
    public void sendEventAsyncBadSendThrows(
            @Mocked final Message mockMessage,
            @Mocked final MessageSentCallback mockCallback)
            throws IOException, URISyntaxException, IotHubClientException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final Map<String, Object> context = new HashMap<>();
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.sendEventAsync(mockMessage, mockCallback, context, null);
                result = new IllegalStateException();
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, ClientOptions.class}, mockIotHubConnectionString, protocol, null);
        Deencapsulation.setField(client, "config", mockConfig);
        client.open(false);

        // act
        try
        {
            Deencapsulation.invoke(client, "sendEventAsync", mockMessage, mockCallback, context);
        }
        catch (IllegalStateException expected)
        {
            // Don't do anything, throw expected.
        }

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.sendEventAsync(mockMessage, mockCallback, context, mockConfig.getDeviceId());
                times = 1;
            }
        };
    }

    // Tests_SRS_INTERNALCLIENT_11_013: [The function shall set the message callback, with its associated context.]
    // Tests_SRS_INTERNALCLIENT_12_001: [The function shall call deviceIO.sendEventAsync with the client's config parameter to enable multiplexing.]
    @Test
    public void setMessageCallbackSetsMessageCallback(
            @Mocked final MessageCallback mockCallback)
            throws IOException, URISyntaxException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final Map<String, Object> context = new HashMap<>();
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, ClientOptions.class}, mockIotHubConnectionString, protocol, null);

        // act
        Deencapsulation.invoke(client, "setMessageCallbackInternal", mockCallback, context);

        // assert
        new Verifications()
        {
            {
                mockConfig.setMessageCallback(mockCallback, context);
                times = 1;
            }
        };
    }

    // Tests_SRS_INTERNALCLIENT_11_014: [If the callback is null but the context is non-null, the function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setMessageCallbackRejectsNullCallbackAndNonnullContext()
            throws IOException, URISyntaxException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final Map<String, Object> context = new HashMap<>();
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class}, mockIotHubConnectionString, protocol);

        // act
        Deencapsulation.invoke(client, "setMessageCallback", new Class[] {MessageCallback.class, Object.class}, null, context);
    }

    /*
    Tests_SRS_INTERNALCLIENT_25_038: [**This method shall subscribe to device methods by calling subscribeToMethodsAsync on DirectMethod object which it created.**]**
     */
    @Test
    public void subscribeToDeviceMethodSucceeds(@Mocked final SubscriptionAcknowledgedCallback mockedStatusCB,
                                                @Mocked final MethodCallback mockedDeviceMethodCB,
                                                @Mocked final DirectMethod mockedMethod) throws IOException, URISyntaxException, IotHubClientException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        final InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, ClientOptions.class}, mockIotHubConnectionString, protocol, null);
        client.open(false);

        //act
        client.subscribeToMethodsAsync(mockedDeviceMethodCB, NULL_OBJECT, mockedStatusCB, NULL_OBJECT);

        //assert
        new Verifications()
        {
            {
                mockedMethod.subscribeToDirectMethods(mockedDeviceMethodCB, any);
                times = 1;
            }
        };

    }

    /*
    Tests_SRS_INTERNALCLIENT_25_036: [**If the client has not been open, the function shall throw an IOException.**]**
     */
    @Test (expected = IllegalStateException.class)
    public void subscribeToDeviceMethodThrowsIfClientNotOpen(@Mocked final SubscriptionAcknowledgedCallback mockedStatusCB,
                                                             @Mocked final MethodCallback mockedDeviceMethodCB)
            throws IOException, URISyntaxException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
            }
        };
        final InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, ClientOptions.class}, mockIotHubConnectionString, protocol, null);

        //act
        client.subscribeToMethodsAsync(mockedDeviceMethodCB, NULL_OBJECT, mockedStatusCB, NULL_OBJECT);
    }

    /*
    Tests_SRS_INTERNALCLIENT_25_037: [**If deviceMethodCallback or deviceMethodStatusCallback is null, the function shall throw an IllegalArgumentException.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void subscribeToDeviceMethodThrowsIfDeviceMethodCallbackNull(@Mocked final MessageSentCallback mockedStatusCB)
            throws IOException, URISyntaxException, IotHubClientException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        final InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class}, mockIotHubConnectionString, protocol);
        client.open(false);

        //act
        Deencapsulation.invoke(client, "subscribeToMethodsAsync", new Class[] {MethodCallback.class, Object.class, MessageSentCallback.class, Object.class}, null, null, mockedStatusCB, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void subscribeToDeviceMethodThrowsIfDeviceMethodStatusCallbackNull(@Mocked final MethodCallback mockedDeviceMethodCB)
            throws IOException, URISyntaxException, IotHubClientException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        final InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class}, mockIotHubConnectionString, protocol);
        client.open(false);

        //act
        Deencapsulation.invoke(client, "subscribeToMethodsAsync", mockedDeviceMethodCB, null, null, null);
    }

    /*
    Tests_SRS_INTERNALCLIENT_25_039: [**This method shall update the deviceMethodCallback if called again, but it shall not subscribe twice.**]**
     */
    @Test
    public void subscribeToDeviceMethodWorksEvenWhenCalledTwice(@Mocked final SubscriptionAcknowledgedCallback mockedStatusCB,
                                                                @Mocked final MethodCallback mockedDeviceMethodCB,
                                                                @Mocked final DirectMethod mockedMethod) throws IOException, URISyntaxException, IotHubClientException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        final InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, ClientOptions.class}, mockIotHubConnectionString, protocol, null);
        client.open(false);
        client.subscribeToMethodsAsync(mockedDeviceMethodCB, NULL_OBJECT, mockedStatusCB, NULL_OBJECT);

        // act
        client.subscribeToMethodsAsync(mockedDeviceMethodCB, NULL_OBJECT, mockedStatusCB, NULL_OBJECT);

        // assert
        new Verifications()
        {
            {
                mockedMethod.subscribeToDirectMethods(mockedDeviceMethodCB, any);
                times = 2;
            }
        };
    }

    //Tests_SRS_INTERNALCLIENT_99_003: [If the callback is null the method shall throw an IllegalArgument exception.]
    @Test (expected = IllegalArgumentException.class)
    public void registerConnectionStateCallbackNullCallback()
            throws IllegalArgumentException, URISyntaxException, IOException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        final InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class}, mockIotHubConnectionString, protocol);

        //act
        Deencapsulation.invoke(client, "registerConnectionStateCallback", null, null);
    }

    // Tests_SRS_INTERNALCLIENT_12_028: [The constructor shall shall set the config, deviceIO and tranportClient to null.]
    @Test
    public void unusedConstructor()
    {
        // act
        InternalClient client = Deencapsulation.newInstance(InternalClient.class);

        // assert
        assertNull(Deencapsulation.getField(client, "config"));
        assertNull(Deencapsulation.getField(client, "deviceIO"));
    }

    //Tests_SRS_INTERNALCLIENT_28_001: [The function shall set the device config's RetryPolicy .]
    @Test
    public void setRetryPolicySetPolicy() throws URISyntaxException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, ClientOptions.class}, mockIotHubConnectionString, protocol, null);
        Deencapsulation.setField(client, "config", mockConfig);

        //act
        Deencapsulation.invoke(client, "setRetryPolicy", new ExponentialBackoffWithJitter());

        //assert
        new Verifications()
        {
            {
                mockConfig.setRetryPolicy((RetryPolicy) any);
                times = 1;
            }
        };
    }

    // Tests_SRS_INTERNALCLIENT_34_070: [The function shall set the device config's operation timeout .]
    @Test
    public void setDeviceOperationTimeoutSetsConfig() throws URISyntaxException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, ClientOptions.class}, mockIotHubConnectionString, protocol, null);
        final long expectedTimeout = 1034;
        Deencapsulation.setField(client, "config", mockConfig);

        //act
        Deencapsulation.invoke(client, "setOperationTimeout", expectedTimeout);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockConfig, "setOperationTimeout", expectedTimeout);
                times = 1;
            }
        };
    }

    // Tests_SRS_INTERNALCLIENT_34_071: [This function shall return the product info saved in config.]
    @Test
    public void getProductInfoFetchesFromConfig() throws URISyntaxException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, ClientOptions.class}, mockIotHubConnectionString, protocol, null);

        new StrictExpectations()
        {
            {
                mockConfig.getProductInfo();
                result = mockedProductInfo;
            }
        };

        //act
        ProductInfo productInfo = Deencapsulation.invoke(client, "getProductInfo"); 

        //assert
        assertEquals(mockedProductInfo, productInfo);
    }
}
