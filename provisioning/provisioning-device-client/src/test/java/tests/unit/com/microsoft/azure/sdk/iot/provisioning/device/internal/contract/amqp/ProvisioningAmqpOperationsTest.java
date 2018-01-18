/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.amqp;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.AmqpListener;
import com.microsoft.azure.sdk.iot.deps.transport.amqp.AmqpMessage;
import com.microsoft.azure.sdk.iot.deps.transport.amqp.AmqpsConnection;
import com.microsoft.azure.sdk.iot.deps.util.ObjectLock;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.amqp.ProvisioningAmqpOperations;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceConnectionException;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.qpid.proton.amqp.Binary;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;

/*
 * Unit tests for ContractAPIHttp
 * Code coverage : 100% methods, 100% lines
 */
@RunWith(JMockit.class)
public class ProvisioningAmqpOperationsTest
{
    private static final String TEST_SCOPE_ID = "testScopeID";
    private static final String TEST_HOST_NAME = "testHostName";
    private static final String TEST_REGISTRATION_ID = "testRegistrationId";
    private static final String TEST_OPERATION_ID = "testOperationId";

    @Mocked
    private AmqpsConnection mockedAmqpConnection;

    @Mocked
    private SSLContext mockedSSLContext;

    @Mocked
    private ResponseCallback mockedResponseCallback;

    @Mocked
    private AmqpMessage mockedAmqpMessage;

    @Mocked
    private LinkedBlockingQueue<AmqpMessage> mockedQueueMessage = new LinkedBlockingQueue<>();

    @Mocked
    private byte[] mockedData;

    @Mocked
    private Binary mockedBinaryData;

    @Mocked
    private ObjectLock mockedObjectLock = new ObjectLock();


    private void setupSendReceiveMocks() throws Exception
    {
        new NonStrictExpectations()
        {
            {
                new AmqpMessage();
                result = mockedAmqpMessage;

                mockedAmqpConnection.sendAmqpMessage(mockedAmqpMessage);

                mockedObjectLock.waitLock(anyLong);

                mockedQueueMessage.size();
                result = 1;

                mockedQueueMessage.remove();
                result = mockedAmqpMessage;

                mockedAmqpMessage.getAmqpBody();
                result = mockedData;
            }
        };
    }

    // SRS_ContractAPIAmqp_07_001: [The constructor shall save the scope id and hostname.]
    @Test
    public void constructorSucceeds() throws ProvisioningDeviceClientException
    {
        //arrange

        //act
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //assert
        assertEquals(TEST_SCOPE_ID, Deencapsulation.getField(provisioningAmqpOperations, "idScope"));
        assertEquals(TEST_HOST_NAME, Deencapsulation.getField(provisioningAmqpOperations, "hostName"));
    }

    // SRS_ProvisioningAmqpOperations_07_002: [The constructor shall throw ProvisioningDeviceClientException if either scopeId and hostName are null or empty.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullScopeId() throws ProvisioningDeviceClientException
    {
        //arrange

        //act
        ProvisioningAmqpOperations contractAPIAmqp = new ProvisioningAmqpOperations(null, TEST_HOST_NAME);

        //assert
    }

    // SRS_ProvisioningAmqpOperations_07_002: [The constructor shall throw ProvisioningDeviceClientException if either scopeId and hostName are null or empty.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullHostName() throws ProvisioningDeviceClientException
    {
        //arrange

        //act
        ProvisioningAmqpOperations contractAPIAmqp = new ProvisioningAmqpOperations(TEST_SCOPE_ID, null);

        //assert
    }

    @Test
    public void isAmqpConnectedNotConnectedSucceeds() throws Exception
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        boolean isConnected = provisioningAmqpOperations.isAmqpConnected();

