/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

public interface SignatureProvider
{
    String sign(String keyName, String data) throws UnsupportedEncodingException, MalformedURLException, TransportException;
}
