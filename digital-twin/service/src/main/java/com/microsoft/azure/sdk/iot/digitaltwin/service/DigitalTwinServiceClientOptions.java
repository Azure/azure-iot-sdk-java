// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service;

/**
 * The configurable options for a digital twin service client. By default, the SDK will choose to use the latest service version
 */
public class DigitalTwinServiceClientOptions
{
    private String apiVersion;

    public enum ServiceVersion
    {
        V2019_07_01_preview
    }

    /**
     * @return The string representation of the API version being used
     */
    public String getApiVersion()
    {
        return apiVersion;
    }

    private void setApiVersion(String apiVersion)
    {
        this.apiVersion = apiVersion;
    }

    /**
     * Create a digital twin service client options instance. By default, we will use the latest service API version.
     */
    public DigitalTwinServiceClientOptions()
    {
        this(ServiceVersion.V2019_07_01_preview);
    }

    /**
     * Create a digital twin service client options instance. The digital twin service client will use the specified service version
     * for all operations
     * @param serviceVersion The service api version.
     */
    private DigitalTwinServiceClientOptions(ServiceVersion serviceVersion)
    {
        String apiVersion = serviceVersionToString(serviceVersion);
        setApiVersion(apiVersion);
    }

    private String serviceVersionToString(ServiceVersion serviceVersion)
    {
        switch (serviceVersion)
        {
            case V2019_07_01_preview:
                return "2019-07-01-preview";
            default:
                throw new IllegalArgumentException("Unrecognized API version.");
        }
    }
}
