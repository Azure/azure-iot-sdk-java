// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service;

import com.microsoft.azure.sdk.iot.deps.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpResponse;
import com.microsoft.azure.sdk.iot.provisioning.service.*;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.contract.ContractApiHttp;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.*;
import mockit.*;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for Registration Status Manager.
 * 100% methods, 100% lines covered
 */
public class RegistrationStatusManagerTest
{
    @Mocked
    private ContractApiHttp mockedContractApiHttp;

    @Mocked
    private HttpResponse mockedHttpResponse;

    RegistrationStatusManager createRegistrationStatusManager()
    {
        return Deencapsulation.invoke(
                RegistrationStatusManager.class, "createFromContractApiHttp",
                new Class[]{ContractApiHttp.class}, mockedContractApiHttp);

    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_001: [The constructor shall throw IllegalArgumentException if the provided ContractApiHttp is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNull()
    {
        // arrange
        // act
        Deencapsulation.newInstance(RegistrationStatusManager.class, new Class[]{ContractApiHttp.class}, (ContractApiHttp)null);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_004: [The factory shall create a new instance of this.] */
    @Test
    public void factoryCreatesNewInstanceSucceed()
    {
        // arrange
        // act
        RegistrationStatusManager registrationStatusManager = Deencapsulation.invoke(
                RegistrationStatusManager.class, "createFromContractApiHttp",
                new Class[]{ContractApiHttp.class}, mockedContractApiHttp);

        // assert
        assertNotNull(registrationStatusManager);
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_005: [The get shall throw IllegalArgumentException if the provided id is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void getThrowsOnNullId() throws ProvisioningServiceClientException
    {
        // arrange
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();

        // act
        Deencapsulation.invoke(registrationStatusManager, "get", new Class[] {String.class}, (String)null);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_005: [The get shall throw IllegalArgumentException if the provided id is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void getThrowsOnEmptyId() throws ProvisioningServiceClientException
    {
        // arrange
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();

        // act
        Deencapsulation.invoke(registrationStatusManager, "get", new Class[] {String.class}, (String)"");

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_006: [The get shall send a Http request for the path `registrations/[id]`.] */
    /* SRS_REGISTRATION_STATUS_MANAGER_21_007: [The get shall send a Http request with a Http verb `GET`.] */
    /* SRS_REGISTRATION_STATUS_MANAGER_21_010: [The get shall return a DeviceRegistrationState object created from the body of the response for the Http request .] */
    @Test
    public void getRequestSucceed(
            @Mocked final DeviceRegistrationState mockedDeviceRegistrationState)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "id-1";
        final String registrationPath = "registrations/" + id;
        final String resultPayload = "validJson";
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.GET, registrationPath, null, "");
                result = mockedHttpResponse;
                times = 1;
                mockedHttpResponse.getBody();
                result = resultPayload.getBytes(StandardCharsets.UTF_8);
                times = 1;
                Deencapsulation.newInstance(DeviceRegistrationState.class, resultPayload);
                result = mockedDeviceRegistrationState;
                times = 1;
            }
        };

        // act
        DeviceRegistrationState response = Deencapsulation.invoke(registrationStatusManager, "get", new Class[] {String.class}, id);

        // assert
        assertNotNull(response);
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_028: [The get shall throw ProvisioningServiceClientServiceException if the heepResponse contains a null body.] */
    @Test (expected = ProvisioningServiceClientServiceException.class)
    public void getRequestThrowsOnNullBody()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "id-1";
        final String registrationPath = "registrations/" + id;
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.GET, registrationPath, null, "");
                result = mockedHttpResponse;
                times = 1;
                mockedHttpResponse.getBody();
                result = null;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(registrationStatusManager, "get", new Class[] {String.class}, id);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_008: [The get shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientTransportException.class)
    public void getRequestTransportFailed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "id-1";
        final String registrationPath = "registrations/" + id;
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.GET, registrationPath, null, "");
                result = new ProvisioningServiceClientTransportException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(registrationStatusManager, "get", new Class[] {String.class}, id);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_009: [The get shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientException.class)
    public void getRequestServiceReportedFail()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "id-1";
        final String registrationPath = "registrations/" + id;
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.GET, registrationPath, null, "");
                result = new ProvisioningServiceClientBadFormatException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(registrationStatusManager, "get", new Class[] {String.class}, id);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_011: [The delete shall throw IllegalArgumentException if the provided DeviceRegistrationState is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void deleteDeviceRegistrationStatusThrowsOnNullDeviceRegistrationState() throws ProvisioningServiceClientException
    {
        // arrange
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();

        // act
        Deencapsulation.invoke(registrationStatusManager, "delete", new Class[] {DeviceRegistrationState.class}, (DeviceRegistrationState)null);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_012: [The delete shall send a Http request for the path `registrations/[id]`.] */
    /* SRS_REGISTRATION_STATUS_MANAGER_21_013: [If the DeviceRegistrationState contains eTag, the delete shall send a Http request with `If-Match` the eTag in the header.] */
    /* SRS_REGISTRATION_STATUS_MANAGER_21_014: [The delete shall send a Http request with a Http verb `DELETE`.] */
    @Test
    public void deleteDeviceRegistrationStatusRequestWithEtagSucceed(
            @Mocked final DeviceRegistrationState mockedDeviceRegistrationState)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "id-1";
        final String eTag = "validETag";
        final String registrationPath = "registrations/" + id;
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();
        new StrictExpectations()
        {
            {
                mockedDeviceRegistrationState.getRegistrationId();
                result = id;
                times = 1;
                mockedDeviceRegistrationState.getEtag();
                result = eTag;
                times = 2;
                mockedContractApiHttp.request(HttpMethod.DELETE, registrationPath, (Map)any, "");
                result = mockedHttpResponse;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(registrationStatusManager, "delete", new Class[] {DeviceRegistrationState.class}, mockedDeviceRegistrationState);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_013: [If the DeviceRegistrationState contains eTag, the delete shall send a Http request with `If-Match` the eTag in the header.] */
    @Test
    public void deleteDeviceRegistrationStatusRequestWithoutEtagSucceed(
            @Mocked final DeviceRegistrationState mockedDeviceRegistrationState)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "id-1";
        final String registrationPath = "registrations/" + id;
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();
        new StrictExpectations()
        {
            {
                mockedDeviceRegistrationState.getRegistrationId();
                result = id;
                times = 1;
                mockedDeviceRegistrationState.getEtag();
                result = null;
                times = 1;
                mockedContractApiHttp.request(HttpMethod.DELETE, registrationPath, (Map)any, "");
                result = mockedHttpResponse;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(registrationStatusManager, "delete", new Class[] {DeviceRegistrationState.class}, mockedDeviceRegistrationState);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_015: [The delete shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientTransportException.class)
    public void deleteDeviceRegistrationStatusRequestTransportFailed(
            @Mocked final DeviceRegistrationState mockedDeviceRegistrationState)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "id-1";
        final String eTag = "validETag";
        final String registrationPath = "registrations/" + id;
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();
        new NonStrictExpectations()
        {
            {
                mockedDeviceRegistrationState.getRegistrationId();
                result = id;
                mockedDeviceRegistrationState.getEtag();
                result = eTag;
                mockedContractApiHttp.request(HttpMethod.DELETE, registrationPath, (Map)any, "");
                result = new ProvisioningServiceClientTransportException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(registrationStatusManager, "delete", new Class[] {DeviceRegistrationState.class}, mockedDeviceRegistrationState);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_016: [The delete shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientException.class)
    public void deleteRegistrationServiceReportedFail(
            @Mocked final DeviceRegistrationState mockedDeviceRegistrationState)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "id-1";
        final String eTag = "validETag";
        final String registrationPath = "registrations/" + id;
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();
        new NonStrictExpectations()
        {
            {
                mockedDeviceRegistrationState.getRegistrationId();
                result = id;
                mockedDeviceRegistrationState.getEtag();
                result = eTag;
                mockedContractApiHttp.request(HttpMethod.DELETE, registrationPath, (Map)any, "");
                result = new ProvisioningServiceClientBadFormatException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(registrationStatusManager, "delete", new Class[] {DeviceRegistrationState.class}, mockedDeviceRegistrationState);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_017: [The delete shall throw IllegalArgumentException if the provided id is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void deleteIdAndETagThrowsOnNullId() throws ProvisioningServiceClientException
    {
        // arrange
        final String eTag = "validEtag";
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();

        // act
        Deencapsulation.invoke(registrationStatusManager, "delete", new Class[] {String.class, String.class}, (String)null, eTag);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_017: [The delete shall throw IllegalArgumentException if the provided id is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void deleteIdAndETagThrowsOnEmptyId() throws ProvisioningServiceClientException
    {
        // arrange
        final String eTag = "validEtag";
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();

        // act
        Deencapsulation.invoke(registrationStatusManager, "delete", new Class[] {String.class, String.class}, (String)"", eTag);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_018: [The delete shall send a Http request for the path `registrations/[id]`.] */
    /* SRS_REGISTRATION_STATUS_MANAGER_21_019: [If the eTag is not null or empty, the delete shall send a Http request with `If-Match` the eTag in the header.] */
    /* SRS_REGISTRATION_STATUS_MANAGER_21_020: [The delete shall send a Http request with a Http verb `DELETE`.] */
    @Test
    public void deleteIdAndETagRequestWithEtagSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "id-1";
        final String eTag = "validETag";
        final String registrationPath = "registrations/" + id;
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.DELETE, registrationPath, (Map)any, "");
                result = mockedHttpResponse;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(registrationStatusManager, "delete", new Class[] {String.class, String.class}, id, eTag);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_019: [If the eTag is not null or empty, the delete shall send a Http request with `If-Match` the eTag in the header.] */
    @Test
    public void deleteIdAndETagRequestWithNullEtagSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "id-1";
        final String registrationPath = "registrations/" + id;
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.DELETE, registrationPath, (Map)any, "");
                result = mockedHttpResponse;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(registrationStatusManager, "delete", new Class[] {String.class, String.class}, id, null);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_019: [If the eTag is not null or empty, the delete shall send a Http request with `If-Match` the eTag in the header.] */
    @Test
    public void deleteIdAndETagRequestWithEmptyEtagSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "id-1";
        final String registrationPath = "registrations/" + id;
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.DELETE, registrationPath, (Map)any, "");
                result = mockedHttpResponse;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(registrationStatusManager, "delete", new Class[] {String.class, String.class}, id, "");

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_021: [The delete shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientTransportException.class)
    public void deleteIdAndETagRequestTransportFailed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "id-1";
        final String eTag = "validETag";
        final String registrationPath = "registrations/" + id;
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.DELETE, registrationPath, (Map)any, "");
                result = new ProvisioningServiceClientTransportException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(registrationStatusManager, "delete", new Class[] {String.class, String.class}, id, eTag);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_022: [The delete shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientException.class)
    public void deleteIdAndETagRequestServiceReportedFail()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String id = "id-1";
        final String eTag = "validETag";
        final String registrationPath = "registrations/" + id;
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.DELETE, registrationPath, (Map)any, "");
                result = new ProvisioningServiceClientBadFormatException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(registrationStatusManager, "delete", new Class[] {String.class, String.class}, id, eTag);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_023: [The createEnrollmentGroupQuery shall throw IllegalArgumentException if the provided querySpecification is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void createEnrollmentGroupQueryThrowsOnNullQuerySpecification() throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();

        // act
        Deencapsulation.invoke(registrationStatusManager, "createEnrollmentGroupQuery", new Class[] {QuerySpecification.class, String.class, Integer.class},
                null, enrollmentGroupId, 0);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_024: [The createEnrollmentGroupQuery shall throw IllegalArgumentException if the provided enrollmentGroupId is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void createEnrollmentGroupQueryThrowsOnNullEnrollmentGroupId(@Mocked final QuerySpecification mockedQuerySpecification) throws ProvisioningServiceClientException
    {
        // arrange
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();

        // act
        Deencapsulation.invoke(registrationStatusManager, "createEnrollmentGroupQuery", new Class[] {QuerySpecification.class, String.class, Integer.class},
                mockedQuerySpecification, null, 0);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_024: [The createEnrollmentGroupQuery shall throw IllegalArgumentException if the provided enrollmentGroupId is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void createEnrollmentGroupQueryThrowsOnEmptyEnrollmentGroupId(@Mocked final QuerySpecification mockedQuerySpecification) throws ProvisioningServiceClientException
    {
        // arrange
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();

        // act
        Deencapsulation.invoke(registrationStatusManager, "createEnrollmentGroupQuery", new Class[] {QuerySpecification.class, String.class, Integer.class},
                mockedQuerySpecification, "", 0);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_025: [The createEnrollmentGroupQuery shall throw IllegalArgumentException if the provided pageSize is negative.] */
    @Test (expected = IllegalArgumentException.class)
    public void createEnrollmentGroupQueryThrowsOnNegativePageSize(@Mocked final QuerySpecification mockedQuerySpecification) throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();

        // act
        Deencapsulation.invoke(registrationStatusManager, "createEnrollmentGroupQuery", new Class[] {QuerySpecification.class, String.class, Integer.class},
                mockedQuerySpecification, enrollmentGroupId, -10);

        // assert
    }

    /* SRS_REGISTRATION_STATUS_MANAGER_21_026: [The createEnrollmentGroupQuery shall create Query iterator with a Http path `registrations/[id]`.] */
    /* SRS_REGISTRATION_STATUS_MANAGER_21_027: [The createEnrollmentGroupQuery shall create and return a new instance of the Query iterator.] */
    @Test
    public void createEnrollmentGroupQuerySucceed(
            @Mocked final QuerySpecification mockedQuerySpecification,
            @Mocked final Query mockedQuery) throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String registrationPath = "registrations/" + enrollmentGroupId;
        RegistrationStatusManager registrationStatusManager = createRegistrationStatusManager();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                        mockedContractApiHttp, registrationPath, mockedQuerySpecification, 0);
                result = mockedQuery;
                times = 1;
            }
        };

        // act
        Query query = Deencapsulation.invoke(registrationStatusManager, "createEnrollmentGroupQuery", new Class[] {QuerySpecification.class, String.class, Integer.class},
                mockedQuerySpecification, enrollmentGroupId, 0);

        // assert
        assertNotNull(query);
    }
}
