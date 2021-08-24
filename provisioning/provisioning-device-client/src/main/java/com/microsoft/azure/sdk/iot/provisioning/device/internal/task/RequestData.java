/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.task;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.SSLContext;

@SuppressWarnings("SameParameterValue") // Some constructors currently are passed null params, but they are designed to be generic.
public class RequestData
{
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private byte[] endorsementKey;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private byte[] storageRootKey;

    @Getter
    private final String registrationId;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String operationId;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private SSLContext sslContext;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String sasToken;

    private final boolean isX509;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String payload;

    /**
     * Constructor for Request data
     * @param endorsementKey Endorsement key value. Can be {@code null}
     * @param storageRootKey Storage root key value. Can be {@code null}
     * @param registrationId Registration ID value. Can be {@code null}
     * @param sslContext SSL context value. Can be {@code null}
     * @param sasToken SasToken value. Can be {@code null}
     * @param payload Payload value. Can be {@code null}
     */
    RequestData(byte[] endorsementKey, byte[] storageRootKey, String registrationId, SSLContext sslContext, String sasToken, String payload)
    {
        //SRS_RequestData_25_001: [ Constructor shall save all the parameters and ignore the null parameters. ]
        this.endorsementKey = endorsementKey;
        this.storageRootKey = storageRootKey;
        this.registrationId = registrationId;
        this.sslContext = sslContext;
        this.sasToken = sasToken;
        this.payload = payload;
        this.isX509 = false;
    }

    /**
     * Constructor for Request data
     * @param registrationId Registration ID value. Can be {@code null}
     * @param sslContext SSL context value. Can be {@code null}
     * @param sasToken SasToken value. Can be {@code null}
     * @param payload Payload value. Can be {@code null}
     */
    RequestData(String registrationId, SSLContext sslContext, String sasToken, String payload)
    {
        //SRS_RequestData_25_001: [ Constructor shall save all the parameters and ignore the null parameters. ]
        this.registrationId = registrationId;
        this.sslContext = sslContext;
        this.sasToken = sasToken;
        this.payload = payload;
        this.isX509 = false;
    }

    /**
     * Constructor for Request data
     * @param registrationId Registration ID value. Can be {@code null}
     * @param sslContext SSL context value. Can be {@code null}
     * @param isX509 True if X509 flow, false otherwise
     * @param payload Payload value. Can be {@code null}
     */
    RequestData(String registrationId, SSLContext sslContext, boolean isX509, String payload)
    {
        //SRS_RequestData_25_001: [ Constructor shall save all the parameters and ignore the null parameters. ]
        this.registrationId = registrationId;
        this.sslContext = sslContext;
        this.isX509 = isX509;
        this.payload = payload;
    }

    /**
     * Constructor for Request data
     * @param registrationId Registration ID value. Can be {@code null};
     * @param operationId Operation ID value. Can be {@code null};
     * @param sslContext SSL context value. Can be {@code null};
     * @param sasToken SasToken value. Can be {@code null};
     * @param payload Payload value. Can be {@code null}
     */
    RequestData(String registrationId, String operationId, SSLContext sslContext, String sasToken, String payload)
    {
        //SRS_RequestData_25_001: [ Constructor shall save all the parameters and ignore the null parameters. ]
        this.registrationId = registrationId;
        this.operationId = operationId;
        this.sslContext = sslContext;
        this.sasToken = sasToken;
        this.payload = payload;
        this.isX509 = false;
    }

    /**
     * If the flow with the service is X509 or not.
     * @return true if the flow is X509.
     */
    public boolean isX509()
    {
        //SRS_RequestData_25_015: [ This method shall return true is it is X509, false otherwise. ]
        return isX509;
    }
}
