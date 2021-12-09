// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningConnectionString;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.provisioning.service.contract.ContractApiHttp;
import com.microsoft.azure.sdk.iot.provisioning.service.contract.SDKUtils;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientExceptionManager;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientNotFoundException;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientTransportException;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Objects;

/**
 * Device Provisioning Service Client.
 *
 * <p> The IoT Hub Device Provisioning Service is a helper service for IoT Hub that enables automatic device
 *     provisioning to a specified IoT hub without requiring human intervention. You can use the Device Provisioning
 *     Service to provision millions of devices in a secure and scalable manner.
 *
 * <p> This java SDK provides an API to help developers to create and maintain Enrollments on the IoT Hub Device
 *     Provisioning Service, it translate the rest API in java Objects and Methods.
 *
 * <p> To use the this SDK, you must include the follow package on your application.
 * <pre>
 * {@code
 * // Include the following imports to use the Device Provisioning Service APIs.
 * import com.microsoft.azure.sdk.iot.provisioning.service.*;
 * }
 * </pre>
 *
 * <p> The main APIs are exposed by the {@link ProvisioningServiceClient}, it contains the public Methods that the
 *     application shall call to create and maintain the Enrollments. The Objects in the <b>configs</b> package shall
 *     be filled and passed as parameters of the public API, for example, to create a new enrollment, the application
 *     shall create the object {@link IndividualEnrollment} with the appropriate enrollment configurations, and call the
 *     {@link #createOrUpdateIndividualEnrollment(IndividualEnrollment)}.
 *
 * <p> The IoT Hub Device Provisioning Service supports SQL queries too. The application can create a new query using
 *     one of the queries factories, for instance {@link #createIndividualEnrollmentQuery(QuerySpecification)}, passing
 *     the {@link QuerySpecification}, with the SQL query. This factory returns a {@link Query} object, which is an
 *     active iterator.
 *
 * <p> This java SDK can be represented in the follow diagram, the first layer are the public APIs the your application
 *     shall use:
 *
 * <pre>
 * {@code
 * +===============+       +==========================================+                           +============+   +===+
 * |    configs    |------>|         ProvisioningServiceClient        |                        +->|    Query   |   |   |
 * +===============+       +==+=================+==================+==+                        |  +======+=====+   | e |
 *                           /                  |                   \                          |         |         | x |
 *                          /                   |                    \                         |         |         | c |
 * +-----------------------+-----+  +-----------+------------+  +-----+---------------------+  |         |         | e |
 * | IndividualEnrollmentManager |  | EnrollmentGroupManager |  | RegistrationStatusManager |  |         |         | p |
 * +---------------+------+------+  +-----------+------+-----+  +-------------+-------+-----+  |         |         | t |
 *                  \      \                    |       \                     |        \       |         |         | i |
 *                   \      +----------------------------+------------------------------+------+         |         | o |
 *                    \                         |                             |                          |         | n |
 *  +--------+      +--+------------------------+-----------------------------+--------------------------+-----+   | s |
 *  |  auth  |----->|                                     ContractApiHttp                                      |   |   |
 *  +--------+      +-------------------------------------------+----------------------------------------------+   +===+
 *                                                              |
 *                                                              |
 *                        +-------------------------------------+------------------------------------------+
 *                        |                 com.microsoft.azure.sdk.iot.deps.transport.http                |
 *                        +--------------------------------------------------------------------------------+
 * }
 * </pre>
 *
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-dps">Azure IoT Hub Device Provisioning Service</a>
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-dps/about-iot-dps">Provisioning devices with Azure IoT Hub Device Provisioning Service</a>
 */
@Slf4j
public final class ProvisioningServiceClient
{

    private final IndividualEnrollmentManager individualEnrollmentManager;
    private final EnrollmentGroupManager enrollmentGroupManager;
    private final RegistrationStatusManager registrationStatusManager;

