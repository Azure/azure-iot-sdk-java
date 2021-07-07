package com.microsoft.azure.sdk.iot.deps.convention;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

public final class GsonWritablePropertyResponse implements IWritablePropertyResponse
{
    /// <summary>
/// Convenience constructor for specifying the properties.
/// </summary>
/// <param name="propertyValue">The unserialized property value.</param>
/// <param name="ackCode">The acknowledgment code, usually an HTTP Status Code e.g. 200, 400.</param>
/// <param name="ackVersion">The acknowledgment version, as supplied in the property update request.</param>
/// <param name="ackDescription">The acknowledgment description, an optional, human-readable message about the result of the property update.</param>
    public GsonWritablePropertyResponse(Object propertyValue, int ackCode, long ackVersion, String ackDescription)
    {
        Value = propertyValue;
        AckCode = ackCode;
        AckVersion = ackVersion;
        AckDescription = ackDescription;
    }

    public GsonWritablePropertyResponse(Object propertyValue, int ackCode, long ackVersion)
    {
        this(propertyValue, ackCode, ackVersion, null);
    }

    /// <summary>
    /// The unserialized property value.
    /// </summary>
    @Getter
    @Setter
    @SerializedName("value")
    public Object Value;
    /// <summary>
    /// The acknowledgment code, usually an HTTP Status Code e.g. 200, 400.
    /// </summary>
    @Getter
    @Setter
    @SerializedName("ac")
    public int AckCode;

    /// <summary>
    /// The acknowledgment version, as supplied in the property update request.
    /// </summary>
    @Getter
    @Setter
    @SerializedName("av")
    public long AckVersion;

    /// <summary>
    /// The acknowledgment description, an optional, human-readable message about the result of the property update.
    /// </summary>
    @Getter
    @Setter
    @SerializedName("ad")
    public String AckDescription;
}