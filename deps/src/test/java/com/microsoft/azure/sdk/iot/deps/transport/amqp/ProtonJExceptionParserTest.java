/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.deps.transport.amqp;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.ProtonJExceptionParser;
import mockit.Expectations;
import mockit.Mocked;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Event;
import org.junit.Assert;
import org.junit.Test;

public class ProtonJExceptionParserTest
{
    @Mocked
    Event mockEvent;

    @Mocked
    ErrorCondition mockErrorCondition;

    private final String expectedErrorDescription = "an error occurred";
    private final String expectedErrorCondition = "amqp:foo";

    @Test
    public void errorFromTransport()
    {
        new Expectations()
        {
            {
                mockEvent.getTransport().getCondition();
                result = mockErrorCondition;

                mockErrorCondition.getDescription();
                result = expectedErrorDescription;

                mockErrorCondition.getCondition().toString();
                result = expectedErrorCondition;
            }
        };

        ProtonJExceptionParser exceptionParser = new ProtonJExceptionParser(mockEvent);

        Assert.assertEquals(expectedErrorCondition, exceptionParser.getError());
        Assert.assertEquals(expectedErrorDescription, exceptionParser.getErrorDescription());
    }

    @Test
    public void errorFromSender()
    {
        new Expectations()
        {
            {
                mockEvent.getSender().getCondition();
                result = mockErrorCondition;

                mockErrorCondition.getDescription();
                result = expectedErrorDescription;

                mockErrorCondition.getCondition().toString();
                result = expectedErrorCondition;
            }
        };

        ProtonJExceptionParser exceptionParser = new ProtonJExceptionParser(mockEvent);

        Assert.assertEquals(expectedErrorCondition, exceptionParser.getError());
        Assert.assertEquals(expectedErrorDescription, exceptionParser.getErrorDescription());
    }

    @Test
    public void errorFromReceiver()
    {
        new Expectations()
        {
            {
                mockEvent.getReceiver().getCondition();
                result = mockErrorCondition;

                mockErrorCondition.getDescription();
                result = expectedErrorDescription;

                mockErrorCondition.getCondition().toString();
                result = expectedErrorCondition;
            }
        };

        ProtonJExceptionParser exceptionParser = new ProtonJExceptionParser(mockEvent);

        Assert.assertEquals(expectedErrorCondition, exceptionParser.getError());
        Assert.assertEquals(expectedErrorDescription, exceptionParser.getErrorDescription());
    }

    @Test
    public void errorFromConnection()
    {
        new Expectations()
        {
            {
                mockEvent.getConnection().getCondition();
                result = mockErrorCondition;

                mockErrorCondition.getDescription();
                result = expectedErrorDescription;

                mockErrorCondition.getCondition().toString();
                result = expectedErrorCondition;
            }
        };

        ProtonJExceptionParser exceptionParser = new ProtonJExceptionParser(mockEvent);

        Assert.assertEquals(expectedErrorCondition, exceptionParser.getError());
        Assert.assertEquals(expectedErrorDescription, exceptionParser.getErrorDescription());
    }

    @Test
    public void errorFromSession()
    {
        new Expectations()
        {
            {
                mockEvent.getSession().getCondition();
                result = mockErrorCondition;

                mockErrorCondition.getDescription();
                result = expectedErrorDescription;

                mockErrorCondition.getCondition().toString();
                result = expectedErrorCondition;
            }
        };

        ProtonJExceptionParser exceptionParser = new ProtonJExceptionParser(mockEvent);

        Assert.assertEquals(expectedErrorCondition, exceptionParser.getError());
        Assert.assertEquals(expectedErrorDescription, exceptionParser.getErrorDescription());
    }

    @Test
    public void errorFromLink()
    {
        new Expectations()
        {
            {
                mockEvent.getLink().getCondition();
                result = mockErrorCondition;

                mockErrorCondition.getDescription();
                result = expectedErrorDescription;

                mockErrorCondition.getCondition().toString();
                result = expectedErrorCondition;
            }
        };

        ProtonJExceptionParser exceptionParser = new ProtonJExceptionParser(mockEvent);

        Assert.assertEquals(expectedErrorCondition, exceptionParser.getError());
        Assert.assertEquals(expectedErrorDescription, exceptionParser.getErrorDescription());
    }

    @Test
    public void noError()
    {
        new Expectations()
        {
            {
                mockEvent.getLink();
                result = null;

                mockEvent.getSession();
                result = null;

                mockEvent.getSender();
                result = null;

                mockEvent.getReceiver();
                result = null;

                mockEvent.getConnection();
                result = null;

                mockEvent.getTransport();
                result = null;
            }
        };

        ProtonJExceptionParser exceptionParser = new ProtonJExceptionParser(mockEvent);

        Assert.assertNull(exceptionParser.getError());
        Assert.assertNull(exceptionParser.getErrorDescription());
    }

    @Test
    public void errorWithNoDescription()
    {
        //errors without descriptions should set the description to a non-null value for logging purposes
        new Expectations()
        {
            {
                mockEvent.getTransport().getCondition();
                result = mockErrorCondition;

                mockErrorCondition.getDescription();
                result = expectedErrorDescription;

                mockErrorCondition.getCondition().toString();
                result = null;
            }
        };

        ProtonJExceptionParser exceptionParser = new ProtonJExceptionParser(mockEvent);

        Assert.assertNotNull(exceptionParser.getErrorDescription());
    }
}
