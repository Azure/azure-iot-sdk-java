// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.*;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUpload;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Unit tests for DeviceClient.
 * Methods: 91%
 * Lines: 82%
 */
public class DeviceClientTest
{
    @Mocked
    DeviceClientConfig mockConfig;

    @Mocked
    IotHubConnectionString mockIotHubConnectionString;

    @Mocked
    DeviceIO mockDeviceIO;

    private static long SEND_PERIOD_MILLIS = 10L;
    private static long RECEIVE_PERIOD_MILLIS_AMQPS = 10L;
    private static long RECEIVE_PERIOD_MILLIS_HTTPS = 25*60*1000; /*25 minutes*/

    private void deviceClientInstanceExpectation(final String connectionString, final IotHubClientProtocol protocol)
    {
        final long receivePeriod;
        switch (protocol)
        {
            case HTTPS:
                receivePeriod = RECEIVE_PERIOD_MILLIS_HTTPS;
                break;
            default:
                receivePeriod = RECEIVE_PERIOD_MILLIS_AMQPS;
                break;
        }

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubConnectionString.class, connectionString);
                result = mockIotHubConnectionString;
                Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);
                result = mockConfig;
                Deencapsulation.newInstance(DeviceIO.class,
                        mockConfig, protocol, SEND_PERIOD_MILLIS, receivePeriod);
                result = mockDeviceIO;
            }
        };
    }

    /* Tests_SRS_DEVICECLIENT_21_001: [The constructor shall interpret the connection string as a set of key-value pairs delimited by ';', using the object IotHubConnectionString.] */
    /* Tests_SRS_DEVICECLIENT_21_002: [The constructor shall initialize the IoT Hub transport for the protocol specified, creating a instance of the deviceIO.] */
    /* Tests_SRS_DEVICECLIENT_21_003: [The constructor shall save the connection configuration using the object DeviceClientConfig.] */
    @Test
    public void constructorSuccess() throws URISyntaxException, IOException
    {
        // arrange
        final String connString =
                "HostName=iothub.device.com;CredentialType=SharedAccessKey;CredentialScope=Device;DeviceId=testdevice;SharedAccessKey=adjkl234j52=;";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        // act
        new DeviceClient(connString, protocol);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubConnectionString.class, connString);
                times = 1;
                new DeviceClientConfig((IotHubConnectionString)any);
                times = 1;
                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                        new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                        (DeviceClientConfig)any, protocol, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICECLIENT_34_058: [The constructor shall interpret the connection string as a set of key-value pairs delimited by ';', using the object IotHubConnectionString.]
    //Tests_SRS_DEVICECLIENT_34_059: [The constructor shall initialize the IoT Hub transport for the protocol specified, creating a instance of the deviceIO.]
    //Tests_SRS_DEVICECLIENT_34_060: [The constructor shall save the connection configuration using the object DeviceClientConfig.]
    //Tests_SRS_DEVICECLIENT_34_063: [This function shall save the provided certificate and key within its config.]
    @Test
    public void constructorSuccessX509() throws URISyntaxException, IOException
    {
        // arrange
        final String publicKeyCert = "someCert";
        final String privateKey = "someKey";

        final String connString =
                "HostName=iothub.device.com;DeviceId=testdevice;x509=true";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS_WS;

        new NonStrictExpectations()
        {
            {
                mockIotHubConnectionString.isUsingX509();
                result = true;
            }
        };

        // act
        new DeviceClient(connString, protocol, publicKeyCert, false, privateKey, false);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubConnectionString.class, connString);
                times = 1;
                new DeviceClientConfig((IotHubConnectionString) any, publicKeyCert, false, privateKey, false);
                times = 1;
                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                        new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                        (DeviceClientConfig)any, protocol, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICECLIENT_21_004: [If the connection string is null or empty, the function shall throw an IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullConnectionStringThrows() throws URISyntaxException, IOException
    {
        // arrange
        final String connString = null;
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        // act
        new DeviceClient(connString, protocol);
    }

    //Tests_SRS_DEVICECLIENT_34_061: [If the connection string is null or empty, the function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void x509ConstructorNullConnectionStringThrows() throws URISyntaxException, IOException
    {
        // arrange
        final String connString = null;
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        // act
        new DeviceClient(connString, protocol, "any cert", false, "any key", false);
    }


    /* Tests_SRS_DEVICECLIENT_21_004: [If the connection string is null or empty, the function shall throw an IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorEmptyConnectionStringThrows() throws URISyntaxException, IOException
    {
        // arrange
        final String connString = "";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        // act
        new DeviceClient(connString, protocol);
    }

    //Tests_SRS_DEVICECLIENT_34_062: [If protocol is null, the function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void x509ConstructorEmptyConnectionStringThrows() throws URISyntaxException, IOException
    {
        // arrange
        final String connString = "";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        // act
        new DeviceClient(connString, protocol, "any cert", false, "any key", false);
    }

    /* Tests_SRS_DEVICECLIENT_21_005: [If protocol is null, the function shall throw an IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullProtocolThrows() throws URISyntaxException, IOException
    {
        // arrange
        final String connString =
                "HostName=iothub.device.com;CredentialType=SharedAccessKey;CredentialScope=Device;DeviceId=testdevice;SharedAccessKey=adjkl234j52=;";
        final IotHubClientProtocol protocol = null;

        // act
        new DeviceClient(connString, protocol);
    }

    /* Tests_SRS_DEVICECLIENT_21_001: [The constructor shall interpret the connection string as a set of key-value pairs delimited by ';', using the object IotHubConnectionString.] */
    @Test
    public void constructorBadConnectionStringThrows() throws URISyntaxException, IOException
    {
        // arrange
        final String connString =
                "HostName=iothub.device.com;CredentialType=SharedAccessKey;CredentialScope=Device;DeviceId=testdevice;SharedAccessKey=adjkl234j52=;";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubConnectionString.class, connString);
                result = new IllegalArgumentException();
            }
        };

        // act
        try
        {
            new DeviceClient(connString, protocol);
        }
        catch (IllegalArgumentException expected)
        {
            // Don't do anything, throw expected.
        }

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubConnectionString.class, connString);
                times = 1;
                new DeviceClientConfig((IotHubConnectionString)any);
                times = 0;
                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                        new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                        (DeviceClientConfig)any, protocol, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);
                times = 0;
            }
        };
    }

    /* Tests_SRS_DEVICECLIENT_21_002: [The constructor shall initialize the IoT Hub transport for the protocol specified, creating a instance of the deviceIO.] */
    @Test
    public void constructorBadDeviceIOThrows() throws URISyntaxException, IOException
    {
        // arrange
        final String connString =
                "HostName=iothub.device.com;CredentialType=SharedAccessKey;CredentialScope=Device;DeviceId=testdevice;SharedAccessKey=adjkl234j52=;";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                        new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                        (DeviceClientConfig)any, protocol, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);
                result = new IllegalArgumentException();
            }
        };

        // act
        try
        {
            new DeviceClient(connString, protocol);
        }
        catch (IllegalArgumentException expected)
        {
            // Don't do anything, throw expected.
        }

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubConnectionString.class, connString);
                times = 1;
                new DeviceClientConfig((IotHubConnectionString)any);
                times = 1;
                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                        new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                        (DeviceClientConfig)any, protocol, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICECLIENT_21_003: [The constructor shall save the connection configuration using the object DeviceClientConfig.] */
    @Test
    public void constructorBadDeviceClientConfigThrows() throws URISyntaxException, IOException
    {
        // arrange
        final String connString =
                "HostName=iothub.device.com;CredentialType=SharedAccessKey;CredentialScope=Device;DeviceId=testdevice;SharedAccessKey=adjkl234j52=;";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                new DeviceClientConfig((IotHubConnectionString)any);
                result = new IllegalArgumentException();
            }
        };

        // act
        try
        {
            new DeviceClient(connString, protocol);
        }
        catch (IllegalArgumentException expected)
        {
            // Don't do anything, throw expected.
        }

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubConnectionString.class, connString);
                times = 1;
                new DeviceClientConfig((IotHubConnectionString)any);
                times = 1;
                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                        new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                        (DeviceClientConfig)any, protocol, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);
                times = 0;
            }
        };
    }

    /* Tests_SRS_DEVICECLIENT_21_006: [The open shall open the deviceIO connection.] */
    @Test
    public void openOpensTransportSuccess() throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                        + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.open();

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.open();
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICECLIENT_21_007: [If the opening a connection via deviceIO is not successful, the open shall throw IOException.] */
    @Test
    public void openBadOpensTransportThrows() throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.open();
                result = new IOException();
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        try
        {
            client.open();
        }
        catch (IOException expected)
        {
            // Don't do anything, throw expected.
        }

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.open();
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICECLIENT_11_040: [The function shall finish all ongoing tasks.] */
    /* Tests_SRS_DEVICECLIENT_11_041: [The function shall cancel all recurring tasks.] */
    /* Tests_SRS_DEVICECLIENT_21_042: [The close shall close the deviceIO connection.] */
    /* Tests_SRS_DEVICECLIENT_21_043: [If the closing a connection via deviceIO is not successful, the close shall throw IOException.] */
    @Test
    public void closeClosesTransportSuccess() throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isEmpty();
                result = true;
            }
        };

        // act
        client.close();

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

    /* Tests_SRS_DEVICECLIENT_11_040: [The function shall finish all ongoing tasks.] */
    /* Tests_SRS_DEVICECLIENT_11_041: [The function shall cancel all recurring tasks.] */
    /* Tests_SRS_DEVICECLIENT_21_042: [The close shall close the deviceIO connection.] */
    /* Tests_SRS_DEVICECLIENT_21_043: [If the closing a connection via deviceIO is not successful, the close shall throw IOException.] */
    @Test
    public void closeWaitAndClosesTransportSuccess() throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isEmpty();
                returns(false, false, true);
            }
        };

        // act
        client.close();

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

    /* Tests_SRS_DEVICECLIENT_21_008: [The closeNow shall close the deviceIO connection.] */
    @Test
    public void closeNowClosesTransportSuccess() throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();

        // act
        client.closeNow();

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.close();
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICECLIENT_21_009: [If the closing a connection via deviceIO is not successful, the close shall throw IOException.] */
    @Test
    public void closeNowBadCloseTransportThrows() throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.close();
                result = new IOException();
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();

        // act
        try
        {
            client.closeNow();
        }
        catch (IOException expected)
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

    /* Tests_SRS_DEVICECLIENT_21_010: [The sendEventAsync shall asynchronously send the message using the deviceIO connection.] */
    @Test
    public void sendEventAsyncSendsSuccess(
            @Mocked final Message mockMessage,
            @Mocked final IotHubEventCallback mockCallback)
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final Map<String, Object> context = new HashMap<>();
        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();

        // act
        client.sendEventAsync(mockMessage, mockCallback, context);

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.sendEventAsync(mockMessage, mockCallback, context);
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICECLIENT_21_011: [If starting to send via deviceIO is not successful, the sendEventAsync shall bypass the threw exception.] */
    @Test
    public void sendEventAsyncBadSendThrows(
            @Mocked final Message mockMessage,
            @Mocked final IotHubEventCallback mockCallback)
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final Map<String, Object> context = new HashMap<>();
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.sendEventAsync(mockMessage, mockCallback, context);
                result = new IllegalStateException();
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        try
        {
            client.sendEventAsync(mockMessage, mockCallback, context);
        }
        catch (IllegalStateException expected)
        {
            // Don't do anything, throw expected.
        }

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.sendEventAsync(mockMessage, mockCallback, context);
                times = 1;
            }
        };
    }

    // Tests_SRS_DEVICECLIENT_11_013: [The function shall set the message callback, with its associated context.]
    @Test
    public void setMessageCallbackSetsMessageCallback(
            @Mocked final MessageCallback mockCallback)
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final Map<String, Object> context = new HashMap<>();
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.setMessageCallback(mockCallback, context);

        // assert
        new Verifications()
        {
            {
                mockConfig.setMessageCallback(mockCallback, context);
                times = 1;
            }
        };
    }

    // Tests_SRS_DEVICECLIENT_11_014: [If the callback is null but the context is non-null, the function shall throw an IllegalArgumentException.]
    @Test(expected = IllegalArgumentException.class)
    public void setMessageCallbackRejectsNullCallbackAndNonnullContext()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final Map<String, Object> context = new HashMap<>();
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.setMessageCallback(null, context);
    }

    /*
    **Tests_SRS_DEVICECLIENT_25_025: [**The function shall create a new instance of class Device Twin and request all twin properties by calling getDeviceTwin**]**
     */
    @Test
    public void startDeviceTwinSucceeds(@Mocked final DeviceTwin mockedDeviceTwin,
                                        @Mocked final IotHubEventCallback mockedStatusCB,
                                        @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        //arrange
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

        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();

        //act
        client.startDeviceTwin(mockedStatusCB, null, mockedPropertyCB, null);

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
    **Tests_SRS_DEVICECLIENT_25_026: [**If the deviceTwinStatusCallback or genericPropertyCallBack is null, the function shall throw an InvalidParameterException.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void startDeviceTwinThrowsIfStatusCBisNull(@Mocked final DeviceTwin mockedDeviceTwin,
                                                      @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException

    {
        //arrange
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

        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();

        //act
        client.startDeviceTwin(null, null, mockedPropertyCB, null);

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
    **Tests_SRS_DEVICECLIENT_25_026: [**If the deviceTwinStatusCallback or genericPropertyCallBack is null, the function shall throw an InvalidParameterException.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void startDeviceTwinThrowsIfPropCBisNull(@Mocked final DeviceTwin mockedDeviceTwin,
                                                      @Mocked final IotHubEventCallback mockedStatusCB) throws IOException, URISyntaxException

    {
        //arrange
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

        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();

        //act
        client.startDeviceTwin(mockedStatusCB, null, null, null);

    }

    /*
    **Tests_SRS_DEVICECLIENT_25_028: [**If this method is called twice on the same instance of the client then this method shall throw UnsupportedOperationException.**]**
     */
    @Test
    public void startDeviceTwinThrowsIfCalledTwice(@Mocked final DeviceTwin mockedDeviceTwin,
                                                   @Mocked final IotHubEventCallback mockedStatusCB,
                                                   @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException

    {
        //arrange
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

        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();
        client.startDeviceTwin(mockedStatusCB, null, mockedPropertyCB, null);

        //act
        try
        {
            client.startDeviceTwin(mockedStatusCB, null, mockedPropertyCB, null);
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
    **Tests_SRS_DEVICECLIENT_25_027: [**If the client has not been open, the function shall throw an IOException.**]**
     */
    @Test (expected = IOException.class)
    public void startDeviceTwinThrowsIfCalledWhenClientNotOpen(@Mocked final IotHubEventCallback mockedStatusCB,
                                                               @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException

    {
        //arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
            }
        };

        DeviceClient client = new DeviceClient(connString, protocol);

        //act
        client.startDeviceTwin(mockedStatusCB, null, mockedPropertyCB, null);
    }

    /*
    **Tests_SRS_DEVICECLIENT_25_031: [**This method shall subscribe to desired properties by calling subscribeDesiredPropertiesNotification on the twin object.**]**
     */
    @Test
    public void subscribeToDPSucceeds(@Mocked final DeviceTwin mockedDeviceTwin,
                                      @Mocked final IotHubEventCallback mockedStatusCB,
                                      @Mocked final PropertyCallBack mockedPropertyCB,
                                      @Mocked final Map<Property, Pair<PropertyCallBack<String, Object>, Object>> mockMap) throws IOException, URISyntaxException

    {
        //arrange
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
        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();
        client.startDeviceTwin(mockedStatusCB, null, mockedPropertyCB, null);

        //act
        client.subscribeToDesiredProperties(mockMap);

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
        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();
        client.startDeviceTwin(mockedStatusCB, null, mockedPropertyCB, null);

        //act
        client.subscribeToDesiredProperties(null);

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.subscribeDesiredPropertiesNotification((Map)any);
                times = 1;
            }
        };

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
        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();
        client.startDeviceTwin(mockedStatusCB, null, mockedPropertyCB, null);
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

    /*
    **Tests_SRS_DEVICECLIENT_25_030: [**If the client has not been open, the function shall throw an IOException.**]**
     */
    @Test
    public void subscribeToDPThrowsIfCalledWhenClientNotOpen(@Mocked final DeviceTwin mockedDeviceTwin,
                                                             @Mocked final IotHubEventCallback mockedStatusCB,
                                                             @Mocked final PropertyCallBack mockedPropertyCB,
                                                             @Mocked final Map<Property, Pair<PropertyCallBack<String, Object>, Object>> mockMap) throws IOException, URISyntaxException

    {
        //arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                returns(true,false);
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();
        client.startDeviceTwin(mockedStatusCB, null, mockedPropertyCB, null);

        //act
        try
        {
            client.subscribeToDesiredProperties(mockMap);
        }
        catch (IOException expected)
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
    **Tests_SRS_DEVICECLIENT_25_029: [**If the client has not started twin before calling this method, the function shall throw an IOException.**]**
     */
    @Test
    public void subscribeToDPThrowsIfCalledBeforeStartingTwin(@Mocked final DeviceTwin mockedDeviceTwin,
                                                              @Mocked final Map<Property, Pair<PropertyCallBack<String, Object>, Object>> mockMap) throws IOException, URISyntaxException

    {
        //arrange
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
        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();

        //act
        try
        {
            client.subscribeToDesiredProperties(mockMap);
        }
        catch (IOException expected)
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
    **Tests_SRS_DEVICECLIENT_25_035: [**This method shall send to reported properties by calling updateReportedProperties on the twin object.**]**
     */
    @Test
    public void sendRPSucceeds(@Mocked final DeviceTwin mockedDeviceTwin,
                               @Mocked final IotHubEventCallback mockedStatusCB,
                               @Mocked final PropertyCallBack mockedPropertyCB,
                               @Mocked final Set<Property> mockSet) throws IOException, URISyntaxException
    {
        //arrange
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
        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();
        client.startDeviceTwin(mockedStatusCB, null, mockedPropertyCB, null);

        //act
        client.sendReportedProperties(mockSet);

        //assert
        new Verifications()
        {
            {
                mockedDeviceTwin.updateReportedProperties(mockSet);
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_DEVICECLIENT_25_032: [**If the client has not started twin before calling this method, the function shall throw an IOException.**]**
     */
    @Test
    public void sendRPThrowsIfCalledBeforeStartingTwin(@Mocked final DeviceTwin mockedDeviceTwin,
                                                       @Mocked final Set<Property> mockSet) throws IOException, URISyntaxException
    {
        //arrange
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
        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();

        //act
        try
        {
            client.sendReportedProperties(mockSet);
        }
        catch (IOException expected)
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

    /*
    **Tests_SRS_DEVICECLIENT_25_033: [**If the client has not been open, the function shall throw an IOException.**]**
     */
    @Test
    public void sendRPThrowsIfCalledWhenClientNotOpen(@Mocked final DeviceTwin mockedDeviceTwin,
                                                      @Mocked final Set<Property> mockSet) throws IOException, URISyntaxException
    {
        //arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol);

        //act
        try
        {
            client.sendReportedProperties(mockSet);
        }
        catch (IOException expected)
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

    /*
    **Tests_SRS_DEVICECLIENT_25_034: [**If reportedProperties is null or empty, the function shall throw an InvalidParameterException.**]**
     */
    @Test
    public void sendRPThrowsIfCalledWhenRPNullOrEmpty(@Mocked final DeviceTwin mockedDeviceTwin,
                                                      @Mocked final IotHubEventCallback mockedStatusCB,
                                                      @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        //arrange
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
        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();
        client.startDeviceTwin(mockedStatusCB, null, mockedPropertyCB, null);

        //act
        try
        {
            client.sendReportedProperties(null);
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

    /*
    Tests_SRS_DEVICECLIENT_25_038: [**This method shall subscribe to device methods by calling subscribeToDeviceMethod on DeviceMethod object which it created.**]**
     */
    @Test
    public void subscribeToDeviceMethodSucceeds(@Mocked final IotHubEventCallback mockedStatusCB,
                                                @Mocked final DeviceMethodCallback mockedDeviceMethodCB,
                                                @Mocked final DeviceMethod mockedMethod) throws IOException, URISyntaxException
    {
        //arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        final DeviceClient client = new DeviceClient(connString, protocol);
        client.open();

        //act
        client.subscribeToDeviceMethod(mockedDeviceMethodCB, null, mockedStatusCB, null);

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
    Tests_SRS_DEVICECLIENT_25_036: [**If the client has not been open, the function shall throw an IOException.**]**
     */
    @Test (expected = IOException.class)
    public void subscribeToDeviceMethodThrowsIfClientNotOpen(@Mocked final IotHubEventCallback mockedStatusCB,
                                                             @Mocked final DeviceMethodCallback mockedDeviceMethodCB)
            throws IOException, URISyntaxException
    {
        //arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
            }
        };
        final DeviceClient client = new DeviceClient(connString, protocol);

        //act
        client.subscribeToDeviceMethod(mockedDeviceMethodCB, null, mockedStatusCB, null);
    }

    /*
    Tests_SRS_DEVICECLIENT_25_037: [**If deviceMethodCallback or deviceMethodStatusCallback is null, the function shall throw an IllegalArgumentException.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void subscribeToDeviceMethodThrowsIfDeviceMethodCallbackNull(@Mocked final IotHubEventCallback mockedStatusCB)
            throws IOException, URISyntaxException
    {
        //arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        final DeviceClient client = new DeviceClient(connString, protocol);
        client.open();

        //act
        client.subscribeToDeviceMethod(null, null, mockedStatusCB, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void subscribeToDeviceMethodThrowsIfDeviceMethodStatusCallbackNull(@Mocked final DeviceMethodCallback mockedDeviceMethodCB)
            throws IOException, URISyntaxException
    {
        //arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        final DeviceClient client = new DeviceClient(connString, protocol);
        client.open();

        //act
        client.subscribeToDeviceMethod(mockedDeviceMethodCB, null, null, null);
    }

    /*
    Tests_SRS_DEVICECLIENT_25_039: [**This method shall update the deviceMethodCallback if called again, but it shall not subscribe twice.**]**
     */
    @Test
    public void subscribeToDeviceMethodWorksEvenWhenCalledTwice(@Mocked final IotHubEventCallback mockedStatusCB,
                                                                @Mocked final DeviceMethodCallback mockedDeviceMethodCB,
                                                                @Mocked final DeviceMethod mockedMethod) throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };
        final DeviceClient client = new DeviceClient(connString, protocol);
        client.open();
        client.subscribeToDeviceMethod(mockedDeviceMethodCB, null, mockedStatusCB, null);

        // act
        client.subscribeToDeviceMethod(mockedDeviceMethodCB, null, mockedStatusCB, null);

        // assert
        new Verifications()
        {
            {
                mockedMethod.subscribeToDeviceMethod(mockedDeviceMethodCB, any);
                times = 2;
            }
        };
    }

    // Tests_SRS_DEVICECLIENT_02_015: [If optionName is null or not an option handled by the client, then it shall throw IllegalArgumentException.]
    @Test(expected = IllegalArgumentException.class)
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
        DeviceClient client = new DeviceClient(connString, protocol);

        long someMilliseconds = 4;

        // act
        client.setOption(null, someMilliseconds);
    }

    // Tests_SRS_DEVICECLIENT_02_015: [If optionName is null or not an option handled by the client, then it shall throw IllegalArgumentException.]
    @Test(expected = IllegalArgumentException.class)
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
        DeviceClient client = new DeviceClient(connString, protocol);

        long someMilliseconds = 4;

        // act
        client.setOption("thisIsNotAHandledOption", someMilliseconds);
    }

    //Tests_SRS_DEVICECLIENT_02_017: [Available only for HTTP.]
    @Test(expected = IllegalArgumentException.class)
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
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.setOption("SetMinimumPollingInterval", someMilliseconds);
    }

    //Tests_SRS_DEVICECLIENT_02_018: [Value needs to have type long].
    @Test(expected = IllegalArgumentException.class)
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
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.setOption("SetMinimumPollingInterval", "thisIsNotALong");
    }

    //Tests_SRS_DEVICECLIENT_02_005: [Setting the option can only be done before open call.]
    @Test(expected = IllegalStateException.class)
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
        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();
        long value = 3;

        // act
        client.setOption("SetMinimumPollingInterval", value);
    }

    //Tests_SRS_DEVICECLIENT_02_016: ["SetMinimumPollingInterval" - time in milliseconds between 2 consecutive polls.]
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
        DeviceClient client = new DeviceClient(connString, protocol);
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

    // Tests_SRS_DEVICECLIENT_21_040: ["SetSendInterval" - time in milliseconds between 2 consecutive message sends.]
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
        DeviceClient client = new DeviceClient(connString, protocol);
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

    // Tests_SRS_DEVICECLIENT_21_041: ["SetSendInterval" needs to have value type long.]
    @Test(expected = IllegalArgumentException.class)
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
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.setOption("SetSendInterval", "thisIsNotALong");
    }

    //Tests_SRS_DEVICECLIENT_25_022: [**"SetSASTokenExpiryTime" should have value type long.]
    @Test(expected = IllegalArgumentException.class)
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
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.setOption("SetSASTokenExpiryTime", "thisIsNotALong");
    }

    //Tests_SRS_DEVICECLIENT_25_021: ["SetSASTokenExpiryTime" - time in seconds after which SAS Token expires.]
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
        DeviceClient client = new DeviceClient(connString, protocol);
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

    //Tests_SRS_DEVICECLIENT_25_021: ["SetSASTokenExpiryTime" - time in seconds after which SAS Token expires.]
    //Tests_SRS_DEVICECLIENT_25_024: ["SetSASTokenExpiryTime" shall restart the transport if transport is already open after updating expiry time.]
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
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = anyString;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;

        DeviceClient client = new DeviceClient(connString, protocol);
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
                mockDeviceIO.open();
                times = 2;

            }
        };
    }

    /*Tests_SRS_DEVICECLIENT_25_024: ["SetSASTokenExpiryTime" shall restart the transport
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
            }
        };
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessSignature=SharedAccessSignature sr=sample-iothub-hostname.net%2fdevices%2fsample-device-ID&sig=S3%2flPidfBF48B7%2fOFAxMOYH8rpOneq68nu61D%2fBP6fo%3d&se=1469813873";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;

        DeviceClient client = new DeviceClient(connString, protocol);
        client.open();
        final long value = 60;

        // act
        client.setOption("SetSASTokenExpiryTime", value);

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.close();
                times = 0;
                mockConfig.getSasTokenAuthentication().setTokenValidSecs(value);
                times = 1;
                mockDeviceIO.open();
                times = 1;

            }
        };
    }
    //Tests_SRS_DEVICECLIENT_25_021: ["SetSASTokenExpiryTime" - Time in secs to specify SAS Token Expiry time.]
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

        DeviceClient client = new DeviceClient(connString, protocol);
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

    //Tests_SRS_DEVICECLIENT_25_021: ["SetSASTokenExpiryTime" - time in seconds after which SAS Token expires.]
    //Tests_SRS_DEVICECLIENT_25_024: ["SetSASTokenExpiryTime" shall restart the transport if transport is already open after updating expiry time.]
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
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = anyString;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        DeviceClient client = new DeviceClient(connString, protocol);
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
                mockDeviceIO.open();
                times = 2;

            }
        };
    }

    //Tests_SRS_DEVICECLIENT_25_021: [**"SetSASTokenExpiryTime" - Time in secs to specify SAS Token Expiry time.]
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
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = anyString;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

        DeviceClient client = new DeviceClient(connString, protocol);
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

    //Tests_SRS_DEVICECLIENT_25_021: ["SetSASTokenExpiryTime" - time in seconds after which SAS Token expires.]
    //Tests_SRS_DEVICECLIENT_25_024: ["SetSASTokenExpiryTime" shall restart the transport if transport is already open after updating expiry time.]
    @Test
    public void setOptionSASTokenExpiryTimeAfterClientOpenMQTTSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations() {
            {
                mockDeviceIO.isOpen();
                result = true;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = anyString;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

        DeviceClient client = new DeviceClient(connString, protocol);
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
                mockDeviceIO.open();
                times = 2;

            }
        };
    }

    //Tests_SRS_DEVICECLIENT_34_055: [If the provided connection string contains an expired SAS token, a SecurityException shall be thrown.]
    @Test (expected = SecurityException.class)
    public void deviceClientInitializedWithExpiredTokenThrowsSecurityException() throws SecurityException, URISyntaxException, IOException
    {
        //This token will always be expired
        final Long expiryTime = 0L;
        final String expiredConnString = "HostName=iothub.device.com;DeviceId=2;SharedAccessSignature=SharedAccessSignature sr=hub.azure-devices.net%2Fdevices%2F2&sig=3V1oYPdtyhGPHDDpjS2SnwxoU7CbI%2BYxpLjsecfrtgY%3D&se=" + expiryTime;
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        new NonStrictExpectations()
        {
            {
                new IotHubConnectionString(anyString);
                result = new SecurityException();

                mockConfig.getIotHubConnectionString().getSharedAccessToken();
                result = expiredConnString;
            }
        };

        // act
        DeviceClient client = new DeviceClient(expiredConnString, protocol);
    }

    //Tests_SRS_DEVICECLIENT_34_044: [**If the SAS token has expired before this call, throw a Security Exception**]
    @Test (expected = SecurityException.class)
    public void tokenExpiresAfterDeviceClientInitializedBeforeOpen() throws SecurityException, URISyntaxException, IOException
    {
        final Long expiryTime = Long.MAX_VALUE;
        final String connString = "HostName=iothub.device.com;DeviceId=2;SharedAccessSignature=SharedAccessSignature sr=hub.azure-devices.net%2Fdevices%2F2&sig=3V1oYPdtyhGPHDDpjS2SnwxoU7CbI%2BYxpLjsecfrtgY%3D&se=" + expiryTime;
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        DeviceClient client = new DeviceClient(connString, protocol);

        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;

                mockConfig.getSasTokenAuthentication().isRenewalNecessary();
                result = true;

                mockConfig.getIotHubConnectionString().getSharedAccessToken();
                result = "SharedAccessSignature sr=hub.azure-devices.net%2Fdevices%2F2&sig=3V1oYPdtyhGPHDDpjS2SnwxoU7CbI%2BYxpLjsecfrtgY%3D&se=" + expiryTime;
            }
        };

        // act
        client.open();
    }

    /* Tests_SRS_DEVICECLIENT_21_044: [The uploadToBlobAsync shall asynchronously upload the stream in `inputStream` to the blob in `destinationBlobName`.] */
    /* Tests_SRS_DEVICECLIENT_21_048: [If there is no instance of the FileUpload, the uploadToBlobAsync shall create a new instance of the FileUpload.] */
    /* Tests_SRS_DEVICECLIENT_21_050: [The uploadToBlobAsync shall start the stream upload process, by calling uploadToBlobAsync on the FileUpload class.] */
    @Test
    public void startFileUploadSucceeds(@Mocked final FileUpload mockedFileUpload,
                                        @Mocked final InputStream mockInputStream,
                                        @Mocked final IotHubEventCallback mockedStatusCB,
                                        @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String destinationBlobName = "valid/blob/name.txt";
        final long streamLength = 100;

        deviceClientInstanceExpectation(connString, protocol);
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(FileUpload.class, mockConfig);
                result = mockedFileUpload;
                Deencapsulation.invoke(mockedFileUpload, "uploadToBlobAsync",
                        destinationBlobName, mockInputStream, streamLength, mockedStatusCB, mockedPropertyCB);
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.uploadToBlobAsync(destinationBlobName, mockInputStream, streamLength, mockedStatusCB, mockedPropertyCB);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(FileUpload.class, mockConfig);
                times = 1;
                Deencapsulation.invoke(mockedFileUpload, "uploadToBlobAsync",
                        destinationBlobName, mockInputStream, streamLength, mockedStatusCB, mockedPropertyCB);
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICECLIENT_21_054: [If the fileUpload is not null, the closeNow shall call closeNow on fileUpload.] */
    @Test
    public void closeNowClosesFileUploadSucceeds(@Mocked final FileUpload mockedFileUpload,
                                                 @Mocked final InputStream mockInputStream,
                                                 @Mocked final IotHubEventCallback mockedStatusCB,
                                                 @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String destinationBlobName = "valid/blob/name.txt";
        final long streamLength = 100;

        deviceClientInstanceExpectation(connString, protocol);
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(FileUpload.class, mockConfig);
                result = mockedFileUpload;
                Deencapsulation.invoke(mockedFileUpload, "uploadToBlobAsync",
                        destinationBlobName, mockInputStream, streamLength, mockedStatusCB, mockedPropertyCB);
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol);

        client.uploadToBlobAsync(destinationBlobName, mockInputStream, streamLength, mockedStatusCB, mockedPropertyCB);

        // act
        client.closeNow();

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedFileUpload, "closeNow");
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICECLIENT_21_045: [If the `callback` is null, the uploadToBlobAsync shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void startFileUploadNullCallbackThrows(@Mocked final InputStream mockInputStream,
                                                  @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String destinationBlobName = "valid/blob/name.txt";
        final long streamLength = 100;

        deviceClientInstanceExpectation(connString, protocol);
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.uploadToBlobAsync(destinationBlobName, mockInputStream, streamLength, null, mockedPropertyCB);
    }

    /* Tests_SRS_DEVICECLIENT_21_046: [If the `inputStream` is null, the uploadToBlobAsync shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void startFileUploadNullInputStreamThrows(@Mocked final IotHubEventCallback mockedStatusCB,
                                                     @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String destinationBlobName = "valid/blob/name.txt";
        final long streamLength = 100;

        deviceClientInstanceExpectation(connString, protocol);
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.uploadToBlobAsync(destinationBlobName, (InputStream) null, streamLength, mockedStatusCB, mockedPropertyCB);
    }

    /* Tests_SRS_DEVICECLIENT_21_052: [If the `streamLength` is negative, the uploadToBlobAsync shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void startFileUploadNegativeLengthThrows(@Mocked final IotHubEventCallback mockedStatusCB,
                                                     @Mocked final InputStream mockInputStream,
                                                     @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String destinationBlobName = "valid/blob/name.txt";

        deviceClientInstanceExpectation(connString, protocol);
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.uploadToBlobAsync(destinationBlobName, mockInputStream, -1, mockedStatusCB, mockedPropertyCB);
    }

    /* Tests_SRS_DEVICECLIENT_21_047: [If the `destinationBlobName` is null, empty, or not valid, the uploadToBlobAsync shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void startFileUploadNullBlobNameThrows(@Mocked final InputStream mockInputStream,
                                                  @Mocked final IotHubEventCallback mockedStatusCB,
                                                  @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final long streamLength = 100;

        deviceClientInstanceExpectation(connString, protocol);
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.uploadToBlobAsync(null, mockInputStream, streamLength, mockedStatusCB, mockedPropertyCB);
    }

    /* Tests_SRS_DEVICECLIENT_21_047: [If the `destinationBlobName` is null, empty, or not valid, the uploadToBlobAsync shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void startFileUploadEmptyBlobNameThrows(@Mocked final InputStream mockInputStream,
                                                   @Mocked final IotHubEventCallback mockedStatusCB,
                                                   @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final long streamLength = 100;

        deviceClientInstanceExpectation(connString, protocol);
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.uploadToBlobAsync("", mockInputStream, streamLength, mockedStatusCB, mockedPropertyCB);
    }

    /* Tests_SRS_DEVICECLIENT_21_047: [If the `destinationBlobName` is null, empty, or not valid, the uploadToBlobAsync shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void startFileUploadInvalidUTF8BlobNameThrows(@Mocked final InputStream mockInputStream,
                                                         @Mocked final IotHubEventCallback mockedStatusCB,
                                                         @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String destinationBlobName = "valid\u1234/blob/name.txt";
        final long streamLength = 100;

        deviceClientInstanceExpectation(connString, protocol);
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.uploadToBlobAsync(destinationBlobName, mockInputStream, streamLength, mockedStatusCB, mockedPropertyCB);
    }

    /* Tests_SRS_DEVICECLIENT_21_047: [If the `destinationBlobName` is null, empty, or not valid, the uploadToBlobAsync shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void startFileUploadInvalidBigBlobNameThrows(@Mocked final InputStream mockInputStream,
                                                        @Mocked final IotHubEventCallback mockedStatusCB,
                                                        @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        StringBuilder bigBlobName = new StringBuilder();
        String directory = "directory/";
        final long streamLength = 100;

        // create a blob name bigger than 1024 characters.
        for (int i = 0; i < (2000/directory.length()); i++)
        {
            bigBlobName.append(directory);
        }
        bigBlobName.append("image.jpg");
        final String destinationBlobName = bigBlobName.toString();

        deviceClientInstanceExpectation(connString, protocol);

        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.uploadToBlobAsync(destinationBlobName, mockInputStream, streamLength, mockedStatusCB, mockedPropertyCB);
    }

    /* Tests_SRS_DEVICECLIENT_21_047: [If the `destinationBlobName` is null, empty, or not valid, the uploadToBlobAsync shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void startFileUploadInvalidPathBlobNameThrows(@Mocked final InputStream mockInputStream,
                                                         @Mocked final IotHubEventCallback mockedStatusCB,
                                                         @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        StringBuilder bigBlobName = new StringBuilder();
        String directory = "a/";
        final long streamLength = 100;

        // create a blob name with more than 254 path segments.
        for (int i = 0; i < 300; i++)
        {
            bigBlobName.append(directory);
        }
        bigBlobName.append("image.jpg");
        final String destinationBlobName = bigBlobName.toString();

        deviceClientInstanceExpectation(connString, protocol);

        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.uploadToBlobAsync(destinationBlobName, mockInputStream, streamLength, mockedStatusCB, mockedPropertyCB);
    }

    /* Tests_SRS_DEVICECLIENT_21_048: [If there is no instance of the FileUpload, the uploadToBlobAsync shall create a new instance of the FileUpload.] */
    @Test
    public void startFileUploadOneFileUploadInstanceSucceeds(@Mocked final FileUpload mockedFileUpload,
                                                             @Mocked final InputStream mockInputStream,
                                                             @Mocked final IotHubEventCallback mockedStatusCB,
                                                             @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String destinationBlobName = "valid/blob/name.txt";
        final long streamLength = 100;

        deviceClientInstanceExpectation(connString, protocol);
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(FileUpload.class, mockConfig);
                result = mockedFileUpload;
                Deencapsulation.invoke(mockedFileUpload, "uploadToBlobAsync",
                                       destinationBlobName, mockInputStream, streamLength, mockedStatusCB, mockedPropertyCB);
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol);
        client.uploadToBlobAsync(destinationBlobName, mockInputStream, streamLength, mockedStatusCB, mockedPropertyCB);

        // act
        client.uploadToBlobAsync(destinationBlobName, mockInputStream, streamLength, mockedStatusCB, mockedPropertyCB);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(FileUpload.class, mockConfig);
                times = 1;
                Deencapsulation.invoke(mockedFileUpload, "uploadToBlobAsync",
                                       destinationBlobName, mockInputStream, streamLength, mockedStatusCB, mockedPropertyCB);
                times = 2;

            }
        };
    }

    //Tests_SRS_DEVICECLIENT_99_001: [The registerConnectionStateCallback shall register the callback with the Device IO.]
    //Tests_SRS_DEVICECLIENT_99_002: [The registerConnectionStateCallback shall register the callback even if the client is not open.]
    @Test
    public void registerConnectionStateCallback(@Mocked final IotHubConnectionStateCallback mockedStateCB) throws URISyntaxException, IOException
    {
        //arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        final DeviceClient client = new DeviceClient(connString, protocol);
        
        //act
        client.registerConnectionStateCallback(mockedStateCB, null);

        //assert
        new Verifications()
        {
            {
                mockDeviceIO.registerConnectionStateCallback(mockedStateCB, null);
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICECLIENT_99_003: [If the callback is null the method shall throw an IllegalArgument exception.]
    @Test (expected = IllegalArgumentException.class)
    public void registerConnectionStateCallbackNullCallback()
            throws IllegalArgumentException, URISyntaxException, IOException
    {
        //arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        final DeviceClient client = new DeviceClient(connString, protocol);

        //act
        client.registerConnectionStateCallback(null, null);
    }
    
    /* Tests_SRS_DEVICECLIENT_21_049: [If uploadToBlobAsync failed to create a new instance of the FileUpload, it shall bypass the exception.] */
    @Test (expected = IllegalArgumentException.class)
    public void startFileUploadNewInstanceThrows(@Mocked final FileUpload mockedFileUpload,
                                                 @Mocked final InputStream mockInputStream,
                                                 @Mocked final IotHubEventCallback mockedStatusCB,
                                                 @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String destinationBlobName = "valid/blob/name.txt";
        final long streamLength = 100;

        deviceClientInstanceExpectation(connString, protocol);
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(FileUpload.class, mockConfig);
                result = new IllegalArgumentException();
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.uploadToBlobAsync(destinationBlobName, mockInputStream, streamLength, mockedStatusCB, mockedPropertyCB);
    }

    /* Tests_SRS_DEVICECLIENT_21_051: [If uploadToBlobAsync failed to start the upload using the FileUpload, it shall bypass the exception.] */
    @Test (expected = IllegalArgumentException.class)
    public void startFileUploadUploadToBlobAsyncThrows(@Mocked final FileUpload mockedFileUpload,
                                                       @Mocked final InputStream mockInputStream,
                                                       @Mocked final IotHubEventCallback mockedStatusCB,
                                                       @Mocked final PropertyCallBack mockedPropertyCB) throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;DeviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String destinationBlobName = "valid/blob/name.txt";
        final long streamLength = 100;

        deviceClientInstanceExpectation(connString, protocol);
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(FileUpload.class, mockConfig);
                result = mockedFileUpload;
                Deencapsulation.invoke(mockedFileUpload, "uploadToBlobAsync",
                        destinationBlobName, mockInputStream, streamLength, mockedStatusCB, mockedPropertyCB);
                result = new IllegalArgumentException();
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.uploadToBlobAsync(destinationBlobName, mockInputStream, streamLength, mockedStatusCB, mockedPropertyCB);
    }

    //Tests_SRS_DEVICECLIENT_34_065: [""SetSASTokenExpiryTime" if this option is called when not using sas token authentication, an IllegalStateException shall be thrown.*]
    @Test(expected = IllegalStateException.class)
    public void setOptionSASTokenExpiryTimeWhenNotUsingSasTokenAuthThrows() throws IOException, URISyntaxException
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
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.X509_CERTIFICATE;
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol, "someCert", false, "someKey", false);

        // act
        client.setOption("SetSASTokenExpiryTime", 25L);
    }

    //Tests_SRS_DEVICECLIENT_34_066: [If this function is called when the device client is using x509 authentication, an UnsupportedOperationException shall be thrown.]
    @Test (expected = UnsupportedOperationException.class)
    public void uploadToBlobUsingX509ThrowsUnsupportedOperationException(@Mocked final InputStream mockInputStream,
                                                                         @Mocked final IotHubEventCallback mockedStatusCB,
                                                                         @Mocked final PropertyCallBack mockedPropertyCB) throws URISyntaxException, IOException
    {
        // arrange
        final String publicKeyCert = "someCert";
        final String privateKey = "someKey";
        final String connString = "HostName=iothub.device.com;DeviceId=testdevice;x509=true";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String destinationBlobName = "valid/blob/name.txt";
        final long streamLength = 100;
        DeviceClient client = new DeviceClient(connString, protocol, publicKeyCert, false, privateKey, false);

        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.X509_CERTIFICATE;
            }
        };

        // act
        client.uploadToBlobAsync(destinationBlobName, mockInputStream, streamLength, mockedStatusCB, mockedPropertyCB);
    }
}
