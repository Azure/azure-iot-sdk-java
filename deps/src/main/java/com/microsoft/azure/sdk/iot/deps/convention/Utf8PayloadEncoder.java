package com.microsoft.azure.sdk.iot.deps.convention;

import lombok.Getter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A {@link StandardCharsets#UTF_8} implementation.
 */
public class Utf8PayloadEncoder extends PayloadEncoder
{
    /**
     * The default instance of this class.
     * @return A static instance of the {@link Utf8PayloadEncoder}
     */
    @Getter
    private static Utf8PayloadEncoder instance = new Utf8PayloadEncoder();


    /**
     * {@inheritDoc}
     */
    @Getter
    private Charset ContentEncoding = StandardCharsets.UTF_8;

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] encodeStringToByteArray(String contentPayload)
    {
        return ContentEncoding.encode(contentPayload).array();
    }
}