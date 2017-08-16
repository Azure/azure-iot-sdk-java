# SymmetricKeyParser Requirements

## Overview

Representation of a SymmetricKey object with a Json deserializer and serializer.

## References


## Exposed API

```java
public class SymmetricKeyParser
{
    public SymmetricKeyParser(String primaryKey, String secondaryKey);
    public SymmetricKeyParser(String json);
    public String toJson();

    public String getPrimaryKey();
    public void setPrimaryKey(String primaryKey);
    public String getSecondaryKey();
    public void setSecondaryKey(String secondaryKey);
}
```

### toJson
```java
public String toJson()
```
**SRS_SYMMETRIC_KEY_PARSER_34_007: [**This method shall return a json representation of this.**]**


### SymmetricKeyParser
```java
public SymmetricKeyParser(String primaryKey, String secondaryKey)
```
**SRS_SYMMETRIC_KEY_PARSER_34_008: [**This constructor shall create and return an instance of a SymmetricKeyParser object that holds the provided primary and secondary keys.**]**

```java
public SymmetricKeyParser(String json)
```
**SRS_SYMMETRIC_KEY_PARSER_34_009: [**This constructor shall create and return an instance of a SymmetricKeyParser object based off the provided json.**]**

**SRS_SYMMETRIC_KEY_PARSER_34_011: [**If the provided json null, empty, or cannot be parsed to a SymmetricKeyParser object, an IllegalArgumentException shall be thrown.**]**

**SRS_SYMMETRIC_KEY_PARSER_34_010: [**If the provided json is missing the field for either PrimaryKey or SecondaryKey, or either is missing a value, an IllegalArgumentException shall be thrown.**]**


### getPrimaryKey
```java
public String getPrimaryKey()
```
**SRS_SymmetricKeyParser_34_001: [**This method shall return the value of primaryKey **]**


### setPrimaryKey
```java
public void setPrimaryKey(String primaryKey);
```
**SRS_SymmetricKeyParser_34_002: [**If the provided primaryKey value is null, an IllegalArgumentException shall be thrown.**]**
**SRS_SymmetricKeyParser_34_003: [**This method shall set the value of primaryKey to the provided value.**]**


### getSecondaryKey
```java
public String getSecondaryKey();
```
**SRS_SymmetricKeyParser_34_004: [**This method shall return the value of secondaryKey **]**


### setSecondaryKey
```java
public void setSecondaryKey(String secondaryKey);
```
**SRS_SymmetricKeyParser_34_005: [**If the provided secondaryKey value is null, an IllegalArgumentException shall be thrown.**]**
**SRS_SymmetricKeyParser_34_006: [**This method shall set the value of secondaryKey to the provided value.**]**
