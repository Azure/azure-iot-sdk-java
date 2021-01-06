// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.IotHubMethod;
import com.microsoft.azure.sdk.iot.device.MessageProperty;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import org.junit.Test;

import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.DEVICE_OPERATION_UNKNOWN;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for IotHubTransportMessage
 * 100% methods, 100% lines covered
 */
public class IotHubTransportMessageTest
{
    /*
    **Tests_SRS_IOTHUBTRANSPORTMESSAGE_12_001: [**If the message body is null, the constructor shall throw an IllegalArgumentException thrown by base constructor.**]**
    */
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithByteArrayThrowsIllegalArgumentExceptionIfMessageBodyNull()
    {
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(null, MessageType.DEVICE_TELEMETRY);
    }

    /*
    **Tests_SRS_IOTHUBTRANSPORTMESSAGE_12_002: [**The constructor shall save the message body by calling super with the body as parameter.**]**
     */
    @Test
    public void constructorWithByteArraySavesMessageData()
    {
        // arrange
        byte[] data = new byte[1];

        // act
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(data, MessageType.DEVICE_TELEMETRY);

        // assert
        assertEquals(data[0], iotHubTransportMessage.getBytes()[0]);
    }

    /*
    **Tests_SRS_IOTHUBTRANSPORTMESSAGE_12_003: [**The constructor shall set the messageType to the given value by calling the super with the given value.**]**
    **Tests_SRS_IOTHUBTRANSPORTMESSAGE_12_015: [**The constructor shall initialize version, requestId and status to null.**]**
    **Tests_SRS_IOTHUBTRANSPORTMESSAGE_12_016: [**The constructor shall initialize operationType to UNKNOWN**]**
     */
    @Test
    public void constructorWithByteArraySetsMessageType()
    {
        // arrange
        byte[] data = new byte[1];
        MessageType messageType = MessageType.DEVICE_TWIN;

        // act
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(data, messageType);

        // assert
        assertEquals(messageType, iotHubTransportMessage.getMessageType());
        assertEquals(null, iotHubTransportMessage.getVersion());
        assertEquals(null, iotHubTransportMessage.getRequestId());
        assertEquals(null, iotHubTransportMessage.getStatus());
        assertEquals(DEVICE_OPERATION_UNKNOWN, iotHubTransportMessage.getDeviceOperationType());
    }

    /* Tests_SRS_IOTHUBTRANSPORTMESSAGE_21_001: [The constructor shall call the supper class with the body. This function do not evaluates this parameter.] */
    @Test
    public void constructorSuccess()
    {
        // arrange
        String body = "This is a valid body";

        // act
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(body);

        // assert
        assertEquals(body, new String(iotHubTransportMessage.getBytes()));
    }

    /*
    **Tests_SRS_IOTHUBTRANSPORTMESSAGE_12_004: [**The function shall set the version.**]**
     */
    @Test
    public void setVersionSetsTheVersion()
    {
        // arrange
        String versionStr = "abcdefg";
        byte[] data = new byte[1];
        MessageType messageType = MessageType.DEVICE_TWIN;
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(data, messageType);

        // act
        iotHubTransportMessage.setVersion(versionStr);

        // assert
        assertEquals(versionStr, iotHubTransportMessage.getVersion());
    }

    /*
    **Tests_SRS_IOTHUBTRANSPORTMESSAGE_12_005: [**The function shall return the value of the version either set by the setter or the default (null) if unset so far.**]**
     */
    @Test
    public void getVersionGetsTheVersion()
    {
        // arrange
        String versionStr = "abcdefg";
        byte[] data = new byte[1];
        MessageType messageType = MessageType.DEVICE_TWIN;
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(data, messageType);
        iotHubTransportMessage.setVersion(versionStr);

        // act
        String version = iotHubTransportMessage.getVersion();

        // assert
        assertEquals(versionStr, version);
    }

