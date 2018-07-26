// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.CustomLogger;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Unit tests for CustomLogger. */
@Ignore
public class CustomLoggerTest
{
    // Tests_SRS_CUSTOMERLOGGER_25_001: [The constructor shall create the logger instance.]
    @Test
    public void constructorCreatesLogger()
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
                LogManager.getLogger((Class)any);
                result = mockLogger;
                mockLogger.isInfoEnabled();
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
                LogManager.getLogger((Class)any);
                result = mockLogger;
                mockLogger.isDebugEnabled();
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
                LogManager.getLogger((Class)any);
                result = mockLogger;
                mockLogger.isTraceEnabled();
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
                LogManager.getLogger((Class)any);
                result = mockLogger;
                mockLogger.isWarnEnabled();
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
               LogManager.getLogger((Class)any);
               result = mockLogger;
                mockLogger.isFatalEnabled();
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
                LogManager.getLogger((Class)any);
                result = mockLogger;
                mockLogger.isErrorEnabled();
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
                LogManager.getLogger((Class)any);
                result = mockLogger;
                mockLogger.isErrorEnabled();
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
