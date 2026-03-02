package com.microsoft.azure.sdk.iot.device.certificatesigning;

import lombok.Getter;

/**
 * IoT hub reported an error during a certificate signing request. Further details are nested in {@link #getError()}.
 */
public class IotHubCertificateSigningException extends Exception
{
    @Getter
    private IotHubCertificateSigningError error;

    public IotHubCertificateSigningException(String message, IotHubCertificateSigningError error)
    {
        super(message);
    }
}
