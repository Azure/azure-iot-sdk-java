// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CustomLogger {

    private final Logger logger;
    private static final int CALLING_METHOD_NAME_DEPTH = 2;

    public CustomLogger(Class<?> clazz)
    {
        logger = LogManager.getLogger(clazz);
    }

    public void LogInfo(String message, Object...params)
    {
        if(logger.isInfoEnabled())
        {
            logger.info(String.format(message, params));
        }
    }

    public void LogDebug(String message, Object...params)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug(String.format(message, params));
        }
    }

    public void LogDebug(String message, Throwable t, Object...params)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug(String.format(message, params), t);
        }
    }

    public void LogTrace(String message, Object...params)
    {
        if(logger.isTraceEnabled())
        {
            logger.trace(String.format(message, params));
        }
    }

    public void LogWarn(String message, Object...params)
    {
        if(logger.isWarnEnabled())
        {
            logger.warn(String.format(message, params));
        }
    }

    public void LogFatal(String message, Object...params)
    {
        if(logger.isFatalEnabled())
        {
            logger.fatal(String.format(message, params));
        }
    }

    public void LogError(String message, Object...params)
    {
        if(logger.isErrorEnabled())
        {
            logger.error(String.format(message, params));
        }
    }

    public void LogError(Throwable exception)
    {
        if(logger.isErrorEnabled())
        {
            logger.error(exception);
        }
    }

    public String getMethodName()
    {
        return Thread.currentThread().getStackTrace()[CALLING_METHOD_NAME_DEPTH].getMethodName();
    }
}
