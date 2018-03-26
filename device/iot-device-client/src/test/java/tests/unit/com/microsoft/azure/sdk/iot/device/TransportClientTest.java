package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Unit tests for TransportClient.
 * Methods: 100%
 * Lines: 96%
 */
public class TransportClientTest
{
    @Mocked
    DeviceClient mockDeviceClient;

    @Mocked
    DeviceIO mockDeviceIO;

    @Mocked
    DeviceClientConfig mockDeviceClientConfig;

    @Mocked
    IotHubSasTokenAuthenticationProvider mockIotHubSasTokenAuthenticationProvider;

    // Tests_SRS_TRANSPORTCLIENT_12_001: [If the `protocol` is not valid, the constructor shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsProtocolMQTT()
    {
        // arrange
        IotHubClientProtocol iotHubClientProtocol = IotHubClientProtocol.MQTT;

        // act
        new TransportClient(iotHubClientProtocol);
    }

    // Tests_SRS_TRANSPORTCLIENT_12_001: [If the `protocol` is not valid, the constructor shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsProtocolMQTT_WS()
    {
        // arrange
        IotHubClientProtocol iotHubClientProtocol = IotHubClientProtocol.MQTT_WS;

        // act
        new TransportClient(iotHubClientProtocol);
    }

    // Tests_SRS_TRANSPORTCLIENT_12_001: [If the `protocol` is not valid, the constructor shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsProtocolHTTPS()
    {
        // arrange
        IotHubClientProtocol iotHubClientProtocol = IotHubClientProtocol.HTTPS;

        // act
        new TransportClient(iotHubClientProtocol);
    }

    // Tests_SRS_TRANSPORTCLIENT_12_002: [The constructor shall store the provided protocol.]
    // Tests_SRS_TRANSPORTCLIENT_12_003: [The constructor shall set the the deviceIO to null.]
    // Tests_SRS_TRANSPORTCLIENT_12_004: [The constructor shall initialize the device list member.]
    @Test
    public void constructorSuccessAMQPS()
    {
        // arrange
        IotHubClientProtocol iotHubClientProtocol = IotHubClientProtocol.AMQPS;

        // act
        TransportClient transportClient = new TransportClient(iotHubClientProtocol);

        // assert
        IotHubClientProtocol actualProtocol = Deencapsulation.getField(transportClient, "iotHubClientProtocol");
        DeviceIO deviceIO = Deencapsulation.getField(transportClient, "deviceIO");
        ArrayList deviceClientList = Deencapsulation.getField(transportClient, "deviceClientList");

        assertEquals(iotHubClientProtocol, actualProtocol);
        assertNull(deviceIO);
        assertNotNull(deviceClientList);
    }

    // Tests_SRS_TRANSPORTCLIENT_12_008: [The function shall throw  IllegalStateException if the connection is already open.]
    @Test (expected = IllegalStateException.class)
    public void openThrowsIfConnectionIsAlreadyOpen() throws IOException
    {
        // arrange
        IotHubClientProtocol iotHubClientProtocol = IotHubClientProtocol.AMQPS;
        TransportClient transportClient = new TransportClient(iotHubClientProtocol);
        Deencapsulation.setField(transportClient, "deviceIO", mockDeviceIO);

        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };

