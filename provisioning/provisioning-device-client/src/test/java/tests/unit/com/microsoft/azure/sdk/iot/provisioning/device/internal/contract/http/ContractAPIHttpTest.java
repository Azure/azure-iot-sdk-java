/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.http;

import com.microsoft.azure.sdk.iot.deps.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpResponse;
import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.UrlPathBuilder;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.http.ContractAPIHttp;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.DeviceRegistrationParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.TpmRegistrationResultParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.RequestData;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ResponseData;
import mockit.*;
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
 * Code coverage : 100% methods, 100% lines
 */
@RunWith(JMockit.class)
public class ContractAPIHttpTest
{
    private static final String TEST_SCOPE_ID = "testScopeID";
    private static final String TEST_HOST_NAME = "testHostName";
    private static final String TEST_REGISTRATION_ID = "testRegistrationId";
    private static final String TEST_OPERATION_ID = "testOperationId";
    private static final String TEST_SAS_TOKEN = "testSasToken";
    private static final byte[] TEST_EK = "testEK".getBytes();
    private static final byte[] TEST_SRK = "testSRK".getBytes();

    @Mocked
    Base64 mockedBas64;

    @Mocked
    DeviceRegistrationParser mockedDeviceRegistrationParser;

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

    @Mocked
    RequestData mockedRequestData;

    @Mocked
    ProvisioningDeviceClientConfig mockedProvisioningDeviceClientConfig;

    @Mocked
    TpmRegistrationResultParser mockedTpmRegistrationResultParser;

    private ContractAPIHttp createContractClass() throws ProvisioningDeviceClientException
    {
        new NonStrictExpectations()
        {
            {
                mockedProvisioningDeviceClientConfig.getIdScope();
                result = TEST_SCOPE_ID;
                mockedProvisioningDeviceClientConfig.getProvisioningServiceGlobalEndpoint();
                result = TEST_HOST_NAME;
            }
        };
        return new ContractAPIHttp(mockedProvisioningDeviceClientConfig);
    }

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
        new NonStrictExpectations()
        {
            {
                mockedProvisioningDeviceClientConfig.getIdScope();
                result = TEST_SCOPE_ID;
                mockedProvisioningDeviceClientConfig.getProvisioningServiceGlobalEndpoint();
                result = TEST_HOST_NAME;
            }
        };

        //act
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(mockedProvisioningDeviceClientConfig);

