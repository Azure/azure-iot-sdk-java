package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.deps.convention.PayloadConvention;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import lombok.Getter;
import lombok.Setter;


public class PayloadCollection extends TwinCollection
{
    @Getter
    @Setter
    public PayloadConvention Convention;
}
