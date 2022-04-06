// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageSentCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.MessagesSentCallback;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;

import java.util.List;

public class EventCallback implements MessageSentCallback, MessagesSentCallback
{
    private final IotHubStatusCode expectedStatusCode;
    public EventCallback(IotHubStatusCode expectedStatusCode)
    {
        this.expectedStatusCode = expectedStatusCode;
    }

    public void onMessageSent(Message sentMessage, IotHubClientException e, Object context)
    {
        if (context != null)
        {
            Success success = (Success) context;

            //null case is for testing that the callback is fired, but not caring what the status code was.
            // In some error injection scenarios, the status code reported cannot be predicted, but the callback
            // still must have been fired.
            if (this.expectedStatusCode == null)
            {
                success.setResult(true);
            }
            else if (this.expectedStatusCode == IotHubStatusCode.OK && e == null)
            {
                success.setResult(true);
            }
            else
            {
                success.setResult(e.getStatusCode().equals(expectedStatusCode));
            }

            success.setCallbackStatusCode(e == null ? IotHubStatusCode.OK : e.getStatusCode());

            success.callbackWasFired();

        }
    }

    @Override
    public void onMessagesSent(List<Message> sentMessages, IotHubClientException clientException, Object callbackContext)
    {
        // just call the single message callback since they will behave the same for these tests
        this.onMessageSent(null, clientException, callbackContext);
    }
}

