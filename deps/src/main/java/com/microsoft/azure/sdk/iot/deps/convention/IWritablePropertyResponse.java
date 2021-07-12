package com.microsoft.azure.sdk.iot.deps.convention;

import lombok.Getter;
import lombok.Setter;

public interface IWritablePropertyResponse
{
    /// <summary>
    /// The unserialized property value.
    /// </summary>
    @Getter
    @Setter
    Object Value = null;

    /// <summary>
    /// The acknowledgment code, usually an HTTP Status Code e.g. 200, 400.
    /// </summary>
    @Getter
    @Setter
    public int AckCode = 0;

    /// <summary>
    /// The acknowledgment version, as supplied in the property update request.
    /// </summary>
    @Getter
    @Setter
    public long AckVersion = 0;

    /// <summary>
    /// The acknowledgment description, an optional, human-readable message about the result of the property update.
    /// </summary>
    @Getter
    @Setter
    public String AckDescription = null;
}
