package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.device.twin.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manages the lifetime of a DeviceClient instance such that it is always either connected or attempting to reconnect.
 * It also demonstrates the best practices for handling a client's twin. See this sample's readme for a more detailed
 * explanation on how this sample works and how it can be modified to fit your needs.
 */
@Slf4j
public class DeviceClientManager implements DesiredPropertiesCallback, MethodCallback, MessageSentCallback, MessageCallback, GetTwinCallback, ReportedPropertiesCallback
{
    // The client. Can be replaced with a module client for writing the equivalent code for a module.
    private final DeviceClient deviceClient;

    // The twin for this client. Stays up to date as reported properties are sent and desired properties are received.
    private Twin twin;

    // Outgoing work queues of the client
    private final Queue<Message> telemetryToResend = new ConcurrentLinkedQueue<>();
    private final TwinCollection reportedPropertiesToSend = new TwinCollection();

    // Connection state of the client. This sample is written in such a way that you do not actually need to check this
    // value before performing any operations, but it is provided here anyways.
    private IotHubConnectionStatus connectionStatus = IotHubConnectionStatus.DISCONNECTED;

    // This lock is used to wake up the Iot-Hub-Connection-Manager-Thread when a terminal disconnection event occurs
    // and the device client needs to be manually re-opened.
    private final Object reconnectionLock = new Object();

    // State flag that signals that the client is in the process of getting the current twin for this client and that
    // any outgoing reported properties should hold off on being sent until this twin has been retrieved.
    private boolean gettingTwinAfterReconnection = false;

    public DeviceClientManager(String deviceConnectionString, IotHubClientProtocol protocol)
    {
        // Lower keep alive interval values help the client recognize disconnection events sooner, but do require
        // sending keep-alive pings more frequently. Scenarios that prioritize minimizing network traffic should have
        // higher values. Scenarios that prioritize minimizing time spent disconnected should have lower values.
        ClientOptions clientOptions = ClientOptions.builder().keepAliveInterval(100).build();
        this.deviceClient = new DeviceClient(deviceConnectionString, protocol, clientOptions);
    }

