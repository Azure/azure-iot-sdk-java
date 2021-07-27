package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.deps.convention.PayloadConvention;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;


/**
 * The base class for the {@code ClientPropertyCollection}.
 */
public class PayloadCollection extends HashMap<String, Object>
{
    @Getter
    @Setter
    /**
     * The convention to be used with this payload collection.
     */
    public PayloadConvention Convention;
}
