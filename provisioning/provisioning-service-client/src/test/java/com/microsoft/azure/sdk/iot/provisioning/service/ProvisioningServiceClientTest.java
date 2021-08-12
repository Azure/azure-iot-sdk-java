// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service;

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

        return new ProvisioningServiceClient(PROVISIONING_CONNECTION_STRING);
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
        ProvisioningServiceClient provisioningServiceClient = new ProvisioningServiceClient(PROVISIONING_CONNECTION_STRING);

        // assert
        assertNotNull(provisioningServiceClient);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_002: [The constructor shall throw IllegalArgumentException if the provided connectionString is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void factoryThrowsOnConnectionStringNull()
    {
        // arrange
        // act
        new ProvisioningServiceClient(null);

        // assert
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_002: [The constructor shall throw IllegalArgumentException if the provided connectionString is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void factoryThrowsOnConnectionStringEmpty()
    {
        // arrange
        // act
        new ProvisioningServiceClient("");

        // assert
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_003: [The constructor shall throw IllegalArgumentException if the ProvisioningConnectionString or one of the inner Managers failed to create a new instance.] */
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
        ProvisioningServiceClient provisioningServiceClient = new ProvisioningServiceClient(PROVISIONING_CONNECTION_STRING);

        // assert
        assertNotNull(provisioningServiceClient);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_003: [The constructor shall throw IllegalArgumentException if the ProvisioningConnectionString or one of the inner Managers failed to create a new instance.] */
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
        ProvisioningServiceClient provisioningServiceClient = new ProvisioningServiceClient(PROVISIONING_CONNECTION_STRING);

        // assert
        assertNotNull(provisioningServiceClient);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_003: [The constructor shall throw IllegalArgumentException if the ProvisioningConnectionString or one of the inner Managers failed to create a new instance.] */
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
        ProvisioningServiceClient provisioningServiceClient = new ProvisioningServiceClient(PROVISIONING_CONNECTION_STRING);

        // assert
        assertNotNull(provisioningServiceClient);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_003: [The constructor shall throw IllegalArgumentException if the ProvisioningConnectionString or one of the inner Managers failed to create a new instance.] */
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
        ProvisioningServiceClient provisioningServiceClient = new ProvisioningServiceClient(PROVISIONING_CONNECTION_STRING);

        // assert
        assertNotNull(provisioningServiceClient);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_003: [The constructor shall throw IllegalArgumentException if the ProvisioningConnectionString or one of the inner Managers failed to create a new instance.] */
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
        ProvisioningServiceClient provisioningServiceClient = new ProvisioningServiceClient(PROVISIONING_CONNECTION_STRING);

        // assert
        assertNotNull(provisioningServiceClient);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_008: [The createOrUpdateIndividualEnrollment shall create a new Provisioning enrollment by calling the createOrUpdate in the individualEnrollmentManager.] */
    @Test
    public void createOrUpdateIndividualEnrollmentSucceed(
            @Mocked final IndividualEnrollment mockedIndividualEnrollment)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedIndividualEnrollmentManager, "createOrUpdate", mockedIndividualEnrollment);
                result = mockedIndividualEnrollment;
                times = 1;
            }
        };

        // act
        IndividualEnrollment result = provisioningServiceClient.createOrUpdateIndividualEnrollment(mockedIndividualEnrollment);

        // assert
        assertNotNull(result);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_009: [The runBulkEnrollmentOperation shall do a Provisioning operation over individualEnrollment by calling the bulkOperation in the individualEnrollmentManager.] */
    @Test
    public void runBulkEnrollmentOperationSucceed(
            @Mocked final IndividualEnrollment mockedIndividualEnrollment,
            @Mocked final BulkEnrollmentOperationResult mockedBulkEnrollmentOperationResult)
            throws ProvisioningServiceClientException
    {
        // arrange
        final List<IndividualEnrollment> individualEnrollments = new LinkedList<>();
        individualEnrollments.add(mockedIndividualEnrollment);
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedIndividualEnrollmentManager, "bulkOperation", BulkOperationMode.CREATE, individualEnrollments);
                result = mockedBulkEnrollmentOperationResult;
                times = 1;
            }
        };

        // act
        BulkEnrollmentOperationResult result = provisioningServiceClient.runBulkEnrollmentOperation(BulkOperationMode.CREATE, individualEnrollments);

        // assert
        assertNotNull(result);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_010: [The getIndividualEnrollment shall retrieve the individualEnrollment information for the provided registrationId by calling the get in the individualEnrollmentManager.] */
    @Test
    public void getIndividualEnrollmentSucceed(
            @Mocked final IndividualEnrollment mockedIndividualEnrollment)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "valid-registration-id";
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedIndividualEnrollmentManager, "get", registrationId);
                result = mockedIndividualEnrollment;
                times = 1;
            }
        };

        // act
        IndividualEnrollment result = provisioningServiceClient.getIndividualEnrollment(registrationId);

        // assert
        assertNotNull(result);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_011: [The deleteIndividualEnrollment shall delete the individualEnrollment for the provided enrollment by calling the delete in the individualEnrollmentManager.] */
    @Test
    public void deleteIndividualEnrollmentWithEnrollmentSucceed(
            @Mocked final IndividualEnrollment mockedIndividualEnrollment)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedIndividualEnrollmentManager, "delete", mockedIndividualEnrollment);
                times = 1;
            }
        };

        // act
        provisioningServiceClient.deleteIndividualEnrollment(mockedIndividualEnrollment);

        // assert
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_012: [The deleteIndividualEnrollment shall delete the individualEnrollment for the provided registrationId by calling the delete in the individualEnrollmentManager.] */
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

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_013: [The deleteIndividualEnrollment shall delete the individualEnrollment for the provided registrationId and etag by calling the delete in the individualEnrollmentManager.] */
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

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_023: [The getDeviceRegistrationState shall retrieve the deviceRegistrationState information for the provided id by calling the get in the registrationStatusManager.] */
    @Test
    public void getDeviceRegistrationStateSucceed(
            @Mocked final DeviceRegistrationState mockedDeviceRegistrationState)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "valid-id";
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedRegistrationStatusManager, "get", id);
                result = mockedDeviceRegistrationState;
                times = 1;
            }
        };

        // act
        DeviceRegistrationState result = provisioningServiceClient.getDeviceRegistrationState(id);

        // assert
        assertNotNull(result);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_024: [The deleteDeviceRegistrationState shall delete the deviceRegistrationState for the provided DeviceRegistrationState by calling the delete in the registrationStatusManager.] */
    @Test
    public void deleteDeviceRegistrationStateWithDeviceRegistrationStateSucceed(
            @Mocked final DeviceRegistrationState mockedDeviceRegistrationState)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClient();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedRegistrationStatusManager, "delete", mockedDeviceRegistrationState);
                times = 1;
            }
        };

        // act
        provisioningServiceClient.deleteDeviceRegistrationState(mockedDeviceRegistrationState);

        // assert
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_025: [The deleteDeviceRegistrationState shall delete the deviceRegistrationState for the provided id by calling the delete in the registrationStatusManager.] */
    @Test
    public void deleteDeviceRegistrationStateWithIdSucceed()
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
        provisioningServiceClient.deleteDeviceRegistrationState(id);

        // assert
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_026: [The deleteDeviceRegistrationState shall delete the deviceRegistrationState for the provided id and eTag by calling the delete in the registrationStatusManager.] */
    @Test
    public void deleteDeviceRegistrationStateWithIdAndEtagSucceed()
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
        provisioningServiceClient.deleteDeviceRegistrationState(id, eTag);

        // assert
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_027: [The createEnrollmentGroupRegistrationStateQuery shall create a new deviceRegistrationState query by calling the createQuery in the registrationStatusManager.] */
    @Test
    public void createRegistrationStateQuerySucceed(
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
        Query query = provisioningServiceClient.createEnrollmentGroupRegistrationStateQuery(mockedQuerySpecification, enrollmentGroupId);

        // assert
        assertNotNull(query);
    }

    /* SRS_PROVISIONING_SERVICE_CLIENT_21_028: [The createEnrollmentGroupRegistrationStateQuery shall create a new deviceRegistrationState query by calling the createQuery in the registrationStatusManager.] */
    @Test
    public void createRegistrationStateQueryWithPageSizeSucceed(
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
        Query query = provisioningServiceClient.createEnrollmentGroupRegistrationStateQuery(mockedQuerySpecification, enrollmentGroupId,10);

        // assert
        assertNotNull(query);
    }
}
