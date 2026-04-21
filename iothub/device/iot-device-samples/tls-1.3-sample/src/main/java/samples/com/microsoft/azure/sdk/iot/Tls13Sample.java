package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.ClientOptions;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Tls13Sample
{
    // This connection string should look like <hub name>.device.azure-devices.<dnsSuffix>
    // Old connection strings that look like <hub name>.azure-devices.<dnsSuffix> won't support TLS 1.3
    private static final String connectionString = System.getenv("IOTHUB_DEVICE_CONNECTION_STRING");

    public static void main(String[] args)
            throws IOException, URISyntaxException, InterruptedException, IotHubClientException, NoSuchAlgorithmException, KeyManagementException
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");

        IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        SSLContext sslContextWithTls13Support = SSLContext.getInstance("TLSv1.3");

        // Initializing the SSLContext with null keyManagers and null trustManagers makes it so the device's default
        // trusted certificates are loaded, and no private keys are loaded. This is fine for devices that use symmetric key
        // authentication, but won't work for devices that use x509 authentication. See the SSLContextBuilder class in one
        // of the other samples for constructing this SSL context object with support for x509 devices.
        sslContextWithTls13Support.init(null, null, new SecureRandom());

        ClientOptions deviceClientOptions = ClientOptions.builder()
                .sslContext(sslContextWithTls13Support)
                .build();

        DeviceClient deviceClient = new DeviceClient(connectionString, protocol, deviceClientOptions);

        deviceClient.open(true);

        System.out.println("Opened connection to IoT Hub.");

        // HTTP clients need to actually send a message to establish a TLS connection
        deviceClient.sendEvent(new Message("Hello from the TLS 1.3 sample!"));

        System.out.println("Closing the client...");
        deviceClient.close();
    }
}