# X509ThumbprintParser Requirements

## Overview

Representation of a X509Thumbprint object with a Json deserializer and serializer.

## References


## Exposed API

```java
public class X509ThumbprintParser
{
    public X509ThumbprintParser(String primaryThumbprint, String secondaryThumbprint);
    public X509ThumbprintParser(String json);
    public String toJson();

    public String getPrimaryThumbprint();
    public void setPrimaryThumbprint(String primaryThumbprint);
    public String getSecondaryThumbprint();
    public void setSecondaryThumbprint(String secondaryThumbprint);
}
```

### toJson
```java
public String toJson()
```
**SRS_X509ThumbprintParser_34_007: [**This method shall return a json representation of this.**]**


### X509ThumbprintParser
```java
public X509ThumbprintParser(String primaryThumbprint, String secondaryThumbprint)
```
**SRS_X509ThumbprintParser_34_008: [**The parser shall create and return an instance of a X509ThumbprintParser object that holds the provided primary and secondary thumbprints.**]**

```java
public X509ThumbprintParser(String json)
```
**SRS_X509ThumbprintParser_34_009: [**The parser shall create and return an instance of a X509ThumbprintParser object based off the provided json.**]**

**SRS_X509ThumbprintParser_34_010: [**If the provided json is null or empty or cannot be parsed into an X509Thumbprint object, an IllegalArgumentException shall be thrown.**]**


### getPrimaryThumbprint
```java
public String getPrimaryThumbprint();
```
**SRS_X509ThumbprintParser_34_001: [**This method shall return the value of primaryThumbprint.**]**


### setPrimaryThumbprint
```java
public void setPrimaryThumbprint(String primaryThumbprint);
```
**SRS_X509ThumbprintParser_34_002: [**If the provided primaryThumbprint value is null, an IllegalArgumentException shall be thrown.**]**
**SRS_X509ThumbprintParser_34_003: [**This method shall set the value of primaryThumbprint to the provided value.**]**


### getSecondaryThumbprint
```java
public String getSecondaryThumbprint();
```
**SRS_X509ThumbprintParser_34_004: [**This method shall return the value of secondaryThumbprint.**]**


### setSecondaryThumbprint
```java
public void setSecondaryThumbprint(String secondaryThumbprint);
```
**SRS_X509ThumbprintParser_34_005: [**If the provided secondaryThumbprint value is null, an IllegalArgumentException shall be thrown.**]**
**SRS_X509ThumbprintParser_34_006: [**This method shall set the value of secondaryThumbprint to the provided value.**]**
