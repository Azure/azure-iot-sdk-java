/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.twin.DesiredPropertiesUpdate;
import com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse;
import com.microsoft.azure.sdk.iot.device.twin.MethodCallback;
import com.microsoft.azure.sdk.iot.device.twin.Property;
import com.microsoft.azure.sdk.iot.device.twin.Twin;
import com.microsoft.azure.sdk.iot.device.twin.TwinCollection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Implement a fake device to the end to end test.
 */
public class DeviceEmulator
{
    public static final String METHOD_RESET = "reset";
    public static final String METHOD_LOOPBACK = "loopback";
    public static final String METHOD_DELAY_IN_MILLISECONDS = "delayInMilliseconds";
    public static final String METHOD_UNKNOWN = "unknown";

    public static final int METHOD_SUCCESS = 200;
    public static final int METHOD_THROWS = 403;
    public static final int METHOD_NOT_DEFINED = 404;

    private static final int DEVICE_METHOD_SUBSCRIBE_TIMEOUT_MILLISECONDS = 60 * 1000;
    private static final int METHOD_SUBSCRIBE_CHECK_PERIOD_MILLISECONDS = 1000;

    private final InternalClient client;
    private final DeviceStatus deviceStatus = new DeviceStatus();
    private final ConcurrentMap<String, ConcurrentLinkedQueue<Object>> twinChanges = new ConcurrentHashMap<>();

    /**
     * CONSTRUCTOR
     * Creates a new instance of the device emulator, and connect it to the IoTHub using the provided connectionString
     * and protocol.
     *
     */
    DeviceEmulator(InternalClient client)
    {
        this.client = client;
    }

    void open() throws IOException
    {
        this.client.open(false);
    }

    /**
     * Ends the DeviceClient connection and destroy the thread.
     * @throws IOException if the DeviceClient cannot close the connection with the IoTHub.
     */
    void tearDown() throws IOException
    {
        if (this.client != null)
        {
            this.client.close();
        }
    }

    /**
     * Enable device method on this device using the local callbacks.
     * @throws IOException if the client failed to subscribe on the device method.
     */
    void subscribeToDirectMethod() throws IOException, InterruptedException
    {
        subscribeToDirectMethod(null, null, null, null);
    }

    @SuppressWarnings("SameParameterValue") // DeviceEmulator will subscribe to default callback in case the supplied callback is null
    void subscribeToDirectMethod(
        MethodCallback methodCallback, Object directMethodCallbackContext,
        IotHubEventCallback directMethodStatusCallback, Object directMethodStatusCallbackContext)
            throws IOException, InterruptedException
    {
        if(methodCallback == null)
        {
            methodCallback = new MethodInvokeCallback();
            directMethodCallbackContext = null;
        }

        if(directMethodStatusCallback == null)
        {
            directMethodStatusCallback = new DeviceStatusCallback();
            directMethodStatusCallbackContext = deviceStatus;
        }

        client.subscribeToMethodsAsync(
            methodCallback, directMethodCallbackContext,
                directMethodStatusCallback, directMethodStatusCallbackContext);

        long startTime = System.currentTimeMillis();
        while (deviceStatus.statusOk == 0)
        {
            if (deviceStatus.statusError > 0)
            {
                throw new AssertionError("Subscribing to device methods failed");
            }

            Thread.sleep(METHOD_SUBSCRIBE_CHECK_PERIOD_MILLISECONDS);

            if (System.currentTimeMillis() - startTime > DEVICE_METHOD_SUBSCRIBE_TIMEOUT_MILLISECONDS)
            {
                throw new AssertionError("Timed out waiting for device method subscription to be acknowledged");
            }
        }
    }

