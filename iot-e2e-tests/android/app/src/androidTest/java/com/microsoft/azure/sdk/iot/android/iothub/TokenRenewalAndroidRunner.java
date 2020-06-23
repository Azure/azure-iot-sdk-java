/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub;

import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup17;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Rerun;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.TokenRenewalTests;

import org.junit.BeforeClass;
import org.junit.Rule;

import java.io.IOException;

@TestGroup17
public class TokenRenewalAndroidRunner extends TokenRenewalTests
{
    @Rule
    public Rerun count = new Rerun(3);
}
