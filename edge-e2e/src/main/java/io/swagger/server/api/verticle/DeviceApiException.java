package io.swagger.server.api.verticle;

import io.swagger.server.api.model.Certificate;
import io.swagger.server.api.model.ConnectResponse;
import io.swagger.server.api.MainApiException;
import io.swagger.server.api.model.RoundtripMethodCallBody;

public final class DeviceApiException extends MainApiException {
    public DeviceApiException(int statusCode, String statusMessage) {
        super(statusCode, statusMessage);
    }
    
    

}