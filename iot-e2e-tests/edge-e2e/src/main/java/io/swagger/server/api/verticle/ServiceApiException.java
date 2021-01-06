package io.swagger.server.api.verticle;

import io.swagger.server.api.MainApiException;

public final class ServiceApiException extends MainApiException {
    public ServiceApiException(int statusCode, String statusMessage) {
        super(statusCode, statusMessage);
    }
    
    

}