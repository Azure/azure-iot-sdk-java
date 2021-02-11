# X509 Certificate Generator using DICE Emulator

## Overview

This is a quick tutorial with the steps to show you how to extract X509 certs from the DICE emulator.
All the artifacts that you need to execute this sample is ready to be built and executed on this sample.

## References

[DICE - Documentation][dice-documentation]

## How to run the X509 certificate generator on Linux or Windows

1. Clone the java SDK repo (https://github.com/Azure/azure-iot-sdk-java.git).
2. Compile the SDK. This step is only necessary if you don't want to use a precompiled Maven package.
    1. Change to the root **azure-iot-sdk-java** directory.
    2. Run `mvn install -DskipTests=true`. It will download all needed packages, compile the SDK.
    3. Navigate to the X509 Cert Generator root `azure-iot-sdk-java/provisioning/provisioning-tools/provisioning-x509-cert-generator`.

    4. In a command line, build your sample:
    ```
    {sample root}/>mvn clean install
    ```
    5. Navigate to the folder containing the executable JAR file for the sample and run the sample as follows:

    The executable JAR file for X509 Certificate generation using DICE emulator can be found at:
    ```
    {sample root}/target/provisioning-x509-cert-generator-{version}-with-deps.jar
    ```

    Navigate to the `target` directory that contains the jar. Run the sample using the following command:
    ```
    java -jar ./provisioning-x509-cert-generator-{version}-with-deps.jar
    ```
    6.  Output at the command line is as follows :
    ```
    Do you want to input common name : Y/N(use default)
    n
    Your registration Id is : <Value of registration Id>
    Client Cert
    -----BEGIN CERTIFICATE-----
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX==
    -----END CERTIFICATE-----

    Client Cert Private Key
    -----BEGIN PRIVATE KEY-----
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    -----END PRIVATE KEY-----

    Root Cert
    -----BEGIN CERTIFICATE-----
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX==
    -----END CERTIFICATE-----

    Do you want to input Verification Code Y/N
    ```

Following the instructions from the command line prompts you can retrieve Registration Id,
Client Certificate used for Individual Enrollments, Root certificate used for Group Enrollments.

Please save the PEM file locally as you will need them in running Registration using X509 Sample located [here][povisioning-x509-sample]

## Using your own common name for the certificates

If you wish to use your own common name for the certificates you can provide it at the prompt. Please keep a note of it as it will be required during enrollment.

## Enrolling using the Certificates
Enrollment can take place either via [Azure Portal][azure-portal] or [Provisioning Service Client][povisioning-service-client].
For creating Individual Enrollment use Client Certificate from above.
For creating Group Enrollment use Root Certificate from above.
If you create Group Enrollment then you can verify your certificate on the portal by answering Y to the prompt (As continuation of step 6 above) as below :
```

Do you want to input Verification Code Y/N

y
Input Verification Code
<Your Verification Code from the Portal>
Verification Cert
-----BEGIN CERTIFICATE-----
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX==
-----END CERTIFICATE-----

```
Upload the certificate from above to verify.

[povisioning-x509-sample]: https://github.com/Azure/azure-iot-sdk-java/tree/master/provisioning/provisioning-samples/provisioning-x509-sample
[povisioning-service-client]: https://github.com/Azure/azure-iot-sdk-java/tree/master/provisioning/provisioning-service-client
[azure-portal]: https://www.portal.azure.com
[dice-documentation]:https://www.microsoft.com/en-us/research/publication/device-identity-dice-riot-keys-certificates/






