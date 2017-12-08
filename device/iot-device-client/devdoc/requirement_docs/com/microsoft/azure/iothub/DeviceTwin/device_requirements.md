# Device Requirements

## Overview

An abstraction of the Device Twin device exposed to user as a utility to help perform device twin operations

## References

## Exposed API

```java
abstract public class Device implements PropertyCallBack<String, Object>
{    
    public HashSet<Property> getReportedProp();
    public void hasReportedProp(Property reportedProp);

    public HashMap<Property, Pair<PropertyCallBack<String, Object>, Object>> getDesiredProp();    
    public void hasDesiredProperty(Property desiredProp, PropertyCallBack<String, Object> desiredPropCallBack, Object desiredPropCallBackContext);

    public void clean();
}
```


### getReportedProp

```java
public HashSet<Property> getReportedProp();
```

**SRS_DEVICE_25_001: [**This method shall return a HashSet of properties that user has set by calling hasReportedProp.**]**


### hasReportedProp

```java
public void hasReportedProp(Property reportedProp);
```

**SRS_DEVICE_25_002: [**The function shall add the new property to the map.**]**

**SRS_DEVICE_25_003: [**If the already existing property is altered and added then this method shall replace the old one.**]**

**SRS_DEVICE_25_004: [**If the parameter reportedProp is null then this method shall throw IllegalArgumentException**]**


### getDesiredProp

```java
public HashMap<Property, Pair<PropertyCallBack<String, Object>, Object>> getDesiredProp();   
```

**SRS_DEVICE_25_005: [**The function shall return the HashMap containing the property and its callback and context pair set by the user so far.**]**


### hasDesiredProperty

```java
public void hasDesiredProperty(Property desiredProp, PropertyCallBack<String, Object> desiredPropCallBack, Object desiredPropCallBackContext);
```

**SRS_DEVICE_25_006: [**The function shall add the property and its callback and context pair to the user map of desired properties.**]**

**SRS_DEVICE_25_007: [**If the parameter desiredProp is null then this method shall throw IllegalArgumentException**]**

**SRS_DEVICE_25_008: [**This method shall add the parameters to the map even if callback and object pair are null**]**

### clean

```java
public void clean();
```

**SRS_DEVICE_34_009: [**The method shall remove all the reported and desired properties set by the user so far.**]**

