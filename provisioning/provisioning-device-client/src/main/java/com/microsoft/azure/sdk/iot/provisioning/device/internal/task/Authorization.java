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

public class Authorization
{
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private String sasToken;

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private SSLContext sslContext;

    /**
     * Constructor to create {@code null} SSLContext and SasToken
     */
    Authorization()
    {
    }
}
