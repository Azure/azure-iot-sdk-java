// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.DeviceTwin;

import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;

/**
 * Interface for receiving desired property callbacks all at once. See {@link TwinPropertyCallback} for the
 * interface for receiving desired property callbacks one at a time.
 */
public interface TwinPropertiesCallback
{
    void onTwinPropertiesChanged(TwinCollection properties, Object context);
}
