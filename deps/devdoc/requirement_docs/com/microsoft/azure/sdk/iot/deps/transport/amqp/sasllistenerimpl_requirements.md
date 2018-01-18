# SaslListenerImpl Requirements

## Overview

This class in an implementation of the Proton-j interface for a SaslListener object. This implementation defers logic
for building payloads in init messages and challenge response messages to a SaslHandler object. It also defers the logic for
deciding what mechanism to choose to the SaslHandler object.

## References

## Exposed API

```java
public class SaslListenerImpl implements SaslListener
{
    public SaslListenerImpl(SaslHandler saslHandler);
    public void onSaslMechanisms(Sasl sasl, Transport transport);
    public void onSaslChallenge(Sasl sasl, Transport transport);
    public void onSaslOutcome(Sasl sasl, Transport transport);
    public void onSaslResponse(Sasl sasl, Transport transport);
    public void onSaslInit(Sasl sasl, Transport transport);
}
```

### SaslListenerImpl

```java
public SaslListenerImpl(SaslHandler saslHandler);
```

**SRS_SASLLISTENERIMPL_34_001: [**This constructor shall save the provided handler.**]**


### onSaslMechanisms

```java
public void onSaslMechanisms(Sasl sasl, Transport transport);
```

**SRS_SASLLISTENERIMPL_34_002: [**This function shall retrieve the remote mechanisms and give them to the saved saslHandler object to decide which mechanism to use.**]**

**SRS_SASLLISTENERIMPL_34_003: [**This function shall ask the saved saslHandler object to create the init payload for the chosen sasl mechanism and then send that payload.**]**

**SRS_SASLLISTENERIMPL_34_012: [**If any exception is thrown while the saslHandler handles the sasl mechanisms, this function shall save that exception and shall not create and send the init payload.**]**

**SRS_SASLLISTENERIMPL_34_013: [**If any exception is thrown while the saslHandler builds the init payload, this function shall save that exception and shall not send the returned init payload.**]**


### onSaslChallenge

```java
public void onSaslChallenge(Sasl sasl, Transport transport);
```

**SRS_SASLLISTENERIMPL_34_004: [**This function shall retrieve the sasl challenge from the provided sasl object.**]**

**SRS_SASLLISTENERIMPL_34_005: [**This function shall give the sasl challenge bytes to the saved saslHandler and send the payload it returns.**]**

**SRS_SASLLISTENERIMPL_34_014: [**If any exception is thrown while the saslHandler handles the challenge, this function shall save that exception and shall not send the challenge response.**]**

### onSaslOutcome

```java
public void onSaslOutcome(Sasl sasl, Transport transport);
```

**SRS_SASLLISTENERIMPL_34_006: [**If the sasl outcome is PN_SASL_TEMP, this function shall tell the saved saslHandler to handleOutcome with SYS_TEMP.**]**

**SRS_SASLLISTENERIMPL_34_007: [**If the sasl outcome is PN_SASL_PERM, this function shall tell the saved saslHandler to handleOutcome with SYS_PERM.**]**

**SRS_SASLLISTENERIMPL_34_008: [**If the sasl outcome is PN_SASL_AUTH, this function shall tell the saved saslHandler to handleOutcome with AUTH.**]**

**SRS_SASLLISTENERIMPL_34_009: [**If the sasl outcome is PN_SASL_OK, this function shall tell the saved saslHandler to handleOutcome with OK.**]**

**SRS_SASLLISTENERIMPL_34_010: [**If the sasl outcome is PN_SASL_SYS or PN_SASL_SKIPPED, this function shall tell the saved saslHandler to handleOutcome with SYS.**]**

**SRS_SASLLISTENERIMPL_34_011: [**If the sasl outcome is PN_SASL_NONE, this function shall save an IllegalStateException.**]**


### onSaslResponse

```java
public void onSaslResponse(Sasl sasl, Transport transport);
```


### onSaslInit

```java
public void onSaslInit(Sasl sasl, Transport transport);
```