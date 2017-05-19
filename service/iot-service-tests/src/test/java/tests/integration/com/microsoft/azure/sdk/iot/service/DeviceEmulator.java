/*
* Copyright (c) Microsoft. All rights reserved.
* Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.integration.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

/**
 * Implement a fake device to the end to end test.
 */
public class DeviceEmulator  implements Runnable
{
    private static final String METHOD_RESET = "reset";
    private static final String METHOD_LOOPBACK = "loopback";
    private static final String METHOD_DELAY_IN_MILLISECONDS = "delayInMilliseconds";

    private static final int METHOD_SUCCESS = 200;
    private static final int METHOD_THROWS = 403;
    private static final int METHOD_NOT_DEFINED = 404;

    private static final int STOP_DEVICE_INTERVAL_IN_MILLISECONDS = 10000;

    private DeviceClient deviceClient;
    private DeviceStatus deviceStatus = new DeviceStatus();
    private Boolean stopDevice = false;

    private DeviceMethodCallback deviceMethodCallback;
    private Object deviceMethodCallbackContext;
    private IotHubEventCallback deviceMethodStatusCallback;
    private Object deviceMethodStatusCallbackContext;

    /**
     * CONSTRUCTOR
     * Creates a new instance of the device emulator, and connect it to the IoTHub using the provided connectionString
     * and protocol.
     *
     * @param connectionString is the string that identify the device in the IoTHub
     * @param protocol is the desired protocol for the transport layer (HTTPS, AMQPS, MQTT, or AMQPS_WS)
     * @throws URISyntaxException if the DeviceClient cannot resolve the URI.
     * @throws IOException if the DeviceClient cannot open the connection with the IoTHub.
     * @throws InterruptedException if the thread had issue to wait for the open connection.
     */
    public DeviceEmulator(String connectionString, IotHubClientProtocol protocol) throws URISyntaxException, IOException, InterruptedException
    {
        this.deviceClient = new DeviceClient(connectionString, protocol);
        clearStatus();
        deviceClient.open();
    }

    @Override
    public void run()
    {
        while(!stopDevice)
        {
            try
            {
                Thread.sleep(STOP_DEVICE_INTERVAL_IN_MILLISECONDS);
            }
            catch (InterruptedException e)
            {

            }
        }
    }

    /**
     * Ends the DeviceClient connection and destroy the thread.
     * @throws IOException if the DeviceClient cannot close the connection with the IoTHub.
     */
    public void stop() throws IOException
    {
        deviceClient.close();
        stopDevice = true;
    }

    /**
     * Enable device method on this device using the local callbacks.
     * @throws IOException if the deviceClient failed to subscribe on the device method.
     */
    public void enableDeviceMethod() throws IOException
    {
        enableDeviceMethod(null, null, null, null);
    }

    /**
     * Enable device method on this device.
     * @param deviceMethodCallback is the callback called when a service invoke a method on this device. If it is null,
     *                             the DeviceEmulator will take care of it using the MethodInvokeCallback.
     * @param deviceMethodCallbackContext is the context for the deviceMethodCallback. Only used if the
     *                                    deviceMethodCallback is not null.
     * @param deviceMethodStatusCallback is the callback called when the service receive the response for the invoked
     *                                   method. If it is null, the DeviceEmulator will take care of it using
     *                                   DeviceMethodStatusCallback.
     * @param deviceMethodStatusCallbackContext is the context for the deviceMethodStatusCallback.Only used if the
     *                                    deviceMethodStatusCallback is not null.
     * @throws IOException if the deviceClient failed to subscribe on the device method.
     */
    public void enableDeviceMethod(
            DeviceMethodCallback deviceMethodCallback, Object deviceMethodCallbackContext,
            IotHubEventCallback deviceMethodStatusCallback, Object deviceMethodStatusCallbackContext)
            throws IOException
    {
        if(deviceMethodCallback == null)
        {
            this.deviceMethodCallback = new MethodInvokeCallback();
            this.deviceMethodCallbackContext = null;
        }
        else
        {
            this.deviceMethodCallback = deviceMethodCallback;
            this.deviceMethodCallbackContext = deviceMethodCallbackContext;
        }

        if(deviceMethodStatusCallback == null)
        {
            this.deviceMethodStatusCallback = new DeviceMethodStatusCallback();
            this.deviceMethodStatusCallbackContext = deviceStatus;
        }
        else
        {
            this.deviceMethodStatusCallback = deviceMethodStatusCallback;
            this.deviceMethodStatusCallbackContext = deviceMethodStatusCallbackContext;
        }

        deviceClient.subscribeToDeviceMethod(
                this.deviceMethodCallback, this.deviceMethodCallbackContext,
                this.deviceMethodStatusCallback, this.deviceMethodStatusCallbackContext);
    }

    /**
     * Clean all previous state to start a new test.
     */
    public void clearStatus()
    {
        deviceStatus.statusOk = 0;
        deviceStatus.statusError = 0;
    }

    /**
     * Get the number of invoke answers with success since the last cleaning.
     * @return Number of invoke answers with success.
     */
    public int getStatusOk()
    {
        return deviceStatus.statusOk;
    }

    /**
     * Get the number of failed invoke answers since the last cleaning.
     * @return Number of failed invoke answers.
     */
    public int getStatusError()
    {
        return deviceStatus.statusError;
    }

    private class DeviceStatus
    {
        int statusOk;
        int statusError;
    }

    private class DeviceMethodStatusCallback implements IotHubEventCallback
    {
        @Override
        public synchronized void execute(IotHubStatusCode status, Object context)
        {
            DeviceStatus deviceStatus = (DeviceStatus)context;
            switch(status)
            {
                case OK:
                case OK_EMPTY:
                    deviceStatus.statusOk++;
                    break;
                default:
                    deviceStatus.statusError++;
                    break;
            }
        }
    }

    protected class MethodInvokeCallback implements DeviceMethodCallback
    {
        @Override
        public synchronized DeviceMethodData call(String methodName, Object methodData, Object context)
        {
            DeviceMethodData deviceMethodData;
            int status;
            String result;
            try
            {
                switch (methodName)
                {
                    case METHOD_RESET:
                        result = METHOD_RESET + ":succeed";
                        status = METHOD_SUCCESS;
                        stop();
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
            deviceMethodData = new DeviceMethodData(status, result);

            return deviceMethodData;
        }
    }

    private String loopback(Object methodData) throws UnsupportedEncodingException
    {
        String payload = new String((byte[])methodData, "UTF-8").replace("\"", "");
        return METHOD_LOOPBACK + ":" + payload;
    }

    private String delayInMilliseconds(Object methodData) throws UnsupportedEncodingException, InterruptedException
    {
        String payload = new String((byte[])methodData, "UTF-8").replace("\"", "");
        long delay = Long.parseLong(payload);
        Thread.sleep(delay);
        return METHOD_DELAY_IN_MILLISECONDS + ":succeed";
    }

}
