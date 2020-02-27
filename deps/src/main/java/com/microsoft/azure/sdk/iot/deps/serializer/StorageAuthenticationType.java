package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.annotations.SerializedName;
/* Specifies authentication type being used for connecting to storage account */
public enum StorageAuthenticationType
{
    /* Use a shared access key for authentication */
    @SerializedName("keyBased")
    KEY,

    /* Use the AD identity configured on the hub for authentication to storage */
    @SerializedName("identityBased")
    IDENTITY,
}
