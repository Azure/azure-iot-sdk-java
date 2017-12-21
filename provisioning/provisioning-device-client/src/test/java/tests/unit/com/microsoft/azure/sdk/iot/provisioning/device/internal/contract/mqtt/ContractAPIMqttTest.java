/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.mqtt;

import com.microsoft.azure.sdk.iot.deps.transport.mqtt.MqttConnection;
import com.microsoft.azure.sdk.iot.deps.transport.mqtt.MqttMessage;
import com.microsoft.azure.sdk.iot.deps.transport.mqtt.MqttQos;
import com.microsoft.azure.sdk.iot.deps.util.ObjectLock;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.mqtt.ContractAPIMqtt;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.RequestData;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ResponseData;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
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

/*
 * Unit tests for ContractAPIMqtt
 * Code coverage : 100% methods, 100% lines
 */
@RunWith(JMockit.class)
public class ContractAPIMqttTest
{
    private static final String TEST_SCOPE_ID = "testScopeID";
    private static final String TEST_HOST_NAME = "testHostName";
    private static final String TEST_REGISTRATION_ID = "testRegistrationId";
    private static final String TEST_OPERATION_ID = "testOperationId";

    @Mocked
    MqttConnection mockedMqttConnection;

    @Mocked
    SSLContext mockedSslContext;

    @Mocked
    RequestData mockedRequestData;

    @Mocked
    ResponseCallback mockedResponseCallback;

    @Mocked
    ResponseData mockedResponseData;

    @Mocked
    MessageImpl mockedMessage;

    @Mocked
    ObjectLock mockedObjectLock;

    @Mocked
    Map<String, Object> mockedHashMap;

    //@Mocked
    //Queue<MqttMessage> mockedQueue;

    @Mocked
    MqttMessage mockedMqttMessage;

    @Mocked
    Object mockSendLock;

    @Mocked
    ProvisioningDeviceClientConfig mockedProvisioningDeviceClientConfig;

    @Mocked
    byte[] mockedByteArray = new byte[10];

    private ContractAPIMqtt createContractClass() throws ProvisioningDeviceClientException
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
        return new ContractAPIMqtt(mockedProvisioningDeviceClientConfig);
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

    private void openContractAPI(ContractAPIMqtt ContractAPIMqtt) throws ProvisioningDeviceClientException
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

