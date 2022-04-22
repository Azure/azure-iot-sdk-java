// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class Message
{
    // ----- Constants -----

    public static final Charset DEFAULT_IOTHUB_MESSAGE_CHARSET = StandardCharsets.UTF_8;

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd_HH:mm:ss.SSSSSSS";

    private static final String SECURITY_CLIENT_JSON_ENCODING = "application/json";

    private static final String UTC_TIMEZONE = "UTC";

    // ----- Data Fields -----

    /**
     * Used to correlate two-way communication.
     *
     * <p>
     *     Required for two way requests.
     * </p>
     * <p>
     *     Format: A case-sensitive string (up to 128 char long) of ASCII 7-bit alphanumeric chars
     *     plus {'-', ':', '/', '\', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     *     Non-alphanumeric characters are from URN RFC.
     * </p>
     */
    @Getter
    @Setter
    private String messageId;

    /**
     * Destination of the message.
     */
    @SuppressWarnings("unused") // Used in getter, leaving for future expansion
    @Getter
    @Setter
    private String to;

    /**
     * Expiry time in milliseconds.
     * <p>
     *      Optional.
     * </p>
     */
    private long expiryTime;

    /**
     * Used in message responses and feedback.
     */
    @Setter
    private String correlationId;

    /**
     * Used to specify the entity creating the message.
     * <p>
     *     Required in feedback messages.
     * </p>
     */
    @Getter
    @Setter
    private String userId;

    /**
     * The authenticated id used to send this message.
     * <p>
     *     Stamped on servicebound messages by IoT Hub.
     * </p>
     */
    @Getter
    @Setter
    private String connectionDeviceId;

    /**
     * Used to specify the type of message exchanged between Iot Hub and Device.
     * <p>
     *     Optional.
     * </p>
     */
    @Getter
    @Setter
    private MessageType messageType;

    /**
     * Used to specify the sender device client for multiplexing scenarios.
     * <p>
     *     Optional.
     * </p>
     */
    @Getter
    @Setter
    private IotHubConnectionString iotHubConnectionString;

    @Getter
    @Setter
    private CorrelatingMessageCallback correlatingMessageCallback;

    /**
     * User context to send when using the correlating message callback.
     * <p>
     *     Optional.
     * </p>
     */
    @Getter
    @Setter
    private Object correlatingMessageCallbackContext;

    /**
     * The connection module id.
     */
    @Getter
    @Setter
    private String connectionModuleId;

    /**
     * The input name of the message, used in routing for module communications.
     * @param inputName the input channel the message was received from.
     * @return the message's input name value.
     */
    @Getter
    @Setter
    private String inputName;

    /**
     * The output channel name to send to.
     * <p>
     *     Used in routing for module communications.
     * </p>
     * @param outputName the output channel name to send to.
     * @return the output channel name.
     */
    @Getter
    @Setter
    private String outputName;

    @SuppressWarnings("unused") // This is not set anywhere but is used in a method
    @Getter
    @Setter
    private String deliveryAcknowledgement;

    /**
     * User-defined properties.
     */
    private ArrayList<MessageProperty> properties;

    /**
     * The message body
     */
    private byte[] body;

    /**
     * The message's content type. This value is null by default
     * @param contentType the content type of the message. May be null if you don't want to specify a content type.
     * @return the message's content type
     */
    @Getter
    @Setter
    private String contentType;

    /**
     * The content encoding of this message. Used in message routing.
     * @param contentEncoding the content encoding of the message. May be null if you don't want to specify a content encoding.
     * @return the message's content encoding.
     */
    @Getter
    @Setter
    private String contentEncoding;

    /**
     * The message creation time in UTC.
     */
    @Getter
    @Setter
    private Date creationTimeUTC;

    /**
     * Security Client flag
     */
    private boolean isSecurityClient;

    /**
     * Sets the component name of the message.
     * <p>
     *     Optional.
     * </p>
     */
    @Getter
    @Setter
    String componentName;
    // ----- Constructors -----

    /**
     * Constructor.
     */
    public Message()
    {
        initialize();
    }

    /**
     * Constructor.
     * @param stream A stream to provide the body of the new Message instance.
     */
    private Message(ByteArrayInputStream stream)
    {
        initialize();
    }

    /**
     * Constructor.
     * @param body The body of the new Message instance.
     */
    public Message(byte[] body)
    {
        if (body == null)
        {
            throw new IllegalArgumentException("Message body cannot be 'null'.");
        }

        initialize();

        this.body = body;
    }

    /**
     * Constructor.
     * @param body The body of the new Message instance. It is internally serialized to a byte array using UTF-8 encoding.
     */
    public Message(String body)
    {
        if (body == null)
        {
            throw new IllegalArgumentException("Message body cannot be 'null'.");
        }

        initialize();

        this.body = body.getBytes(DEFAULT_IOTHUB_MESSAGE_CHARSET);
        this.setContentType(DEFAULT_IOTHUB_MESSAGE_CHARSET.name());
    }

    
    // ----- Public Methods -----

    /**
     * The stream content of the body.
     * @return always returns null.
     */
    @SuppressWarnings("SameReturnValue")
    public ByteArrayOutputStream getBodyStream()
    {
        return null;
    }

    /**
     * The byte content of the body.
     * @return A copy of this Message body, as a byte array.
     */
    public byte[] getBytes()
    {
        byte[] bodyClone = null;

        if (this.body != null) {
            bodyClone = Arrays.copyOf(this.body, this.body.length);
        }

        return bodyClone;
    }

    /**
     * Gets the values of user-defined properties of this Message.
     * @param name Name of the user-defined property to search for.
     * @return The value of the property if it is set, or null otherwise.
     */
    public String getProperty(String name)
    {
        MessageProperty messageProperty = null;

        for (MessageProperty currentMessageProperty: this.properties)
        {
            if (currentMessageProperty.hasSameName(name))
            {
                messageProperty = currentMessageProperty;
                break;
            }
        }

        if (messageProperty == null)
        {
            return null;
        }

        return messageProperty.getValue();
    }

    /**
     * Getter for the correlationId property
     * @return The property value
     */
    public String getCorrelationId()
    {
        // Codes_SRS_MESSAGE_34_045: [The function shall return the message's correlation ID.]
        if (correlationId == null)
        {
            return "";
        }

        return correlationId;
    }

    /**
     * Adds or sets user-defined properties of this Message.
     * @param name Name of the property to be set.
     * @param value Value of the property to be set.
     * @exception IllegalArgumentException If any of the arguments provided is null.
     */
    public void setProperty(String name, String value)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Property name cannot be 'null'.");
        }

        if (value == null)
        {
            throw new IllegalArgumentException("Property value cannot be 'null'.");
        }

        MessageProperty messageProperty = null;

        for (MessageProperty currentMessageProperty: this.properties)
        {
            if (currentMessageProperty.hasSameName(name))
            {
                messageProperty = currentMessageProperty;
                break;
            }
        }

        if (messageProperty != null)
        {
            this.properties.remove(messageProperty);
        }

        this.properties.add(new MessageProperty(name, value));
    }

    /**
     * Returns a copy of the message properties.
     *
     * @return a copy of the message properties.
     */
    public MessageProperty[] getProperties()
    {
        return properties.toArray(new MessageProperty[this.properties.size()]);
    }

    // ----- Private Methods -----

    /**
     * Internal initializer method for a new Message instance.
     */
    private void initialize()
    {
        this.messageId = UUID.randomUUID().toString();
        this.correlationId = UUID.randomUUID().toString();
        this.properties = new ArrayList<>();
        this.isSecurityClient = false;
    }

    /**
     * Verifies whether the message is expired or not
     * @return true if the message is expired, false otherwise
     */
    public boolean isExpired()
    {
        boolean messageExpired;

        if (this.expiryTime == 0)
        {
            messageExpired = false;
        }
        else
        {
            long currentTime = System.currentTimeMillis();
            if (currentTime > expiryTime)
            {
                log.warn("The message with correlation id {} expired", this.getCorrelationId());
                messageExpired = true;
            }
            else
            {
                messageExpired = false;
            }
        }

        return messageExpired;
    }

    /**
     * Setter for the expiryTime property. This setter uses relative time, not absolute time.
     * @param timeOut The time out for the message, in milliseconds, from the current time.
     */
    public void setExpiryTime(long timeOut)
    {
        long currentTime = System.currentTimeMillis();
        this.expiryTime = currentTime + timeOut;
        log.trace("The message with messageid {} has expiry time in {} milliseconds and the message will expire on {}", this.getMessageId(), timeOut, new Date(this.expiryTime));
    }

    /**
     * Setter for the expiryTime property using absolute time
     * @param absoluteTimeout The time out for the message, in milliseconds.
     */
    public void setAbsoluteExpiryTime(long absoluteTimeout)
    {
        if (absoluteTimeout < 0)
        {
            throw new IllegalArgumentException("ExpiryTime may not be negative");
        }

        this.expiryTime = absoluteTimeout;
    }

    /**
     * Set the content type of this message. Used in message routing.
     * @param contentType the content type of the message. May be null if you don't want to specify a content type.
     */
    public final void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    /**
     * Returns the iot hub accepted format for the creation time utc
     *
     * ex:
     * oct 1st, 2018 yields
     * 2008-10-01T17:04:32.0000000
     *
     * @return the iot hub accepted format for the creation time utc
     */
    public String getCreationTimeUTCString()
    {
        if (this.creationTimeUTC == null)
        {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(UTC_TIMEZONE));
        return sdf.format(this.creationTimeUTC).replace("_", "T") + "Z";
    }

    public void setAsSecurityMessage()
    {
        // Set the message as json encoding
        this.contentEncoding = SECURITY_CLIENT_JSON_ENCODING;
        this.isSecurityClient = true;
    }

    public boolean isSecurityMessage()
    {
        return this.isSecurityClient;
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append(" Message details: ");
        if (this.correlationId != null && !this.correlationId.isEmpty())
        {
            s.append("Correlation Id [").append(this.correlationId).append("] ");
        }

        if (this.messageId != null && !this.messageId.isEmpty())
        {
            s.append("Message Id [").append(this.messageId).append("] ");
        }

        return s.toString();
    }
}