    public void run()
    {
        try
        {
            this.deviceClient.setConnectionStatusChangeCallback(
                (connectionStatusChangeContext) ->
                {
                    IotHubConnectionStatus newStatus = connectionStatusChangeContext.getNewStatus();
                    IotHubConnectionStatusChangeReason newStatusReason = connectionStatusChangeContext.getNewStatusReason();
                    IotHubConnectionStatus previousStatus = connectionStatusChangeContext.getPreviousStatus();

                    this.connectionStatus = newStatus;

                    if (newStatusReason == IotHubConnectionStatusChangeReason.BAD_CREDENTIAL)
                    {
                        // Should only happen if using a custom SAS token provider and the user-generated SAS token
                        // was incorrectly formatted. Users who construct the device client with a connection string
                        // will never see this, and users who use x509 authentication will never see this.
                        log.error("Ending sample because the provided credentials were incorrect or malformed");
                        System.exit(-1);
                    }

                    if (newStatus == IotHubConnectionStatus.DISCONNECTED && newStatusReason == IotHubConnectionStatusChangeReason.EXPIRED_SAS_TOKEN)
                    {
                        // Should only happen if the user provides a shared access signature instead of a connection string.
                        // indicates that the device client is now unusable because there is no way to renew the shared
                        // access signature. Users who want to pass in these tokens instead of using a connection string
                        // should see the custom SAS token provider sample in this repo.
                        // https://github.com/Azure/azure-iot-sdk-java/blob/main/device/iot-device-samples/custom-sas-token-provider-sample/src/main/java/samples/com/microsoft/azure/sdk/iot/CustomSasTokenProviderSample.java
                        log.error("Ending sample because the provided credentials have expired.");
                        System.exit(-1);
                    }

                    if (newStatus == IotHubConnectionStatus.DISCONNECTED
                        && newStatusReason != IotHubConnectionStatusChangeReason.CLIENT_CLOSE)
                    {
                        // only need to reconnect if the device client reaches a DISCONNECTED state and if it wasn't
                        // from intentionally closing the client.
                        synchronized (this.reconnectionLock)
                        {
                            // Note that you cannot call "deviceClient.open()" or "deviceClient.close()" from here
                            // since this is a callback thread. You must open/close the client from a different thread.
                            // Because of that, this sample wakes up the Iot-Hub-Connection-Manager-Thread to do that.
                            log.debug("Notifying the connection manager thread to start re-opening this client");
                            this.reconnectionLock.notify();
                        }
                    }

                    // upon reconnecting, it is optional, but recommended, to get the current twin state. This will
                    // allow you to get the current desired properties state in case this client missed any desired
                    // property updates while it was temporarily disconnected.
                    if (previousStatus == IotHubConnectionStatus.DISCONNECTED_RETRYING
                        && newStatus == IotHubConnectionStatus.CONNECTED)
                    {
                        // hold off on sending any new reported properties until the twin has been retrieved
                        this.gettingTwinAfterReconnection = true;
                        this.deviceClient.getTwinAsync(this, null);
                    }
                },
                null);

            Thread.currentThread().setName("Iot-Hub-Connection-Manager-Thread");
            while (true)
            {
                boolean encounteredFatalException = !openDeviceClientWithRetry();
                if (encounteredFatalException)
                {
                    // Fatal error encountered. Exit the sample, but close the client in the finally block first
                    return;
                }

                new Thread(() ->
                {
                    try
                    {
                        Thread.currentThread().setName("Iot-Hub-Worker-Thread");
                        // this function simulates how a user may use a device client instance once it is open. In this
                        // sample, it includes sending telemetry and updating reported properties.
                        doWork();
                    }
                    catch (IllegalStateException e)
                    {
                        log.debug("Client was closed while doing work. Ending worker thread while re-opening the client.");
                    }
                }).start();

                synchronized (this.reconnectionLock)
                {
                    // Wait until the client needs to be re-opened. This happens when the client reaches a
                    // terminal DISCONNECTED state unexpectedly. For instance, if the client loses network connectivity
                    // and does not regain network connectivity before the SDK's retry logic has timed out.
                    this.reconnectionLock.wait();
                }

                log.debug("Connection manager thread woken up to start re-opening this client");
            }
        }
        catch (InterruptedException e)
        {
            log.debug("Connection management function interrupted, likely because the sample has ended");
        }
        finally
        {
            deviceClient.close();
        }
    }

    private boolean openDeviceClientWithRetry() throws InterruptedException
    {
        while (true)
        {
            try
            {
                this.deviceClient.close();

                log.debug("Attempting to open the device client");
                this.deviceClient.open(false);
                log.debug("Successfully opened the device client");

                // region cloud to device messaging setup
                // This region can be removed if no cloud to device messaging will be sent to this client
                this.deviceClient.setMessageCallback(this, null);
                // endregion

                // region direct methods setup
                // This region can be removed if no direct methods will be invoked on this client
                this.deviceClient.subscribeToMethods(this, null);
                // endregion

                // region twin setup
                // This region can be removed if no twin features will be used by this client
                this.deviceClient.subscribeToDesiredProperties(this, null);

                // note that this call is optional, but recommended for most scenarios. If a device is booting up for the
                // first time, this call is the only way to get all the desired property updates that it missed while
                // being offline. However this does send and receive bits over the wire, so it is not free.
                this.twin = this.deviceClient.getTwin();

                this.reportedPropertiesToSend.setVersion(twin.getReportedProperties().getVersion());

                log.debug("Current twin received:");
                log.debug(this.twin.toString());
                // endregion

                return true;
            }
            catch (IotHubClientException e)
            {
                switch (e.getStatusCode())
                {
                    case UNAUTHORIZED:
                        log.error("Failed to open the device client due to incorrect or badly formatted credentials", e);
                        return false;
                    case NOT_FOUND:
                        log.error("Failed to open the device client because the device is not registered on your IoT Hub", e);
                        return false;
                }

                if (e.isRetryable())
                {
                    log.debug("Failed to open the device client due to a retryable exception", e);
                }
                else
                {
                    log.error("Failed to open the device client due to a non-retryable exception", e);
                    return false;
                }
            }

            // Users may want to have this delay determined by an exponential backoff in order to avoid flooding the
            // service with reconnect attempts. Lower delays mean a client will be reconnected sooner, but at the risk
            // of wasting CPU cycles with attempts that the service is too overwhelmed to handle anyways.
            log.debug("Sleeping a bit before retrying to open device client");
            Thread.sleep(1000);
        }
    }

