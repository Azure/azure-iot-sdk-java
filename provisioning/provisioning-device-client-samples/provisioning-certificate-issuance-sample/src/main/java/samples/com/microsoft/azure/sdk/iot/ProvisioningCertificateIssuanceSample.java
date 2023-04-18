// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.AdditionalData;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderSymmetricKey;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderX509Cert;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.*;
import java.security.Key;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Provisioning Sample for Certificate Issuance
 */
@SuppressWarnings("CommentedOutCode") // Ignored in samples as we use these comments to show other options.
public class ProvisioningCertificateIssuanceSample
{
    private static final String idScope = "[Your ID scope here]";
    private static final String globalEndpoint = "[Your Provisioning Service Global Endpoint here]";

    // For the sake of security, you shouldn't save keys into String variables as that places them in heap memory. For the sake
    // of simplicity within this sample, though, we will save it as a string. Typically this key would be loaded as byte[] so that
    // it can be removed from stack memory.
    private static final String SYMMETRIC_KEY = "[Enter your Symmetric Key here]";

    // The registration Id to provision the device to. When creating an individual enrollment prior to running this sample, you choose this value.
    private static final String REGISTRATION_ID = "[Enter your Registration ID here]";

    // TODO -- where to actually create this directory?
    // TODO -- is this path specficied correctly?
    private static final String DPS_CLIENT_CERTIFICATE_FOLDER = ".\\DpsClientCertificates";

    private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.HTTPS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.AMQPS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.AMQPS_WS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.MQTT;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.MQTT_WS;
    private static final int MAX_TIME_TO_WAIT_FOR_REGISTRATION = 10000; // in milli seconds

    // TODO -- needed?
/*    private static final String leafPublicPem = "<Your Public Leaf Certificate Here>";
    private static final String leafPrivateKeyPem = "<Your Leaf Key Here>";*/
    //private static final Collection<String> signerCertificatePemList = new LinkedList<>();

    static class ProvisioningStatus
    {
        ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationInfoClient = new ProvisioningDeviceClientRegistrationResult();
        Exception exception;
    }

    static class ProvisioningDeviceClientRegistrationCallbackImpl implements ProvisioningDeviceClientRegistrationCallback
    {
        @Override
        public void run(ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationResult, Exception exception, Object context)
        {
            if (context instanceof ProvisioningStatus)
            {
                ProvisioningStatus status = (ProvisioningStatus) context;
                status.provisioningDeviceClientRegistrationInfoClient = provisioningDeviceClientRegistrationResult;
                status.exception = exception;
            }
            else
            {
                System.out.println("Received unknown context");
            }
        }
    }

    private static class MessageSentCallbackImpl implements MessageSentCallback
    {
        @Override
        public void onMessageSent(Message sentMessage, IotHubClientException exception, Object callbackContext)
        {
            System.out.println("Message sent!");
        }
    }

    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(SYMMETRIC_KEY.getBytes(StandardCharsets.UTF_8), REGISTRATION_ID);
        ProvisioningDeviceClient provisioningDeviceClient = null;
        DeviceClient deviceClient = null;
        try
        {
            ProvisioningStatus provisioningStatus = new ProvisioningStatus();

            File csrDirectory = new File(DPS_CLIENT_CERTIFICATE_FOLDER);
            if (csrDirectory.mkdir())
            {
                System.out.println("Certificate directory created.");
            }
            else
            {
                System.out.println("Failed to create directory for certificates.");
                throw new IOException();
            }

            // For group enrollment uncomment this line
            // TODO -- needed?
            //signerCertificatePemList.add("<Your Signer/intermediate Certificate Here>");

            // TODO -- use x509 instead of symmetric key?
/*            X509Certificate leafPublicCert = parsePublicKeyCertificate(leafPublicPem);
            Key leafPrivateKey = parsePrivateKey(leafPrivateKeyPem);
            Collection<X509Certificate> signerCertificates = new LinkedList<>();
            for (String signerCertificatePem : signerCertificatePemList)
            {
                signerCertificates.add(parsePublicKeyCertificate(signerCertificatePem));
            }

            SecurityProvider securityProviderX509 = new SecurityProviderX509Cert(leafPublicCert, leafPrivateKey, signerCertificates);
            */

            provisioningDeviceClient = ProvisioningDeviceClient.create(globalEndpoint, idScope, PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL,
                                                                       securityProviderSymmetricKey);

            GenerateCertificateSigningRequestFiles(securityProviderSymmetricKey.getRegistrationId(), csrDirectory);
            String csrFile = String.format("%s\\%s.csr", csrDirectory.getAbsolutePath(), securityProviderSymmetricKey.getRegistrationId());

            // Read certificate signing request
            Scanner sc = new Scanner(new File(csrFile));
            String certificateSigningRequest = sc.next();

            // Inform provisioning device client about the CSR
            AdditionalData additionalData = new AdditionalData();
            additionalData.setOperationalCertificateRequest(certificateSigningRequest);

            provisioningDeviceClient.registerDevice(new ProvisioningDeviceClientRegistrationCallbackImpl(), provisioningStatus, additionalData);

            while (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() != ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED)
            {
                if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ERROR ||
                        provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_DISABLED ||
                        provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_FAILED )

                {
                    provisioningStatus.exception.printStackTrace();
                    System.out.println("Registration error, bailing out");
                    break;
                }
                System.out.println("Waiting for Provisioning Service to register");
                Thread.sleep(MAX_TIME_TO_WAIT_FOR_REGISTRATION);
            }

