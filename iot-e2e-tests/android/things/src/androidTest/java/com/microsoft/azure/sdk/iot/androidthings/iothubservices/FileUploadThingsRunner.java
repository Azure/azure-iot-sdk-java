/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.androidthings.iothubservices;

import com.microsoft.azure.sdk.iot.androidthings.BuildConfig;
import com.microsoft.azure.sdk.iot.common.helpers.Rerun;
import com.microsoft.azure.sdk.iot.common.tests.iothubservices.FileUploadTests;

import org.bouncycastle.operator.OperatorCreationException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;

public class FileUploadThingsRunner extends FileUploadTests
{
    @Rule
    public Rerun count = new Rerun(3);

    @BeforeClass
    public static void setup() throws Exception
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        FileUploadTests.setUp();
    }

    @Ignore
    @Override
    @Test
    public void uploadToBlobAsyncSingleFileZeroLength()
    {
    }
}
