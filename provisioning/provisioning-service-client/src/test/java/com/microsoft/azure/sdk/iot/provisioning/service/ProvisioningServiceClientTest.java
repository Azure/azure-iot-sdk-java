// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
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
    private TokenCredential mockedTokenCredential;

    @Mocked
    private AzureSasCredential mockedAzureSasCredential;

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

    private ProvisioningServiceClient createClientFromConnectionString()
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

    private static final String VALID_HOST_NAME = "testProvisioningHostName.azure.net";

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

    @Test (expected = IllegalArgumentException.class)
    public void factoryThrowsOnConnectionStringNull()
    {
        // arrange
        // act
        new ProvisioningServiceClient(null);

        // assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void factoryThrowsOnConnectionStringEmpty()
    {
        // arrange
        // act
        new ProvisioningServiceClient("");

        // assert
    }

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

    @Test
    public void tokenCredentialConstructorSucceed()
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                new ContractApiHttp(VALID_HOST_NAME, mockedTokenCredential);
                result = mockedContractApiHttp;
                Deencapsulation.invoke(IndividualEnrollmentManager.class, "createFromContractApiHttp", mockedContractApiHttp);
                result = mockedIndividualEnrollmentManager;
                Deencapsulation.invoke(EnrollmentGroupManager.class, "createFromContractApiHttp", mockedContractApiHttp);
                result = mockedEnrollmentGroupManager;
                Deencapsulation.invoke(RegistrationStatusManager.class, "createFromContractApiHttp", mockedContractApiHttp);
                result = mockedRegistrationStatusManager;
            }
        };

        //act
        ProvisioningServiceClient provisioningServiceClient = new ProvisioningServiceClient(VALID_HOST_NAME, mockedTokenCredential);

        //assert
        assertNotNull(provisioningServiceClient);
    }

    @Test (expected = IllegalArgumentException.class)
    public void tokenCredentialConstructorThrowsOnNullHostName()
    {
        //arrange
        TokenCredential credential = mockedTokenCredential;

        //act
        new ProvisioningServiceClient(null, credential);

        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void tokenCredentialConstructorThrowsOnEmptyHostName()
    {
        //arrange
        TokenCredential credential = mockedTokenCredential;

        //act
        new ProvisioningServiceClient("", credential);

        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void tokenCredentialConstructorThrowsOnNullCredential()
    {
        //arrange
        TokenCredential credential = null;

        //act
        new ProvisioningServiceClient(VALID_HOST_NAME, credential);

        //assert
    }

    @Test
    public void azureSasCredentialConstructorSucceed()
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                new ContractApiHttp(VALID_HOST_NAME, mockedAzureSasCredential);
                result = mockedContractApiHttp;
                Deencapsulation.invoke(IndividualEnrollmentManager.class, "createFromContractApiHttp", mockedContractApiHttp);
                result = mockedIndividualEnrollmentManager;
                Deencapsulation.invoke(EnrollmentGroupManager.class, "createFromContractApiHttp", mockedContractApiHttp);
                result = mockedEnrollmentGroupManager;
                Deencapsulation.invoke(RegistrationStatusManager.class, "createFromContractApiHttp", mockedContractApiHttp);
                result = mockedRegistrationStatusManager;
            }
        };

        //act
        ProvisioningServiceClient provisioningServiceClient = new ProvisioningServiceClient(VALID_HOST_NAME, mockedAzureSasCredential);

        //assert
        assertNotNull(provisioningServiceClient);
    }

    @Test (expected = IllegalArgumentException.class)
    public void azureSasCredentialConstructorThrowsOnNullHostName()
    {
        //arrange
        AzureSasCredential azureSasCredential = mockedAzureSasCredential;

        //act
        new ProvisioningServiceClient(null, azureSasCredential);

        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void azureSasCredentialConstructorThrowsOnEmptyHostName()
    {
        //arrange
        AzureSasCredential azureSasCredential = mockedAzureSasCredential;

        //act
        new ProvisioningServiceClient("", azureSasCredential);

        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void azureSasCredentialConstructorThrowsOnNullCredential()
    {
        //arrange
        AzureSasCredential azureSasCredential = null;

        //act
        new ProvisioningServiceClient(VALID_HOST_NAME, azureSasCredential);

        //assert
    }

    @Test
    public void createOrUpdateIndividualEnrollmentSucceed(
            @Mocked final IndividualEnrollment mockedIndividualEnrollment)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void runBulkEnrollmentOperationSucceed(
            @Mocked final IndividualEnrollment mockedIndividualEnrollment,
            @Mocked final BulkEnrollmentOperationResult mockedBulkEnrollmentOperationResult)
            throws ProvisioningServiceClientException
    {
        // arrange
        final List<IndividualEnrollment> individualEnrollments = new LinkedList<>();
        individualEnrollments.add(mockedIndividualEnrollment);
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void getIndividualEnrollmentSucceed(
            @Mocked final IndividualEnrollment mockedIndividualEnrollment)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "valid-registration-id";
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void deleteIndividualEnrollmentWithEnrollmentSucceed(
            @Mocked final IndividualEnrollment mockedIndividualEnrollment)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void deleteIndividualEnrollmentWithRegistrationIdSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "valid-registration-id";
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void deleteIndividualEnrollmentWithRegistrationIdAndEtagSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "valid-registration-id";
        final String eTag = "valid-eTag";
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void createIndividualEnrollmentQuerySucceed(
            @Mocked final QuerySpecification mockedQuerySpecification)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void createIndividualEnrollmentQueryWithPageSizeSucceed(
            @Mocked final QuerySpecification mockedQuerySpecification)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void createOrUpdateEnrollmentGroupSucceed(
            @Mocked final EnrollmentGroup mockedEnrollmentGroup)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void getEnrollmentGroupSucceed(
            @Mocked final EnrollmentGroup mockedEnrollmentGroup)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "valid-enrollmentGroupId";
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void deleteEnrollmentGroupWithEnrollmentGroupSucceed(
            @Mocked final EnrollmentGroup mockedEnrollmentGroup)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void deleteEnrollmentGroupWithEnrollmentGroupIdSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "valid-enrollmentGroupId";
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void deleteEnrollmentGroupWithEnrollmentGroupIdAndEtagSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "valid-enrollmentGroupId";
        final String eTag = "valid-eTag";
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void createEnrollmentGroupQuerySucceed(
            @Mocked final QuerySpecification mockedQuerySpecification)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void createEnrollmentGroupQueryWithPageSizeSucceed(
            @Mocked final QuerySpecification mockedQuerySpecification)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void getDeviceRegistrationStateSucceed(
            @Mocked final DeviceRegistrationState mockedDeviceRegistrationState)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "valid-id";
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void deleteDeviceRegistrationStateWithDeviceRegistrationStateSucceed(
            @Mocked final DeviceRegistrationState mockedDeviceRegistrationState)
            throws ProvisioningServiceClientException
    {
        // arrange
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void deleteDeviceRegistrationStateWithIdSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "valid-id";
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void deleteDeviceRegistrationStateWithIdAndEtagSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "valid-id";
        final String eTag = "valid-eTag";
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void createRegistrationStateQuerySucceed(
            @Mocked final QuerySpecification mockedQuerySpecification)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "valid-enrollmentGroupId";
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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

    @Test
    public void createRegistrationStateQueryWithPageSizeSucceed(
            @Mocked final QuerySpecification mockedQuerySpecification)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "valid-enrollmentGroupId";
        ProvisioningServiceClient provisioningServiceClient = createClientFromConnectionString();
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
