// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations;

import java.lang.annotation.*;

/**
 * Tests that run against IoT Hub
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface DigitalTwinTest
{

}
