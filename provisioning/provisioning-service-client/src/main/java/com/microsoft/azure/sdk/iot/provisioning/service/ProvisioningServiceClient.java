// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service;

import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningConnectionString;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.provisioning.service.contract.ContractApiHttp;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientExceptionManager;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientNotFoundException;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientTransportException;

import java.util.Collection;

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
 *     shall create the object {@link Enrollment} with the appropriate enrollment configurations, and call the
 *     {@link #createOrUpdateIndividualEnrollment(Enrollment)}.
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
     * PRIVATE CONSTRUCTOR
     *
     * @param connectionString the {@code String} that contains the connection string for the Provisioning service.
     * @throws IllegalArgumentException if the connectionString is {@code null}, empty, or invalid.
     */
    private ProvisioningServiceClient(String connectionString)
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_002: [The constructor shall throws IllegalArgumentException if the provided connectionString is null or empty.] */
        if(Tools.isNullOrEmpty(connectionString))
        {
            throw new IllegalArgumentException("connectionString cannot be null or empty");
        }

        /* SRS_PROVISIONING_SERVICE_CLIENT_21_003: [The constructor shall throws IllegalArgumentException if the ProvisioningConnectionString or one of the inner Managers failed to create a new instance.] */
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_004: [The constructor shall create a new instance of the ContractApiHttp class using the provided connectionString.] */
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);
        ContractApiHttp contractApiHttp = ContractApiHttp.createFromConnectionString(provisioningConnectionString);

        /* SRS_PROVISIONING_SERVICE_CLIENT_21_005: [The constructor shall create a new instance of the IndividualEnrollmentManger.] */
        this.individualEnrollmentManager = IndividualEnrollmentManager.createFromContractApiHttp(contractApiHttp);
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_006: [The constructor shall create a new instance of the EnrollmentGroupManager.] */
        this.enrollmentGroupManager = EnrollmentGroupManager.createFromContractApiHttp(contractApiHttp);
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_007: [The constructor shall create a new instance of the RegistrationStatusManager.] */
        this.registrationStatusManager = RegistrationStatusManager.createFromContractApiHttp(contractApiHttp);
    }

    /**
     * Create or update a individual device enrollment record.
     *
     * <p> This API creates a new enrollment or update a existed one. All enrollments in the Device Provisioning Service
     *     contains a unique identifier called registrationId. If this API is called for an enrollment with a
     *     registrationId that already exists, it will replace the existed enrollment information by the new one.
     *     On the other hand, if the registrationId does not exit, this API will create a new enrollment.
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
     * <p> The follow code will create a new enrollment that will provisioning the ContosoDevice1000 to the
     *     ContosoHub.azure-devices.net using TPM attestation.
     * <pre>
     * {@code
     * // Enrollment information.
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
     *     // Create a new individual enrollment configurations.
     *     Attestation attestation = new TpmAttestation(TPM_ENDORSEMENT_KEY);
     *     Enrollment enrollment =
     *        new Enrollment(
     *             REGISTRATION_ID,
     *             attestation);
     *     enrollment.setDeviceId(DEVICE_ID);
     *     enrollment.setIotHubHostName(IOTHUB_HOST_NAME);
     *     enrollment.setProvisioningStatus(ProvisioningStatus.DISABLED);
     *
     *     // Create a new individual enrollment.
     *     Enrollment enrollmentResult =  deviceProvisioningServiceClient.createOrUpdateIndividualEnrollment(enrollment);
     * }
     * }
     * </pre>
     *
     * <p> If the registrationId already exists, this method will update existed enrollments. Note that update the
     *     enrollment will not change the status of the device that was already registered using the old enrollment.
     *
     * <p> The follow code will update the provisioningStatus of the previous enrollment from <b>disabled</b> to <b>enabled</b>.
     * <pre>
     * {@code
     * // Enrollment information.
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
     *     // Get the content of the previous enrollment.
     *     Enrollment enrollment =  deviceProvisioningServiceClient.getIndividualEnrollment(REGISTRATION_ID);
     *
     *     // Change the provisioning status, from disabled to enabled
     *     enrollment.setProvisioningStatus(ProvisioningStatus.ENABLED);
     *
     *     // Update the individual enrollment information.
     *     Enrollment enrollmentResult =  deviceProvisioningServiceClient.createOrUpdateIndividualEnrollment(enrollment);
     * }
     * }
     * </pre>
     *
     * @see <a href="https://docs.microsoft.com/en-us/azure/iot-dps/">Azure IoT Hub Device Provisioning Service</a>
     * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
     *
     * @param enrollment the {@link Enrollment} object that describes the individual enrollment that will be created of
     *                   updated. It cannot be {@code null}.
     * @return An {@link Enrollment} object with the result of the create or update requested.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to create or update the enrollment.
     */
    public Enrollment createOrUpdateIndividualEnrollment(Enrollment enrollment) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_008: [The createOrUpdateIndividualEnrollment shall create a new Provisioning enrollment by calling the createOrUpdate in the individualEnrollmentManager.] */
        return individualEnrollmentManager.createOrUpdate(enrollment);
    }

    /**
     * Create, update or delete a set of individual device enrollments.
     *
     * <p> This API provide the means to do a single operation over multiple individual enrollments. A valid operation
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
     * // Enrollment information.
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
     *     // Create two new individual enrollment configurations.
     *     Attestation attestation = new TpmAttestation(TPM_ENDORSEMENT_KEY);
     *     Enrollment enrollment1 =
     *        new Enrollment(
     *             REGISTRATION_ID_1,
     *             attestation);
     *     enrollment1.setDeviceId(DEVICE_ID_1);
     *     enrollment1.setIotHubHostName(IOTHUB_HOST_NAME);
     *     enrollment1.setProvisioningStatus(ProvisioningStatus.DISABLED);
     *
     *     Enrollment enrollment2 =
     *        new Enrollment(
     *             REGISTRATION_ID_2,
     *             attestation);
     *     enrollment2.setDeviceId(DEVICE_ID_2);
     *     enrollment2.setIotHubHostName(IOTHUB_HOST_NAME);
     *     enrollment2.setProvisioningStatus(ProvisioningStatus.DISABLED);
     *
     *     // Add these 2 enrollments to a list of enrollments.
     *     List<Enrollment> enrollments = new LinkedList<>();
     *     enrollments.add(enrollment1);
     *     enrollments.add(enrollment2);
     *
     *     // Create these 2 new individual enrollment using the bulk operation.
     *     BulkOperationResult bulkOperationResult =  provisioningServiceClient.runBulkOperation(BulkOperationMode.create, enrollments);
     * }
     * }
     * </pre>
     *
     * @see <a href="https://docs.microsoft.com/en-us/azure/iot-dps/">Azure IoT Hub Device Provisioning Service</a>
     * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
     *
     * @param bulkOperationMode the {@link BulkOperationMode} that defines the single operation to do over the enrollments. It cannot be {@code null}.
     * @param enrollments the collection of {@link Enrollment} that contains the description of each individual enrollment. It cannot be {@code null} or empty.
     * @return A {@link BulkOperationResult} object with the result of operation for each enrollment.
     * @throws IllegalArgumentException if the provided parameters are not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the bulk operation.
     */
    public BulkOperationResult runBulkOperation(
            BulkOperationMode bulkOperationMode, Collection<Enrollment> enrollments)
            throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_009: [The runBulkOperation shall do a Provisioning operation over individual enrollment by calling the bulkOperation in the individualEnrollmentManager.] */
        return individualEnrollmentManager.bulkOperation(bulkOperationMode, enrollments);
    }

    /**
     * Retrieve the individual enrollment information.
     *
     * <p> This method will return the enrollment information for the provided registrationId. It will retrieve
     *     the correspondent individual enrollment from the Device Provisioning Service, and return it in the
     *     {@link Enrollment} object.
     *
     * <p> If the registrationId do not exists, this method will throw
     *     {@link ProvisioningServiceClientNotFoundException}.
     *     for more exceptions that this method can throw, please see
     *     {@link ProvisioningServiceClientExceptionManager}
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will get and print the information about the individual enrollment "registrationId-1".
     * <pre>
     * {@code
     * // Enrollment information.
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
     *     // Get the individual enrollment information.
     *     Enrollment enrollmentResult =  deviceProvisioningServiceClient.getIndividualEnrollment(REGISTRATION_ID);
     *     System.out.println(enrollmentResult.toString());
     * }
     * }
     * </pre>
     *
     * @param registrationId the {@code String} that identifies the individual enrollment. It cannot be {@code null} or empty.
     * @return The {@link Enrollment} with the content of the individual enrollment in the Provisioning Device Service.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the bulk operation.
     */
    public Enrollment getIndividualEnrollment(String registrationId) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_010: [The getIndividualEnrollment shall retrieve the individual enrollment information for the provided registrationId by calling the get in the individualEnrollmentManager.] */
        return individualEnrollmentManager.get(registrationId);
    }

    /**
     * Delete the individual enrollment information.
     *
     * <p> This method will remove the individual enrollment from the Device Provisioning Service using the
     *     provided {@link Enrollment} information. The Device Provisioning Service will care about the
     *     registrationId and the eTag on the enrollment. If you want to delete the enrollment regardless the
     *     eTag, you can set the {@code eTag="*"} into the enrollment, or use the {@link #deleteIndividualEnrollment(String)}
     *     passing only the registrationId.
     *
     * <p> Note that delete the enrollment will not remove the Device itself from the IotHub.
     *
     * <p> If the registrationId does not exists or the eTag not matches, this method will throw
     *     {@link ProvisioningServiceClientNotFoundException}.
     *     for more exceptions that this method can throw, please see
     *     {@link ProvisioningServiceClientExceptionManager}
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will delete the information about the individual enrollment "registrationId-1".
     * <pre>
     * {@code
     * // Enrollment information.
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
     *     // Get the individual enrollment information.
     *     Enrollment enrollmentResult =  deviceProvisioningServiceClient.getIndividualEnrollment(REGISTRATION_ID);
     *
     *     // Delete the individual enrollment information.
     *     deviceProvisioningServiceClient.deleteIndividualEnrollment(enrollmentResult);
     * }
     * }
     * </pre>
     *
     * @param enrollment the {@link Enrollment} that identifies the individual enrollment. It cannot be {@code null}.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the bulk operation.
     */
    public void deleteIndividualEnrollment(Enrollment enrollment) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_011: [The deleteIndividualEnrollment shall delete the individual enrollment for the provided enrollment by calling the delete in the individualEnrollmentManager.] */
        individualEnrollmentManager.delete(enrollment);
    }

    /**
     * Delete the individual enrollment information.
     *
     * <p> This method will remove the individual enrollment from the Device Provisioning Service using the
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
     * <p> The follow code will delete the information about the individual enrollment "registrationId-1".
     * <pre>
     * {@code
     * // Enrollment information.
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
     *     // Delete the individual enrollment information.
     *     deviceProvisioningServiceClient.deleteIndividualEnrollment(REGISTRATION_ID);
     * }
     * }
     * </pre>
     *
     * @param registrationId the {@code String} that identifies the individual enrollment. It cannot be {@code null} or empty.
     * @throws IllegalArgumentException if the provided registrationId is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the bulk operation.
     */
    public void deleteIndividualEnrollment(String registrationId) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_012: [The deleteIndividualEnrollment shall delete the individual enrollment for the provided registrationId by calling the delete in the individualEnrollmentManager.] */
        individualEnrollmentManager.delete(registrationId, null);
    }

    /**
     * Delete the individual enrollment information.
     *
     * <p> This method will remove the individual enrollment from the Device Provisioning Service using the
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
     * <p> The follow code will delete the information about the individual enrollment "registrationId-1" regardless the eTag.
     * <pre>
     * {@code
     * // Enrollment information.
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
     *     // Delete the individual enrollment information.
     *     deviceProvisioningServiceClient.deleteIndividualEnrollment(REGISTRATION_ID, ANY_ETAG);
     * }
     * }
     * </pre>
     *
     * @param registrationId the {@code String} that identifies the individual enrollment. It cannot be {@code null} or empty.
     * @param eTag the {@code String} with the Enrollment eTag. It can be {@code null} or empty.
     *             The Device Provisioning Service will ignore it in all of these cases.
     * @throws IllegalArgumentException if the provided registrationId is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the bulk operation.
     */
    public void deleteIndividualEnrollment(String registrationId, String eTag) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_013: [The deleteIndividualEnrollment shall delete the individual enrollment for the provided registrationId and etag by calling the delete in the individualEnrollmentManager.] */
        individualEnrollmentManager.delete(registrationId, eTag);
    }

    /**
     * Factory to create a individual enrollment query.
     *
     * <p> This method will create a new individual enrollment query for Device Provisioning Service and return it
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
     * Factory to create a individual enrollment query.
     *
     * <p> This method will create a new individual enrollment query for Device Provisioning Service and return it
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
     * @param enrollmentGroup the {@link EnrollmentGroup} object that describes the individual enrollment that will be created of updated.
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
     *     // Get the individual enrollment information.
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
     * <p> This method will return the registrationStatus for the provided id. It will retrieve
     *     the correspondent registrationStatus from the Device Provisioning Service, and return it in the
     *     {@link DeviceRegistrationStatus} object.
     *
     * <p> If the id do not exists, this method will throw
     *     {@link ProvisioningServiceClientNotFoundException}.
     *     for more exceptions that this method can throw, please see
     *     {@link ProvisioningServiceClientExceptionManager}
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will get and print the information about the registrationStatus "registrationId-1".
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
     *     // Get the registrationStatus information.
     *     DeviceRegistrationStatus registrationStatusResult =  deviceProvisioningServiceClient.getRegistrationStatus(REGISTRATION_ID);
     *     System.out.println(registrationStatusResult.toString());
     * }
     * }
     * </pre>
     *
     * @param id the {@code String} that identifies the registrationStatus. It cannot be {@code null} or empty.
     * @return The {@link DeviceRegistrationStatus} with the content of the registrationStatus in the Provisioning Device Service.
     * @throws ProvisioningServiceClientException if the Provisioning Device Service was not able to retrieve the
     *                                            registrationStatus information for the provided registrationId.
     */
    public DeviceRegistrationStatus getRegistrationStatus(String id) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_023: [The getRegistrationStatus shall retrieve the registrationStatus information for the provided id by calling the get in the registrationStatusManager.] */
        return registrationStatusManager.get(id);
    }

    /**
     * Delete the Registration Status information.
     *
     * <p> This method will remove the registrationStatus from the Device Provisioning Service using the
     *     provided {@link DeviceRegistrationStatus} information. The Device Provisioning Service will care about the
     *     id and the eTag on the deviceRegistrationStatus. If you want to delete the registrationStatus regardless the
     *     eTag, you can use the {@link #deleteRegistrationStatus(String)} passing only the id.
     *
     * <p> If the id does not exists or the eTag does not matches, this method will throw
     *     {@link ProvisioningServiceClientNotFoundException}.
     *     for more exceptions that this method can throw, please see
     *     {@link ProvisioningServiceClientExceptionManager}
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will delete the information about the registrationStatus "registrationId-1".
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
     *     DeviceRegistrationStatus registrationStatusResult =  deviceProvisioningServiceClient.getRegistrationStatus(REGISTRATION_ID);
     *
     *     // Delete the registration status information.
     *     deviceProvisioningServiceClient.deleteRegistrationStatus(registrationStatusResult);
     * }
     * }
     * </pre>
     *
     * @param deviceRegistrationStatus the {@link DeviceRegistrationStatus} that identifies the registrationStatus. It cannot be {@code null}.
     * @throws ProvisioningServiceClientException if the Provisioning Device Service was not able to delete the
     *                                            registration status information for the provided deviceRegistrationStatus.
     */
    public void deleteRegistrationStatus(DeviceRegistrationStatus deviceRegistrationStatus) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_024: [The deleteRegistrationStatus shall delete the registrationStatus for the provided deviceRegistrationStatus by calling the delete in the registrationStatusManager.] */
        registrationStatusManager.delete(deviceRegistrationStatus);
    }

    /**
     * Delete the registration status information.
     *
     * <p> This method will remove the registrationStatus from the Device Provisioning Service using the
     *     provided id. It will delete the registration status regardless the eTag. It means that this API
     *     correspond to the {@link #deleteRegistrationStatus(String, String)} with the {@code eTag="*"}.
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
     * // RegistrationStatus information.
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
     *     deviceProvisioningServiceClient.deleteRegistrationStatus(REGISTRATION_ID);
     * }
     * }
     * </pre>
     *
     * @param id the {@code String} that identifies the registrationStatus. It cannot be {@code null} or empty.
     * @throws ProvisioningServiceClientException if the Provisioning Device Service was not able to delete the
     *                                            registrationStatus information for the provided registrationId.
     */
    public void deleteRegistrationStatus(String id) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_025: [The deleteRegistrationStatus shall delete the registrationStatus for the provided id by calling the delete in the registrationStatusManager.] */
        registrationStatusManager.delete(id, null);
    }

    /**
     * Delete the registration status information.
     *
     * <p> This method will remove the registration status from the Device Provisioning Service using the
     *     provided id and eTag. If you want to delete the registration status regardless the eTag, you can
     *     use {@link #deleteRegistrationStatus(String)} or you can pass the eTag as {@code null}, empty, or
     *     {@code "*"}.
     *
     * <p> If the id does not exists or the eTag does not matches, this method will throw
     *     {@link ProvisioningServiceClientNotFoundException}.
     *     for more exceptions that this method can throw, please see
     *     {@link ProvisioningServiceClientExceptionManager}
     *
     * <p> <b>Sample:</b>
     * <p> The follow code will delete the information about the registrationStatus "registrationId-1" regardless the eTag.
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
     *     // Delete the registrationStatus information.
     *     deviceProvisioningServiceClient.deleteRegistrationStatus(REGISTRATION_ID, ANY_ETAG);
     * }
     * }
     * </pre>
     *
     * @param id the {@code String} that identifies the registrationStatus. It cannot be {@code null} or empty.
     * @param eTag the {@code String} with the registrationStatus eTag. It can be {@code null} or empty.
     *             The Device Provisioning Service will ignore it in all of these cases.
     * @throws ProvisioningServiceClientException if the Provisioning Device Service was not able to delete the
     *                                            registrationStatus information for the provided registrationId and eTag.
     */
    public void deleteRegistrationStatus(String id, String eTag) throws ProvisioningServiceClientException
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_026: [The deleteRegistrationStatus shall delete the registrationStatus for the provided id and eTag by calling the delete in the registrationStatusManager.] */
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
    public Query createEnrollmentGroupRegistrationStatusQuery(QuerySpecification querySpecification, String enrollmentGroupId)
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_027: [The createEnrollmentGroupRegistrationStatusQuery shall create a new registrationStatus query by calling the createQuery in the registrationStatusManager.] */
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
    public Query createEnrollmentGroupRegistrationStatusQuery(QuerySpecification querySpecification, String enrollmentGroupId, int pageSize)
    {
        /* SRS_PROVISIONING_SERVICE_CLIENT_21_028: [The createEnrollmentGroupRegistrationStatusQuery shall create a new registrationStatus query by calling the createQuery in the registrationStatusManager.] */
        return registrationStatusManager.createEnrollmentGroupQuery(querySpecification, enrollmentGroupId, pageSize);
    }
}
