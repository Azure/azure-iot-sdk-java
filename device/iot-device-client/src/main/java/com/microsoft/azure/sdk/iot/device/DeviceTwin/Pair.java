// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.DeviceTwin;

public class Pair<Type1, Type2>
{
    private final Type1 key;
    private Type2 value;

    public Pair(Type1 t1, Type2 t2)
    {
        /*
        **Codes_SRS_Pair_25_001: [**The constructor shall save the key and value representing this Pair.**]**
         */
        this.key = t1;
        this.value = t2;
    }

    public Type1 getKey()
    {
        /*
        **Codes_SRS_Pair_25_002: [**The function shall return the value of the key corresponding to this Pair.**]**
         */
        return key;
    }

    public Type2 getValue()
    {
        /*
        **Codes_SRS_Pair_25_003: [**The function shall return the value for this Pair.**]**
         */
        return value;
    }

    public Type2 setValue(Type2 newValue)
    {
        /*
        **Codes_SRS_Pair_25_004: [**The function shall overwrite the new value for old and return old value.**]**
         */
        Type2 oldValue = this.value;
        this.value = newValue;
        return oldValue;
    }
}
