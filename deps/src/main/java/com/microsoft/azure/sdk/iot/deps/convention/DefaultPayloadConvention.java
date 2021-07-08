package com.microsoft.azure.sdk.iot.deps.convention;

import lombok.Getter;

/// <summary>
/// The default implementation of the <see cref="PayloadConvention"/> class.
/// </summary>
/// <remarks>
/// This class makes use of the <see cref="NewtonsoftJsonPayloadSerializer"/> serializer and the <see cref="Utf8PayloadEncoder"/>.
/// </remarks>
public final class DefaultPayloadConvention extends PayloadConvention
{
    /// <summary>
    /// A static instance of this class.
    /// </summary>
    @Getter
    public static DefaultPayloadConvention Instance = new DefaultPayloadConvention();

    @Getter
    public PayloadSerializer PayloadSerializer = GsonPayloadSerializer.Instance;

    @Getter
    public PayloadEncoder PayloadEncoder = Utf8PayloadEncoder.Instance;
}