            String issuedClientCertificate = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIssuedClientCertificate();

            if (issuedClientCertificate == null)
            {
                System.out.println("Expected client certificate was not returned by DPS, exiting sample.");
                return;
            }

            // Write issued certificate to disk
            String cerFile = String.format("%s\\%s.cer", csrDirectory.getAbsolutePath(), provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getRegistrationId());
            try
            {
                BufferedWriter writer = new BufferedWriter(new FileWriter(cerFile));
                writer.write(issuedClientCertificate);
            }
            catch (IOException ex)
            {
                System.out.println("Encountered an exception writing issued client certificate to disk: " + ex.getMessage());
                return;
            }

            System.out.println("Creatign an X509 certifiate from the issued client certificate...");
            GeneratePfxFromPublicCertificateAndPrivateKey();
            // TODO -- finish sample flow

            if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED)
            {
                System.out.println("IotHUb Uri : " + provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri());
                System.out.println("Device ID : " + provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());

                // connect to iothub
                String iotHubUri = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri();
                String deviceId = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId();
                try
                {
                    // TODO -- should this auth use the new certificate?
                    deviceClient = new DeviceClient(iotHubUri, deviceId, securityProviderSymmetricKey, IotHubClientProtocol.MQTT);
                    deviceClient.open(false);
                    Message messageToSendFromDeviceToHub =  new Message("Whatever message you would like to send");

                    System.out.println("Sending message from device to IoT Hub...");
                    deviceClient.sendEventAsync(messageToSendFromDeviceToHub, new MessageSentCallbackImpl(), null);
                }
                catch (IOException e)
                {
                    System.out.println("Device client threw an exception: " + e.getMessage());
                    if (deviceClient != null)
                    {
                        deviceClient.close();
                    }
                }
            }
        }
        catch (ProvisioningDeviceClientException | InterruptedException e)
        {
            System.out.println("Provisioning Device Client threw an exception" + e.getMessage());
            if (provisioningDeviceClient != null)
            {
                provisioningDeviceClient.close();
            }
        }

        System.out.println("Press any key to exit...");

        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
        scanner.nextLine();

        System.out.println("Shutting down...");
        if (provisioningDeviceClient != null)
        {
            provisioningDeviceClient.close();
        }
        if (deviceClient != null)
        {
            deviceClient.close();
        }
    }

    private static void GenerateCertificateSigningRequestFiles(String subject, File certificateDirectory)
    {
        // TODO -- linux file path formatting
        String path = certificateDirectory.getAbsolutePath();
        String keyfile = String.format("%s\\%s.key", path, subject);
        String csrFile = String.format("%s\\%s.csr", path, subject);

        System.out.println(String.format("Generating private key for the certificate with subject %s", subject));
        String keyGen = String.format("openssl genpkey -out \"%s\" -algorithm RSA -pkeyopt rsa_keygen_bits:2048", keyfile);

        try
        {
            Process keyGenProcess = Runtime.getRuntime().exec(keyGen);
        }
        catch (IOException ex)
        {
            System.out.println("An exception was thrown while running an openssl command: " + ex.getMessage());
        }

        System.out.println(String.format("Generating CSR for certificate with subject %s", subject));
        String csrGen = String.format("req -new -subj /CN=%s -key \"%s\" -out \"%s\"", keyfile, csrFile);

        try
        {
            Process csrGenProcess = Runtime.getRuntime().exec(csrGen);
        }
        catch (IOException ex)
        {
            System.out.println("An exception was thrown while running an openssl command: " + ex.getMessage());
        }
    }

    private static void GeneratePfxFromPublicCertificateAndPrivateKey()
    {

    }

    // TODO -- this should not return void
    private static void CreateX509CertificateFromPfxFile()
    {

    }


    // TODO -- needed?
    private static Key parsePrivateKey(String privateKeyString) throws IOException
    {
        Security.addProvider(new BouncyCastleProvider());
        PEMParser privateKeyParser = new PEMParser(new StringReader(privateKeyString));
        Object possiblePrivateKey = privateKeyParser.readObject();
        return getPrivateKey(possiblePrivateKey);
    }

    // TODO -- needed?
    private static X509Certificate parsePublicKeyCertificate(String publicKeyCertificateString) throws IOException, CertificateException
    {
        Security.addProvider(new BouncyCastleProvider());
        PemReader publicKeyCertificateReader = new PemReader(new StringReader(publicKeyCertificateString));
        PemObject possiblePublicKeyCertificate = publicKeyCertificateReader.readPemObject();
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(possiblePublicKeyCertificate.getContent()));
    }

    // TODO -- needed?
    private static Key getPrivateKey(Object possiblePrivateKey) throws IOException
    {
        if (possiblePrivateKey instanceof PEMKeyPair)
        {
            return new JcaPEMKeyConverter().getKeyPair((PEMKeyPair) possiblePrivateKey)
                .getPrivate();
        }
        else if (possiblePrivateKey instanceof PrivateKeyInfo)
        {
            return new JcaPEMKeyConverter().getPrivateKey((PrivateKeyInfo) possiblePrivateKey);
        }
        else
        {
            throw new IOException("Unable to parse private key, type unknown");
        }
    }
}
