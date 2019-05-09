/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.helpers;

public class StandardTierOnlyRule implements ConditionalIgnoreRule.IgnoreCondition
{
    @Override
    public boolean isSatisfied()
    {
        return IntegrationTest.isBasicTierHub;
    }
}