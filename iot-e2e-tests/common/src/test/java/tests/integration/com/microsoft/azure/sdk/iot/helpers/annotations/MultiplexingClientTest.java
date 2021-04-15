package tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tests that use the {@link com.microsoft.azure.sdk.iot.device.MultiplexingClient}. This annotation adds some additional
 * logging to a test when it fails from a {@link com.microsoft.azure.sdk.iot.device.exceptions.MultiplexingClientDeviceRegistrationAuthenticationException}.
 * see {@link tests.integration.com.microsoft.azure.sdk.iot.helpers.rules.MultiplexingClientTestRule} for more details.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface MultiplexingClientTest
{

}
