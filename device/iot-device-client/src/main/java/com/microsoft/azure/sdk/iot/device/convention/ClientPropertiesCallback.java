package com.microsoft.azure.sdk.iot.device.convention;

public interface ClientPropertiesCallback
{
    void execute(ClientProperties responseStatus, Object callbackContext);
}
