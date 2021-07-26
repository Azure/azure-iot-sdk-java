package com.microsoft.azure.sdk.iot.device.convention;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * Container for the
 */
@AllArgsConstructor
public class ClientProperties
{
    @Getter
    @Setter
    private ClientPropertyCollection writableProperties;

    @Getter
    @Setter
    private ClientPropertyCollection reportedFromClient;

}
