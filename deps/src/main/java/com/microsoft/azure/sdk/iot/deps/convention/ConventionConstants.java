package com.microsoft.azure.sdk.iot.deps.convention;

public final class ConventionConstants
{
    /// <summary>
    /// Separator for a component-level command name.
    /// </summary>
    public static final char ComponentLevelCommandSeparator = '*';

    /// <summary>
    /// Marker key to indicate a component-level property.
    /// </summary>
    public static final String ComponentIdentifierKey = "__t";

    /// <summary>
    /// Marker value to indicate a component-level property.
    /// </summary>
    public static final String ComponentIdentifierValue = "c";

    /// <summary>
    /// Represents the JSON document property name for the value of a writable property response.
    /// </summary>
    public static final String ValuePropertyName = "value";

    /// <summary>
    /// Represents the JSON document property name for the Ack Code of a writable property response.
    /// </summary>
    public static final String AckCodePropertyName = "ac";

    /// <summary>
    /// Represents the JSON document property name for the Ack Version of a writable property response.
    /// </summary>
    public static final String AckVersionPropertyName = "av";

    /// <summary>
    /// Represents the JSON document property name for the Ack Description of a writable property response.
    /// </summary>
    public static final String AckDescriptionPropertyName = "ad";
}