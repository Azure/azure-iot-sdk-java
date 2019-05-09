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
    @TestGroup13
    @TestGroup14
    @TestGroup15
    @TestGroup16
    @TestGroup17
    @TestGroup18
    @TestGroup19
    @TestGroup20
    @TestGroup21
    @TestGroup22
    @TestGroup23
    @TestGroup24
    @TestGroup25
    @TestGroup26
    @TestGroup27
    @TestGroup28
    @TestGroup29
    @TestGroup30
    @TestGroup31
    @TestGroup32
    @TestGroup33
    @TestGroup34
    @TestGroup35
    @TestGroup36
    @TestGroup37
    @TestGroup38
    @Test
    @ConditionalIgnoreRule.ConditionalIgnore(condition = BasicTierOnlyRule.class)
    public void dummyTest()
    {
    }
}