    /**
     * Sends telemetry and updates reported properties periodically. Returns if the client's connection was lost during
     * this.
     * <p>
     * This method simulates typical useage of a client, but can be modified to fit your use case without losing the
     * automatic reconnection and retry that this sample has.
     *
     * @throws IllegalStateException If the client's connection was lost as this method attempted to send telemetry or
     * update reported properties.
     */
    private void doWork() throws IllegalStateException
    {
        while (true)
        {
            sendTelemetryAsync();
            updateReportedPropertiesAsync();

            try
            {
                // not recommended for actual applications, but this makes the sample's logs print at a readable pace
                Thread.sleep(10000);
            }
            catch (InterruptedException e)
            {
                log.debug("Worker function interrupted, likely because the sample is being stopped.");
                return;
            }
        }
    }

    // region telemetry
    private void sendTelemetryAsync()
    {
        Message messageToSend;
        if (this.telemetryToResend.isEmpty())
        {
            // If no previous messages failed to send, send a new message
            messageToSend = new Message("hello world");
        }
        else
        {
            // If any previous message failed to send, retry sending it before moving on to new messages
            messageToSend = this.telemetryToResend.remove();
        }

        try
        {
            this.deviceClient.sendEventAsync(messageToSend, this, null);
        }
        catch (IllegalStateException e)
        {
            log.debug("Device client was closed, so requeueing the message locally");
            this.telemetryToResend.add(messageToSend);
            throw e;
        }
    }

    // callback for when a telemetry message is sent
    @Override
    public void onMessageSent(Message sentMessage, IotHubClientException e, Object callbackContext)
    {
        if (e == null)
        {
            log.debug("Successfully sent message with correlation Id {}", sentMessage.getCorrelationId());
            return;
        }

        if (e.isRetryable())
        {
            log.warn("Failed to send message with correlation Id {} due to retryable error with status code {}. " +
                "Requeueing message.", sentMessage.getCorrelationId(), e.getStatusCode().name());

            telemetryToResend.add(sentMessage);
        }
        else
        {
            log.error("Failed to send message with correlation Id {} due to an unretryable error with status code {}. " +
                "Discarding message as it can never be sent", sentMessage.getCorrelationId(), e.getStatusCode().name());
        }
    }
    // endregion

    // region twin
    private void updateReportedPropertiesAsync()
    {
        String newPropertyKey = UUID.randomUUID().toString();
        String newPropertyValue = UUID.randomUUID().toString();
        this.reportedPropertiesToSend.put(newPropertyKey, newPropertyValue);

        if (this.gettingTwinAfterReconnection)
        {
            log.debug("Delaying sending new reported properties update until the full twin has been retrieved after the most recent disconnection");
            return;
        }

        try
        {
            this.deviceClient.updateReportedPropertiesAsync(
                this.reportedPropertiesToSend,
                this,
                newPropertyKey);
        }
        catch (IllegalStateException e)
        {
            log.debug("Device client was closed. Waiting until the connection has been re-opened to update reported properties.");
            throw e;
        }
    }