        // act
        transportClient.open();
    }

    // Tests_SRS_TRANSPORTCLIENT_12_009: [The function shall do nothing if the the registration list is empty.]
    @Test
    public void openDoesNothing() throws IOException
    {
        // arrange
        IotHubClientProtocol iotHubClientProtocol = IotHubClientProtocol.AMQPS;
        TransportClient transportClient = new TransportClient(iotHubClientProtocol);
        Deencapsulation.setField(transportClient, "deviceIO", mockDeviceIO);

        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
            }
        };

        // act
        transportClient.open();

        // assert
        final ArrayList<DeviceClient> actualDeviceClientList = Deencapsulation.getField(transportClient, "deviceClientList");
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockDeviceIO, "open");
                times = 0;
            }
        };
    }

    // Tests_SRS_TRANSPORTCLIENT_12_010: [The function shall renew each device client token if it is expired.]
    // Tests_SRS_TRANSPORTCLIENT_12_011: [The function shall create a new DeviceIO using the first registered device client's configuration.]
    // Tests_SRS_TRANSPORTCLIENT_12_012: [The function shall set the created DeviceIO to all registered device client.]
    // Tests_SRS_TRANSPORTCLIENT_12_013: [The function shall open the transport in multiplexing mode.]
    @Test
    public void openSuccess() throws IOException
    {
        // arrange
        IotHubClientProtocol iotHubClientProtocol = IotHubClientProtocol.AMQPS;
        final TransportClient transportClient = new TransportClient(iotHubClientProtocol);
        Deencapsulation.invoke(transportClient, "registerDeviceClient", mockDeviceClient);

        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceClient.getConfig();
                result = mockDeviceClientConfig;
                mockDeviceClientConfig.getSasTokenAuthentication();
                result = mockIotHubSasTokenAuthenticationProvider;
                mockIotHubSasTokenAuthenticationProvider.isRenewalNecessary();
                result = true;
            }
        };

        // act
        transportClient.open();

        // assert
        final ArrayList<DeviceClient> actualDeviceClientList = Deencapsulation.getField(transportClient, "deviceClientList");
        final DeviceIO actualDeviceIO =  Deencapsulation.getField(transportClient, "deviceIO");

        assertNotNull(actualDeviceIO);
        new Verifications()
        {
            {
                mockIotHubSasTokenAuthenticationProvider.getRenewedSasToken();
                times = 1;
                Deencapsulation.invoke(mockDeviceClient, "setDeviceIO", actualDeviceIO);
                times = 1;
                Deencapsulation.invoke(mockDeviceIO, "open");
                times = 1;
            }
        };
    }

    // Tests_SRS_TRANSPORTCLIENT_12_015: [If the registered device list is not empty the function shall call closeFileUpload on all devices.]
    @Test
    public void closeNowNoDevice() throws IOException
    {
        // arrange
        IotHubClientProtocol iotHubClientProtocol = IotHubClientProtocol.AMQPS;
        final TransportClient transportClient = new TransportClient(iotHubClientProtocol);
        Deencapsulation.setField(transportClient, "deviceIO", mockDeviceIO);

        // act
        transportClient.closeNow();

        // assert
        ArrayList<DeviceClient> actualDeviceClientList = Deencapsulation.getField(transportClient, "deviceClientList");
        assertEquals(actualDeviceClientList.size(), 0);

        DeviceIO deviceIO = Deencapsulation.getField(transportClient, "deviceIO");
        assertNull(deviceIO);

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockDeviceClient, "closeFileUpload");
                times = 0;
            }
        };
    }

    // Tests_SRS_TRANSPORTCLIENT_12_014: [If the deviceIO not null the function shall call multiplexClose on the deviceIO and set the deviceIO to null.]
    // Tests_SRS_TRANSPORTCLIENT_12_015: [If the registered device list is not empty the function shall call closeFileUpload on all devices.]
    // Tests_SRS_TRANSPORTCLIENT_12_016: [The function shall clear the registered device list.]
    @Test
    public void closeNowSuccess() throws IOException
    {
        // arrange
        IotHubClientProtocol iotHubClientProtocol = IotHubClientProtocol.AMQPS;
        final TransportClient transportClient = new TransportClient(iotHubClientProtocol);
        Deencapsulation.setField(transportClient, "deviceIO", mockDeviceIO);
        Deencapsulation.invoke(transportClient, "registerDeviceClient", mockDeviceClient);

        // act
        transportClient.closeNow();

        // assert
        DeviceIO deviceIO = Deencapsulation.getField(transportClient, "deviceIO");
        assertNull(deviceIO);

        new Verifications()
        {
            {
                mockDeviceIO.multiplexClose();
                times = 1;
                Deencapsulation.invoke(mockDeviceClient, "closeFileUpload");
                times = 1;
            }
        };
    }

    // Tests_SRS_TRANSPORTCLIENT_12_017: [The function shall throw IllegalArgumentException if the newIntervalInMilliseconds parameter is less or equql to zero.]
    @Test (expected = IllegalArgumentException.class)
    public void setSendIntervalThrowsNegativeNumber() throws IOException
    {
        // arrange
        final long value = -1;
        IotHubClientProtocol iotHubClientProtocol = IotHubClientProtocol.AMQPS;
        TransportClient transportClient = new TransportClient(iotHubClientProtocol);

        // act
        transportClient.setSendInterval(value);
    }

    // Tests_SRS_TRANSPORTCLIENT_12_023: [The function shall throw  IllegalStateException if the connection is already open.]
    @Test (expected = IllegalStateException.class)
    public void setSendIntervalThrowsAlreadyOpenState() throws IOException
    {
        // arrange
        final long value = 42;
        IotHubClientProtocol iotHubClientProtocol = IotHubClientProtocol.AMQPS;
        TransportClient transportClient = new TransportClient(iotHubClientProtocol);
        Deencapsulation.setField(transportClient, "deviceIO", mockDeviceIO);
        Deencapsulation.setField(transportClient, "transportClientState", TransportClient.TransportClientState.CLOSED);

        // act
        transportClient.setSendInterval(value);
    }

    // Tests_SRS_TRANSPORTCLIENT_12_023: [The function shall throw  IllegalStateException if the connection is already open.]
    @Test (expected = IllegalStateException.class)
    public void setSendIntervalThrowsAlreadyOpenIO() throws IOException
    {
        // arrange
        final long value = 42;
        IotHubClientProtocol iotHubClientProtocol = IotHubClientProtocol.AMQPS;
        TransportClient transportClient = new TransportClient(iotHubClientProtocol);
        Deencapsulation.setField(transportClient, "transportClientState", TransportClient.TransportClientState.OPENED);

        // act
        transportClient.setSendInterval(value);
    }

    // Tests_SRS_TRANSPORTCLIENT_12_018: [The function shall set the new interval on the underlying device IO it the transport client is not open.]
    @Test
    public void setSendIntervalSuccess() throws IOException
    {
        // arrange
        final long value = 42;
        IotHubClientProtocol iotHubClientProtocol = IotHubClientProtocol.AMQPS;
        TransportClient transportClient = new TransportClient(iotHubClientProtocol);
        Deencapsulation.setField(transportClient, "deviceIO", mockDeviceIO);
        Deencapsulation.setField(transportClient, "transportClientState", TransportClient.TransportClientState.OPENED);

        // act
        transportClient.setSendInterval(value);
        new Verifications()
        {
            {
                mockDeviceIO.setSendPeriodInMilliseconds(value);
                times = 1;
            }
        };
    }

    // Tests_SRS_TRANSPORTCLIENT_12_005: [The function shall throw  IllegalArgumentException if the deviceClient parameter is null.]
    @Test (expected = IllegalArgumentException.class)
    public void registerDeviceClientThrowsDeviceClientNull()
    {
        // arrange
        IotHubClientProtocol iotHubClientProtocol = IotHubClientProtocol.AMQPS;
        TransportClient transportClient = new TransportClient(iotHubClientProtocol);

        // act
        Deencapsulation.invoke(transportClient, "registerDeviceClient", (DeviceClient)null);
    }

    // Tests_SRS_TRANSPORTCLIENT_12_006: [The function shall throw  IllegalStateException if the connection is already open.]
    @Test (expected = IllegalStateException.class)
    public void registerDeviceClientThrowsOpen()
    {
        // arrange
        IotHubClientProtocol iotHubClientProtocol = IotHubClientProtocol.AMQPS;
        TransportClient transportClient = new TransportClient(iotHubClientProtocol);
        Deencapsulation.setField(transportClient, "deviceIO", mockDeviceIO);

        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
            }
        };

        // act
        Deencapsulation.invoke(transportClient, "registerDeviceClient", mockDeviceClient);
    }

    // Tests_SRS_TRANSPORTCLIENT_12_007: [The function shall add the given device client to the deviceClientList.]
    @Test
    public void registerDeviceClientSuccess()
    {
        // arrange
        IotHubClientProtocol iotHubClientProtocol = IotHubClientProtocol.AMQPS;
        TransportClient transportClient = new TransportClient(iotHubClientProtocol);

        // act
        Deencapsulation.invoke(transportClient, "registerDeviceClient", mockDeviceClient);

        // assert
        ArrayList<DeviceClient> actualDeviceClientList = Deencapsulation.getField(transportClient, "deviceClientList");
        assertEquals(actualDeviceClientList.size(), 1);
    }


    // Tests_SRS_TRANSPORTCLIENT_12_020: [The function shall call the underlying deviceIO updateDeviceConfig with the given config.]


    // Tests_SRS_TRANSPORTCLIENT_12_019: [The getter shall return with the value of the transportClientState.]
    @Test
    public void getSenderLinkAddressReturnsSenderLinkAddress()
    {
        //arrange
        IotHubClientProtocol iotHubClientProtocol = IotHubClientProtocol.AMQPS;
        TransportClient transportClient = new TransportClient(iotHubClientProtocol);
        Deencapsulation.setField(transportClient, "transportClientState", TransportClient.TransportClientState.OPENED);

        //act
        TransportClient.TransportClientState actualState = Deencapsulation.invoke(transportClient, "getTransportClientState");

        //assert
        assertEquals(TransportClient.TransportClientState.OPENED, actualState);
    }

}
