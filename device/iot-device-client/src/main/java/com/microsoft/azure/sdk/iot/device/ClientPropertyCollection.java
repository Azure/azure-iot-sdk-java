package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;

import java.util.*;

public class ClientPropertyCollection extends PayloadCollection
{

    public Set<Property> getSetOfProperty()
    {
        return new HashSet<>();
    }
}
