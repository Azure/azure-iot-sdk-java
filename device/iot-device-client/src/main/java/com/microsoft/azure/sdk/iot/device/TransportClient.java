package com.microsoft.azure.sdk.iot.device;

import java.io.IOException;
import java.util.ArrayList;

/**
 * <p>
 * The public-facing API. Allows user to create a transport client
 * abstracton object to use it for multiple devices to connect 
 * to an IoT Hub using the same connection (multiplexing). 
 * Handle to register devices to transport client and open / closeNow
 * the connection. 
 * </p>
 * The multiplexed connection is supported with AMQPS / AMQPS_WS protocols.
 */
public class TransportClient
{
    public enum TransportClientState
    {
        CLOSED,
        OPENED
    }

    public static long SEND_PERIOD_MILLIS = 10L;
    public static long RECEIVE_PERIOD_MILLIS_AMQPS = 10L;

    private IotHubClientProtocol iotHubClientProtocol;
    private DeviceIO deviceIO;
    private TransportClientState transportClientState;

    private ArrayList<DeviceClient> deviceClientList;

    private CustomLogger logger;

    /**
     * Constructor that takes a protocol as an argument.
     *
     * @param protocol the communication protocol used (i.e. AMQPS or AMQPS_WS).
     *
     * @throws IllegalArgumentException if other protocol given.
     */
    public TransportClient(IotHubClientProtocol protocol)
    {
        // Codes_SRS_TRANSPORTCLIENT_12_001: [If the `protocol` is not valid, the constructor shall throw an IllegalArgumentException.]
        switch (protocol)
        {
            case AMQPS:
            case AMQPS_WS:
                break;
            case MQTT:
            case MQTT_WS:
            case HTTPS:
                throw new IllegalArgumentException("Multiplexing is only supported for AMQPS and AMQPS_WS");
            default:
                // should never happen.
                throw new IllegalStateException(
                        "Invalid client protocol specified.");
        }
        // Codes_SRS_TRANSPORTCLIENT_12_002: [The constructor shall store the provided protocol.]
        this.iotHubClientProtocol = protocol;

        // Codes_SRS_TRANSPORTCLIENT_12_003: [The constructor shall set the the deviceIO to null.]
        this.deviceIO = null;

        // Codes_SRS_TRANSPORTCLIENT_12_004: [The constructor shall initialize the device list member.]
        this.deviceClientList = new ArrayList<>();

        this.transportClientState = TransportClientState.CLOSED;

        this.logger = new CustomLogger(this.getClass());

        logger.LogInfo("TransportClient object is created successfully, method name is %s ", logger.getMethodName());
    }

    /**
     * Creates a deviceIO and sets it to all the device client.
     * Verifies all device client's SAS tokens and renew them if it is necessary.
     * Opens the transport client connection.
     *
     * @throws IllegalStateException if the connection is already open.
     * @throws IOException if the connection to an IoT Hub cannot be opened.
     */
    public void open() throws IllegalStateException, IOException
    {
        // Codes_SRS_TRANSPORTCLIENT_12_008: [The function shall throw  IllegalStateException if the connection is already open.]
        if ((this.deviceIO != null) && (this.deviceIO.isOpen()))
        {
            throw new IllegalStateException("The transport client connection is already open.");
        }

        // Codes_SRS_TRANSPORTCLIENT_12_009: [The function shall do nothing if the the registration list is empty.]
        for (int i = 0; i < this.deviceClientList.size(); i++)
        {
            // Codes_SRS_TRANSPORTCLIENT_12_010: [The function shall renew each device client token if it is expired.]
            if (deviceClientList.get(i).getConfig().getSasTokenAuthentication() != null &&
                deviceClientList.get(i).getConfig().getSasTokenAuthentication().isRenewalNecessary())
            {
                deviceClientList.get(i).getConfig().getSasTokenAuthentication().getRenewedSasToken();
            }
        }

        // Codes_SRS_TRANSPORTCLIENT_12_009: [The function shall do nothing if the the registration list is empty.]
        if (this.deviceClientList.size() > 0)
        {
            // Codes_SRS_TRANSPORTCLIENT_12_011: [The function shall create a new DeviceIO using the first registered device client's configuration.]
            this.deviceIO = new DeviceIO(deviceClientList.get(0).getConfig(), iotHubClientProtocol, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);

            // Codes_SRS_TRANSPORTCLIENT_12_012: [The function shall set the created DeviceIO to all registered device client.]
            for (int i = 0; i < this.deviceClientList.size(); i++)
            {
                deviceClientList.get(i).setDeviceIO(this.deviceIO);
            }

            // Codes_SRS_TRANSPORTCLIENT_12_013: [The function shall open the transport in multiplexing mode.]
            this.deviceIO.multiplexOpen(deviceClientList);
        }

        this.transportClientState = TransportClientState.OPENED;

        logger.LogInfo("TransportClient is opened successfully, method name is %s ", logger.getMethodName());
    }

