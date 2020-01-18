// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.device;

public enum DigitalTwinClientResult {
    /** The operation is successful. */
    DIGITALTWIN_CLIENT_OK,
    /** The operation failed since bind is already done.
     * Most likely caused by bind API been called multi times.
     */
    DIGITALTWIN_CLIENT_ERROR_COMPONENTS_ALREADY_BOUND,
    /**
     * The operation failed since bind hasn't been completed.
     * Most likely caused by calling an API before bind completed.
     */
    DIGITALTWIN_CLIENT_ERROR_COMPONENTS_NOT_BOUND,
    /**
     * The operation failed since there was unexpected exception and there is no suggested way to recover.
     * Please check the log to figure out the details.
     */
    DIGITALTWIN_CLIENT_ERROR
}
