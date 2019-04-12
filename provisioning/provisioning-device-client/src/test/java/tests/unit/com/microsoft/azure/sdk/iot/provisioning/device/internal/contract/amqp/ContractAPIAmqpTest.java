/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.amqp;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.AmqpDeviceOperations;
import com.microsoft.azure.sdk.iot.deps.transport.amqp.SaslHandler;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.amqp.AmqpsProvisioningSymmetricKeySaslHandler;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.amqp.ProvisioningAmqpOperations;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.amqp.ContractAPIAmqp;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceConnectionException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.DeviceRegistrationParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.RequestData;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.impl.MessageImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.net.ssl.SSLContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/*
 * Unit tests for ContractAPIAmqp
 * Code coverage : 50% methods, 28% lines
 */
@RunWith(JMockit.class)
public class ContractAPIAmqpTest
{
    private static final String TEST_SCOPE_ID = "testScopeID";
    private static final String TEST_HOST_NAME = "testHostName";
    private static final String TEST_REGISTRATION_ID = "testRegistrationId";
    private static final String TEST_OPERATION_ID = "testOperationId";
    private static final String TEST_SAS_TOKEN = "testSasToken";
    private static final byte[] TEST_EK = "testEK".getBytes();
    private static final byte[] TEST_SRK = "testSRK".getBytes();

    @Mocked
    AmqpDeviceOperations mockedAmqpProvOperations;

    @Mocked
    ProvisioningAmqpOperations mockedProvisionAmqpConnection;

    @Mocked
    SSLContext mockedSslContext;

    @Mocked
    RequestData mockedRequestData;

    @Mocked
    ResponseCallback mockedResponseCallback;

    @Mocked
    MessageImpl mockedMessage;

    @Mocked
    Map<String, Object> mockedHashMap;

    @Mocked
    Object mockSendLock;

    @Mocked
    ProvisioningDeviceClientConfig mockedProvisioningDeviceClientConfig;

    @Mocked
    byte[] mockedByteArray = new byte[10];

    @Mocked
    AmqpsProvisioningSymmetricKeySaslHandler mockedAmqpsProvisioningSymmetricKeySaslHandler;

    @Mocked
    DeviceRegistrationParser mockedDeviceRegistrationParser;

