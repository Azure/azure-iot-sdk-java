/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.dps.device.internal.contract.http;

import com.microsoft.azure.sdk.iot.deps.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpResponse;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.UrlPathBuilder;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.http.ContractAPIHttp;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/*
 * Unit tests for ContractAPIHttp
 * Code coverage : 100% methods, 98% lines
 */
@RunWith(JMockit.class)
public class ContractAPIHttpTest
{
    private static final String TEST_SCOPE_ID = "testScopeID";
    private static final String TEST_HOST_NAME = "testHostName";
    private static final String TEST_REGISTRATION_ID = "testRegistrationId";
    private static final String TEST_OPERATION_ID = "testOperationId";
    private static final String TEST_SAS_TOKEN = "testSasToken";

    @Mocked
    HttpRequest mockedHttpRequest;

    @Mocked
    HttpResponse mockedHttpResponse;

    @Mocked
    SSLContext mockedSslContext;

    @Mocked
    ResponseCallback mockedResponseCallback;

    @Mocked
    UrlPathBuilder mockedUrlPathBuilder;

    @Mocked
    URL mockedUrl;

    @Mocked
    ProvisioningDeviceClientExceptionManager mockedProvisioningDeviceClientExceptionManager;


    private void prepareRequestExpectations() throws IOException
    {
        new NonStrictExpectations()
        {
            {
                new HttpRequest((URL) any, (HttpMethod) any, (byte[]) any);
                result = mockedHttpRequest;
            }
        };
    }

    private void prepareRequestVerifications(HttpMethod method, int headerSize) throws IOException
    {
        new Verifications()
        {
            {
                new HttpRequest((URL)any, method, (byte[])any);
                times = 1;
                mockedHttpRequest.setHeaderField(anyString, anyString);
                times = 3 + headerSize;
            }
        };
    }

    //SRS_ContractAPIHttp_25_001: [The constructor shall save the scope id and hostname.]
    @Test
    public void constructorSucceeds() throws ProvisioningDeviceClientException
    {
        //arrange

        //act
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);

