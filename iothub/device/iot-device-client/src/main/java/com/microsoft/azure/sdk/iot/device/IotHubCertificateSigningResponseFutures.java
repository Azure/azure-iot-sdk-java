package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.certificatesigning.IotHubCertificateSigningException;
import com.microsoft.azure.sdk.iot.device.certificatesigning.IotHubCertificateSigningRequestAccepted;
import com.microsoft.azure.sdk.iot.device.certificatesigning.IotHubCertificateSigningResponse;
import lombok.AccessLevel;
import lombok.Setter;

import java.util.concurrent.Future;

/**
 * A collection of futures that complete at each stage of the certificate signing process
 */
public class IotHubCertificateSigningResponseFutures
{
    @Setter(AccessLevel.PACKAGE)
    private Future<IotHubCertificateSigningRequestAccepted> onCertificateSigningRequestAccepted;

    @Setter(AccessLevel.PACKAGE)
    private Future<IotHubCertificateSigningResponse> onCertificateSigningCompleted;

    /**
     * <p>
     * This future will complete once IoT hub has accepted the certificate signing request.
     * </p>
     * <p>
     * If IoT hub instead responds with an error, this future will complete exceptionally with a {@link IotHubCertificateSigningException}.
     * </p>
     */
    public Future<IotHubCertificateSigningRequestAccepted> OnCertificateSigningRequestAccepted()
    {
        return this.onCertificateSigningRequestAccepted;
    }

    /**
     * <p>
     * This future will complete once IoT hub has finished signing the certificates and has sent them back to this client.
     * </p>
     * <p>
     * If IoT hub instead responds with an error, this future will complete exceptionally with a {@link IotHubCertificateSigningException}.
     * </p>
     */
    public Future<IotHubCertificateSigningResponse> OnCertificateSigningRequestCompleted()
    {
        return this.onCertificateSigningCompleted;
    }
}
