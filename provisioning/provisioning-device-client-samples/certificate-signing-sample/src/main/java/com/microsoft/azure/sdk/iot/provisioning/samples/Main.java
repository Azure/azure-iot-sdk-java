package com.microsoft.azure.sdk.iot.provisioning.samples;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.certificatesigning.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderSymmetricKey;

import javax.net.ssl.SSLContext;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main
{
    // The path to write all created certificates to
    private static final String SAMPLE_CERTIFICATES_OUTPUT_PATH = "~/SampleCertificates";
    private static final String DPS_ID_SCOPE = "<>";
    private static final String DPS_REGISTRATION_ID = "<>";
    private static final String DPS_SYMMETRIC_KEY = "<>";

    public static void main(String[] args)
            throws IOException, URISyntaxException, InterruptedException, IotHubClientException, GeneralSecurityException, ProvisioningDeviceClientException
    {
        // Certificate signing feature is currently only supported over MQTT/MQTT_WS
        IotHubClientProtocol iotHubProtocol = IotHubClientProtocol.MQTT;
        //IotHubClientProtocol iotHubProtocol = IotHubClientProtocol.MQTT_WS;

        ProvisioningDeviceClientTransportProtocol dpsProtocol = ProvisioningDeviceClientTransportProtocol.MQTT;
        //ProvisioningDeviceClientTransportProtocol dpsProtocol = ProvisioningDeviceClientTransportProtocol.MQTT_WS;

        CertificateType certificateType = CertificateType.ECC;
        //CertificateType certificateType = CertificateType.RSA;

        CertificateSigningRequestGenerator csrGenerator =
                //new CertificateSigningRequest(CertificateType.RSA, PROVISIONED_DEVICE_ID);
                new CertificateSigningRequestGenerator(certificateType, PROVISIONED_DEVICE_ID);

        CertificateSigningRequest dpsCsr = csrGenerator.GenerateNewCertificateSigningRequest();

        String privateKeyPem = getPrivateKeyString(dpsCsr.getPrivateKey(), certificateType);
        WriteToFile(SAMPLE_CERTIFICATES_OUTPUT_PATH, "privateKey.pem", privateKeyPem);

        SecurityProvider securityProvider = CreateSecurityProvider();

        ProvisioningDeviceClient provisioningDeviceClient = ProvisioningDeviceClient.create(
                "global.azure-devices-provisioning.net",
                DPS_ID_SCOPE,
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
        WriteToFile(SAMPLE_CERTIFICATES_OUTPUT_PATH, "clientCertificates.pem", issuedClientCertificatesPem);

        String leafCertificatePem = ConvertToPem(provisioningResult.getIssuedClientCertificateChain().get(0));

        SSLContext deviceClientSslContext = SSLContextBuilder.buildSSLContext(leafCertificatePem, privateKeyPem);

        provisioningDeviceClient.close();
        System.out.println("Provisioning finished successfully. Opening device client connection with the newly signed certificates.");

        String deviceId = provisioningResult.getDeviceId();
        String iotHubUri = provisioningResult.getIothubUri();

        ClientOptions clientOptions = ClientOptions.builder().sslContext(deviceClientSslContext).build();
        String derivedConnectionString = String.format("HostName=%s;DeviceId=%s;x509=true", iotHubUri, deviceId);
        DeviceClient client = new DeviceClient(derivedConnectionString, iotHubProtocol, clientOptions);

        //TODO there is some delay between provisioning result and IoT hub accepting those credentials? This sometimes
        // hits an unauthorized error
        client.open(true);

        client.sendEvent(new Message("Hello from the CSR sample!"));


        System.out.println("Creating new CSR to send to IoT hub.");
        CertificateSigningRequest renewalCsr = csrGenerator.GenerateNewCertificateSigningRequest();

        IotHubCertificateSigningRequest iothubCsr =
                new IotHubCertificateSigningRequest(deviceId, renewalCsr.getBase64EncodedPKCS10(), "*");

        System.out.println("Sending new CSR to IoT hub.");
        IotHubCertificateSigningResponseFutures csrResponseFutures = client.sendCertificateSigningRequest(iothubCsr);

        IotHubCertificateSigningResponse response;
        IotHubCertificateSigningRequestAccepted accepted;

        try
        {
            accepted = csrResponseFutures.getOnCertificateSigningRequestAccepted().get();
            System.out.println("The certificate signing request was accepted by Iot Hub. Operation will expire at: " + accepted.getOperationExpires());
            response = csrResponseFutures.getOnCertificateSigningCompleted().get();
            System.out.println("Iot Hub completed the certificate signing request.");
        }
        catch (ExecutionException e)
        {
            //TODO I believe this should always be the case, but double check this
            IotHubCertificateSigningException ex = (IotHubCertificateSigningException) e.getCause();
            System.out.println("Encountered an issue while renewing the certificates: " + ex.getMessage());
            return;
        }

        String renewedClientCertificatesPem = ConvertToPem(response.getCertificates());
        WriteToFile(SAMPLE_CERTIFICATES_OUTPUT_PATH, "clientCertificates.pem", renewedClientCertificatesPem);

        String renewedLeafCertificatePem = ConvertToPem(provisioningResult.getIssuedClientCertificateChain().get(0));

        client.close();

        SSLContext renewedDeviceClientSslContext = SSLContextBuilder.buildSSLContext(renewedLeafCertificatePem, privateKeyPem);
        ClientOptions renewedClientOptions = ClientOptions.builder().sslContext(renewedDeviceClientSslContext).build();
        client = new DeviceClient(derivedConnectionString, iotHubProtocol, renewedClientOptions);

        client.open(true);

        client.sendEvent(new Message("Hello from the CSR sample!"));

        client.close();

        System.out.println("Done.");
    }

    // This sample can use any combination of individual enrollment vs enrollment group and TPM vs Symmetric Key vs x509 auth.
    // For simpicity in demonstrating the CSR feature, though, this sample will use Symmetric Key + individual enrollment.
    private static SecurityProviderSymmetricKey CreateSecurityProvider() throws NoSuchAlgorithmException, InvalidKeyException
    {
        byte[] derivedSymmetricKey =
                SecurityProviderSymmetricKey
                        .ComputeDerivedSymmetricKey(
                                ENROLLMENT_GROUP_SYMMETRIC_KEY.getBytes(StandardCharsets.UTF_8),
                                PROVISIONED_DEVICE_ID);

        return new SecurityProviderSymmetricKey(derivedSymmetricKey, PROVISIONED_DEVICE_ID);
    }

    private static String getPrivateKeyString(PrivateKey privateKey, CertificateType certificateType) throws IOException
    {
        StringBuilder privateKeyStringBuilder = new StringBuilder();
        if (certificateType == CertificateType.RSA)
        {
            privateKeyStringBuilder.append("-----BEGIN PRIVATE KEY-----\r\n");
        }
        else if (certificateType == CertificateType.ECC)
        {
            privateKeyStringBuilder.append("-----BEGIN EC PRIVATE KEY-----\r\n");
        }
        String privateKeyBase64Encoded = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        privateKeyStringBuilder.append(privateKeyBase64Encoded);
        privateKeyStringBuilder.append("\r\n");
        if (certificateType == CertificateType.RSA)
        {
            privateKeyStringBuilder.append("-----END PRIVATE KEY-----\r\n");
        }
        else if (certificateType == CertificateType.ECC)
        {
            privateKeyStringBuilder.append("-----END EC PRIVATE KEY-----\r\n");
        }
        return privateKeyStringBuilder.toString();
    }

    private static String ConvertToPem(List<String> issuedClientCertificates)
    {
        StringBuilder pemBuilder = new StringBuilder();
        for (String issuedClientCertificate : issuedClientCertificates)
        {
            pemBuilder.append("-----BEGIN CERTIFICATE-----\r\n");
            pemBuilder.append(issuedClientCertificate);
            pemBuilder.append("\r\n");
            pemBuilder.append("-----END CERTIFICATE-----\r\n");
        }

        return pemBuilder.toString();
    }

    private static String ConvertToPem(String issuedLeafCertificate)
    {
        StringBuilder pemBuilder = new StringBuilder();
        pemBuilder.append("-----BEGIN CERTIFICATE-----\r\n");
        pemBuilder.append(issuedLeafCertificate);
        pemBuilder.append("\r\n");
        pemBuilder.append("-----END CERTIFICATE-----\r\n");

        return pemBuilder.toString();
    }

    private static void WriteToFile(String path, String filename, String contents) throws IOException
    {
        Files.deleteIfExists(Path.of(path, filename));
        BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + filename));
        writer.write(contents);
        writer.close();
    }
}