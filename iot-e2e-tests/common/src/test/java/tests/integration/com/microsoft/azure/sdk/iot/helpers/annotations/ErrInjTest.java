package tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations;

import java.lang.annotation.*;

/**
 * Tests that run against IoT Hub with error injection.
 * If you are using a hub that is not part of our group with the error injection feature flag turned on you need to set
 * the RUN_ERRINJ_TESTS variable to false. Otherwise you'll have a number of test failures.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface ErrInjTest
{

}
