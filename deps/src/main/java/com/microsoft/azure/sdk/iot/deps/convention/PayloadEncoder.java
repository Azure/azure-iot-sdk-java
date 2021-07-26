package com.microsoft.azure.sdk.iot.deps.convention;

import lombok.Getter;
import lombok.Setter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * This class specifies the byte encoding for the payload.
 * <p>
 *     The encoder is responsible for encoding all of your objects into the correct bytes for the <see cref="PayloadConvention"/> that uses it.
 * </p>
 * <p>
 *     By default we have implemented the {@link Utf8PayloadEncoder} class that uses the {@link StandardCharsets#UTF_8} {@link Charset} to handle the encoding for the {@link DefaultPayloadConvention} class.
 * </p>
 */
public abstract class PayloadEncoder
{
    /**
     * The {@link Charset} used for the payload.
     */
    @Getter
    Charset ContentEncoding;

    /**
     * Outputs an encoded byte array for the specified payload string.
     * @param contentPayload The contents of the message payload.
     * @return An encoded byte array.
     */
    public abstract byte[] encodeStringToByteArray(String contentPayload);
}
