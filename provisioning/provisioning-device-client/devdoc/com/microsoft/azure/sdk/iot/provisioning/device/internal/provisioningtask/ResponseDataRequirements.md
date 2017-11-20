# ResponseData Requirements

## Overview

An object that holds ResponseData retrived from service contract

## References

## Exposed API

```java
public class ResponseData 
{    
    ResponseData();

    byte[] getResponseData()
    void setResponseData(byte[] responseData)

    ContractState getContractState()
    void setContractState(ContractState ContractState)
}
```

### ResponseData

```java
    
    ResponseData();
```
**SRS_ResponseData_25_001: [** Constructor shall create null responseData and set the ContractState to  `DPS_REGISTRATION_UNKNOWN`. **]**

### setResponseData

```java
    void setResponseData(byte[] responseData)
```

**SRS_ResponseData_25_002: [** This method shall save the value of `responseData`. **]**

### getResponseData

```java
    byte[] getResponseData()
```

**SRS_ResponseData_25_003: [** This method shall return the saved value of `responseData`. **]**

### setContractState

```java
    void setContractState(ContractState contractState)
```

**SRS_ResponseData_25_004: [** This method shall save the value of `contractState`. **]**

### getContractState

```java
    ContractState getContractState()
```

**SRS_ResponseData_25_005: [** This method shall return the saved value of `contractState`. **]**