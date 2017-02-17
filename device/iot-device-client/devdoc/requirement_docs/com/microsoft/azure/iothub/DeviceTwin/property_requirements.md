# Property Requirements

## Overview

A Device Twin property containing a name and value. This class can be used to describe desired and reported properties.

## References

## Exposed API

```java
public final class Property
{    
    public Property(String key, Object value);  
    public String getKey();
    public Object getValue();
    public void setValue(Object newValue);  
    
}
```

### Property

```java
public Property(String key, Object value);  
```

**SRS_Property_25_001: [**The constructor shall save the key and value representing this property.**]**

**SRS_Property_25_002: [**If the key is null or empty, the constructor shall throw an IllegalArgumentException.**]**

**SRS_Property_25_006: [**If the key contains illegal unicode control characters i.e ' ', '.', '$', the constructor shall throw an IllegalArgumentException.**]**


### getKey

```java
public String getKey();
```

**SRS_Property_25_003: [**The function shall return the value of the key corresponding to this property.**]**


### getValue

```java
public Object getValue();
```

**SRS_Property_25_004: [**The function shall return the value for this property.**]**


### setValue

```java
public void setValue(Object newValue);
```

**SRS_Property_25_005: [**The function shall overwrite the new value for old and return old value.**]**

