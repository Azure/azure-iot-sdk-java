// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import java.io.IOException;

public class TopicParser
{
    private String[] topicTokens = null;

    private final String QUESTION = "?";

    private final String REQ_ID = "$rid=";
    private final String VERSION = "$version=";

    public TopicParser(String topic)
    {
        if (topic == null || topic.length() == 0)
        {
            /*
            Codes_SRS_TopicParser_25_002: [**The constructor shall throw IllegalArgument exception if topic is null or empty.**]**
             */
            throw new IllegalArgumentException("topic cannot be null or empty");
        }

        /*
        Codes_SRS_TopicParser_25_001: [**The constructor shall spilt the topic by "/" and save the tokens.**]**
         */
        this.topicTokens = topic.split("/");
    }

    protected String getStatus(int tokenIndexStatus) throws IOException
    {
        String status = null;

        if (tokenIndexStatus <= 0 || tokenIndexStatus >= topicTokens.length)
        {
            /*
            Codes_SRS_TopicParser_25_003: [**If tokenIndexStatus is not valid i.e less than or equal to zero or greater then token length then getStatus shall throw  IllegalArgumentException.**]**
             */
            throw new IllegalArgumentException("Invalid token Index for status");
        }

        if (topicTokens.length > tokenIndexStatus)
        {
            String token = topicTokens[tokenIndexStatus];

            if (token != null)
            {
                /*
                Codes_SRS_TopicParser_25_004: [**This method shall return the status corresponding to the tokenIndexStatus from tokens if it is not null.**]**
                 */
                status = token;
            }
            else
            {
            /*
            Codes_SRS_TopicParser_25_005: [**If token corresponding to tokenIndexStatus is null then this method shall throw IoException.**]**
             */
                throw new IOException("Status could not be parsed");
            }
        }

        return status;
    }

    String getRequestId(int tokenIndexReqID)
    {
        String reqId = null;

        if (tokenIndexReqID <= 0 || tokenIndexReqID >= topicTokens.length)
        {
            /*
            Codes_SRS_TopicParser_25_006: [**If tokenIndexReqID is not valid i.e less than or equal to zero or greater then token length then getRequestId shall throw  IllegalArgumentException.**]**
             */
            throw new IllegalArgumentException("Invalid token Index for request id");
        }

        if (topicTokens.length > tokenIndexReqID)
        {
            String token = topicTokens[tokenIndexReqID];

            /*
            Codes_SRS_TopicParser_25_008: [**If the topic token does not contain request id then this method shall return null.**]**
             */
            if (token.contains(REQ_ID) && token.contains(QUESTION)) // restriction for request id
            {
                int startIndex = token.indexOf(REQ_ID) + REQ_ID.length();
                int endIndex = token.length();

                if (token.contains(VERSION) && !token.contains(QUESTION + VERSION))
                {
                    // version after rid in the query
                    endIndex = token.indexOf(VERSION) - 1;
                }

                /*
                Codes_SRS_TopicParser_25_007: [**This method shall return the request ID value corresponding to the tokenIndexReqID from tokens.**]**
                 */
                reqId = token.substring(startIndex, endIndex);
            }
        }

        return reqId;
    }

    protected String getVersion(int tokenIndexVersion)
    {
        String version = null;

        if (tokenIndexVersion <= 0 || tokenIndexVersion >= topicTokens.length)
        {
            /*
            Codes_SRS_TopicParser_25_009: [**If tokenIndexVersion is not valid i.e less than or equal to zero or greater then token length then getVersion shall throw  IllegalArgumentException.**]**
             */
            throw new IllegalArgumentException("Invalid token Index for Version");
        }

        if (topicTokens.length > tokenIndexVersion)
        {

            String token = topicTokens[tokenIndexVersion];

            /*
            Codes_SRS_TopicParser_25_010: [**This method shall return the version value(if present) corresponding to the tokenIndexVersion from tokens.**]**
            Codes_SRS_TopicParser_25_011: [**If the topic token does not contain version then this method shall return null.**]**
             */
            if (token.contains(VERSION) && token.contains(QUESTION) ) //restriction for version
            {
                int startIndex = token.indexOf(VERSION) + VERSION.length();
                int endIndex = token.length();

                if(!token.contains(QUESTION + REQ_ID) && token.contains(REQ_ID))
                {
                    endIndex = token.indexOf(REQ_ID) - 1;
                }

                version = token.substring(startIndex, endIndex);
            }
        }

        return version;
    }


    String getMethodName(int tokenIndexMethod) throws IOException
    {
        String methodName = null;

        if (tokenIndexMethod <= 0 || tokenIndexMethod >= topicTokens.length)
        {
            /*
            Codes_SRS_TopicParser_25_012: [**If tokenIndexMethod is not valid i.e less than or equal to zero or greater then token length then getMethodName shall throw  IllegalArgumentException.**]**
             */
            throw new IllegalArgumentException("Invalid token Index for Method Name");
        }

        if (topicTokens.length > tokenIndexMethod)
        {
            String token = topicTokens[tokenIndexMethod];
            /*
            Codes_SRS_TopicParser_25_013: [**This method shall return the method name(if present) corresponding to the tokenIndexMethod from tokens.**]**

            Codes_SRS_TopicParser_25_014: [**If the topic token does not contain method name or is null then this method shall throw IOException.**]**

            */
            if (token != null)
            {
                methodName = token;
            }
            else
            {
                throw new IOException("method name could not be parsed");
            }
        }

        return methodName;
    }

}