    /**
     * Create a new instance of the {@code DeviceProvisioningServiceClient} that exposes
     * the API to the Device Provisioning Service.
     *
     * <p> The Device Provisioning Service Client is created based on a <b>Provisioning Connection String</b>.
     * <p> Once you create a Device Provisioning Service on Azure, you can get the connection string on the Azure portal.
     *
     * @see <a href="http://portal.azure.com/">Azure portal</a>
     *
     * @param connectionString the {@code String} that cares the connection string of the Device Provisioning Service.
     * @return The {@code ProvisioningServiceClient} with the new instance of this object.
     * @throws IllegalArgumentException if the connectionString is {@code null} or empty.
     */
    public static ProvisioningServiceClient createFromConnectionString(String connectionString)
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_001: [The createFromConnectionString shall create a new instance of this class using the provided connectionString.] */
        return new ProvisioningServiceClient(connectionString);
    }

    /**
     * Create a {@link ProvisioningServiceClient} instance with a custom {@link TokenCredential} to allow for finer grain control
     * of authentication tokens used in the underlying connection.
     *
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     */
    public ProvisioningServiceClient(TokenCredential credential)
    {
        Objects.requireNonNull(credential, "credential cannot be null");

        ContractApiHttp contractApiHttp = new ContractApiHttp(credential);

        /* SRS_PROVISIONING_SERVICE_CLIENT_21_005: [The constructor shall create a new instance of the IndividualEnrollmentManger.] */
        this.individualEnrollmentManager = IndividualEnrollmentManager.createFromContractApiHttp(contractApiHttp);
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_006: [The constructor shall create a new instance of the EnrollmentGroupManager.] */
        this.enrollmentGroupManager = EnrollmentGroupManager.createFromContractApiHttp(contractApiHttp);
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_007: [The constructor shall create a new instance of the RegistrationStatusManager.] */
        this.registrationStatusManager = RegistrationStatusManager.createFromContractApiHttp(contractApiHttp);
    }

    /**
     * Create a {@link ProvisioningServiceClient} instance with the specifed {@link AzureSasCredential}.
     *
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     */
    public ProvisioningServiceClient(AzureSasCredential azureSasCredential)
    {
        Objects.requireNonNull(azureSasCredential, "credential cannot be null");

        ContractApiHttp contractApiHttp = new ContractApiHttp(azureSasCredential);

        /* SRS_PROVISIONING_SERVICE_CLIENT_21_005: [The constructor shall create a new instance of the IndividualEnrollmentManger.] */
        this.individualEnrollmentManager = IndividualEnrollmentManager.createFromContractApiHttp(contractApiHttp);
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_006: [The constructor shall create a new instance of the EnrollmentGroupManager.] */
        this.enrollmentGroupManager = EnrollmentGroupManager.createFromContractApiHttp(contractApiHttp);
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_007: [The constructor shall create a new instance of the RegistrationStatusManager.] */
        this.registrationStatusManager = RegistrationStatusManager.createFromContractApiHttp(contractApiHttp);
    }

    /**
     * PRIVATE CONSTRUCTOR
     *
     * @param connectionString the {@code String} that contains the connection string for the Provisioning service.
     * @throws IllegalArgumentException if the connectionString is {@code null}, empty, or invalid.
     */
    private ProvisioningServiceClient(String connectionString)
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_002: [The constructor shall throw IllegalArgumentException if the provided connectionString is null or empty.] */
        if(Tools.isNullOrEmpty(connectionString))
        {
            throw new IllegalArgumentException("connectionString cannot be null or empty");
        }

        /* SRS_PROVISIONING_SERVICE_CLIENT_21_003: [The constructor shall throw IllegalArgumentException if the ProvisioningConnectionString or one of the inner Managers failed to create a new instance.] */
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_004: [The constructor shall create a new instance of the ContractApiHttp class using the provided connectionString.] */
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);
        ContractApiHttp contractApiHttp = ContractApiHttp.createFromConnectionString(provisioningConnectionString);

        /* SRS_PROVISIONING_SERVICE_CLIENT_21_005: [The constructor shall create a new instance of the IndividualEnrollmentManger.] */
        this.individualEnrollmentManager = IndividualEnrollmentManager.createFromContractApiHttp(contractApiHttp);
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_006: [The constructor shall create a new instance of the EnrollmentGroupManager.] */
        this.enrollmentGroupManager = EnrollmentGroupManager.createFromContractApiHttp(contractApiHttp);
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_007: [The constructor shall create a new instance of the RegistrationStatusManager.] */
        this.registrationStatusManager = RegistrationStatusManager.createFromContractApiHttp(contractApiHttp);

        log.debug("Initialized a ProvisioningServiceClient instance using SDK version {}", SDKUtils.getServiceApiVersion());
    }

    /**
     * Create or update a individual Device Enrollment record.
     *
     * <p> This API creates a new individualEnrollment or update a existed one. All enrollments in the Device Provisioning Service
     *     contains a unique identifier called registrationId. If this API is called for an individualEnrollment with a
     *     registrationId that already exists, it will replace the existed individualEnrollment information by the new one.
     *     On the other hand, if the registrationId does not exit, this API will create a new individualEnrollment.
     *
     * <p> To use the Device Provisioning Service API, you must include the follow package on your application.
     * <pre>
     * {@code
     * // Include the following imports to use the Device Provisioning Service APIs.
     * import com.microsoft.azure.sdk.iot.provisioning.service.*;
     * }
     * </pre>
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will create a new individualEnrollment that will provisioning the ContosoDevice1000 to the
     *     ContosoHub.azure-devices.net using TPM attestation.
     * <pre>
     * {@code
     * // IndividualEnrollment information.
     * private static final String PROVISIONING_CONNECTION_STRING = "HostName=ContosoProvisioning.azure-devices-provisioning.net;" +
     *                                                              "SharedAccessKeyName=contosoprovisioningserviceowner;" +
     *                                                              "SharedAccessKey=0000000000000000000000000000000000000000000=";
     * private static final String TPM_ENDORSEMENT_KEY = "tpm-endorsement-key";
     * private static final String REGISTRATION_ID = "registrationId-1";
     * private static final String DEVICE_ID = "ContosoDevice1000";
     * private static final String IOTHUB_HOST_NAME = "ContosoHub.azure-devices.net";
     *
     * public static void main(String[] args) throws IOException, URISyntaxException, ProvisioningServiceClientException
     * {
     *     // Create a Device Provisioning Service Client.
     *     DeviceProvisioningServiceClient deviceProvisioningServiceClient =
     *         DeviceProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
     *
     *     // Create a new individualEnrollment configurations.
     *     Attestation attestation = new TpmAttestation(TPM_ENDORSEMENT_KEY);
     *     IndividualEnrollment individualEnrollment =
     *        new IndividualEnrollment(
     *             REGISTRATION_ID,
     *             attestation);
     *     individualEnrollment.setDeviceId(DEVICE_ID);
     *     individualEnrollment.setIotHubHostName(IOTHUB_HOST_NAME);
     *     individualEnrollment.setProvisioningStatus(ProvisioningStatus.DISABLED);
     *
     *     // Create a new individualEnrollment.
     *     IndividualEnrollment enrollmentResult =  deviceProvisioningServiceClient.createOrUpdateIndividualEnrollment(individualEnrollment);
     * }
     * }
     * </pre>
     *
     * <p> If the registrationId already exists, this method will update existed enrollments. Note that update the
     *     individualEnrollment will not change the status of the device that was already registered using the old individualEnrollment.
     *
     * <p> The follow code will update the provisioningStatus of the previous individualEnrollment from <b>disabled</b> to <b>enabled</b>.
     * <pre>
     * {@code
     * // IndividualEnrollment information.
     * private static final String PROVISIONING_CONNECTION_STRING = "HostName=ContosoProvisioning.azure-devices-provisioning.net;" +
     *                                                              "SharedAccessKeyName=contosoprovisioningserviceowner;" +
     *                                                              "SharedAccessKey=0000000000000000000000000000000000000000000=";
     * private static final String REGISTRATION_ID = "registrationId-1";
     *
     * public static void main(String[] args) throws IOException, URISyntaxException, ProvisioningServiceClientException
     * {
     *     // Create a Device Provisioning Service Client.
     *     DeviceProvisioningServiceClient deviceProvisioningServiceClient =
     *         DeviceProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
     *
     *     // Get the content of the previous individualEnrollment.
     *     IndividualEnrollment individualEnrollment =  deviceProvisioningServiceClient.getIndividualEnrollment(REGISTRATION_ID);
     *
     *     // Change the provisioning status, from disabled to enabled
     *     individualEnrollment.setProvisioningStatus(ProvisioningStatus.ENABLED);
     *
     *     // Update the individualEnrollment information.
     *     IndividualEnrollment enrollmentResult =  deviceProvisioningServiceClient.createOrUpdateIndividualEnrollment(individualEnrollment);
     * }
     * }
     * </pre>
     *
     * @see <a href="https://docs.microsoft.com/en-us/azure/iot-dps/">Azure IoT Hub Device Provisioning Service</a>
     * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
     *
     * @param individualEnrollment the {@link IndividualEnrollment} object that describes the individualEnrollment that will be created of
     *                   updated. It cannot be {@code null}.
     * @return An {@link IndividualEnrollment} object with the result of the create or update requested.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to create or update the individualEnrollment.
     */
    public IndividualEnrollment createOrUpdateIndividualEnrollment(IndividualEnrollment individualEnrollment) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_008: [The createOrUpdateIndividualEnrollment shall create a new Provisioning individualEnrollment by calling the createOrUpdate in the individualEnrollmentManager.] */
        return individualEnrollmentManager.createOrUpdate(individualEnrollment);
    }

    /**
     * Create, update or delete a set of individual Device Enrollments.
     *
     * <p> This API provide the means to do a single operation over multiple individualEnrollments. A valid operation
     *     is determined by {@link BulkOperationMode}, and can be 'create', 'update', 'updateIfMatchETag', or 'delete'.
     *
     * <p> To use the Device Provisioning Service API, you must include the follow package on your application.
     * <pre>
     * {@code
     * // Include the following imports to use the Device Provisioning Service APIs.
     * import com.microsoft.azure.sdk.iot.provisioning.service.*;
     * }
     * </pre>
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will create two new enrollment that will provisioning the ContosoDevice1000 and
     *     ContosoDevice1001 to the ContosoHub.azure-devices.net using TPM attestation.
     * <pre>
     * {@code
     * // IndividualEnrollment information.
     * private static final String PROVISIONING_CONNECTION_STRING = "HostName=ContosoProvisioning.azure-devices-provisioning.net;" +
     *                                                              "SharedAccessKeyName=contosoprovisioningserviceowner;" +
     *                                                              "SharedAccessKey=0000000000000000000000000000000000000000000=";
     * private static final String TPM_ENDORSEMENT_KEY = "tpm-endorsement-key";
     * private static final String IOTHUB_HOST_NAME = "ContosoHub.azure-devices.net";
     *
     * private static final String REGISTRATION_ID_1 = "registrationId-1";
     * private static final String DEVICE_ID_1 = "ContosoDevice1000";
     *
     * private static final String REGISTRATION_ID_2 = "registrationId-2";
     * private static final String DEVICE_ID_2 = "ContosoDevice1001";
     *
     * public static void main(String[] args) throws IOException, URISyntaxException, ProvisioningServiceClientException
     * {
     *     // Create a Device Provisioning Service Client.
     *     DeviceProvisioningServiceClient deviceProvisioningServiceClient =
     *         DeviceProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
     *
     *     // Create two new individualEnrollment configurations.
     *     Attestation attestation = new TpmAttestation(TPM_ENDORSEMENT_KEY);
     *     IndividualEnrollment enrollment1 =
     *        new IndividualEnrollment(
     *             REGISTRATION_ID_1,
     *             attestation);
     *     enrollment1.setDeviceId(DEVICE_ID_1);
     *     enrollment1.setIotHubHostName(IOTHUB_HOST_NAME);
     *     enrollment1.setProvisioningStatus(ProvisioningStatus.DISABLED);
     *
     *     IndividualEnrollment enrollment2 =
     *        new IndividualEnrollment(
     *             REGISTRATION_ID_2,
     *             attestation);
     *     enrollment2.setDeviceId(DEVICE_ID_2);
     *     enrollment2.setIotHubHostName(IOTHUB_HOST_NAME);
     *     enrollment2.setProvisioningStatus(ProvisioningStatus.DISABLED);
     *
     *     // Add these 2 individualEnrollments to a list of individualEnrollments.
     *     List<IndividualEnrollment> individualEnrollments = new LinkedList<>();
     *     individualEnrollments.add(enrollment1);
     *     individualEnrollments.add(enrollment2);
     *
     *     // Create these 2 new individualEnrollment using the bulk operation.
     *     BulkEnrollmentOperationResult bulkEnrollmentOperationResult =  provisioningServiceClient.runBulkEnrollmentOperation(BulkOperationMode.create, individualEnrollments);
     * }
     * }
     * </pre>
     *
     * @see <a href="https://docs.microsoft.com/en-us/azure/iot-dps/">Azure IoT Hub Device Provisioning Service</a>
     * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
     *
     * @param bulkOperationMode the {@link BulkOperationMode} that defines the single operation to do over the individualEnrollments. It cannot be {@code null}.
     * @param individualEnrollments the collection of {@link IndividualEnrollment} that contains the description of each individualEnrollment. It cannot be {@code null} or empty.
     * @return A {@link BulkEnrollmentOperationResult} object with the result of operation for each enrollment.
     * @throws IllegalArgumentException if the provided parameters are not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the bulk operation.
     */
    public BulkEnrollmentOperationResult runBulkEnrollmentOperation(
            BulkOperationMode bulkOperationMode, Collection<IndividualEnrollment> individualEnrollments)
            throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_009: [The runBulkEnrollmentOperation shall do a Provisioning operation over individualEnrollment by calling the bulkOperation in the individualEnrollmentManager.] */
        return individualEnrollmentManager.bulkOperation(bulkOperationMode, individualEnrollments);
    }

    /**
     * Retrieve the individualEnrollment information.
     *
     * <p> This method will return the enrollment information for the provided registrationId. It will retrieve
     *     the correspondent individualEnrollment from the Device Provisioning Service, and return it in the
     *     {@link IndividualEnrollment} object.
     *
     * <p> If the registrationId do not exists, this method will throw
     *     {@link ProvisioningServiceClientNotFoundException}.
     *     for more exceptions that this method can throw, please see
     *     {@link ProvisioningServiceClientExceptionManager}
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will get and print the information about the individualEnrollment "registrationId-1".
     * <pre>
     * {@code
     * // IndividualEnrollment information.
     * private static final String PROVISIONING_CONNECTION_STRING = "HostName=ContosoProvisioning.azure-devices-provisioning.net;" +
     *                                                              "SharedAccessKeyName=contosoprovisioningserviceowner;" +
     *                                                              "SharedAccessKey=0000000000000000000000000000000000000000000=";
     * private static final String REGISTRATION_ID = "registrationId-1";
     *
     * public static void main(String[] args) throws IOException, URISyntaxException, ProvisioningServiceClientException
     * {
     *     // Create a Device Provisioning Service Client.
     *     DeviceProvisioningServiceClient deviceProvisioningServiceClient =
     *         DeviceProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
     *
     *     // Get the individualEnrollment information.
     *     IndividualEnrollment enrollmentResult =  deviceProvisioningServiceClient.getIndividualEnrollment(REGISTRATION_ID);
     *     System.out.println(enrollmentResult.toString());
     * }
     * }
     * </pre>
     *
     * @param registrationId the {@code String} that identifies the individualEnrollment. It cannot be {@code null} or empty.
     * @return The {@link IndividualEnrollment} with the content of the individualEnrollment in the Provisioning Device Service.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the bulk operation.
     */
    public IndividualEnrollment getIndividualEnrollment(String registrationId) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_010: [The getIndividualEnrollment shall retrieve the individualEnrollment information for the provided registrationId by calling the get in the individualEnrollmentManager.] */
        return individualEnrollmentManager.get(registrationId);
    }

    /**
     * Get the attestation mechanism details for a given individual enrollment
     * @param registrationId the registration id of the individual enrollment to look up the attestation mechanism for
     * @return the attestation mechanism of the given individual enrollment
     * @throws ProvisioningServiceClientException if any exception is thrown while getting the attestation mechanism
     */
    public AttestationMechanism getIndividualEnrollmentAttestationMechanism(String registrationId) throws ProvisioningServiceClientException
    {
        return individualEnrollmentManager.getAttestationMechanism(registrationId);
    }

    /**
     * Delete the individualEnrollment information.
     *
     * <p> This method will remove the individualEnrollment from the Device Provisioning Service using the
     *     provided {@link IndividualEnrollment} information. The Device Provisioning Service will care about the
     *     registrationId and the eTag on the individualEnrollment. If you want to delete the individualEnrollment regardless the
     *     eTag, you can set the {@code eTag="*"} into the individualEnrollment, or use the {@link #deleteIndividualEnrollment(String)}
     *     passing only the registrationId.
     *
     * <p> Note that delete the individualEnrollment will not remove the Device itself from the IotHub.
     *
     * <p> If the registrationId does not exists or the eTag not matches, this method will throw
     *     {@link ProvisioningServiceClientNotFoundException}.
     *     for more exceptions that this method can throw, please see
     *     {@link ProvisioningServiceClientExceptionManager}
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will delete the information about the individualEnrollment "registrationId-1".
     * <pre>
     * {@code
     * // IndividualEnrollment information.
     * private static final String PROVISIONING_CONNECTION_STRING = "HostName=ContosoProvisioning.azure-devices-provisioning.net;" +
     *                                                              "SharedAccessKeyName=contosoprovisioningserviceowner;" +
     *                                                              "SharedAccessKey=0000000000000000000000000000000000000000000=";
     * private static final String REGISTRATION_ID = "registrationId-1";
     *
     * public static void main(String[] args) throws IOException, URISyntaxException, ProvisioningServiceClientException
     * {
     *     // Create a Device Provisioning Service Client.
     *     DeviceProvisioningServiceClient deviceProvisioningServiceClient =
     *         DeviceProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
     *
     *     // Get the individualEnrollment information.
     *     IndividualEnrollment enrollmentResult =  deviceProvisioningServiceClient.getIndividualEnrollment(REGISTRATION_ID);
     *
     *     // Delete the individualEnrollment information.
     *     deviceProvisioningServiceClient.deleteIndividualEnrollment(enrollmentResult);
     * }
     * }
     * </pre>
     *
     * @param individualEnrollment the {@link IndividualEnrollment} that identifies the individualEnrollment. It cannot be {@code null}.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the bulk operation.
     */
    public void deleteIndividualEnrollment(IndividualEnrollment individualEnrollment) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_011: [The deleteIndividualEnrollment shall delete the individualEnrollment for the provided individualEnrollment by calling the delete in the individualEnrollmentManager.] */
        individualEnrollmentManager.delete(individualEnrollment);
    }

    /**
     * Delete the individualEnrollment information.
     *
     * <p> This method will remove the individualEnrollment from the Device Provisioning Service using the
     *     provided registrationId. It will delete the enrollment regardless the eTag. It means that this API
     *     correspond to the {@link #deleteIndividualEnrollment(String, String)} with the {@code eTag="*"}.
     *
     * <p> Note that delete the enrollment will not remove the Device itself from the IotHub.
     *
     * <p> If the registrationId does not exists, this method will throw
     *     {@link ProvisioningServiceClientNotFoundException}.
     *     for more exceptions that this method can throw, please see
     *     {@link ProvisioningServiceClientExceptionManager}
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will delete the information about the individualEnrollment "registrationId-1".
     * <pre>
     * {@code
     * // IndividualEnrollment information.
     * private static final String PROVISIONING_CONNECTION_STRING = "HostName=ContosoProvisioning.azure-devices-provisioning.net;" +
     *                                                              "SharedAccessKeyName=contosoprovisioningserviceowner;" +
     *                                                              "SharedAccessKey=0000000000000000000000000000000000000000000=";
     * private static final String REGISTRATION_ID = "registrationId-1";
     *
     * public static void main(String[] args) throws IOException, URISyntaxException, ProvisioningServiceClientException
     * {
     *     // Create a Device Provisioning Service Client.
     *     DeviceProvisioningServiceClient deviceProvisioningServiceClient =
     *         DeviceProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
     *
     *     // Delete the individualEnrollment information.
     *     deviceProvisioningServiceClient.deleteIndividualEnrollment(REGISTRATION_ID);
     * }
     * }
     * </pre>
     *
     * @param registrationId the {@code String} that identifies the individualEnrollment. It cannot be {@code null} or empty.
     * @throws IllegalArgumentException if the provided registrationId is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the bulk operation.
     */
    public void deleteIndividualEnrollment(String registrationId) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_012: [The deleteIndividualEnrollment shall delete the individualEnrollment for the provided registrationId by calling the delete in the individualEnrollmentManager.] */
        individualEnrollmentManager.delete(registrationId, null);
    }

    /**
     * Delete the individualEnrollment information.
     *
     * <p> This method will remove the individualEnrollment from the Device Provisioning Service using the
     *     provided registrationId and eTag. If you want to delete the enrollment regardless the eTag, you can
     *     use {@link #deleteIndividualEnrollment(String)} or you can pass the eTag as {@code null}, empty, or
     *     {@code "*"}.
     *
     * <p> Note that delete the enrollment will not remove the Device itself from the IotHub.
     *
     * <p> If the registrationId does not exists or the eTag does not matches, this method will throw
     *     {@link ProvisioningServiceClientNotFoundException}.
     *     for more exceptions that this method can throw, please see
     *     {@link ProvisioningServiceClientExceptionManager}
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will delete the information about the individualEnrollment "registrationId-1" regardless the eTag.
     * <pre>
     * {@code
     * // IndividualEnrollment information.
     * private static final String PROVISIONING_CONNECTION_STRING = "HostName=ContosoProvisioning.azure-devices-provisioning.net;" +
     *                                                              "SharedAccessKeyName=contosoprovisioningserviceowner;" +
     *                                                              "SharedAccessKey=0000000000000000000000000000000000000000000=";
     * private static final String REGISTRATION_ID = "registrationId-1";
     * private Static final String ANY_ETAG = "*";
     *
     * public static void main(String[] args) throws IOException, URISyntaxException, ProvisioningServiceClientException
     * {
     *     // Create a Device Provisioning Service Client.
     *     DeviceProvisioningServiceClient deviceProvisioningServiceClient =
     *         DeviceProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
     *
     *     // Delete the individualEnrollment information.
     *     deviceProvisioningServiceClient.deleteIndividualEnrollment(REGISTRATION_ID, ANY_ETAG);
     * }
     * }
     * </pre>
     *
     * @param registrationId the {@code String} that identifies the individualEnrollment. It cannot be {@code null} or empty.
     * @param eTag the {@code String} with the IndividualEnrollment eTag. It can be {@code null} or empty.
     *             The Device Provisioning Service will ignore it in all of these cases.
     * @throws IllegalArgumentException if the provided registrationId is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the bulk operation.
     */
    public void deleteIndividualEnrollment(String registrationId, String eTag) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_013: [The deleteIndividualEnrollment shall delete the individualEnrollment for the provided registrationId and etag by calling the delete in the individualEnrollmentManager.] */
        individualEnrollmentManager.delete(registrationId, eTag);
    }

    /**
     * Factory to create a individualEnrollment query.
     *
     * <p> This method will create a new individualEnrollment query for Device Provisioning Service and return it
     *     as a {@link Query} iterator.
     *
     * <p> The Device Provisioning Service expects a SQL query in the {@link QuerySpecification}, for instance
     *     {@code "SELECT * FROM enrollments"}.
     *
     * @param querySpecification the {@link QuerySpecification} with the SQL query. It cannot be {@code null}.
     * @return The {@link Query} iterator.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     */
    public Query createIndividualEnrollmentQuery(QuerySpecification querySpecification)
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_014: [The createIndividualEnrollmentQuery shall create a new individual enrolment query by calling the createQuery in the individualEnrollmentManager.] */
        return individualEnrollmentManager.createQuery(querySpecification, 0);
    }

    /**
     * Factory to create a individualEnrollment query.
     *
     * <p> This method will create a new individualEnrollment query for Device Provisioning Service and return it
     *     as a {@link Query} iterator.
     *
     * <p> The Device Provisioning Service expects a SQL query in the {@link QuerySpecification}, for instance
     *     {@code "SELECT * FROM enrollments"}.
     *
     * <p> For each iteration, the Query will return a List of objects correspondent to the query result. The maximum
     *     number of items per iteration can be specified by the pageSize. It is optional, you can provide <b>0</b> for
     *     default pageSize or use the API {@link #createIndividualEnrollmentQuery(QuerySpecification)}.
     *
     * @param querySpecification the {@link QuerySpecification} with the SQL query. It cannot be {@code null}.
     * @param pageSize the {@code int} with the maximum number of items per iteration. It can be 0 for default, but not negative.
     * @return The {@link Query} iterator.
     * @throws IllegalArgumentException if the provided parameters are not correct.
     */
    public Query createIndividualEnrollmentQuery(QuerySpecification querySpecification, int pageSize)
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_015: [The createIndividualEnrollmentQuery shall create a new individual enrolment query by calling the createQuery in the individualEnrollmentManager.] */
        return individualEnrollmentManager.createQuery(querySpecification, pageSize);
    }

    /**
     * Create or update an enrollment group record.
     *
     * <p> This API creates a new enrollment group or update a existed one. All enrollment group in the Device
     *     Provisioning Service contains a unique identifier called enrollmentGroupId. If this API is called
     *     with an enrollmentGroupId that already exists, it will replace the existed enrollmentGroup information
     *     by the new one. On the other hand, if the enrollmentGroupId does not exit, it will be created.
     *
     * <p> To use the Device Provisioning Service API, you must include the follow package on your application.
     * <pre>
     * {@code
     * // Include the following imports to use the Device Provisioning Service APIs.
     * import com.microsoft.azure.sdk.iot.provisioning.service.*;
     * }
     * </pre>
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will create a new enrollmentGroup that will provisioning multiple devices to the
     *     ContosoHub.azure-devices.net.
     * <pre>
     * {@code
     * // EnrollmentGroup information.
     * private static final String PROVISIONING_CONNECTION_STRING = "HostName=ContosoProvisioning.azure-devices-provisioning.net;" +
     *                                                              "SharedAccessKeyName=contosoprovisioningserviceowner;" +
     *                                                              "SharedAccessKey=0000000000000000000000000000000000000000000=";
     * private static final String ENROLLMENT_GROUP_ID = "enrollmentGroupId-1";
     * private static final String IOTHUB_HOST_NAME = "ContosoHub.azure-devices.net";
     * private static String PUBLIC_CERTIFICATE_STRING =
     *         "-----BEGIN CERTIFICATE-----\n" +
     *         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     *         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     *         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     *         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     *         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     *         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     *         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     *         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     *         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     *         "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
     *         "-----END CERTIFICATE-----\n";
     *
     * public static void main(String[] args) throws IOException, URISyntaxException, ProvisioningServiceClientException
     * {
     *     // Create a Device Provisioning Service Client.
     *     DeviceProvisioningServiceClient deviceProvisioningServiceClient =
     *         DeviceProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
     *
     *     // Create a new enrollmentGroup configurations.
     *     Attestation attestation = X509Attestation.createFromSigningCertificates(PUBLIC_KEY_CERTIFICATE_STRING);
     *     EnrollmentGroup enrollmentGroup =
     *         new EnrollmentGroup(
     *             enrollmentGroupId,
     *             attestation);
     *     enrollmentGroup.setIotHubHostName(IOTHUB_HOST_NAME);
     *     enrollmentGroup.setProvisioningStatus(ProvisioningStatus.ENABLED);
     *
     *     // Create a new enrollmentGroup.
     *     EnrollmentGroup enrollmentGroupResult =  provisioningServiceClient.createOrUpdateEnrollmentGroup(enrollmentGroup);
     * }
     * }
     * </pre>
     *
     * @see <a href="https://docs.microsoft.com/en-us/azure/iot-dps/">Azure IoT Hub Device Provisioning Service</a>
     * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollmentgroup">Device Enrollment Group</a>
     *
     * @param enrollmentGroup the {@link EnrollmentGroup} object that describes the individualEnrollment that will be created of updated.
     * @return An {@link EnrollmentGroup} object with the result of the create or update requested.
     * @throws ProvisioningServiceClientException if the Provisioning was not able to create or update the enrollment
     */
    public EnrollmentGroup createOrUpdateEnrollmentGroup(EnrollmentGroup enrollmentGroup) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_016: [The createOrUpdateEnrollmentGroup shall create a new Provisioning enrollmentGroup by calling the createOrUpdate in the enrollmentGroupManager.] */
        return enrollmentGroupManager.createOrUpdate(enrollmentGroup);
    }

    /**
     * Retrieve the enrollmentGroup information.
     *
     * <p> This method will return the enrollmentGroup information for the provided enrollmentGroupId. It will retrieve
     *     the correspondent enrollmentGroup from the Device Provisioning Service, and return it in the
     *     {@link EnrollmentGroup} object.
     *
     * <p> If the enrollmentGroupId does not exists, this method will throw
     *     {@link ProvisioningServiceClientNotFoundException}.
     *     for more exceptions that this method can throw, please see
     *     {@link ProvisioningServiceClientExceptionManager}
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will get and print the information about the enrollmentGroupId "enrollmentGroupId-1".
     * <pre>
     * {@code
     * // EnrollmentGroup information.
     * private static final String PROVISIONING_CONNECTION_STRING = "HostName=ContosoProvisioning.azure-devices-provisioning.net;" +
     *                                                              "SharedAccessKeyName=contosoprovisioningserviceowner;" +
     *                                                              "SharedAccessKey=0000000000000000000000000000000000000000000=";
     * private static final String ENROLLMENT_GROUP_ID = "enrollmentGroupId-1";
     *
     * public static void main(String[] args) throws IOException, URISyntaxException, ProvisioningServiceClientException
     * {
     *     // Create a Device Provisioning Service Client.
     *     DeviceProvisioningServiceClient deviceProvisioningServiceClient =
     *         DeviceProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
     *
     *     // Get the individualEnrollment information.
     *     EnrollmentGroup enrollmentGroupResult =  deviceProvisioningServiceClient.getEnrollmentGroup(ENROLLMENT_GROUP_ID);
     *     System.out.println(enrollmentGroupResult.toString());
     * }
     * }
     * </pre>
     *
     * @param enrollmentGroupId the {@code String} that identifies the enrollmentGroup. It cannot be {@code null} or empty.
     * @return The {@link EnrollmentGroup} with the content of the enrollmentGroup in the Provisioning Device Service.
     * @throws ProvisioningServiceClientException if the Provisioning Device Service was not able to retrieve the
     *                                            enrollmentGroup information for the provided enrollmentGroupId.
     */
    public EnrollmentGroup getEnrollmentGroup(String enrollmentGroupId) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_017: [The getEnrollmentGroup shall retrieve the enrollmentGroup information for the provided enrollmentGroupId by calling the get in the enrollmentGroupManager.] */
        return enrollmentGroupManager.get(enrollmentGroupId);
    }

    /**
     * Get the attestation mechanism details for a given enrollment group
     * @param enrollmentGroupId the group id of the enrollment group to look up the attestation mechanism for
     * @return the attestation mechanism of the given enrollment group
     * @throws ProvisioningServiceClientException if any exception is thrown while getting the attestation mechanism
     */
    public AttestationMechanism getEnrollmentGroupAttestationMechanism(String enrollmentGroupId) throws ProvisioningServiceClientException
    {
        return enrollmentGroupManager.getAttestationMechanism(enrollmentGroupId);
    }

    /**
     * Delete the enrollmentGroup information.
     *
     * <p> This method will remove the enrollmentGroup from the Device Provisioning Service using the
     *     provided {@link EnrollmentGroup} information. The Device Provisioning Service will care about the
     *     enrollmentGroupId and the eTag on the enrollmentGroup. If you want to delete the enrollment regardless the
     *     eTag, you can set the {@code eTag="*"} into the enrollmentGroup, or use the {@link #deleteEnrollmentGroup(String)}
     *     passing only the enrollmentGroupId.
     *
     * <p> Note that delete the enrollmentGroup will not remove the Devices itself from the IotHub.
     *
     * <p> If the enrollmentGroupId does not exists or the eTag does not matches, this method will throw
     *     {@link ProvisioningServiceClientNotFoundException}.
     *     for more exceptions that this method can throw, please see
     *     {@link ProvisioningServiceClientExceptionManager}
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will delete the information about the enrollmentGroup "enrollmentGroupId-1".
     * <pre>
     * {@code
     * // EnrollmentGroup information.
     * private static final String PROVISIONING_CONNECTION_STRING = "HostName=ContosoProvisioning.azure-devices-provisioning.net;" +
     *                                                              "SharedAccessKeyName=contosoprovisioningserviceowner;" +
     *                                                              "SharedAccessKey=0000000000000000000000000000000000000000000=";
     * private static final String ENROLLMENT_GROUP_ID = "enrollmentGroupId-1";
     *
     * public static void main(String[] args) throws IOException, URISyntaxException, ProvisioningServiceClientException
     * {
     *     // Create a Device Provisioning Service Client.
     *     DeviceProvisioningServiceClient deviceProvisioningServiceClient =
     *         DeviceProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
     *
     *     // Get the enrollmentGroup information.
     *     EnrollmentGroup enrollmentGroupResult =  deviceProvisioningServiceClient.getEnrollmentGroup(ENROLLMENT_GROUP_ID);
     *
     *     // Delete the enrollmentGroup information.
     *     deviceProvisioningServiceClient.deleteEnrollmentGroup(enrollmentResult);
     * }
     * }
     * </pre>
     *
     * @param enrollmentGroup the {@link EnrollmentGroup} that identifies the enrollmentGroup. It cannot be {@code null}.
     * @throws ProvisioningServiceClientException if the Provisioning Device Service was not able to delete the
     *                                            enrollmentGroup information for the provided enrollmentGroup.
     */
    public void deleteEnrollmentGroup(EnrollmentGroup enrollmentGroup) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_018: [The deleteEnrollmentGroup shall delete the enrollmentGroup for the provided enrollmentGroup by calling the delete in the enrollmentGroupManager.] */
        enrollmentGroupManager.delete(enrollmentGroup);
    }

    /**
     * Delete the enrollmentGroup information.
     *
     * <p> This method will remove the enrollmentGroup from the Device Provisioning Service using the
     *     provided enrollmentGroupId. It will delete the enrollmentGroup regardless the eTag. It means that this API
     *     correspond to the {@link #deleteEnrollmentGroup(String, String)} with the {@code eTag="*"}.
     *
     * <p> Note that delete the enrollmentGroup will not remove the Devices itself from the IotHub.
     *
     * <p> If the enrollmentGroupId does not exists, this method will throw
     *     {@link ProvisioningServiceClientNotFoundException}.
     *     for more exceptions that this method can throw, please see
     *     {@link ProvisioningServiceClientExceptionManager}
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will delete the information about the enrollmentGroup "enrollmentGroupId-1".
     * <pre>
     * {@code
     * // EnrollmentGroup information.
     * private static final String PROVISIONING_CONNECTION_STRING = "HostName=ContosoProvisioning.azure-devices-provisioning.net;" +
     *                                                              "SharedAccessKeyName=contosoprovisioningserviceowner;" +
     *                                                              "SharedAccessKey=0000000000000000000000000000000000000000000=";
     * private static final String ENROLLMENT_GROUP_ID = "enrollmentGroupId-1";
     *
     * public static void main(String[] args) throws IOException, URISyntaxException, ProvisioningServiceClientException
     * {
     *     // Create a Device Provisioning Service Client.
     *     DeviceProvisioningServiceClient deviceProvisioningServiceClient =
     *         DeviceProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
     *
     *     // Delete the enrollmentGroup information.
     *     deviceProvisioningServiceClient.deleteEnrollmentGroup(ENROLLMENT_GROUP_ID);
     * }
     * }
     * </pre>
     *
     * @param enrollmentGroupId the {@code String} that identifies the enrollmentGroup. It cannot be {@code null} or empty.
     * @throws ProvisioningServiceClientException if the Provisioning Device Service was not able to delete the
     *                                            enrollmentGroup information for the provided enrollmentGroupId.
     */
    public void deleteEnrollmentGroup(String enrollmentGroupId) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_019: [The deleteEnrollmentGroup shall delete the enrollmentGroup for the provided enrollmentGroupId by calling the delete in the enrollmentGroupManager.] */
        enrollmentGroupManager.delete(enrollmentGroupId, null);
    }

    /**
     * Delete the enrollmentGroup information.
     *
     * <p> This method will remove the enrollmentGroup from the Device Provisioning Service using the
     *     provided enrollmentGroupId and eTag. If you want to delete the enrollmentGroup regardless the eTag, you can
     *     use {@link #deleteEnrollmentGroup(String)} or you can pass the eTag as {@code null}, empty, or
     *     {@code "*"}.
     *
     * <p> Note that delete the enrollmentGroup will not remove the Device itself from the IotHub.
     *
     * <p> If the enrollmentGroupId does not exists or eTag does not matches, this method will throw
     *     {@link ProvisioningServiceClientNotFoundException}.
     *     for more exceptions that this method can throw, please see
     *     {@link ProvisioningServiceClientExceptionManager}
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will delete the information about the enrollmentGroup "enrollmentGroupId-1" regardless the eTag.
     * <pre>
     * {@code
     * // enrollmentGroup information.
     * private static final String PROVISIONING_CONNECTION_STRING = "HostName=ContosoProvisioning.azure-devices-provisioning.net;" +
     *                                                              "SharedAccessKeyName=contosoprovisioningserviceowner;" +
     *                                                              "SharedAccessKey=0000000000000000000000000000000000000000000=";
     * private static final String ENROLLMENT_GROUP_ID = "enrollmentGroupId-1";
     * private Static final String ANY_ETAG = "*";
     *
     * public static void main(String[] args) throws IOException, URISyntaxException, ProvisioningServiceClientException
     * {
     *     // Create a Device Provisioning Service Client.
     *     DeviceProvisioningServiceClient deviceProvisioningServiceClient =
     *         DeviceProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
     *
     *     // Delete the enrollmentGroup information.
     *     deviceProvisioningServiceClient.deleteEnrollmentGroup(ENROLLMENT_GROUP_ID, ANY_ETAG);
     * }
     * }
     * </pre>
     *
     * @param enrollmentGroupId the {@code String} that identifies the enrollmentGroup. It cannot be {@code null} or empty.
     * @param eTag the {@code String} with the enrollmentGroup eTag. It can be {@code null} or empty.
     *             The Device Provisioning Service will ignore it in all of these cases.
     * @throws ProvisioningServiceClientException if the Provisioning Device Service was not able to delete the
     *                                            enrollmentGroup information for the provided enrollmentGroupId and eTag.
     */
    public void deleteEnrollmentGroup(String enrollmentGroupId, String eTag) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_020: [The deleteEnrollmentGroup shall delete the enrollmentGroup for the provided enrollmentGroupId and eTag by calling the delete in the enrollmentGroupManager.] */
        enrollmentGroupManager.delete(enrollmentGroupId, eTag);
    }

    /**
     * Factory to create an enrollmentGroup query.
     *
     * <p> This method will create a new enrollment group query on Device Provisioning Service and return it as
     *     a {@link Query} iterator.
     *
     * <p> The Device Provisioning Service expects a SQL query in the {@link QuerySpecification}, for instance
     *     {@code "SELECT * FROM enrollments"}.
     *
     * @param querySpecification the {@link QuerySpecification} with the SQL query. It cannot be {@code null}.
     * @return The {@link Query} iterator.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     */
    public Query createEnrollmentGroupQuery(QuerySpecification querySpecification)
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_021: [The createEnrollmentGroupQuery shall create a new enrolmentGroup query by calling the createQuery in the enrollmentGroupManager.] */
        return enrollmentGroupManager.createQuery(querySpecification, 0);
    }

    /**
     * Factory to create an enrollmentGroup query.
     *
     * <p> This method will create a new enrollment group query on Device Provisioning Service and return it as
     *     a {@link Query} iterator.
     *
     * <p> The Device Provisioning Service expects a SQL query in the {@link QuerySpecification}, for instance
     *     {@code "SELECT * FROM enrollments"}.
     *
     * <p> For each iteration, the Query will return a List of objects correspondent to the query result. The maximum
     *     number of items per iteration can be specified by the pageSize. It is optional, you can provide <b>0</b> for
     *     default pageSize or use the API {@link #createEnrollmentGroupQuery(QuerySpecification)}.
     *
     * @param querySpecification the {@link QuerySpecification} with the SQL query. It cannot be {@code null}.
     * @param pageSize the {@code int} with the maximum number of items per iteration. It can be 0 for default, but not negative.
     * @return The {@link Query} iterator.
     * @throws IllegalArgumentException if the provided parameters are not correct.
     */
    public Query createEnrollmentGroupQuery(QuerySpecification querySpecification, int pageSize)
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_022: [The createEnrollmentGroupQuery shall create a new enrolmentGroup query by calling the createQuery in the enrollmentGroupManager.] */
        return enrollmentGroupManager.createQuery(querySpecification, pageSize);
    }

    /**
     * Retrieve the registration status information.
     *
     * <p> This method will return the {@link DeviceRegistrationState} for the provided id. It will retrieve
     *     the correspondent deviceRegistrationState from the Device Provisioning Service, and return it in the
     *     {@link DeviceRegistrationState} object.
     *
     * <p> If the id do not exists, this method will throw
     *     {@link ProvisioningServiceClientNotFoundException}.
     *     for more exceptions that this method can throw, please see
     *     {@link ProvisioningServiceClientExceptionManager}
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will get and print the information about the deviceRegistrationState "registrationId-1".
     * <pre>
     * {@code
     * // Registration status information.
     * private static final String PROVISIONING_CONNECTION_STRING = "HostName=ContosoProvisioning.azure-devices-provisioning.net;" +
     *                                                              "SharedAccessKeyName=contosoprovisioningserviceowner;" +
     *                                                              "SharedAccessKey=0000000000000000000000000000000000000000000=";
     * private static final String REGISTRATION_ID = "registrationId-1";
     *
     * public static void main(String[] args) throws IOException, URISyntaxException, ProvisioningServiceClientException
     * {
     *     // Create a Device Provisioning Service Client.
     *     DeviceProvisioningServiceClient deviceProvisioningServiceClient =
     *         DeviceProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
     *
     *     // Get the deviceRegistrationState information.
     *     DeviceRegistrationState registrationStateResult =  deviceProvisioningServiceClient.getDeviceRegistrationState(REGISTRATION_ID);
     *     System.out.println(registrationStateResult.toString());
     * }
     * }
     * </pre>
     *
     * @param id the {@code String} that identifies the deviceRegistrationState. It cannot be {@code null} or empty.
     * @return The {@link DeviceRegistrationState} with the content of the deviceRegistrationState in the Provisioning Device Service.
     * @throws ProvisioningServiceClientException if the Provisioning Device Service was not able to retrieve the
     *                                            deviceRegistrationState information for the provided registrationId.
     */
    public DeviceRegistrationState getDeviceRegistrationState(String id) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_023: [The getDeviceRegistrationState shall retrieve the deviceRegistrationState information for the provided id by calling the get in the registrationStatusManager.] */
        return registrationStatusManager.get(id);
    }

    /**
     * Delete the Registration Status information.
     *
     * <p> This method will remove the {@link DeviceRegistrationState} from the Device Provisioning Service using the
     *     provided {@link DeviceRegistrationState} information. The Device Provisioning Service will care about the
     *     id and the eTag on the DeviceRegistrationState. If you want to delete the deviceRegistrationState regardless the
     *     eTag, you can use the {@link #deleteDeviceRegistrationState(String)} passing only the id.
     *
     * <p> If the id does not exists or the eTag does not matches, this method will throw
     *     {@link ProvisioningServiceClientNotFoundException}.
     *     for more exceptions that this method can throw, please see
     *     {@link ProvisioningServiceClientExceptionManager}
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will delete the information about the deviceRegistrationState "registrationId-1".
     * <pre>
     * {@code
     * // Registration Status information.
     * private static final String PROVISIONING_CONNECTION_STRING = "HostName=ContosoProvisioning.azure-devices-provisioning.net;" +
     *                                                              "SharedAccessKeyName=contosoprovisioningserviceowner;" +
     *                                                              "SharedAccessKey=0000000000000000000000000000000000000000000=";
     * private static final String REGISTRATION_ID = "registrationId-1";
     *
     * public static void main(String[] args) throws IOException, URISyntaxException, ProvisioningServiceClientException
     * {
     *     // Create a Device Provisioning Service Client.
     *     DeviceProvisioningServiceClient deviceProvisioningServiceClient =
     *         DeviceProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
     *
     *     // Get the registration status information.
     *     DeviceRegistrationState registrationStateResult =  deviceProvisioningServiceClient.getDeviceRegistrationState(REGISTRATION_ID);
     *
     *     // Delete the registration status information.
     *     deviceProvisioningServiceClient.deleteDeviceRegistrationState(registrationStateResult);
     * }
     * }
     * </pre>
     *
     * @param deviceRegistrationState the {@link DeviceRegistrationState} that identifies the deviceRegistrationState. It cannot be {@code null}.
     * @throws ProvisioningServiceClientException if the Provisioning Device Service was not able to delete the
     *                                            registration status information for the provided DeviceRegistrationState.
     */
    public void deleteDeviceRegistrationState(DeviceRegistrationState deviceRegistrationState) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_024: [The deleteDeviceRegistrationState shall delete the deviceRegistrationState for the provided DeviceRegistrationState by calling the delete in the registrationStatusManager.] */
        registrationStatusManager.delete(deviceRegistrationState);
    }

    /**
     * @deprecated As of release 1.0.0, replaced by {@link #deleteDeviceRegistrationState(DeviceRegistrationState)} ()}
     * @param deviceRegistrationState the {@link DeviceRegistrationState} that identifies the deviceRegistrationState. It cannot be {@code null}.
     * @throws ProvisioningServiceClientException if the Provisioning Device Service was not able to delete the
     *                                            registration status information for the provided DeviceRegistrationState.
     */
    @Deprecated
    public void deleteDeviceRegistrationStatus(DeviceRegistrationState deviceRegistrationState) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_024: [The deleteDeviceRegistrationStatus shall delete the deviceRegistrationState for the provided DeviceRegistrationState by calling the delete in the registrationStatusManager.] */
        registrationStatusManager.delete(deviceRegistrationState);
    }

    /**
     * Delete the registration status information.
     *
     * <p> This method will remove the {@link DeviceRegistrationState} from the Device Provisioning Service using the
     *     provided id. It will delete the registration status regardless the eTag. It means that this API
     *     correspond to the {@link #deleteDeviceRegistrationState(String, String)} with the {@code eTag="*"}.
     *
     * <p> If the id does not exists, this method will throw
     *     {@link ProvisioningServiceClientNotFoundException}.
     *     for more exceptions that this method can throw, please see
     *     {@link ProvisioningServiceClientExceptionManager}
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will delete the information about the registration status "registrationId-1".
     * <pre>
     * {@code
     * // deviceRegistrationState information.
     * private static final String PROVISIONING_CONNECTION_STRING = "HostName=ContosoProvisioning.azure-devices-provisioning.net;" +
     *                                                              "SharedAccessKeyName=contosoprovisioningserviceowner;" +
     *                                                              "SharedAccessKey=0000000000000000000000000000000000000000000=";
     * private static final String REGISTRATION_ID = "registrationId-1";
     *
     * public static void main(String[] args) throws IOException, URISyntaxException, ProvisioningServiceClientException
     * {
     *     // Create a Device Provisioning Service Client.
     *     DeviceProvisioningServiceClient deviceProvisioningServiceClient =
     *         DeviceProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
     *
     *     // Delete the registration status information.
     *     deviceProvisioningServiceClient.deleteDeviceRegistrationState(REGISTRATION_ID);
     * }
     * }
     * </pre>
     *
     * @param id the {@code String} that identifies the deviceRegistrationState. It cannot be {@code null} or empty.
     * @throws ProvisioningServiceClientException if the Provisioning Device Service was not able to delete the
     *                                            deviceRegistrationState information for the provided registrationId.
     */
    public void deleteDeviceRegistrationState(String id) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_025: [The deleteDeviceRegistrationState shall delete the deviceRegistrationState for the provided id by calling the delete in the registrationStatusManager.] */
        registrationStatusManager.delete(id, null);
    }

    /**
     * @deprecated As of release 1.0.0, replaced by {@link #deleteDeviceRegistrationState(String)} ()}
     * @param id the {@code String} that identifies the deviceRegistrationState. It cannot be {@code null} or empty.
     * @throws ProvisioningServiceClientException if the Provisioning Device Service was not able to delete the
     *                                            deviceRegistrationState information for the provided registrationId.
     */
    @Deprecated
    public void deleteDeviceRegistrationStatus(String id) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_025: [The deleteDeviceRegistrationStatus shall delete the deviceRegistrationState for the provided id by calling the delete in the registrationStatusManager.] */
        registrationStatusManager.delete(id, null);
    }

    /**
     * Delete the registration status information.
     *
     * <p> This method will remove the registration status from the Device Provisioning Service using the
     *     provided id and eTag. If you want to delete the registration status regardless the eTag, you can
     *     use {@link #deleteDeviceRegistrationState(String)} or you can pass the eTag as {@code null}, empty, or
     *     {@code "*"}.
     *
     * <p> If the id does not exists or the eTag does not matches, this method will throw
     *     {@link ProvisioningServiceClientNotFoundException}.
     *     for more exceptions that this method can throw, please see
     *     {@link ProvisioningServiceClientExceptionManager}
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will delete the information about the deviceRegistrationState "registrationId-1" regardless the eTag.
     * <pre>
     * {@code
     * // Registration Status information.
     * private static final String PROVISIONING_CONNECTION_STRING = "HostName=ContosoProvisioning.azure-devices-provisioning.net;" +
     *                                                              "SharedAccessKeyName=contosoprovisioningserviceowner;" +
     *                                                              "SharedAccessKey=0000000000000000000000000000000000000000000=";
     * private static final String REGISTRATION_ID = "registrationId-1";
     * private Static final String ANY_ETAG = "*";
     *
     * public static void main(String[] args) throws IOException, URISyntaxException, ProvisioningServiceClientException
     * {
     *     // Create a Device Provisioning Service Client.
     *     DeviceProvisioningServiceClient deviceProvisioningServiceClient =
     *         DeviceProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
     *
     *     // Delete the deviceRegistrationState information.
     *     deviceProvisioningServiceClient.deleteDeviceRegistrationState(REGISTRATION_ID, ANY_ETAG);
     * }
     * }
     * </pre>
     *
     * @param id the {@code String} that identifies the deviceRegistrationState. It cannot be {@code null} or empty.
     * @param eTag the {@code String} with the deviceRegistrationState eTag. It can be {@code null} or empty.
     *             The Device Provisioning Service will ignore it in all of these cases.
     * @throws ProvisioningServiceClientException if the Provisioning Device Service was not able to delete the
     *                                            deviceRegistrationState information for the provided registrationId and eTag.
     */
    public void deleteDeviceRegistrationState(String id, String eTag) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_026: [The deleteDeviceRegistrationState shall delete the deviceRegistrationState for the provided id and eTag by calling the delete in the registrationStatusManager.] */
        registrationStatusManager.delete(id, eTag);
    }

    /**
     * @deprecated As of release 1.0.0, replaced by {@link #deleteDeviceRegistrationState(String, String)} ()}
     * @param id the {@code String} that identifies the deviceRegistrationState. It cannot be {@code null} or empty.
     * @param eTag the {@code String} with the deviceRegistrationState eTag. It can be {@code null} or empty.
     *             The Device Provisioning Service will ignore it in all of these cases.
     * @throws ProvisioningServiceClientException if the Provisioning Device Service was not able to delete the
     *                                            deviceRegistrationState information for the provided registrationId and eTag.
     */
    @Deprecated
    public void deleteDeviceRegistrationStatus(String id, String eTag) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_026: [The deleteDeviceRegistrationStatus shall delete the deviceRegistrationState for the provided id and eTag by calling the delete in the registrationStatusManager.] */
        registrationStatusManager.delete(id, eTag);
    }

    /**
     * Factory to create a registration status query.
     *
     * <p> This method will create a new registration status query for a specific enrollment group on the Device
     *     Provisioning Service and return it as a {@link Query} iterator.
     *
     * <p> The Device Provisioning Service expects a SQL query in the {@link QuerySpecification}, for instance
     *     {@code "SELECT * FROM enrollments"}.
     *
     * @param querySpecification the {@link QuerySpecification} with the SQL query. It cannot be {@code null}.
     * @param enrollmentGroupId the {@code String} that identifies the enrollmentGroup. It cannot be {@code null} or empty.
     * @return The {@link Query} iterator.
     */
    public Query createEnrollmentGroupRegistrationStateQuery(QuerySpecification querySpecification, String enrollmentGroupId)
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_027: [The createEnrollmentGroupRegistrationStateQuery shall create a new deviceRegistrationState query by calling the createQuery in the registrationStatusManager.] */
        return registrationStatusManager.createEnrollmentGroupQuery(querySpecification, enrollmentGroupId,0);
    }

    /**
     * @deprecated As of release 1.0.0, replaced by {@link #createEnrollmentGroupRegistrationStateQuery(QuerySpecification, String)} ()}
     * @param querySpecification the {@link QuerySpecification} with the SQL query. It cannot be {@code null}.
     * @param enrollmentGroupId the {@code String} that identifies the enrollmentGroup. It cannot be {@code null} or empty.
     * @return The {@link Query} iterator.
     */
    @Deprecated
    public Query createEnrollmentGroupRegistrationStatusQuery(QuerySpecification querySpecification, String enrollmentGroupId)
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_027: [The createEnrollmentGroupRegistrationStatusQuery shall create a new deviceRegistrationState query by calling the createQuery in the registrationStatusManager.] */
        return registrationStatusManager.createEnrollmentGroupQuery(querySpecification, enrollmentGroupId,0);
    }

    /**
     * Factory to create a registration status query.
     *
     * <p> This method will create a new registration status query for a specific enrollment group on the Device
     *     Provisioning Service and return it as a {@link Query} iterator.
     *
     * <p> The Device Provisioning Service expects a SQL query in the {@link QuerySpecification}, for instance
     *     {@code "SELECT * FROM enrollments"}.
     *
     * <p> For each iteration, the Query will return a List of objects correspondent to the query result. The maximum
     *     number of items per iteration can be specified by the pageSize. It is optional, you can provide <b>0</b> for
     *     default pageSize or use the API {@link #createIndividualEnrollmentQuery(QuerySpecification)}.
     *
     * @param querySpecification the {@link QuerySpecification} with the SQL query. It cannot be {@code null}.
     * @param enrollmentGroupId the {@code String} that identifies the enrollmentGroup. It cannot be {@code null} or empty.
     * @param pageSize the {@code int} with the maximum number of items per iteration. It can be 0 for default, but not negative.
     * @return The {@link Query} iterator.
     * @throws IllegalArgumentException if the provided parameters are not correct.
     */
    public Query createEnrollmentGroupRegistrationStateQuery(QuerySpecification querySpecification, String enrollmentGroupId, int pageSize)
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_028: [The createEnrollmentGroupRegistrationStateQuery shall create a new deviceRegistrationState query by calling the createQuery in the registrationStatusManager.] */
        return registrationStatusManager.createEnrollmentGroupQuery(querySpecification, enrollmentGroupId, pageSize);
    }

    /**
     * @deprecated As of release 1.0.0, replaced by {@link #createEnrollmentGroupRegistrationStateQuery(QuerySpecification, String, int)} ()}
     * @param querySpecification the {@link QuerySpecification} with the SQL query. It cannot be {@code null}.
     * @param enrollmentGroupId the {@code String} that identifies the enrollmentGroup. It cannot be {@code null} or empty.
     * @param pageSize the {@code int} with the maximum number of items per iteration. It can be 0 for default, but not negative.
     * @return The {@link Query} iterator.
     * @throws IllegalArgumentException if the provided parameters are not correct.
     */
    @Deprecated
    public Query createEnrollmentGroupRegistrationStatusQuery(QuerySpecification querySpecification, String enrollmentGroupId, int pageSize)
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_028: [The createEnrollmentGroupRegistrationStatusQuery shall create a new deviceRegistrationState query by calling the createQuery in the registrationStatusManager.] */
        return registrationStatusManager.createEnrollmentGroupQuery(querySpecification, enrollmentGroupId, pageSize);
    }
}
