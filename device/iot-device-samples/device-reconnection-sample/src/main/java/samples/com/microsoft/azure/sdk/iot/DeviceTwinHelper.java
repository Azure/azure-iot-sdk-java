package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class DeviceTwinHelper {
    static AtomicBoolean Succeed = new AtomicBoolean(false);
    static String TwinPropertyKey = "HomeTemp(F)";

    protected static class DeviceTwinStatusCallBack implements IotHubEventCallback {
        @Override
        public void execute(IotHubStatusCode status, Object context) {
            if ((status == IotHubStatusCode.OK) || (status == IotHubStatusCode.OK_EMPTY)) {
                Succeed.set(true);
            } else {
                Succeed.set(false);
            }
            log.debug("IoT Hub responded to device twin operation with status {}", status.name());
        }
    }

    protected static class onProperty implements TwinPropertyCallBack {
        @Override
        public void TwinPropertyCallBack(Property property, Object context) {
            log.debug(">> onProperty callback for {} property: {} to {}, Properties version: {}",
                    (property.getIsReported() ? "reported" : "desired"),
                    property.getKey(),
                    property.getValue(),
                    property.getVersion());
        }
    }
}
