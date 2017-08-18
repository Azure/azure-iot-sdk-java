// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubReceiveTask;
import com.microsoft.azure.sdk.iot.device.transport.IotHubSendTask;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransport;
import com.microsoft.azure.sdk.iot.device.transport.amqps.AmqpsTransport;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransport;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttTransport;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Unit tests for DeviceIO.
 * Coverage : 100% method, 99% line
 */
public class DeviceIOTest
{
    @Mocked
    IotHubSendTask mockIotHubSendTask;

    @Mocked
    IotHubReceiveTask mockIotHubReceiveTask;

    @Mocked 
    AmqpsTransport mockAmqpsTransport;

    @Mocked
    HttpsTransport mockHttpsTransport;

    @Mocked
    MqttTransport mockMqttTransport;
    
    @Mocked 
    DeviceClientConfig mockConfig;

    @Mocked
    Executors mockExecutors;

    @Mocked ScheduledExecutorService mockScheduler;

    private final static long SEND_PERIOD_MILLIS = 10L;
    private final static long RECEIVE_PERIOD_MILLIS_AMQPS = 10L;
    private final static long RECEIVE_PERIOD_MILLIS_MQTT = 10L;
    private final static long RECEIVE_PERIOD_MILLIS_HTTPS = 25*60*1000; /*25 minutes*/

    private Object newDeviceIOAmqp()
    {
        new NonStrictExpectations()
        {
            {
                new AmqpsTransport(mockConfig);
                result = mockAmqpsTransport;
            }
        };

        final Object deviceIO = Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                mockConfig, IotHubClientProtocol.AMQPS, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());

