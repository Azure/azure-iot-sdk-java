package com.microsoft.azure.sdk.iot.common.helpers.annotations;

import java.lang.annotation.*;

/**
 * Tests that only run against basic tier IoT Hubs
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface BasicTierHubOnlyTest
{
}
