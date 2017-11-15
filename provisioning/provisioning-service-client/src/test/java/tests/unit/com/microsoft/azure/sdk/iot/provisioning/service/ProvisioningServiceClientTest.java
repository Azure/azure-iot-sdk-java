// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.service;

import com.microsoft.azure.sdk.iot.provisioning.service.*;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningConnectionString;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.contract.ContractApiHttp;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for Provisioning Service Client public API.
 * 100% methods, 100% lines covered
 */
public class ProvisioningServiceClientTest
{
    @Mocked
    private ProvisioningConnectionString mockedProvisioningConnectionString;

    @Mocked
    private ContractApiHttp mockedContractApiHttp;

    @Mocked
    private ProvisioningConnectionStringBuilder mockedProvisioningConnectionStringBuilder;

    @Mocked
    private IndividualEnrollmentManager mockedIndividualEnrollmentManager;

    @Mocked
    private EnrollmentGroupManager mockedEnrollmentGroupManager;

    @Mocked
    private RegistrationStatusManager mockedRegistrationStatusManager;

    private ProvisioningServiceClient createClient()
    {
        new NonStrictExpectations()
        {
            {
                ProvisioningConnectionStringBuilder.createConnectionString(PROVISIONING_CONNECTION_STRING);
                result = mockedProvisioningConnectionString;
                ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);
                result = mockedContractApiHttp;
                Deencapsulation.invoke(IndividualEnrollmentManager.class, "createFromContractApiHttp", mockedContractApiHttp);
                result = mockedIndividualEnrollmentManager;
                Deencapsulation.invoke(EnrollmentGroupManager.class, "createFromContractApiHttp", mockedContractApiHttp);
                result = mockedEnrollmentGroupManager;
                Deencapsulation.invoke(RegistrationStatusManager.class, "createFromContractApiHttp", mockedContractApiHttp);
                result = mockedRegistrationStatusManager;
            }
        };