    /*
    **Tests_SRS_IOTHUBTRANSPORTMESSAGE_12_006: [**The function shall save the request id.**]**
     */
    @Test
    public void setRequestIdSetsTheRequestId()
    {
        // arrange
        String requestIdStr = "abcdefg";
        byte[] data = new byte[1];
        MessageType messageType = MessageType.DEVICE_TWIN;
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(data, messageType);

        // act
        iotHubTransportMessage.setRequestId(requestIdStr);

        // assert
        assertEquals(requestIdStr, iotHubTransportMessage.getRequestId());
    }

    /*
     **Tests_SRS_IOTHUBTRANSPORTMESSAGE_12_007: [**The function shall return the value of the request id either set by the setter or the default (null) if unset so far.**]**
     */
    @Test
    public void getRequestIdGetsTheRequestId()
    {
        // arrange
        String requestIdStr = "abcdefg";
        byte[] data = new byte[1];
        MessageType messageType = MessageType.DEVICE_TWIN;
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(data, messageType);
        iotHubTransportMessage.setRequestId(requestIdStr);

        // act
        String requestId = iotHubTransportMessage.getRequestId();

        // assert
        assertEquals(requestIdStr, requestId);
    }

    /*
     **Tests_SRS_IOTHUBTRANSPORTMESSAGE_12_008: [**The function shall save the status.**]**
     */
    @Test
    public void setStatusSetsTheStatus()
    {
        // arrange
        String StatusStr = "abcdefg";
        byte[] data = new byte[1];
        MessageType messageType = MessageType.DEVICE_TWIN;
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(data, messageType);

        // act
        iotHubTransportMessage.setStatus(StatusStr);

        // assert
        assertEquals(StatusStr, iotHubTransportMessage.getStatus());
    }

    /*
     **Tests_SRS_IOTHUBTRANSPORTMESSAGE_12_009: [**The function shall return the value of the status either set by the setter or the default (null) if unset so far.**]**
     */
    @Test
    public void getStatusGetsTheStatus()
    {
        // arrange
        String StatusStr = "abcdefg";
        byte[] data = new byte[1];
        MessageType messageType = MessageType.DEVICE_TWIN;
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(data, messageType);
        iotHubTransportMessage.setStatus(StatusStr);

        // act
        String Status = iotHubTransportMessage.getStatus();

        // assert
        assertEquals(StatusStr, Status);
    }

    /*
     **Tests_SRS_IOTHUBTRANSPORTMESSAGE_12_010: [**The function shall save the device twin operation type.**]**
     */
    @Test
    public void setOperationTypeSetsTheOperationType()
    {
        // arrange
        DeviceOperations deviceOperations = DeviceOperations.DEVICE_OPERATION_TWIN_GET_REQUEST;
        byte[] data = new byte[1];
        MessageType messageType = MessageType.DEVICE_TWIN;
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(data, messageType);

        // act
        iotHubTransportMessage.setDeviceOperationType(deviceOperations);

        // assert
        assertEquals(deviceOperations, iotHubTransportMessage.getDeviceOperationType());
    }

    /*
     **Tests_SRS_IOTHUBTRANSPORTMESSAGE_12_011: [**The function shall return the operation type either set by the setter or the default if unset so far.**]**
     */
    @Test
    public void getOperationTypeGetsTheOperationType()
    {
        // arrange
        DeviceOperations deviceOperations = DeviceOperations.DEVICE_OPERATION_TWIN_GET_REQUEST;
        byte[] data = new byte[1];
        MessageType messageType = MessageType.DEVICE_TWIN;
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(data, messageType);
        iotHubTransportMessage.setDeviceOperationType(deviceOperations);

        // act
        DeviceOperations operationType = iotHubTransportMessage.getDeviceOperationType();

        // assert
        assertEquals(deviceOperations, operationType);
    }

