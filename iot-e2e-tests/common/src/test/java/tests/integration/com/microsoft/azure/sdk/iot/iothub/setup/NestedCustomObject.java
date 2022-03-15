// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.iothub.setup;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class NestedCustomObject
{
    @Getter
    private String stringAttri;

    @Getter
    private int intAttri;

    public NestedCustomObject(String stringAttri, int intAttri)
    {
        this.stringAttri = stringAttri;
        this.intAttri = intAttri;
    }

    public String toString()
    {
        Map<String, Object> map = new HashMap(){{
            put("stringAttri", stringAttri);
            put("intAttri", intAttri);
        }};

        return map.toString();
    }
}
