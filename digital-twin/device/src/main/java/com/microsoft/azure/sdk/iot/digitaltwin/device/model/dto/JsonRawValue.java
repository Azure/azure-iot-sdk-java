// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

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
