package com.microsoft.azure.sdk.iot.device;

import java.util.List;

/**
 * Used to construct batch messages to be sent to the IoT Hub {@link com.microsoft.azure.sdk.iot.device.InternalClient#sendEventsAsync(List, MessagesSentCallback, Object)}
 */
public class BatchMessage extends Message
{
    /**
     * List of nested messages.
     */
    private final List<Message> nestedMessages;

    /**
     * Creates a batch message
     * This constructor is internal and can only be called within the com.microsoft.azure.sdk.iot.device package.
     * @param messages The messages that will be sent as a batch
     */
    BatchMessage(List<Message> messages)
    {
        this.nestedMessages = messages;
    }

    /**
     * Gets the list of nested messages.
     * @return All nested messages.
     */
    public List<Message> getNestedMessages()
    {
        return this.nestedMessages;
    }
}
