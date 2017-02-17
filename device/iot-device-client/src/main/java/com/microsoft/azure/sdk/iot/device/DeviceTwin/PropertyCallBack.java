// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.DeviceTwin;

public interface PropertyCallBack <Type1, Type2>
{
    void PropertyCall(Type1 propertyKey, Type2 propertyValue,  Object context);
}
