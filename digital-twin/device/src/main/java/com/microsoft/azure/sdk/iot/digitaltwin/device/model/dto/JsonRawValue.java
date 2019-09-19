package com.microsoft.azure.sdk.iot.digitaltwin.device.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@JsonSerialize(using = JsonRawValueSerializer.class)
@JsonDeserialize(using = JsonRawValueDeserializer.class)
@RequiredArgsConstructor
@Getter
public class JsonRawValue {
    @NonNull
    private final String rawJson;
}
