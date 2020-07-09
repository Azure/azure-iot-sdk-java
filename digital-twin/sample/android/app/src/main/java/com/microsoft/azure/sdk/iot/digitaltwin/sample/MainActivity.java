package com.microsoft.azure.sdk.iot.digitaltwin.sample;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinCallback;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinClientResult;
import com.microsoft.azure.sdk.iot.digitaltwin.device.DigitalTwinDeviceClient;

import lombok.extern.slf4j.Slf4j;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static java.util.Arrays.asList;

@Slf4j
public class MainActivity extends AppCompatActivity implements UiHandler {

    private static final String CONNECTION_STRING = "[YOUR_DIGITAL_TWIN_DEVICE_CONNECTION_STRING]";
    private static final String DCM_ID = "urn:azureiot:samplemodel:1";
    private static final String ENVIRONMENTAL_SENSOR_INTERFACE_INSTANCE_NAME = "sensor";
    private TextView nameView;
    private TextView brightnessView;
    private TextView temperatureView;
    private TextView humidityView;
    private TextView connectivityView;
    private TextView registrationView;
    private TextView onoffView;
    private ObjectAnimator anim;
    private boolean blinking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        nameView = (TextView) findViewById(R.id.name);
        brightnessView = (TextView) findViewById(R.id.brightness);
        temperatureView = (TextView) findViewById(R.id.temperature);
        humidityView = (TextView) findViewById(R.id.humidity);
        connectivityView = (TextView) findViewById(R.id.connectivity);
        registrationView = (TextView) findViewById(R.id.registration);
        onoffView = (TextView) findViewById(R.id.onoff);
        anim = ObjectAnimator.ofInt(findViewById(R.id.blink), "backgroundColor", Color.WHITE, Color.RED, Color.WHITE);

        try {
            DeviceClient deviceClient = new DeviceClient(CONNECTION_STRING, MQTT);
            deviceClient.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallback() {
                @Override
                public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext) {
                    log.debug("Device client status changed to: {}, reason: {}, cause: {}", status, statusChangeReason, throwable);
                    updateConnectivity(status);
                }
            }, deviceClient);
            DigitalTwinDeviceClient digitalTwinDeviceClient = new DigitalTwinDeviceClient(deviceClient);
            final EnvironmentalSensor environmentalSensor = new EnvironmentalSensor(ENVIRONMENTAL_SENSOR_INTERFACE_INSTANCE_NAME, this);
            final DeviceInformation deviceInformation = DeviceInformation.builder()
                    .manufacturer("Microsoft")
                    .model("1.0.0")
                    .osName(System.getProperty("os.nameView"))
                    .processorArchitecture(System.getProperty("os.arch"))
                    .processorManufacturer("Intel(R) Core(TM)")
                    .softwareVersion("JDK" + System.getProperty("java.version"))
                    .totalMemory(16e9)
                    .totalStorage(1e12)
                    .build();
            registrationView.setText("Registering...");
            digitalTwinDeviceClient.registerInterfacesAsync(
                    DCM_ID,
                    asList(deviceInformation, environmentalSensor),
                    new DigitalTwinCallback() {
                        @Override
                        public void onResult(DigitalTwinClientResult digitalTwinClientResult, Object context) {
                            log.debug("Register interfaces {}.", digitalTwinClientResult);
                            if (digitalTwinClientResult == DigitalTwinClientResult.DIGITALTWIN_CLIENT_OK) {
                                updateRegistrationStatus("Registered");
                            } else {
                                updateRegistrationStatus("Register Failed");
                            }
                        }
                    },
                    digitalTwinDeviceClient
            );
        } catch (Exception e) {
            log.error("Unable to start DigitalTwinDeviceClient", e);
        }
    }

    public void updateName(final String name) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                nameView.setText(name);
            }
        });
    }

    public void updateBrightness(final double brightness) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                brightnessView.setText(String.valueOf(brightness));
            }
        });
    }

    public void updateTemperature(final double temperature) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                temperatureView.setText(String.valueOf(temperature));
            }
        });
    }

    public void updateHumidity(final double humidity) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                humidityView.setText(String.valueOf(humidity));
            }
        });
    }

    private void updateConnectivity(final IotHubConnectionStatus connectionStatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectivityView.setText(connectionStatus.toString());
            }
        });
    }

    private void updateRegistrationStatus(final String registrationStatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                registrationView.setText(registrationStatus);
            }
        });
    }

    public void updateOnoff(final boolean on) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onoffView.setText(on ? "ON" : "OFF");
            }
        });
    }

    public void startBlink(final long interval) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (interval < 0) {
                    return;
                }
                anim.setDuration(interval);
                if (!blinking) {
                    blinking = true;
                    anim.setEvaluator(new ArgbEvaluator());
                    anim.setRepeatMode(ValueAnimator.REVERSE);
                    anim.setRepeatCount(Animation.INFINITE);
                    anim.start();
                }
            }
        });
    }

}
