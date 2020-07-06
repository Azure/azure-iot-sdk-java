// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class Thermostat {

    public enum StatusCode {
        COMPLETED (200),
        IN_PROGRESS (202),
        NOT_FOUND (404);

        private final int value;
        StatusCode(int value) {
            this.value = value;
        }
    }

    private static final String deviceConnectionString = System.getenv("IOTHUB_DEVICE_CONNECTION_STRING");
    private static final String MODEL_ID = "dtmi:com:example:Thermostat;1";

    // Plug and play features are available over either MQTT or MQTT_WS.
    private static final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

    private static final Random random = new Random();

    // HashMap to hold the temperature updates sent over each "Thermostat" component.
    // NOTE: Memory constrained device should leverage storage capabilities of an external service to store this information and perform computation.
    // See https://docs.microsoft.com/en-us/azure/event-grid/compare-messaging-services for more details.
    private static final Map<Date, Double> temperatureReadings = new HashMap<>();

    private static DeviceClient deviceClient;
    private static double temperature = 0.0d;
    private static double maxTemperature = 0.0d;
    private static boolean temperatureReset = true;

    public static void main(String[] args) throws URISyntaxException, IOException {

        // This sample follows the following workflow:
        // -> Initialize device client instance.
        // -> Set handler to receive "targetTemperature" updates, and send the received update over reported property.
        // -> Set handler to receive "getMaxMinReport" command, and send the generated report as command response.
        // -> Periodically send "temperature" over telemetry.
        // -> Send "maxTempSinceLastReboot" over property update, when a new max temperature is set.

        log.debug("Initialize the device client.");
        initializeDeviceClient();

        log.debug("Start twin and set handler to receive \"targetTemperature\" updates.");
        deviceClient.startDeviceTwin(new TwinIotHubEventCallback(), null, new TargetTemperatureUpdateCallback(), null);
        Map<Property, Pair<TwinPropertyCallBack, Object>> desiredPropertyUpdateCallback =
                Collections.singletonMap(
                        new Property("targetTemperature", null),
                        new Pair<TwinPropertyCallBack, Object>(new TargetTemperatureUpdateCallback(), null));
        deviceClient.subscribeToTwinDesiredProperties(desiredPropertyUpdateCallback);

        log.debug("Set handler to receive \"getMaxMinReport\" command.");
        String methodName = "getMaxMinReport";
        deviceClient.subscribeToDeviceMethod(new GetMaxMinReportMethodCallback(), methodName, new MethodIotHubEventCallback(), methodName);

        new Thread(new Runnable() {
            @SneakyThrows(InterruptedException.class)
            @Override
            public void run() {
                while (true) {
                    if (temperatureReset) {
                        // Generate a random value between 5.0°C and 45.0°C for the current temperature reading.
                        temperature = BigDecimal.valueOf(random.nextDouble() * 40 + 5).setScale(1, RoundingMode.HALF_UP).doubleValue();
                        temperatureReset = false;
                    }

                    try {
                        sendTemperatureReading();
                    } catch (IOException e) {
                        throw new RuntimeException("IOException when sending reported property update: ", e);
                    }

                    Thread.sleep(5 * 1000);
                }
            }
        }).start();
    }

    /**
     * Initialize the device client instance over Mqtt protocol, setting the ModelId into ClientOptions.
     * This method also sets a connection status change callback, that will get triggered any time the device's connection status changes.
     */
    private static void initializeDeviceClient() throws URISyntaxException, IOException {
        ClientOptions options = new ClientOptions();
        options.setModelId(MODEL_ID);
        deviceClient = new DeviceClient(deviceConnectionString, protocol, options);

        deviceClient.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallback() {
            @Override
            public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext) {
                log.debug("Connection status change registered: status={}, reason={}", status, statusChangeReason);

                if (throwable != null) {
                    log.debug("The connection status change was caused by the following Throwable: {}", throwable.getMessage());
                    throwable.printStackTrace();
                }
            }
        }, deviceClient);

        deviceClient.open();
    }

    /**
     * The desired property update callback, which receives the target temperature as a desired property update,
     * and updates the current temperature value over telemetry and reported property update.
     */
    private static class TargetTemperatureUpdateCallback implements TwinPropertyCallBack {

        String propertyName = "targetTemperature";

        @SneakyThrows(InterruptedException.class)
        @Override
        public void TwinPropertyCallBack(Property property, Object context) {
            if (property.getKey().equalsIgnoreCase(propertyName)) {
                double targetTemperature = ((Number)property.getValue()).doubleValue();
                log.debug("Property: Received - {\"{}\": {}°C}.", propertyName, targetTemperature);

                EmbeddedPropertyUpdate pendingUpdate = new EmbeddedPropertyUpdate(targetTemperature, StatusCode.IN_PROGRESS.value, property.getVersion(), null);
                Property reportedPropertyPending = new Property(propertyName, pendingUpdate);
                try {
                    deviceClient.sendReportedProperties(Collections.singleton(reportedPropertyPending));
                } catch (IOException e) {
                    throw new RuntimeException("IOException when sending reported property update: ", e);
                }
                log.debug("Property: Update - {\"{}\": {}°C} is {}", propertyName, targetTemperature, StatusCode.IN_PROGRESS);

                // Update temperature in 2 steps
                double step = (targetTemperature - temperature) / 2;
                for (int i = 1; i <=2; i++) {
                    temperature = BigDecimal.valueOf(temperature + step).setScale(1, RoundingMode.HALF_UP).doubleValue();
                    Thread.sleep(5 * 1000);
                }

                EmbeddedPropertyUpdate completedUpdate = new EmbeddedPropertyUpdate(temperature, StatusCode.COMPLETED.value, property.getVersion(), "Successfully updated target temperature");
                Property reportedPropertyCompleted = new Property(propertyName, completedUpdate);
                try {
                    deviceClient.sendReportedProperties(Collections.singleton(reportedPropertyCompleted));
                } catch (IOException e) {
                    throw new RuntimeException("IOException when sending reported property update: ", e);
                }
                log.debug("Property: Update - {\"{}\": {}°C} is {}", propertyName, temperature, StatusCode.COMPLETED);
            } else {
                log.debug("Property: Received an unrecognized property update from service.");
            }
        }
    }

    @AllArgsConstructor
    private static class EmbeddedPropertyUpdate {
        @NonNull
        @SerializedName("value")
        public Object value;
        @NonNull
        @SerializedName("ac")
        public Integer ackCode;
        @NonNull
        @SerializedName("av")
        public Integer ackVersion;
        @SerializedName("ad")
        public String ackDescription;
    }

    /**
     * The callback to handle "getMaxMinReport" command.
     * This method will returns the max, min and average temperature from the specified time to the current time.
     */
    private static class GetMaxMinReportMethodCallback implements DeviceMethodCallback {
        String commandName = "getMaxMinReport";
        String formatPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

        @SneakyThrows(ParseException.class)
        @Override
        public DeviceMethodData call(String methodName, Object methodData, Object context) {
            if (methodName.equalsIgnoreCase(commandName)) {

                String jsonRequest = new String((byte[]) methodData, StandardCharsets.UTF_8);
                JsonObject jsonObject = new Gson().fromJson(jsonRequest, JsonObject.class);
                String sinceString = jsonObject.get("commandRequest").getAsJsonObject().get("value").getAsString();

                DateFormat format = new SimpleDateFormat(formatPattern);
                Date since = format.parse(sinceString);
                log.debug("Command: Received - Generating min, max, avg temperature report since {}.", since);

                double runningTotal = 0;
                Map<Date, Double> filteredReadings = new HashMap<>();
                for (Map.Entry<Date, Double> entry : temperatureReadings.entrySet()) {
                    if (entry.getKey().after(since)) {
                        filteredReadings.put(entry.getKey(), entry.getValue());
                        runningTotal += entry.getValue();
                    }
                }

                if (filteredReadings.size() > 1) {
                    double maxTemp = Collections.max(filteredReadings.values());
                    double minTemp = Collections.min(filteredReadings.values());
                    double avgTemp = runningTotal / filteredReadings.size();
                    Date startTime = Collections.min(filteredReadings.keySet());
                    Date endTime = Collections.max(filteredReadings.keySet());
                    String responsePayload = String.format(
                            "{\"maxTemp\": {%f}, {\"minTemp\": {%f}, \"avgTemp\": {%f}, \"startTime\": {%tc}, \"endTime\": {%tc}",
                            maxTemp,
                            minTemp,
                            avgTemp,
                            startTime,
                            endTime);
                    log.debug("Command: MaxMinReport since {}: \"maxTemp\": {}°C, \"minTemp\": {}°C, \"avgTemp\": {}°C, \"startTime\": {}, \"endTime\": {}",
                            since,
                            maxTemp,
                            minTemp,
                            avgTemp,
                            startTime,
                            endTime);

                    return new DeviceMethodData(StatusCode.COMPLETED.value, responsePayload);
                }

                log.debug("Command: No relevant readings found since {}, cannot generate any report.", since);
                return new DeviceMethodData(StatusCode.NOT_FOUND.value, null);
            }

            log.error("Command: Unknown command {} invoked from service.", methodName);
            return new DeviceMethodData(StatusCode.NOT_FOUND.value, null);
        }
    }

    /**
     * The callback to be invoked in response to device twin operations in IoT Hub.
     */
    private static class TwinIotHubEventCallback implements IotHubEventCallback {

        @Override
        public void execute(IotHubStatusCode responseStatus, Object callbackContext) {
            log.debug("Property - Response from IoT Hub: {}", responseStatus.name());
        }
    }

    /**
     * The callback to be invoked in response to command invocation from IoT Hub.
     */
    private static class MethodIotHubEventCallback implements IotHubEventCallback {

        @Override
        public void execute(IotHubStatusCode responseStatus, Object callbackContext) {
            String commandName = (String) callbackContext;
            log.debug("Command - Response from IoT Hub: command name={}, status={}", commandName, responseStatus.name());
        }
    }

    /**
     * The callback to be invoked when a telemetry response is received from IoT Hub.
     */
    private static class MessageIotHubEventCallback implements IotHubEventCallback {

        @Override
        public void execute(IotHubStatusCode responseStatus, Object callbackContext) {
            Message msg = (Message) callbackContext;
            log.debug("Telemetry - Response from IoT Hub: message Id={}, status={}", msg.getMessageId(), responseStatus.name());
        }
    }

    private static void sendTemperatureReading() throws IOException {
        sendTemperatureTelemetry();

        double currentMaxTemp = Collections.max(temperatureReadings.values());
        if (currentMaxTemp > maxTemperature) {
            maxTemperature = currentMaxTemp;
            updateMaxTemperatureSinceLastReboot();
        }
    }

    private static void sendTemperatureTelemetry() {
        String telemetryName = "temperature";
        String telemetryPayload = String.format("{\"%s\": %f}", telemetryName, temperature);
        String messageId = java.util.UUID.randomUUID().toString();

        Message message = new Message(telemetryPayload);
        message.setContentEncoding(StandardCharsets.UTF_8.name());
        message.setContentTypeFinal("application/json");
        message.setMessageId(messageId);

        deviceClient.sendEventAsync(message, new MessageIotHubEventCallback(), message);
        log.debug("Telemetry: Sent - {\"{}\": {}°C} with message Id {}.", telemetryName, temperature, messageId);
        temperatureReadings.put(new Date(), temperature);
    }

    private static void updateMaxTemperatureSinceLastReboot() throws IOException {
        String propertyName = "maxTempSinceLastReboot";
        Property reportedProperty = new Property(propertyName, maxTemperature);

        deviceClient.sendReportedProperties(Collections.singleton(reportedProperty));
        log.debug("Property: Update - {\"{}\": {}°C} is {}.", propertyName, maxTemperature, StatusCode.COMPLETED);
    }

}
