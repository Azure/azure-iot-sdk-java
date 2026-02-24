package com.microsoft.azure.sdk.iot.device.certificatesigning;

import lombok.Getter;

public class IotHubCertificateSigningException extends Exception
{
    @Getter
    private IotHubCertificateSigningError error;

    public IotHubCertificateSigningException(String message, IotHubCertificateSigningError error)
    {
        super(message);
    }
}
