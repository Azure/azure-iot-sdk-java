// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.DeviceClientException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubReceiveTask;
import com.microsoft.azure.sdk.iot.device.transport.IotHubSendTask;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransport;
import mockit.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Unit tests for DeviceIO.
 * Coverage :
 * 100% method,
 * 97% line
 */

public class DeviceIOTest
{

    @Mocked
    IotHubSendTask mockIotHubSendTask;

    @Mocked
    IotHubReceiveTask mockIotHubReceiveTask;

    @Mocked
    IotHubTransport mockedTransport;

    @Mocked 
    DeviceClientConfig mockConfig;

    @Mocked
    Executors mockExecutors;

    @Mocked
    ScheduledExecutorService mockScheduler;

    private final static Collection<DeviceClientConfig> configs = new ArrayList<>();

    @Mocked
    IotHubEventCallback mockedIotHubEventCallback;

    private final static long SEND_PERIOD_MILLIS = 10L;
    private final static long RECEIVE_PERIOD_MILLIS_AMQPS = 10L;
    private final static long RECEIVE_PERIOD_MILLIS_MQTT = 10L;
    private final static long RECEIVE_PERIOD_MILLIS_HTTPS = 25*60*1000; /*25 minutes*/

    private DeviceIO newDeviceIO()
    {
        new NonStrictExpectations()
        {
            {
                new IotHubTransport(mockConfig);
                result = mockedTransport;
            }
        };

        final DeviceIO deviceIO = Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                new Class[] {DeviceClientConfig.class, long.class, long.class},
                mockConfig, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);

        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());

        return deviceIO;
    }

    private void openDeviceIO(
            final Object deviceIO,
            final IotHubTransport transport,
            final Executors executors,
            final ScheduledExecutorService scheduledExecutorService) throws IOException
    {
        new NonStrictExpectations()
        {
            {
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
    public void constructorAmqpSuccess()
    {
        // arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockConfig.getProtocol();
                result = protocol;
            }
        };

        // act
        Object deviceIO = Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                new Class[] {DeviceClientConfig.class, long.class, long.class},
                mockConfig, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);

        // assert
        assertEquals(mockConfig, Deencapsulation.getField(deviceIO, "config"));
        assertEquals(protocol, Deencapsulation.getField(deviceIO, "protocol"));
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());
        assertEquals(SEND_PERIOD_MILLIS, Deencapsulation.getField(deviceIO, "sendPeriodInMilliseconds"));
        assertEquals(RECEIVE_PERIOD_MILLIS_AMQPS, Deencapsulation.getField(deviceIO, "receivePeriodInMilliseconds"));

        new Verifications()
        {
            {
                mockConfig.setUseWebsocket(true);
                times = 0;
            }
        };
    }
    
    /* Tests_SRS_DEVICE_IO_21_002: [If the `config` is null, the constructor shall throw an IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorConnectionStringThrows()
            throws URISyntaxException
    {
        // act
        Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                new Class[] {DeviceClientConfig.class, long.class, long.class},
                null, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);
    }

    /* Tests_SRS_DEVICE_IO_21_003: [The constructor shall initialize the IoT Hub transport that uses the `protocol` specified.] */
    @Test
    public void constructorMqttSuccess(
            @Mocked final DeviceClientConfig mockConfig)
    {
        // arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

        // assert
        new NonStrictExpectations()
        {
            {
                mockConfig.getProtocol();
                result = protocol;
            }
        };

        // act
        Object deviceIO = Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                new Class[] {DeviceClientConfig.class, long.class, long.class},
                mockConfig, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_MQTT);


        // assert
        assertEquals(mockConfig, Deencapsulation.getField(deviceIO, "config"));
        assertEquals(protocol, Deencapsulation.getField(deviceIO, "protocol"));
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());
        assertEquals(SEND_PERIOD_MILLIS, Deencapsulation.getField(deviceIO, "sendPeriodInMilliseconds"));
        assertEquals(RECEIVE_PERIOD_MILLIS_MQTT, Deencapsulation.getField(deviceIO, "receivePeriodInMilliseconds"));

        new Verifications()
        {
            {
                mockConfig.setUseWebsocket(true);
                times = 0;
            }
        };
    }

    /* Tests_SRS_DEVICE_IO_21_003: [The constructor shall initialize the IoT Hub transport that uses the `protocol` specified.] */
    @Test
    public void constructorHttpSuccess()
    {
        // arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;

        new NonStrictExpectations()
        {
            {
                mockConfig.getProtocol();
                result = protocol;
            }
        };

        // act
        Object deviceIO = Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                new Class[] {DeviceClientConfig.class, long.class, long.class},
                mockConfig, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_HTTPS);

        // assert
        assertEquals(mockConfig, Deencapsulation.getField(deviceIO, "config"));
        assertEquals(protocol, Deencapsulation.getField(deviceIO, "protocol"));
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());
        assertEquals(SEND_PERIOD_MILLIS, Deencapsulation.getField(deviceIO, "sendPeriodInMilliseconds"));
        assertEquals(RECEIVE_PERIOD_MILLIS_HTTPS, Deencapsulation.getField(deviceIO, "receivePeriodInMilliseconds"));

        new Verifications()
        {
            {
                mockConfig.setUseWebsocket(true);
                times = 0;
            }
        };
    }

    /* Tests_SRS_DEVICE_IO_21_003: [The constructor shall initialize the IoT Hub transport that uses the `protocol` specified.] */
    @Test
    public void constructorAmqpWSSuccess()
    {
        // arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS_WS;

        new NonStrictExpectations()
        {
            {
                mockConfig.getProtocol();
                result = protocol;
            }
        };

        // act
        Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                new Class[] {DeviceClientConfig.class, long.class, long.class},
                mockConfig, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);

        // assert
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

        new NonStrictExpectations()
        {
            {
                mockConfig.getProtocol();
                result = protocol;
            }
        };

        // act
        Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                                    new Class[] {DeviceClientConfig.class, long.class, long.class},
                                    mockConfig, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_MQTT);

        // assert
        new Verifications()
        {
            {
                mockConfig.setUseWebsocket(true);
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICE_IO_21_007: [If the client is already open, the open shall do nothing.] */
    @Test
    public void openDoesNothingIfCalledTwiceSuccess()
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIO();
        openDeviceIO(deviceIO, mockedTransport, mockExecutors, mockScheduler);

        // act
        Deencapsulation.invoke(deviceIO, "open");

        // assert
        assertEquals("OPEN", Deencapsulation.getField(deviceIO, "state").toString());
    }

    /* Tests_SRS_DEVICE_IO_21_012: [The open shall open the transport to communicate with an IoT Hub.] */
    /* Tests_SRS_DEVICE_IO_21_013: [The open shall schedule send tasks to run every SEND_PERIOD_MILLIS milliseconds.] */
    /* Tests_SRS_DEVICE_IO_21_014: [The open shall schedule receive tasks to run every RECEIVE_PERIOD_MILLIS milliseconds.] */
    /* Tests_SRS_DEVICE_IO_21_016: [The open shall set the `state` as `CONNECTED`.] */
    @Test
    public void openSuccess() throws DeviceClientException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIO();
        configs.add(mockConfig);
        Deencapsulation.setField(deviceIO, "deviceClientConfigs", configs);

        new NonStrictExpectations()
        {
            {
                new IotHubSendTask(mockedTransport);
                result = mockIotHubSendTask;
                new IotHubReceiveTask(mockedTransport);
                result = mockIotHubReceiveTask;
                mockExecutors.newScheduledThreadPool(2);
                result = mockScheduler;
            }
        };

        // act
        Deencapsulation.invoke(deviceIO, "open");

        // assert
        new Verifications()
        {
            {
                mockedTransport.open(configs);
                mockScheduler.scheduleAtFixedRate(mockIotHubSendTask,
                        0, SEND_PERIOD_MILLIS,
                        TimeUnit.MILLISECONDS);
                mockScheduler.scheduleAtFixedRate(mockIotHubReceiveTask,
                        0, RECEIVE_PERIOD_MILLIS_AMQPS,
                        TimeUnit.MILLISECONDS);
                assertEquals("OPEN", Deencapsulation.getField(deviceIO, "state").toString());
            }
        };
    }

    /* Tests_SRS_DEVICE_IO_21_015: [If an error occurs in opening the transport, the open shall throw an IOException.] */
    @Test (expected = IOException.class)
    public void openThrowsIOExceptionIfTransportOpenThrows() throws DeviceClientException
    {
        // arrange
        final Object deviceIO = newDeviceIO();
        configs.add(mockConfig);
        Deencapsulation.setField(deviceIO, "deviceClientConfigs", configs);

        new NonStrictExpectations()
        {
            {
                mockedTransport.open(configs);
                result = new DeviceClientException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(deviceIO, "open");

        // assert
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());
    }

    /* Tests_SRS_DEVICE_IO_21_017: [The closeNow shall finish all ongoing tasks.] */
    /* Tests_SRS_DEVICE_IO_21_018: [The closeNow shall cancel all recurring tasks.] */
    @Test
    public void closeWaitsForTaskShutdownToFinishSuccess()
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIO();
        openDeviceIO(deviceIO, mockedTransport, mockExecutors, mockScheduler);

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
    public void closeClosesTransportSuccess() throws IOException, DeviceClientException
    {
        // arrange
        final Object deviceIO = newDeviceIO();
        openDeviceIO(deviceIO, mockedTransport, mockExecutors, mockScheduler);

        // act
        Deencapsulation.invoke(deviceIO, "close");

        // assert
        new Verifications()
        {
            {
                mockedTransport.close(IotHubConnectionStatusChangeReason.CLIENT_CLOSE, null);
                times = 1;
            }
        };
    }

    @Test (expected = IOException.class)
    public void closeCloseTransportThrows() throws DeviceClientException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIO();
        try
        {
            openDeviceIO(deviceIO, mockedTransport, mockExecutors, mockScheduler);
        }
        catch (IOException e)
        {
            // catch and ignore IOException from open
        }

        new NonStrictExpectations()
        {
            {
                mockedTransport.close(IotHubConnectionStatusChangeReason.CLIENT_CLOSE, null);
                result = new DeviceClientException();
            }
        };

        // act
        Deencapsulation.invoke(deviceIO, "close");
    }

    /* Tests_SRS_DEVICE_IO_21_021: [The closeNow shall set the `state` as `CLOSE`.] */
    @Test
    public void closeDoesNothingOnUnopenedClientSuccess()
    {
        // arrange
        final Object deviceIO = newDeviceIO();

        // act
        Deencapsulation.invoke(deviceIO, "close");

        // assert
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());
    }

    /* Tests_SRS_DEVICE_IO_21_021: [The closeNow shall set the `state` as `CLOSE`.] */
    @Test
    public void closeDoesNothingOnClosedClientSuccess()
            throws IOException
    {
        // arrange
        final Object deviceIO = newDeviceIO();
        openDeviceIO(deviceIO, mockedTransport, mockExecutors, mockScheduler);
        Deencapsulation.invoke(deviceIO, "close");
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());

        // act
        Deencapsulation.invoke(deviceIO, "close");

        // assert
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());
    }

    /* Tests_SRS_DEVICE_IO_21_021: [The closeNow shall set the `state` as `CLOSE`.] */
    @Test
    public void closeChangeStateToClosedSuccess()
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIO();
        openDeviceIO(deviceIO, mockedTransport, mockExecutors, mockScheduler);

        // act
        Deencapsulation.invoke(deviceIO, "close");

        // assert
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());
    }

    // Tests_SRS_DEVICE_IO_12_009: [THe function shall call close().]
    @Test
    public void multiplexCloseCallClose() throws IOException
    {
        // arrange
        final Object deviceIO = newDeviceIO();
        openDeviceIO(deviceIO, mockedTransport, mockExecutors, mockScheduler);
        new StrictExpectations()
        {
            {
                Deencapsulation.invoke(deviceIO, "close");
            }
        };

        // act
        Deencapsulation.invoke(deviceIO, "multiplexClose");

        // assert
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());
    }


    /* Tests_SRS_DEVICE_IO_21_022: [The sendEventAsync shall add the message, with its associated callback and callback context, to the transport.] */
    // Tests_SRS_DEVICE_IO_12_001: [The function shall set the deviceId on the message if the deviceId parameter is not null.]
    @Test
    public void sendEventAsyncAddsMessageToTransportSuccess(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback)
            throws IOException
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        final Object deviceIO = newDeviceIO();
        openDeviceIO(deviceIO, mockedTransport, mockExecutors, mockScheduler);

        // act
        Deencapsulation.invoke(deviceIO, "sendEventAsync",
                new Class[] {Message.class, IotHubEventCallback.class, Object.class, String.class},
                mockMsg, mockCallback, context, "someDeviceId");

        // assert
        new Verifications()
        {
            {
                mockMsg.setConnectionDeviceId("someDeviceId");
                times = 1;
                mockedTransport.addMessage(mockMsg, mockCallback, context);
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICE_IO_21_023: [If the message given is null, the sendEventAsync shall throw an IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void sendEventAsyncRejectsNullMessageThrows(
            @Mocked final IotHubEventCallback mockCallback)
            throws IOException
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        final Object deviceIO = newDeviceIO();
        openDeviceIO(deviceIO, mockedTransport, mockExecutors, mockScheduler);

        // act
        Deencapsulation.invoke(deviceIO, "sendEventAsync",
                new Class[] {Message.class, IotHubEventCallback.class, Object.class, IotHubConnectionString.class},
                null, mockCallback, context, mockConfig.getDeviceId());
    }

    /* Tests_SRS_DEVICE_IO_21_024: [If the client is closed, the sendEventAsync shall throw an IllegalStateException.] */
    @Test (expected = IllegalStateException.class)
    public void sendEventAsyncClientNotOpenedThrows(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback)
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        final Object deviceIO = newDeviceIO();

        // act
        Deencapsulation.invoke(deviceIO, "sendEventAsync",
                new Class[] {Message.class, IotHubEventCallback.class, Object.class, String.class},
                mockMsg, mockCallback, context, mockConfig.getDeviceId());
    }

    /* Tests_SRS_DEVICE_IO_21_024: [If the client is closed, the sendEventAsync shall throw an IllegalStateException.] */
    @Test (expected = IllegalStateException.class)
    public void sendEventAsyncClientAlreadyClosedThrows(
            @Mocked final Message mockMsg,
            @Mocked final IotHubEventCallback mockCallback)
            throws IOException
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        final Object deviceIO = newDeviceIO();
        openDeviceIO(deviceIO, mockedTransport, mockExecutors, mockScheduler);
        Deencapsulation.invoke(deviceIO, "close");
        assertEquals("CLOSED", Deencapsulation.getField(deviceIO, "state").toString());

        // act
        Deencapsulation.invoke(deviceIO, "sendEventAsync", new Class[] {Message.class, IotHubEventCallback.class, Object.class, String.class}, mockMsg, mockCallback, context, mockConfig.getDeviceId());
    }

    /* Tests_SRS_DEVICE_IO_21_025: [The getProtocol shall return the protocol for transport.] */
    @Test
    public void getTransportProtocolSuccess()
            throws URISyntaxException, IOException
    {
        //arrange
        final IotHubClientProtocol expected = IotHubClientProtocol.AMQPS;
        new NonStrictExpectations()
        {
            {
                mockConfig.getProtocol();
                result = expected;
            }
        };

        final DeviceIO deviceIO = Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                new Class[] {DeviceClientConfig.class, long.class, long.class},
                mockConfig, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);

        // act
        IotHubClientProtocol actualProtocol = Deencapsulation.invoke(deviceIO, "getProtocol") ;

        // assert
        assertEquals(expected, actualProtocol);
    }

    /* Tests_SRS_DEVICE_IO_21_026: [The getReceivePeriodInMilliseconds shall return the programed receive period in milliseconds.] */
    @Test
    public void getReceivePeriodInMillisecondsSuccess()
            throws URISyntaxException, IOException
    {
        // arrange
        Deencapsulation.setField(mockConfig, "protocol", IotHubClientProtocol.AMQPS );
        final Object deviceIO = newDeviceIO();

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
        final Object deviceIO = newDeviceIO();
        assertEquals(RECEIVE_PERIOD_MILLIS_AMQPS, Deencapsulation.getField(deviceIO, "receivePeriodInMilliseconds"));

        // act
        Deencapsulation.invoke(deviceIO, "setReceivePeriodInMilliseconds",  20L);

        // assert
        assertEquals(20L, Deencapsulation.getField(deviceIO, "receivePeriodInMilliseconds"));
    }

     /* Tests_SRS_DEVICE_IO_21_028: [If the task scheduler already exists, the setReceivePeriodInMilliseconds shall change the `scheduleAtFixedRate` for the receiveTask to the new value.] */
    @Test
    public void setReceivePeriodInMillisecondsTransportOpenedSuccess()
            throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        final long interval = 1234L;
        final long lastInterval = 4321L;
        final Object deviceIO = newDeviceIO();
        Deencapsulation.invoke(deviceIO, "setReceivePeriodInMilliseconds",  lastInterval);
        new NonStrictExpectations()
        {
            {
                new IotHubSendTask(mockedTransport);
                result = mockIotHubSendTask;
                new IotHubReceiveTask(mockedTransport);
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
    public void setReceivePeriodInMillisecondsNullReceiveTaskThrows()
            throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        final Object deviceIO = newDeviceIO();
        openDeviceIO(deviceIO, mockedTransport, mockExecutors, mockScheduler);
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
        final Object deviceIO = newDeviceIO();

        // act
        Deencapsulation.invoke(deviceIO, "setReceivePeriodInMilliseconds",  0L);
    }

    /* Tests_SRS_DEVICE_IO_21_030: [If the the provided interval is zero or negative, the setReceivePeriodInMilliseconds shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void setReceivePeriodInMillisecondsNegativeIntervalThrows(
            @Mocked final IotHubTransport mockedTransport,
            @Mocked final DeviceClientConfig mockConfig)
            throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        final Object deviceIO = newDeviceIO();

        // act
        Deencapsulation.invoke(deviceIO, "setReceivePeriodInMilliseconds",  -10L);
    }

    /* Tests_SRS_DEVICE_IO_21_032: [The getSendPeriodInMilliseconds shall return the programed send period in milliseconds.] */
    @Test
    public void getSendPeriodInMillisecondsSuccess()
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIO();

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
        final Object deviceIO = newDeviceIO();
        assertEquals(SEND_PERIOD_MILLIS, Deencapsulation.getField(deviceIO, "sendPeriodInMilliseconds"));

        // act
        Deencapsulation.invoke(deviceIO, "setSendPeriodInMilliseconds",  20L);

        // assert
        assertEquals(20L, Deencapsulation.getField(deviceIO, "sendPeriodInMilliseconds"));
    }

    /* Tests_SRS_DEVICE_IO_21_034: [If the task scheduler already exists, the setSendPeriodInMilliseconds shall change the `scheduleAtFixedRate` for the sendTask to the new value.] */
    @Test
    public void setSendPeriodInMillisecondsTransportOpenedSuccess()
            throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        final long interval = 1234L;
        final long lastInterval = 4321L;
        final Object deviceIO = newDeviceIO();
        Deencapsulation.invoke(deviceIO, "setSendPeriodInMilliseconds",  lastInterval);
        new NonStrictExpectations()
        {
            {
                new IotHubSendTask(mockedTransport);
                result = mockIotHubSendTask;
                new IotHubReceiveTask(mockedTransport);
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
    public void setSendPeriodInMillisecondsNullSendTaskThrows()
            throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        final Object deviceIO = newDeviceIO();
        openDeviceIO(deviceIO, mockedTransport, mockExecutors, mockScheduler);
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
        final Object deviceIO = newDeviceIO();

        // act
        Deencapsulation.invoke(deviceIO, "setSendPeriodInMilliseconds",  0L);
    }

    /* Tests_SRS_DEVICE_IO_21_036: [If the the provided interval is zero or negative, the setSendPeriodInMilliseconds shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void setSendPeriodInMillisecondsNegativeIntervalThrows()
            throws URISyntaxException, IOException, InterruptedException
    {
        // arrange
        final Object deviceIO = newDeviceIO();

        // act
        Deencapsulation.invoke(deviceIO, "setSendPeriodInMilliseconds",  -10L);
    }

    /* Tests_SRS_DEVICE_IO_21_031: [The isOpen shall return the connection state, true if connection is open, false if it is closed.] */
    @Test
    public void isOpenOpenedSuccess()
            throws URISyntaxException, IOException
    {
        // arrange
        final Object deviceIO = newDeviceIO();
        openDeviceIO(deviceIO, mockedTransport, mockExecutors, mockScheduler);

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
        final Object deviceIO = newDeviceIO();

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
        final Object deviceIO = newDeviceIO();

        // assert
        new NonStrictExpectations()
        {
            {
                mockedTransport.isEmpty();
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
        final Object deviceIO = newDeviceIO();

        // assert
        new NonStrictExpectations()
        {
            {
                mockedTransport.isEmpty();
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
        final Object deviceIO = newDeviceIO();

        //act
        Deencapsulation.invoke(deviceIO, "registerConnectionStateCallback", mockedStateCB, Object.class);

        //assert
        new Verifications()
        {
            {
                mockedTransport.registerConnectionStateCallback(mockedStateCB, null);
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICE_IO_34_020: [This function shall register the callback with the transport.]
    @Test
    public void registerConnectionStatusChangeCallbackSuccess(@Mocked final IotHubConnectionStatusChangeCallback mockedStateCB)
    {
        //arrange
        final Object deviceIO = newDeviceIO();

        //act
        Deencapsulation.invoke(deviceIO, "registerConnectionStatusChangeCallback", mockedStateCB, Object.class);

        //assert
        new Verifications()
        {
            {
                mockedTransport.registerConnectionStatusChangeCallback(mockedStateCB, null);
                times = 1;
            }
        };
    }
}
