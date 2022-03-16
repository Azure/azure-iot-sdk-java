// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.


package com.microsoft.azure.sdk.iot.device;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** An IoT Hub message property. */
@Slf4j
public final class MessageProperty {
    /**
     * A set of reserved property names. The reserved property names are
     * interpreted in a meaningful way by the device and the IoT Hub.
     */
    public static final Set<String> RESERVED_PROPERTY_NAMES;

    public static final String OUTPUT_NAME_PROPERTY = "iothub-outputname";
    public static final String CONNECTION_DEVICE_ID = "iothub-connection-device-id";
    public static final String CONNECTION_MODULE_ID = "iothub-connection-module-id";

    //iot hub level content type and encoding. Not to be confused with transport level content type and encoding
    public static final String IOTHUB_CONTENT_ENCODING = "iothub-contentencoding";
    public static final String IOTHUB_CONTENT_TYPE = "iothub-contenttype";

    public static final String IOTHUB_CREATION_TIME_UTC = "iothub-creation-time-utc";
    public static final String IOTHUB_SECURITY_INTERFACE_ID = "iothub-interface-id";
    public static final String IOTHUB_SECURITY_INTERFACE_ID_VALUE = "urn:azureiot:Security:SecurityAgent:1";

    public static final String COMPONENT_ID = "dt-subject";

    static {
        HashSet<String> reservedPropertyNames = new HashSet<>();
        reservedPropertyNames.add("iothub-enqueuedtime");
        reservedPropertyNames.add("iothub-messagelocktoken");
        reservedPropertyNames.add("iothub-sequencenumber");
        reservedPropertyNames.add("iothub-operation");
        reservedPropertyNames.add("iothub-partition-key");
        reservedPropertyNames.add("iothub-ack");
        reservedPropertyNames.add("iothub-connection-auth-method");
        reservedPropertyNames.add("iothub-connection-auth-generation-id");
        reservedPropertyNames.add("iothub-messageid");
        reservedPropertyNames.add("iothub-correlationid");
        reservedPropertyNames.add("iothub-userid");
        reservedPropertyNames.add("iothub-to");
        reservedPropertyNames.add("iothub-content-type");
        reservedPropertyNames.add(IOTHUB_CONTENT_TYPE);
        reservedPropertyNames.add("iothub-content-encoding");
        reservedPropertyNames.add(IOTHUB_CONTENT_ENCODING);
        reservedPropertyNames.add("iothub-absolute-expiry-time");
        reservedPropertyNames.add("IoThub-methodname");
        reservedPropertyNames.add("connectionDeviceId");
        reservedPropertyNames.add("connectionModuleId");
        reservedPropertyNames.add(OUTPUT_NAME_PROPERTY);
        reservedPropertyNames.add("iothub-inputname");
        reservedPropertyNames.add(CONNECTION_DEVICE_ID);
        reservedPropertyNames.add(CONNECTION_MODULE_ID);

        RESERVED_PROPERTY_NAMES = Collections.unmodifiableSet(reservedPropertyNames);
    }

    /** The property name. */
    private final String name;
    /** The property value. */
    private final String value;

    /**
     * Constructor.
     *
     * @param name The IoT Hub message property name.
     * @param value The IoT Hub message property value.
     *
     * @throws IllegalArgumentException if the name and value constitute an
     * invalid IoT Hub message property. A message property can only contain US-ASCII. 
     * A message property name cannot be one of the reserved property names.
     */
    public MessageProperty(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Property argument 'name' cannot be null.");
        }

        if (value == null) {
            throw new IllegalArgumentException("Property argument 'value' cannot be null.");
        }

        if (RESERVED_PROPERTY_NAMES.contains(name)) {
            String errMsg = String.format("%s is a reserved IoT Hub message property name.%n", name);
            throw new IllegalArgumentException(errMsg);
        }

        this.name = name;
        this.value = value;
    }

    /**
     * Returns the property name.
     *
     * @return the property name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the property value.
     *
     * @return the property value.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Equivalent to property.getName().equalsIgnoreCase(name).
     *
     * @param name the property name.
     *
     * @return true if the given name is the property name.
     */
    public boolean hasSameName(String name) {
        boolean nameMatches = false;

        if (this.getName().equalsIgnoreCase(name)) {
            nameMatches = true;
        }

        return nameMatches;
    }

    /**
     * Returns whether the property is a valid application property. The
     * property is valid if it is not one of the reserved properties, only uses US-ASCII 
     *
     * @param name the property name.
     * @param value the property value.
     *
     * @return whether the property is a valid application property.
     */
    public static boolean isValidAppProperty(String name, String value)
    {
        return !RESERVED_PROPERTY_NAMES.contains(name);
    }

    /**
     * Returns whether the property is a valid system property. The
     * property is valid if it is one of the reserved properties and only uses US-ASCII
     *
     * @param name the property name.
     * @param value the property value.
     *
     * @return whether the property is a valid system property.
     */
    public static boolean isValidSystemProperty(String name, String value)
    {
        return RESERVED_PROPERTY_NAMES.contains(name);
    }

    @SuppressWarnings("unused")
    protected MessageProperty() {
        this.name = null;
        this.value = null;
    }
}
