// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service;

import com.microsoft.azure.sdk.iot.provisioning.service.transport.https.HttpMethod;
import com.microsoft.azure.sdk.iot.provisioning.service.transport.https.HttpResponse;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.contract.ContractApiHttp;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.*;
import mockit.*;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for EnrollmentGroup Manager.
 * 100% methods, 100% lines covered
 */
public class EnrollmentGroupManagerTest
{
    @Mocked
    private ContractApiHttp mockedContractApiHttp;

    @Mocked
    private HttpResponse mockedHttpResponse;

    EnrollmentGroupManager createEnrollmentGroupManager()
    {
        return Deencapsulation.invoke(
                EnrollmentGroupManager.class, "createFromContractApiHttp",
                new Class[]{ContractApiHttp.class}, mockedContractApiHttp);

    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_001: [The constructor shall throw IllegalArgumentException if the provided ContractApiHttp is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNull()
    {
        // arrange
        // act
        Deencapsulation.newInstance(EnrollmentGroupManager.class, new Class[]{ContractApiHttp.class}, (ContractApiHttp)null);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_004: [The factory shall create a new instance of this.] */
    @Test
    public void factoryCreatesNewInstanceSucceed()
    {
        // arrange
        // act
        EnrollmentGroupManager enrollmentGroupManager = Deencapsulation.invoke(
                EnrollmentGroupManager.class, "createFromContractApiHttp",
                new Class[]{ContractApiHttp.class}, mockedContractApiHttp);

        // assert
        assertNotNull(enrollmentGroupManager);
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_005: [The createOrUpdate shall throw IllegalArgumentException if the provided enrollmentGroup is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void createOrUpdateThrowsOnNullEnrollment() throws ProvisioningServiceClientException
    {
        // arrange
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "createOrUpdate", new Class[] {EnrollmentGroup.class}, (EnrollmentGroup)null);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_006: [The createOrUpdate shall send a Http request for the path `enrollmentGroups/[enrollmentGroupId]`.] */
    /* SRS_ENROLLMENT_GROUP_MANAGER_21_007: [The createOrUpdate shall send a Http request with a body with the enrollmentGroup content in JSON format.] */
    /* SRS_ENROLLMENT_GROUP_MANAGER_21_008: [The createOrUpdate shall send a Http request with a Http verb `PUT`.] */
    /* SRS_ENROLLMENT_GROUP_MANAGER_21_011: [The createOrUpdate shall return an EnrollmentGroup object created from the body of the response for the Http request .] */
    /* SRS_ENROLLMENT_GROUP_MANAGER_21_045: [If the enrollmentGroup contains eTag, the createOrUpdate shall send a Http request with `If-Match` the eTag in the header.] */
    @Test
    public void createOrUpdateRequestSucceed(
            @Mocked final EnrollmentGroup mockedEnrollmentGroup,
            @Mocked final EnrollmentGroup mockedEnrollmentGroupResponse)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String enrollmentGroupPath = "enrollmentGroups/" + enrollmentGroupId;
        final String enrollmentGroupPayload = "validJson";
        final String resultPayload = "validJson";
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new StrictExpectations()
        {
            {
                mockedEnrollmentGroup.getEnrollmentGroupId();
                result = enrollmentGroupId;
                times = 1;
                mockedEnrollmentGroup.toJson();
                result = enrollmentGroupPayload;
                times = 1;
                mockedEnrollmentGroup.getEtag();
                result = null;
                times = 1;
                mockedContractApiHttp.request(HttpMethod.PUT, enrollmentGroupPath, (Map)any, enrollmentGroupPayload);
                result = mockedHttpResponse;
                times = 1;
                mockedHttpResponse.getBody();
                result = resultPayload.getBytes(StandardCharsets.UTF_8);
                times = 1;
                Deencapsulation.newInstance(EnrollmentGroup.class, resultPayload);
                result = mockedEnrollmentGroupResponse;
                times = 1;
            }
        };

        // act
        EnrollmentGroup response = Deencapsulation.invoke(enrollmentGroupManager, "createOrUpdate", new Class[] {EnrollmentGroup.class}, mockedEnrollmentGroup);

        // assert
        assertNotNull(response);
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_045: [If the enrollmentGroup contains eTag, the createOrUpdate shall send a Http request with `If-Match` the eTag in the header.] */
    @Test
    public void createOrUpdateRequestWithEtagSucceed(
            @Mocked final EnrollmentGroup mockedEnrollmentGroup,
            @Mocked final EnrollmentGroup mockedEnrollmentGroupResponse)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String enrollmentGroupPath = "enrollmentGroups/" + enrollmentGroupId;
        final String enrollmentGroupPayload = "validJson";
        final String resultPayload = "validJson";
        final String eTag = "validEtag";
        final Map<String, String> headerParameters = new HashMap<String, String>()
        {
            {
                put("If-Match", eTag);
            }
        };
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new StrictExpectations()
        {
            {
                mockedEnrollmentGroup.getEnrollmentGroupId();
                result = enrollmentGroupId;
                times = 1;
                mockedEnrollmentGroup.toJson();
                result = enrollmentGroupPayload;
                times = 1;
                mockedEnrollmentGroup.getEtag();
                result = eTag;
                times = 2;
                mockedContractApiHttp.request(HttpMethod.PUT, enrollmentGroupPath, headerParameters, enrollmentGroupPayload);
                result = mockedHttpResponse;
                times = 1;
                mockedHttpResponse.getBody();
                result = resultPayload.getBytes(StandardCharsets.UTF_8);
                times = 1;
                Deencapsulation.newInstance(EnrollmentGroup.class, resultPayload);
                result = mockedEnrollmentGroupResponse;
                times = 1;
            }
        };

        // act
        EnrollmentGroup response = Deencapsulation.invoke(enrollmentGroupManager, "createOrUpdate", new Class[] {EnrollmentGroup.class}, mockedEnrollmentGroup);

        // assert
        assertNotNull(response);
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_042: [The createOrUpdate shall throw ProvisioningServiceClientServiceException if the heepResponse contains a null body.] */
    @Test (expected = ProvisioningServiceClientServiceException.class)
    public void createOrUpdateRequestTrowsOnNullBody(
            @Mocked final EnrollmentGroup mockedEnrollmentGroup)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String enrollmentGroupPath = "enrollmentGroups/" + enrollmentGroupId;
        final String enrollmentGroupPayload = "validJson";
        final String resultPayload = "validJson";
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new StrictExpectations()
        {
            {
                mockedEnrollmentGroup.getEnrollmentGroupId();
                result = enrollmentGroupId;
                times = 1;
                mockedEnrollmentGroup.toJson();
                result = enrollmentGroupPayload;
                times = 1;
                mockedEnrollmentGroup.getEtag();
                result = null;
                times = 1;
                mockedContractApiHttp.request(HttpMethod.PUT, enrollmentGroupPath, (Map)any, enrollmentGroupPayload);
                result = mockedHttpResponse;
                times = 1;
                mockedHttpResponse.getBody();
                result = null;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "createOrUpdate", new Class[] {EnrollmentGroup.class}, mockedEnrollmentGroup);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_009: [The createOrUpdate shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientTransportException.class)
    public void createOrUpdateRequestTransportFailed(
            @Mocked final EnrollmentGroup mockedEnrollmentGroup)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String enrollmentGroupPath = "enrollmentGroups/" + enrollmentGroupId;
        final String enrollmentGroupPayload = "validJson";
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new NonStrictExpectations()
        {
            {
                mockedEnrollmentGroup.getEnrollmentGroupId();
                result = enrollmentGroupId;
                mockedEnrollmentGroup.toJson();
                result = enrollmentGroupPayload;
                mockedEnrollmentGroup.getEtag();
                result = null;
                mockedContractApiHttp.request(HttpMethod.PUT, enrollmentGroupPath, (Map)any, enrollmentGroupPayload);
                result = new ProvisioningServiceClientTransportException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "createOrUpdate", new Class[] {EnrollmentGroup.class}, mockedEnrollmentGroup);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_010: [The createOrUpdate shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientInternalServerErrorException.class)
    public void createOrUpdateServiceReportedFail(
            @Mocked final EnrollmentGroup mockedEnrollmentGroup)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String enrollmentGroupPath = "enrollmentGroups/" + enrollmentGroupId;
        final String enrollmentGroupPayload = "validJson";
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new NonStrictExpectations()
        {
            {
                mockedEnrollmentGroup.getEnrollmentGroupId();
                result = enrollmentGroupId;
                mockedEnrollmentGroup.toJson();
                result = enrollmentGroupPayload;
                mockedEnrollmentGroup.getEtag();
                result = null;
                mockedContractApiHttp.request(HttpMethod.PUT, enrollmentGroupPath, (Map)any, enrollmentGroupPayload);
                result = new ProvisioningServiceClientInternalServerErrorException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "createOrUpdate", new Class[] {EnrollmentGroup.class}, mockedEnrollmentGroup);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_020: [The get shall throw IllegalArgumentException if the provided enrollmentGroupId is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void getThrowsOnNullEnrollmentGroupId() throws ProvisioningServiceClientException
    {
        // arrange
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "get", new Class[] {String.class}, (String)null);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_020: [The get shall throw IllegalArgumentException if the provided enrollmentGroupId is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void getThrowsOnEmptyEnrollmentGroupId() throws ProvisioningServiceClientException
    {
        // arrange
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "get", new Class[] {String.class}, (String)"");

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_021: [The get shall send a Http request for the path `enrollmentGroups/[enrollmentGroupId]`.] */
    /* SRS_ENROLLMENT_GROUP_MANAGER_21_022: [The get shall send a Http request with a Http verb `GET`.] */
    /* SRS_ENROLLMENT_GROUP_MANAGER_21_025: [The get shall return an EnrollmentGroup object created from the body of the response for the Http request .] */
    @Test
    public void getRequestSucceed(
            @Mocked final EnrollmentGroup mockedEnrollmentGroupResponse)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String enrollmentGroupPath = "enrollmentGroups/" + enrollmentGroupId;
        final String resultPayload = "validJson";
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.GET, enrollmentGroupPath, null, "");
                result = mockedHttpResponse;
                times = 1;
                mockedHttpResponse.getBody();
                result = resultPayload.getBytes(StandardCharsets.UTF_8);
                times = 1;
                Deencapsulation.newInstance(EnrollmentGroup.class, resultPayload);
                result = mockedEnrollmentGroupResponse;
                times = 1;
            }
        };

        // act
        EnrollmentGroup response = Deencapsulation.invoke(enrollmentGroupManager, "get", new Class[] {String.class}, enrollmentGroupId);

        // assert
        assertNotNull(response);
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_043: [The get shall throw ProvisioningServiceClientServiceException if the heepResponse contains a null body.] */
    @Test (expected = ProvisioningServiceClientServiceException.class)
    public void getRequestThrowsOnNullBody()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String enrollmentGroupPath = "enrollmentGroups/" + enrollmentGroupId;
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.GET, enrollmentGroupPath, null, "");
                result = mockedHttpResponse;
                times = 1;
                mockedHttpResponse.getBody();
                result = null;
                times = 1;
            }
        };

        // act
        EnrollmentGroup response = Deencapsulation.invoke(enrollmentGroupManager, "get", new Class[] {String.class}, enrollmentGroupId);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_023: [The get shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientTransportException.class)
    public void getRequestTransportFailed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String enrollmentGroupPath = "enrollmentGroups/" + enrollmentGroupId;
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.GET, enrollmentGroupPath, null, "");
                result = new ProvisioningServiceClientTransportException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "get", new Class[] {String.class}, enrollmentGroupId);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_024: [The get shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientException.class)
    public void getRequestServiceReportedFail()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String enrollmentGroupPath = "enrollmentGroups/" + enrollmentGroupId;
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.GET, enrollmentGroupPath, null, "");
                result = new ProvisioningServiceClientBadFormatException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "get", new Class[] {String.class}, enrollmentGroupId);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_026: [The delete shall throw IllegalArgumentException if the provided enrollmentGroup is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void deleteEnrollmentThrowsOnNullEnrollment() throws ProvisioningServiceClientException
    {
        // arrange
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "delete", new Class[] {EnrollmentGroup.class}, (EnrollmentGroup)null);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_027: [The delete shall send a Http request for the path `enrollmentGroups/[enrollmentGroupId]`.] */
    /* SRS_ENROLLMENT_GROUP_MANAGER_21_028: [If the enrollmentGroup contains eTag, the delete shall send a Http request with `If-Match` the eTag in the header.] */
    /* SRS_ENROLLMENT_GROUP_MANAGER_21_029: [The delete shall send a Http request with a Http verb `DELETE`.] */
    @Test
    public void deleteEnrollmentRequestWithEtagSucceed(
            @Mocked final EnrollmentGroup mockedEnrollmentGroup)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String eTag = "validETag";
        final String enrollmentGroupPath = "enrollmentGroups/" + enrollmentGroupId;
        final Map<String, String> headerParameters = new HashMap<String, String>()
        {
            {
                put("If-Match", eTag);
            }
        };
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new StrictExpectations()
        {
            {
                mockedEnrollmentGroup.getEnrollmentGroupId();
                result = enrollmentGroupId;
                times = 1;
                mockedEnrollmentGroup.getEtag();
                result = eTag;
                times = 2;
                mockedContractApiHttp.request(HttpMethod.DELETE, enrollmentGroupPath, headerParameters, "");
                result = mockedHttpResponse;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "delete", new Class[] {EnrollmentGroup.class}, mockedEnrollmentGroup);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_028: [If the enrollmentGroup contains eTag, the delete shall send a Http request with `If-Match` the eTag in the header.] */
    @Test
    public void deleteEnrollmentRequestWithoutEtagSucceed(
            @Mocked final EnrollmentGroup mockedEnrollmentGroup)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String enrollmentGroupPath = "enrollmentGroups/" + enrollmentGroupId;
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new StrictExpectations()
        {
            {
                mockedEnrollmentGroup.getEnrollmentGroupId();
                result = enrollmentGroupId;
                times = 1;
                mockedEnrollmentGroup.getEtag();
                result = null;
                times = 1;
                mockedContractApiHttp.request(HttpMethod.DELETE, enrollmentGroupPath, (Map)any, "");
                result = mockedHttpResponse;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "delete", new Class[] {EnrollmentGroup.class}, mockedEnrollmentGroup);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_030: [The delete shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientTransportException.class)
    public void deleteEnrollmentRequestTransportFailed(
            @Mocked final EnrollmentGroup mockedEnrollmentGroup)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String eTag = "validETag";
        final String enrollmentGroupPath = "enrollmentGroups/" + enrollmentGroupId;
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new NonStrictExpectations()
        {
            {
                mockedEnrollmentGroup.getEnrollmentGroupId();
                result = enrollmentGroupId;
                mockedEnrollmentGroup.getEtag();
                result = eTag;
                mockedContractApiHttp.request(HttpMethod.DELETE, enrollmentGroupPath, (Map)any, "");
                result = new ProvisioningServiceClientTransportException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "delete", new Class[] {EnrollmentGroup.class}, mockedEnrollmentGroup);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_031: [The delete shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientException.class)
    public void deleteEnrollmentServiceReportedFail(
            @Mocked final EnrollmentGroup mockedEnrollmentGroup)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String eTag = "validETag";
        final String enrollmentGroupPath = "enrollmentGroups/" + enrollmentGroupId;
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new NonStrictExpectations()
        {
            {
                mockedEnrollmentGroup.getEnrollmentGroupId();
                result = enrollmentGroupId;
                mockedEnrollmentGroup.getEtag();
                result = eTag;
                mockedContractApiHttp.request(HttpMethod.DELETE, enrollmentGroupPath, (Map)any, "");
                result = new ProvisioningServiceClientBadFormatException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "delete", new Class[] {EnrollmentGroup.class}, mockedEnrollmentGroup);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_032: [The delete shall throw IllegalArgumentException if the provided enrollmentGroupId is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void deleteEnrollmentGroupIdAndETagThrowsOnNullEnrollmentGroupId() throws ProvisioningServiceClientException
    {
        // arrange
        final String eTag = "validEtag";
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "delete", new Class[] {String.class, String.class}, (String)null, eTag);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_032: [The delete shall throw IllegalArgumentException if the provided enrollmentGroupId is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void deleteEnrollmentGroupIdAndETagThrowsOnEmptyEnrollmentGroupId() throws ProvisioningServiceClientException
    {
        // arrange
        final String eTag = "validEtag";
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "delete", new Class[] {String.class, String.class}, (String)"", eTag);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_033: [The delete shall send a Http request for the path `enrollmentGroups/[enrollmentGroupId]`.] */
    /* SRS_ENROLLMENT_GROUP_MANAGER_21_034: [If the eTag is not null or empty, the delete shall send a Http request with `If-Match` the eTag in the header.] */
    /* SRS_ENROLLMENT_GROUP_MANAGER_21_035: [The delete shall send a Http request with a Http verb `DELETE`.] */
    @Test
    public void deleteEnrollmentGroupIdAndETagRequestWithEtagSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String eTag = "validETag";
        final String enrollmentGroupPath = "enrollmentGroups/" + enrollmentGroupId;
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.DELETE, enrollmentGroupPath, (Map)any, "");
                result = mockedHttpResponse;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "delete", new Class[] {String.class, String.class}, enrollmentGroupId, eTag);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_034: [If the eTag is not null or empty, the delete shall send a Http request with `If-Match` the eTag in the header.] */
    @Test
    public void deleteEnrollmentGroupIdAndETagRequestWithNullEtagSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String enrollmentGroupPath = "enrollmentGroups/" + enrollmentGroupId;
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.DELETE, enrollmentGroupPath, (Map)any, "");
                result = mockedHttpResponse;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "delete", new Class[] {String.class, String.class}, enrollmentGroupId, null);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_034: [If the eTag is not null or empty, the delete shall send a Http request with `If-Match` the eTag in the header.] */
    @Test
    public void deleteEnrollmentGroupIdAndETagRequestWithEmptyEtagSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String enrollmentGroupPath = "enrollmentGroups/" + enrollmentGroupId;
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.DELETE, enrollmentGroupPath, (Map)any, "");
                result = mockedHttpResponse;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "delete", new Class[] {String.class, String.class}, enrollmentGroupId, "");

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_036: [The delete shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientTransportException.class)
    public void deleteEnrollmentGroupIdAndETagRequestTransportFailed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String eTag = "validETag";
        final String enrollmentGroupPath = "enrollmentGroups/" + enrollmentGroupId;
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.DELETE, enrollmentGroupPath, (Map)any, "");
                result = new ProvisioningServiceClientTransportException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "delete", new Class[] {String.class, String.class}, enrollmentGroupId, eTag);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_037: [The delete shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientException.class)
    public void deleteEnrollmentGroupIdAndETagRequestServiceReportedFail()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupId = "enrollmentGroupId-1";
        final String eTag = "validETag";
        final String enrollmentGroupPath = "enrollmentGroups/" + enrollmentGroupId;
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.DELETE, enrollmentGroupPath, (Map)any, "");
                result = new ProvisioningServiceClientBadFormatException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "delete", new Class[] {String.class, String.class}, enrollmentGroupId, eTag);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_038: [The createQuery shall throw IllegalArgumentException if the provided querySpecification is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void createQueryThrowsOnNullQuerySpecification() throws ProvisioningServiceClientException
    {
        // arrange
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "createQuery", new Class[] {QuerySpecification.class, Integer.class},
                null, 0);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_039: [The createQuery shall throw IllegalArgumentException if the provided pageSize is negative.] */
    @Test (expected = IllegalArgumentException.class)
    public void createQueryThrowsOnNegativePageSize(@Mocked final QuerySpecification mockedQuerySpecification) throws ProvisioningServiceClientException
    {
        // arrange
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();

        // act
        Deencapsulation.invoke(enrollmentGroupManager, "createQuery", new Class[] {QuerySpecification.class, Integer.class},
                mockedQuerySpecification, -10);

        // assert
    }

    /* SRS_ENROLLMENT_GROUP_MANAGER_21_040: [The createQuery shall create Query iterator with a Http path `enrollmentGroups`.] */
    /* SRS_ENROLLMENT_GROUP_MANAGER_21_041: [The createQuery shall create and return a new instance of the Query iterator.] */
    @Test
    public void createQuerySucceed(
            @Mocked final QuerySpecification mockedQuerySpecification,
            @Mocked final Query mockedQuery) throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentGroupPath = "enrollmentGroups";
        EnrollmentGroupManager enrollmentGroupManager = createEnrollmentGroupManager();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                        mockedContractApiHttp, enrollmentGroupPath, mockedQuerySpecification, 0);
                result = mockedQuery;
                times = 1;
            }
        };

        // act
        Query query = Deencapsulation.invoke(enrollmentGroupManager, "createQuery", new Class[] {QuerySpecification.class, Integer.class},
                mockedQuerySpecification, 0);

        // assert
        assertNotNull(query);
    }
}
