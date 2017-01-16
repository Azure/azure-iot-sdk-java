package com.microsoft.azure.sdk.iot.device;

import org.apache.log4j.*;

public class CustomLogger {

    private Logger logger;

    public CustomLogger(Class<?> clazz)
    {
        logger = Logger.getLogger(clazz);
    }

    public void LogInfo(String message, Object...params)
    {
        if(logger.isEnabledFor(Level.INFO))
        {
            logger.info(String.format(message, params));
        }
    }

	public void LogDebug(String message, Object...params)
    {
        if(logger.isEnabledFor(Level.DEBUG))
        {
            logger.debug(String.format(message, params));
        }
    }

	public void LogTrace(String message, Object...params)
    {
        if(logger.isEnabledFor(Level.TRACE))
        {
            logger.trace(String.format(message, params));
        }
    }
	
	public void LogWarn(String message, Object...params)
    {
        if(logger.isEnabledFor(Level.WARN))
        {
            logger.warn(String.format(message, params));
        }
    }
	
	public void LogFatal(String message, Object...params)
    {
        if(logger.isEnabledFor(Level.FATAL))
        {
            logger.fatal(String.format(message, params));
        }
    }

    public void LogError(String message, Object...params)
    {
        if(logger.isEnabledFor(Level.ERROR))
        {
            logger.error(String.format(message, params));
        }
    }

    public void LogError(Throwable exception)
    {
        if(logger.isEnabledFor(Level.ERROR))
        {
            logger.error(exception);
        }
    }
}
