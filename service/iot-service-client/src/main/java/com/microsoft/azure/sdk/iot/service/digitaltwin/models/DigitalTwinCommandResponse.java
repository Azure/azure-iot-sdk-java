// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.
package com.microsoft.azure.sdk.iot.service.digitaltwin.models;

import lombok.Getter;
import lombok.Setter;

public final class DigitalTwinCommandResponse {
    @Getter
    @Setter
    Integer status;

    @Getter
    @Setter
    String payload;
}