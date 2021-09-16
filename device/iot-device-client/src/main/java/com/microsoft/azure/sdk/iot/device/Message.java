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

    /**
     * Used to correlate the message across the send/receive lifecycle.
     * <p>
     *     Optional.
     * </p>
     */
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
    @Getter
    @Setter
    @Accessors(prefix = "is")
    boolean isSecurityMessage;

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
    public Message(ByteArrayInputStream stream)
    {
        initialize();
    }

    /**
     * Constructor.
     * @param body The body of the new Message instance.
     */
    public Message(byte[] body)
    {
        // Codes_SRS_MESSAGE_11_025: [If the message body is null, the constructor shall throw an IllegalArgumentException.]
        if (body == null)
        {
            throw new IllegalArgumentException("Message body cannot be 'null'.");
        }

        initialize();

        // Codes_SRS_MESSAGE_11_024: [The constructor shall save the message body.]
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
        this.setContentTypeFinal(DEFAULT_IOTHUB_MESSAGE_CHARSET.name());
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
        // Codes_SRS_MESSAGE_11_002: [The function shall return the message body.]
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

        // Codes_SRS_MESSAGE_11_034: [If no value associated with the property name is found, the function shall return null.]
        if (messageProperty == null) {
            return null;
        }

        // Codes_SRS_MESSAGE_11_032: [The function shall return the value associated with the message property name, where the name can be either the HTTPS or AMQPS property name.]
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
        // Codes_SRS_MESSAGE_11_028: [If name is null, the function shall throw an IllegalArgumentException.]
        if (name == null)
        {
            throw new IllegalArgumentException("Property name cannot be 'null'.");
        }

        // Codes_SRS_MESSAGE_11_029: [If value is null, the function shall throw an IllegalArgumentException.]
        if (value == null)
        {
            throw new IllegalArgumentException("Property value cannot be 'null'.");
        }

        // Codes_SRS_MESSAGE_11_026: [The function shall set the message property to the given value.]
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
        // Codes_SRS_MESSAGE_11_033: [The function shall return a copy of the message properties.]
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
        this.isSecurityMessage = false;
    }

    /**
     * Verifies whether the message is expired or not
     * @return true if the message is expired, false otherwise
     */
    public boolean isExpired()
    {
        boolean messageExpired;

        // Codes_SRS_MESSAGE_15_035: [The function shall return false if the expiryTime is set to 0.]
        if (this.expiryTime == 0)
        {
            messageExpired = false;
        }
        else
        {
            // Codes_SRS_MESSAGE_15_036: [The function shall return true if the current time is greater than the expiry time and false otherwise.]
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
        // Codes_SRS_MESSAGE_34_047: [The function shall set the message's expiry time.]
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
        // Codes_SRS_MESSAGE_34_038: [If the provided absolute expiry time is negative, an IllegalArgumentException shall be thrown.]
        if (absoluteTimeout < 0)
        {
            throw new IllegalArgumentException("ExpiryTime may not be negative");
        }

        // Codes_SRS_MESSAGE_34_037: [The function shall set the message's expiry time to be the number of milliseconds since the epoch provided in absoluteTimeout.]
        this.expiryTime = absoluteTimeout;
    }


    /**
     * Set the content type of this message. Used in message routing.
     * @param contentType the content type of the message. May be null if you don't want to specify a content type.
     */
    public final void setContentTypeFinal(String contentType)
    {
        // Codes_SRS_MESSAGE_34_060: [The function shall save the provided content type.]
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

        // Codes_SRS_MESSAGE_34_064: [The function shall return the saved creationTimeUTC as a string in the format "yyyy-MM-dd_HH:mm:ss.SSSSSSSZ".]
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(UTC_TIMEZONE));
        return sdf.format(this.creationTimeUTC).replace("_", "T") + "Z";
    }

    public void setAsSecurityMessage()
    {
        // Set the message as json encoding
        this.contentEncoding = SECURITY_CLIENT_JSON_ENCODING;
        this.isSecurityMessage = true;
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