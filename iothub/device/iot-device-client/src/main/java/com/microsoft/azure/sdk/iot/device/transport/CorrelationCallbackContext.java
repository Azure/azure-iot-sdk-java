package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.CorrelatingMessageCallback;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
class CorrelationCallbackContext
{
    @Getter
    private final CorrelatingMessageCallback callback;

    @Getter
    private final Object userContext;

    // Used to store the number of milliseconds since epoch that this packet was created for a correlationId
    @Getter
    private final Long startTimeMillis;
}
