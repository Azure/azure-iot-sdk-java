// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.auth;

/**
 * Enum for the type of authentication used.
 * SAS - shared access signature
 * SELF_SIGNED - self signed certificate
 * CERTIFICATE_AUTHORITY - certificate authority signed certificate
 */
public enum AuthenticationType
{
    SAS(0,"sas"),
    SELF_SIGNED(1, "selfSigned"),
    CERTIFICATE_AUTHORITY(2,"certificateAuthority");

    private int key;
    private String value;

    AuthenticationType(int key, String value){
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString(){
        return this.value;
    }
}