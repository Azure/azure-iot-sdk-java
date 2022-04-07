package com.microsoft.azure.sdk.iot.device.convention;

public final class ConventionConstants
{
    /**
     *  Separator for a component-level command name.
     */
    public static final char COMPONENT_LEVEL_COMMAND_SEPARATOR = '*';

    /**
     *  Marker key to indicate a component-level property.
     */
    public static final String COMPONENT_IDENTIFIER_KEY = "__t";

    /**
     * Marker value to indicate a component-level property.
     */
    public static final String COMPONENT_IDENTIFIER_VALUE = "c";

    /**
     * Represents the JSON document property name for the value of a writable property response.
     */
    public static final String VALUE_PROPERTY_NAME = "value";

    /**
     * Represents the JSON document property name for the Ack Code of a writable property response.
     */
    public static final String ACK_CODE_PROPERTY_NAME = "ac";

    /**
     * Represents the JSON document property name for the Ack Version of a writable property response.
     */
    public static final String ACK_VERSION_PROPERTY_NAME = "av";

    /**
     * Represents the JSON document property name for the Ack Description of a writable property response.
     */
    public static final String ACK_DESCRIPTION_PROPERTY_NAME = "ad";
}