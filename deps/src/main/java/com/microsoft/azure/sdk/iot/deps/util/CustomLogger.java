// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.util;

public class CustomLogger
{
    private static final int CALLING_METHOD_NAME_DEPTH = 2;

    public CustomLogger()
    {
    }

    public void LogInfo(String message, Object...params)
    {
    }

    public void LogDebug(String message, Object...params)
    {
    }

    public void LogDebug(String message, Throwable t, Object...params)
    {
    }

    public void LogTrace(String message, Object...params)
    {
    }

    public void LogWarn(String message, Object...params)
    {
    }

    public void LogFatal(String message, Object...params)
    {
    }

    public void LogError(String message, Object...params)
    {
    }

    public void LogError(Throwable exception)
    {
    }

    public String getMethodName()
    {
        return Thread.currentThread().getStackTrace()[CALLING_METHOD_NAME_DEPTH].getMethodName();
    }
}
