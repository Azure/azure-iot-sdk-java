package com.microsoft.azure.iothub;

import org.apache.log4j.*;

public class CustomLogger {
	
	private static final Logger  logger = Logger.getLogger(CustomLogger.class);
	
	public void LogInfo(String message)
	{
		if(logger.isEnabledFor(Level.INFO))
		{
			logger.info("[ERROR]" + message);
		}
	}
	
	public void LogError(String message)
	{
		if(logger.isEnabledFor(Level.ERROR))
		{
			logger.error("[INFO]" + message);
		}
	}
}
