package io.swagger.server.api.verticle;

import io.swagger.server.api.MainApiException;

@SuppressWarnings("ALL")
public final class WrapperApiException extends MainApiException {
    public WrapperApiException(int statusCode, String statusMessage) {
        super(statusCode, statusMessage);
    }
    
    

}