package com.microsoft.azure.sdk.iot.device.convention;

import com.microsoft.azure.sdk.iot.device.InternalClient;

public interface WritablePropertiesRequestsCallback
{
    void execute(ClientPropertyCollection writablePropertiesRequestsCollection, Object context);
}
