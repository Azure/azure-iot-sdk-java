# Pair Requirements

## Overview

A Device Twin Pair containing a name and value. This class can be used to describe tags, desired and reported properties to be transferred in json format.

## References

## Exposed API

```java
public final class Pair
{    
    public Pair(String key, Object value);  
    public String getKey();
    public Object getValue();
    public void setValue(Object newValue);  
    
}
```

### Pair

```java
public Pair(String key, Object value);  
```

**SRS_Pair_25_001: [**The constructor shall save the key and value representing this Pair.**]**

**SRS_Pair_25_002: [**If the key is null or empty, the constructor shall throw an IllegalArgumentException.**]**

**SRS_Pair_25_003: [**If the key contains illegal unicode control characters i.e ' ', '.', '$' or if length is greater than 128 chars, the constructor shall throw an IllegalArgumentException.**]**


### getKey

```java
public String getKey();
```

**SRS_Pair_25_004: [**The function shall return the value of the key corresponding to this Pair.**]**


### getValue

```java
public Object getValue();
```

**SRS_Pair_25_005: [**The function shall return the value for this Pair.**]**


### setValue

```java
public void setValue(Object newValue);
```

**SRS_Pair_25_006: [**The function shall overwrite the new value for old and return old value.**]**