        //assert
        assertEquals(TEST_SCOPE_ID, Deencapsulation.getField(contractAPIHttp, "idScope"));
        assertEquals(TEST_HOST_NAME, Deencapsulation.getField(contractAPIHttp, "hostName"));
    }

    //SRS_ContractAPIHttp_25_002: [The constructor shall throw ProvisioningDeviceClientException if either idScope and hostName are null or empty.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullScopeID() throws ProvisioningDeviceClientException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedProvisioningDeviceClientConfig.getIdScope();
                result = null;
            }
        };

        //act
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(mockedProvisioningDeviceClientConfig);

        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnEmptyScopeID() throws ProvisioningDeviceClientException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedProvisioningDeviceClientConfig.getIdScope();
                result = "";
            }
        };

        //act
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(mockedProvisioningDeviceClientConfig);

        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullHostName() throws ProvisioningDeviceClientException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedProvisioningDeviceClientConfig.getIdScope();
                result = TEST_SCOPE_ID;
                mockedProvisioningDeviceClientConfig.getProvisioningServiceGlobalEndpoint();
                result = null;
            }
        };

        //act
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(mockedProvisioningDeviceClientConfig);

        //assert

    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnEmptyHostName() throws ProvisioningDeviceClientException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedProvisioningDeviceClientConfig.getIdScope();
                result = TEST_SCOPE_ID;
                mockedProvisioningDeviceClientConfig.getProvisioningServiceGlobalEndpoint();
                result = "";
            }
        };

        //act
        ContractAPIHttp contractAPIHttp = new ContractAPIHttp(mockedProvisioningDeviceClientConfig);

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
        ContractAPIHttp contractAPIHttp = createContractClass();
        prepareRequestExpectations();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getEndorsementKey();
                result = TEST_EK;
                mockedRequestData.getStorageRootKey();
                result = TEST_SRK;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                ProvisioningDeviceClientExceptionManager.verifyHttpResponse(mockedHttpResponse);
                result = new ProvisioningDeviceHubException("test Exception");
                mockedHttpResponse.getStatus();
                result = 401;
                TpmRegistrationResultParser.createFromJson(new String(mockedHttpResponse.getBody()));
                result = mockedTpmRegistrationResultParser;
                mockedTpmRegistrationResultParser.getAuthenticationKey();
                result = "some auth key";
                Base64.decodeBase64Local((byte[]) any);
                result = new byte[]{};
                new DeviceRegistrationParser(anyString, anyString, anyString);
                result = mockedDeviceRegistrationParser;
                mockedDeviceRegistrationParser.toJson();
                result = "some json";
            }
        };

        //act
        contractAPIHttp.requestNonceForTPM(mockedRequestData, mockedResponseCallback, null);

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
                mockedResponseCallback.run((ResponseData) any, null);
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
        ContractAPIHttp contractAPIHttp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = null;
                mockedRequestData.getEndorsementKey();
                result = TEST_EK;
                mockedRequestData.getStorageRootKey();
                result = TEST_SRK;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
            }
        };
        //act
        contractAPIHttp.requestNonceForTPM(mockedRequestData, mockedResponseCallback, null);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void requestNonceWithDPSTPMThrowsOnEmptyRegistrationId() throws ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = "";
                mockedRequestData.getEndorsementKey();
                result = TEST_EK;
                mockedRequestData.getStorageRootKey();
                result = TEST_SRK;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
            }
        };
        //act
        contractAPIHttp.requestNonceForTPM(mockedRequestData, mockedResponseCallback, null);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void requestNonceWithDPSTPMThrowsOnNullEk() throws ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getEndorsementKey();
                result = null;
                mockedRequestData.getStorageRootKey();
                result = TEST_SRK;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
            }
        };
        //act
        contractAPIHttp.requestNonceForTPM(mockedRequestData, mockedResponseCallback, null);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void requestNonceWithDPSTPMThrowsOnNullSRk() throws ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getEndorsementKey();
                result = TEST_EK;
                mockedRequestData.getStorageRootKey();
                result = null;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
            }
        };
        //act
        contractAPIHttp.requestNonceForTPM(mockedRequestData, mockedResponseCallback, null);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void requestNonceWithDPSTPMThrowsOnNullSSLContext() throws ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getEndorsementKey();
                result = TEST_EK;
                mockedRequestData.getStorageRootKey();
                result = TEST_SRK;
                mockedRequestData.getSslContext();
                result = null;
            }
        };
        //act
        contractAPIHttp.requestNonceForTPM(mockedRequestData, mockedResponseCallback, null);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void requestNonceWithDPSTPMThrowsOnNullResponseCallback() throws ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getEndorsementKey();
                result = TEST_EK;
                mockedRequestData.getStorageRootKey();
                result = TEST_SRK;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
            }
        };
        //act
        contractAPIHttp.requestNonceForTPM(mockedRequestData, null, null);
    }

    //SRS_ContractAPIHttp_25_009: [If service return any other status other than 404 then this method shall throw ProvisioningDeviceTransportException in case of 202 or ProvisioningDeviceHubException on any other status.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void requestNonceWithDPSTPMThrowsHubExceptionWithStatusOtherThan404Throws() throws IOException, ProvisioningDeviceClientException
    {
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = createContractClass();
        prepareRequestExpectations();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getEndorsementKey();
                result = TEST_EK;
                mockedRequestData.getStorageRootKey();
                result = TEST_SRK;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                ProvisioningDeviceClientExceptionManager.verifyHttpResponse(mockedHttpResponse);
                result = new ProvisioningDeviceHubException("test Exception");
                mockedHttpResponse.getStatus();
                new DeviceRegistrationParser(anyString, anyString, anyString);
                result = mockedDeviceRegistrationParser;
                mockedDeviceRegistrationParser.toJson();
                result = "some json";
            }
        };

        //act
        try
        {
            contractAPIHttp.requestNonceForTPM(mockedRequestData, mockedResponseCallback, null);
        }
        finally
        {
            //assert
            new Verifications()
            {
                {
                    mockedResponseCallback.run((ResponseData) any, any);
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
        ContractAPIHttp contractAPIHttp = createContractClass();
        prepareRequestExpectations();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getEndorsementKey();
                result = TEST_EK;
                mockedRequestData.getStorageRootKey();
                result = TEST_SRK;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                ProvisioningDeviceClientExceptionManager.verifyHttpResponse(mockedHttpResponse);
                mockedHttpResponse.getStatus();
                result = 200;
                new DeviceRegistrationParser(anyString, anyString, anyString);
                result = mockedDeviceRegistrationParser;
                mockedDeviceRegistrationParser.toJson();
                result = "some json";
            }
        };

        //act
        try
        {
            contractAPIHttp.requestNonceForTPM(mockedRequestData, mockedResponseCallback, null);
        }
        finally
        {
            //assert
            new Verifications()
            {
                {
                    mockedResponseCallback.run((ResponseData) any, any);
                    times = 0;
                }
            };
        }
    }

    @Test (expected = ProvisioningDeviceTransportException.class)
    public void requestNonceWithDPSTPMThrowsTransportExceptionIfAnyOfTheTransportCallsFails() throws IOException, ProvisioningDeviceClientException
    {
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getEndorsementKey();
                result = TEST_EK;
                mockedRequestData.getStorageRootKey();
                result = TEST_SRK;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE_ID, ProvisioningDeviceClientTransportProtocol.HTTPS);
                result = new IOException("test IOException");
            }
        };

        //act
        try
        {
            contractAPIHttp.requestNonceForTPM(mockedRequestData, mockedResponseCallback, null);
        }
        finally
        {
            //assert
            new Verifications()
            {
                {
                    mockedResponseCallback.run((ResponseData) any, any);
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
    public void authenticateWithDPSWithOutAuthSucceeds(@Mocked DeviceRegistrationParser mockedDeviceRegistrationParser) throws IOException, ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = createContractClass();
        prepareRequestExpectations();
        new NonStrictExpectations()
        {
            {
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getEndorsementKey();
                result = null;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.getSasToken();
                result = null;
                mockedDeviceRegistrationParser.toJson();
                result = "TEST JSON";
                new DeviceRegistrationParser(anyString);
                result = mockedDeviceRegistrationParser;
                mockedDeviceRegistrationParser.toJson();
                result = "some json";
            }
        };

        //act
        contractAPIHttp.authenticateWithProvisioningService(mockedRequestData, mockedResponseCallback, null);

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
                mockedResponseCallback.run((ResponseData) any, null);
                times = 1;

            }
        };
    }

    //SRS_ContractAPIHttp_25_026: [ This method shall build the required Json input using parser. ]
    @Test
    public void authenticateWithDPSForTPMSucceeds() throws IOException, ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = createContractClass();
        prepareRequestExpectations();
        new NonStrictExpectations()
        {
            {
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                mockedRequestData.getEndorsementKey();
                result = TEST_EK;
                mockedRequestData.getStorageRootKey();
                result = TEST_SRK;
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.getSasToken();
                result = null;

                Deencapsulation.newInstance(DeviceRegistrationParser.class, new Class[] {String.class, String.class, String.class}, anyString, "", "");
                result = mockedDeviceRegistrationParser;

                mockedDeviceRegistrationParser.toJson();
                result = "some json";
            }
        };

        //act
        contractAPIHttp.authenticateWithProvisioningService(mockedRequestData, mockedResponseCallback, null);

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
                mockedResponseCallback.run((ResponseData) any, null);
                times = 1;

            }
        };
    }

    @Test (expected = ProvisioningDeviceHubException.class)
    public void authenticateWithDPSThrowsOnSendFailure() throws IOException, ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = createContractClass();
        prepareRequestExpectations();
        new NonStrictExpectations()
        {
            {
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getEndorsementKey();
                result = null;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.getSasToken();
                result = null;
                ProvisioningDeviceClientExceptionManager.verifyHttpResponse(mockedHttpResponse);
                result = new ProvisioningDeviceHubException("test Exception");
                new DeviceRegistrationParser(anyString);
                result = mockedDeviceRegistrationParser;
                mockedDeviceRegistrationParser.toJson();
                result = "some json";
            }
        };

        //act
        contractAPIHttp.authenticateWithProvisioningService(mockedRequestData, mockedResponseCallback, null);

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
                mockedResponseCallback.run((ResponseData) any, null);
                times = 0;

            }
        };
    }

    @Test
    public void authenticateWithDPSWithAuthSucceeds() throws IOException, ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = createContractClass();
        prepareRequestExpectations();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getEndorsementKey();
                result = TEST_EK;
                mockedRequestData.getStorageRootKey();
                result = TEST_SRK;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.getSasToken();
                result = TEST_SAS_TOKEN;
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                mockedHttpResponse.getStatus();
                result = 400;
                new DeviceRegistrationParser(anyString, anyString, anyString);
                result = mockedDeviceRegistrationParser;
                mockedDeviceRegistrationParser.toJson();
                result = "some json";
            }
        };

        //act
        contractAPIHttp.authenticateWithProvisioningService(mockedRequestData, mockedResponseCallback, null);

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
                mockedResponseCallback.run((ResponseData) any, null);
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
        ContractAPIHttp contractAPIHttp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = null;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.getSasToken();
                result = null;
            }
        };
        //act
        contractAPIHttp.authenticateWithProvisioningService(mockedRequestData, mockedResponseCallback, null);

    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithDPSThrowsOnEmptyRegistrationId() throws ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = "";
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.getSasToken();
                result = null;
            }
        };
        //act
        contractAPIHttp.authenticateWithProvisioningService(mockedRequestData, mockedResponseCallback, null);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithDPSThrowsOnNullSSLContext() throws ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = null;
                mockedRequestData.getSasToken();
                result = null;
            }
        };
        //act
        contractAPIHttp.authenticateWithProvisioningService(mockedRequestData, mockedResponseCallback, null);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithDPSThrowsOnNullResponseCallback() throws ProvisioningDeviceClientException
    {
        //arrange
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.getSasToken();
                result = null;
            }
        };
        //act
        contractAPIHttp.authenticateWithProvisioningService(mockedRequestData, null, null);

    }

    @Test (expected = ProvisioningDeviceTransportException.class)
    public void authenticateWithDPSThrowsTransportExceptionIfAnyOfTheTransportCallsFails() throws ProvisioningDeviceClientException
    {
        final byte[] expectedPayload = "testByte".getBytes();
        ContractAPIHttp contractAPIHttp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.getSasToken();
                result = null;
                new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE_ID, ProvisioningDeviceClientTransportProtocol.HTTPS);
                result = new IOException("test IOException");
            }
        };

        //act
        try
        {
            contractAPIHttp.authenticateWithProvisioningService(mockedRequestData, mockedResponseCallback, null);
        }
        finally
        {
            //assert
            new Verifications()
            {
                {
                    mockedResponseCallback.run((ResponseData) any, any);
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
        ContractAPIHttp contractAPIHttp = createContractClass();
        prepareRequestExpectations();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = TEST_OPERATION_ID;
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.getSasToken();
                result = TEST_SAS_TOKEN;
                mockedHttpRequest.send();
                result = mockedHttpResponse;
            }
        };

        //act
        contractAPIHttp.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);

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
                mockedResponseCallback.run((ResponseData) any, null);
                times = 1;
            }
        };
    }

    @Test
    public void getRegistrationStatusWithOutAuthSucceeds() throws IOException, ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIHttp contractAPIHttp = createContractClass();
        prepareRequestExpectations();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = TEST_OPERATION_ID;
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.getSasToken();
                result = null;
                mockedHttpRequest.send();
                result = mockedHttpResponse;
            }
        };

        //act
        contractAPIHttp.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);

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
                mockedResponseCallback.run((ResponseData) any, null);
                times = 1;
            }
        };
    }

    //SRS_ContractAPIHttp_25_024: [If service return any other status other than < 300 then this method shall throw ProvisioningDeviceHubException.]
    @Test (expected = ProvisioningDeviceHubException.class)
    public void getRegistrationStatusThrowsOnFailureStatus() throws IOException, ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIHttp contractAPIHttp = createContractClass();
        prepareRequestExpectations();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = TEST_OPERATION_ID;
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.getSasToken();
                result = null;
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                ProvisioningDeviceClientExceptionManager.verifyHttpResponse(mockedHttpResponse);
                result = new ProvisioningDeviceHubException("test Exception");
            }
        };

        //act
        try
        {
            contractAPIHttp.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);
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
                    mockedResponseCallback.run((ResponseData) any, null);
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
        ContractAPIHttp contractAPIHttp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = null;
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.getSasToken();
                result = null;
            }
        };

        //act
        contractAPIHttp.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnEmptyOperationId() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIHttp contractAPIHttp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = "";
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.getSasToken();
                result = null;
            }
        };

        //act
        contractAPIHttp.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnNullRegistrationId() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIHttp contractAPIHttp = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = TEST_OPERATION_ID;
                mockedRequestData.getRegistrationId();
                result = null;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.getSasToken();
                result = null;
            }
        };
        //act
        contractAPIHttp.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);

    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnEmptyRegistrationId() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIHttp contractAPIHttp = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = TEST_OPERATION_ID;
                mockedRequestData.getRegistrationId();
                result = "";
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.getSasToken();
                result = null;
            }
        };

        //act
        contractAPIHttp.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);
    }


    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnNullSSLContext() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIHttp contractAPIHttp = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = TEST_OPERATION_ID;
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = null;
                mockedRequestData.getSasToken();
                result = null;
            }
        };
        //act
        contractAPIHttp.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnNullResponseCallback() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIHttp contractAPIHttp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = TEST_OPERATION_ID;
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.getSasToken();
                result = null;
            }
        };

        //act
        contractAPIHttp.getRegistrationStatus(mockedRequestData, null, null);
    }

    @Test (expected = ProvisioningDeviceTransportException.class)
    public void getRegistrationStatusThrowsTransportExceptionIfAnyOfTheTransportCallsFails() throws ProvisioningDeviceClientException
    {
        ContractAPIHttp contractAPIHttp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = TEST_OPERATION_ID;
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.getSasToken();
                result = TEST_SAS_TOKEN;
                new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE_ID, ProvisioningDeviceClientTransportProtocol.HTTPS);
                result = new IOException("test IOException");
            }
        };

        //act
        try
        {
            contractAPIHttp.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);
        }
        finally
        {
            //assert
            new Verifications()
            {
                {
                    mockedResponseCallback.run((ResponseData) any, any);
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
        ContractAPIHttp contractAPIHttp = createContractClass();

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
        ContractAPIHttp contractAPIHttp = createContractClass();

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
        ContractAPIHttp contractAPIHttp = createContractClass();

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
        ContractAPIHttp contractAPIHttp = createContractClass();

        //act
        Deencapsulation.invoke(contractAPIHttp, "prepareRequest", new Class[] {URL.class, HttpMethod.class, byte[].class, Integer.class, Map.class, String.class}, mockedUrl, HttpMethod.PUT, expectedPayload, -2, null, expectedUserAgentValue);
        //assert
    }

    @Test (expected = IOException.class)
    public void sendRequestThrowsOnSendFailure() throws Exception
    {
        ContractAPIHttp contractAPIHttp = createContractClass();
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