        return ProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);
    }

    private static final String PROVISIONING_CONNECTION_STRING = "HostName=valid-host-name.azure-devices-provisioning.net;SharedAccessKeyName=valid-key-name;SharedAccessKey=0000000000000000000000000000000000000000000=";

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_001: [The createFromConnectionString shall create a new instance of this class using the provided connectionString.] */
    /* SRS_PROVISIONING_SERVICE_CLIENT_21_004: [The constructor shall create a new instance of the contractApiHttp class using the provided connectionString.] */
    /* SRS_PROVISIONING_SERVICE_CLIENT_21_005: [The constructor shall create a new instance of the IndividualEnrollmentManger.] */
    /* SRS_PROVISIONING_SERVICE_CLIENT_21_006: [The constructor shall create a new instance of the EnrollmentGroupManager.] */
    /* SRS_PROVISIONING_SERVICE_CLIENT_21_007: [The constructor shall create a new instance of the RegistrationStatusManager.] */
    @Test
    public void factorySucceed()
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                ProvisioningConnectionStringBuilder.createConnectionString(PROVISIONING_CONNECTION_STRING);
                result = mockedProvisioningConnectionString;
                times = 1;
                ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);
                result = mockedContractApiHttp;
                times = 1;
                Deencapsulation.invoke(IndividualEnrollmentManager.class, "createFromContractApiHttp", mockedContractApiHttp);
                result = mockedIndividualEnrollmentManager;
                times = 1;
                Deencapsulation.invoke(EnrollmentGroupManager.class, "createFromContractApiHttp", mockedContractApiHttp);
                result = mockedEnrollmentGroupManager;
                times = 1;
                Deencapsulation.invoke(RegistrationStatusManager.class, "createFromContractApiHttp", mockedContractApiHttp);
                result = mockedRegistrationStatusManager;
                times = 1;
            }
        };

        // act
        ProvisioningServiceClient provisioningServiceClient = ProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);

        // assert
        assertNotNull(provisioningServiceClient);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_002: [The constructor shall throws IllegalArgumentException if the provided connectionString is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void factoryThrowsOnConnectionStringNull()
    {
        // arrange
        // act
        ProvisioningServiceClient.createFromConnectionString(null);

        // assert
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_002: [The constructor shall throws IllegalArgumentException if the provided connectionString is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void factoryThrowsOnConnectionStringEmpty()
    {
        // arrange
        // act
        ProvisioningServiceClient.createFromConnectionString("");

        // assert
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_003: [The constructor shall throws IllegalArgumentException if the ProvisioningConnectionString or one of the inner Managers failed to create a new instance.] */
    @Test (expected = IllegalArgumentException.class)
    public void factoryThrowsOnProvisioningConnectionStringFail()
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                ProvisioningConnectionStringBuilder.createConnectionString(PROVISIONING_CONNECTION_STRING);
                result = new IllegalArgumentException();
                times = 1;
            }
        };

        // act
        ProvisioningServiceClient provisioningServiceClient = ProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);

        // assert
        assertNotNull(provisioningServiceClient);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_003: [The constructor shall throws IllegalArgumentException if the ProvisioningConnectionString or one of the inner Managers failed to create a new instance.] */
    @Test (expected = IllegalArgumentException.class)
    public void factoryThrowsOnContractApiHttpFail()
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                ProvisioningConnectionStringBuilder.createConnectionString(PROVISIONING_CONNECTION_STRING);
                result = mockedProvisioningConnectionString;
                times = 1;
                ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);
                result = new IllegalArgumentException();
                times = 1;
            }
        };

        // act
        ProvisioningServiceClient provisioningServiceClient = ProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);

        // assert
        assertNotNull(provisioningServiceClient);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_003: [The constructor shall throws IllegalArgumentException if the ProvisioningConnectionString or one of the inner Managers failed to create a new instance.] */
    @Test (expected = IllegalArgumentException.class)
    public void factoryThrowsOnIndividualEnrollmentManagerFail()
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                ProvisioningConnectionStringBuilder.createConnectionString(PROVISIONING_CONNECTION_STRING);
                result = mockedProvisioningConnectionString;
                times = 1;
                ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);
                result = mockedContractApiHttp;
                times = 1;
                Deencapsulation.invoke(IndividualEnrollmentManager.class, "createFromContractApiHttp", mockedContractApiHttp);
                result = new IllegalArgumentException();
                times = 1;
            }
        };

        // act
        ProvisioningServiceClient provisioningServiceClient = ProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);

        // assert
        assertNotNull(provisioningServiceClient);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_003: [The constructor shall throws IllegalArgumentException if the ProvisioningConnectionString or one of the inner Managers failed to create a new instance.] */
    @Test (expected = IllegalArgumentException.class)
    public void factoryThrowsOnEnrollmentGroupManagerFail()
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                ProvisioningConnectionStringBuilder.createConnectionString(PROVISIONING_CONNECTION_STRING);
                result = mockedProvisioningConnectionString;
                times = 1;
                ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);
                result = mockedContractApiHttp;
                times = 1;
                Deencapsulation.invoke(IndividualEnrollmentManager.class, "createFromContractApiHttp", mockedContractApiHttp);
                result = mockedIndividualEnrollmentManager;
                times = 1;
                Deencapsulation.invoke(EnrollmentGroupManager.class, "createFromContractApiHttp", mockedContractApiHttp);
                result = new IllegalArgumentException();
                times = 1;
            }
        };

        // act
        ProvisioningServiceClient provisioningServiceClient = ProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);

        // assert
        assertNotNull(provisioningServiceClient);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_003: [The constructor shall throws IllegalArgumentException if the ProvisioningConnectionString or one of the inner Managers failed to create a new instance.] */
    @Test (expected = IllegalArgumentException.class)
    public void factoryThrowsOnRegistrationStatusManagerFail()
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                ProvisioningConnectionStringBuilder.createConnectionString(PROVISIONING_CONNECTION_STRING);
                result = mockedProvisioningConnectionString;
                times = 1;
                ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);
                result = mockedContractApiHttp;
                times = 1;
                Deencapsulation.invoke(IndividualEnrollmentManager.class, "createFromContractApiHttp", mockedContractApiHttp);
                result = mockedIndividualEnrollmentManager;
                times = 1;
                Deencapsulation.invoke(EnrollmentGroupManager.class, "createFromContractApiHttp", mockedContractApiHttp);
                result = mockedEnrollmentGroupManager;
                times = 1;
                Deencapsulation.invoke(RegistrationStatusManager.class, "createFromContractApiHttp", mockedContractApiHttp);
                result = new IllegalArgumentException();
                times = 1;
            }
        };

        // act
        ProvisioningServiceClient provisioningServiceClient = ProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);

        // assert
        assertNotNull(provisioningServiceClient);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_008: [The createOrUpdateIndividualEnrollment shall create a new Provisioning enrollment by calling the createOrUpdate in the individualEnrollmentManager.] */
    @Test
    public void createOrUpdateIndividualEnrollmentSucceed(
            @Mocked final Enrollment mockedEnrollment)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedIndividualEnrollmentManager, "createOrUpdate", mockedEnrollment);
                result = mockedEnrollment;
                times = 1;
            }
        };

        // act
        Enrollment result = provisioningServiceClient.createOrUpdateIndividualEnrollment(mockedEnrollment);

        // assert
        assertNotNull(result);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_009: [The runBulkOperation shall do a Provisioning operation over individual enrollment by calling the bulkOperation in the individualEnrollmentManager.] */
    @Test
    public void runBulkOperationSucceed(
            @Mocked final Enrollment mockedEnrollment,
            @Mocked final BulkOperationResult mockedBulkOperationResult)
            throws ProvisioningServiceClientException
    {
        // arrange
        final List<Enrollment> enrollments = new LinkedList<>();
        enrollments.add(mockedEnrollment);
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedIndividualEnrollmentManager, "bulkOperation", BulkOperationMode.CREATE, enrollments);
                result = mockedBulkOperationResult;
                times = 1;
            }
        };

        // act
        BulkOperationResult result = provisioningServiceClient.runBulkOperation(BulkOperationMode.CREATE, enrollments);

        // assert
        assertNotNull(result);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_010: [The getIndividualEnrollment shall retrieve the individual enrollment information for the provided registrationId by calling the get in the individualEnrollmentManager.] */
    @Test
    public void getIndividualEnrollmentSucceed(
            @Mocked final Enrollment mockedEnrollment)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "valid-registration-id";
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedIndividualEnrollmentManager, "get", registrationId);
                result = mockedEnrollment;
                times = 1;
            }
        };

        // act
        Enrollment result = provisioningServiceClient.getIndividualEnrollment(registrationId);

        // assert
        assertNotNull(result);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_011: [The deleteIndividualEnrollment shall delete the individual enrollment for the provided enrollment by calling the delete in the individualEnrollmentManager.] */
    @Test
    public void deleteIndividualEnrollmentWithEnrollmentSucceed(
            @Mocked final Enrollment mockedEnrollment)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedIndividualEnrollmentManager, "delete", mockedEnrollment);
                times = 1;
            }
        };

        // act
        provisioningServiceClient.deleteIndividualEnrollment(mockedEnrollment);

        // assert
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_012: [The deleteIndividualEnrollment shall delete the individual enrollment for the provided registrationId by calling the delete in the individualEnrollmentManager.] */
    @Test
    public void deleteIndividualEnrollmentWithRegistrationIdSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "valid-registration-id";
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedIndividualEnrollmentManager, "delete", new Class[]{String.class, String.class}, registrationId, null);
                times = 1;
            }
        };

        // act
        provisioningServiceClient.deleteIndividualEnrollment(registrationId);

        // assert
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_013: [The deleteIndividualEnrollment shall delete the individual enrollment for the provided registrationId and etag by calling the delete in the individualEnrollmentManager.] */
    @Test
    public void deleteIndividualEnrollmentWithRegistrationIdAndEtagSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "valid-registration-id";
        final String eTag = "valid-eTag";
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedIndividualEnrollmentManager, "delete", registrationId, eTag);
                times = 1;
            }
        };

        // act
        provisioningServiceClient.deleteIndividualEnrollment(registrationId, eTag);

        // assert
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_014: [The createIndividualEnrollmentQuery shall create a new individual enrolment query by calling the createQuery in the individualEnrollmentManager.] */
    @Test
    public void createIndividualEnrollmentQuerySucceed(
            @Mocked final QuerySpecification mockedQuerySpecification)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedIndividualEnrollmentManager, "createQuery", mockedQuerySpecification, 0);
                times = 1;
            }
        };

        // act
        Query query = provisioningServiceClient.createIndividualEnrollmentQuery(mockedQuerySpecification);

        // assert
        assertNotNull(query);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_015: [The createIndividualEnrollmentQuery shall create a new individual enrolment query by calling the createQuery in the individualEnrollmentManager.] */
    @Test
    public void createIndividualEnrollmentQueryWithPageSizeSucceed(
            @Mocked final QuerySpecification mockedQuerySpecification)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedIndividualEnrollmentManager, "createQuery", mockedQuerySpecification, 10);
                times = 1;
            }
        };

        // act
        Query query = provisioningServiceClient.createIndividualEnrollmentQuery(mockedQuerySpecification, 10);

        // assert
        assertNotNull(query);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_016: [The createOrUpdateEnrollmentGroup shall create a new Provisioning enrollmentGroup by calling the createOrUpdate in the enrollmentGroupManager.] */
    @Test
    public void createOrUpdateEnrollmentGroupSucceed(
            @Mocked final EnrollmentGroup mockedEnrollmentGroup)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedEnrollmentGroupManager, "createOrUpdate", mockedEnrollmentGroup);
                result = mockedEnrollmentGroup;
                times = 1;
            }
        };

        // act
        EnrollmentGroup result = provisioningServiceClient.createOrUpdateEnrollmentGroup(mockedEnrollmentGroup);

        // assert
        assertNotNull(result);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_017: [The getEnrollmentGroup shall retrieve the enrollmentGroup information for the provided enrollmentGroupId by calling the get in the enrollmentGroupManager.] */
    @Test
    public void getEnrollmentGroupSucceed(
            @Mocked final EnrollmentGroup mockedEnrollmentGroup)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "valid-enrollmentGroupId";
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedEnrollmentGroupManager, "get", enrollmentGroupId);
                result = mockedEnrollmentGroup;
                times = 1;
            }
        };

        // act
        EnrollmentGroup result = provisioningServiceClient.getEnrollmentGroup(enrollmentGroupId);

        // assert
        assertNotNull(result);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_018: [The deleteEnrollmentGroup shall delete the enrollmentGroup for the provided enrollmentGroup by calling the delete in the enrollmentGroupManager.] */
    @Test
    public void deleteEnrollmentGroupWithEnrollmentGroupSucceed(
            @Mocked final EnrollmentGroup mockedEnrollmentGroup)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedEnrollmentGroupManager, "delete", mockedEnrollmentGroup);
                times = 1;
            }
        };

        // act
        provisioningServiceClient.deleteEnrollmentGroup(mockedEnrollmentGroup);

        // assert
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_019: [The deleteEnrollmentGroup shall delete the enrollmentGroup for the provided enrollmentGroupId by calling the delete in the enrollmentGroupManager.] */
    @Test
    public void deleteEnrollmentGroupWithEnrollmentGroupIdSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "valid-enrollmentGroupId";
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedEnrollmentGroupManager, "delete", new Class[]{String.class, String.class}, enrollmentGroupId, null);
                times = 1;
            }
        };

        // act
        provisioningServiceClient.deleteEnrollmentGroup(enrollmentGroupId);

        // assert
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_020: [The deleteEnrollmentGroup shall delete the enrollmentGroup for the provided enrollmentGroupId and eTag by calling the delete in the enrollmentGroupManager.] */
    @Test
    public void deleteEnrollmentGroupWithEnrollmentGroupIdAndEtagSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "valid-enrollmentGroupId";
        final String eTag = "valid-eTag";
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedEnrollmentGroupManager, "delete", enrollmentGroupId, eTag);
                times = 1;
            }
        };

        // act
        provisioningServiceClient.deleteEnrollmentGroup(enrollmentGroupId, eTag);

        // assert
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_021: [The createEnrollmentGroupQuery shall create a new enrolmentGroup query by calling the createQuery in the enrollmentGroupManager.] */
    @Test
    public void createEnrollmentGroupQuerySucceed(
            @Mocked final QuerySpecification mockedQuerySpecification)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedEnrollmentGroupManager, "createQuery", mockedQuerySpecification, 0);
                times = 1;
            }
        };

        // act
        Query query = provisioningServiceClient.createEnrollmentGroupQuery(mockedQuerySpecification);

        // assert
        assertNotNull(query);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_022: [The createEnrollmentGroupQuery shall create a new enrolmentGroup query by calling the createQuery in the enrollmentGroupManager.] */
    @Test
    public void createEnrollmentGroupQueryWithPageSizeSucceed(
            @Mocked final QuerySpecification mockedQuerySpecification)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedEnrollmentGroupManager, "createQuery", mockedQuerySpecification, 10);
                times = 1;
            }
        };

        // act
        Query query = provisioningServiceClient.createEnrollmentGroupQuery(mockedQuerySpecification, 10);

        // assert
        assertNotNull(query);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_023: [The getRegistrationStatus shall retrieve the registrationStatus information for the provided id by calling the get in the registrationStatusManager.] */
    @Test
    public void getRegistrationStatusSucceed(
            @Mocked final DeviceRegistrationStatus mockedDeviceRegistrationStatus)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "valid-id";
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedRegistrationStatusManager, "get", id);
                result = mockedDeviceRegistrationStatus;
                times = 1;
            }
        };

        // act
        DeviceRegistrationStatus result = provisioningServiceClient.getRegistrationStatus(id);

        // assert
        assertNotNull(result);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_024: [The deleteRegistrationStatus shall delete the registrationStatus for the provided deviceRegistrationStatus by calling the delete in the registrationStatusManager.] */
    @Test
    public void deleteRegistrationStatusWithDeviceRegistrationStatusSucceed(
            @Mocked final DeviceRegistrationStatus mockedDeviceRegistrationStatus)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedRegistrationStatusManager, "delete", mockedDeviceRegistrationStatus);
                times = 1;
            }
        };

        // act
        provisioningServiceClient.deleteRegistrationStatus(mockedDeviceRegistrationStatus);

        // assert
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_025: [The deleteRegistrationStatus shall delete the registrationStatus for the provided id by calling the delete in the registrationStatusManager.] */
    @Test
    public void deleteRegistrationStatusWithIdSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "valid-id";
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedRegistrationStatusManager, "delete", new Class[]{String.class, String.class}, id, null);
                times = 1;
            }
        };

        // act
        provisioningServiceClient.deleteRegistrationStatus(id);

        // assert
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_026: [The deleteRegistrationStatus shall delete the registrationStatus for the provided id and eTag by calling the delete in the registrationStatusManager.] */
    @Test
    public void deleteRegistrationStatusWithIdAndEtagSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "valid-id";
        final String eTag = "valid-eTag";
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedRegistrationStatusManager, "delete", id, eTag);
                times = 1;
            }
        };

        // act
        provisioningServiceClient.deleteRegistrationStatus(id, eTag);

        // assert
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_027: [The createEnrollmentGroupRegistrationStatusQuery shall create a new registrationStatus query by calling the createQuery in the registrationStatusManager.] */
    @Test
    public void createRegistrationStatusQuerySucceed(
            @Mocked final QuerySpecification mockedQuerySpecification)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "valid-enrollmentGroupId";
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedRegistrationStatusManager, "createEnrollmentGroupQuery", mockedQuerySpecification, enrollmentGroupId, 0);
                times = 1;
            }
        };

        // act
        Query query = provisioningServiceClient.createEnrollmentGroupRegistrationStatusQuery(mockedQuerySpecification, enrollmentGroupId);

        // assert
        assertNotNull(query);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_028: [The createEnrollmentGroupRegistrationStatusQuery shall create a new registrationStatus query by calling the createQuery in the registrationStatusManager.] */
    @Test
    public void createRegistrationStatusQueryWithPageSizeSucceed(
            @Mocked final QuerySpecification mockedQuerySpecification)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "valid-enrollmentGroupId";
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedRegistrationStatusManager, "createEnrollmentGroupQuery", mockedQuerySpecification, enrollmentGroupId, 10);
                times = 1;
            }
        };

        // act
        Query query = provisioningServiceClient.createEnrollmentGroupRegistrationStatusQuery(mockedQuerySpecification, enrollmentGroupId,10);

        // assert
        assertNotNull(query);
    }
}
