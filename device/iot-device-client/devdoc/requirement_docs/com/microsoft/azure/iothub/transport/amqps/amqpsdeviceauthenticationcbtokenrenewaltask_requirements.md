# AmqpsDeviceAuthenticationCBSSendTask Requirements

## Overview

Sends authentication messages. Meant to be used with an executor that continuously calls run().


## References

## Exposed API


```java
class AmqpsDeviceAuthenticationCBSTokenRenewalTask extends Runnable
{
    AmqpsDeviceAuthenticationCBSTokenRenewalTask(AmqpsSessionDeviceOperation amqpsSessionDeviceOperation);
    public void run();
```


### AmqpsDeviceAuthenticationCBSTokenRenewalTask

```java
AmqpsDeviceAuthenticationCBSTokenRenewalTask(AmqpsSessionDeviceOperation amqpsSessionDeviceOperation);
```

**SRS_AMQPSDEVICEAUTHENTICATIONCBSTOKENRENEWALTASK_12_001: [**The constructor shall throw IllegalArgumentException if the amqpsSessionDeviceOperation parameter is null.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBSTOKENRENEWALTASK_002: [**The constructor shall save the amqpsSessionDeviceOperation.**]**


### run

```java
public void run();
```

**SRS_AMQPSDEVICEAUTHENTICATIONCBSTOKENRENEWALTASK_12_003: [**The function shall call the amqpsSessionDeviceOperation.renewToken.**]**

