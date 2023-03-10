/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.twin.Pair;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.junit.Assert;

import java.io.IOException;
import java.util.*;

import static tests.integration.com.microsoft.azure.sdk.iot.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;

/*
 * This class contains common code for Junit and Android test cases
 */
public class IotHubServicesCommon
{
    private static final int TIMEOUT_MILLISECONDS = 60 * 1000; // 1 minute
    private static final int CHECK_INTERVAL_MILLISECONDS = 300;

    public static void sendErrorInjectionMessageAndWaitForResponse(InternalClient client, MessageAndResult messageAndResult, IotHubClientProtocol protocol)
    {
        if (protocol == IotHubClientProtocol.MQTT || protocol == IotHubClientProtocol.MQTT_WS)
        {
            // error injection message will not be ack'd by service if sent over MQTT/MQTT_WS, so the SDK's
            // retry logic will try to send it again after the connection drops. By setting expiry time,
            // we ensure that error injection message isn't resent to service too many times. The message will still likely
            // be sent 3 or 4 times causing 3 or 4 disconnections, but the test should recover anyways.
            messageAndResult.message.setExpiryTime(1000);

            // Since the message won't be ack'd, then we don't need to validate the status code when this message's callback is fired
            messageAndResult.statusCode = null;
        }

        sendMessageAndWaitForResponse(client, messageAndResult, protocol);
    }

    public static void sendMessageAndWaitForResponse(InternalClient client, MessageAndResult messageAndResult, IotHubClientProtocol protocol)
    {
        try
        {
            Success messageSent = new Success();
            EventCallback callback = new EventCallback(messageAndResult.statusCode);
            client.sendEventAsync(messageAndResult.message, callback, messageSent);

            long startTime = System.currentTimeMillis();
            while (!messageSent.wasCallbackFired())
            {
                Thread.sleep(CHECK_INTERVAL_MILLISECONDS);
                if (System.currentTimeMillis() - startTime > TIMEOUT_MILLISECONDS)
                {
                    Assert.fail(buildExceptionMessage("Timed out waiting for a message callback", client));
                    break;
                }
            }

            if (messageAndResult.statusCode != null && messageSent.getCallbackStatusCode() != messageAndResult.statusCode)
            {
                Assert.fail(buildExceptionMessage("Sending message over " + protocol + " protocol failed: expected " + messageAndResult.statusCode + " but received " + messageSent.getCallbackStatusCode(), client));
            }
        }
        catch (Exception e)
        {
            Assert.fail(buildExceptionMessage("Sending message over " + protocol + " protocol failed: Exception encountered while sending and waiting on a message: " + e.getMessage(), client));
        }
    }

    public static void waitForStabilizedConnection(List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates, InternalClient client) throws InterruptedException
    {
        //Wait until error injection takes effect
        long startTime = System.currentTimeMillis();
        while (!actualStatusUpdatesContainsStatus(actualStatusUpdates, IotHubConnectionStatus.DISCONNECTED_RETRYING))
        {
            Thread.sleep(200);
            long timeElapsed = System.currentTimeMillis() - startTime;
            if (timeElapsed > TIMEOUT_MILLISECONDS)
            {
                Assert.fail(buildExceptionMessage("Timed out waiting for error injection message to take effect", client));
            }
        }

        // Wait for first connect
        while (actualStatusUpdates.size() == 0)
        {
            Thread.sleep(200);

            long timeElapsed = System.currentTimeMillis() - startTime;

            // 2 minutes timeout waiting for first connection to occur
            if (timeElapsed > 20000)
            {
                Assert.fail(buildExceptionMessage("Timed out waiting for a first connection success", client));
            }
        }

        confirmOpenStabilized(actualStatusUpdates, client);
    }

    public static void confirmOpenStabilized(List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates, InternalClient client) throws InterruptedException
    {
        long startTime = System.currentTimeMillis();
        long timeElapsed;

        int numOfUpdates = 0;
        if (actualStatusUpdates != null)
        {
            while (numOfUpdates == 0
                    || numOfUpdates != actualStatusUpdates.size()
                    || actualStatusUpdates.get(actualStatusUpdates.size() - 1).getKey() != IotHubConnectionStatus.CONNECTED)
            {
                numOfUpdates = actualStatusUpdates.size();
                Thread.sleep(1000);
                timeElapsed = System.currentTimeMillis() - startTime;

                if (timeElapsed > TIMEOUT_MILLISECONDS)
                {
                    Assert.fail(buildExceptionMessage("Timed out waiting for a stable connection on first open", client));
                }
            }
        }
    }

    public static boolean actualStatusUpdatesContainsStatus(List<Pair<IotHubConnectionStatus, Throwable>> actualStatusUpdates, IotHubConnectionStatus status)
    {
        for (Pair<IotHubConnectionStatus, Throwable> actualStatusUpdate : actualStatusUpdates)
        {
            if (actualStatusUpdate.getKey() == status)
            {
                return true;
            }
        }

        return false;
    }
}
