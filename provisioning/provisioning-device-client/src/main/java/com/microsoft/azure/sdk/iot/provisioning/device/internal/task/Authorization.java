/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.task;

import javax.net.ssl.SSLContext;

public class Authorization
{
    private String sasToken;
    private SSLContext sslContext;

    /**
     * Constructor to create {@code null} SSLContext and SasToken
     */
    Authorization()
    {
        //SRS_Authorization_25_001: [ Constructor shall create null SasToken and null SSL Context ]
        this.sasToken = null;
        this.sslContext = null;
    }

    /**
     * Getter for SSLContext
     * @return returns the saved value of SSLContext
     */
    SSLContext getSslContext()
    {
        //SRS_Authorization_25_003: [ This method shall return the saved value of SSLContext. ]
        return sslContext;
    }

    /**
     * Sets the value of SSLContext
     * @param sslContext Input SSLContext
     */
    void setSslContext(SSLContext sslContext)
    {
        //SRS_Authorization_25_002: [ This method shall save the value of SSLContext. ]
        this.sslContext = sslContext;
    }

    /**
     * Getter for SasToken
     * @return returns the saved value of SasToken
     */
    String getSasToken()
    {
        //SRS_Authorization_25_005: [ This method shall return the saved value of sasToken. ]
        return sasToken;
    }

    /**
     * Sets the value of SasToken
     * @param sasToken Input SasToken
     */
    void setSasToken(String sasToken)
    {
        //SRS_Authorization_25_004: [ This method shall save the value of sasToken. ]
        this.sasToken = sasToken;
    }
}
