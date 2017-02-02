// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device;
	
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import com.microsoft.azure.sdk.iot.device.CustomLogger;
import org.apache.log4j.*;
import mockit.Mocked;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Test;

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
    public void allLevelMessageLogging()
    {
        CustomLogger logger = new CustomLogger(this.getClass());
        mockLogger();
        String message = "This is INFO message";
        logger.LogInfo(message, logger.getMethodName());
        message = "This is DEBUG message";
        logger.LogDebug(message, logger.getMethodName());
        message = "This is TRACE message";
        logger.LogTrace(message, logger.getMethodName());
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
	// Tests_SRS_CUSTOMERLOGGER_25_002: [The function shall mock CustomLogger for unit testing.]
	private void mockLogger() {
        new MockUp<CustomLogger>() {
            @Mock
            public void LogInfo(String msg, Object... params) {
                assertThat("This is INFO message", is(equalTo(msg)));
            }
			@Mock
            public void LogDebug(String msg, Object... params) {
                assertThat("This is DEBUG message", is(equalTo(msg)));
            }
            @Mock
            public void LogTrace(String msg, Object... params) {
                assertThat("This is TRACE message", is(equalTo(msg)));
            }
            @Mock
            public void LogWarn(String msg, Object... params) {
                assertThat("This is WARN message", is(equalTo(msg)));
            }
            @Mock
            public void LogFatal(String msg, Object... params) {
                assertThat("This is FATAL message", is(equalTo(msg)));
            }
            @Mock
            public void LogError(String msg, Object... params) {
                assertThat("This is CUSTOM ERROR message", is(equalTo(msg)));
            }
            @Mock
            public void LogError(Throwable exception) {
                assertThat("This is ERROR message", is(equalTo(exception.getMessage())));
            }
        };
    }
}

