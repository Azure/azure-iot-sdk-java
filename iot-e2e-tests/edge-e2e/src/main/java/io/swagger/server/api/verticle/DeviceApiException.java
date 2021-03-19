package io.swagger.server.api.verticle;

import io.swagger.server.api.MainApiException;

@SuppressWarnings("ALL")
public final class DeviceApiException extends MainApiException {
    public DeviceApiException(int statusCode, String statusMessage) {
        super(statusCode, statusMessage);
    }
    
    

}