        //assert
        assertEquals(TEST_SCOPE_ID, Deencapsulation.getField(contractAPIHttp, "scopeId"));
        assertEquals(TEST_HOST_NAME, Deencapsulation.getField(contractAPIHttp, "hostName"));
    }

    //SRS_ContractAPIHttp_25_002: [The constructor shall throw ProvisioningDeviceClientException if either scopeId and hostName are null or empty.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullScopeID() throws ProvisioningDeviceClientException
    {
        //arrange

        //act
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(null, TEST_HOST_NAME);

        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnEmptyScopeID() throws ProvisioningDeviceClientException
    {
        //arrange

        //act
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp("", TEST_HOST_NAME);

        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullHostName() throws ProvisioningDeviceClientException
    {
        //arrange

        //act
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, null);

        //assert

    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnEmptyHostName() throws ProvisioningDeviceClientException
    {
        //arrange

        //act
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, "");

        //assert
    }

    //SRS_ContractAPIHttp_25_004: [This method shall retrieve the Url by calling 'generateRegisterUrl' on an object for UrlPathBuilder.]
    //SRS_ContractAPIHttp_25_005: [This method shall prepare the PUT request by setting following headers on a HttpRequest 1. User-Agent : User Agent String for the SDK 2. Accept : "application/json" 3. Content-Type: "application/json; charset=utf-8".]
    //SRS_ContractAPIHttp_25_006: [This method shall set the SSLContext for the Http Request.]
    //SRS_ContractAPIHttp_25_008: [If service return a status as 404 then this method shall trigger the callback to the user with the response message.]
    @Test
    public void requestNonceWithDPSTPMSucceeds() throws IOException, ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        prepareRequestExpectations();
        new NonStrictExpectations()
        {
            {
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                ProvisioningDeviceClientExceptionManager.verifyHttpResponse(mockedHttpResponse);
                result = new ProvisioningDeviceHubException("test Exception");
                mockedHttpResponse.getStatus();
                result = 401;
            }
        };

        //act
        contractAPIHttp.requestNonceForTPM(expectedPayload, TEST_REGISTRATION_ID, mockedSslContext, mockedResponseCallback, null);

        //assert
        prepareRequestVerifications(HttpMethod.PUT, 0);

        new Verifications()
        {
            {
                new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE_ID, ProvisioningDeviceClientTransportProtocol.HTTPS);
                times = 1;
                mockedUrlPathBuilder.generateRegisterUrl(TEST_REGISTRATION_ID);
                times = 1;
                mockedHttpRequest.setSSLContext(mockedSslContext);
                times = 1;
                mockedResponseCallback.run((byte[]) any, null);
                times = 1;

            }
        };
    }

    //SRS_ContractAPIHttp_25_003: [If either registrationId, sslcontext or restResponseCallback is null or if registrationId is empty then this method shall throw ProvisioningDeviceClientException.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void requestNonceWithDPSTPMThrowsOnNullRegistrationId() throws ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        //act
        contractAPIHttp.requestNonceForTPM(expectedPayload, null, mockedSslContext, mockedResponseCallback, null);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void requestNonceWithDPSTPMThrowsOnEmptyRegistrationId() throws ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        //act
        contractAPIHttp.requestNonceForTPM(expectedPayload, "", mockedSslContext, mockedResponseCallback, null);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void requestNonceWithDPSTPMThrowsOnNullSSLContext() throws ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        //act
        contractAPIHttp.requestNonceForTPM(expectedPayload, TEST_REGISTRATION_ID, null, mockedResponseCallback, null);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void requestNonceWithDPSTPMThrowsOnNullResponseCallback() throws ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        //act
        contractAPIHttp.requestNonceForTPM(expectedPayload, TEST_REGISTRATION_ID, mockedSslContext, null, null);
    }

    //SRS_ContractAPIHttp_25_009: [If service return any other status other than 404 then this method shall throw ProvisioningDeviceTransportException in case of 202 or ProvisioningDeviceHubException on any other status.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void requestNonceWithDPSTPMThrowsHubExceptionWithStatusOtherThan404Throws() throws IOException, ProvisioningDeviceClientException
    {
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        prepareRequestExpectations();
        new NonStrictExpectations()
        {
            {
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                ProvisioningDeviceClientExceptionManager.verifyHttpResponse(mockedHttpResponse);
                result = new ProvisioningDeviceHubException("test Exception");
                mockedHttpResponse.getStatus();
                result = 400;
            }
        };

        //act
        try
        {
            contractAPIHttp.requestNonceForTPM(expectedPayload, TEST_REGISTRATION_ID, mockedSslContext, mockedResponseCallback, null);
        }
        finally
        {
            //assert
            new Verifications()
            {
                {
                    mockedResponseCallback.run((byte[])any, any);
                    times = 0;
                }
            };
        }
    }

    //SRS_ContractAPIHttp_25_009: [If service return any other status other than 404 then this method shall throw ProvisioningDeviceTransportException in case of < 300 or ProvisioningDeviceHubException on any other status.]
    @Test (expected = ProvisioningDeviceTransportException.class)
    public void requestNonceWithDPSTPMThrowsHubExceptionWithStatusLessThan300Throws() throws IOException, ProvisioningDeviceClientException
    {
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        prepareRequestExpectations();
        new NonStrictExpectations()
        {
            {
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                ProvisioningDeviceClientExceptionManager.verifyHttpResponse(mockedHttpResponse);
                mockedHttpResponse.getStatus();
                result = 200;
            }
        };

        //act
        try
        {
            contractAPIHttp.requestNonceForTPM(expectedPayload, TEST_REGISTRATION_ID, mockedSslContext, mockedResponseCallback, null);
        }
        finally
        {
            //assert
            new Verifications()
            {
                {
                    mockedResponseCallback.run((byte[])any, any);
                    times = 0;
                }
            };
        }
    }

    @Test (expected = ProvisioningDeviceTransportException.class)
    public void requestNonceWithDPSTPMThrowsTransportExceptionIfAnyOfTheTransportCallsFails() throws IOException, ProvisioningDeviceClientException
    {
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE_ID, ProvisioningDeviceClientTransportProtocol.HTTPS);
                result = new IOException("test IOException");
            }
        };

        //act
        try
        {
            contractAPIHttp.requestNonceForTPM(expectedPayload, TEST_REGISTRATION_ID, mockedSslContext, mockedResponseCallback, null);
        }
        finally
        {
            //assert
            new Verifications()
            {
                {
                    mockedResponseCallback.run((byte[])any, any);
                    times = 0;
                }
            };
        }
    }

    //SRS_ContractAPIHttp_25_012: [This method shall retrieve the Url by calling 'generateRegisterUrl' on an object for UrlPathBuilder.]
    //SRS_ContractAPIHttp_25_013: [This method shall prepare the PUT request by setting following headers on a HttpRequest 1. User-Agent : User Agent String for the SDK 2. Accept : "application/json" 3. Content-Type: "application/json; charset=utf-8" 4. Authorization: specified sas token as authorization if a non null value is given.]
    //SRS_ContractAPIHttp_25_014: [This method shall set the SSLContext for the Http Request.]
    //SRS_ContractAPIHttp_25_015: [This method shall send http request and verify the status by calling 'ProvisioningDeviceClientExceptionManager.verifyHttpResponse'.]
    @Test
    public void authenticateWithDPSWithOutAuthSucceeds() throws IOException, ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        prepareRequestExpectations();
        new NonStrictExpectations()
        {
            {
                mockedHttpRequest.send();
                result = mockedHttpResponse;
            }
        };

        //act
        contractAPIHttp.authenticateWithProvisioningService(expectedPayload, TEST_REGISTRATION_ID, mockedSslContext, null, mockedResponseCallback, null);

        //assert
        prepareRequestVerifications(HttpMethod.PUT, 0);

        new Verifications()
        {
            {
                new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE_ID, ProvisioningDeviceClientTransportProtocol.HTTPS);
                times = 1;
                mockedUrlPathBuilder.generateRegisterUrl(TEST_REGISTRATION_ID);
                times = 1;
                mockedHttpRequest.setSSLContext(mockedSslContext);
                times = 1;
                mockedResponseCallback.run((byte[]) any, null);
                times = 1;

            }
        };
    }

    @Test (expected = ProvisioningDeviceHubException.class)
    public void authenticateWithDPSThrowsOnSendFailure() throws IOException, ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        prepareRequestExpectations();
        new NonStrictExpectations()
        {
            {
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                ProvisioningDeviceClientExceptionManager.verifyHttpResponse(mockedHttpResponse);
                result = new ProvisioningDeviceHubException("test Exception");
            }
        };

        //act
        contractAPIHttp.authenticateWithProvisioningService(expectedPayload, TEST_REGISTRATION_ID, mockedSslContext, null, mockedResponseCallback, null);

        //assert
        prepareRequestVerifications(HttpMethod.PUT, 0);

        new Verifications()
        {
            {
                new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE_ID, ProvisioningDeviceClientTransportProtocol.HTTPS);
                times = 1;
                mockedUrlPathBuilder.generateRegisterUrl(TEST_REGISTRATION_ID);
                times = 1;
                mockedHttpRequest.setSSLContext(mockedSslContext);
                times = 1;
                mockedResponseCallback.run((byte[]) any, null);
                times = 0;

            }
        };
    }

    @Test
    public void authenticateWithDPSWithAuthSucceeds() throws IOException, ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        prepareRequestExpectations();
        new NonStrictExpectations()
        {
            {
                mockedHttpRequest.send();
                result = mockedHttpResponse;
            }
        };

        //act
        contractAPIHttp.authenticateWithProvisioningService(expectedPayload, TEST_REGISTRATION_ID, mockedSslContext, TEST_SAS_TOKEN, mockedResponseCallback, null);

        //assert
        prepareRequestVerifications(HttpMethod.PUT, 1);

        new Verifications()
        {
            {
                new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE_ID, ProvisioningDeviceClientTransportProtocol.HTTPS);
                times = 1;
                mockedUrlPathBuilder.generateRegisterUrl(TEST_REGISTRATION_ID);
                times = 1;
                mockedHttpRequest.setSSLContext(mockedSslContext);
                times = 1;
                mockedResponseCallback.run((byte[]) any, null);
                times = 1;

            }
        };
    }

    //SRS_ContractAPIHttp_25_011: [If either registrationId, sslcontext or restResponseCallback is null or if registrationId is empty then this method shall throw ProvisioningDeviceClientException.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithDPSThrowsOnNullRegistrationId() throws ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        //act
        contractAPIHttp.authenticateWithProvisioningService(expectedPayload, null, mockedSslContext, null, mockedResponseCallback, null);

    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithDPSThrowsOnEmptyRegistrationId() throws ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        //act
        contractAPIHttp.authenticateWithProvisioningService(expectedPayload, "", mockedSslContext, null, mockedResponseCallback, null);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithDPSThrowsOnNullSSLContext() throws ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        //act
        contractAPIHttp.authenticateWithProvisioningService(expectedPayload, TEST_REGISTRATION_ID, null, null, mockedResponseCallback, null);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithDPSThrowsOnNullResponseCallback() throws ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        //act
        contractAPIHttp.authenticateWithProvisioningService(expectedPayload, TEST_REGISTRATION_ID, mockedSslContext, null, null, null);

    }

    @Test (expected = ProvisioningDeviceTransportException.class)
    public void authenticateWithDPSThrowsTransportExceptionIfAnyOfTheTransportCallsFails() throws ProvisioningDeviceClientException
    {
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE_ID, ProvisioningDeviceClientTransportProtocol.HTTPS);
                result = new IOException("test IOException");
            }
        };

        //act
        try
        {
            contractAPIHttp.authenticateWithProvisioningService(expectedPayload, TEST_REGISTRATION_ID, mockedSslContext, TEST_SAS_TOKEN, mockedResponseCallback, null);
        }
        finally
        {
            //assert
            new Verifications()
            {
                {
                    mockedResponseCallback.run((byte[])any, any);
                    times = 0;
                }
            };
        }
    }

    //SRS_ContractAPIHttp_25_019: [This method shall retrieve the Url by calling generateRequestUrl on an object for UrlPathBuilder.]
    //SRS_ContractAPIHttp_25_020: [This method shall prepare the GET request by setting following headers on a HttpRequest 1. User-Agent : User Agent String for the SDK 2. Accept : "application/json" 3. Content-Type: "application/json; charset=utf-8" 4. Authorization: specified sas token as authorization if a non null value is given.]
    //SRS_ContractAPIHttp_25_021: [This method shall set the SSLContext for the Http Request.]
    //SRS_ContractAPIHttp_25_022: [This method shall send http request and verify the status by calling 'ProvisioningDeviceClientExceptionManager.verifyHttpResponse'.]
    //SRS_ContractAPIHttp_25_023: [If service return a status as < 300 then this method shall trigger the callback to the user with the response message.]
    @Test
    public void getRegistrationStatusWithAuthSucceeds() throws IOException, ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        prepareRequestExpectations();
        new NonStrictExpectations()
        {
            {
                mockedHttpRequest.send();
                result = mockedHttpResponse;
            }
        };

        //act
        contractAPIHttp.getRegistrationStatus(TEST_OPERATION_ID, TEST_REGISTRATION_ID, TEST_SAS_TOKEN, mockedSslContext, mockedResponseCallback, null);

        //assert
        prepareRequestVerifications(HttpMethod.GET, 1);

        new Verifications()
        {
            {
                new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE_ID, ProvisioningDeviceClientTransportProtocol.HTTPS);
                times = 1;
                mockedUrlPathBuilder.generateRequestUrl(TEST_REGISTRATION_ID, TEST_OPERATION_ID);
                times = 1;
                mockedHttpRequest.setSSLContext(mockedSslContext);
                times = 1;
                mockedResponseCallback.run((byte[]) any, null);
                times = 1;
            }
        };
    }

    @Test
    public void getRegistrationStatusWithOutAuthSucceeds() throws IOException, ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        prepareRequestExpectations();
        new NonStrictExpectations()
        {
            {
                mockedHttpRequest.send();
                result = mockedHttpResponse;
            }
        };

        //act
        contractAPIHttp.getRegistrationStatus(TEST_OPERATION_ID, TEST_REGISTRATION_ID, null, mockedSslContext, mockedResponseCallback, null);

        //assert
        prepareRequestVerifications(HttpMethod.GET, 0);

        new Verifications()
        {
            {
                new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE_ID, ProvisioningDeviceClientTransportProtocol.HTTPS);
                times = 1;
                mockedUrlPathBuilder.generateRequestUrl(TEST_REGISTRATION_ID, TEST_OPERATION_ID);
                times = 1;
                mockedHttpRequest.setSSLContext(mockedSslContext);
                times = 1;
                mockedResponseCallback.run((byte[]) any, null);
                times = 1;
            }
        };
    }

    //SRS_ContractAPIHttp_25_024: [If service return any other status other than < 300 then this method shall throw ProvisioningDeviceHubException.]
    @Test (expected = ProvisioningDeviceHubException.class)
    public void getRegistrationStatusThrowsOnFailureStatus() throws IOException, ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        prepareRequestExpectations();
        new NonStrictExpectations()
        {
            {
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                ProvisioningDeviceClientExceptionManager.verifyHttpResponse(mockedHttpResponse);
                result = new ProvisioningDeviceHubException("test Exception");
            }
        };

        //act
        try
        {
            contractAPIHttp.getRegistrationStatus(TEST_OPERATION_ID, TEST_REGISTRATION_ID, null, mockedSslContext, mockedResponseCallback, null);
        }
        finally
        {
            //assert
            prepareRequestVerifications(HttpMethod.GET, 0);

            new Verifications()
            {
                {
                    new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE_ID, ProvisioningDeviceClientTransportProtocol.HTTPS);
                    times = 1;
                    mockedUrlPathBuilder.generateRequestUrl(TEST_REGISTRATION_ID, TEST_OPERATION_ID);
                    times = 1;
                    mockedHttpRequest.setSSLContext(mockedSslContext);
                    times = 1;
                    mockedResponseCallback.run((byte[]) any, null);
                    times = 0;
                }
            };
        }
    }

    //SRS_ContractAPIHttp_25_018: [If either operationId, registrationId, sslcontext or restResponseCallback is null or if operationId, registrationId is empty then this method shall throw ProvisioningDeviceClientException.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnNullOperationId() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        contractAPIHttp.getRegistrationStatus(null, TEST_REGISTRATION_ID, null, mockedSslContext, mockedResponseCallback, null);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnEmptyOperationId() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        contractAPIHttp.getRegistrationStatus("", TEST_REGISTRATION_ID, null, mockedSslContext, mockedResponseCallback, null);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnNullRegistrationId() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        contractAPIHttp.getRegistrationStatus(TEST_OPERATION_ID, null, null, mockedSslContext, mockedResponseCallback, null);

    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnEmptyRegistrationId() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        contractAPIHttp.getRegistrationStatus(TEST_OPERATION_ID, "", null, mockedSslContext, mockedResponseCallback, null);
    }


    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnNullSSLContext() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        contractAPIHttp.getRegistrationStatus(TEST_OPERATION_ID, TEST_REGISTRATION_ID, null, null, mockedResponseCallback, null);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnNullResponseCallback() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        contractAPIHttp.getRegistrationStatus(TEST_OPERATION_ID, TEST_REGISTRATION_ID, null, mockedSslContext, null, null);
    }

    @Test (expected = ProvisioningDeviceTransportException.class)
    public void getRegistrationStatusThrowsTransportExceptionIfAnyOfTheTransportCallsFails() throws ProvisioningDeviceClientException
    {
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE_ID, ProvisioningDeviceClientTransportProtocol.HTTPS);
                result = new IOException("test IOException");
            }
        };

        //act
        try
        {
            contractAPIHttp.getRegistrationStatus(TEST_OPERATION_ID, TEST_REGISTRATION_ID, TEST_SAS_TOKEN, mockedSslContext, mockedResponseCallback, null);
        }
        finally
        {
            //assert
            new Verifications()
            {
                {
                    mockedResponseCallback.run((byte[])any, any);
                    times = 0;
                }
            };
        }
    }

    @Test (expected = IllegalArgumentException.class)
    public void prepareRequestThrowsOnNullUrl() throws Exception
    {
        //arrange
        final byte[] expectedPayload = "TestBytes".getBytes();
        final String expectedUserAgentValue = "TestUserAgent";
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        Deencapsulation.invoke(contractAPIHttp, "prepareRequest", new Class[] {URL.class, HttpMethod.class, byte[].class, Integer.class, Map.class, String.class}, null, HttpMethod.PUT, expectedPayload, 0, null, expectedUserAgentValue);
        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void prepareRequestThrowsOnNullMethod() throws Exception
    {
        //arrange
        final byte[] expectedPayload = "TestBytes".getBytes();
        final String expectedUserAgentValue = "TestUserAgent";
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        Deencapsulation.invoke(contractAPIHttp, "prepareRequest", new Class[] {URL.class, HttpMethod.class, byte[].class, Integer.class, Map.class, String.class}, mockedUrl, null, expectedPayload, 0, null, expectedUserAgentValue);
        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void prepareRequestThrowsOnNullPayload() throws Exception
    {
        //arrange
        final byte[] expectedPayload = null;
        final String expectedUserAgentValue = "TestUserAgent";
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        Deencapsulation.invoke(contractAPIHttp, "prepareRequest", new Class[] {URL.class, HttpMethod.class, byte[].class, Integer.class, Map.class, String.class}, mockedUrl, HttpMethod.PUT, expectedPayload, 0, null, expectedUserAgentValue);
        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void prepareRequestThrowsOnInvalidTimeout() throws Exception
    {
        //arrange
        final byte[] expectedPayload = "TestBytes".getBytes();
        final String expectedUserAgentValue = "TestUserAgent";
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        Deencapsulation.invoke(contractAPIHttp, "prepareRequest", new Class[] {URL.class, HttpMethod.class, byte[].class, Integer.class, Map.class, String.class}, mockedUrl, HttpMethod.PUT, expectedPayload, -2, null, expectedUserAgentValue);
        //assert
    }

    @Test (expected = IOException.class)
    public void sendRequestThrowsOnSendFailure() throws Exception
    {
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                mockedHttpRequest.send();
                result = new IOException("Send failure");
            }
        };

        //act
        Deencapsulation.invoke(contractAPIHttp, "sendRequest", mockedHttpRequest);
        //assert
    }
}
