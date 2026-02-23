package com.microsoft.azure.sdk.iot.device;

public interface CertificateSigningResponseCallback
{
    /**
     * <p>
     * Executes if/when IoT hub sends a 202 in response to the initial certificate signing request call.
     * </p>
     * <p>
     * When this executes, it signals that IoT hub has begun processing the certificate signing request and
     * will notify this client again once that request has been completed via {@link #onCertificateSigningComplete(CertificateSigningResponse)} ()}
     * </p>
     * <p>
     * If the certificate signing request is not accetepted or fails for any reason, {@link #onCertificateSigningError(CertificateSigningError)} ()}
     * will execute instead of this callback.
     * </p>
     */
    public void onCertificateSigningRequestAccepted(CertificateSigningRequestAccepted accepted);

    public void onCertificateSigningComplete(CertificateSigningResponse response);

    public void onCertificateSigningError(CertificateSigningError error);
}