        return deviceIO;
    }

    private Object newDeviceIOHttps()
    {
        new NonStrictExpectations()
        {
            {
                new HttpsTransport(mockConfig);
                result = mockHttpsTransport;
            }
        };

        final Object deviceIO = Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                mockConfig, IotHubClientProtocol.HTTPS, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_HTTPS);
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());

        return deviceIO;
    }

    private void openDeviceIO(
            final Object deviceIO,
            final IotHubTransport transport,
            final DeviceClientConfig config,
            final IotHubSSLContext iotHubSSLContext,
            final Executors executors,
            final ScheduledExecutorService scheduledExecutorService) throws IOException
    {
        new NonStrictExpectations()
        {
            {
                config.getPathToCertificate();
                result = null;
                config.getUserCertificateString();
                result = null;
                Deencapsulation.newInstance(IotHubSSLContext.class);
                result = iotHubSSLContext;
                new IotHubSendTask(transport);
                result = mockIotHubSendTask;
                new IotHubReceiveTask(transport);
                result = mockIotHubReceiveTask;
                executors.newScheduledThreadPool(2);
                result = scheduledExecutorService;
            }
        };

        Deencapsulation.invoke(deviceIO, "open");
        assertEquals("OPEN", Deencapsulation.getField(deviceIO, "state").toString());
    }

    /* Tests_SRS_DEVICE_IO_21_001: [The constructor shall store the provided protocol and config information.] */
    /* Tests_SRS_DEVICE_IO_21_003: [The constructor shall initialize the IoT Hub transport that uses the `protocol` specified.] */
    /* Tests_SRS_DEVICE_IO_21_006: [The constructor shall set the `state` as `CLOSED`.] */
    /* Tests_SRS_DEVICE_IO_21_037: [The constructor shall initialize the `sendPeriodInMilliseconds` with default value of 10 milliseconds.] */
    /* Tests_SRS_DEVICE_IO_21_038: [The constructor shall initialize the `receivePeriodInMilliseconds` with default value of each protocol.] */
    @Test
    public void constructorAmqpSuccess() throws URISyntaxException
    {
        // arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        // assert
        new NonStrictExpectations()
        {
            {
                new AmqpsTransport(mockConfig);
                result = mockAmqpsTransport;
                times = 1;
            }
        };

        // act
        Object deviceIO = Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                mockConfig, protocol, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);

        // assert
        assertEquals(mockConfig, Deencapsulation.getField(deviceIO, "config"));
        assertEquals(protocol, Deencapsulation.getField(deviceIO, "protocol"));
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());
        assertEquals(SEND_PERIOD_MILLIS, Deencapsulation.getField(deviceIO, "sendPeriodInMilliseconds"));
        assertEquals(RECEIVE_PERIOD_MILLIS_AMQPS, Deencapsulation.getField(deviceIO, "receivePeriodInMilliseconds"));

        new Verifications()
        {
            {
                mockConfig.setUseWebsocket(false);
                times = 1;
            }
        };
    }
    
    /* Tests_SRS_DEVICE_IO_21_002: [If the `config` is null, the constructor shall throw an IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorConnectionStringThrows()
            throws URISyntaxException
    {
        // arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        // act
        Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                null, protocol, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);
    }

    /* Tests_SRS_DEVICE_IO_21_003: [The constructor shall initialize the IoT Hub transport that uses the `protocol` specified.] */
    @Test
    public void constructorMqttSuccess(
            @Mocked final MqttTransport mockMqttTransport,
            @Mocked final DeviceClientConfig mockConfig) throws URISyntaxException
    {
        // arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

        // assert
        new NonStrictExpectations()
        {
            {
                new MqttTransport(mockConfig);
                result = mockMqttTransport;
                times = 1;
            }
        };

        // act
        Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                mockConfig, protocol, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_MQTT);

        new Verifications()
        {
            {
                mockConfig.setUseWebsocket(false);
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICE_IO_21_003: [The constructor shall initialize the IoT Hub transport that uses the `protocol` specified.] */
    @Test
    public void constructorHttpSuccess() throws URISyntaxException
    {
        // arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;

        // assert
        new NonStrictExpectations()
        {
            {
                new HttpsTransport(mockConfig);
                result = mockHttpsTransport;
                times = 1;
            }
        };

        // act
        Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                mockConfig, protocol, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_HTTPS);

        new Verifications()
        {
            {
                mockConfig.setUseWebsocket(false);
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICE_IO_21_003: [The constructor shall initialize the IoT Hub transport that uses the `protocol` specified.] */
    @Test
    public void constructorAmqpWSSuccess() throws URISyntaxException
    {
        // arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS_WS;

        // assert
        new NonStrictExpectations()
        {
            {
                new AmqpsTransport(mockConfig);
                result = mockAmqpsTransport;
                times = 1;
            }
        };

        // act
        Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                mockConfig, protocol, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);

        new Verifications()
        {
            {
                mockConfig.setUseWebsocket(true);
                times = 1;
            }
        };
    }


    @Test
    public void constructorMqttWSSuccess() throws URISyntaxException
    {
        // arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT_WS;

        // assert
        new NonStrictExpectations()
        {
            {
                new MqttTransport(mockConfig);
                result = mockMqttTransport;
                times = 1;
            }
        };

        // act
        Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                                    new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                                    mockConfig, protocol, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);

        new Verifications()
        {
            {
                mockConfig.setUseWebsocket(true);
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICE_IO_21_004: [If the `protocol` is null, the constructor shall throw an IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullProtocolThrows()
            throws URISyntaxException
    {
        // arrange
        final IotHubClientProtocol protocol = null;

        // act
        Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                mockConfig, protocol, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);
    }

    /* Tests_SRS_DEVICE_IO_21_005: [If the `protocol` is not valid, the constructor shall throw an IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorInvalidProtocolThrows(
            @Mocked final DeviceClientConfig mockConfig)
            throws URISyntaxException
    {
        // act
        Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                mockConfig, null, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);
    }

    /* Tests_SRS_DEVICE_IO_21_007: [If the client is already open, the open shall do nothing.] */
    @Test
    public void openDoesNothingIfCalledTwiceSuccess(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();
        openDeviceIO(deviceIO, mockAmqpsTransport, mockConfig, mockIotHubSSLContext, mockExecutors, mockScheduler);

        // act
        Deencapsulation.invoke(deviceIO, "open");

        // assert
        assertEquals("OPEN", Deencapsulation.getField(deviceIO, "state").toString());
    }

    /* Tests_SRS_DEVICE_IO_21_008: [The open shall create default IotHubSSL context if no certificate input was provided by user and save it by calling setIotHubSSLContext.] */
    @Test
    public void openAmqpCreateSSLContextSuccess(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();

        // assert
        new NonStrictExpectations()
        {
            {
                mockConfig.getPathToCertificate();
                result = null;
                times = 1;
                mockConfig.getUserCertificateString();
                result = null;
                times = 1;
                Deencapsulation.newInstance(IotHubSSLContext.class);
                result = mockIotHubSSLContext;
                times = 1;
                mockConfig.setIotHubSSLContext(mockIotHubSSLContext);
                times = 1;
                mockAmqpsTransport.open();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(deviceIO, "open");
    }

    /* Tests_SRS_DEVICE_IO_21_008: [The open shall create default IotHubSSL context if no certificate input was provided by user and save it by calling setIotHubSSLContext.] */
    @Test
    public void openHttpCreateSSLContextSuccess(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOHttps();

        // assert
        new NonStrictExpectations()
        {
            {
                mockConfig.getPathToCertificate();
                result = null;
                times = 1;
                mockConfig.getUserCertificateString();
                result = null;
                times = 1;
                Deencapsulation.newInstance(IotHubSSLContext.class);
                result = mockIotHubSSLContext;
                times = 1;
                mockConfig.setIotHubSSLContext(mockIotHubSSLContext);
                times = 1;
                mockHttpsTransport.open();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(deviceIO, "open");
    }

    /* Tests_SRS_DEVICE_IO_21_011: [If an exception is thrown when creating a SSL context then Open shall throw IOException to the user indicating the failure] */
    @Test
    public void openCreatesSSLContextThrowsIfDefaultCertThrows(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();

        // assert
        new NonStrictExpectations()
        {
            {
                mockConfig.getPathToCertificate();
                result = null;
                times = 1;
                mockConfig.getUserCertificateString();
                result = null;
                times = 1;
                Deencapsulation.newInstance(IotHubSSLContext.class);
                result = new CertificateException();
                times = 1;
            }
        };

        // act
        try
        {
            Deencapsulation.invoke(deviceIO, "open");
            assert true;
        }
        catch (Exception expected)
        {
            // Don't do anything. Expected exception.
        }

        // assert
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());
    }

    /* Tests_SRS_DEVICE_IO_21_009: [The open shall create IotHubSSL context with the certificate path if input was provided by user and save it by calling setIotHubSSLContext.] */
    @Test
    public void openCreatesSSLContextWithCertificatePathProvidedSuccess(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException
    {
        // arrange
        final String pathToCert = "test/path";
        final Object deviceIO = newDeviceIOAmqp();

        // assert
        new NonStrictExpectations()
        {
            {
                mockConfig.getPathToCertificate();
                result = pathToCert;
                times = 3;
                Deencapsulation.newInstance(IotHubSSLContext.class, pathToCert, true);
                result = mockIotHubSSLContext;
                times = 1;
                mockConfig.setIotHubSSLContext(mockIotHubSSLContext);
                times = 1;
                mockAmqpsTransport.open();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(deviceIO, "open");
    }

    /* Tests_SRS_DEVICE_IO_21_011: [If an exception is thrown when creating a SSL context then Open shall throw IOException to the user indicating the failure] */
    @Test
    public void openCreatesSSLContextWithCertificatePathProvidedThrowsIfInvalidCertPathThrows(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException
    {
        // arrange
        final String pathToCert = "test/path";
        final Object deviceIO = newDeviceIOAmqp();

        new NonStrictExpectations()
        {
            {
                mockConfig.getPathToCertificate();
                result = pathToCert;
                times = 3;
                Deencapsulation.newInstance(IotHubSSLContext.class, pathToCert, true);
                result = new FileNotFoundException();
                times = 1;
            }
        };

        // act
        try
        {
            Deencapsulation.invoke(deviceIO, "open");
            assert true;
        }
        catch (Exception expected)
        {
            // Don't do anything. Expected exception.
        }

        // assert
        new Verifications()
        {
            {
                mockAmqpsTransport.open();
                times = 0;
            }
        };
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());
    }

    /* Tests_SRS_DEVICE_IO_21_010: [The open shall create IotHubSSL context with the certificate String if input was provided by user and save it by calling setIotHubSSLContext.] */
    @Test
    public void openCreatesSSLContextWithCertificateProvidedSuccess(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException
    {
        // arrange
        final String userCert = "test/path";
        final Object deviceIO = newDeviceIOAmqp();

        // assert
        new NonStrictExpectations()
        {
            {
                mockConfig.getPathToCertificate();
                result = null;
                times = 2;
                mockConfig.getUserCertificateString();
                result = userCert;
                times = 2;
                Deencapsulation.newInstance(IotHubSSLContext.class, userCert, false);
                result = mockIotHubSSLContext;
                times = 1;
                mockConfig.setIotHubSSLContext(mockIotHubSSLContext);
                times = 1;
                mockAmqpsTransport.open();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(deviceIO, "open");
    }

    /* Tests_SRS_DEVICE_IO_21_011: [If an exception is thrown when creating a SSL context then Open shall throw IOException to the user indicating the failure] */
    @Test
    public void openCreatesSSLContextWithCertificateProvidedInvalidThrowsThrows(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException
    {
        // arrange
        final String userCert = "test/path";
        final Object deviceIO = newDeviceIOAmqp();
        mockConfig.setUserCertificateString(userCert);

        // assert
        new NonStrictExpectations()
        {
            {
                mockConfig.getPathToCertificate();
                result = null;
                times = 2;
                mockConfig.getUserCertificateString();
                result = userCert;
                times = 2;
                Deencapsulation.newInstance(IotHubSSLContext.class, userCert, false);
                result = new IllegalArgumentException();
                times = 1;
            }
        };

        // act
        try
        {
            Deencapsulation.invoke(deviceIO, "open");
            assert true;
        }
        catch (Exception expected)
        {
            // Don't do anything. Expected exception.
        }

        // assert
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());
    }

    /* Tests_SRS_DEVICE_IO_21_012: [The open shall open the transport to communicate with an IoT Hub.] */
    /* Tests_SRS_DEVICE_IO_21_013: [The open shall schedule send tasks to run every SEND_PERIOD_MILLIS milliseconds.] */
    /* Tests_SRS_DEVICE_IO_21_014: [The open shall schedule receive tasks to run every RECEIVE_PERIOD_MILLIS milliseconds.] */
    /* Tests_SRS_DEVICE_IO_21_016: [The open shall set the `state` as `OPEN`.] */
    @Test
    public void openSuccess(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();

        // assert
        new NonStrictExpectations()
        {
            {
                mockConfig.getPathToCertificate();
                result = null;
                times = 1;
                mockConfig.getUserCertificateString();
                result = null;
                times = 1;
                Deencapsulation.newInstance(IotHubSSLContext.class);
                result = mockIotHubSSLContext;
                times = 1;
                mockConfig.setIotHubSSLContext(mockIotHubSSLContext);
                times = 1;
                mockAmqpsTransport.open();
                times = 1;
                new IotHubSendTask(mockAmqpsTransport);
                result = mockIotHubSendTask;
                times = 1;
                new IotHubReceiveTask(mockAmqpsTransport);
                result = mockIotHubReceiveTask;
                times = 1;
                mockExecutors.newScheduledThreadPool(2);
                result = mockScheduler;
                times = 1;
                mockScheduler.scheduleAtFixedRate(mockIotHubSendTask,
                        0, SEND_PERIOD_MILLIS,
                        TimeUnit.MILLISECONDS);
                times = 1;
                mockScheduler.scheduleAtFixedRate(mockIotHubReceiveTask,
                        0, RECEIVE_PERIOD_MILLIS_AMQPS,
                        TimeUnit.MILLISECONDS);
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(deviceIO, "open");

        // assert
        assertEquals("OPEN", Deencapsulation.getField(deviceIO, "state").toString());
    }

    /* Tests_SRS_DEVICE_IO_21_015: [If an error occurs in opening the transport, the open shall throw an IOException.] */
    @Test
    public void openThrowsIOExceptionIfTransportOpenThrows(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();

        // assert
        new NonStrictExpectations()
        {
            {
                mockConfig.getPathToCertificate();
                result = null;
                mockConfig.getUserCertificateString();
                result = null;
                Deencapsulation.newInstance(IotHubSSLContext.class);
                result = mockIotHubSSLContext;
                mockAmqpsTransport.open();
                result = new IOException();
                times = 1;
            }
        };

        // act
        try
        {
            Deencapsulation.invoke(deviceIO, "open");
            assert true;
        }
        catch (Exception expected)
        {
            // Don't do anything. Expected exception.
        }

        // assert
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());
    }

    /* Tests_SRS_DEVICE_IO_21_017: [The close shall finish all ongoing tasks.] */
    /* Tests_SRS_DEVICE_IO_21_018: [The close shall cancel all recurring tasks.] */
    @Test
    public void closeWaitsForTaskShutdownToFinishSuccess(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();
        openDeviceIO(deviceIO, mockAmqpsTransport, mockConfig, mockIotHubSSLContext, mockExecutors, mockScheduler);

        // act
        Deencapsulation.invoke(deviceIO, "close");

        // assert
        new Verifications()
        {
            {
                mockScheduler.shutdown();
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICE_IO_21_019: [The close shall close the transport.] */
    @Test
    public void closeClosesTransportSuccess(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();
        openDeviceIO(deviceIO, mockAmqpsTransport, mockConfig, mockIotHubSSLContext, mockExecutors, mockScheduler);

        // act
        Deencapsulation.invoke(deviceIO, "close");

        // assert
        new Verifications()
        {
            {
                mockAmqpsTransport.close();
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICE_IO_21_020: [If the client is already closed, the close shall do nothing.] */
    @Test
    public void closeDoesNothingOnUnopenedClientSuccess()
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();

        // act
        Deencapsulation.invoke(deviceIO, "close");

        // assert
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());
    }

    /* Tests_SRS_DEVICE_IO_21_020: [If the client is already closed, the close shall do nothing.] */
    @Test
    public void closeDoesNothingOnClosedClientSuccess(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();
        openDeviceIO(deviceIO, mockAmqpsTransport, mockConfig, mockIotHubSSLContext, mockExecutors, mockScheduler);
        Deencapsulation.invoke(deviceIO, "close");
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());

        // act
        Deencapsulation.invoke(deviceIO, "close");

        // assert
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());
    }

    /* Tests_SRS_DEVICE_IO_21_021: [The close shall set the `state` as `CLOSE`.] */
    @Test
    public void closeChangeStateToClosedSuccess(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();
        openDeviceIO(deviceIO, mockAmqpsTransport, mockConfig, mockIotHubSSLContext, mockExecutors, mockScheduler);

        // act
        Deencapsulation.invoke(deviceIO, "close");

        // assert
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());
    }

    /* Tests_SRS_DEVICE_IO_21_022: [The sendEventAsync shall add the message, with its associated callback and callback context, to the transport.] */
    @Test
    public void sendEventAsyncAddsMessageToTransportSuccess(
            @Mocked final IotHubSSLContext mockIotHubSSLContext,
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback)
            throws URISyntaxException, IOException
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        final Object deviceIO = newDeviceIOAmqp();
        openDeviceIO(deviceIO, mockAmqpsTransport, mockConfig, mockIotHubSSLContext, mockExecutors, mockScheduler);

        // act
        Deencapsulation.invoke(deviceIO, "sendEventAsync", mockMsg, mockCallback, context);

        // assert
        new Verifications()
        {
            {
                mockAmqpsTransport.addMessage(mockMsg, mockCallback, context);
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICE_IO_21_023: [If the message given is null, the sendEventAsync shall throw an IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void sendEventAsyncRejectsNullMessageThrows(
            @Mocked final IotHubSSLContext mockIotHubSSLContext,
            @Mocked final IotHubEventCallback mockCallback)
            throws URISyntaxException, IOException
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        final Object deviceIO = newDeviceIOAmqp();
        openDeviceIO(deviceIO, mockAmqpsTransport, mockConfig, mockIotHubSSLContext, mockExecutors, mockScheduler);

        // act
        Deencapsulation.invoke(deviceIO, "sendEventAsync", 
                new Class[] {Message.class, IotHubEventCallback.class, Object.class}, 
                null, mockCallback, context);
    }

    /* Tests_SRS_DEVICE_IO_21_024: [If the client is closed, the sendEventAsync shall throw an IllegalStateException.] */
    @Test (expected = IllegalStateException.class)
    public void sendEventAsyncClientNotOpenedThrows(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback)
            throws URISyntaxException, IOException
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        final Object deviceIO = newDeviceIOAmqp();

        // act
        Deencapsulation.invoke(deviceIO, "sendEventAsync", mockMsg, mockCallback, context);
    }

    /* Tests_SRS_DEVICE_IO_21_024: [If the client is closed, the sendEventAsync shall throw an IllegalStateException.] */
    @Test (expected = IllegalStateException.class)
    public void sendEventAsyncClientAlreadyClosedThrows(
            @Mocked final IotHubSSLContext mockIotHubSSLContext,
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback)
            throws URISyntaxException, IOException
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        final Object deviceIO = newDeviceIOAmqp();
        openDeviceIO(deviceIO, mockAmqpsTransport, mockConfig, mockIotHubSSLContext, mockExecutors, mockScheduler);
        Deencapsulation.invoke(deviceIO, "close");
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());

        // act
        Deencapsulation.invoke(deviceIO, "sendEventAsync", mockMsg, mockCallback, context);
    }

    /* Tests_SRS_DEVICE_IO_21_040: [The sendEventAsync shall add the message, with its associated callback and callback context, to the transport.] */
    @Test
    public void sendEventAsyncAddsMessageWithResponseToTransportSuccess(
            @Mocked final IotHubSSLContext mockIotHubSSLContext,
            @Mocked final Message mockMsg,
            @Mocked final IotHubResponseCallback mockCallback)
            throws URISyntaxException, IOException
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        final Object deviceIO = newDeviceIOAmqp();
        openDeviceIO(deviceIO, mockAmqpsTransport, mockConfig, mockIotHubSSLContext, mockExecutors, mockScheduler);

        // act
        Deencapsulation.invoke(deviceIO, "sendEventAsync", mockMsg, mockCallback, context);

        // assert
        new Verifications()
        {
            {
                mockAmqpsTransport.addMessage(mockMsg, mockCallback, context);
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICE_IO_21_041: [If the message given is null, the sendEventAsync shall throw an IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void sendEventAsyncRejectsNullMessageWithResponseThrows(
            @Mocked final IotHubSSLContext mockIotHubSSLContext,
            @Mocked final IotHubResponseCallback mockCallback)
            throws URISyntaxException, IOException
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        final Object deviceIO = newDeviceIOAmqp();
        openDeviceIO(deviceIO, mockAmqpsTransport, mockConfig, mockIotHubSSLContext, mockExecutors, mockScheduler);

        // act
        Deencapsulation.invoke(deviceIO, "sendEventAsync",
                new Class[] {Message.class, IotHubResponseCallback.class, Object.class},
                null, mockCallback, context);
    }

    /* Tests_SRS_DEVICE_IO_21_042: [If the client is closed, the sendEventAsync shall throw an IllegalStateException.] */
    @Test (expected = IllegalStateException.class)
    public void sendEventAsyncWithResponseClientNotOpenedThrows(
            @Mocked final Message mockMsg,
            @Mocked final IotHubResponseCallback mockCallback)
            throws URISyntaxException, IOException
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        final Object deviceIO = newDeviceIOAmqp();

        // act
        Deencapsulation.invoke(deviceIO, "sendEventAsync", mockMsg, mockCallback, context);
    }

    /* Tests_SRS_DEVICE_IO_21_042: [If the client is closed, the sendEventAsync shall throw an IllegalStateException.] */
    @Test (expected = IllegalStateException.class)
    public void sendEventAsyncWithResponseClientAlreadyClosedThrows(
            @Mocked final IotHubSSLContext mockIotHubSSLContext,
            @Mocked final Message mockMsg,
            @Mocked final IotHubResponseCallback mockCallback)
            throws URISyntaxException, IOException
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        final Object deviceIO = newDeviceIOAmqp();
        openDeviceIO(deviceIO, mockAmqpsTransport, mockConfig, mockIotHubSSLContext, mockExecutors, mockScheduler);
        Deencapsulation.invoke(deviceIO, "close");
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());

        // act
        Deencapsulation.invoke(deviceIO, "sendEventAsync", mockMsg, mockCallback, context);
    }

    /* Tests_SRS_DEVICE_IO_21_025: [The getProtocol shall return the protocol for transport.] */
    @Test
    public void getTransportProtocolSuccess(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();
        openDeviceIO(deviceIO, mockAmqpsTransport, mockConfig, mockIotHubSSLContext, mockExecutors, mockScheduler);

        // act
        IotHubClientProtocol actualProtocol = Deencapsulation.invoke(deviceIO, "getProtocol") ;

        // assert
        assertEquals(IotHubClientProtocol.AMQPS, actualProtocol);
    }

    /* Tests_SRS_DEVICE_IO_21_026: [The getReceivePeriodInMilliseconds shall return the programed receive period in milliseconds.] */
    @Test
    public void getReceivePeriodInMillisecondsSuccess()
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();

        // act
        long receivePeriodInMilliseconds = Deencapsulation.invoke(deviceIO, "getReceivePeriodInMilliseconds") ;

        // assert
        assertEquals(RECEIVE_PERIOD_MILLIS_AMQPS, Deencapsulation.getField(deviceIO, "receivePeriodInMilliseconds"));
        assertEquals(RECEIVE_PERIOD_MILLIS_AMQPS, receivePeriodInMilliseconds);
    }

    /* Tests_SRS_DEVICE_IO_21_027: [The setReceivePeriodInMilliseconds shall store the new receive period in milliseconds.] */
    @Test
    public void setReceivePeriodInMillisecondsSuccess()
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();
        assertEquals(RECEIVE_PERIOD_MILLIS_AMQPS, Deencapsulation.getField(deviceIO, "receivePeriodInMilliseconds"));

        // act
        Deencapsulation.invoke(deviceIO, "setReceivePeriodInMilliseconds",  20L);

        // assert
        assertEquals(20L, Deencapsulation.getField(deviceIO, "receivePeriodInMilliseconds"));
    }

     /* Tests_SRS_DEVICE_IO_21_028: [If the task scheduler already exists, the setReceivePeriodInMilliseconds shall change the `scheduleAtFixedRate` for the receiveTask to the new value.] */
    @Test
    public void setReceivePeriodInMillisecondsTransportOpenedSuccess(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        final long interval = 1234L;
        final long lastInterval = 4321L;
        final Object deviceIO = newDeviceIOAmqp();
        Deencapsulation.invoke(deviceIO, "setReceivePeriodInMilliseconds",  lastInterval);
        new NonStrictExpectations()
        {
            {
                mockConfig.getPathToCertificate();
                result = null;
                mockConfig.getUserCertificateString();
                result = null;
                Deencapsulation.newInstance(IotHubSSLContext.class);
                result = mockIotHubSSLContext;
                new IotHubSendTask(mockAmqpsTransport);
                result = mockIotHubSendTask;
                new IotHubReceiveTask(mockAmqpsTransport);
                result = mockIotHubReceiveTask;
                mockExecutors.newScheduledThreadPool(2);
                result = mockScheduler;
            }
        };

        Deencapsulation.invoke(deviceIO, "open");
        assertEquals(lastInterval, Deencapsulation.getField(deviceIO, "receivePeriodInMilliseconds"));

        // act
        Deencapsulation.invoke(deviceIO, "setReceivePeriodInMilliseconds",  interval);

        // assert
        new Verifications()
        {
            {
                mockScheduler.scheduleAtFixedRate(mockIotHubReceiveTask,
                        0, lastInterval,
                        TimeUnit.MILLISECONDS);
                times = 1;
                mockScheduler.scheduleAtFixedRate(mockIotHubReceiveTask,
                        0, interval,
                        TimeUnit.MILLISECONDS);
                times = 1;
            }
        };
        assertEquals(interval, Deencapsulation.getField(deviceIO, "receivePeriodInMilliseconds"));
    }

    /* Tests_SRS_DEVICE_IO_21_029: [If the `receiveTask` is null, the setReceivePeriodInMilliseconds shall throw IOException.] */
    @Test (expected = IOException.class)
    public void setReceivePeriodInMillisecondsNullReceiveTaskThrows(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();
        openDeviceIO(deviceIO, mockAmqpsTransport, mockConfig, mockIotHubSSLContext, mockExecutors, mockScheduler);
        Deencapsulation.setField(deviceIO, "receiveTask", null);

        // act
        Deencapsulation.invoke(deviceIO, "setReceivePeriodInMilliseconds",  1234L);
    }

    /* Tests_SRS_DEVICE_IO_21_030: [If the the provided interval is zero or negative, the setReceivePeriodInMilliseconds shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void setReceivePeriodInMillisecondsZeroIntervalThrows()
            throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();

        // act
        Deencapsulation.invoke(deviceIO, "setReceivePeriodInMilliseconds",  0L);
    }

    /* Tests_SRS_DEVICE_IO_21_030: [If the the provided interval is zero or negative, the setReceivePeriodInMilliseconds shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void setReceivePeriodInMillisecondsNegativeIntervalThrows(
            @Mocked final AmqpsTransport mockAmqpsTransport,
            @Mocked final DeviceClientConfig mockConfig)
            throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();

        // act
        Deencapsulation.invoke(deviceIO, "setReceivePeriodInMilliseconds",  -10L);
    }

    /* Tests_SRS_DEVICE_IO_21_032: [The getSendPeriodInMilliseconds shall return the programed send period in milliseconds.] */
    @Test
    public void getSendPeriodInMillisecondsSuccess()
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();

        // act
        long sendPeriodInMilliseconds = Deencapsulation.invoke(deviceIO, "getSendPeriodInMilliseconds" );

        // assert
        assertEquals(SEND_PERIOD_MILLIS, Deencapsulation.getField(deviceIO, "sendPeriodInMilliseconds"));
        assertEquals(SEND_PERIOD_MILLIS, sendPeriodInMilliseconds);
    }

    /* Tests_SRS_DEVICE_IO_21_033: [The setSendPeriodInMilliseconds shall store the new send period in milliseconds.] */
    @Test
    public void setSendPeriodInMillisecondsSuccess()
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();
        assertEquals(SEND_PERIOD_MILLIS, Deencapsulation.getField(deviceIO, "sendPeriodInMilliseconds"));

        // act
        Deencapsulation.invoke(deviceIO, "setSendPeriodInMilliseconds",  20L);

        // assert
        assertEquals(20L, Deencapsulation.getField(deviceIO, "sendPeriodInMilliseconds"));
    }

    /* Tests_SRS_DEVICE_IO_21_034: [If the task scheduler already exists, the setSendPeriodInMilliseconds shall change the `scheduleAtFixedRate` for the sendTask to the new value.] */
    @Test
    public void setSendPeriodInMillisecondsTransportOpenedSuccess(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        final long interval = 1234L;
        final long lastInterval = 4321L;
        final Object deviceIO = newDeviceIOAmqp();
        Deencapsulation.invoke(deviceIO, "setSendPeriodInMilliseconds",  lastInterval);
        new NonStrictExpectations()
        {
            {
                mockConfig.getPathToCertificate();
                result = null;
                mockConfig.getUserCertificateString();
                result = null;
                Deencapsulation.newInstance(IotHubSSLContext.class);
                result = mockIotHubSSLContext;
                new IotHubSendTask(mockAmqpsTransport);
                result = mockIotHubSendTask;
                new IotHubReceiveTask(mockAmqpsTransport);
                result = mockIotHubReceiveTask;
                mockExecutors.newScheduledThreadPool(2);
                result = mockScheduler;
            }
        };

        Deencapsulation.invoke(deviceIO, "open");
        assertEquals(lastInterval, Deencapsulation.getField(deviceIO, "sendPeriodInMilliseconds"));

        // act
        Deencapsulation.invoke(deviceIO, "setSendPeriodInMilliseconds",  interval);

        // assert
        new Verifications()
        {
            {
                mockScheduler.scheduleAtFixedRate(mockIotHubSendTask,
                        0, lastInterval,
                        TimeUnit.MILLISECONDS);
                times = 1;
                mockScheduler.scheduleAtFixedRate(mockIotHubSendTask,
                        0, interval,
                        TimeUnit.MILLISECONDS);
                times = 1;
            }
        };
        assertEquals(interval, Deencapsulation.getField(deviceIO, "sendPeriodInMilliseconds"));
    }

    /* Tests_SRS_DEVICE_IO_21_035: [If the `sendTask` is null, the setSendPeriodInMilliseconds shall throw IOException.] */
    @Test (expected = IOException.class)
    public void setSendPeriodInMillisecondsNullSendTaskThrows(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();
        openDeviceIO(deviceIO, mockAmqpsTransport, mockConfig, mockIotHubSSLContext, mockExecutors, mockScheduler);
        Deencapsulation.setField(deviceIO, "sendTask", null);

        // act
        Deencapsulation.invoke(deviceIO, "setSendPeriodInMilliseconds",  1234L);
    }

    /* Tests_SRS_DEVICE_IO_21_036: [If the the provided interval is zero or negative, the setSendPeriodInMilliseconds shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void setSendPeriodInMillisecondsZeroIntervalThrows()
            throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();

        // act
        Deencapsulation.invoke(deviceIO, "setSendPeriodInMilliseconds",  0L);
    }

    /* Tests_SRS_DEVICE_IO_21_036: [If the the provided interval is zero or negative, the setSendPeriodInMilliseconds shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void setSendPeriodInMillisecondsNegativeIntervalThrows()
            throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();

        // act
        Deencapsulation.invoke(deviceIO, "setSendPeriodInMilliseconds",  -10L);
    }

    /* Tests_SRS_DEVICE_IO_21_031: [The isOpen shall return the connection state, true if connection is open, false if it is closed.] */
    @Test
    public void isOpenOpenedSuccess(
            @Mocked final IotHubSSLContext mockIotHubSSLContext)
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();
        openDeviceIO(deviceIO, mockAmqpsTransport, mockConfig, mockIotHubSSLContext, mockExecutors, mockScheduler);

        // act
        boolean isOpen = Deencapsulation.invoke(deviceIO, "isOpen" );

        // assert
        assertTrue(isOpen);
    }

    /* Tests_SRS_DEVICE_IO_21_031: [The isOpen shall return the connection state, true if connection is open, false if it is closed.] */
    @Test
    public void isOpenClosedSuccess()
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();

        // act
        boolean isOpen = Deencapsulation.invoke(deviceIO, "isOpen" );

        // assert
        assertFalse(isOpen);
    }

    /* Tests_SRS_DEVICE_IO_21_039: [The isEmpty shall return the transport queue state, true if the queue is empty, false if there is pending messages in the queue.] */
    @Test
    public void isEmptyTrueSuccess()
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();

        // assert
        new NonStrictExpectations()
        {
            {
                mockAmqpsTransport.isEmpty();
                result = true;
                times = 1;
            }
        };

        // act
        boolean isOpen = Deencapsulation.invoke(deviceIO, "isEmpty" );

        // assert
        assertTrue(isOpen);
    }

    /* Tests_SRS_DEVICE_IO_21_039: [The isEmpty shall return the transport queue state, true if the queue is empty, false if there is pending messages in the queue.] */
    @Test
    public void isEmptyFalseSuccess()
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIOAmqp();

        // assert
        new NonStrictExpectations()
        {
            {
                mockAmqpsTransport.isEmpty();
                result = false;
                times = 1;
            }
        };

        // act
        boolean isOpen = Deencapsulation.invoke(deviceIO, "isEmpty" );

        // assert
        assertFalse(isOpen);
    }

    /* Tests_SRS_DEVICE_IO_99_001: [The registerConnectionStateCallback shall register the callback with the transport.] */
    @Test
    public void registerConnectionStateCallbackSuccess(@Mocked final IotHubConnectionStateCallback mockedStateCB)
    {
        //arrange
        final Object deviceIO = newDeviceIOAmqp();
        
        //act
        Deencapsulation.invoke(deviceIO, "registerConnectionStateCallback", mockedStateCB, Object.class);

        //assert
        new Verifications()
        {
            {
                mockAmqpsTransport.registerConnectionStateCallback(mockedStateCB, null);
                times = 1;
            }
        };
    }
}
