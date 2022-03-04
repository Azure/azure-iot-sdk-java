package io.swagger.server.api;

public class MainApiException extends Exception {
    private final int statusCode;
    private final String statusMessage;

    public MainApiException(int statusCode, String statusMessage) {
        super();
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
    
    public static final MainApiException INTERNAL_SERVER_ERROR = new MainApiException(500, "Internal Server Error"); 
}