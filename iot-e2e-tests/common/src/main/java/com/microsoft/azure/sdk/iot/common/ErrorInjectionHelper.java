/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common;

import com.microsoft.azure.sdk.iot.device.Message;

import java.util.UUID;

public class ErrorInjectionHelper
{
    public static final String FaultType_Tcp = "KillTcp";
    public static final String FaultType_AmqpConn = "KillAmqpConnection";
    public static final String FaultType_AmqpSess = "KillAmqpSession";
    public static final String FaultType_AmqpCBSReq = "KillAmqpCBSLinkReq";
    public static final String FaultType_AmqpCBSResp = "KillAmqpCBSLinkResp";
    public static final String FaultType_AmqpD2C = "KillAmqpD2CLink";
    public static final String FaultType_AmqpC2D = "KillAmqpC2DLink";
    public static final String FaultType_AmqpTwinReq = "KillAmqpTwinLinkReq";
    public static final String FaultType_AmqpTwinResp = "KillAmqpTwinLinkResp";
    public static final String FaultType_AmqpMethodReq = "KillAmqpMethodReqLink";
    public static final String FaultType_AmqpMethodResp = "KillAmqpMethodRespLink";
    public static final String FaultType_Throttle = "InvokeThrottling";
    public static final String FaultType_QuotaExceeded = "InvokeMaxMessageQuota";
    public static final String FaultType_Auth = "InvokeAuthError";
    public static final String FaultType_GracefulShutdownAmqp = "ShutDownAmqp";
    public static final String FaultType_GracefulShutdownMqtt = "ShutDownMqtt";

    public static final String FaultCloseReason_Boom = "Boom";
    public static final String FaultCloseReason_Bye = "byebye";

    public static final int DefaultDelayInSec = 1;
    public static final int DefaultDurationInSec = 5;

    public static Message tcpConnectionDropErrorInjectionMessage(int delayInSecs, int durationInSecs)
    {
        return createMessageWithErrorInjectionProperties(FaultType_Tcp, FaultCloseReason_Boom, delayInSecs, durationInSecs);
    }

    public static Message amqpsConnectionDropErrorInjectionMessage(int delayInSecs, int durationInSecs)
    {
        return createMessageWithErrorInjectionProperties(FaultType_AmqpConn, FaultCloseReason_Boom, delayInSecs, durationInSecs);
    }

    public static Message amqpsSessionDropErrorInjectionMessage(int delayInSecs, int durationInSecs)
    {
        return createMessageWithErrorInjectionProperties(FaultType_AmqpSess, FaultCloseReason_Boom, delayInSecs, durationInSecs);
    }

    public static Message amqpsCBSReqLinkDropErrorInjectionMessage(int delayInSecs, int durationInSecs)
    {
        return createMessageWithErrorInjectionProperties(FaultType_AmqpCBSReq, FaultCloseReason_Boom, delayInSecs, durationInSecs);
    }

    public static Message amqpsCBSRespLinkDropErrorInjectionMessage(int delayInSecs, int durationInSecs)
    {
        return createMessageWithErrorInjectionProperties(FaultType_AmqpCBSResp, FaultCloseReason_Boom, delayInSecs, durationInSecs);
    }

    public static Message amqpsD2CTelemetryLinkDropErrorInjectionMessage(int delayInSecs, int durationInSecs)
    {
        return createMessageWithErrorInjectionProperties(FaultType_AmqpD2C, FaultCloseReason_Boom, delayInSecs, durationInSecs);
    }

    public static Message amqpsC2DLinkDropErrorInjectionMessage(int delayInSecs, int durationInSecs)
    {
        return createMessageWithErrorInjectionProperties(FaultType_AmqpC2D, FaultCloseReason_Boom, delayInSecs, durationInSecs);
    }

    public static Message createMessageWithErrorInjectionProperties(String faultType, String reason, int delayInSecs, int durationInSecs)
    {
        String dataBuffer = UUID.randomUUID().toString();
        Message message = new Message(dataBuffer);
        message.setProperty("AzIoTHub_FaultOperationType", faultType);
        message.setProperty("AzIoTHub_FaultOperationCloseReason", reason);
        message.setProperty("AzIoTHub_FaultOperationDelayInSecs", String.valueOf(delayInSecs));
        message.setProperty("AzIoTHub_FaultOperationDurationInSecs", String.valueOf(durationInSecs));

        return message;
    }
}