    /*
     **Tests_SRS_IOTHUBTRANSPORTMESSAGE_12_012: [**The function shall throw IllegalArgumentException if the methodName is null.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void setMethodNameThrowsIllegalArgumentExceptionIfMethodNameIsNull()
    {
        // arrange
        byte[] data = new byte[1];
        MessageType messageType = MessageType.DEVICE_TWIN;
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(data, messageType);

        // act
        iotHubTransportMessage.setMethodName(null);
    }

    /*
     **Tests_SRS_IOTHUBTRANSPORTMESSAGE_12_013: [**The function shall set the methodName.**]**
     */
    @Test
    public void setMethodNameSetsTheMethodName()
    {
        // arrange
        String methodNameStr = "abcdefg";
        byte[] data = new byte[1];
        MessageType messageType = MessageType.DEVICE_TWIN;
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(data, messageType);

        // act
        iotHubTransportMessage.setMethodName(methodNameStr);

        // assert
        assertEquals(methodNameStr, iotHubTransportMessage.getMethodName());
    }

    /*
     **Tests_SRS_IOTHUBTRANSPORTMESSAGE_12_014: [**The function shall return the methodName either set by the setter or the default (null) if unset so far.**]**
     */
    @Test
    public void getMethodNameGetsTheMethodName()
    {
        // arrange
        String methodNameStr = "abcdefg";
        byte[] data = new byte[1];
        MessageType messageType = MessageType.DEVICE_TWIN;
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(data, messageType);
        iotHubTransportMessage.setMethodName(methodNameStr);

        // act
        String methodName = iotHubTransportMessage.getMethodName();

        // assert
        assertEquals(methodNameStr, methodName);
    }

    /* Tests_SRS_IOTHUBTRANSPORTMESSAGE_21_002: [The setIotHubMethod shall store the iotHubMethod. This function do not evaluates this parameter.] */
    /* Tests_SRS_IOTHUBTRANSPORTMESSAGE_21_004: [The getIotHubMethod shall return the stored iotHubMethod.] */
    @Test
    public void setGetIotHubMethodSuccess()
    {
        // arrange
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage("This is a valid body");

        // act
        iotHubTransportMessage.setIotHubMethod(IotHubMethod.POST);

        // assert
        assertEquals(IotHubMethod.POST, iotHubTransportMessage.getIotHubMethod());
    }

    /* Tests_SRS_IOTHUBTRANSPORTMESSAGE_21_003: [The setUriPath shall store the uriPath. This function do not evaluates this parameter.] */
    /* Tests_SRS_IOTHUBTRANSPORTMESSAGE_21_005: [The getUriPath shall return the stored uriPath.] */
    @Test
    public void setGetUriPathSuccess()
    {
        // arrange
        String uriPath = "valid/uri";
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage("This is a valid body");

        // act
        iotHubTransportMessage.setUriPath(uriPath);

        // assert
        assertEquals(uriPath, iotHubTransportMessage.getUriPath());
    }

    //Tests_SRS_IOTHUBTRANSPORTMESSAGE_34_017: [This constructor shall return an instance of IotHubTransportMessage with provided bytes, messagetype, correlationid, messageid, and application properties.]
    @Test
    public void constructorWithPropertiesSavesProperties()
    {
        //arrange
        byte[] expectedData = new byte[] {12,34,56};
        MessageType expectedMessageType = MessageType.DEVICE_TELEMETRY;
        String expectedCorrelationId = "1234";
        String expectedMessageId = "5678";
        MessageProperty[] expectedProperties = new MessageProperty[2];
        expectedProperties[0] = new MessageProperty("bob", "job");
        expectedProperties[1] = new MessageProperty("john", "bill");

        //act
        IotHubTransportMessage transportMessage = new IotHubTransportMessage(expectedData, expectedMessageType, expectedMessageId, expectedCorrelationId, expectedProperties);

        //assert
        assertArrayEquals(expectedData, transportMessage.getBytes());
        assertEquals(expectedMessageType, transportMessage.getMessageType());
        assertEquals(expectedMessageId, transportMessage.getMessageId());
        assertEquals(expectedCorrelationId, transportMessage.getCorrelationId());
        assertEquals(expectedProperties.length, transportMessage.getProperties().length);
    }
}
