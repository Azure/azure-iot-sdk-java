// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.util;

public class CustomLogger
{
    private static final int CALLING_METHOD_NAME_DEPTH = 2;

    public CustomLogger()
    {
    }

    public String getMethodName()
    {
        return Thread.currentThread().getStackTrace()[CALLING_METHOD_NAME_DEPTH].getMethodName();
    }
}
