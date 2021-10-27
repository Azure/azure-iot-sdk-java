// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers;

import com.microsoft.azure.sdk.iot.device.Message;
import lombok.extern.slf4j.Slf4j;
import samples.com.microsoft.azure.sdk.iot.DeviceClientManager;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ErrorInjectionHelper;

import java.util.concurrent.TimeUnit;

import static tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.LonghaulTests.DELAY_BETWEEN_FAULT_INJECTIONS_MINUTES_MAXIMUM;
import static tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.LonghaulTests.DELAY_BETWEEN_FAULT_INJECTIONS_MINUTES_MINUMUM;

/**
 * A thread manager for randomly applying faults to a particular device.
 */
@Slf4j
public class FaultInjectionManager
{
    final Thread faultInjectionThread;

    public FaultInjectionManager(DeviceClientManager deviceClientManager, String faultType, String faultReason)
    {
        this.faultInjectionThread = new Thread(() ->
        {
            try
            {
                while (true)
                {
                    int randomMinutesToSleepBeforeNextFaultInjection = (int) (Math.random() * DELAY_BETWEEN_FAULT_INJECTIONS_MINUTES_MAXIMUM + DELAY_BETWEEN_FAULT_INJECTIONS_MINUTES_MINUMUM);

                    log.trace("Sleeping {} minutes before sending the next fault injection message", randomMinutesToSleepBeforeNextFaultInjection);
                    TimeUnit.MINUTES.sleep(randomMinutesToSleepBeforeNextFaultInjection);

                    log.trace("Sending fault injection message...");
                    Message message = ErrorInjectionHelper.createMessageWithErrorInjectionProperties(
                        faultType,
                        faultReason,
                        ErrorInjectionHelper.DefaultDelayInSec,
                        ErrorInjectionHelper.DefaultDurationInSec);

                    deviceClientManager.sendEventAsync(
                        message,
                        (responseStatus, callbackContext) -> log.info("Fault injection message sent successfully"),
                        null);
                }
            }
            catch (InterruptedException ex)
            {
                log.info("Ending random fault injection thread");
            }
        });
    }

    public void startRandomFaultInjection()
    {
        this.faultInjectionThread.start();
    }

    public void stopRandomFaultInjection()
    {
        this.faultInjectionThread.interrupt();
    }
}
