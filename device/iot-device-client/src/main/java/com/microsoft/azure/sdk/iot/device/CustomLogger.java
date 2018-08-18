// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomLogger {

    private final Logger logger;
    private static final int CALLING_METHOD_NAME_DEPTH = 2;

    public CustomLogger(Class<?> clazz)
    {
        logger = LoggerFactory.getLogger(clazz);
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

    /**
     * @param message
     * @param params
     * @deprecated Since the switch from Log4j to slf4j there is no fatal log level anymore. Mapped to error level
     */
    public void LogFatal(String message, Object...params)
    {
        if(logger.isErrorEnabled())
        {
            logger.error(String.format(message, params));
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
            logger.error(exception.toString());
        }
    }

    public String getMethodName()
    {
        return Thread.currentThread().getStackTrace()[CALLING_METHOD_NAME_DEPTH].getMethodName();
    }
}
