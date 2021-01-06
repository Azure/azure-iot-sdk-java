// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.TopicParser;
import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.*;

public class TopicParserTest
{
    /*
    Tests_SRS_TopicParser_25_001: [**The constructor shall spilt the topic by "/" and save the tokens.**]**
     */
    @Test
    public void constructorSucceeds() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/";

        //act
        TopicParser testParser = new TopicParser(validString);

        //assert
        assertNotNull(testParser);
        String[] tokens = Deencapsulation.getField(testParser, "topicTokens");
        assertNotNull(tokens);
        assertTrue(tokens.length > 1);
    }

    /*
    Tests_SRS_TopicParser_25_002: [**The constructor shall throw TransportException if topic is null or empty.**]**
     */
    @Test (expected = TransportException.class)
    public void constructorFailsInvalidTopic() throws TransportException
    {
        //arrange
        String validString = "";

        //act
        TopicParser testParser = new TopicParser(validString);

    }

    /*
    Tests_SRS_TopicParser_25_004: [**This method shall return the status corresponding to the tokenIndexStatus from tokens if it is not null.**]**
     */
    @Test
    public void getStatusGets() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/status";
        TopicParser testParser = new TopicParser(validString);

        //act
        String status = Deencapsulation.invoke(testParser, "getStatus", 3);

        //assert
        assertNotNull(status);
        assertTrue(status.equals("status"));

    }

    /*
    Tests_SRS_TopicParser_25_003: [**If tokenIndexStatus is not valid i.e less than or equal to zero or greater then token length then getStatus shall throw TransportException.**]**
     */
    @Test (expected = TransportException.class)
    public void getStatusInvalidTokenThrows() throws TransportException
    {
        String validString = "$iothub/twin/res/status";
        TopicParser testParser = new TopicParser(validString);

        //act
        String status = Deencapsulation.invoke(testParser, "getStatus", 4);
    }

    /*
    Tests_SRS_TopicParser_25_005: [**If token corresponding to tokenIndexStatus is null then this method shall throw TransportException.**]**
     */
    @Test (expected = TransportException.class)
    public void getStatusMandatoryStatusExpected() throws TransportException
    {
        String validString = "$iothub/twin/res/";
        TopicParser testParser = new TopicParser(validString);

        //act
        String status = Deencapsulation.invoke(testParser, "getStatus", 3);
    }

    /*
    Tests_SRS_TopicParser_25_007: [**This method shall return the request ID value corresponding to the tokenIndexReqID from tokens.**]**
     */
    @Test
    public void getRequestIdGets() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/?$rid=5";
        TopicParser testParser = new TopicParser(validString);

        //act
        String status = Deencapsulation.invoke(testParser, "getRequestId", 3);

        //assert
        assertNotNull(status);
        assertTrue(status.equals(String.valueOf(5)));
    }

    @Test
    public void getRequestIdGets_pattern1() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/$rid=5";
        TopicParser testParser = new TopicParser(validString);

        //act
        String status = Deencapsulation.invoke(testParser, "getRequestId", 3);

        //assert
        assertNull(status);
    }

    @Test
    public void getRequestIdGets_pattern2() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/?$rid=";
        TopicParser testParser = new TopicParser(validString);

        //act
        String status = Deencapsulation.invoke(testParser, "getRequestId", 3);

        //assert
        assertNotNull(status);
    }

    @Test
    public void getRequestIdGets_pattern3() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/?$rid5&$version=7";
        TopicParser testParser = new TopicParser(validString);

        //act
        String status = Deencapsulation.invoke(testParser, "getRequestId", 3);

        assertNull(status);
    }
    /*
    Tests_SRS_TopicParser_25_008: [**If the topic token does not contain request id then this method shall return null.**]**
     */
    @Test
    public void getRequestIdGetsNullIfNotFound() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/?$version=5";
        TopicParser testParser = new TopicParser(validString);

        //act
        String status = Deencapsulation.invoke(testParser, "getRequestId", 3);

        //assert
        assertNull(status);

    }

    @Test
    public void getRequestIdOnTopicWithVersionGets() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/?$version=7&$rid=5";
        TopicParser testParser = new TopicParser(validString);

        //act
        String status = Deencapsulation.invoke(testParser, "getRequestId", 3);

        //assert
        assertNotNull(status);
        assertTrue(status.equals(String.valueOf(5)));

    }

    @Test
    public void getRequestIdOnTopicWithVersionBeforeRidGets() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/?$rid=5&$version=7";
        TopicParser testParser = new TopicParser(validString);

        //act
        String status = Deencapsulation.invoke(testParser, "getRequestId", 3);

        //assert
        assertNotNull(status);
        assertTrue(status.equals(String.valueOf(5)));

    }

    /*
    Tests_SRS_TopicParser_25_006: [**If tokenIndexReqID is not valid i.e less than or equal to zero or greater then token length then getRequestId shall throw TransportException.**]**
     */
    @Test (expected = TransportException.class)
    public void getRequestIdInvalidTokenThrows() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/?$rid=5&$version=7";
        TopicParser testParser = new TopicParser(validString);

        //act
        String status = Deencapsulation.invoke(testParser, "getRequestId", 4);

    }

    /*
    Tests_SRS_TopicParser_25_010: [**This method shall return the version value(if present) corresponding to the tokenIndexVersion from tokens.**]**
     */
    @Test
    public void getVersionGets() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/?$version=7";
        TopicParser testParser = new TopicParser(validString);

        //act
        String version = Deencapsulation.invoke(testParser, "getVersion", 3);

        //assert
        assertNotNull(version);
        assertTrue(version.equals(String.valueOf(7)));
    }

    @Test
    public void getVersionGets_pattern1() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/$version=7";
        TopicParser testParser = new TopicParser(validString);

        //act
        String status = Deencapsulation.invoke(testParser, "getVersion", 3);

        //assert
        assertNull(status);
    }

    @Test
    public void getVersionGets_pattern2() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/?$version=";
        TopicParser testParser = new TopicParser(validString);

        //act
        String status = Deencapsulation.invoke(testParser, "getVersion", 3);

        //assert
        assertNotNull(status);
    }

    @Test
    public void getVersionGets_pattern3() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/?$rid5&$version7";
        TopicParser testParser = new TopicParser(validString);

        //act
        String status = Deencapsulation.invoke(testParser, "getVersion", 3);

        assertNull(status);
    }

    /*
    Tests_SRS_TopicParser_25_011: [**If the topic token does not contain version then this method shall return null.**]**
     */
    @Test
    public void getVersionGetsNullIfNotPresent() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/?$rid=7";
        TopicParser testParser = new TopicParser(validString);

        //act
        String version = Deencapsulation.invoke(testParser, "getVersion", 3);

        //assert
        assertNull(version);
    }
    @Test
    public void getVersionOnTopicWithRIDGets() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/?$rid=5&$version=7";
        TopicParser testParser = new TopicParser(validString);

        //act
        String version = Deencapsulation.invoke(testParser, "getVersion", 3);

        //assert
        assertNotNull(version);
        assertTrue(version.equals(String.valueOf(7)));

    }

    @Test
    public void getVersionOnTopicWithVersionBeforeRIDGets() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/?$version=7&$rid=5";
        TopicParser testParser = new TopicParser(validString);

        //act
        String version = Deencapsulation.invoke(testParser, "getVersion", 3);

        //assert
        assertNotNull(version);
        assertTrue(version.equals(String.valueOf(7)));

    }
    /*
    Tests_SRS_TopicParser_25_009: [**If tokenIndexVersion is not valid i.e less than or equal to zero or greater then token length then getVersion shall throw TransportException.**]**
     */
    @Test (expected = TransportException.class)
    public void getVersionInvalidTokenThrows() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/?$version=7&$rid=5";
        TopicParser testParser = new TopicParser(validString);

        //act
        String version = Deencapsulation.invoke(testParser, "getVersion", 0);
    }

    /*
    Tests_SRS_TopicParser_25_013: [**This method shall return the method name(if present) corresponding to the tokenIndexMethod from tokens.**]**

    Tests_SRS_TopicParser_25_014: [**If the topic token does not contain method name or is null then this method shall throw TransportException.**]**
     */
    @Test
    public void getMethodNameGets() throws TransportException
    {
        //arrange
        String validString = "$iothub/methods/res/methodName";
        TopicParser testParser = new TopicParser(validString);

        //act
        String methodName = Deencapsulation.invoke(testParser, "getMethodName", 3);

        //assert
        assertNotNull(methodName);
        assertTrue(methodName.equals("methodName"));


    }

    /*
    Tests_SRS_TopicParser_25_012: [**If tokenIndexMethod is not valid i.e less than or equal to zero or greater then token length then getMethodName shall throw  TransportException.**]**
     */
    @Test (expected = TransportException.class)
    public void getMethodNameInvalidTokenThrows() throws TransportException
    {
        //arrange
        String validString = "$iothub/methods/res/methodName";
        TopicParser testParser = new TopicParser(validString);

        //act
        String methodName = Deencapsulation.invoke(testParser, "getMethodName", 4);
    }

    @Test (expected = TransportException.class)
    public void getMethodNameInvalidTokenThrows_1() throws TransportException
    {
        //arrange
        String validString = "$iothub/methods/res/";
        TopicParser testParser = new TopicParser(validString);

        //act
        String methodName = Deencapsulation.invoke(testParser, "getMethodName", 3);
    }

    @Test (expected = TransportException.class)
    public void getMethodNameInvalidTokenThrows_2() throws TransportException
    {
        //arrange
        String validString = "$iothub/methods/res//";
        TopicParser testParser = new TopicParser(validString);

        //act
        String methodName = Deencapsulation.invoke(testParser, "getMethodName", 3);
    }
}
