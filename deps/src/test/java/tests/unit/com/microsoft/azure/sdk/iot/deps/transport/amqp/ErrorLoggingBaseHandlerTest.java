/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.unit.com.microsoft.azure.sdk.iot.deps.transport.amqp;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.ErrorLoggingBaseHandler;
import com.microsoft.azure.sdk.iot.deps.transport.amqp.ProtonJExceptionParser;
import mockit.Expectations;
import mockit.Mocked;
import org.apache.qpid.proton.engine.Event;
import org.junit.Test;

public class ErrorLoggingBaseHandlerTest
{
    @Mocked Event mockEvent;

    @Mocked
    ProtonJExceptionParser mockProtonJExceptionParser;

    @Test
    public void onTransportErrorParsesError()
    {
        new Expectations()
        {
            {
                new ProtonJExceptionParser(mockEvent);
                result = mockProtonJExceptionParser;

                mockProtonJExceptionParser.getError();
                result = "amqp:io";
            }
        };

        ErrorLoggingBaseHandler errorLoggingBaseHandler = new ErrorLoggingBaseHandler();
        errorLoggingBaseHandler.onTransportError(mockEvent);
    }
}
