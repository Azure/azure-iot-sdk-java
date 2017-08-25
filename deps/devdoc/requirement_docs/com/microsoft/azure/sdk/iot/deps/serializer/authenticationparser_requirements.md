# AuthenticationParser Requirements

## Overview

Representation of an Authentication object used for Json deserialization and serialization.

## References


## Exposed API

```java
public class AuthenticationParser
{
    public SymmetricKeyParser getSymmetricKey()

    public void setSymmetricKey(SymmetricKeyParser symmetricKey)
    public X509ThumbprintParser getThumbprint()
    public void setThumbprint(X509ThumbprintParser thumbprint)
    public AuthenticationTypeParser getType()
    public void setType(AuthenticationTypeParser type) throws IllegalArgumentException
}
```


### AuthenticationParser
```java
protected AuthenticationParser();
```
**SRS_AUTHENTICATION_PARSER_34_001: [**This Constructor shall create a new instance of an authenticationParser object and return it.**]**


### getType
```java
public AuthenticationTypeParser getType()
```
**SRS_AUTHENTICATION_PARSER_34_002: [**This method shall return the value of this object's authenticationTypeParser.**]**


### setType
```java
public void setType(AuthenticationTypeParser type)
````
**SRS_AUTHENTICATION_PARSER_34_008: [**This method shall set the value of this object's authentication type equal to the provided value.**]**
**SRS_AUTHENTICATION_PARSER_34_003: [**If the provided type is null, an IllegalArgumentException shall be thrown.**]**


### getThumbprint
```java
public X509ThumbprintParser getThumbprint()
```
**SRS_AUTHENTICATION_PARSER_34_004: [**This method shall return the value of this object's thumbprint.**]**


### setThumbprint
```java
public void setThumbprint(X509ThumbprintParser thumbprint)
```
**SRS_AUTHENTICATION_PARSER_34_005: [**This method shall set the value of this object's thumbprint equal to the provided value.**]**


### getSymmetricKey
```java
public SymmetricKeyParser getSymmetricKey()
```
**SRS_AUTHENTICATION_PARSER_34_006: [**This method shall return the value of this object's symmetricKey.**]**


### setSymmetricKey
```java
public void setSymmetricKey(SymmetricKeyParser symmetricKey)
```
**SRS_AUTHENTICATION_PARSER_34_007: [**This method shall set the value of symmetricKey equal to the provided value.**]**