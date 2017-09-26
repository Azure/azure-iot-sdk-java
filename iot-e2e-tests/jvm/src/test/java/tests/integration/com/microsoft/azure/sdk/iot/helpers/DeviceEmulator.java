/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Device;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Implement a fake device to the end to end test.
 */
public class DeviceEmulator  implements Runnable
{
    public static final String METHOD_RESET = "reset";
    public static final String METHOD_LOOPBACK = "loopback";
    public static final String METHOD_DELAY_IN_MILLISECONDS = "delayInMilliseconds";
    public static final String METHOD_UNKNOWN = "unknown";

    public static final int METHOD_SUCCESS = 200;
    public static final int METHOD_THROWS = 403;
    public static final int METHOD_NOT_DEFINED = 404;

    private static final int STOP_DEVICE_INTERVAL_IN_MILLISECONDS = 10000;

    private enum LIGHTS{ ON, OFF, DISABLED }

    private DeviceClient deviceClient;
    private DeviceStatus deviceStatus = new DeviceStatus();
    private ConcurrentMap<String, ConcurrentLinkedQueue<Object>> twinChanges = new ConcurrentHashMap<>();
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
    DeviceEmulator(String connectionString, IotHubClientProtocol protocol) throws URISyntaxException, IOException, InterruptedException
    {
        this.deviceClient = new DeviceClient(connectionString, protocol);
        clearStatistics();
        deviceClient.open();
    }

    /**
     * CONSTRUCTOR
     * Creates a new instance of the device emulator, and connect it to the IoTHub using the provided connectionString, protocol, and x509 keys.
     *
     * @param connectionString is the string that identify the device in the IoTHub
     * @param protocol is the desired protocol for the transport layer (HTTPS, AMQPS, MQTT, or AMQPS_WS)
     * @throws URISyntaxException if the DeviceClient cannot resolve the URI.
     * @throws IOException if the DeviceClient cannot open the connection with the IoTHub.
     * @throws InterruptedException if the thread had issue to wait for the open connection.
     */
    DeviceEmulator(String connectionString, IotHubClientProtocol protocol, String publicKeyCert, String privateKey) throws URISyntaxException, IOException, InterruptedException
    {
        this.deviceClient = new DeviceClient(connectionString, protocol, publicKeyCert, false, privateKey, false);
        clearStatistics();
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
    void stop() throws IOException
    {
        deviceClient.close();
        stopDevice = true;
    }

    /**
     * Enable device method on this device using the local callbacks.
     * @throws IOException if the deviceClient failed to subscribe on the device method.
     */
    void enableDeviceMethod() throws IOException
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
     *                                   DeviceStatusCallback.
     * @param deviceMethodStatusCallbackContext is the context for the deviceMethodStatusCallback.Only used if the
     *                                    deviceMethodStatusCallback is not null.
     * @throws IOException if the deviceClient failed to subscribe on the device method.
     */
    void enableDeviceMethod(
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
            this.deviceMethodStatusCallback = new DeviceStatusCallback();
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
     * Enable device twin on the emulator using the standard values.
     *
     * For standard values, getTwinChanges will provide a Map with desired
     *  properties reported in the callback, and getStatusOK and getStatusError
     *  will replace the status callback.
     *
     * @throws IOException if failed to start the Device Twin.
     */
    void enableDeviceTwin() throws IOException
    {
        enableDeviceTwin(null, null, null, null, true);
    }

    /**
     * Enable device twin on the emulated device.
     *
     * @param deviceTwinStatusCallBack callback to twin status. If {@code null}, use the local status callback.
     * @param deviceTwinStatusCallbackContext context for status callback. Used only if deviceTwinStatusCallBack is not {@code null}.
     * @param deviceTwin is the device twin including the properties callback. If {@code null}, use the local device with standard properties.
     * @param propertyCallBackContext context for the properties callback. Used only if deviceTwin is not {@code null}.
     * @param mustSubscribeToDesiredProperties is a boolean to define if it should or not subscribe to the desired properties.
     * @throws IOException if failed to start the Device Twin.
     */
    void enableDeviceTwin(IotHubEventCallback deviceTwinStatusCallBack, Object deviceTwinStatusCallbackContext,
                          Device deviceTwin, Object propertyCallBackContext, boolean mustSubscribeToDesiredProperties) throws IOException
    {
        // If user do not provide any status callback, use the local one.
        if(deviceTwinStatusCallBack == null)
        {
            deviceTwinStatusCallBack = new DeviceStatusCallback();
            deviceTwinStatusCallbackContext = deviceStatus;
        }

        // If user do not provide any deviceTwin, use the local one.
        if(deviceTwin == null)
        {
            deviceTwin = new DeviceTwinProperty();
            propertyCallBackContext = twinChanges;
        }

        deviceClient.startDeviceTwin(deviceTwinStatusCallBack, deviceTwinStatusCallbackContext, deviceTwin, propertyCallBackContext);

        if(mustSubscribeToDesiredProperties)
        {
            deviceClient.subscribeToDesiredProperties(null);
        }
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

    private class DeviceStatus
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
                case OK_EMPTY:
                    deviceStatus.statusOk++;
                    break;
                default:
                    deviceStatus.statusError++;
                    break;
            }
        }
    }

    protected class DeviceTwinProperty extends Device
    {
        @Override
        public synchronized void PropertyCall(String propertyKey, Object propertyValue, Object context)
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

    protected class MethodInvokeCallback implements DeviceMethodCallback
    {
        @Override
        public synchronized DeviceMethodData call(String methodName, Object methodData, Object context)
        {
            System.out.println("Device invoked " + methodName);
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
