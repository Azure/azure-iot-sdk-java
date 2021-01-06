package io.swagger.server.api.verticle;

import io.swagger.server.api.MainApiException;

public final class ModuleApiException extends MainApiException {
    public ModuleApiException(int statusCode, String statusMessage) {
        super(statusCode, statusMessage);
    }
    
    

}