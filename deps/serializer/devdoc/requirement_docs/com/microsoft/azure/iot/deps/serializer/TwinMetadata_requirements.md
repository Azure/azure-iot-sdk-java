# TwinMetadata Requirements

## Overview

TwinMetadata is a representation of the device twin metadata.


## References

Session 7.3.1 of Azure IoT Hub - Device Twin.
(ISO8601)[http://www.iso.org/iso/iso8601]


## Exposed API

```java
public class TwinMetadata {

    @SerializedName("$lastUpdated")
    protected String lastUpdated;

    @SerializedName("$lastUpdatedVersion")
    protected Integer lastUpdatedVersion;

    public TwinMetadata(Integer version)
    public String GetLastUpdate();
    public Integer GetLastUpdateVersion();
}
```


### TwinMetadata

```java
    public TwinMetadata(Integer version)
```

**SRS_TWIN_METADATA_21_001: [**The constructor shall receive a `Integer` that represents the property version.**]**  
**SRS_TWIN_METADATA_21_002: [**The constructor shall store the version in lastUpdatedVersion.**]**  
**SRS_TWIN_METADATA_21_003: [**The constructor shall store the current data and time in the format UTC ISO8601 in lastUpdated.**]**  


### GetLastUpdate

```java
    public String GetLastUpdate();
```

**SRS_TWIN_METADATA_21_004: [**The GetLastUpdate shall return a string with the property last update stored in the lastUpdated.**]**  


### GetLastUpdateVersion

```java
    public Integer GetLastUpdateVersion();
```

**SRS_TWIN_METADATA_21_004: [**The GetLastUpdateVersion shall return an Integer with the property last update version stored in the lastUpdatedVersion.**]**  

