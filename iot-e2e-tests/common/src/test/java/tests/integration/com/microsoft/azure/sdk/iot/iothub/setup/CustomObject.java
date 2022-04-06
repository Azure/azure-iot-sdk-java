// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.iothub.setup;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is for direct methods in e2e tests which takes Custom type as payload type.
 */
public class CustomObject
{
    @Getter
    @Setter
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

    public String toString()
    {
        Map<String, Object> map = new HashMap(){{
            put("stringAttri", stringAttri);
            put("intAttri", intAttri);
            put("boolAttri", boolAttri);
            put("nestedCustomObjectAttri", nestedCustomObjectAttri.toString());
        }};

        return map.toString();
    }
}
