package com.microsoft.azure.sdk.iot.common.helpers.annotations;

import org.junit.Rule;
import org.junit.rules.Timeout;

import java.lang.annotation.*;

/**
 * Tests with this annotation will only be run during nightly or continuous integration builds. Typically these tests
 * run longer than most tests, cover niche scenarios, or test failure case scenarios that are atypical for users.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface ContinuousIntegrationTest {
    int E2E_TEST_TIMEOUT_MS = 17 * 60 * 1000;

    // Each CI test must finish in under 17 minutes
    @Rule
    Timeout timeout = new Timeout(E2E_TEST_TIMEOUT_MS);
}
