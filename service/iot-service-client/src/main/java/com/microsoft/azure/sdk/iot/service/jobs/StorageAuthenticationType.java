package com.microsoft.azure.sdk.iot.service.jobs;

import com.google.gson.annotations.SerializedName;
/* Specifies authentication type being used for connecting to storage account */
public enum StorageAuthenticationType
{
    /** Use a shared access key for authentication
     * This means authentication must be supplied in the storage URI(s) */
    @SerializedName("keyBased")
    KEY,

    /** Use the Active Directory identity configured on the hub for authentication to storage */
    @SerializedName("identityBased")
    IDENTITY,
}