    private ContractAPIAmqp createContractClass() throws ProvisioningDeviceClientException
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
        return new ContractAPIAmqp(mockedProvisioningDeviceClientConfig);
    }

    private void prepareSendMessage(String operationId) throws IOException
    {
        new NonStrictExpectations()
        {
            {
                Proton.message();
                result = mockedMessage;
                new HashMap<>();
                result = mockedHashMap;
                mockedHashMap.put((String)any, (String)any);

                if (operationId != null)
                {
                    mockedHashMap.put((String)any, (String)any);
                }
                new ApplicationProperties(mockedHashMap);
                mockedMessage.setApplicationProperties((ApplicationProperties)any);
            }
        };
    }

    private void openContractAPI(ContractAPIAmqp contractAPIAmqp) throws ProvisioningDeviceClientException
    {
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = TEST_OPERATION_ID;
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
            }
        };

        contractAPIAmqp.open(mockedRequestData);
    }

    // SRS_ContractAPIAmqp_07_001: [The constructor shall save the scope id and hostname.]
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
        ContractAPIAmqp contractAPIAmqp = new ContractAPIAmqp(mockedProvisioningDeviceClientConfig);
    }

    // SRS_ContractAPIAmqp_07_002: [The constructor shall throw ProvisioningDeviceClientException if either idScope and hostName are null or empty.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullProvisioningDeviceClientConfig() throws ProvisioningDeviceClientException
    {
        //arrange

        //act
        ContractAPIAmqp contractAPIAmqp = new ContractAPIAmqp(null);

        //assert
    }

    // SRS_ContractAPIAmqp_07_002: [The constructor shall throw ProvisioningDeviceClientException if either idScope and hostName are null or empty.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullScopeId() throws ProvisioningDeviceClientException
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
        ContractAPIAmqp contractAPIAmqp = new ContractAPIAmqp(mockedProvisioningDeviceClientConfig);

        //assert
    }

    // SRS_ContractAPIAmqp_07_002: [The constructor shall throw ProvisioningDeviceClientException if either idScope and hostName are null or empty.]
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
        ContractAPIAmqp contractAPIAmqp = new ContractAPIAmqp(mockedProvisioningDeviceClientConfig);

        //assert
    }

    @Test
    public void openSucceeds() throws ProvisioningDeviceClientException, IOException
    {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.isX509();
                result = true;

                mockedProvisionAmqpConnection.open(anyString, mockedSslContext, null, false);
            }
        };

        //act
        contractAPIAmqp.open(mockedRequestData);

        //assert
    }

    // SRS_ContractAPIAmqp_07_022: [If the amqpConnection is NULL or Is not open this method will do nothing.]
    @Test
    public void closeAmqpConnectionNullSucceeds() throws ProvisioningDeviceClientException, IOException
    {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();

        //act
        contractAPIAmqp.close();

        //assert
        new Verifications()
        {
            {
                mockedProvisionAmqpConnection.close();
            }
        };
    }

    // SRS_ContractAPIAmqp_07_022: [If the amqpConnection is NULL or Is not open this method will do nothing.]
    @Test
    public void closeNotOpenSucceeds() throws Exception
    {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
            }
        };
        contractAPIAmqp.open(mockedRequestData);
        new NonStrictExpectations()
        {
            {
                mockedProvisionAmqpConnection.isAmqpConnected();
                result = false;
            }
        };

        //act
        contractAPIAmqp.close();

        //assert
        new Verifications()
        {
            {
            }
        };
    }

    // SRS_ContractAPIAmqp_07_023: [This method will close the amqpConnection connection.]
    @Test
    public void closeSucceeds() throws Exception
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

        ContractAPIAmqp contractAPIAmqp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
            }
        };
        contractAPIAmqp.open(mockedRequestData);
        new NonStrictExpectations()
        {
            {
                mockedProvisionAmqpConnection.isAmqpConnected();
                result = true;
            }
        };

        //act
        contractAPIAmqp.close();

        //assert
        new Verifications()
        {
            {
                mockedProvisionAmqpConnection.close();
            }
        };
    }

    //SRS_ContractAPIAmqp_07_003: [If responseCallback is null, this method shall throw ProvisioningDeviceClientException.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithProvisioningServiceThrowsOnResponseNull() throws ProvisioningDeviceClientException, IOException, InterruptedException {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();

        //act
        contractAPIAmqp.authenticateWithProvisioningService(mockedRequestData, null, null, null);
    }

    // SRS_ContractAPIAmqp_07_005: [This method shall send an AMQP message with the property of iotdps-register.]
    // SRS_ContractAPIAmqp_07_006: [This method shall wait MAX_WAIT_TO_SEND_MSG for a reply from the service.]
    @Test
    public void authenticateWithProvisioningServiceX509Succeeds() throws Exception
    {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();
        new Expectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.isX509();
                result = true;

                Deencapsulation.newInstance(DeviceRegistrationParser.class, new Class[] {String.class, String.class}, TEST_REGISTRATION_ID, "");
                result = mockedDeviceRegistrationParser;

                mockedDeviceRegistrationParser.toJson();
                result = "{ \"registration\":\"" + TEST_REGISTRATION_ID + "\" }";
            }
        };

        //act
        contractAPIAmqp.authenticateWithProvisioningService(mockedRequestData, "", mockedResponseCallback, null);

        //assert
        new Verifications()
        {
            {
                mockedProvisionAmqpConnection.sendRegisterMessage((ResponseCallback)any, any, (byte[])any);
                times = 1;
            }
        };
    }

    //Tests_SRS_ContractAPIAmqp_34_020: [If the request is not x509, but the sasl handler is null, this function shall assume SymmetricKey authentication
    // and it shall open the connection with the request's sas token.]
    @Test
    public void authenticateWithProvisioningServiceSucceedsSymmetricKey() throws Exception
    {
        //arrange
        final String expectedSasToken = "asdf";
        ContractAPIAmqp contractAPIAmqp = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.isX509();
                result = false;
                mockedRequestData.getSasToken();
                result = expectedSasToken;
                Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[] {String.class, String.class, String.class}, TEST_SCOPE_ID, TEST_REGISTRATION_ID, expectedSasToken);
                result = mockedAmqpsProvisioningSymmetricKeySaslHandler;
            }
        };

        contractAPIAmqp.open(mockedRequestData);
        new Expectations()
        {
            {
                Deencapsulation.newInstance(DeviceRegistrationParser.class, new Class[] {String.class, String.class}, TEST_REGISTRATION_ID, "");
                result = mockedDeviceRegistrationParser;

                mockedDeviceRegistrationParser.toJson();
                result = "{ \"registration\":\"" + TEST_REGISTRATION_ID + "\" }";
            }
        };

        //act
        contractAPIAmqp.authenticateWithProvisioningService(mockedRequestData, "", mockedResponseCallback, null);

        //assert
        new Verifications()
        {
            {
                mockedProvisionAmqpConnection.open(TEST_REGISTRATION_ID, mockedSslContext, mockedAmqpsProvisioningSymmetricKeySaslHandler, anyBoolean);
                times = 1;

                mockedProvisionAmqpConnection.sendRegisterMessage((ResponseCallback)any, any, (byte[])any);
                times = 1;
            }
        };
    }

    // SRS_ContractAPIAmqp_07_009: [If requestData is null this method shall throw ProvisioningDeviceClientException.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnRequestNull() throws ProvisioningDeviceClientException, IOException, InterruptedException {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();

        //act
        contractAPIAmqp.getRegistrationStatus(null, mockedResponseCallback, null);
    }

    // SRS_ContractAPIAmqp_07_010: [If requestData.getOperationId() is null or empty, this method shall throw ProvisioningDeviceClientException.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnOperationIdNull() throws ProvisioningDeviceClientException, IOException, InterruptedException {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = null;
            }
        };

        //act
        contractAPIAmqp.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);
    }

    // SRS_ContractAPIAmqp_07_010: [If requestData.getOperationId() is null or empty, this method shall throw ProvisioningDeviceClientException.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnOperationIdEmpty() throws ProvisioningDeviceClientException, IOException, InterruptedException {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = "";
            }
        };

        //act
        contractAPIAmqp.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);
    }

    // SRS_ContractAPIAmqp_07_010: [If responseCallback is null, this method shall throw ProvisioningDeviceClientException.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnResponseNull() throws ProvisioningDeviceClientException, IOException, InterruptedException {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = TEST_OPERATION_ID;
            }
        };

        //act
        contractAPIAmqp.getRegistrationStatus(mockedRequestData, null, null);
    }

    // SRS_ContractAPIAmqp_07_012: [If amqpConnection is null or not connected, this method shall throw ProvisioningDeviceConnectionException.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnAmqpNull() throws ProvisioningDeviceClientException, IOException, InterruptedException {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = TEST_OPERATION_ID;
            }
        };

        //act
        contractAPIAmqp.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);
    }

    // SRS_ContractAPIAmqp_07_012: [If amqpConnection is null or not connected, this method shall throw ProvisioningDeviceConnectionException.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnNotConnected() throws Exception
    {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = TEST_OPERATION_ID;
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
            }
        };

        contractAPIAmqp.open(mockedRequestData);
        new NonStrictExpectations()
        {
            {
                mockedProvisionAmqpConnection.isAmqpConnected();
                result = false;
            }
        };

        //act
        contractAPIAmqp.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);
    }

    // SRS_ContractAPIAmqp_07_013: [This method shall send an AMQP message with the property of iotdps-get-operationstatus and the OperationId.]
    // SRS_ContractAPIAmqp_07_014: [This method shall wait MAX_WAIT_TO_SEND_MSG for a reply from the service.]
    // SRS_ContractAPIAmqp_07_015: [If the service fails to reply in the alloted time this method shall throw ProvisioningDeviceClientException.]
    // SRS_ContractAPIAmqp_07_016: [This method shall responds to the responseCallback with amqp response data and the status DPS_REGISTRATION_RECEIVED.]
    @Test
    public void getRegistrationStatusSuccess() throws Exception
    {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();

        openContractAPI(contractAPIAmqp);

        new NonStrictExpectations()
        {
            {
                mockedProvisionAmqpConnection.isAmqpConnected();
                result = true;
            }
        };

        //act
        contractAPIAmqp.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);

        //assert
        new Verifications()
        {
            {
                mockedProvisionAmqpConnection.sendStatusMessage((String)any, (ResponseCallback)any, (Object)any);
                times = 1;
            }
        };
    }

    @Test  (expected = ProvisioningDeviceClientException.class)
    public void requestNonceForTPMThrowResponseCallbackNull() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();
        openContractAPI(contractAPIAmqp);

        //act
        contractAPIAmqp.requestNonceForTPM(mockedRequestData, "", null, null);

        //assert
    }

    @Test  (expected = ProvisioningDeviceClientException.class)
    public void requestNonceForTPMThrowSslContextNull() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getEndorsementKey();
                result = mockedByteArray;
                mockedRequestData.getStorageRootKey();
                result = mockedByteArray;
                mockedRequestData.getSslContext();
                result = null;
            }
        };

        //act
        contractAPIAmqp.requestNonceForTPM(mockedRequestData, "", mockedResponseCallback, null);

        //assert
    }

    @Test  (expected = ProvisioningDeviceClientException.class)
    public void requestNonceForTPMThrowStorageRootKeyNull() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getEndorsementKey();
                result = mockedByteArray;
                mockedRequestData.getStorageRootKey();
                result = null;
            }
        };

        //act
        contractAPIAmqp.requestNonceForTPM(mockedRequestData, "", mockedResponseCallback, null);

        //assert
    }

    @Test  (expected = ProvisioningDeviceClientException.class)
    public void requestNonceForTPMThrowEndorsementKeyNull() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getEndorsementKey();
                result = null;
            }
        };

        //act
        contractAPIAmqp.requestNonceForTPM(mockedRequestData, "", mockedResponseCallback, null);

        //assert
    }

    @Test  (expected = ProvisioningDeviceClientException.class)
    public void requestNonceForTPMThrowRegistrationIdNull() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        ContractAPIAmqp contractAPIAmqp = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = null;
            }
        };

        //act
        contractAPIAmqp.requestNonceForTPM(mockedRequestData, "", mockedResponseCallback, null);

        //assert
    }

    @Test  (expected = ProvisioningDeviceClientException.class)
    public void requestNonceForTPMThrowRegistrationIdEmpty() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        ContractAPIAmqp contractAPIAmqp = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = "";
            }
        };

        //act
        contractAPIAmqp.requestNonceForTPM(mockedRequestData, "", mockedResponseCallback, null);

        //assert
    }

    @Test  (expected = ProvisioningDeviceClientException.class)
    public void requestNonceForTPMThrowRequestDataNull() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        ContractAPIAmqp contractAPIAmqp = createContractClass();

        //act
        contractAPIAmqp.requestNonceForTPM(null, "", mockedResponseCallback, null);

        //assert
    }

    @Test  (expected = ProvisioningDeviceClientException.class)
    public void requestNonceForTPMSuccess() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        ContractAPIAmqp contractAPIAmqp = createContractClass();

        //act
        contractAPIAmqp.requestNonceForTPM(mockedRequestData, "", mockedResponseCallback, null);

        //assert
    }

    //Tests_SRS_ContractAPIAmqp_34_014: [If the requestData is using X509, this function shall open the underlying provisioningAmqpOperations object with the provided registrationId and sslContext.]
    @Test
    public void openDoesNothingIfUsingTPM() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.isX509();
                result = false;
            }
        };

        //act
        contractAPIAmqp.open(mockedRequestData);

        //assert
        new Verifications()
        {
            {
                mockedProvisionAmqpConnection.open(anyString, (SSLContext) any, (SaslHandler) any, anyBoolean);
                times = 0;
            }
        };
    }

    //Test_SRS_ContractAPIAmqp_34_006: [If requestData is null, this function shall throw a ProvisioningDeviceConnectionException.]
    @Test (expected = ProvisioningDeviceConnectionException.class)
    public void openThrowsForNullRequestData() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();

         //act
        contractAPIAmqp.open(null);
    }

    //Tests_SRS_ContractAPIAmqp_34_007: [If requestData contains a null or empty registrationId, this function shall throw a ProvisioningDeviceConnectionException.]
    @Test (expected = ProvisioningDeviceConnectionException.class)
    public void openThrowsForNullRegistrationId() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.isX509();
                result = true;
                mockedRequestData.getRegistrationId();
                result = null;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
            }
        };

        //act
        contractAPIAmqp.open(mockedRequestData);
    }

    //Codes_SRS_ContractAPIAmqp_34_008: [If requestData contains a null SSLContext, this function shall throw a ProvisioningDeviceConnectionException.]
    @Test (expected = ProvisioningDeviceConnectionException.class)
    public void openThrowsForNullSSLContext() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.isX509();
                result = true;
                mockedRequestData.getSslContext();
                result = null;
                mockedRequestData.getRegistrationId();
                result = "some registration id";
            }
        };

        //act
        contractAPIAmqp.open(mockedRequestData);
    }

    //Tests_SRS_ContractAPIAmqp_34_014: [If the requestData is using X509, this function shall open the underlying provisioningAmqpOperations object with the provided registrationId and sslContext.]
    @Test
    public void openCallsOpenOnProvisioningAmqpOperations() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIAmqp contractAPIAmqp = createContractClass();
        final String registrationId = "some registrationId";
        new NonStrictExpectations()
        {
            {
                mockedRequestData.isX509();
                result = true;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.getRegistrationId();
                result = registrationId;
            }
        };

        //act
        contractAPIAmqp.open(mockedRequestData);

        //assert
        new Verifications()
        {
            {
                mockedProvisionAmqpConnection.open(registrationId, mockedSslContext, null, false);
                times = 1;
            }
        };
    }
}
