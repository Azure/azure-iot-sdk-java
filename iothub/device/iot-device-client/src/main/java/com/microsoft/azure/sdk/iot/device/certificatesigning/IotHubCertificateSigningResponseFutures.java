package com.microsoft.azure.sdk.iot.device.certificatesigning;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.Future;

/**
 * A collection of futures that complete at each stage of the certificate signing process
 */
public class IotHubCertificateSigningResponseFutures
{
    /**
     * <p>
     * This future will complete once IoT hub has accepted the certificate signing request.
     * </p>
     * <p>
     * If IoT hub instead responds with an error, this future will complete exceptionally with a {@link IotHubCertificateSigningException}.
     * </p>
     */
    @Getter
    @Setter
    Future<IotHubCertificateSigningRequestAccepted> OnCertificateSigningRequestAccepted;

    /**
     * <p>
     * This future will complete once IoT hub has finished signing the certificates and has sent them back to this client.
     * </p>
     * <p>
     * If IoT hub instead responds with an error, this future will complete exceptionally with a {@link IotHubCertificateSigningException}.
     * </p>
     */
    @Getter
    @Setter
    Future<IotHubCertificateSigningResponse> OnCertificateSigningCompleted;
}
