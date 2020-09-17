// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin.models;

/**
 * Represents the headers for InvokeCommandAsync and InvokeComponentCommandAsync operation.
 */
public final class DigitalTwinInvokeCommandHeaders {

    /**
     * Server Generated Request Id (GUID), to uniquely identify this request in the service.
     */
    private String requestId;

    public String getRequestId()
    {
        return requestId;
    }

    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
    }
}
