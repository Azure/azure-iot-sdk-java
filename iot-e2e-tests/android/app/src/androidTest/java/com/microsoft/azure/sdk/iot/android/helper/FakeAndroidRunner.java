/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.helper;

import org.junit.Test;

// This class is to add a fake test for every TestGroup.  Currently, android testing errors out
// when a TestGroup did not execute any actual tests, so this forces each test group to have at least
// this test
public class FakeAndroidRunner
{
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
    @Test
    public void fakeTest()
    {
    }
}
