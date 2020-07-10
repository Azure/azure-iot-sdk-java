# Plug and play device sample

These samples demonstrate how a plug and play enabled device interacts with IoT hub, to:
- Send telemetry.
- Update read-only and read-write porperties.
- Respond to command invocation. 

The samples demonstrate two scenarios:
- The device is modeled as a plug and play device, having only a root interface - [Thermostat](thermostat-device-sample)
  - This model defines root level telemetry, read-only and read-write properties and commands.
- The device is modeled as a plug and play device having multiple components - [Temperature controller](temperature-controller-device-sample).
  - This model defines root level telemetry, read-only property and commands.
  - It also defines two [Thermostat](thermostat-device-sample/src/main/resources/Thermostat.json) components, and a [device information][d-device-info] component.

### Arguments Description

* Device Connection String:
  * Device connection string format:

    ```
    HostName=your-hub.azure-devices.net;DeviceId=yourDevice;SharedAccessKey=XXXYYYZZZ=;
    ```
  * Set the following environment variables on the terminal from which you want to run the application.

    `IOTHUB_DEVICE_CONNECTION_STRING`


## Build the sample

```
$> cd {sample root}
$> mvn clean package
```

## Run the sample

To run the Thermostat sample:
```
$> mvn exec:java -Dexec.mainClass="samples.com.microsoft.azure.sdk.iot.device.Thermostat"
```

To run the Temperature Controller sample:
```
$> mvn exec:java -Dexec.mainClass="samples.com.microsoft.azure.sdk.iot.device.TemperatureController"
```

[d-device-info]: https://devicemodels.azureiotsolutions.com/models/public/dtmi:azure:DeviceManagement:DeviceInformation;1?codeView=true