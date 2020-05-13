package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;

public interface SubscriptionMessageRequestSentCallback
{
    void onSubscriptionMessageSent(int deliveryTag, SubscriptionType deviceOperationType);

    enum SubscriptionType
    {
        DESIRED_PROPERTIES_SUBSCRIPTION
    }
}
