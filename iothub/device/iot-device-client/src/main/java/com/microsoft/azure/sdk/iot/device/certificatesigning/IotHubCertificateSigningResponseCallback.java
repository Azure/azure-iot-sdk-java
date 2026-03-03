package com.microsoft.azure.sdk.iot.device.certificatesigning;

/**
 * The callback for each stage of making a certificate signing request to IoT Hub.
 */
public interface IotHubCertificateSigningResponseCallback
{
    /**
     * <p>
     * Executes if/when IoT hub sends a 202 in response to the certificate signing request.
     * </p>
     * <p>
     * When this executes, it signals that IoT hub has begun processing the certificate signing request and
     * will notify this client again once that request has been completed via {@link #onCertificateSigningComplete(IotHubCertificateSigningResponse)} ()}
     * </p>
     * <p>
     * If the certificate signing request is not accetepted or fails for any reason, {@link #onCertificateSigningError(IotHubCertificateSigningError)} ()}
     * will execute instead of this callback.
     * </p>
     * @param accepted The response message from IoT hub saying that the certificate signing request was accepted.
     */
    public void onCertificateSigningRequestAccepted(IotHubCertificateSigningRequestAccepted accepted);

    /**
     * <p>
     * Executes if/when IoT hub sends a 200 in response to the certificate signing request.
     * </p>
     * <p>
     * When this executes, it signals that IoT hub has completed signing the certificates.
     * </p>
     * <p>
     * If the certificate signing request cannot be completed or fails for any reason, {@link #onCertificateSigningError(IotHubCertificateSigningError)} ()}
     * will execute instead of this callback.
     * </p>
     * @param response The signed certificates
     */
    public void onCertificateSigningComplete(IotHubCertificateSigningResponse response);

    /**
     * <p>
     * Executes if/when IoT hub sends a response to the certificate signing request, but it is neither a 202 nor a 200.
     * </p>
     * <p>
     * This callback may execute even after a certificate signing request has been accepted.
     * </p>
     * @param error details on why this certificate signing request failed.
     */
    public void onCertificateSigningError(IotHubCertificateSigningError error);
}
