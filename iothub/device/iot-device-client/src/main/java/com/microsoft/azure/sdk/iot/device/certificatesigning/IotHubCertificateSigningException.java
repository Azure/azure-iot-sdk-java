package com.microsoft.azure.sdk.iot.device.certificatesigning;

import lombok.Getter;

/**
 * IoT hub reported an error during a certificate signing request..
 */
public class IotHubCertificateSigningException extends Exception
{
    /**
     * The error reported by IoT hub that caused the certificate signing to fail.
     */
    @Getter
    private IotHubCertificateSigningError error;

    public IotHubCertificateSigningException(String message, IotHubCertificateSigningError error)
    {
        super(message);
    }
}
