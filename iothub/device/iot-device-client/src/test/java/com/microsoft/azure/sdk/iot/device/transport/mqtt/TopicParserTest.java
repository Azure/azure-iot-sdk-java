// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.transport.TransportException;
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
    @Test (expected = IllegalArgumentException.class)
    public void constructorFailsInvalidTopic() throws TransportException
    {
        //arrange
        String validString = "";

        //act
        TopicParser testParser = new TopicParser(validString);

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
        assertEquals(status, String.valueOf(5));
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
        assertEquals(status, String.valueOf(5));

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
        assertEquals(status, String.valueOf(5));

    }

    /*
    Tests_SRS_TopicParser_25_006: [**If tokenIndexReqID is not valid i.e less than or equal to zero or greater then token length then getRequestId shall throw TransportException.**]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void getRequestIdInvalidTokenThrows() throws TransportException
    {
        //arrange
        String validString = "$iothub/twin/res/?$rid=5&$version=7";
        TopicParser testParser = new TopicParser(validString);

        //act
        String status = Deencapsulation.invoke(testParser, "getRequestId", 4);

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
        assertEquals("methodName", methodName);


    }
}
