package com.microsoft.azure.sdk.iot.common.helpers.annotations;

import org.junit.Rule;
import org.junit.rules.Timeout;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface StandardTierHubOnlyTest
{
}
