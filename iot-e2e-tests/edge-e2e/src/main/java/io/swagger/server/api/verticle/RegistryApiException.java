package io.swagger.server.api.verticle;

import io.swagger.server.api.MainApiException;

public final class RegistryApiException extends MainApiException {
    public RegistryApiException(int statusCode, String statusMessage) {
        super(statusCode, statusMessage);
    }
    
    

}