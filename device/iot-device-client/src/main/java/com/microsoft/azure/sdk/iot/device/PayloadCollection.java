package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.deps.convention.PayloadConvention;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

public class PayloadCollection extends HashMap<String, Object>
{
    @Getter
    @Setter
    public PayloadConvention Convention;

    @Override
    public Object put(String key, Object value)
    {
        return super.put(key, value);
    }
}
