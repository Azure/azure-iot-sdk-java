/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.transport.amqp;

import lombok.Getter;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Endpoint;
import org.apache.qpid.proton.engine.Event;

@Getter
public class ProtonJExceptionParser
{
    private String error;
    private String errorDescription;

    public static String DEFAULT_ERROR = "unknown";
    public static String DEFAULT_ERROR_DESCRIPTION = "An unknown transport error has occurred";


    public ProtonJExceptionParser(Event event)
    {
        getTransportExceptionFromProtonEndpoints(event.getSender(), event.getReceiver(), event.getConnection(), event.getTransport(), event.getSession(), event.getLink());
    }

    private ErrorCondition getErrorConditionFromEndpoint(Endpoint endpoint)
    {
        return endpoint.getCondition() != null && endpoint.getCondition().getCondition() != null ? endpoint.getCondition() : endpoint.getRemoteCondition();
    }

    private void getTransportExceptionFromProtonEndpoints(Endpoint... endpoints)
    {
        error = DEFAULT_ERROR;
        errorDescription = DEFAULT_ERROR_DESCRIPTION;

        for (Endpoint endpoint : endpoints)
        {
            if (endpoint == null)
            {
                continue;
            }

            ErrorCondition errorCondition = getErrorConditionFromEndpoint(endpoint);
            if (errorCondition == null || errorCondition.getCondition() == null)
            {
                continue;
            }

            error = errorCondition.getCondition().toString();
            errorDescription = errorCondition.getDescription();
        }
    }
}
