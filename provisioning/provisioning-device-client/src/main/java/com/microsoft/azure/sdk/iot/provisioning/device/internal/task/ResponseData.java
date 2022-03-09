/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.task;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class ResponseData
{
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private byte[] responseData;

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private ContractState contractState = ContractState.DPS_REGISTRATION_UNKNOWN;

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private long waitForStatusInMilliseconds = 0L;

    /**
     * Constructor to create null data and Unknown contract state
     */
    ResponseData()
    {
    }

    /**
     * Contrautor for Response Data
     * @param responseData response data value. Can be {@code null}
     * @param contractState contract state value. Can be {@code null}
     * @param waitForStatusInMilliseconds Maximum time to wait before query status. Can be {@code null}
     */
    public ResponseData(byte[] responseData, ContractState contractState, long waitForStatusInMilliseconds)
    {
        this.responseData = responseData;
        this.contractState = contractState;
        this.waitForStatusInMilliseconds = waitForStatusInMilliseconds;
    }
}
