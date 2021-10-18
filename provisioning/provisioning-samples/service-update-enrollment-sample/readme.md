# Provisioning Service Client Sample For Update Individual Enrollment

## Overview

This is a quick tutorial with the steps to update an individualEnrollment in the Microsoft Azure IoT Hub 
Device Provisioning Service using the [ProvisioningServiceClient](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.provisioningserviceclient?view=azure-java-stable)
on the java SDK.

All the artifacts that you need to execute this sample are ready to be built and executed on this sample.

## References

[Provisioning service client - documentation](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.sdk.iot.provisioning.service.provisioningserviceclient?view=azure-java-stable)
[Provisioning service client - source code](https://github.com/Azure/azure-iot-sdk-java/tree/main/provisioning/provisioning-service-client)

## How to run the samples on Linux or Windows

Note that the samples for Windows and Linux use Maven.

1. Clone the java SDK repo (https://github.com/Azure/azure-iot-sdk-java.git).
2. Navigate to the sample root `azure-iot-sdk-java/provisioning/provisioning-samples/service-update-enrollment-sample`.
3. Edit the `/src/main/java/samples/com/microsoft/azure/sdk/iot/ServiceUpdateEnrollmentSample.java` 
   to add your provisioning service information:
    1. Replace the `[Provisioning Connection String]` by the Provisioning Connection String that you copied from the 
        portal.
        ```java
        /*
         * Details of the Provisioning.
         */
        private static final String PROVISIONING_CONNECTION_STRING = "[Provisioning Connection String]";
        ```
        
    2. For **TPM** attestation:
        1. From the device that you have, you must copy the registrationId and the endorsementKey. If you don't have 
            a physical device with TPM, you can use the [tpm-simulator](https://github.com/Azure/azure-iot-sdk-java/tree/main/provisioning/provisioning-tools/tpm-simulator).
        2. Fill the `REGISTRATION_ID` and the `TPM_ENDORSEMENT_KEY` with the device information.
            ```java
            private static final String REGISTRATION_ID = "[RegistrationId]";
            private static final String TPM_ENDORSEMENT_KEY = "[TPM Endorsement Key]";
            ```

    3. For **X509** attestation:
        1. From the device that you have, you must copy the registrationId and the client certificate. If you don't have 
            a physical device with X509, you can use the [provisioning X509 cert generator](https://github.com/Azure/azure-iot-sdk-java/tree/main/provisioning/provisioning-tools/provisioning-x509-cert-generator).
            Answer `Y` to provide your common name, the Client Cert commonName is your registrationId.
        2. Fill the `REGISTRATION_ID` with the device commonName, and replace the `TPM_ENDORSEMENT_KEY` by the
            `PUBLIC_KEY_CERTIFICATE_STRING` that contains your client certificate. Be careful to do **not** change your
            certificate, _adding_ or _removing_ characters like spaces, tabs or new lines (`\n`).
            ```java
            private static final String REGISTRATION_ID = "[RegistrationId]";
            private static final String PUBLIC_KEY_CERTIFICATE_STRING =
                    "-----BEGIN CERTIFICATE-----\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
                    "-----END CERTIFICATE-----\n";
            ```
        3. Replace the Attestation mechanism in the sample, line #41, to work with X509 client certificate instead of TPM.

            **Replace:**
            ```java
            Attestation attestation = new TpmAttestation(device.getValue());
            ```

            **By:**
            ```java
            Attestation attestation = X509Attestation.createFromClientCertificates(device.getValue());
            ```
            
5. In a command line, navigate to the directory `azure-iot-sdk-java/provisioning/provisioning-samples/service-update-enrollment-sample` 
    where the `pom.xml` file for this test lives, and build your sample:
    ```
    {sample root}/>mvn install -DskipTests
    ```
6. Navigate to the folder containing the executable JAR file for the sample and run the sample as follows:

    The executable JAR file for update a IndividualEnrollment can be found at:
    ```
    {sample root}/target/service-update-enrollment-sample-{version}-with-deps.jar
    ```

    Navigate to the `target` directory that containing the jar. Run the sample using the following command:
    ```
    java -jar ./service-update-enrollment-sample-{version}-with-deps.jar
    ```
