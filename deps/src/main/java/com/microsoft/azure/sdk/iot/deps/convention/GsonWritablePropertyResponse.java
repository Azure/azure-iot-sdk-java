package com.microsoft.azure.sdk.iot.deps.convention;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public final class GsonWritablePropertyResponse implements IWritablePropertyResponse
{

    /// <summary>
    /// The unserialized property value.
    /// </summary>
    @NonNull
    @SerializedName("value")
    Object Value;

    /// <summary>
    /// The acknowledgment code, usually an HTTP Status Code e.g. 200, 400.
    /// </summary>
    @NonNull
    @SerializedName("ac")
    int AckCode;

    /// <summary>
    /// The acknowledgment version, as supplied in the property update request.
    /// </summary>
    @NonNull
    @SerializedName("av")
    long AckVersion;

    /// <summary>
    /// The acknowledgment description, an optional, human-readable message about the result of the property update.
    /// </summary>
    @SerializedName("ad")
    String AckDescription;
}