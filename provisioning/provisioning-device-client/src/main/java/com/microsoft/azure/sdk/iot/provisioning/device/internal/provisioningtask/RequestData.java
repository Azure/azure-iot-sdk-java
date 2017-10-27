/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.provisioningtask;

import javax.net.ssl.SSLContext;

public class RequestData
{
    private byte[] endorsementKey;
    private byte[] storageRootKey;
    private  String registrationId;
    private  String operationId;

    private  SSLContext sslContext;
    private  String sasToken;

    /**
     * Constructor for Request data
     * @param endorsementKey Endorsement key value. Can be {@code null};
     * @param storageRootKey Storage root key value. Can be {@code null};
     * @param registrationId Registration ID value. Can be {@code null};
     * @param operationId Operation ID value. Can be {@code null};
     * @param sslContext SSL context value. Can be {@code null};
     * @param sasToken SasToken value. Can be {@code null};
     */
    RequestData(byte[] endorsementKey, byte[] storageRootKey, String registrationId, String operationId, SSLContext sslContext, String sasToken)
    {
        //SRS_RequestData_25_001: [ Constructor shall save all the parameters and ignore the null parameters. ]
        this.endorsementKey = endorsementKey;
        this.storageRootKey = storageRootKey;
        this.registrationId = registrationId;
        this.operationId = operationId;
        this.sslContext = sslContext;
        this.sasToken = sasToken;
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
     * Setter for endorsement key
     * @param endorsementKey endorsement key value. Can be {@code null};
     */
    public void setEndorsementKey(byte[] endorsementKey)
    {
        //SRS_RequestData_25_003: [ This method shall set endorsementKey. ]
        this.endorsementKey = endorsementKey;
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
     * Setter for storage root key
     * @param storageRootKey storage root key value. Can be {@code null};
     */
    void setStorageRootKey(byte[] storageRootKey)
    {
        //SRS_RequestData_25_005: [ This method shall set storageRootKey. ]
        this.storageRootKey = storageRootKey;
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
     * Setter for registration id
     * @param registrationId registration id value. Can be {@code null};
     */
    void setRegistrationId(String registrationId)
    {
        //SRS_RequestData_25_007: [ This method shall set registrationId. ]
        this.registrationId = registrationId;
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
     * Setter for operation Id.
     * @param operationId Operation Id value. Can be {@code null};
     */
    void setOperationId(String operationId)
    {
        //SRS_RequestData_25_009: [ This method shall set operationId. ]
        this.operationId = operationId;
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
}
