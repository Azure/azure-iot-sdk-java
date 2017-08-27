# AmqpsSendReturnValue Requirements

## Overview

Class to provide placeholder for return value for send device operation. 

## References

## Exposed API

```java
class AmqpsSendReturnValue
{
    AmqpsSendReturnValue(boolean deliverySuccessful, int deliveryHash);
    boolean isDeliverySuccessful();
    int getDeliveryHash();
```

### AmqpsSendReturnValue

```java
AmqpsSendReturnValue(boolean deliverySuccessful, int deliveryHash);
```


**SRS_AMQPSSENDRETURNVALUE_12_001: [**The constructor shall initialize deliverySuccessful and deliveryHash private member variables with the given arguments.**]**


### isDeliverySuccessful

```java
boolean isDeliverySuccessful();
```

**SRS_AMQPSSENDRETURNVALUE_12_002: [**The function shall return the current value of deliverySuccessful private member.**]**


### getDeliveryHash

```java
int getDeliveryHash();
```

**SRS_AMQPSSENDRETURNVALUE_12_003: [**The function shall return the current value of deliveryHash private member.**]**

