package tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations;

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
