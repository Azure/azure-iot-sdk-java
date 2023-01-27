package io.swagger.server.api.verticle;

import io.swagger.server.api.MainApiException;

@SuppressWarnings("ALL")
public final class EventhubApiException extends MainApiException {
    public EventhubApiException(int statusCode, String statusMessage) {
        super(statusCode, statusMessage);
    }
    
    

}