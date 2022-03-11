// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.iothub.setup;

import lombok.Getter;

public class CustomObject
{
    @Getter
    private String stringAttri;

    @Getter
    private int intAttri;

    private boolean boolAttri;

    public boolean getBooleanAttri() { return this.boolAttri; }

    @Getter
    private NestedCustomObject nestedCustomObjectAttri;

    public CustomObject(String stringAttri, int intAttri, boolean boolAttri, NestedCustomObject nestedCustomObjectAttri)
    {
        this.stringAttri = stringAttri;
        this.intAttri = intAttri;
        this.boolAttri = boolAttri;
        this.nestedCustomObjectAttri = nestedCustomObjectAttri;
    }
}
