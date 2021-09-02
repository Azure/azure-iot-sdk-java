package com.microsoft.azure.sdk.iot.device.convention;

/**
 * The interface for retrieving client properties from a convention based device.
 */
public interface ClientPropertiesCallback
{
    /**
     * The method to execute for the callback.
     * @param responseStatus The response status of the client.
     * @param callbackContext User supplied context for this callback. Can be {@code null} if unused.
     */
    void execute(ClientProperties responseStatus, Object callbackContext);
}
