// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class CustomAllocationDefinition implements Serializable
{
    // the webhook url for allocation requests
    private static final String WEBHOOK_URL_TAG = "webhookUrl";
    @Expose
    @SerializedName(WEBHOOK_URL_TAG)
    @Getter
    @Setter
    private String webhookUrl;

    // the API version of the provisioning service types (such as IndividualEnrollment) sent in the custom allocation request.
    private static final String API_VERSION_TAG = "apiVersion";
    @Expose
    @SerializedName(API_VERSION_TAG)
    @Getter
    @Setter
    private String apiVersion;
}
