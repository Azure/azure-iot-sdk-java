// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.AmqpsMessage;
import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.MessageCallback;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.engine.Receiver;

import java.util.Map;

import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.DEVICE_OPERATION_UNKNOWN;

public final class AmqpsTelemetryReceiverLinkHandler extends AmqpsReceiverLinkHandler
{
    private static final String CORRELATION_ID_KEY = "com.microsoft:channel-correlation-id";

    private static final String DEVICE_RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/messages/devicebound";

    private static final String MODULE_RECEIVER_LINK_ENDPOINT_PATH_EDGEHUB = "/devices/%s/modules/%s/messages/events";
    private static final String MODULE_RECEIVER_LINK_ENDPOINT_PATH_IOTHUB = "/devices/%s/modules/%s/messages/devicebound";

    private static final String RECEIVER_LINK_TAG_PREFIX = "receiver_link_telemetry-";

    private static final String INPUT_NAME_PROPERTY_KEY = "x-opt-input-name";

    private static final String LINK_TYPE = "telemetry";

    private final DeviceClientConfig deviceClientConfig;

    AmqpsTelemetryReceiverLinkHandler(Receiver receiver, AmqpsLinkStateCallback amqpsLinkStateCallback, DeviceClientConfig deviceClientConfig, String linkCorrelationId)
    {
        super(receiver, amqpsLinkStateCallback, linkCorrelationId);

        this.deviceClientConfig = deviceClientConfig;

        this.receiverLinkTag = getTag(deviceClientConfig, linkCorrelationId);
        this.receiverLinkAddress = getAddress(deviceClientConfig);

        this.amqpProperties.put(Symbol.getSymbol(VERSION_IDENTIFIER_KEY), deviceClientConfig.getProductInfo().getUserAgentString());

        String moduleId = this.deviceClientConfig.getModuleId();
        if (moduleId != null)
        {
            this.amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(this.deviceClientConfig.getDeviceId() + "/" + moduleId));
        }
        else
        {
            this.amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(this.deviceClientConfig.getDeviceId()));
        }
    }

    static String getTag(DeviceClientConfig clientConfig, String linkCorrelationId)
    {
        String moduleId = clientConfig.getModuleId();
        String deviceId = clientConfig.getDeviceId();
        if (moduleId != null && !moduleId.isEmpty())
        {
            return RECEIVER_LINK_TAG_PREFIX + deviceId + "/" + moduleId + "-" + linkCorrelationId;
        }
        else
        {
            return RECEIVER_LINK_TAG_PREFIX + deviceId + "-" + linkCorrelationId;
        }
    }

    private static String getAddress(DeviceClientConfig deviceClientConfig)
    {
        String moduleId = deviceClientConfig.getModuleId();
        String deviceId = deviceClientConfig.getDeviceId();
        if (moduleId != null && !moduleId.isEmpty())
        {
            if (deviceClientConfig.getGatewayHostname() != null)
            {
                return String.format(MODULE_RECEIVER_LINK_ENDPOINT_PATH_EDGEHUB, deviceId, moduleId);
            }
            else
            {
                return String.format(MODULE_RECEIVER_LINK_ENDPOINT_PATH_IOTHUB, deviceId, moduleId);
            }
        }
        else
        {
            return String.format(DEVICE_RECEIVER_LINK_ENDPOINT_PATH, deviceId);
        }
    }

    @Override
    public String getLinkInstanceType()
    {
        return LINK_TYPE;
    }

    @Override
    protected IotHubTransportMessage protonMessageToIoTHubMessage(AmqpsMessage protonMsg)
    {
        IotHubTransportMessage iotHubTransportMessage = super.protonMessageToIoTHubMessage(protonMsg);
        iotHubTransportMessage.setMessageType(MessageType.DEVICE_TELEMETRY);
        iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_UNKNOWN);

        if (protonMsg.getMessageAnnotations() != null && protonMsg.getMessageAnnotations().getValue() != null)
        {
            Map<Symbol, Object> applicationProperties = protonMsg.getMessageAnnotations().getValue();
            for (Map.Entry<Symbol, Object> entry : applicationProperties.entrySet())
            {
                String propertyKey = entry.getKey().toString();
                if (propertyKey.equals(INPUT_NAME_PROPERTY_KEY))
                {
                    iotHubTransportMessage.setInputName(entry.getValue().toString());
                }
            }
        }

        //inputName may be null, and if it is, then the default callback and default callback context will be used from config
        String inputName = iotHubTransportMessage.getInputName();
        MessageCallback messageCallback = deviceClientConfig.getDeviceTelemetryMessageCallback(inputName);
        Object messageContext = deviceClientConfig.getDeviceTelemetryMessageContext(inputName);

        iotHubTransportMessage.setMessageCallback(messageCallback);
        iotHubTransportMessage.setMessageCallbackContext(messageContext);

        return iotHubTransportMessage;
    }
}
