// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.CustomLogger;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/** Unit tests for CustomLogger. */
public class CustomLoggerTest
{
    // Tests_SRS_CUSTOMERLOGGER_25_001: [The constructor shall create the logger instance.]
    @Test
    public void contructorCreatesLogger()
    {
        CustomLogger logger = new CustomLogger(this.getClass());
        assertThat(logger, notNullValue());
    }

    // Tests_SRS_CUSTOMERLOGGER_25_002: [The function shall log message for all levels.]
    @Test
    public void testMethodSignature()
    {
        CustomLogger logger = new CustomLogger(this.getClass());
        
        String message = "This is INFO message";
        logger.LogInfo(message, logger.getMethodName());
        message = "This is DEBUG message";
        logger.LogDebug(message, logger.getMethodName());
        message = "This is TRACE message";
        logger.LogTrace(message, logger.getMethodName());
        message = "This is WARN message";
        logger.LogWarn(message, logger.getMethodName());
        message = "This is FATAL message";
        logger.LogFatal(message, logger.getMethodName());
        message = "This is CUSTOM ERROR message";
        logger.LogError(message, logger.getMethodName());
        message = "This is ERROR message";
        logger.LogError(new Throwable(message));
    }

    // Tests_SRS_CUSTOMERLOGGER_25_002: [The function shall log message for all levels.]
    @Test
    public void getExecutingMethodName()
    {
        CustomLogger logger = new CustomLogger(this.getClass());
        String methodName = logger.getMethodName();
        assertThat("getExecutingMethodName", is(equalTo(methodName)));
    }

    // Tests_SRS_CUSTOMERLOGGER_25_002: [The function shall print message for INFO level.]
    @Test
    public void testLogInfo(@Mocked final Logger mockLogger)
    {
        final String message = "This is INFO message";

        new NonStrictExpectations()
        {
            {
                Logger.getLogger((Class)any);
                result = mockLogger;
                mockLogger.isEnabledFor(Level.INFO);
                result = true;
            }
        };

        new CustomLogger(this.getClass()).LogInfo(message);	

        new Verifications()
        {
            {
                mockLogger.info(anyString);
                times = 1; 
            }
        };
    }

    // Tests_SRS_CUSTOMERLOGGER_25_002: [The function shall print message for DEBUG level.]
    @Test
    public void testLogDebug(@Mocked final Logger mockLogger)
    {
        final String message = "This is DEBUG message";

        new NonStrictExpectations()
        {
            {
                Logger.getLogger((Class)any);
                result = mockLogger;
                mockLogger.isEnabledFor(Level.DEBUG);
                result = true;
            }
        };

        new CustomLogger(this.getClass()).LogDebug(message);	

        new Verifications()
        {
            {
                mockLogger.debug(anyString);
                times = 1; 
            }
        };
    }

    // Tests_SRS_CUSTOMERLOGGER_25_002: [The function shall print message for TRACE level.]
    @Test
    public void testLogTrace(@Mocked final Logger mockLogger)
    {
        final String message = "This is TRACE message";

        new NonStrictExpectations()
        {
            {
                Logger.getLogger((Class)any);
                result = mockLogger;
                mockLogger.isEnabledFor(Level.TRACE);
                result = true;
            }
        };

        new CustomLogger(this.getClass()).LogTrace(message);	

        new Verifications()
        {
            {
                mockLogger.trace(anyString);
                times = 1; 
            }
        };
    }

    // Tests_SRS_CUSTOMERLOGGER_25_002: [The function shall print message for WARN level.]
    @Test
    public void testLogWarn(@Mocked final Logger mockLogger)
    {
        final String message = "This is WARN message";

        new NonStrictExpectations()
        {
            {
                Logger.getLogger((Class)any);
                result = mockLogger;
                mockLogger.isEnabledFor(Level.WARN);
                result = true;
            }
        };

        new CustomLogger(this.getClass()).LogWarn(message);	

        new Verifications()
        {
           {
                mockLogger.warn(anyString);
                times = 1; 
           }
       };
    }

  	// Tests_SRS_CUSTOMERLOGGER_25_002: [The function shall print message for FATAL level.]
    @Test
    public void testLogFatal(@Mocked final Logger mockLogger)
    {
        final String message = "This is FATAL message";

        new NonStrictExpectations()
        {
           {
               Logger.getLogger((Class)any);
               result = mockLogger;
                mockLogger.isEnabledFor(Level.FATAL);
                result = true;
            }
        };

        new CustomLogger(this.getClass()).LogFatal(message);	

        new Verifications()
        {
            {
                mockLogger.fatal(anyString);
                times = 1; 
            }
        };
    }

    // Tests_SRS_CUSTOMERLOGGER_25_002: [The function shall print message for ERROR level.]
    @Test
    public void testLogError(@Mocked final Logger mockLogger)
    {
        final String message = "This is CUSTOM ERROR message";

        new NonStrictExpectations()
        {
            {
                Logger.getLogger((Class)any);
                result = mockLogger;
                mockLogger.isEnabledFor(Level.ERROR);
                result = true;
            }
        };

        new CustomLogger(this.getClass()).LogError(message);	
        new Verifications()
        {
            {
                mockLogger.error(anyString);
                times = 1; 
            }
        };

        new NonStrictExpectations()
        {
            {
                Logger.getLogger((Class)any);
                result = mockLogger;
                mockLogger.isEnabledFor(Level.ERROR);
                result = true;
            }
        };

        new CustomLogger(this.getClass()).LogError(new Throwable("This is ERROR message"));
        new Verifications()
        {
            {
                mockLogger.error(any);
                times = 1; 
            }
        };
    }
}
