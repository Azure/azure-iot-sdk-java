# Pair Requirements

## Overview

Store the status and payload received as result of a method invoke.

## References

## Exposed API

```java
/**
 * Store the status and payload received as result of a method invoke.
 */
public final class MethodResult
{
    public MethodResult(Integer status, Object payload);
    public Integer getStatus();
    public Object getPayload();
}
```

### MethodResult
```java
public MethodResult(Integer status, Object payload);  
```
**SRS_METHODRESULT_21_001: [**The constructor shall save the status and payload representing the method invoke result.**]**
**SRS_METHODRESULT_21_002: [**There is no restrictions for these values, it can be empty, or null.**]**

### getStatus
```java
public Integer getStatus();
```
**SRS_METHODRESULT_21_003: [**The getStatus shall return the status stored by the constructor.**]**

### getPayload
```java
public Object getPayload();
```
**SRS_METHODRESULT_21_004: [**The getPayload shall return the payload stored by the constructor.**]**
