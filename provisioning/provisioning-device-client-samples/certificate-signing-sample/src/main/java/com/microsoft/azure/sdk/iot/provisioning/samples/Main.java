package com.microsoft.azure.sdk.iot.provisioning.samples;

import com.microsoft.azure.sdk.iot.device.*;
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
import java.util.Observable;
import java.util.concurrent.Future;

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

        CertificateSigningRequestGenerator csrGenerator =
                //new CertificateSigningRequest("RSA", registrationId);
                new CertificateSigningRequestGenerator("ECDSA", registrationId);

        CertificateSigningRequest dpsCsr = csrGenerator.GenerateNewCertificateSigningRequest();

        String privateKeyPem = getPrivateKeyString(dpsCsr.getPrivateKey());
        WriteToFile(savedCertificatesPath, "privateKey.pem", privateKeyPem);

        SecurityProvider securityProvider = CreateSecurityProviderX509();
        //SecurityProvider securityProvider = CreateSecurityProviderSymmetricKey();

        ProvisioningDeviceClient provisioningDeviceClient = ProvisioningDeviceClient.create(
                "global.azure-devices-provisioning.net",
                idScope,
                dpsProtocol,
                securityProvider);

        AdditionalData provisioningAdditionalData = new AdditionalData();
        provisioningAdditionalData.setClientCertificateSigningRequest(dpsCsr.getBase64EncodedPKCS10());
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

        String deviceId = provisioningResult.getDeviceId();
        String iotHubUri = provisioningResult.getIothubUri();

        ClientOptions clientOptions = ClientOptions.builder().sslContext(deviceClientSslContext).build();
        String derivedConnectionString = String.format("HostName=%s;DeviceId=%s;x509=true", iotHubUri, deviceId);
        DeviceClient client = new DeviceClient(derivedConnectionString, iotHubProtocol, clientOptions);

        CertificateSigningRequest renewalCsr = csrGenerator.GenerateNewCertificateSigningRequest();

        IotHubCertificateSigningRequest iothubCsr =
                new IotHubCertificateSigningRequest(deviceId, renewalCsr.getBase64EncodedPKCS10(), "*");

        Future<>
        client.sendCertificateSigningRequest(iothubCsr, new IotHubCertificateSigningResponseCallback()
        {
            @Override
            public void onCertificateSigningRequestAccepted(IotHubCertificateSigningRequestAccepted accepted)
            {

            }

            @Override
            public void onCertificateSigningComplete(IotHubCertificateSigningResponse response)
            {

            }

            @Override
            public void onCertificateSigningError(IotHubCertificateSigningError error)
            {

            }
        });
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