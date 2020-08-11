package com.microsoft.azure.sdk.iot.device;

import java.util.List;

public class BatchMessages extends Message
{
    /**
     * List of nested messages.
     */
    private List<Message> nestedMessages;

    /**
     * Creates a batch message
     * This constructor is internal and can only be called within the com.microsoft.azure.sdk.iot.device package.
     * @param messages
     */
    BatchMessages(List<Message> messages)
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
