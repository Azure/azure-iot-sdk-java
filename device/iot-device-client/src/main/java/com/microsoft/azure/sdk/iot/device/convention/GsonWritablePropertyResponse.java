package com.microsoft.azure.sdk.iot.device.convention;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * The Gson annotated version of the WritablePropertyResponse class
 */
public final class GsonWritablePropertyResponse implements WritablePropertyResponse
{

    /**
     * The unserialized property value.
     */
    @NonNull
    @SerializedName("value")
    Object value;

    /**
     * The acknowledgment code, usually an HTTP Status Code e.g. 200, 400.
     */
    @NonNull
    @SerializedName("ac")
    Integer ackCode;

    /**
     * The acknowledgment version, as supplied in the property update request.
     */
    @NonNull
    @SerializedName("av")
    Long ackVersion;

    /**
     * The acknowledgment description, an optional, human-readable message about the result of the property update.
     */
    @SerializedName("ad")
    String ackDescription;
}