package com.microsoft.azure.sdk.iot.service.transport.amqps;

import org.apache.qpid.proton.engine.Session;

interface CbsSessionStateCallback
{
    void onAuthenticationSucceeded(Session session);
    void onAuthenticationFailed(Exception e);
}
