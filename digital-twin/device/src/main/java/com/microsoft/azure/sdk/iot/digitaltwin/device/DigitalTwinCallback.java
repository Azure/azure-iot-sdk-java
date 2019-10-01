// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.device;

/**
 * User specified callback that will be invoked on async operation completion or failure.
 */
public interface DigitalTwinCallback {
    /**
     * Function to be invoked when the async operation is successfully or fails.
     *
     * @param digitalTwinClientResult Result for the async operation
     * @param context                 Context passed in when async operation is invoked.
     */
    void onResult(DigitalTwinClientResult digitalTwinClientResult, Object context);
}
