package com.microsoft.azure.sdk.iot.deps.convention;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public final class GsonWritablePropertyResponse implements WritablePropertyResponse
{

    /**
     * The unserialized property value.
     */
    @NonNull
    @SerializedName("value")
    Object Value;

    /**
     * The acknowledgment code, usually an HTTP Status Code e.g. 200, 400.
     */
    @NonNull
    @SerializedName("ac")
    int AckCode;

    /**
     * The acknowledgment version, as supplied in the property update request.
     */
    @NonNull
    @SerializedName("av")
    long AckVersion;

    /**
     * The acknowledgment description, an optional, human-readable message about the result of the property update.
     */
    @SerializedName("ad")
    String AckDescription;
}