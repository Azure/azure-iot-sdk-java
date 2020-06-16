package com.microsoft.azure.sdk.iot.common.helpers.annotations;

import java.lang.annotation.*;

/**
 * Tests that have been identified as flakey. Ideally, these tests will be fixed or deleted eventually.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface FlakeyTest
{

}
