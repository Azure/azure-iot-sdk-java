# X509Thumbprint Requirements

## Overview

Store primary thumbprint and secondary thumbprint.

## References

## Exposed API

```java
public class X509Thumbprint
{
    public X509Thumbprint();
    public X509Thumbprint(String primaryThumbprint, String secondaryThumbprint);

    public String getPrimaryThumbprint();
    public String getSecondaryThumbprint();
    public void setPrimaryThumbprint(String primaryThumbprint);
    public void setSecondaryThumbprint(String secondaryThumbprint);
}
```

### X509Thumbprint

```java
public X509Thumbprint();
```
**SRS_X509THUMBPRINT_34_011: [**This constructor shall generate a random primary and secondary thumbprint.**]**


```java
public X509Thumbprint(String primaryThumbprint, String secondaryThumbprint);
```
**SRS_X509THUMBPRINT_34_006: [**This constructor shall create an X509Thumbprint with the provided primary thumbprint and the provided secondary thumbprint.**]**
**SRS_X509THUMBPRINT_34_010: [**This constructor shall throw an IllegalArgumentException if the provided thumbprints are null, empty, or not a valid format.**]**


### getPrimaryThumbprint

```java
public String getPrimaryThumbprint();
```
**SRS_X509THUMBPRINT_34_001: [**The function shall return the primary thumbprint value of this.**]**


### getSecondaryThumbprint

```java
public String getSecondaryThumbprint();
```
**SRS_X509THUMBPRINT_34_002: [**The function shall return the secondary thumbprint value of this.**]**


### setPrimaryThumbprint

```java
public void setPrimaryThumbprint(String primaryThumbprint);
```
**SRS_X509THUMBPRINT_34_003: [**The function shall set the primary thumbprint to the given value.**]**
**SRS_X509THUMBPRINT_34_007: [**If the provided thumbprint string is null, empty, or not the proper format, an IllegalArgumentException shall be thrown.**]**


### setSecondaryThumbprint

```java
public void setSecondaryThumbprint(String secondaryThumbprint);
```
**SRS_X509THUMBPRINT_34_004: [**The function shall set the secondary thumbprint to the given value.**]**
**SRS_X509THUMBPRINT_34_008: [**If the provided thumbprint string is not the proper format, an IllegalArgumentException shall be thrown.**]**