package com.microsoft.azure.sdk.iot.deps.convention;

import lombok.Getter;
import lombok.Setter;

import java.nio.charset.Charset;

/// This class specifies the byte encoding for the payload.
/// </summary>
/// <remarks>
/// The encoder is responsible for encoding all of your objects into the correct bytes for the <see cref="PayloadConvention"/> that uses it.
/// <para>
/// By default we have implemented the <see cref="Utf8PayloadEncoder"/> class that uses <see cref="System.Text.Encoding.UTF8"/>
/// to handle the encoding for the <see cref="DefaultPayloadConvention"/> class.
/// </para>
/// </remarks>
public abstract class PayloadEncoder
{
    /// <summary>
    /// The <see cref="Encoding"/> used for the payload.
    /// </summary>
    @Getter
    public Charset ContentEncoding;

    /// <summary>
    /// Outputs an encoded byte array for the specified payload string.
    /// </summary>
    /// <param name="contentPayload">The contents of the message payload.</param>
    /// <returns>An encoded byte array.</returns>
    public abstract byte[] EncodeStringToByteArray(String contentPayload);
}
