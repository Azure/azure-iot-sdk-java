package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientException;
import com.microsoft.azure.sdk.iot.device.transport.RetryPolicy;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.azure.sdk.iot.device.MultiplexingClient.DEFAULT_REGISTRATION_TIMEOUT_MILLISECONDS;

/**
 * <p>
 * The public-facing API. Allows user to create a transport client
 * abstracton object to use it for multiple devices to connect 
 * to an IoT Hub using the same connection (multiplexing). 
 * Handle to register devices to transport client and open / closeNow
 * the connection. 
 * </p>
 * The multiplexed connection is supported with AMQPS / AMQPS_WS protocols.
 *
 * @deprecated This client has been replaced with {@link MultiplexingClient} since this client does not support adding
 * or removing devices once the connection has been established. {@link MultiplexingClient} allows for adding and removing
 * of devices from multiplexed connections before or after opening the connection.
 */
@Slf4j
@Deprecated
public class TransportClient
{
    public enum TransportClientState
    {
        CLOSED,
        OPENED
    }

    @SuppressWarnings("CanBeFinal") // Public member can be changed
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL") // Marking this as final would be a breaking change
    public static long SEND_PERIOD_MILLIS = 10L;
    @SuppressWarnings("CanBeFinal") // Public member can be changed
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL") // Marking this as final would be a breaking change
    public static long RECEIVE_PERIOD_MILLIS_AMQPS = 10L;

    private final IotHubClientProtocol iotHubClientProtocol;
    private DeviceIO deviceIO;
    private TransportClientState transportClientState;

    private final ArrayList<DeviceClient> deviceClientList;

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
        if (this.deviceClientList.size() > 0)
        {
            // Codes_SRS_TRANSPORTCLIENT_12_011: [The function shall create a new DeviceIO using the first registered device client's configuration.]
            this.deviceIO = new DeviceIO(deviceClientList.get(0).getConfig(), SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS, true);
            deviceClientList.get(0).setDeviceIO(this.deviceIO);

            // Codes_SRS_TRANSPORTCLIENT_12_012: [The function shall set the created DeviceIO to all registered device client.]
            List<DeviceClientConfig> configList = new ArrayList<>();
            for (int i = 1; i < this.deviceClientList.size(); i++)
            {
                deviceClientList.get(i).setDeviceIO(this.deviceIO);
                //propagate this client config to amqp connection
                configList.add(deviceClientList.get(i).getConfig());
            }

            try
            {
                this.deviceIO.registerMultiplexedDeviceClient(configList, DEFAULT_REGISTRATION_TIMEOUT_MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                throw new IOException("Interrupted while registering device clients to the multiplexed connection", e);
            }
            catch (MultiplexingClientException e)
            {
                throw new IOException("Failed to register one or more device clients to the multiplexed connection", e);
            }

            // Codes_SRS_TRANSPORTCLIENT_12_013: [The function shall open the transport in multiplexing mode.]
            //this.deviceIO.multiplexOpen(deviceClientList);
            // if client is added just open to get rid of multiplex open.
            this.deviceIO.open(false);
        }

        this.transportClientState = TransportClientState.OPENED;

        log.info("Transport client opened successfully");
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
        for (DeviceClient deviceClient : this.deviceClientList)
        {
            deviceClient.closeFileUpload();
        }

        // Codes_SRS_TRANSPORTCLIENT_12_014: [If the deviceIO not null the function shall call closeWithoutWrappingException on the deviceIO and set the deviceIO to null.]
        if (this.deviceIO != null)
        {
            this.deviceIO.close();
            this.deviceIO = null;
        }

        log.info("Transport client closed successfully");
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

        log.debug("Send interval updated successfully in the transport client");
    }

    /**
     * Sets the given retry policy on the underlying transport
     * Sets the given retry policy on the underlying transport
     * <a href="https://github.com/Azure/azure-iot-sdk-java/blob/master/device/iot-device-client/devdoc/requirement_docs/com/microsoft/azure/iothub/retryPolicy.md">
     *     See more details about the default retry policy and about using custom retry policies here</a>
     * @param retryPolicy the new interval in milliseconds
     * @throws UnsupportedOperationException if no device client has been registered yet.
     */
    public void setRetryPolicy(RetryPolicy retryPolicy)
    {
        if (deviceClientList.size() == 0)
        {
            // Codes_SRS_TRANSPORTCLIENT_28_001: [The function shall throw UnsupportedOperationException if there is no registered device client]
            throw new UnsupportedOperationException("TransportClient.setRetryPolicy only works when there is at least one registered device client.");
        }

        for (DeviceClient deviceClient : this.deviceClientList)
        {
            // Codes_SRS_TRANSPORTCLIENT_28_002: [The function shall set the retry policies to all registered device clients.]
            deviceClient.getConfig().setRetryPolicy(retryPolicy);
        }

        log.debug("Retry policy updated successfully in the transport client");
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

        log.debug("DeviceClient instance successfully added to the transport client");
    }

    /**
     * Getter for the iotHubClientProtocol
     * @return the current protocol for the iotHubClient
     */
    IotHubClientProtocol getIotHubClientProtocol()
    {
        return iotHubClientProtocol;
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