        ContractAPIMqtt.open(mockedRequestData);
    }

    // SRS_ContractAPIMqtt_07_001: [The constructor shall save the scope id and hostname.]
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
        ContractAPIMqtt contractAPIMqtt = new ContractAPIMqtt(mockedProvisioningDeviceClientConfig);

        //assert
    }

    // SRS_ContractAPIMqtt_07_002: [The constructor shall throw ProvisioningDeviceClientException if either scopeId and hostName are null or empty.]
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
        ContractAPIMqtt contractAPIMqtt = new ContractAPIMqtt(mockedProvisioningDeviceClientConfig);

        //assert
    }

    // SRS_ContractAPIMqtt_07_002: [The constructor shall throw ProvisioningDeviceClientException if either scopeId and hostName are null or empty.]
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
        ContractAPIMqtt contractAPIMqtt = new ContractAPIMqtt(mockedProvisioningDeviceClientConfig);

        //assert
    }

    @Test
    public void openSucceeds() throws ProvisioningDeviceClientException, IOException
    {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
                mockedRequestData.isX509();
                result = true;
            }
        };

        //act
        contractAPIMqtt.open(mockedRequestData);

        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void openThrowsOnNullRequestData() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();

        //act
        contractAPIMqtt.open(null);

        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void openThrowsOnNullRegistrationId() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = null;
            }
        };

        //act
        contractAPIMqtt.open(mockedRequestData);

        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void openThrowsOnNullSSLContext() throws ProvisioningDeviceClientException
    {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = null;
            }
        };

        //act
        contractAPIMqtt.open(mockedRequestData);

        //assert
    }

    // SRS_ContractAPIMqtt_07_022: [If the amqpConnection is NULL or Is not open this method will do nothing.]
    @Test
    public void closeAmqpConnectionNullSucceeds() throws ProvisioningDeviceClientException, IOException
    {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();

        //act
        contractAPIMqtt.close();

        //assert
        new Verifications()
        {
            {
                //mockedProvisionAmqpConnection.close();
            }
        };
    }

    // SRS_ContractAPIMqtt_07_022: [If the amqpConnection is NULL or Is not open this method will do nothing.]
    @Test
    public void closeNotOpenSucceeds() throws ProvisioningDeviceClientException, IOException
    {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
            }
        };
        contractAPIMqtt.open(mockedRequestData);
        new NonStrictExpectations()
        {
            {
                mockedMqttConnection.isMqttConnected();
                result = false;
            }
        };

        //act
        contractAPIMqtt.close();

        //assert
        new Verifications()
        {
            {
            }
        };
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void closeThrowOnDisconnect() throws ProvisioningDeviceClientException, IOException
    {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
            }
        };
        contractAPIMqtt.open(mockedRequestData);
        new NonStrictExpectations()
        {
            {
                mockedMqttConnection.isMqttConnected();
                result = new IOException();
            }
        };

        //act
        contractAPIMqtt.close();

        //assert
    }

    // SRS_ContractAPIMqtt_07_023: [This method will close the amqpConnection connection.]
    @Test
    public void closeSucceeds() throws ProvisioningDeviceClientException, IOException
    {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();
        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedRequestData.getSslContext();
                result = mockedSslContext;
            }
        };
        contractAPIMqtt.open(mockedRequestData);
        new NonStrictExpectations()
        {
            {
                mockedMqttConnection.isMqttConnected();
                result = true;
            }
        };

        //act
        contractAPIMqtt.close();

        //assert
        new Verifications()
        {
            {
                mockedMqttConnection.disconnect();
            }
        };
    }

    //SRS_ContractAPIMqtt_07_003: [If responseCallback is null, this method shall throw ProvisioningDeviceClientException.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithProvisioningServiceThrowsOnResponseNull() throws ProvisioningDeviceClientException, IOException, InterruptedException {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();

        //act
        contractAPIMqtt.authenticateWithProvisioningService(mockedRequestData, null, null);
    }

    // SRS_ContractAPIMqtt_07_004: [If amqpConnection is null or not connected, this method shall throw ProvisioningDeviceConnectionException.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithProvisioningServiceThrowsOnNotConnected() throws ProvisioningDeviceClientException, IOException, InterruptedException {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();

        //act
        contractAPIMqtt.authenticateWithProvisioningService(mockedRequestData, mockedResponseCallback, null);
    }

    // SRS_ContractAPIMqtt_07_005: [This method shall send an AMQP message with the property of iotdps-register.]
    // SRS_ContractAPIMqtt_07_006: [This method shall wait MAX_WAIT_TO_SEND_MSG for a reply from the service.]
    @Test
    public void authenticateWithProvisioningServiceSucceeds() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();
        openContractAPI(contractAPIMqtt);

        new NonStrictExpectations()
        {
            {
                mockedMqttConnection.isMqttConnected();
                result = true;

                mockedMqttConnection.publishMessage(anyString, (MqttQos) any, null);

                mockedObjectLock.waitLock(anyInt);
            }
        };
        contractAPIMqtt.messageReceived(mockedMqttMessage);

        //act
        contractAPIMqtt.authenticateWithProvisioningService(mockedRequestData, mockedResponseCallback, null);

        //assert
        new Verifications()
        {
            {
                mockedMqttConnection.publishMessage(anyString, (MqttQos)any, null);
                times = 1;
            }
        };
    }

    // SRS_ContractAPIMqtt_07_009: [If requestData is null this method shall throw ProvisioningDeviceClientException.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnRequestNull() throws ProvisioningDeviceClientException, IOException, InterruptedException {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();

        //act
        contractAPIMqtt.getRegistrationStatus(null, mockedResponseCallback, null);
    }

    // SRS_ContractAPIMqtt_07_010: [If requestData.getOperationId() is null or empty, this method shall throw ProvisioningDeviceClientException.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnOperationIdNull() throws ProvisioningDeviceClientException, IOException, InterruptedException {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = null;
            }
        };

        //act
        contractAPIMqtt.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);
    }

    // SRS_ContractAPIMqtt_07_010: [If requestData.getOperationId() is null or empty, this method shall throw ProvisioningDeviceClientException.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnOperationIdEmpty() throws ProvisioningDeviceClientException, IOException, InterruptedException {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = "";
            }
        };

        //act
        contractAPIMqtt.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);
    }

    // SRS_ContractAPIMqtt_07_010: [If responseCallback is null, this method shall throw ProvisioningDeviceClientException.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnResponseNull() throws ProvisioningDeviceClientException, IOException, InterruptedException {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = TEST_OPERATION_ID;
            }
        };

        //act
        contractAPIMqtt.getRegistrationStatus(mockedRequestData, null, null);
    }

    // SRS_ContractAPIMqtt_07_012: [If amqpConnection is null or not connected, this method shall throw ProvisioningDeviceConnectionException.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnMqttNull() throws ProvisioningDeviceClientException, IOException, InterruptedException {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getOperationId();
                result = TEST_OPERATION_ID;
            }
        };

        //act
        contractAPIMqtt.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);
    }

    // SRS_ContractAPIMqtt_07_012: [If amqpConnection is null or not connected, this method shall throw ProvisioningDeviceConnectionException.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void getRegistrationStatusThrowsOnNotConnected() throws ProvisioningDeviceClientException, IOException, InterruptedException {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();

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

        contractAPIMqtt.open(mockedRequestData);
        new NonStrictExpectations()
        {
            {
                mockedMqttConnection.isMqttConnected();
                result = false;
            }
        };

        //act
        contractAPIMqtt.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);
    }

    // SRS_ContractAPIMqtt_07_013: [This method shall send an AMQP message with the property of iotdps-get-operationstatus and the OperationId.]
    // SRS_ContractAPIMqtt_07_014: [This method shall wait MAX_WAIT_TO_SEND_MSG for a reply from the service.]
    // SRS_ContractAPIMqtt_07_015: [If the service fails to reply in the alloted time this method shall throw ProvisioningDeviceClientException.]
    // SRS_ContractAPIMqtt_07_016: [This method shall responds to the responseCallback with amqp response data and the status DPS_REGISTRATION_RECEIVED.]
    @Test
    public void getRegistrationStatusSuccess() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();

        openContractAPI(contractAPIMqtt);

        new NonStrictExpectations()
        {
            {
                mockedMqttConnection.isMqttConnected();
                result = true;

                mockedMqttConnection.publishMessage(anyString, (MqttQos) any, null);

                mockedObjectLock.waitLock(anyInt);
            }
        };

        contractAPIMqtt.messageReceived(mockedMqttMessage);

        //act
        contractAPIMqtt.getRegistrationStatus(mockedRequestData, mockedResponseCallback, null);

        //assert
        new Verifications()
        {
            {
                mockedMqttConnection.publishMessage(anyString, (MqttQos)any, null);
                times = 1;
            }
        };
    }

    @Test  (expected = ProvisioningDeviceClientException.class)
    public void requestNonceForTPMThrowProvisioningDeviceClientException() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();

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
                result = mockedSslContext;
            }
        };

        //act
        contractAPIMqtt.requestNonceForTPM(mockedRequestData, mockedResponseCallback, null);

        //assert
    }

    @Test  (expected = ProvisioningDeviceClientException.class)
    public void requestNonceForTPMThrowResponseCallbackNull() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();
        openContractAPI(contractAPIMqtt);

        //act
        contractAPIMqtt.requestNonceForTPM(mockedRequestData, null, null);

        //assert
    }

    @Test  (expected = ProvisioningDeviceClientException.class)
    public void requestNonceForTPMThrowSslContextNull() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();

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
        contractAPIMqtt.requestNonceForTPM(mockedRequestData, mockedResponseCallback, null);

        //assert
    }

    @Test  (expected = ProvisioningDeviceClientException.class)
    public void requestNonceForTPMThrowStorageRootKeyNull() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();

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
        contractAPIMqtt.requestNonceForTPM(mockedRequestData, mockedResponseCallback, null);

        //assert
    }

    @Test  (expected = ProvisioningDeviceClientException.class)
    public void requestNonceForTPMThrowEndorsementKeyNull() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        ContractAPIMqtt contractAPIMqtt = createContractClass();

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
        contractAPIMqtt.requestNonceForTPM(mockedRequestData, mockedResponseCallback, null);

        //assert
    }

    @Test  (expected = ProvisioningDeviceClientException.class)
    public void requestNonceForTPMThrowRegistrationIdNull() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        ContractAPIMqtt contractAPIMqtt = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = null;
            }
        };

        //act
        contractAPIMqtt.requestNonceForTPM(mockedRequestData, mockedResponseCallback, null);

        //assert
    }

    @Test  (expected = ProvisioningDeviceClientException.class)
    public void requestNonceForTPMThrowRegistrationIdEmpty() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        ContractAPIMqtt contractAPIMqtt = createContractClass();

        new NonStrictExpectations()
        {
            {
                mockedRequestData.getRegistrationId();
                result = "";
            }
        };

        //act
        contractAPIMqtt.requestNonceForTPM(mockedRequestData, mockedResponseCallback, null);

        //assert
    }

    @Test  (expected = ProvisioningDeviceClientException.class)
    public void requestNonceForTPMThrowRequestDataNull() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        ContractAPIMqtt ContractAPIMqtt = createContractClass();

        //act
        ContractAPIMqtt.requestNonceForTPM(null, mockedResponseCallback, null);

        //assert
    }

    @Test  (expected = ProvisioningDeviceClientException.class)
    public void requestNonceForTPMSuccess() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        ContractAPIMqtt contractAPIMqtt = createContractClass();

        //act
        contractAPIMqtt.requestNonceForTPM(mockedRequestData, mockedResponseCallback, null);

        //assert
    }
}
