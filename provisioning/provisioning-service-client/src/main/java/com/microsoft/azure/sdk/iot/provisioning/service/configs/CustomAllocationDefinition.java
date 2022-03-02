// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CustomAllocationDefinition implements Serializable
{
    // the webhook url for allocation requests
    private static final String WEBHOOK_URL_TAG = "webhookUrl";
    @Expose
    @SerializedName(WEBHOOK_URL_TAG)
    private String webhookUrl;

    // the API version of the provisioning service types (such as IndividualEnrollment) sent in the custom allocation request.
    private static final String API_VERSION_TAG = "apiVersion";
    @Expose
    @SerializedName(API_VERSION_TAG)
    private String apiVersion;

    /**
     * Getter for the webhook URL used for allocation requests.
     *
     * @return The {@code URL} with the webhook url content.
     */
    public String getWebhookUrl()
    {
        //Codes_SRS_CUSTOM_ALLOCATION_DEFINITION_34_001: [This function shall return the saved webhook uri.]
        return this.webhookUrl;
    }

    /**
     * Setter for the webhook URL.
     *
     * @param webhookUrl the {@code URL} with the webhook URL used for allocation requests.
     */
    public void setWebhookUrl(String webhookUrl)
    {
        //Codes_SRS_CUSTOM_ALLOCATION_DEFINITION_34_002: [This function shall save the provided webhook uri.]
        this.webhookUrl = webhookUrl;
    }

    /**
     * Getter for the API version.
     *
     * @return The {@code String} with the API version content.
     */
    public String getApiVersion()
    {
        //Codes_SRS_CUSTOM_ALLOCATION_DEFINITION_34_003: [This function shall return the saved api version.]
        return this.apiVersion;
    }

    /**
     * Setter for the API version.
     *
     * @param apiVersion the {@code String} with the API version of the provisioning service types (such as IndividualEnrollment) sent in the custom allocation request.
     */
    public void setApiVersion(String apiVersion)
    {
        //Codes_SRS_CUSTOM_ALLOCATION_DEFINITION_34_004: [This function shall save the provided api version.]
        this.apiVersion = apiVersion;
    }
}
