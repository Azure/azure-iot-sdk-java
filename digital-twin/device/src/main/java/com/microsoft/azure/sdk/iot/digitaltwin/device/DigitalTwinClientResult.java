// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.device;

public enum DigitalTwinClientResult {
    /** The operation is successful. */
    DIGITALTWIN_CLIENT_OK,
    /** The operation failed since registration is still processing. */
    DIGITALTWIN_CLIENT_ERROR_REGISTRATION_PENDING,
    /** The operation failed since registration is already done.
     * Most likely caused by registration API been called multi times.
     */
    DIGITALTWIN_CLIENT_ERROR_INTERFACE_ALREADY_REGISTERED,
    /**
     * The operation failed since registration hasn't been completed.
     * Most likely caused by calling an API before registration completed.
     */
    DIGITALTWIN_CLIENT_ERROR_INTERFACE_NOT_REGISTERED,
    /**
     * The operation failed since there was unexpected exception and there is no suggested way to recover.
     * Please check the log to figure out the details.
     */
    DIGITALTWIN_CLIENT_ERROR
}
