/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.task;

import javax.net.ssl.SSLContext;

public class RequestData
{
    private byte[] endorsementKey;
    private byte[] storageRootKey;
    private final String registrationId;
    private String operationId;

    private SSLContext sslContext;
    private String sasToken;
    private boolean isX509;
    private String jsonPayload;

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
        this.jsonPayload = payload;
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
        this.jsonPayload = payload;
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
        this.jsonPayload = payload;
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
        this.jsonPayload = payload;
    }

    /**
     * Getter for endorsement key
     * @return endorsement key
     */
    public byte[] getEndorsementKey()
    {
        //SRS_RequestData_25_002: [ This method shall retrieve endorsementKey. ]
        return endorsementKey;
    }

    /**
     * Getter for Storage root key
     * @return Storage root key.
     */
    public byte[] getStorageRootKey()
    {
        //SRS_RequestData_25_004: [ This method shall retrieve storageRootKey. ]
        return storageRootKey;
    }

    /**
     * Getter for Registration ID
     * @return registration id.
     */
    public String getRegistrationId()
    {
        //SRS_RequestData_25_006: [ This method shall retrieve registrationId. ]
        return registrationId;
    }

    /**
     * Getter for operation Id
     * @return Operation Id value.
     */
    public String getOperationId()
    {
        //SRS_RequestData_25_008: [ This method shall retrieve operationId. ]
        return operationId;
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

    /**
     * Getter for SSL Context
     * @return SSL context
     */
    public SSLContext getSslContext()
    {
        //SRS_RequestData_25_010: [ This method shall retrieve sslContext. ]
        return sslContext;
    }

    /**
     * Setter for SSL context.
     * @param sslContext sslContext value. Can be {@code null};
     */
    void setSslContext(SSLContext sslContext)
    {
        //SRS_RequestData_25_011: [ This method shall set sslContext. ]
        this.sslContext = sslContext;
    }

    /**
     * Getter for SasToken.
     * @return SasToken value.
     */
    public String getSasToken()
    {
        //SRS_RequestData_25_012: [ This method shall retrieve sasToken. ]
        return sasToken;
    }

    /**
     * Setter for SasToken.
     * @param sasToken Sastoken value. Can be {@code null};
     */
    void setSasToken(String sasToken)
    {
        //SRS_RequestData_25_013: [ This method shall set sasToken. ]
        this.sasToken = sasToken;
    }

    /**
     * Setter for Payload.
     * @param payload value. Can be {@code null};
     */
    public void setPayload(String payload)
    {
        this.jsonPayload = payload;
    }

    /**
     * Getter for Payload.
     * @return Payload value.
     */
    public String getPayload()
    {
        return this.jsonPayload;
    }
}