        //assert
        assertEquals(false, isConnected);
    }

    @Test
    public void isAmqpConnectedSucceeds() throws Exception
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.open();
            }
        };

        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, null, false);
        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.isConnected();
                result = true;
            }
        };

        //act
        boolean isConnected = provisioningAmqpOperations.isAmqpConnected();

        //assert
        assertEquals(true, isConnected);
    }

    // SRS_ProvisioningAmqpOperations_07_004: [open shall throw ProvisioningDeviceClientException if either registrationId or sslContext are null or empty.]
    @Test (expected = ProvisioningDeviceConnectionException.class)
    public void openThrowsOnNullRegistrationId() throws ProvisioningDeviceClientException
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        provisioningAmqpOperations.open(null, mockedSSLContext, null, false);

        //assert
    }

    // SRS_ProvisioningAmqpOperations_07_004: [open shall throw ProvisioningDeviceClientException if either registrationId or sslContext are null or empty.]
    @Test (expected = ProvisioningDeviceConnectionException.class)
    public void openThrowsOnEmptyRegistrationId() throws ProvisioningDeviceClientException
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        provisioningAmqpOperations.open("", mockedSSLContext, null, false);

        //assert
    }

    // SRS_ProvisioningAmqpOperations_07_004: [open shall throw ProvisioningDeviceClientException if either registrationId or sslContext are null or empty.]
    @Test (expected = ProvisioningDeviceConnectionException.class)
    public void openThrowsOnNullSSLContext() throws ProvisioningDeviceClientException
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, null, null, false);

        //assert
    }

    // SRS_ProvisioningAmqpOperations_07_004: [open shall throw ProvisioningDeviceClientException if either registrationId or sslContext are null or empty.]
    @Test (expected = ProvisioningDeviceConnectionException.class)
    public void openThrowsOnOpenFailure() throws ProvisioningDeviceClientException, IOException
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.openAmqpAsync();
                result = new Exception();
            }
        };

        //act
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, null, false);
    }

    // SRS_ProvisioningAmqpOperations_07_005: [This method shall construct the Link Address with /<scopeId>/registrations/<registrationId>.]
    @Test
    public void openSucceeds() throws ProvisioningDeviceClientException, IOException
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.open();
            }
        };

        //act
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, null, false);

        //assert
        new Verifications()
        {
            {
                mockedAmqpConnection.openAmqpAsync();
                times = 1;
            }
        };
    }

    @Test (expected = IOException.class)
    public void closeThrowsIoException() throws ProvisioningDeviceClientException, IOException
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.open();
            }
        };
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, null, false);

        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.close();
                result = new IOException();
            }
        };

        //act
        provisioningAmqpOperations.close();

        //assert
    }

    // SRS_ProvisioningAmqpOperations_07_007: [If amqpConnection is null, this method shall do nothing.]
    // SRS_ProvisioningAmqpOperations_07_008: [This method shall call close on amqpConnection.]
    @Test
    public void closeSucceeds() throws ProvisioningDeviceClientException, IOException
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.open();
            }
        };
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, null, false);

        //act
        provisioningAmqpOperations.close();

        //assert
        new Verifications()
        {
            {
                mockedAmqpConnection.close();
                times = 1;
            }
        };
    }

    // SRS_ProvisioningAmqpOperations_07_015: [sendStatusMessage shall throw ProvisioningDeviceClientException if either operationId or responseCallback are null or empty.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void sendStatusThrowsOnOperationIdNull() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        provisioningAmqpOperations.sendStatusMessage(null, mockedResponseCallback, null);

        //assert
    }

    // SRS_ProvisioningAmqpOperations_07_015: [sendStatusMessage shall throw ProvisioningDeviceClientException if either operationId or responseCallback are null or empty.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void sendStatusThrowsOnOperationIdEmpty() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        provisioningAmqpOperations.sendStatusMessage("", mockedResponseCallback, null);

        //assert
    }

    // SRS_ProvisioningAmqpOperations_07_015: [sendStatusMessage shall throw ProvisioningDeviceClientException if either operationId or responseCallback are null or empty.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void sendStatusThrowsOnResponseCallbackNull() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        provisioningAmqpOperations.sendStatusMessage(TEST_OPERATION_ID, null, null);

        //assert
    }

    // SRS_ProvisioningAmqpOperations_07_016: [This method shall send the Operation Status AMQP Provisioning message.]
    // SRS_ProvisioningAmqpOperations_07_017: [This method shall wait for the response of this message for MAX_WAIT_TO_SEND_MSG and call the responseCallback with the reply.]
    @Test
    public void sendStatusMessageSucceeds() throws Exception
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.open();
            }
        };
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, null, false);

        setupSendReceiveMocks();

        //act
        provisioningAmqpOperations.sendStatusMessage(TEST_OPERATION_ID, mockedResponseCallback, null);

        //assert
    }

    // SRS_ProvisioningAmqpOperations_07_018: [This method shall throw ProvisioningDeviceClientException if any failure is encountered.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void sendStatusMessageThrowsOnWaitLock() throws Exception
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.open();
            }
        };
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, null, false);

        new NonStrictExpectations()
        {
            {
                new AmqpMessage();
                result = mockedAmqpMessage;

                mockedAmqpConnection.sendAmqpMessage(mockedAmqpMessage);

                mockedObjectLock.waitLock(anyLong);
                result = new InterruptedException();
            }
        };

        //act
        provisioningAmqpOperations.sendStatusMessage(TEST_OPERATION_ID, mockedResponseCallback, null);

        //assert
    }

    // SRS_ProvisioningAmqpOperations_07_018: [This method shall throw ProvisioningDeviceClientException if any failure is encountered.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void sendStatusMessageThrowsOnSendAmqpMessage() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener) any);
                mockedAmqpConnection.open();
            }
        };
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, null, false);

        new NonStrictExpectations()
        {
            {
                new AmqpMessage();
                result = new Exception();
            }
        };

        //act
        provisioningAmqpOperations.sendStatusMessage(TEST_OPERATION_ID, mockedResponseCallback, null);

        //assert
    }

    // SRS_ProvisioningAmqpOperations_07_009: [sendRegisterMessage shall throw ProvisioningDeviceClientException if either responseCallback is null.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void sendRegisterMessageThrowsOnResponseCallbackNull() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        provisioningAmqpOperations.sendRegisterMessage(null, null);

        //assert
    }

    // SRS_ProvisioningAmqpOperations_07_012: [This method shall throw ProvisioningDeviceClientException if any failure is encountered.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void sendRegisterMessageThrowsInterrruptedException() throws Exception
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.open();
            }
        };
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, null, false);

        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.sendAmqpMessage(mockedAmqpMessage);

                mockedAmqpConnection.isConnected();
                result = new InterruptedException();
            }
        };

        //act
        provisioningAmqpOperations.sendRegisterMessage(mockedResponseCallback, null);

        //assert
    }

    // SRS_ProvisioningAmqpOperations_07_010: [This method shall send the Register AMQP Provisioning message.]
    // SRS_ProvisioningAmqpOperations_07_011: [This method shall wait for the response of this message for MAX_WAIT_TO_SEND_MSG and call the responseCallback with the reply.]
    @Test
    public void sendRegisterMessageSucceeds() throws Exception
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);
        new NonStrictExpectations()
        {
            {
                mockedAmqpConnection.setListener((AmqpListener)any);
                mockedAmqpConnection.open();
                mockedAmqpConnection.isConnected();
                result = true;
            }
        };
        provisioningAmqpOperations.open(TEST_REGISTRATION_ID, mockedSSLContext, null, false);

        setupSendReceiveMocks();

        //act
        provisioningAmqpOperations.sendRegisterMessage(mockedResponseCallback, null);

        //assert
    }

    // SRS_ProvisioningAmqpOperations_07_013: [This method shall add the message to a message queue.]
    // SRS_ProvisioningAmqpOperations_07_014: [This method shall then Notify the receiveLock.]
    @Test
    public void MessageReceivedSucceeds() throws ProvisioningDeviceClientException, IOException, InterruptedException
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        provisioningAmqpOperations.messageReceived(mockedAmqpMessage);

        //assert
        new Verifications()
        {
            {
                mockedObjectLock.notifyLock();
                times = 1;
            }
        };
    }

    @Test
    public void UnusedFunctionsSucceeds() throws ProvisioningDeviceClientException
    {
        //arrange
        ProvisioningAmqpOperations provisioningAmqpOperations = new ProvisioningAmqpOperations(TEST_SCOPE_ID, TEST_HOST_NAME);

        //act
        provisioningAmqpOperations.connectionEstablished();
        provisioningAmqpOperations.connectionLost();
        provisioningAmqpOperations.messageSent();

        //assert
    }
}
