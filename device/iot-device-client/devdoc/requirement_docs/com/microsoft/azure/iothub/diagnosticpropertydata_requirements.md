# DiagnosticPropertyData Requirements

## Overview

A message of diagnostic data

## References

## Exposed API

```java
public class DiagnosticPropertyData
{
    public DiagnosticPropertyData(String diagnosticId, String diagnosticCreationTimeUtc);
    public String getDiagnosticId();
    public void setDiagnosticId(String diagnosticId);
    public String getDiagnosticCreationTimeUtc();
    public void setDiagnosticCreationTimeUtc(String diagnosticCreationTimeUtc);
    public String getCorrelationContext();
}
```
## Validation
**SRS_DIAGNOSTICPROPERTYDATA_02_001: [**A valid `diagnosticId` shall not be null or empty.**]**  
**SRS_DIAGNOSTICPROPERTYDATA_02_002: [**A valid `diagnosticCreationTimeUtc` shall not be null or empty.**]**  

### DiagnosticPropertyData

```java
public DiagnosticPropertyData(String diagnosticId, String diagnosticCreationTimeUtc);
```

**SRS_DIAGNOSTICPROPERTYDATA_01_001: [**The constructor shall save the message body.**]**

**SRS_DIAGNOSTICPROPERTYDATA_01_002: [**If the diagnosticId or diagnosticCreationTimeUtc is null, the constructor shall throw an IllegalArgumentException.**]**


### getDiagnosticId

```java
public String getDiagnosticId();
```

**SRS_DIAGNOSTICPROPERTYDATA_01_003: [**The function shall return the value of diagnostic id.**]**

### setDiagnosticId

```java
public void setDiagnosticId(String diagnosticId);
```

**SRS_DIAGNOSTICPROPERTYDATA_01_004: [**The constructor shall set the value of diagnostic id.**]**

### getDiagnosticCreationTimeUtc

```java
public String getDiagnosticCreationTimeUtc();
```

**SRS_DIAGNOSTICPROPERTYDATA_01_005: [**The function shall return the value of diagnostic creation time.**]**

### setDiagnosticCreationTimeUtc

```java
public void setDiagnosticCreationTimeUtc(String diagnosticCreationTimeUtc);
```

**SRS_DIAGNOSTICPROPERTYDATA_01_006: [**The constructor shall set the value of diagnostic creation time.**]**

### getCorrelationContext

```java
public String getCorrelationContext();
```

**SRS_DIAGNOSTICPROPERTYDATA_01_007: [**The function shall return concat string of all correlation contexts.**]**
