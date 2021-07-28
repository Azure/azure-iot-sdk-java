package com.microsoft.azure.sdk.iot.deps.convention;

import lombok.Getter;

import java.lang.annotation.Inherited;

/**
 * The default implementation of the {@link PayloadConvention} class.
 *
 * <p>This class makes use of the {@link GsonPayloadSerializer} serializer and the {@link Utf8PayloadEncoder}.</p>
 */
public final class DefaultPayloadConvention extends PayloadConvention
{
    /**
     * A static instance of this class.
     */
    @Getter
    private static DefaultPayloadConvention Instance = new DefaultPayloadConvention();

    @Getter
    PayloadSerializer PayloadSerializer = GsonPayloadSerializer.Instance;

    @Getter
    PayloadEncoder PayloadEncoder = Utf8PayloadEncoder.getInstance();
}