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

    public Utf8PayloadEncoder()
    {
        contentEncoding = StandardCharsets.UTF_8;
    }

    @Override
    public byte[] encodeStringToByteArray(String contentPayload)
    {
        return contentPayload.getBytes(getContentEncoding());
    }

    @Override
    public String decodeByteArrayToString(byte[] byteArray)
    {
        return new String(byteArray, getContentEncoding());
    }
}