package com.microsoft.azure.sdk.iot.common.helpers.annotations;

import java.lang.annotation.*;

/**
 * Tests that use the Device Provisioning Service
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface DeviceProvisioningServiceTest
{

}
