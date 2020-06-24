package tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations;

import java.lang.annotation.*;

/**
 * Tests with this annotation will only be run during nightly or continuous integration builds. Typically these tests
 * run longer than most tests, cover niche scenarios, or test failure case scenarios that are atypical for users.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface ContinuousIntegrationTest
{

}