    /**
     * Enable device twin on the emulator using the standard values.
     *
     * For standard values, getTwinChanges will provide a Map with desired
     *  properties reported in the callback, and getStatusOK and getStatusError
     *  will replace the status callback.
     *
     * @throws IOException if failed to start the Device twin.
     */
    void subscribeToDeviceTwin() throws IOException
    {
        CountDownLatch subscribedLatch = new CountDownLatch(1);
        client.subscribeToDesiredPropertiesAsync(desiredPropertiesUpdate ->
            {
                Twin twin = desiredPropertiesUpdate.getTwin();
                TwinCollection desiredProperties = twin.getDesiredProperties();
                for (String key : desiredProperties.keySet())
                {
                    DeviceTwinProperty.onPropertyChanged(key, desiredProperties.get(key), null);
                }
            },
            (responseStatus, callbackContext) -> subscribedLatch.countDown());
    }

    /**
     * Clean all previous state to start a new test.
     */
    void clearStatistics()
    {
        deviceStatus.statusOk = 0;
        deviceStatus.statusError = 0;

        twinChanges.clear();
    }

    ConcurrentMap<String, ConcurrentLinkedQueue<Object>> getTwinChanges()
    {
        return this.twinChanges;
    }

    /**
     * Get the number of invoke answers with success since the last cleaning.
     * @return Number of invoke answers with success.
     */
    int getStatusOk()
    {
        return deviceStatus.statusOk;
    }

    /**
     * Get the number of failed invoke answers since the last cleaning.
     * @return Number of failed invoke answers.
     */
    int getStatusError()
    {
        return deviceStatus.statusError;
    }

    InternalClient getClient() {return client;}

    private static class DeviceStatus
    {
        int statusOk;
        int statusError;
    }

    private class DeviceStatusCallback implements IotHubEventCallback
    {
        @Override
        public synchronized void execute(IotHubStatusCode status, Object context)
        {
            DeviceStatus deviceStatus = (DeviceStatus)context;
            switch(status)
            {
                case OK:
                    deviceStatus.statusOk++;
                    break;
                default:
                    deviceStatus.statusError++;
                    break;
            }
        }
    }

    protected static class DeviceTwinProperty
    {
        public static synchronized void onPropertyChanged(String propertyKey, Object propertyValue, Object context)
        {
            System.out.println("Device updated " + propertyKey + " to " + propertyValue.toString());
            ConcurrentMap<String, ConcurrentLinkedQueue<Object>> twinChanges = (ConcurrentMap<String, ConcurrentLinkedQueue<Object>>)context;
            if(!twinChanges.containsKey(propertyKey))
            {
                twinChanges.put(propertyKey, new ConcurrentLinkedQueue<>());
            }
            twinChanges.get(propertyKey).add(propertyValue.toString());
        }
    }

    protected class MethodInvokeCallback implements MethodCallback
    {
        @Override
        public synchronized DirectMethodResponse call(String methodName, Object methodData, Object context)
        {
            System.out.println("Device invoked " + methodName);
            DirectMethodResponse directMethodResponse;
            int status;
            String result;
            try
            {
                switch (methodName)
                {
                    case METHOD_RESET:
                        result = METHOD_RESET + ":succeed";
                        status = METHOD_SUCCESS;
                        tearDown();
                        break;
                    case METHOD_LOOPBACK:
                        result = loopback(methodData);
                        status = METHOD_SUCCESS;
                        break;
                    case METHOD_DELAY_IN_MILLISECONDS:
                        result = delayInMilliseconds(methodData);
                        status = METHOD_SUCCESS;
                        break;
                    default:
                        result = "unknown:" + methodName;
                        status = METHOD_NOT_DEFINED;
                        break;
                }
            }
            catch (Exception e)
            {
                result = e.toString();
                status = METHOD_THROWS;
            }
            directMethodResponse = new DirectMethodResponse(status, result);

            return directMethodResponse;
        }
    }

    private String loopback(Object methodData)
    {
        String payload = new String((byte[])methodData, StandardCharsets.UTF_8).replace("\"", "");
        return METHOD_LOOPBACK + ":" + payload;
    }

    private String delayInMilliseconds(Object methodData) throws InterruptedException
    {
        String payload = new String((byte[])methodData, StandardCharsets.UTF_8).replace("\"", "");
        long delay = Long.parseLong(payload);
        Thread.sleep(delay);
        return METHOD_DELAY_IN_MILLISECONDS + ":succeed";
    }
}
