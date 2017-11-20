// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.service;

import com.microsoft.azure.sdk.iot.deps.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpResponse;
import com.microsoft.azure.sdk.iot.provisioning.service.*;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.contract.ContractApiHttp;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.*;
import mockit.*;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for Individual Enrollment Manager.
 * 100% methods, 100% lines covered
 */
public class IndividualEnrollmentManagerTest
{
    @Mocked
    private ContractApiHttp mockedContractApiHttp;
    
    @Mocked
    private HttpResponse mockedHttpResponse;

    IndividualEnrollmentManager createIndividualEnrollmentManager()
    {
        return Deencapsulation.invoke(
                IndividualEnrollmentManager.class, "createFromContractApiHttp",
                new Class[]{ContractApiHttp.class}, mockedContractApiHttp);

    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_001: [The constructor shall throws IllegalArgumentException if the provided ContractApiHttp is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNull()
    {
        // arrange
        // act
        Deencapsulation.newInstance(IndividualEnrollmentManager.class, new Class[]{ContractApiHttp.class}, (ContractApiHttp)null);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_004: [The factory shall create a new instance of this.] */
    @Test
    public void factoryCreatesNewInstanceSucceed()
    {
        // arrange
        // act
        IndividualEnrollmentManager individualEnrollmentManager = Deencapsulation.invoke(
                IndividualEnrollmentManager.class, "createFromContractApiHttp",
                new Class[]{ContractApiHttp.class}, mockedContractApiHttp);

        // assert
        assertNotNull(individualEnrollmentManager);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_005: [The createOrUpdate shall throws IllegalArgumentException if the provided enrollment is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void createOrUpdateThrowsOnNullEnrollment() throws ProvisioningServiceClientException
    {
        // arrange
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "createOrUpdate", new Class[] {Enrollment.class}, (Enrollment)null);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_006: [The createOrUpdate shall send a Http request for the path `enrollments/[registrationId]`.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_007: [The createOrUpdate shall send a Http request with a body with the enrollment content in JSON format.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_008: [The createOrUpdate shall send a Http request with a Http verb `PUT`.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_011: [The createOrUpdate shall return an Enrollment object created from the body of the response for the Http request .] */
    @Test
    public void createOrUpdateRequestSucceed(
            @Mocked final Enrollment mockedEnrollment,
            @Mocked final Enrollment mockedEnrollmentResponse)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "registrationId-1";
        final String enrollmentPath = "enrollments/" + registrationId;
        final String enrollmentPayload = "validJson";
        final String resultPayload = "validJson";
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new StrictExpectations()
        {
            {
                mockedEnrollment.getRegistrationId();
                result = registrationId;
                times = 1;
                mockedEnrollment.toJson();
                result = enrollmentPayload;
                times = 1;
                mockedContractApiHttp.request(HttpMethod.PUT, enrollmentPath, null, enrollmentPayload);
                result = mockedHttpResponse;
                times = 1;
                mockedHttpResponse.getBody();
                result = resultPayload.getBytes();
                times = 1;
                Deencapsulation.newInstance(Enrollment.class, resultPayload);
                result = mockedEnrollmentResponse;
                times = 1;
            }
        };

        // act
        Enrollment response = Deencapsulation.invoke(individualEnrollmentManager, "createOrUpdate", new Class[] {Enrollment.class}, mockedEnrollment);

        // assert
        assertNotNull(response);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_042: [The createOrUpdate shall throws ProvisioningServiceClientServiceException if the heepResponse contains a null body.] */
    @Test (expected = ProvisioningServiceClientServiceException.class)
    public void createOrUpdateRequestThrowsOnNullBody(
            @Mocked final Enrollment mockedEnrollment)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "registrationId-1";
        final String enrollmentPath = "enrollments/" + registrationId;
        final String enrollmentPayload = "validJson";
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new StrictExpectations()
        {
            {
                mockedEnrollment.getRegistrationId();
                result = registrationId;
                times = 1;
                mockedEnrollment.toJson();
                result = enrollmentPayload;
                times = 1;
                mockedContractApiHttp.request(HttpMethod.PUT, enrollmentPath, null, enrollmentPayload);
                result = mockedHttpResponse;
                times = 1;
                mockedHttpResponse.getBody();
                result = null;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "createOrUpdate", new Class[] {Enrollment.class}, mockedEnrollment);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_009: [The createOrUpdate shall throws ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientTransportException.class)
    public void createOrUpdateRequestTransportFailed(
            @Mocked final Enrollment mockedEnrollment)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "registrationId-1";
        final String enrollmentPath = "enrollments/" + registrationId;
        final String enrollmentPayload = "validJson";
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new NonStrictExpectations()
        {
            {
                mockedEnrollment.getRegistrationId();
                result = registrationId;
                mockedEnrollment.toJson();
                result = enrollmentPayload;
                mockedContractApiHttp.request(HttpMethod.PUT, enrollmentPath, null, enrollmentPayload);
                result = new ProvisioningServiceClientTransportException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "createOrUpdate", new Class[] {Enrollment.class}, mockedEnrollment);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_010: [The createOrUpdate shall throws ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientInternalServerErrorException.class)
    public void createOrUpdateServiceReportedFail(
            @Mocked final Enrollment mockedEnrollment)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "registrationId-1";
        final String enrollmentPath = "enrollments/" + registrationId;
        final String enrollmentPayload = "validJson";
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new NonStrictExpectations()
        {
            {
                mockedEnrollment.getRegistrationId();
                result = registrationId;
                mockedEnrollment.toJson();
                result = enrollmentPayload;
                mockedContractApiHttp.request(HttpMethod.PUT, enrollmentPath, null, enrollmentPayload);
                result = new ProvisioningServiceClientInternalServerErrorException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "createOrUpdate", new Class[] {Enrollment.class}, mockedEnrollment);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_012: [The bulkOperation shall throws IllegalArgumentException if the provided bulkOperationMode is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void bulkOperationThrowsOnNullbulkOperationMode(
            @Mocked final Enrollment mockedEnrollment) throws ProvisioningServiceClientException
    {
        // arrange
        final Collection<Enrollment> enrollments = new LinkedList<>();
        enrollments.add(mockedEnrollment);
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "bulkOperation", new Class[] {BulkOperationMode.class, Collection.class}, (BulkOperationMode)null, enrollments);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_013: [The bulkOperation shall throws IllegalArgumentException if the provided enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void bulkOperationThrowsOnNullEnrollments() throws ProvisioningServiceClientException
    {
        // arrange
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "bulkOperation", new Class[] {BulkOperationMode.class, Collection.class}, BulkOperationMode.CREATE, null);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_013: [The bulkOperation shall throws IllegalArgumentException if the provided enrollments is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void bulkOperationThrowsOnEmptyEnrollments() throws ProvisioningServiceClientException
    {
        // arrange
        final Collection<Enrollment> enrollments = new LinkedList<>();
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "bulkOperation", new Class[] {BulkOperationMode.class, Collection.class}, BulkOperationMode.CREATE, enrollments);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_014: [The bulkOperation shall send a Http request for the path `enrollments`.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_015: [The bulkOperation shall send a Http request with a body with the enrollments content in JSON format.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_016: [The bulkOperation shall send a Http request with a Http verb `POST`.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_019: [The bulkOperation shall return a BulkOperationResult object created from the body of the response for the Http request .] */
    @Test
    public void bulkOperationRequestSucceed(
            @Mocked final Enrollment mockedEnrollment,
            @Mocked final BulkOperation mockedBulkOperation,
            @Mocked final BulkOperationResult mockedBulkOperationResult) throws ProvisioningServiceClientException
    {
        // arrange
        final String bulkEnrollmentPath = "enrollments";
        final String bulkEnrollmentPayload = "validJson";
        final String resultPayload = "validJson";
        final Collection<Enrollment> enrollments = new LinkedList<>();
        enrollments.add(mockedEnrollment);
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new StrictExpectations()
        {
            {
                mockedBulkOperation.toJson(BulkOperationMode.CREATE, enrollments);
                result = bulkEnrollmentPayload;
                times = 1;
                mockedContractApiHttp.request(HttpMethod.POST, bulkEnrollmentPath, null, bulkEnrollmentPayload);
                result = mockedHttpResponse;
                times = 1;
                mockedHttpResponse.getBody();
                result = resultPayload.getBytes();
                times = 1;
                Deencapsulation.newInstance(BulkOperationResult.class, resultPayload);
                result = mockedBulkOperationResult;
                times = 1;
            }
        };

        // act
        BulkOperationResult bulkOperationResult = Deencapsulation.invoke(
                individualEnrollmentManager, "bulkOperation", 
                new Class[] {BulkOperationMode.class, Collection.class}, BulkOperationMode.CREATE, enrollments);

        // assert
        assertNotNull(bulkOperationResult);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_043: [The bulkOperation shall throws ProvisioningServiceClientServiceException if the heepResponse contains a null body.] */
    @Test (expected = ProvisioningServiceClientServiceException.class)
    public void bulkOperationRequestThrowsOnNullBody(
            @Mocked final Enrollment mockedEnrollment,
            @Mocked final BulkOperation mockedBulkOperation) throws ProvisioningServiceClientException
    {
        // arrange
        final String bulkEnrollmentPath = "enrollments";
        final String bulkEnrollmentPayload = "validJson";
        final Collection<Enrollment> enrollments = new LinkedList<>();
        enrollments.add(mockedEnrollment);
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new StrictExpectations()
        {
            {
                mockedBulkOperation.toJson(BulkOperationMode.CREATE, enrollments);
                result = bulkEnrollmentPayload;
                times = 1;
                mockedContractApiHttp.request(HttpMethod.POST, bulkEnrollmentPath, null, bulkEnrollmentPayload);
                result = mockedHttpResponse;
                times = 1;
                mockedHttpResponse.getBody();
                result = null;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(
                individualEnrollmentManager, "bulkOperation",
                new Class[] {BulkOperationMode.class, Collection.class}, BulkOperationMode.CREATE, enrollments);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_017: [The bulkOperation shall throws ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientTransportException.class)
    public void bulkOperationRequestTransportFailed(
            @Mocked final Enrollment mockedEnrollment,
            @Mocked final BulkOperation mockedBulkOperation) throws ProvisioningServiceClientException
    {
        // arrange
        final String bulkEnrollmentPath = "enrollments";
        final String bulkEnrollmentPayload = "validJson";
        final Collection<Enrollment> enrollments = new LinkedList<>();
        enrollments.add(mockedEnrollment);
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new StrictExpectations()
        {
            {
                mockedBulkOperation.toJson(BulkOperationMode.CREATE, enrollments);
                result = bulkEnrollmentPayload;
                mockedContractApiHttp.request(HttpMethod.POST, bulkEnrollmentPath, null, bulkEnrollmentPayload);
                result = new ProvisioningServiceClientTransportException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(
                individualEnrollmentManager, "bulkOperation",
                new Class[] {BulkOperationMode.class, Collection.class}, BulkOperationMode.CREATE, enrollments);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_018: [The bulkOperation shall throws ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientException.class)
    public void bulkOperationServiceReportedFail(
            @Mocked final Enrollment mockedEnrollment,
            @Mocked final BulkOperation mockedBulkOperation) throws ProvisioningServiceClientException
    {
        // arrange
        final String bulkEnrollmentPath = "enrollments";
        final String bulkEnrollmentPayload = "validJson";
        final Collection<Enrollment> enrollments = new LinkedList<>();
        enrollments.add(mockedEnrollment);
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new StrictExpectations()
        {
            {
                mockedBulkOperation.toJson(BulkOperationMode.CREATE, enrollments);
                result = bulkEnrollmentPayload;
                mockedContractApiHttp.request(HttpMethod.POST, bulkEnrollmentPath, null, bulkEnrollmentPayload);
                result = new ProvisioningServiceClientBadFormatException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(
                individualEnrollmentManager, "bulkOperation",
                new Class[] {BulkOperationMode.class, Collection.class}, BulkOperationMode.CREATE, enrollments);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_020: [The get shall throws IllegalArgumentException if the provided registrationId is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void getThrowsOnNullRegistrationId() throws ProvisioningServiceClientException
    {
        // arrange
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "get", new Class[] {String.class}, (String)null);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_020: [The get shall throws IllegalArgumentException if the provided registrationId is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void getThrowsOnEmptyRegistrationId() throws ProvisioningServiceClientException
    {
        // arrange
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "get", new Class[] {String.class}, (String)"");

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_021: [The get shall send a Http request for the path `enrollments/[registrationId]`.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_022: [The get shall send a Http request with a Http verb `GET`.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_025: [The get shall return an Enrollment object created from the body of the response for the Http request .] */
    @Test
    public void getRequestSucceed(
            @Mocked final Enrollment mockedEnrollmentResponse)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "registrationId-1";
        final String enrollmentPath = "enrollments/" + registrationId;
        final String resultPayload = "validJson";
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.GET, enrollmentPath, null, "");
                result = mockedHttpResponse;
                times = 1;
                mockedHttpResponse.getBody();
                result = resultPayload.getBytes();
                times = 1;
                Deencapsulation.newInstance(Enrollment.class, resultPayload);
                result = mockedEnrollmentResponse;
                times = 1;
            }
        };

        // act
        Enrollment response = Deencapsulation.invoke(individualEnrollmentManager, "get", new Class[] {String.class}, registrationId);

        // assert
        assertNotNull(response);
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_044: [The get shall throws ProvisioningServiceClientServiceException if the heepResponse contains a null body.] */
    @Test (expected = ProvisioningServiceClientServiceException.class)
    public void getRequestThrowsOnNullBody()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "registrationId-1";
        final String enrollmentPath = "enrollments/" + registrationId;
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.GET, enrollmentPath, null, "");
                result = mockedHttpResponse;
                times = 1;
                mockedHttpResponse.getBody();
                result = null;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "get", new Class[] {String.class}, registrationId);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_023: [The get shall throws ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientTransportException.class)
    public void getRequestTransportFailed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "registrationId-1";
        final String enrollmentPath = "enrollments/" + registrationId;
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.GET, enrollmentPath, null, "");
                result = new ProvisioningServiceClientTransportException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "get", new Class[] {String.class}, registrationId);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_024: [The get shall throws ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientException.class)
    public void getRequestServiceReportedFail()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "registrationId-1";
        final String enrollmentPath = "enrollments/" + registrationId;
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.GET, enrollmentPath, null, "");
                result = new ProvisioningServiceClientBadFormatException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "get", new Class[] {String.class}, registrationId);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_026: [The delete shall throws IllegalArgumentException if the provided enrollment is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void deleteEnrollmentThrowsOnNullEnrollment() throws ProvisioningServiceClientException
    {
        // arrange
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "delete", new Class[] {Enrollment.class}, (Enrollment)null);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_027: [The delete shall send a Http request for the path `enrollments/[registrationId]`.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_028: [If the enrollment contains eTag, the delete shall send a Http request with `If-Match` the eTag in the header.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_029: [The delete shall send a Http request with a Http verb `DELETE`.] */
    @Test
    public void deleteEnrollmentRequestWithEtagSucceed(
            @Mocked final Enrollment mockedEnrollment)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "registrationId-1";
        final String eTag = "validETag";
        final String enrollmentPath = "enrollments/" + registrationId;
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new StrictExpectations()
        {
            {
                mockedEnrollment.getRegistrationId();
                result = registrationId;
                times = 1;
                mockedEnrollment.getEtag();
                result = eTag;
                times = 2;
                mockedContractApiHttp.request(HttpMethod.DELETE, enrollmentPath, (Map)any, "");
                result = mockedHttpResponse;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "delete", new Class[] {Enrollment.class}, mockedEnrollment);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_028: [If the enrollment contains eTag, the delete shall send a Http request with `If-Match` the eTag in the header.] */
    @Test
    public void deleteEnrollmentRequestWithoutEtagSucceed(
            @Mocked final Enrollment mockedEnrollment)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "registrationId-1";
        final String enrollmentPath = "enrollments/" + registrationId;
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new StrictExpectations()
        {
            {
                mockedEnrollment.getRegistrationId();
                result = registrationId;
                times = 1;
                mockedEnrollment.getEtag();
                result = null;
                times = 1;
                mockedContractApiHttp.request(HttpMethod.DELETE, enrollmentPath, (Map)any, "");
                result = mockedHttpResponse;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "delete", new Class[] {Enrollment.class}, mockedEnrollment);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_030: [The delete shall throws ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientTransportException.class)
    public void deleteEnrollmentRequestTransportFailed(
            @Mocked final Enrollment mockedEnrollment)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "registrationId-1";
        final String eTag = "validETag";
        final String enrollmentPath = "enrollments/" + registrationId;
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new NonStrictExpectations()
        {
            {
                mockedEnrollment.getRegistrationId();
                result = registrationId;
                mockedEnrollment.getEtag();
                result = eTag;
                mockedContractApiHttp.request(HttpMethod.DELETE, enrollmentPath, (Map)any, "");
                result = new ProvisioningServiceClientTransportException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "delete", new Class[] {Enrollment.class}, mockedEnrollment);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_031: [The delete shall throws ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientException.class)
    public void deleteEnrollmentServiceReportedFail(
            @Mocked final Enrollment mockedEnrollment)
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "registrationId-1";
        final String eTag = "validETag";
        final String enrollmentPath = "enrollments/" + registrationId;
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new NonStrictExpectations()
        {
            {
                mockedEnrollment.getRegistrationId();
                result = registrationId;
                mockedEnrollment.getEtag();
                result = eTag;
                mockedContractApiHttp.request(HttpMethod.DELETE, enrollmentPath, (Map)any, "");
                result = new ProvisioningServiceClientBadFormatException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "delete", new Class[] {Enrollment.class}, mockedEnrollment);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_032: [The delete shall throws IllegalArgumentException if the provided registrationId is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void deleteRegistrationIdAndETagThrowsOnNullRegistrationId() throws ProvisioningServiceClientException
    {
        // arrange
        final String eTag = "validEtag";
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "delete", new Class[] {String.class, String.class}, (String)null, eTag);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_032: [The delete shall throws IllegalArgumentException if the provided registrationId is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void deleteRegistrationIdAndETagThrowsOnEmptyRegistrationId() throws ProvisioningServiceClientException
    {
        // arrange
        final String eTag = "validEtag";
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "delete", new Class[] {String.class, String.class}, (String)"", eTag);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_033: [The delete shall send a Http request for the path `enrollments/[registrationId]`.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_034: [If the eTag is not null or empty, the delete shall send a Http request with `If-Match` the eTag in the header.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_035: [The delete shall send a Http request with a Http verb `DELETE`.] */
    @Test
    public void deleteRegistrationIdAndETagRequestWithEtagSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "registrationId-1";
        final String eTag = "validETag";
        final String enrollmentPath = "enrollments/" + registrationId;
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.DELETE, enrollmentPath, (Map)any, "");
                result = mockedHttpResponse;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "delete", new Class[] {String.class, String.class}, registrationId, eTag);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_034: [If the eTag is not null or empty, the delete shall send a Http request with `If-Match` the eTag in the header.] */
    @Test
    public void deleteRegistrationIdAndETagRequestWithNullEtagSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "registrationId-1";
        final String enrollmentPath = "enrollments/" + registrationId;
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.DELETE, enrollmentPath, (Map)any, "");
                result = mockedHttpResponse;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "delete", new Class[] {String.class, String.class}, registrationId, null);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_034: [If the eTag is not null or empty, the delete shall send a Http request with `If-Match` the eTag in the header.] */
    @Test
    public void deleteRegistrationIdAndETagRequestWithEmptyEtagSucceed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "registrationId-1";
        final String enrollmentPath = "enrollments/" + registrationId;
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.DELETE, enrollmentPath, (Map)any, "");
                result = mockedHttpResponse;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "delete", new Class[] {String.class, String.class}, registrationId, "");

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_036: [The delete shall throws ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientTransportException.class)
    public void deleteRegistrationIdAndETagRequestTransportFailed()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "registrationId-1";
        final String eTag = "validETag";
        final String enrollmentPath = "enrollments/" + registrationId;
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.DELETE, enrollmentPath, (Map)any, "");
                result = new ProvisioningServiceClientTransportException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "delete", new Class[] {String.class, String.class}, registrationId, eTag);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_037: [The delete shall throws ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
    @Test (expected = ProvisioningServiceClientException.class)
    public void deleteRegistrationIdAndETagRequestServiceReportedFail()
            throws ProvisioningServiceClientException
    {
        // arrange
        final String registrationId = "registrationId-1";
        final String eTag = "validETag";
        final String enrollmentPath = "enrollments/" + registrationId;
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new StrictExpectations()
        {
            {
                mockedContractApiHttp.request(HttpMethod.DELETE, enrollmentPath, (Map)any, "");
                result = new ProvisioningServiceClientBadFormatException();
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "delete", new Class[] {String.class, String.class}, registrationId, eTag);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_038: [The createQuery shall throws IllegalArgumentException if the provided querySpecification is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void createQueryThrowsOnNullQuerySpecification() throws ProvisioningServiceClientException
    {
        // arrange
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "createQuery", new Class[] {QuerySpecification.class, Integer.class},
                null, 0);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_039: [The createQuery shall throws IllegalArgumentException if the provided pageSize is negative.] */
    @Test (expected = IllegalArgumentException.class)
    public void createQueryThrowsOnNegativePageSize(@Mocked final QuerySpecification mockedQuerySpecification) throws ProvisioningServiceClientException
    {
        // arrange
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();

        // act
        Deencapsulation.invoke(individualEnrollmentManager, "createQuery", new Class[] {QuerySpecification.class, Integer.class},
                mockedQuerySpecification, -10);

        // assert
    }

    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_040: [The createQuery shall create Query iterator with a Http path `enrollments`.] */
    /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_041: [The createQuery shall create and return a new instance of the Query iterator.] */
    @Test
    public void createQuerySucceed(
            @Mocked final QuerySpecification mockedQuerySpecification,
            @Mocked final Query mockedQuery) throws ProvisioningServiceClientException
    {
        // arrange
        final String enrollmentPath = "enrollments";
        IndividualEnrollmentManager individualEnrollmentManager = createIndividualEnrollmentManager();
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[]{ContractApiHttp.class, String.class, QuerySpecification.class, Integer.class},
                        mockedContractApiHttp, enrollmentPath, mockedQuerySpecification, 0);
                result = mockedQuery;
                times = 1;
            }
        };

        // act
        Query query = Deencapsulation.invoke(individualEnrollmentManager, "createQuery", new Class[] {QuerySpecification.class, Integer.class},
                mockedQuerySpecification, 0);

        // assert
        assertNotNull(query);
    }
}
