# ProvisioningStatus Requirements

## Overview

An enum to represent ProvisioningStatus state with the service

## References

## Exposed API

```java
public class ProvisioningStatus 
{    
    UNASSIGNED("unassigned"),
    ASSIGNING("assigning"),
    ASSIGNED("assigned"),
    FAILED("failed"),
    DISABLED("disabled");

    ProvisioningStatus(String status);
    public static ProvisioningStatus fromString(String type);
}
```

### ProvisioningStatus

```java
    
    ProvisioningStatus(String status);
```
**SRS_ProvisioningStatus_25_001: [** Constructor shall create an enum **]**

### fromString

```java
    public static ProvisioningStatus fromString(String status);
```

**SRS_ProvisioningStatus_25_002: [** This method shall return the enum corresponding to the `status`. **]**

**SRS_ProvisioningStatus_25_003: [** If none of the enum's match the `status` it shall return null. **]**