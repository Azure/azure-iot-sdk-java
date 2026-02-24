package com.microsoft.azure.sdk.iot.device.certificatesigning;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.Future;

public class IotHubCertificateSigningResponseFutures
{
    @Getter
    @Setter
    Future<IotHubCertificateSigningRequestAccepted> OnCertificateSigningRequestAccepted;

    @Getter
    @Setter
    Future<IotHubCertificateSigningResponse> OnCertificateSigningCompleted;
}
