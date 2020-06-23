package com.microsoft.azure.sdk.iot.common.helpers.annotations;

import java.lang.annotation.*;

/**
 * Tests that run against IoT Hub
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface IotHubTest
{

}
