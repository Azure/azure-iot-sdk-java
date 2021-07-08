package com.microsoft.azure.sdk.iot.deps.convention;

import lombok.Getter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/// <summary>
/// A UTF-8 <see cref="PayloadEncoder"/> implementation.
/// </summary>
public class Utf8PayloadEncoder extends PayloadEncoder
{
    /// <summary>
/// The default instance of this class.
/// </summary>
    @Getter
    public static Utf8PayloadEncoder Instance = new Utf8PayloadEncoder();

    /// <inheritdoc/>
    @Getter
    public Charset ContentEncoding = StandardCharsets.UTF_8;

    @Override
    public byte[] EncodeStringToByteArray(String contentPayload)
    {
        return ContentEncoding.encode(contentPayload).array();
    }
}