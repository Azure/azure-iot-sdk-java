package com.microsoft.azure.sdk.iot.common.helpers;

public class StandardTierOnlyRule implements ConditionalIgnoreRule.IgnoreCondition
{
    @Override
    public boolean isSatisfied()
    {
        return IntegrationTest.isBasicTierHub;
    }
}