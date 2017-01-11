package com.microsoft.azure.iothub;

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