    /**
     * Completes all current outstanding requests and closes the IoT Hub client.
     * Must be called to terminate the background thread that is sending data to
     * IoT Hub. After {@code closeNow()} is called, the IoT Hub client is no longer
     * usable. If the client is already closed, the function shall do nothing.
     *
     * @throws IOException if the connection to an IoT Hub cannot be closed.
     */
    public void closeNow() throws IOException
    {
        // Codes_SRS_TRANSPORTCLIENT_12_015: [If the registered device list is not empty the function shall call closeFileUpload on all devices.]
        for (int i = 0; i < this.deviceClientList.size(); i++)
        {
            deviceClientList.get(i).closeFileUpload();
        }

        // Codes_SRS_TRANSPORTCLIENT_12_014: [If the deviceIO not null the function shall call multiplexClose on the deviceIO and set the deviceIO to null.]
        if (this.deviceIO != null)
        {
            this.deviceIO.multiplexClose();
            this.deviceIO = null;
        }

        logger.LogInfo("Connection closed with success, method name is %s ", logger.getMethodName());
    }

    /***
     * Sets the given send interval on the underlying device IO
     *
     * @param newIntervalInMilliseconds the new interval in milliseconds
     * @throws IOException if the given number is less or equal to zero.
     */
    public void setSendInterval(long newIntervalInMilliseconds) throws IOException
    {
        if (newIntervalInMilliseconds <= 0)
        {
            // Codes_SRS_TRANSPORTCLIENT_12_017: [The function shall throw IllegalArgumentException if the newIntervalInMilliseconds parameter is less or equql to zero.]
            throw new IllegalArgumentException("send interval can not be zero or negative");
        }

        if ((this.transportClientState != TransportClientState.OPENED) || (deviceIO == null))
        {
            // Codes_SRS_TRANSPORTCLIENT_12_023: [The function shall throw  IllegalStateException if the connection is already open.]
            throw new IllegalStateException("TransportClient.setSendInterval only works when the transport client is opened");
        }

        // Codes_SRS_TRANSPORTCLIENT_12_018: [The function shall set the new interval on the underlying device IO it the transport client is not open.]
        this.deviceIO.setSendPeriodInMilliseconds(newIntervalInMilliseconds);

        logger.LogInfo("Send interval updated successfully in the transport client, method name is %s ", logger.getMethodName());
    }

    /**
     * Registers the given device into the transport client.
     *
     * @throws IllegalArgumentException if the deviceClient parameter is null.
     * @throws IllegalStateException if the connection is open.
     */
    void registerDeviceClient(DeviceClient deviceClient) throws IllegalArgumentException, IllegalStateException
    {
        // Codes_SRS_TRANSPORTCLIENT_12_005: [The function shall throw  IllegalArgumentException if the deviceClient parameter is null.]
        if (deviceClient == null)
        {
            throw new IllegalArgumentException("deviceClient parameter cannot be null.");
        }

        // Codes_SRS_TRANSPORTCLIENT_12_006: [The function shall throw  IllegalStateException if the connection is already open.]
        if ((this.deviceIO != null) && (this.deviceIO.isOpen()))
        {
            throw new IllegalStateException("deviceClient cannot be registered if the connection is open.");
        }

        // Codes_SRS_TRANSPORTCLIENT_12_007: [The function shall add the given device client to the deviceClientList.]
        this.deviceClientList.add(deviceClient);

        logger.LogInfo("DeviceClient is added successfully to the transport client, method name is %s ", logger.getMethodName());
    }

    /**
     * Getter for the transportClientState
     * @return the current transportClientState
     */
    TransportClientState getTransportClientState()
    {
        // Codes_SRS_TRANSPORTCLIENT_12_019: [The getter shall return with the value of the transportClientState.]
        return this.transportClientState;
    }
}
