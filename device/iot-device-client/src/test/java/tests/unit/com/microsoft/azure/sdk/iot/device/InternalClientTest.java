/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUpload;
import com.microsoft.azure.sdk.iot.device.transport.ExponentialBackoffWithJitter;
import com.microsoft.azure.sdk.iot.device.transport.RetryPolicy;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import mockit.*;
import org.junit.Test;

import java.io.IOError;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for InternalClient.java
 * Methods: 89%
 * Lines: 93%
 */
public class InternalClientTest
{
    private static final long SEND_PERIOD = 10;
    private static final long RECEIVE_PERIOD = 10;
    private static final Object NULL_OBJECT = null;

    @Mocked
    IotHubEventCallback mockedIotHubEventCallback;

    @Mocked
    TwinPropertyCallBack mockedTwinPropertyCallback;

    @Mocked
    DeviceClientConfig mockConfig;

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

    private static long SEND_PERIOD_MILLIS = 10L;
    private static long RECEIVE_PERIOD_MILLIS_AMQPS = 10L;
    private static long RECEIVE_PERIOD_MILLIS_HTTPS = 25*60*1000; /*25 minutes*/

    /* Tests_SRS_INTERNALCLIENT_21_004: [If the connection string is null or empty, the function shall throw an IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullConnectionStringThrows() throws URISyntaxException, IOException
    {
        //arrange
        final IotHubConnectionString mockIotHubConnectionString = null;
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        // act
        Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
    }

    // Tests_SRS_INTERNALCLIENT_34_078: [If the connection string or protocol is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void x509ConstructorNullConnectionStringThrows() throws URISyntaxException, IOException
    {
        //arrange
        final String mockIotHubConnectionString = null;
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        // act
        Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, String.class, boolean.class, String.class, boolean.class, long.class, long.class}, mockIotHubConnectionString, protocol, "any cert", false, "any key", false, SEND_PERIOD, RECEIVE_PERIOD);
    }

    // Tests_SRS_INTERNALCLIENT_34_078: [If the connection string or protocol is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void x509ConstructorNullProtocolThrows() throws URISyntaxException, IOException
    {
        //arrange
        final String mockIotHubConnectionString = "some connection string";
        final IotHubClientProtocol protocol = null;

        // act
        Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, String.class, boolean.class, String.class, boolean.class, long.class, long.class}, mockIotHubConnectionString, protocol, "any cert", false, "any key", false, SEND_PERIOD, RECEIVE_PERIOD);
    }

    // Tests_SRS_INTERNALCLIENT_34_079: [This function shall save a new config using the provided connection string, and x509 certificate information.]
    // Tests_SRS_INTERNALCLIENT_34_080: [This function shall save a new DeviceIO instance using the created config and the provided send/receive periods.]
    @Test
    public void x509ConstructorCreatesConfigAndDeviceIO() throws URISyntaxException, IOException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String publicCert = "any cert";
        final String privateKey = "any key";

        // act
        Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, String.class, boolean.class, String.class, boolean.class, long.class, long.class}, mockIotHubConnectionString, protocol, publicCert, false, privateKey, false, SEND_PERIOD, RECEIVE_PERIOD);

        new Verifications()
        {
            {
                Deencapsulation.newInstance(DeviceClientConfig.class, new Class[] {IotHubConnectionString.class, String.class, boolean.class, String.class, boolean.class}, mockIotHubConnectionString, publicCert, false, privateKey, false);
                times = 1;

                Deencapsulation.newInstance(DeviceIO.class, new Class[] {DeviceClientConfig.class, long.class, long.class}, any, SEND_PERIOD, RECEIVE_PERIOD);
                times = 1;
            }
        };
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
        DeviceClient.createFromSecurityProvider(expectedUri, expectedDeviceId, mockSecurityProvider, expectedProtocol);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(DeviceClientConfig.class, new Class[] {IotHubConnectionString.class, SecurityProvider.class}, mockIotHubConnectionString, mockSecurityProvider);
                times = 1;

                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                        new Class[] {DeviceClientConfig.class, long.class, long.class},
                        any, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_HTTPS);
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
        Deencapsulation.newInstance(InternalClient.class, new Class[] {String.class, String.class, SecurityProvider.class, IotHubClientProtocol.class, long.class, long.class}, expectedUri, expectedDeviceId, null, expectedProtocol, 1, 1);
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
        Deencapsulation.newInstance(InternalClient.class, new Class[] {String.class, String.class, SecurityProvider.class, IotHubClientProtocol.class, long.class, long.class}, null, expectedDeviceId, mockSecurityProvider, expectedProtocol, 1, 1);
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
        Deencapsulation.newInstance(InternalClient.class, new Class[] {String.class, String.class, SecurityProvider.class, IotHubClientProtocol.class, long.class, long.class}, expectedUri, null, mockSecurityProvider, expectedProtocol, 1, 1);
    }

    //Tests_SRS_INTERNALCLIENT_34_072: [If the provided protocol is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void createFromSecurityProviderThrowForNullProtocol() throws URISyntaxException, SecurityProviderException, IOException
    {
        //arrange
        final String expectedUri = "some uri";
        final String expectedDeviceId = "some device id";

        //act
        Deencapsulation.newInstance(InternalClient.class, new Class[] {String.class, String.class, SecurityProvider.class, IotHubClientProtocol.class, long.class, long.class}, expectedUri, expectedDeviceId, mockSecurityProvider, null, 1, 1);
    }
    
    /* Tests_SRS_INTERNALCLIENT_21_005: [If protocol is null, the function shall throw an IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullProtocolThrows() throws URISyntaxException, IOException
    {
        //arrange
        final IotHubClientProtocol protocol = null;

        // act
        Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
    }

    /* Tests_SRS_INTERNALCLIENT_21_002: [The constructor shall initialize the IoT Hub transport for the protocol specified, creating a instance of the deviceIO.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorBadDeviceIOThrows() throws URISyntaxException, IOException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        // act
        Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubConnectionString.class, mockIotHubConnectionString);
                times = 1;
                Deencapsulation.newInstance(DeviceClientConfig.class, (IotHubConnectionString)any, DeviceClientConfig.AuthType.SAS_TOKEN);
                times = 1;
                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                        new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                        (DeviceClientConfig)any, protocol, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);
                times = 1;
            }
        };
    }

    /* Tests_SRS_INTERNALCLIENT_21_003: [The constructor shall save the connection configuration using the object DeviceClientConfig.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorBadDeviceClientConfigThrows() throws URISyntaxException, IOException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        // act
        Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(DeviceClientConfig.class, (IotHubConnectionString)any, DeviceClientConfig.AuthType.SAS_TOKEN);
                times = 1;
                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                        new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                        (DeviceClientConfig)any, protocol, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);
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
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);

        // act
        Deencapsulation.invoke(client, "open");

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                        new Class[] {DeviceClientConfig.class, long.class, long.class},
                        any, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);
                times = 1;

                Deencapsulation.invoke(mockDeviceIO, "open");
                times = 1;
            }
        };
    }

    /* Tests_SRS_INTERNALCLIENT_11_040: [The function shall finish all ongoing tasks.] */
    /* Tests_SRS_INTERNALCLIENT_11_041: [The function shall cancel all recurring tasks.] */
    /* Tests_SRS_INTERNALCLIENT_21_042: [The closeNow shall closeNow the deviceIO connection.] */
    /* Tests_SRS_INTERNALCLIENT_21_043: [If the closing a connection via deviceIO is not successful, the closeNow shall throw IOException.] */
    @Test
    public void closeClosesTransportSuccess() throws IOException, URISyntaxException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isEmpty();
                result = true;
            }
        };

        // act
        Deencapsulation.invoke(client, "close");

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.isEmpty();
                times = 1;
                mockDeviceIO.close();
                times = 1;
            }
        };
    }

    /* Tests_SRS_INTERNALCLIENT_11_040: [The function shall finish all ongoing tasks.] */
    /* Tests_SRS_INTERNALCLIENT_11_041: [The function shall cancel all recurring tasks.] */
    /* Tests_SRS_INTERNALCLIENT_21_042: [The closeNow shall closeNow the deviceIO connection.] */
    /* Tests_SRS_INTERNALCLIENT_21_043: [If the closing a connection via deviceIO is not successful, the closeNow shall throw IOException.] */
    @Test
    public void closeWaitAndClosesTransportSuccess() throws IOException, URISyntaxException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isEmpty();
                returns(false, false, true);
            }
        };

        // act
        Deencapsulation.invoke(client, "close");

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.isEmpty();
                times = 3;
                mockDeviceIO.close();
                times = 1;
            }
        };
    }

    /* Tests_SRS_INTERNALCLIENT_21_008: [The closeNow shall closeNow the deviceIO connection.] */
    @Test
    public void closeNowClosesTransportSuccess() throws IOException, URISyntaxException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");

        // act
        Deencapsulation.invoke(client, "closeNow");

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.close();
                times = 1;
            }
        };
    }

    /* Tests_SRS_INTERNALCLIENT_21_009: [If the closing a connection via deviceIO is not successful, the closeNow shall throw IOException.] */
    @Test
    public void closeNowBadCloseTransportThrows() throws IOException, URISyntaxException
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
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");

        // act
        try
        {
            Deencapsulation.invoke(client, "closeNow");
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
            @Mocked final IotHubEventCallback mockCallback)
            throws IOException, URISyntaxException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final Map<String, Object> context = new HashMap<>();
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.setField(client, "config", mockConfig);
        Deencapsulation.invoke(client, "open");

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
            @Mocked final IotHubEventCallback mockCallback)
            throws IOException, URISyntaxException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final Map<String, Object> context = new HashMap<>();
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.setField(client, "config", mockConfig);
        Deencapsulation.invoke(client, "open");
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
            @Mocked final IotHubEventCallback mockCallback)
            throws IOException, URISyntaxException
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
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.setField(client, "config", mockConfig);
        Deencapsulation.invoke(client, "open");

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
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);

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
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);

        // act
        Deencapsulation.invoke(client, "setMessageCallback", new Class[] {MessageCallback.class, Object.class}, null, context);
    }

    /*
     **Tests_SRS_INTERNALCLIENT_25_025: [**The function shall create a new instance of class Device Twin and request all twin properties by calling getDeviceTwin**]**
     */
    @Test
    public void startDeviceTwinSucceeds(@Mocked final DeviceTwin mockedDeviceTwin,
                                        @Mocked final IotHubEventCallback mockedStatusCB,
                                        @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };

        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");

        //act
        Deencapsulation.invoke(client, "startTwinInternal", new Class[] {IotHubEventCallback.class, Object.class, PropertyCallBack.class, Object.class}, mockedStatusCB, NULL_OBJECT, mockedPropertyCB, NULL_OBJECT);

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.getDeviceTwin();
                times = 1;
            }
        };
    }

    /*
     **Tests_SRS_INTERNALCLIENT_25_026: [**If the deviceTwinStatusCallback or genericPropertyCallBack is null, the function shall throw an InvalidParameterException.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void startDeviceTwinThrowsIfStatusCBisNull(@Mocked final DeviceTwin mockedDeviceTwin,
                                                      @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException

    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };

        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");

        //act
        Deencapsulation.invoke(client, "startTwinInternal", new Class[] {IotHubEventCallback.class, Object.class, PropertyCallBack.class, Object.class}, null, null, mockedPropertyCB, null);

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.getDeviceTwin();
                times = 0;
            }
        };

    }

    /*
     **Tests_SRS_INTERNALCLIENT_25_026: [**If the deviceTwinStatusCallback or genericPropertyCallBack is null, the function shall throw an InvalidParameterException.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void startDeviceTwinThrowsIfPropCBisNull(@Mocked final DeviceTwin mockedDeviceTwin,
                                                    @Mocked final IotHubEventCallback mockedStatusCB) throws IOException, URISyntaxException

    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };

        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");

        //act
        Deencapsulation.invoke(client, "startTwinInternal", mockedStatusCB, null, (PropertyCallBack) null, null);

    }

    /*
     **Tests_SRS_INTERNALCLIENT_25_028: [**If this method is called twice on the same instance of the client then this method shall throw UnsupportedOperationException.**]**
     */
    @Test
    public void startDeviceTwinThrowsIfCalledTwice(@Mocked final DeviceTwin mockedDeviceTwin,
                                                   @Mocked final IotHubEventCallback mockedStatusCB,
                                                   @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException

    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };

        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");
        Deencapsulation.invoke(client, "startTwinInternal", new Class[] {IotHubEventCallback.class, Object.class, PropertyCallBack.class, Object.class}, mockedStatusCB, NULL_OBJECT, mockedPropertyCB, NULL_OBJECT);

        //act
        try
        {
            Deencapsulation.invoke(client, "startTwinInternal", new Class[] {IotHubEventCallback.class, Object.class, PropertyCallBack.class, Object.class}, mockedStatusCB, NULL_OBJECT, mockedPropertyCB, NULL_OBJECT);
        }
        catch (UnsupportedOperationException expected)
        {
            // Don't do anything, throw expected.
        }

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.getDeviceTwin();
                times = 1;
            }
        };
    }

    /*
     **Tests_SRS_INTERNALCLIENT_25_027: [**If the client has not been open, the function shall throw an IOException.**]**
     */
    @Test (expected = IOException.class)
    public void startDeviceTwinThrowsIfCalledWhenClientNotOpen(@Mocked final IotHubEventCallback mockedStatusCB,
                                                               @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException

    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
            }
        };

        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);

        //act
        Deencapsulation.invoke(client, "startTwinInternal", new Class[] {IotHubEventCallback.class, Object.class, PropertyCallBack.class, Object.class}, mockedStatusCB, NULL_OBJECT, mockedPropertyCB, NULL_OBJECT);
    }

    /*
     **Tests_SRS_INTERNALCLIENT_25_031: [**This method shall subscribe to desired properties by calling subscribeDesiredPropertiesNotification on the twin object.**]**
     */
    @Test
    public void subscribeToDPSucceeds(@Mocked final DeviceTwin mockedDeviceTwin,
                                      @Mocked final IotHubEventCallback mockedStatusCB,
                                      @Mocked final PropertyCallBack mockedPropertyCB,
                                      @Mocked final Map<Property, Pair<PropertyCallBack<String, Object>, Object>> mockMap) throws IOException, URISyntaxException

    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");
        Deencapsulation.invoke(client, "startTwinInternal", new Class[] {IotHubEventCallback.class, Object.class, PropertyCallBack.class, Object.class}, mockedStatusCB, NULL_OBJECT, mockedPropertyCB, NULL_OBJECT);

        //act
        Deencapsulation.invoke(client, "subscribeToDesiredProperties", mockMap);

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.subscribeDesiredPropertiesNotification(mockMap);
                times = 1;
            }
        };

    }

    @Test
    public void subscribeToDPWorksWhenMapIsNull(@Mocked final DeviceTwin mockedDeviceTwin,
                                                @Mocked final IotHubEventCallback mockedStatusCB,
                                                @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException

    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");
        Deencapsulation.invoke(client, "startTwinInternal", new Class[] {IotHubEventCallback.class, Object.class, PropertyCallBack.class, Object.class}, mockedStatusCB, NULL_OBJECT, mockedPropertyCB, NULL_OBJECT);

        //act
        Deencapsulation.invoke(client, "subscribeToDesiredProperties", new Class[] {Map.class}, NULL_OBJECT);

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.subscribeDesiredPropertiesNotification((Map)any);
                times = 1;
            }
        };

    }

    /*
     **Tests_SRS_INTERNALCLIENT_25_030: [**If the client has not been open, the function shall throw an IOException.**]**
     */
    @Test
    public void subscribeToDPThrowsIfCalledWhenClientNotOpen(@Mocked final DeviceTwin mockedDeviceTwin,
                                                             @Mocked final IotHubEventCallback mockedStatusCB,
                                                             @Mocked final PropertyCallBack mockedPropertyCB,
                                                             @Mocked final Map<Property, Pair<PropertyCallBack<String, Object>, Object>> mockMap) throws IOException, URISyntaxException

    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                returns(true,false);
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");
        Deencapsulation.invoke(client, "startTwinInternal", new Class[] {IotHubEventCallback.class, Object.class, PropertyCallBack.class, Object.class}, mockedStatusCB, NULL_OBJECT, mockedPropertyCB, NULL_OBJECT);

        //act
        try
        {
            Deencapsulation.invoke(client, "subscribeToDesiredProperties", mockMap);
        }
        catch (Exception expected)
        {
            // Don't do anything, throw expected.
        }

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.subscribeDesiredPropertiesNotification(mockMap);
                times = 0;
            }
        };
    }

    /*
     **Tests_SRS_INTERNALCLIENT_25_029: [**If the client has not started twin before calling this method, the function shall throw an IOException.**]**
     */
    @Test
    public void subscribeToDPThrowsIfCalledBeforeStartingTwin(@Mocked final DeviceTwin mockedDeviceTwin,
                                                              @Mocked final Map<Property, Pair<PropertyCallBack<String, Object>, Object>> mockMap) throws IOException, URISyntaxException

    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");

        //act
        try
        {
            Deencapsulation.invoke(client, "subscribeToDesiredProperties", mockMap);
        }
        catch (Exception expected)
        {
            // Don't do anything, throw expected.
        }

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.subscribeDesiredPropertiesNotification(mockMap);
                times = 0;
            }
        };

    }
    /*
     **Tests_SRS_INTERNALCLIENT_25_035: [**This method shall send to reported properties by calling updateReportedProperties on the twin object.**]**
     */
    @Test
    public void sendRPSucceeds(@Mocked final DeviceTwin mockedDeviceTwin,
                               @Mocked final IotHubEventCallback mockedStatusCB,
                               @Mocked final PropertyCallBack mockedPropertyCB,
                               @Mocked final Set<Property> mockSet) throws IOException, URISyntaxException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");
        Deencapsulation.invoke(client, "startTwinInternal", new Class[] {IotHubEventCallback.class, Object.class, PropertyCallBack.class, Object.class}, mockedStatusCB, NULL_OBJECT, mockedPropertyCB, NULL_OBJECT);

        //act
        Deencapsulation.invoke(client, "sendReportedProperties", mockSet);

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.updateReportedProperties(mockSet);
                times = 1;
            }
        };
    }

    @Test
    public void sendRPWithVersionSucceeds(@Mocked final DeviceTwin mockedDeviceTwin,
                                          @Mocked final IotHubEventCallback mockedStatusCB,
                                          @Mocked final PropertyCallBack mockedPropertyCB,
                                          @Mocked final Set<Property> mockSet) throws IOException, URISyntaxException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");
        Deencapsulation.invoke(client, "startTwinInternal", new Class[] {IotHubEventCallback.class, Object.class, PropertyCallBack.class, Object.class}, mockedStatusCB, NULL_OBJECT, mockedPropertyCB, NULL_OBJECT);

        //act
        Deencapsulation.invoke(client, "sendReportedProperties", mockSet, 10);

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.updateReportedProperties(mockSet, 10);
                times = 1;
            }
        };
    }

    /*
     **Tests_SRS_INTERNALCLIENT_25_032: [**If the client has not started twin before calling this method, the function shall throw an IOException.**]**
     */
    @Test
    public void sendRPThrowsIfCalledBeforeStartingTwin(@Mocked final DeviceTwin mockedDeviceTwin,
                                                       @Mocked final Set<Property> mockSet) throws IOException, URISyntaxException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");

        //act
        try
        {
            Deencapsulation.invoke(client, "sendReportedProperties", mockSet);
        }
        catch (Exception expected)
        {
            // Don't do anything, throw expected.
        }

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.updateReportedProperties(mockSet);
                times = 0;
            }
        };
    }

    @Test
    public void sendRPWithVersionThrowsIfCalledBeforeStartingTwin(@Mocked final DeviceTwin mockedDeviceTwin,
                                                                  @Mocked final Set<Property> mockSet) throws IOException, URISyntaxException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");

        //act
        try
        {
            Deencapsulation.invoke(client, "sendReportedProperties", mockSet, 10);
        }
        catch (Exception expected)
        {
            // Don't do anything, throw expected.
        }

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.updateReportedProperties(mockSet, 10);
                times = 0;
            }
        };
    }

    /*
     **Tests_SRS_INTERNALCLIENT_25_033: [**If the client has not been open, the function shall throw an IOException.**]**
     */
    @Test
    public void sendRPThrowsIfCalledWhenClientNotOpen(@Mocked final DeviceTwin mockedDeviceTwin,
                                                      @Mocked final Set<Property> mockSet) throws IOException, URISyntaxException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.setField(client, "twin", mockedDeviceTwin);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);

        //act
        try
        {
            Deencapsulation.invoke(client, "sendReportedProperties", mockSet);
        }
        catch (Exception expected)
        {
            // Don't do anything, throw expected.
        }

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.updateReportedProperties(mockSet);
                times = 0;
            }
        };
    }

    @Test
    public void sendRPWithVersionThrowsIfCalledWhenClientNotOpen(@Mocked final DeviceTwin mockedDeviceTwin,
                                                                 @Mocked final Set<Property> mockSet) throws IOException, URISyntaxException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.setField(client, "twin", mockedDeviceTwin);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);

        //act
        try
        {
            Deencapsulation.invoke(client, "sendReportedProperties", mockSet, 10);
        }
        catch (Exception expected)
        {
            // Don't do anything, throw expected.
        }

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.updateReportedProperties(mockSet, 10);
                times = 0;
            }
        };
    }

    /*
     **Tests_SRS_INTERNALCLIENT_25_034: [**If reportedProperties is null or empty, the function shall throw an InvalidParameterException.**]**
     */
    @Test
    public void sendRPThrowsIfCalledWhenRPNullOrEmpty(@Mocked final DeviceTwin mockedDeviceTwin,
                                                      @Mocked final IotHubEventCallback mockedStatusCB,
                                                      @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");
        Deencapsulation.invoke(client, "startTwinInternal", new Class[] {IotHubEventCallback.class, Object.class, PropertyCallBack.class, Object.class}, mockedStatusCB, NULL_OBJECT, mockedPropertyCB, NULL_OBJECT);

        //act
        try
        {
            Deencapsulation.invoke(client, "sendReportedProperties", null);
        }
        catch (IllegalArgumentException expected)
        {
            // Don't do anything, throw expected.
        }

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.updateReportedProperties((Set)any);
                times = 0;
            }
        };
    }

    @Test
    public void sendRPWithVersionThrowsIfCalledWhenRPNullOrEmpty(@Mocked final DeviceTwin mockedDeviceTwin,
                                                                 @Mocked final IotHubEventCallback mockedStatusCB,
                                                                 @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");
        Deencapsulation.invoke(client, "startTwinInternal", new Class[] {IotHubEventCallback.class, Object.class, PropertyCallBack.class, Object.class}, mockedStatusCB, NULL_OBJECT, mockedPropertyCB, NULL_OBJECT);

        //act
        try
        {
            Deencapsulation.invoke(client, "sendReportedProperties", new Class[] {Set.class, int.class}, null, 10);
        }
        catch (IllegalArgumentException expected)
        {
            // Don't do anything, throw expected.
        }

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.updateReportedProperties((Set)any, 10);
                times = 0;
            }
        };
    }

    /*
     **Tests_SRS_INTERNALCLIENT_21_053: [**If version is negative, the function shall throw an IllegalArgumentException.**]**
     */
    @Test
    public void sendRPThrowsIfCalledWhenVersionIsNegative(@Mocked final DeviceTwin mockedDeviceTwin,
                                                          @Mocked final IotHubEventCallback mockedStatusCB,
                                                          @Mocked final PropertyCallBack mockedPropertyCB,
                                                          @Mocked final Set<Property> mockSet) throws IOException, URISyntaxException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");
        Deencapsulation.invoke(client, "startTwinInternal", new Class[] {IotHubEventCallback.class, Object.class, PropertyCallBack.class, Object.class}, mockedStatusCB, NULL_OBJECT, mockedPropertyCB, NULL_OBJECT);

        //act
        try
        {
            Deencapsulation.invoke(client, "sendReportedProperties", mockSet, -1);
        }
        catch (IllegalArgumentException expected)
        {
            // Don't do anything, throw expected.
        }

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.updateReportedProperties((Set)any, -1);
                times = 0;
            }
        };
    }

    /*
    Tests_SRS_INTERNALCLIENT_25_038: [**This method shall subscribe to device methods by calling subscribeToDeviceMethod on DeviceMethod object which it created.**]**
     */
    @Test
    public void subscribeToDeviceMethodSucceeds(@Mocked final IotHubEventCallback mockedStatusCB,
                                                @Mocked final DeviceMethodCallback mockedDeviceMethodCB,
                                                @Mocked final DeviceMethod mockedMethod) throws IOException, URISyntaxException
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
        final InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");

        //act

        Deencapsulation.invoke(client, "subscribeToMethodsInternal", new Class[] {DeviceMethodCallback.class, Object.class, IotHubEventCallback.class, Object.class}, mockedDeviceMethodCB, NULL_OBJECT, mockedStatusCB, NULL_OBJECT);

        //assert
        new Verifications()
        {
            {
                mockedMethod.subscribeToDeviceMethod(mockedDeviceMethodCB, any);
                times = 1;
            }
        };

    }

    /*
    Tests_SRS_INTERNALCLIENT_25_036: [**If the client has not been open, the function shall throw an IOException.**]**
     */
    @Test (expected = IOException.class)
    public void subscribeToDeviceMethodThrowsIfClientNotOpen(@Mocked final IotHubEventCallback mockedStatusCB,
                                                             @Mocked final DeviceMethodCallback mockedDeviceMethodCB)
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
        final InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);

        //act
        Deencapsulation.invoke(client, "subscribeToMethodsInternal", new Class[] {DeviceMethodCallback.class, Object.class, IotHubEventCallback.class, Object.class}, mockedDeviceMethodCB, null, mockedStatusCB, null);
    }

    /*
    Tests_SRS_INTERNALCLIENT_25_037: [**If deviceMethodCallback or deviceMethodStatusCallback is null, the function shall throw an IllegalArgumentException.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void subscribeToDeviceMethodThrowsIfDeviceMethodCallbackNull(@Mocked final IotHubEventCallback mockedStatusCB)
            throws IOException, URISyntaxException
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
        final InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");

        //act
        Deencapsulation.invoke(client, "subscribeToMethodsInternal", new Class[] {DeviceMethodCallback.class, Object.class, IotHubEventCallback.class, Object.class}, null, null, mockedStatusCB, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void subscribeToDeviceMethodThrowsIfDeviceMethodStatusCallbackNull(@Mocked final DeviceMethodCallback mockedDeviceMethodCB)
            throws IOException, URISyntaxException
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
        final InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");

        //act
        Deencapsulation.invoke(client, "subscribeToMethodsInternal", mockedDeviceMethodCB, null, null, null);
    }

    /*
    Tests_SRS_INTERNALCLIENT_25_039: [**This method shall update the deviceMethodCallback if called again, but it shall not subscribe twice.**]**
     */
    @Test
    public void subscribeToDeviceMethodWorksEvenWhenCalledTwice(@Mocked final IotHubEventCallback mockedStatusCB,
                                                                @Mocked final DeviceMethodCallback mockedDeviceMethodCB,
                                                                @Mocked final DeviceMethod mockedMethod) throws IOException, URISyntaxException
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
        final InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.invoke(client, "open");
        Deencapsulation.invoke(client, "subscribeToMethodsInternal", new Class[] {DeviceMethodCallback.class, Object.class, IotHubEventCallback.class, Object.class}, mockedDeviceMethodCB, NULL_OBJECT, mockedStatusCB, NULL_OBJECT);

        // act
        Deencapsulation.invoke(client, "subscribeToMethodsInternal", new Class[] {DeviceMethodCallback.class, Object.class, IotHubEventCallback.class, Object.class}, mockedDeviceMethodCB, NULL_OBJECT, mockedStatusCB, NULL_OBJECT);

        // assert
        new Verifications()
        {
            {
                mockedMethod.subscribeToDeviceMethod(mockedDeviceMethodCB, any);
                times = 2;
            }
        };
    }

    //Tests_SRS_INTERNALCLIENT_34_044: [**If the SAS token has expired before this call, throw a Security Exception**]
    @Test (expected = SecurityException.class)
    public void tokenExpiresAfterDeviceClientInitializedBeforeOpen() throws SecurityException, URISyntaxException, IOException
    {
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;

                mockConfig.getSasTokenAuthentication().isRenewalNecessary();
                result = true;
            }
        };

        // act
        Deencapsulation.invoke(client, "open");
    }

    //Tests_SRS_INTERNALCLIENT_99_003: [If the callback is null the method shall throw an IllegalArgument exception.]
    @Test (expected = IllegalArgumentException.class)
    public void registerConnectionStateCallbackNullCallback()
            throws IllegalArgumentException, URISyntaxException, IOException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        final InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);

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

    //Tests_SRS_INTERNALCLIENT_34_069: [This function shall register the provided callback and context with its device IO instance.]
    @Test
    public void registerConnectionStatusChangeCallbackRegistersCallbackWithDeviceIO()
    {
        //arrange
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, IotHubClientProtocol.AMQPS, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);
        final Object context = new Object();

        //act
        Deencapsulation.invoke(client, "registerConnectionStatusChangeCallback",  new Class[] {IotHubConnectionStatusChangeCallback.class, Object.class}, mockedIotHubConnectionStatusChangeCallback, context);

        //assert
        new Verifications()
        {
            {
                mockDeviceIO.registerConnectionStatusChangeCallback(mockedIotHubConnectionStatusChangeCallback, context);
                times = 1;
            }
        };
    }

    //Tests_SRS_INTERNALCLIENT_28_001: [The function shall set the device config's RetryPolicy .]
    @Test
    public void setRetryPolicySetPolicy() throws URISyntaxException
    {
        //arrange
        
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
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
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
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
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);

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

    @Test
    public void subscribeToDPSucceedsEvenWhenUserCBIsNull(@Mocked final DeviceTwin mockedDeviceTwin,
                                                          @Mocked final IotHubEventCallback mockedStatusCB,
                                                          @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        //arrange
        final Device mockDevice = new Device()
        {
            @Override
            public void PropertyCall(String propertyKey, Object propertyValue, Object context)
            {

            }
        };
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        client.open();
        Deencapsulation.invoke(client, "startTwinInternal", new Class[] {IotHubEventCallback.class, Object.class, PropertyCallBack.class, Object.class}, mockedStatusCB, null, mockedPropertyCB, null);
        mockDevice.setDesiredPropertyCallback(new Property("Desired", null), null, null);

        //act
        client.subscribeToDesiredProperties(mockDevice.getDesiredProp());

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.subscribeDesiredPropertiesNotification((Map)any);
                times = 1;
            }
        };
    }

    //Tests_SRS_INTERNALCLIENT_21_040: [If the client has not started twin before calling this method, the function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void getDeviceTwinThrowsIfNotStartedYet() throws URISyntaxException, IOException
    {
        //arrange
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, IotHubClientProtocol.AMQPS, SEND_PERIOD, RECEIVE_PERIOD);

        //act
        Deencapsulation.invoke(client, "getTwinInternal");
    }

    //Tests_SRS_INTERNALCLIENT_21_041: [If the client has not been open, the function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void getDeviceTwinThrowsIfNotOpen(@Mocked DeviceTwin mockedDeviceTwin) throws URISyntaxException, IOException
    {
        //arrange
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, IotHubClientProtocol.AMQPS, SEND_PERIOD, RECEIVE_PERIOD);

        Deencapsulation.setField(client, "twin", mockedDeviceTwin);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);

        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
            }
        };

        //act
        Deencapsulation.invoke(client, "getTwinInternal");
    }

    //Tests_SRS_INTERNALCLIENT_21_042: [The function shall get all desired properties by calling getDeviceTwin.]
    @Test
    public void getDeviceTwinSuccess(final @Mocked DeviceTwin mockedDeviceTwin) throws URISyntaxException, IOException
    {
        //arrange
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, IotHubClientProtocol.AMQPS, SEND_PERIOD, RECEIVE_PERIOD);

        Deencapsulation.setField(client, "twin", mockedDeviceTwin);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);

        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };

        //act
        Deencapsulation.invoke(client, "getTwinInternal");

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.getDeviceTwin();
                times = 1;
            }
        };
    }

    //Tests_SRS_INTERNALCLIENT_34_081: [If device io has not been opened yet, this function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void startDeviceTwinThrowsIfClientNotOpen() throws IOException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);

        // act
        Deencapsulation.invoke(client, "startTwinInternal", mockedIotHubEventCallback, new Object(), mockedTwinPropertyCallback, new Object());
    }

    //Tests_SRS_INTERNALCLIENT_34_082: [If either callback is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void startDeviceTwinThrowsIfCallbackNull() throws IOException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };

        // act
        Deencapsulation.invoke(client, "startTwinInternal", (IotHubEventCallback) null, new Object(), mockedTwinPropertyCallback, new Object());
    }

    //Tests_SRS_INTERNALCLIENT_34_083: [If either callback is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = UnsupportedOperationException.class)
    public void startDeviceTwinThrowsIfTwinAlreadyStarted(final @Mocked DeviceTwin mockedDeviceTwin) throws IOException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);
        Deencapsulation.setField(client, "twin", mockedDeviceTwin);
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };

        // act
        Deencapsulation.invoke(client, "startTwinInternal", mockedIotHubEventCallback, new Object(), mockedTwinPropertyCallback, new Object());
    }

    //Tests_SRS_INTERNALCLIENT_34_084: [This function shall initialize a DeviceTwin object and invoke getDeviceTwin on it.]
    @Test
    public void startDeviceTwinSuccess(final @Mocked DeviceTwin mockedDeviceTwin) throws IOException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);
        Deencapsulation.setField(client, "config", mockConfig);
        Deencapsulation.setField(client, "twin", null);
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;

                new DeviceTwin(mockDeviceIO, mockConfig, mockedIotHubEventCallback, any, mockedTwinPropertyCallback, any);
                result = mockedDeviceTwin;
            }
        };

        // act
        Deencapsulation.invoke(client, "startTwinInternal", mockedIotHubEventCallback, new Object(), mockedTwinPropertyCallback, new Object());

        //assert
        assertNotNull(Deencapsulation.getField(client, "twin"));
        new Verifications()
        {
            {
                new DeviceTwin(mockDeviceIO, mockConfig, mockedIotHubEventCallback, any, mockedTwinPropertyCallback, any);
                times = 1;

                mockedDeviceTwin.getDeviceTwin();
                times = 1;
            }
        };
    }

    //Tests_SRS_INTERNALCLIENT_34_087: [If the client has not started twin before calling this method, the function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void subscribeToTwinDesiredPropertiesThrowsIfTwinNotStarted() throws IOException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.setField(client, "twin", null);

        Map<Property, Pair<TwinPropertyCallBack, Object>> onDesiredPropertyChange = new HashMap<>();

        // act
        client.subscribeToTwinDesiredProperties(onDesiredPropertyChange);
    }

    //Tests_SRS_INTERNALCLIENT_34_086: [If the client has not been open, the function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void subscribeToTwinDesiredPropertiesThrowsIfClientNotOpen(final @Mocked DeviceTwin mockedDeviceTwin) throws IOException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);
        Deencapsulation.setField(client, "twin", mockedDeviceTwin);
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
            }
        };

        Map<Property, Pair<TwinPropertyCallBack, Object>> onDesiredPropertyChange = new HashMap<>();

        // act
        client.subscribeToTwinDesiredProperties(onDesiredPropertyChange);
    }

    //Tests_SRS_INTERNALCLIENT_34_085: [This method shall subscribe to desired properties by calling subscribeDesiredPropertiesNotification on the twin object.]
    @Test
    public void subscribeToTwinDesiredPropertiesSuccess(final @Mocked DeviceTwin mockedDeviceTwin) throws IOException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);
        Deencapsulation.setField(client, "twin", mockedDeviceTwin);
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };

        final Map<Property, Pair<TwinPropertyCallBack, Object>> onDesiredPropertyChange = new HashMap<>();

        // act
        client.subscribeToTwinDesiredProperties(onDesiredPropertyChange);

        //assert
        assertNotNull(Deencapsulation.getField(client, "twin"));
        new Verifications()
        {
            {
                mockedDeviceTwin.subscribeDesiredPropertiesTwinPropertyNotification(onDesiredPropertyChange);
                times = 1;
            }
        };
    }

    // Tests_SRS_INTERNALCLIENT_02_015: [If optionName is null or not an option handled by the client, then it shall throw IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setOptionWithNullOptionNameThrows()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
            }
        };
        
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);

        long someMilliseconds = 4;

        // act
        client.setOption(null, someMilliseconds);
    }

    // Tests_SRS_INTERNALCLIENT_02_015: [If optionName is null or not an option handled by the client, then it shall throw IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setOptionWithUnknownOptionNameThrows()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);

        long someMilliseconds = 4;

        // act
        client.setOption("thisIsNotAHandledOption", someMilliseconds);
    }

    //Tests_SRS_INTERNALCLIENT_02_017: [Available only for HTTP.]
    @Test (expected = IllegalArgumentException.class)
    public void setOptionMinimumPollingIntervalWithAMQPfails()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        long someMilliseconds = 4;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.AMQPS;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);

        // act
        client.setOption("SetMinimumPollingInterval", someMilliseconds);
    }

    //Tests_SRS_INTERNALCLIENT_02_018: [Value needs to have type long].
    @Test (expected = IllegalArgumentException.class)
    public void setOptionMinimumPollingIntervalWithStringInsteadOfLongFails()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);

        // act
        client.setOption("SetMinimumPollingInterval", "thisIsNotALong");
    }

    //Tests_SRS_INTERNALCLIENT_02_005: [Setting the option can only be done before open call.]
    @Test (expected = IllegalStateException.class)
    public void setOptionMinimumPollingIntervalAfterOpenFails()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        client.open();
        long value = 3;

        // act
        client.setOption("SetMinimumPollingInterval", value);
    }

    //Tests_SRS_INTERNALCLIENT_02_016: ["SetMinimumPollingInterval" - time in milliseconds between 2 consecutive polls.]
    @Test
    public void setOptionMinimumPollingIntervalSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        final long value = 3L;

        // act
        client.setOption("SetMinimumPollingInterval", value);

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.setReceivePeriodInMilliseconds(value);
            }
        };
    }

    // Tests_SRS_INTERNALCLIENT_21_040: ["SetSendInterval" - time in milliseconds between 2 consecutive message sends.]
    @Test
    public void setOptionSendIntervalSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        final long value = 3L;

        // act
        client.setOption("SetSendInterval", value);

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.setSendPeriodInMilliseconds(value);
            }
        };
    }

    @Test (expected = IllegalArgumentException.class)
    public void setOptionSendIntervalWithStringInsteadOfLongFails()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);

        // act
        client.setOption("SetSendInterval", "thisIsNotALong");
    }

    @Test (expected = IllegalArgumentException.class)
    public void setOptionValueNullThrows()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);

        // act
        client.setOption("", null);
    }

    //Tests_SRS_INTERNALCLIENT_25_022: [**"SetSASTokenExpiryTime" should have value type long.]
    @Test (expected = IllegalArgumentException.class)
    public void setOptionSASTokenExpiryTimeWithStringInsteadOfLongFails()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);

        // act
        client.setOption("SetSASTokenExpiryTime", "thisIsNotALong");
    }

    //Tests_SRS_INTERNALCLIENT_25_021: ["SetSASTokenExpiryTime" - time in seconds after which SAS Token expires.]
    @Test
    public void setOptionSASTokenExpiryTimeHTTPSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        final long value = 60;

        // act
        client.setOption("SetSASTokenExpiryTime", value);

        // assert
        new Verifications()
        {
            {
                mockConfig.getSasTokenAuthentication().setTokenValidSecs(value);
                times = 1;
            }
        };
    }

    //Tests_SRS_INTERNALCLIENT_25_021: ["SetSASTokenExpiryTime" - time in seconds after which SAS Token expires.]
    //Tests_SRS_INTERNALCLIENT_25_024: ["SetSASTokenExpiryTime" shall restart the transport if transport is already open after updating expiry time.]
    @Test
    public void setOptionSASTokenExpiryTimeAfterClientOpenHTTPSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getSasTokenAuthentication().canRefreshToken();
                result = true;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;

        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        client.open();
        final long value = 60;

        // act
        client.setOption("SetSASTokenExpiryTime", value);

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.close();
                times = 1;
                mockConfig.getSasTokenAuthentication().setTokenValidSecs(value);
                times = 1;
                Deencapsulation.invoke(mockDeviceIO, "open");
                times = 2;
            }
        };
    }

    /*Tests_SRS_INTERNALCLIENT_25_024: ["SetSASTokenExpiryTime" shall restart the transport
                                    1. If the device currently uses device key and
                                    2. If transport is already open
                                    after updating expiry time.]
    */
    @Test
    public void setOptionSASTokenExpiryTimeAfterClientOpenTransportWithSasTokenSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getSasTokenAuthentication().canRefreshToken();
                result = true;
            }
        };
        final String connString = "some string";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;

        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        client.open();
        final long value = 60;

        // act
        client.setOption("SetSASTokenExpiryTime", value);

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.close();
                times = 1;
                mockConfig.getSasTokenAuthentication().setTokenValidSecs(value);
                times = 1;
                Deencapsulation.invoke(mockDeviceIO, "open");
                times = 2;
            }
        };
    }
    //Tests_SRS_INTERNALCLIENT_25_021: ["SetSASTokenExpiryTime" - Time in secs to specify SAS Token Expiry time.]
    @Test
    public void setOptionSASTokenExpiryTimeAMQPSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };

        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        final long value = 60;

        // act
        client.setOption("SetSASTokenExpiryTime", value);

        // assert
        new Verifications()
        {
            {
                mockConfig.getSasTokenAuthentication().setTokenValidSecs(value);
                times = 1;
            }
        };
    }

    //Tests_SRS_INTERNALCLIENT_25_021: ["SetSASTokenExpiryTime" - time in seconds after which SAS Token expires.]
    //Tests_SRS_INTERNALCLIENT_25_024: ["SetSASTokenExpiryTime" shall restart the transport if transport is already open after updating expiry time.]
    @Test
    public void setOptionSASTokenExpiryTimeAfterClientOpenAMQPSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getSasTokenAuthentication().canRefreshToken();
                result = true;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        client.open();
        final long value = 60;

        // act
        client.setOption("SetSASTokenExpiryTime", value);

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.close();
                times = 1;
                mockConfig.getSasTokenAuthentication().setTokenValidSecs(value);
                times = 1;
                Deencapsulation.invoke(mockDeviceIO, "open");
                times = 2;
            }
        };
    }

    //Tests_SRS_INTERNALCLIENT_25_021: [**"SetSASTokenExpiryTime" - Time in secs to specify SAS Token Expiry time.]
    @Test
    public void setOptionSASTokenExpiryTimeMQTTSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations() {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getSasTokenAuthentication().canRefreshToken();
                result = true;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        final long value = 60;

        // act
        client.setOption("SetSASTokenExpiryTime", value);

        // assert
        new Verifications()
        {
            {
                mockConfig.getSasTokenAuthentication().setTokenValidSecs(value);
                times = 1;
            }
        };
    }

    //Tests_SRS_INTERNALCLIENT_25_021: ["SetSASTokenExpiryTime" - time in seconds after which SAS Token expires.]
    //Tests_SRS_INTERNALCLIENT_25_024: ["SetSASTokenExpiryTime" shall restart the transport if transport is already open after updating expiry time.]
    @Test
    public void setOptionSASTokenExpiryTimeAfterClientOpenMQTTSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getSasTokenAuthentication().canRefreshToken();
                result = true;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        client.open();
        final long value = 60;

        // act
        client.setOption("SetSASTokenExpiryTime", value);

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.close();
                times = 1;
                mockConfig.getSasTokenAuthentication().setTokenValidSecs(value);
                times = 1;
                Deencapsulation.invoke(mockDeviceIO, "open");
                times = 2;
            }
        };
    }

    // Tests_SRS_INTERNALCLIENT_12_027: [The function shall throw IOError if either the deviceIO or the tranportClient's open() or closeNow() throws.]
    @Test (expected = IOError.class)
    public void setOptionClientSASTokenExpiryTimeAfterClientOpenAMQPThrowsDeviceIOClose()
            throws IOException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getSasTokenAuthentication().canRefreshToken();
                result = true;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockDeviceIO.close();
                result =  new IOException();
            }
        };
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.setField(client, "config", mockConfig);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);
        final long value = 60;

        // act
        client.setOption("SetSASTokenExpiryTime", value);
    }

    // Tests_SRS_INTERNALCLIENT_12_027: [The function shall throw IOError if either the deviceIO or the tranportClient's open() or closeNow() throws.]
    @Test (expected = IOError.class)
    public void setOptionClientSASTokenExpiryTimeAfterClientOpenAMQPThrowsTransportDeviceIOOpen()
            throws IOException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getSasTokenAuthentication().canRefreshToken();
                result = true;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                Deencapsulation.invoke(mockDeviceIO, "open");
                result =  new IOException();
            }
        };
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.setField(client, "config", mockConfig);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);
        final long value = 60;

        // act
        client.setOption("SetSASTokenExpiryTime", value);
    }

    //Tests_SRS_INTERNALCLIENT_34_065: [""SetSASTokenExpiryTime" if this option is called when not using sas token authentication, an IllegalStateException shall be thrown.*]
    @Test (expected = IllegalStateException.class)
    public void setOptionSASTokenExpiryTimeWhenNotUsingSasTokenAuthThrows() throws URISyntaxException, IOException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.X509_CERTIFICATE;
            }
        };
        final String publicCert = "any cert";
        final String privateKey = "any key";

        // act
        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, String.class, boolean.class, String.class, boolean.class, long.class, long.class}, mockIotHubConnectionString, protocol, publicCert, false, privateKey, false, SEND_PERIOD, RECEIVE_PERIOD);

        // act
        client.setOption("SetSASTokenExpiryTime", 25L);
    }

    // Tests_SRS_INTERNALCLIENT_12_029: [*SetCertificatePath" shall throw if the transportClient or deviceIO already open, otherwise set the path on the config.]
    @Test
    public void setOptionSetCertificatePathX509Success()
            throws IOException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.AMQPS_WS;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.X509_CERTIFICATE;
            }
        };
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS_WS;

        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.setField(client, "config", mockConfig);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);
        final String value = "certificatePath";

        // act
        client.setOption("SetCertificatePath", value);

        new Verifications()
        {
            {
                mockConfig.getAuthenticationProvider();
                times = 1;
                mockIotHubAuthenticationProvider.setPathToIotHubTrustedCert(value);
                times = 1;
            }
        };
    }

    // Tests_SRS_INTERNALCLIENT_12_029: [*SetCertificatePath" shall throw if the transportClient or deviceIO already open, otherwise set the path on the config.]
    @Test
    public void setOptionSetCertificatePathSASSuccess()
            throws IOException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.AMQPS_WS;
            }
        };
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS_WS;

        InternalClient client = Deencapsulation.newInstance(InternalClient.class, new Class[] {IotHubConnectionString.class, IotHubClientProtocol.class, long.class, long.class}, mockIotHubConnectionString, protocol, SEND_PERIOD, RECEIVE_PERIOD);
        Deencapsulation.setField(client, "config", mockConfig);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);
        final String value = "certificatePath";

        // act
        client.setOption("SetCertificatePath", value);

        new Verifications()
        {
            {
                mockConfig.getAuthenticationProvider();
                times = 1;
                mockIotHubAuthenticationProvider.setPathToIotHubTrustedCert(value);
                times = 1;
            }
        };
    }
}
