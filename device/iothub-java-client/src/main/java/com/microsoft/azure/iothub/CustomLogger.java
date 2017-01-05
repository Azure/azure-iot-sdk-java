package com.microsoft.azure.iothub;

import org.apache.log4j.*;
import java.util.Date;

public class CustomLogger {
	
	private static final Logger  logger = Logger.getLogger(CustomLogger.class);
	
	protected final static String INFO = "[INFO]";
	protected final static String ERROR = "[ERROR]";
	
	public void LogInfo(String message)
	{
		if(logger.isEnabledFor(Level.INFO))
		{
			logger.info(String.format("%s %s %s",INFO, new Date(), message));
		}
	}
	
	public void LogError(String message)
	{
		if(logger.isEnabledFor(Level.ERROR))
		{
			logger.error(String.format("%s %s %s",ERROR, new Date(), message));
		}
	}
}
