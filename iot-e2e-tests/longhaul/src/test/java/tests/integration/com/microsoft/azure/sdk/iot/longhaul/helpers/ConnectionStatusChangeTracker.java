// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers;

import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.LonghaulTests.DECIMAL_FORMAT;

/**
 * Implementation of {@link IotHubConnectionStatusChangeCallback} that tracks statistics around device connectivity.
 */
@Slf4j
public class ConnectionStatusChangeTracker implements IotHubConnectionStatusChangeCallback
{
    final String deviceId;

    /**
     * Construct a connection status change tracker for a Multiplexed connection.
     */
    public ConnectionStatusChangeTracker()
    {
        // this connection status change tracker is for a multiplexing client, not for a particular device
        this.deviceId = null;
    }

    /**
     * Construct a connection status change tracker for a singleplexed connection or for a device within a multiplexed connection.
     */
    public ConnectionStatusChangeTracker(String deviceId)
    {
        this.deviceId = deviceId;
    }

    @Getter
    private IotHubConnectionStatus currentStatus = null;

    @Getter
    @Setter
    private long timeOfLastConnectionStatusUpdate = System.currentTimeMillis();

    @Getter
    @Setter
    private double minutesSpentConnected = 0;

    @Getter
    @Setter
    private double minutesSpentDisconnectedRetrying = 0;

    @Getter
    @Setter
    private double minutesSpentDisconnected = 0;

    @Getter
    private long disconnectedRetryingCount = 0;

    @Override
    public void execute(IotHubConnectionStatus newStatus, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
    {
        if (deviceId == null)
        {
            log.info("MULTIPLEXED CONNECTION STATUS UPDATE: {}", newStatus);
            log.info("MULTIPLEXED CONNECTION STATUS REASON: {}", statusChangeReason);
            log.info("MULTIPLEXED CONNECTION STATUS THROWABLE: {}", (throwable == null ? "null" : throwable.getMessage()));
        }
        else
        {
            log.info("{} CONNECTION STATUS UPDATE: {}", this.deviceId, newStatus);
            log.info("{} CONNECTION STATUS REASON: {}", this.deviceId, statusChangeReason);
            log.info("{} CONNECTION STATUS THROWABLE: {}", this.deviceId, (throwable == null ? "null" : throwable.getMessage()));
        }

        if (throwable != null)
        {
            log.error("Full error stacktrace", throwable);
        }

        if (newStatus == IotHubConnectionStatus.DISCONNECTED_RETRYING)
        {
            disconnectedRetryingCount++;
        }

        if (currentStatus != newStatus)
        {
            double previousIntervalInMinutes = (System.currentTimeMillis() - timeOfLastConnectionStatusUpdate) / (1000.0 * 60.0);
            if (currentStatus == IotHubConnectionStatus.CONNECTED)
            {
                minutesSpentConnected += previousIntervalInMinutes;
            }
            else if (currentStatus == IotHubConnectionStatus.DISCONNECTED_RETRYING)
            {
                minutesSpentDisconnectedRetrying += previousIntervalInMinutes;
            }
            else if (currentStatus == IotHubConnectionStatus.DISCONNECTED)
            {
                minutesSpentDisconnected += previousIntervalInMinutes;
            }
        }

        timeOfLastConnectionStatusUpdate = System.currentTimeMillis();
        currentStatus = newStatus;
    }

    /**
     * Log the current status and the cumulative connection statistics.
     */
    public void logCurrentStatus()
    {
        if (this.deviceId != null)
        {
            log.info("Current device connection status for device {}: {}", this.deviceId, currentStatus);
            log.info("Total disconnected retrying count for device {}: {}", this.deviceId, disconnectedRetryingCount);
            log.info("Total minutes spent connected for device {}: {}", this.deviceId, DECIMAL_FORMAT.format(minutesSpentConnected));
            log.info("Total minutes spent disconnected retrying for device {}: {}", this.deviceId, DECIMAL_FORMAT.format(minutesSpentDisconnectedRetrying));
            log.info("Total minutes spent disconnected for device {}: {}", this.deviceId, DECIMAL_FORMAT.format(minutesSpentDisconnected));
        }
        else
        {
            log.info("Current multiplexed connection status: {}", currentStatus);
            log.info("Total disconnected retrying count: {}", disconnectedRetryingCount);
            log.info("Total minutes spent connected: {}", DECIMAL_FORMAT.format(minutesSpentConnected));
            log.info("Total minutes spent disconnected retrying: {}", DECIMAL_FORMAT.format(minutesSpentDisconnectedRetrying));
            log.info("Total minutes spent disconnected: {}", DECIMAL_FORMAT.format(minutesSpentDisconnected));
        }
    }

    /**
     * Update the connection statistics manually. These are automatically updated upon each connection status change, but
     * a healthy connection doesn't have many of those, so the statistics would fall out of date quickly without manual updates.
     */
    public void refreshConnectionStatistics()
    {
        double previousIntervalInMinutes = (System.currentTimeMillis() - this.getTimeOfLastConnectionStatusUpdate()) / (1000.0 * 60.0);
        if (this.getCurrentStatus() == IotHubConnectionStatus.CONNECTED)
        {
            this.setMinutesSpentConnected(this.getMinutesSpentConnected() + previousIntervalInMinutes);
        }
        else if (this.getCurrentStatus() == IotHubConnectionStatus.DISCONNECTED_RETRYING)
        {
            this.setMinutesSpentDisconnectedRetrying(this.getMinutesSpentDisconnectedRetrying() + previousIntervalInMinutes);
        }
        else if (this.getCurrentStatus() == IotHubConnectionStatus.DISCONNECTED)
        {
            this.setMinutesSpentDisconnected(this.getMinutesSpentDisconnected() + previousIntervalInMinutes);
        }

        this.setTimeOfLastConnectionStatusUpdate(System.currentTimeMillis());
    }
}
