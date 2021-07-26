package com.microsoft.azure.sdk.iot.device.convention;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.DeviceIO;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethod;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;

public class DeviceCommand extends DeviceMethod
{
    /**
     * This constructor creates an instance of device method class which helps facilitate the interation for device methods
     * between the user and IotHub.
     *
     * @param deviceIO                          Device client  object for this connection instance for the device. Cannot be {@code null}
     * @param config                            Device client  configuration Cannot be {@code null}
     * @param deviceMethodStatusCallback        Callback to provide status for device method state with IotHub. Cannot be {@code null}.
     * @param deviceMethodStatusCallbackContext Context to be passed when device method status is invoked. Can be {@code null}
     * @throws IllegalArgumentException This exception is thrown if either deviceIO or config or deviceMethodStatusCallback are null
     */
    public DeviceCommand(DeviceIO deviceIO, DeviceClientConfig config, IotHubEventCallback deviceMethodStatusCallback, Object deviceMethodStatusCallbackContext) throws IllegalArgumentException
    {
        super(deviceIO, config, deviceMethodStatusCallback, deviceMethodStatusCallbackContext);
    }
}
