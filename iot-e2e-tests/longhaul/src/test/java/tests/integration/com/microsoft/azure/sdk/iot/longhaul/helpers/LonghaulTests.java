/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers;


import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.rules.LonghaulTestRule;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Common rules, flags, and constants for all longhaul tests.
 */
@Slf4j
public abstract class LonghaulTests
{
    public static String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static final String MINUTES_PRINT_PATTERN = "#.##";
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(MINUTES_PRINT_PATTERN);

    public static final int SLEEP_PERIOD_IN_SECONDS = 60 * 10; // 10 minutes between each testable action
    public static final int LONGHAUL_TEST_LENGTH_HOURS = 1;

    public static final String SET_MINIMUM_POLLING_INTERVAL = "SetMinimumPollingInterval";
    public static final Long ONE_SECOND_POLLING_INTERVAL = 1000L;

    public static final int DELAY_BETWEEN_FAULT_INJECTIONS_MINUTES_MINUMUM = 5; // no more than one fault injection event every 5 minutes.
    public static final int DELAY_BETWEEN_FAULT_INJECTIONS_MINUTES_MAXIMUM = 60 * 2 - DELAY_BETWEEN_FAULT_INJECTIONS_MINUTES_MINUMUM; // at least one fault injection event every 2 hours

    @Rule
    public TestRule watcher = new TestWatcher()
    {
        protected void starting(Description description)
        {
            log.info("Starting test: {}", description.getMethodName());
        }

        protected void finished(Description description)
        {
            log.info("Finished test: {}", description.getMethodName());
        }
    };

    // This rule makes sure that the environment variables are setup to run these tests. By default, they are skipped
    @Rule
    public LonghaulTestRule longhaulTestRule = new LonghaulTestRule();

    @Rule
    public TestName testName = new TestName();

    public abstract String getTestClassName();

    @Before
    public void setupLogger()
    {
        try
        {
            // Setup logging settings dynamically rather than in log4j.properties file so that we can write logs for each
            // test to a custom log file rather than just write to system.out
            // This is also necessary because Azure Devops does not publish the test logs for a test that succeeded,
            // and we will definitely want those logs. So we log to files, and then publish those files to Azure Devops as
            // build artifacts.
            PatternLayout layout = new PatternLayout();
            layout.setConversionPattern("%d %p (%t) [%c] - %m%n");
            String testSpecificLogFile = "./src/logs/" + getTestClassName() + "/" + this.testName.getMethodName() + ".txt";
            FileAppender appender = new FileAppender(layout, testSpecificLogFile,false);
            org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
            rootLogger.addAppender(appender);
            rootLogger.setLevel(Level.TRACE);
        }
        catch (IOException e)
        {
            // shouldn't happen
            throw new RuntimeException("Failed to create log files for the test, aborting test run", e);
        }
    }

    @After
    public void intentionallyFail()
    {
        throw new RuntimeException("Test passed, but maven surefire only gives us logs if the test fails");
    }
}
