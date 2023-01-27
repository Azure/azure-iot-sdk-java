package tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations;

import java.lang.annotation.*;

/**
 * Tests that can only run against standard tier hubs. Usually these tests involve twin or methods, which are a feature
 * of standard tier hubs, but not basic tier hubs.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface StandardTierHubOnlyTest
{
}
