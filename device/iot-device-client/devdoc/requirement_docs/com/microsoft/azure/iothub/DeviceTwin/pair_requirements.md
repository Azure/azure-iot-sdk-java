# Pair Requirements

## Overview

A Pair is a utility representing generic type of key and generic type of value defined by the user.

## References

## Exposed API

```java
public final class Pair<Type1, Type2>
{    
    public Pair(Type1 key, Type2 value);  
    public Type1 getKey();
    public Type2 getValue();
    public Type2 setValue(Object newValue);  
    
}
```

### Pair

```java
public Pair(Type1 key, Type2 value); 
```

**SRS_Pair_25_001: [**The constructor shall save the key and value representing this Pair.**]**


### getKey

```java
public Type1 getKey();
```

**SRS_Pair_25_002: [**The function shall return the value of the key corresponding to this Pair.**]**


### getValue

```java
public Type2 getValue();
```

**SRS_Pair_25_003: [**The function shall return the value for this Pair.**]**


### setValue

```java
public Type2 setValue(Object newValue);
```

**SRS_Pair_25_004: [**The function shall overwrite the new value for old and return old value.**]**