    // callback for when a reported properties update request has been acknowledged by the service
    @Override
    public void onReportedPropertiesUpdateAcknowledged(
        IotHubStatusCode statusCode,
        ReportedPropertiesUpdateResponse response,
        IotHubClientException e,
        Object context)
    {
        String newReportedPropertyKey = (String) context;

        if (e != null)
        {
            // no need to do anything with the error here. Below, the status code is checked in order to figure out how to respond
            log.warn("Encountered an issue sending a reported properties update request", e);
        }

        if (e == null)
        {
            for (String propertyKey : this.reportedPropertiesToSend.keySet())
            {
                log.debug("Successfully updated reported properties with new key {} with value {}",
                    propertyKey,
                    this.reportedPropertiesToSend.get(propertyKey));
            }

            int newReportedPropertiesVersion = response.getVersion();
            log.debug("New reported properties version is {}", newReportedPropertiesVersion);
            this.reportedPropertiesToSend.setVersion(newReportedPropertiesVersion);

            // update the local twin object now that the reported properties have been sent
            twin.getReportedProperties().setVersion(newReportedPropertiesVersion);

            if (!this.reportedPropertiesToSend.isEmpty())
            {
                twin.getReportedProperties().putAll(this.reportedPropertiesToSend);
            }

            // no need to send these properties again in the next reported properties update, so clear these properties.
            this.reportedPropertiesToSend.clear();
        }
        else if (statusCode == IotHubStatusCode.PRECONDITION_FAILED)
        {
            for (String propertyKey : this.reportedPropertiesToSend.keySet())
            {
                log.debug("Failed to update reported properties with new key {} with value {} due to the reported " +
                    "properties version being out of date. Will try sending again later after updating the " +
                    "reported properties version.", propertyKey, this.reportedPropertiesToSend.get(propertyKey));
            }

            this.reportedPropertiesToSend.setVersion(twin.getReportedProperties().getVersion());
        }
        else if (e.isRetryable())
        {
            for (String propertyKey : this.reportedPropertiesToSend.keySet())
            {
                log.warn("Failed to update reported properties with new key {} with value {} due to retryable error " +
                        "with status code {}. Will try sending again later.",
                    propertyKey,
                    this.reportedPropertiesToSend.get(propertyKey),
                    statusCode.name());
            }
        }
        else
        {
            String newReportedPropertyValue = (String) twin.getReportedProperties().remove(newReportedPropertyKey);
            log.error("Failed to update reported properties with new key {} with value {} due to an unretryable error " +
                    "with status code {}. Removing new property from twin.",
                newReportedPropertyKey,
                newReportedPropertyValue,
                statusCode.name());
        }
    }

    // callback for when a desired property update is received
    @Override
    public void onDesiredPropertiesUpdated(Twin newTwin, Object context)
    {
        log.debug("Desired properties update received by device");
        if (!newTwin.getDesiredProperties().isEmpty())
        {
            this.twin.getDesiredProperties().putAll(newTwin.getDesiredProperties());
        }

        this.twin.getDesiredProperties().setVersion(newTwin.getDesiredProperties().getVersion());

        log.debug("New twin state from device side:");
        log.debug(this.twin.toString());
    }

    // callback for when a get twin call has succeeded and the current twin has been retrieved.
    @Override
    public void onTwinReceived(Twin twin, IotHubClientException e, Object context)
    {
        if (e == null)
        {
            log.debug("Received the current twin state: ");
            log.debug(twin.toString());
            this.twin = twin;
            this.reportedPropertiesToSend.setVersion(twin.getReportedProperties().getVersion());
            this.gettingTwinAfterReconnection = false;
        }
        else if (e.isRetryable())
        {
            log.warn("Encountered a retryable error with status code {} while trying to get the client's twin. " +
                "Trying again...", e.getStatusCode());

            this.deviceClient.getTwinAsync(this, null);
        }
        else
        {
            log.error("Encountered a non retryable error with status code {} while trying to get the client's twin. " +
                "Abandoning getting twin.", e.getStatusCode());
        }

    }
    // endregion

    // region direct methods
    // callback for when a direct method is invoked on this device
    @Override
    public DirectMethodResponse onMethodInvoked(String methodName, DirectMethodPayload payload, Object context)
    {
        // Typically there would be some method handling that differs based on the name of the method and/or the payload
        // provided, but this sample's method handling is simplified for brevity. There are other samples in this repo
        // that demonstrate handling methods in more depth.
        log.debug("Method {} invoked on device.", methodName);
        return new DirectMethodResponse(200, null);
    }
    // endregion

    // region cloud to device messaging
    // callback for when a cloud to device message is received by this device
    @Override
    public IotHubMessageResult onCloudToDeviceMessageReceived(Message message, Object callbackContext)
    {
        log.debug("Received cloud to device message with correlation Id {}", message.getCorrelationId());
        return IotHubMessageResult.COMPLETE;
    }
    // endregion
}
