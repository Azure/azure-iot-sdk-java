// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

public class TopicParser
{
    private final String[] topicTokens;

    private static final String QUESTION = "?";

    private static final String REQ_ID = "$rid=";
    private static final String VERSION = "$version=";

    public TopicParser(String topic)
    {
        if (topic == null || topic.length() == 0)
        {
            throw new IllegalArgumentException("Unexpected topic");
        }

        this.topicTokens = topic.split("/");
    }

    @SuppressWarnings("SameParameterValue") // Method is designed to be generic, with any acceptable value for "tokenIndexReqID".
    public String getRequestId(int tokenIndexReqID)
    {
        String reqId = null;

        if (tokenIndexReqID <= 0 || tokenIndexReqID >= topicTokens.length)
        {
            throw new IllegalArgumentException("Invalid token Index for request id");
        }

        String token = topicTokens[tokenIndexReqID];

        if (token.contains(REQ_ID) && token.contains(QUESTION)) // restriction for request id
        {
            int startIndex = token.indexOf(REQ_ID) + REQ_ID.length();
            int endIndex = token.length();

            if (token.contains(VERSION) && !token.contains(QUESTION + VERSION))
            {
                // version after rid in the query
                endIndex = token.indexOf(VERSION) - 1;
            }

            reqId = token.substring(startIndex, endIndex);
        }

        return reqId;
    }

    @SuppressWarnings("SameParameterValue") // Method is designed to be generic, with any acceptable value for "tokenIndexMethod"
    public String getMethodName(int tokenIndexMethod)
    {
        String methodName;

        if (tokenIndexMethod <= 0 || tokenIndexMethod >= topicTokens.length)
        {
            throw new IllegalArgumentException("Invalid token Index for Method Name");
        }

        String token = topicTokens[tokenIndexMethod];

        if (token != null)
        {
            methodName = token;
        }
        else
        {
            throw new IllegalArgumentException("method name could not be parsed");
        }

        return methodName;
    }
}
