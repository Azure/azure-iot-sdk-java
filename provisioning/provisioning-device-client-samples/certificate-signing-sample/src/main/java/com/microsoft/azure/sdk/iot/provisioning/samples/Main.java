package com.microsoft.azure.sdk.iot.provisioning.samples;

import com.microsoft.azure.sdk.iot.device.ClientOptions;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderSymmetricKey;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderX509;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderX509Cert;

import javax.net.ssl.SSLContext;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.List;

public class Main
{
    private static String idScope = "<Your DPS instance's id scope>";
    private static String registrationId = "<>";
    private static String savedCertificatesPath = "<>";

    public static void main(String[] args)
            throws IOException, URISyntaxException, InterruptedException, IotHubClientException, GeneralSecurityException, ProvisioningDeviceClientException, ProvisioningDeviceClientException
    {
        // Certificate signing feature is currently only supported over MQTT/MQTT_WS
        IotHubClientProtocol iotHubProtocol = IotHubClientProtocol.MQTT;
        //IotHubClientProtocol iotHubProtocol = IotHubClientProtocol.MQTT_WS;

        ProvisioningDeviceClientTransportProtocol dpsProtocol = ProvisioningDeviceClientTransportProtocol.MQTT;
        //ProvisioningDeviceClientTransportProtocol dpsProtocol = ProvisioningDeviceClientTransportProtocol.MQTT_WS;

        CertificateSigningRequest certificateSigningRequest =
                //new CertificateSigningRequest("RSA", registrationId);
                new CertificateSigningRequest("ECDSA", registrationId);

        String privateKeyPem = getPrivateKeyString(certificateSigningRequest.privateKey);
        WriteToFile(savedCertificatesPath, "privateKey.pem", privateKeyPem);

        SecurityProvider securityProvider = CreateSecurityProviderX509();
        //SecurityProvider securityProvider = CreateSecurityProviderSymmetricKey();

        ProvisioningDeviceClient provisioningDeviceClient = ProvisioningDeviceClient.create(
                "global.azure-devices-provisioning.net",
                idScope,
                dpsProtocol,
                securityProvider);

        AdditionalData provisioningAdditionalData = new AdditionalData();
        provisioningAdditionalData.setClientCertificateSigningRequest(certificateSigningRequest.base64EncodedPKCS10);
        ProvisioningDeviceClientRegistrationResult provisioningResult = provisioningDeviceClient.registerDeviceSync(provisioningAdditionalData);

        if (provisioningResult.getProvisioningDeviceClientStatus() != ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED)
        {
            System.out.println("Provisioning failed with status: " + provisioningResult.getProvisioningDeviceClientStatus());
            return;
        }

        if (provisioningResult.getIssuedClientCertificateChain() == null
                || provisioningResult.getIssuedClientCertificateChain().isEmpty())
        {
            System.out.println("Provisioning did not yield any issued client certificates. Did you include the certificate signing request in the provisioning request?");
            return;
        }

        String issuedClientCertificatesPem = ConvertToPem(provisioningResult.getIssuedClientCertificateChain());
        WriteToFile(savedCertificatesPath, "clientCertificates.pem", issuedClientCertificatesPem);

        String leafCertificatePem = ConvertToPem(provisioningResult.getIssuedClientCertificateChain().get(0));

        SSLContext deviceClientSslContext = SSLContextBuilder.buildSSLContext(leafCertificatePem, privateKeyPem);

        ClientOptions clientOptions = ClientOptions.builder().sslContext(deviceClientSslContext).build();
        String derivedConnectionString = String.format("HostName=%s;DeviceId=%;x509=true", provisioningResult.getIothubUri(), provisioningResult.getDeviceId());
        DeviceClient client = new DeviceClient(derivedConnectionString, iotHubProtocol, clientOptions);

    }

    private static SecurityProviderX509 CreateSecurityProviderX509()
    {
        return new SecurityProviderX509Cert(todo);
    }

    private static SecurityProviderSymmetricKey CreateSecurityProviderSymmetricKey()
    {
        return new SecurityProviderSymmetricKey(todo);
    }

    private static String getPrivateKeyString(PrivateKey privateKey) throws IOException
    {
        StringBuilder privateKeyStringBuilder = new StringBuilder();
        privateKeyStringBuilder.append("-----BEGIN PRIVATE KEY-----");
        String privateKeyBase64Encoded = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        privateKeyStringBuilder.append(privateKeyBase64Encoded);
        privateKeyStringBuilder.append("-----END PRIVATE KEY-----");
        return privateKeyStringBuilder.toString();
    }

    private static String ConvertToPem(List<String> issuedClientCertificates)
    {
        StringBuilder pemBuilder = new StringBuilder();
        for (String issuedClientCertificate : issuedClientCertificates)
        {
            pemBuilder.append("-----BEGIN CERTIFICATE-----");
            pemBuilder.append(issuedClientCertificate);
            pemBuilder.append("-----END CERTIFICATE-----");
        }

        return pemBuilder.toString();
    }

    private static String ConvertToPem(String issuedLeafCertificate)
    {
        StringBuilder pemBuilder = new StringBuilder();
        pemBuilder.append("-----BEGIN CERTIFICATE-----");
        pemBuilder.append(issuedLeafCertificate);
        pemBuilder.append("-----END CERTIFICATE-----");

        return pemBuilder.toString();
    }

    private static void WriteToFile(String path, String filename, String contents) throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + filename));
        writer.write(contents);
        writer.close();
    }
}