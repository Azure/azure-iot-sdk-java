# Twin Requirements

## Overview

TwinProperties is a representation of the device twin set of properties, which includes `Desired` and `Reported` properties.


## References

Session 7.3.1 of Azure IoT Hub - Device Twin.
 

## Exposed API

```java
public class TwinProperties
{
    public TwinProperty Desired = new TwinProperty();
    public TwinProperty Reported = new TwinProperty();

    public String toJson()
    public void fromJson(String json)
}
```


### Desired

```java
    public TwinProperty Desired;
```

**SRS_TWIN_PROPERTIES_21_001: [**The Desired shall store an instance of the TwinProperty for the Twin `Desired` properties.**]**  


### Reported

```java
    public TwinProperty Reported;
```

**SRS_TWIN_PROPERTIES_21_002: [**The Reported shall store an instance of the TwinProperty for the Twin `Reported` properties.**]**  

