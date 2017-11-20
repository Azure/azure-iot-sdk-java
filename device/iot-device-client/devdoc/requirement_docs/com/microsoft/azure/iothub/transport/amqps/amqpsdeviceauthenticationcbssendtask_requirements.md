# AmqpsDeviceAuthenticationCBSSendTask Requirements

## Overview

Sends authentication messages. Meant to be used with an executor that continuously calls run().


## References

## Exposed API


```java
class AmqpsDeviceAuthenticationCBSSendTask extends Runnable
{
    AmqpsDeviceAuthenticationCBSSendTask(AmqpsDeviceAuthenticationCBS amqpsDeviceAuthenticationCBS);
    public void run();
```


### AmqpsDeviceAuthentication

```java
AmqpsDeviceAuthenticationCBSSendTask(AmqpsDeviceAuthenticationCBS amqpsDeviceAuthenticationCBS);
```

**SRS_AMQPSDEVICEAUTHENTICATIONCBSSENDTASK_12_001: [**The constructor shall throw IllegalArgumentException if the amqpsDeviceAuthenticationCBS parameter is null.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBSSENDTASK_12_002: [**The constructor shall save the amqpsDeviceAuthenticationCBS.**]**


### run

```java
public void run();
```

**SRS_AMQPSDEVICEAUTHENTICATIONCBSSENDTASK_12_003: [**The function shall call the amqpsDeviceAuthenticationCBS.sendAuthenticationMessages.**]**

