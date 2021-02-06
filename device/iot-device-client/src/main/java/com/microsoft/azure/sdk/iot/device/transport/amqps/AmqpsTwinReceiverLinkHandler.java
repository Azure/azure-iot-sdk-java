// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.MessageCallback;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.engine.Receiver;

import java.util.Map;

import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.*;

@Slf4j
final class AmqpsTwinReceiverLinkHandler extends AmqpsReceiverLinkHandler
{
    private static final String CORRELATION_ID_KEY = "com.microsoft:channel-correlation-id";
    private static final String CORRELATION_ID_KEY_PREFIX = "twin:";

    private static final String DEVICE_RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/twin";

    private static final String MODULE_RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/modules/%s/twin";

    private static final String RECEIVER_LINK_TAG_PREFIX = "receiver_link_devicetwin-";

    private static final String MESSAGE_ANNOTATION_FIELD_KEY_RESOURCE = "resource";
    private static final String MESSAGE_ANNOTATION_FIELD_KEY_STATUS = "status";
    private static final String MESSAGE_ANNOTATION_FIELD_KEY_VERSION = "version";

    private static final String DEFAULT_STATUS_CODE = "200";

    private static final String MESSAGE_ANNOTATION_FIELD_VALUE_PROPERTIES_DESIRED = "/properties/desired";

    //This map is shared between the twin sender and receiver links. Twin sender links attach a correlation Id to each
    // request, and this receiver link needs to know those correlation Ids so that it can tell what type of twin
    // response each message received is.
    private final Map<String, DeviceOperations> twinOperationCorrelationMap;

    private static final String LINK_TYPE = "twin";

    private final DeviceClientConfig deviceClientConfig;

    AmqpsTwinReceiverLinkHandler(Receiver receiver, AmqpsLinkStateCallback amqpsLinkStateCallback, DeviceClientConfig deviceClientConfig, String linkCorrelationId, Map<String, DeviceOperations> twinOperationCorrelationMap)
    {
        super(receiver, amqpsLinkStateCallback, linkCorrelationId);

        this.deviceClientConfig = deviceClientConfig;

        this.receiverLinkAddress = getAddress(deviceClientConfig);

        //Note that this correlation id value must be equivalent to the correlation id in the twin sender link that it is paired with
        this.amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(CORRELATION_ID_KEY_PREFIX + this.linkCorrelationId));
        this.amqpProperties.put(Symbol.getSymbol(VERSION_IDENTIFIER_KEY), deviceClientConfig.getProductInfo().getUserAgentString());

        this.twinOperationCorrelationMap = twinOperationCorrelationMap;
    }

    static String getTag(DeviceClientConfig deviceClientConfig, String linkCorrelationId)
    {
        String moduleId = deviceClientConfig.getModuleId();
        String deviceId = deviceClientConfig.getDeviceId();
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
            return String.format(MODULE_RECEIVER_LINK_ENDPOINT_PATH, deviceId, moduleId);
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

        MessageCallback messageCallback = deviceClientConfig.getDeviceTwinMessageCallback();
        Object messageContext = deviceClientConfig.getDeviceTwinMessageContext();

        iotHubTransportMessage.setMessageCallback(messageCallback);
        iotHubTransportMessage.setMessageCallbackContext(messageContext);

        iotHubTransportMessage.setMessageType(MessageType.DEVICE_TWIN);
        iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_UNKNOWN);

        MessageAnnotations messageAnnotations = protonMsg.getMessageAnnotations();
        if (messageAnnotations != null)
        {
            for (Map.Entry<Symbol, Object> entry : messageAnnotations.getValue().entrySet())
            {
                Symbol key = entry.getKey();
                Object value = entry.getValue();

                if (key.toString().equals(MESSAGE_ANNOTATION_FIELD_KEY_STATUS))
                {
                    iotHubTransportMessage.setStatus(value.toString());
                }
                else if (key.toString().equals(MESSAGE_ANNOTATION_FIELD_KEY_VERSION))
                {
                    iotHubTransportMessage.setVersion(value.toString());
                }
                else if (key.toString().equals(MESSAGE_ANNOTATION_FIELD_KEY_RESOURCE) && value.toString().equals(MESSAGE_ANNOTATION_FIELD_VALUE_PROPERTIES_DESIRED))
                {
                    iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);
                }
            }
        }

        Properties properties = protonMsg.getProperties();
        if (properties != null && properties.getCorrelationId() != null)
        {
            iotHubTransportMessage.setCorrelationId(properties.getCorrelationId().toString());

            if (twinOperationCorrelationMap.containsKey(properties.getCorrelationId().toString()))
            {
                DeviceOperations deviceOperations = twinOperationCorrelationMap.get(properties.getCorrelationId().toString());
                switch (deviceOperations)
                {
                    case DEVICE_OPERATION_TWIN_GET_REQUEST:
                        iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_TWIN_GET_RESPONSE);
                        break;
                    case DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST:
                        iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE);
                        break;
                    case DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST:
                        iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);
                        break;
                    case DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST:
                        iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);
                        break;
                    default:
                        log.error("Unrecognized device operation type during conversion of proton message into an iothub message");
                }
                this.twinOperationCorrelationMap.remove(properties.getCorrelationId().toString());
            }
        }
        else if (iotHubTransportMessage.getDeviceOperationType() == DEVICE_OPERATION_UNKNOWN)
        {
            iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);

            if (iotHubTransportMessage.getStatus() == null || iotHubTransportMessage.getStatus().isEmpty())
            {
                iotHubTransportMessage.setStatus(DEFAULT_STATUS_CODE);
            }
        }

        return iotHubTransportMessage;
    }
}
