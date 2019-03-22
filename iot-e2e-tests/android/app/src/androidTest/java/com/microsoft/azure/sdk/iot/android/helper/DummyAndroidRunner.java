/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.helper;

import com.microsoft.appcenter.espresso.Factory;
import com.microsoft.appcenter.espresso.ReportHelper;
import com.microsoft.azure.sdk.iot.common.helpers.BasicTierOnlyRule;
import com.microsoft.azure.sdk.iot.common.helpers.ConditionalIgnoreRule;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

// This class is to add a dummy test for every TestGroup.  Currently, App Center errors out
// when a TestGroup did not execute any actual tests.
public class DummyAndroidRunner
{
    @Rule
    public ReportHelper reportHelper = Factory.getReportHelper();

    @After
    public void labelSnapshot()
    {
        reportHelper.label("Stopping App");
    }

    @TestGroup1
    @TestGroup2
    @TestGroup3
    @TestGroup4
    @TestGroup5
    @TestGroup6
    @TestGroup7
    @TestGroup8
    @TestGroup9
    @TestGroup10
    @TestGroup11
    @TestGroup12
    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = BasicTierOnlyRule.class)
    public void dummyTest()
    {
    }
}
