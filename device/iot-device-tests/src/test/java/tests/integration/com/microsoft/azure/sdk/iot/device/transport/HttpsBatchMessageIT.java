// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.integration.com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsBatchMessage;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsSingleMessage;
import org.junit.Test;

import javax.naming.SizeLimitExceededException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/** Integration tests for HttpsBatchMessage. */
public class HttpsBatchMessageIT
{
    protected Charset UTF8 = StandardCharsets.UTF_8;

    @Test
    public void batchMessageSetsFieldsCorrectly() throws
            SizeLimitExceededException
    {
        String msgBytes0 = "abc";
        Message msg0 = new Message(msgBytes0);
        String messageid0 = msg0.getMessageId();
        msg0.setProperty("prop-0", "value-0");
        HttpsSingleMessage httpsMsg0 = HttpsSingleMessage.parseHttpsMessage(msg0);

        byte[] msgBytes1 = { 48, 49, 50 };
        Message msg1 = new Message(msgBytes1);
        String messageid1 = msg1.getMessageId();
        msg1.setProperty("prop-1", "value-1");
        HttpsSingleMessage httpsMsg1 = HttpsSingleMessage.parseHttpsMessage(msg1);
        HttpsBatchMessage batch = new HttpsBatchMessage();
        batch.addMessage(httpsMsg0);
        batch.addMessage(httpsMsg1);

        // JSON body with whitespace removed.
        String testBatchBody = new String(batch.getBody(), UTF8).replaceAll("\\s", "");

        String expectedBatchBody = "["
                + "{\"body\":\"abc\","
                + "\"base64Encoded\":false,"
                + "\"properties\":{"
                +   "\"iothub-app-prop-0\":\"value-0\","
                +   "\"iothub-messageid\":\"" + messageid0 + "\"}},"
                + "{\"body\":\"012\","
                + "\"base64Encoded\":false,"
                + "\"properties\":{"
                +   "\"iothub-app-prop-1\":\"value-1\","
                +   "\"iothub-messageid\":\"" + messageid1 + "\"}}"
                + "]";


        assertThat(testBatchBody, is(expectedBatchBody));
    }
}
