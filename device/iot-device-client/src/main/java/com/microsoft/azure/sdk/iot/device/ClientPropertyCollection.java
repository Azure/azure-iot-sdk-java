package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;

import java.util.*;

public class ClientPropertyCollection extends PayloadCollection
{
    public Set<Property> getCollectionAsSetOfProperty()
    {
        HashSet<Property> toReturn = new HashSet<>();
        for (Map.Entry<String, Object> entry : this.entrySet())
        {
            toReturn.add(new Property(entry.getKey(), entry.getValue()));
        }
        return toReturn;
    }

    public <T> T getValue(String key)
    {
        Object objectToGet = this.get(key);
        if (objectToGet != null)
        {
            return Convention.PayloadSerializer.convertFromObject(objectToGet);
        }
        return null;
    }
}
