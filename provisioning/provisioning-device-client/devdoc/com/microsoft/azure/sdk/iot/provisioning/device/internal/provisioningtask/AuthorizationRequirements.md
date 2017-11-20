# Authorization Requirements

## Overview

An object that holds authorization info with the service

## References

## Exposed API

```java
public class Authorization 
{    
    Authorization();

    SSLContext getSslContext();
    void setSslContext(SSLContext sslContext);

    String getSasToken();
    void setSasToken(String sasToken);
}
```

### Authorization

```java
    
    Authorization();
```
**SRS_Authorization_25_001: [** Constructor shall create null SasToken and null SSL Context **]**

### setSslContext

```java
    void setSslContext(SSLContext sslContext);
```

**SRS_Authorization_25_002: [** This method shall save the value of SSLContext. **]**

### getSslContext

```java
    SSLContext getSslContext();
```

**SRS_Authorization_25_003: [** This method shall return the saved value of SSLContext. **]**

### setSasToken

```java
    void setSasToken(String sasToken);
```

**SRS_Authorization_25_004: [** This method shall save the value of sasToken. **]**

### getSasToken

```java
    String getSasToken();
```

**SRS_Authorization_25_005: [** This method shall return the saved value of sasToken. **]**