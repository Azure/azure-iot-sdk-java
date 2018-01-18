# AmqpsProvisioningSaslHandler Requirements

## Overview

This class is am implementation of the SaslHandler interface. It handles sasl negotiation for the Provisioning service TPM authentication

## References

## Exposed API

```java
public class AmqpsProvisioningSaslHandler implements SaslHandler
{
    AmqpsProvisioningSaslHandler(String idScope, String registrationId, byte[] endorsementKey, byte[] storageRootKey, ResponseCallback responseCallback, Object authorizationCallbackContext);
    
    public String chooseSaslMechanism(String[] mechanisms);
    public byte[] buildInitPayload(String chosenMechanism);
    public byte[] handleChallenge(byte[] saslChallenge);
    public void handleOutcome(SaslOutcome outcome);
    
    void setSasToken(String sasToken);
}
```

### AmqpsProvisioningSaslHandler

```java
AmqpsProvisioningSaslHandler(String idScope, String registrationId, byte[] endorsementKey, byte[] storageRootKey, ResponseCallback responseCallback, Object authorizationCallbackContext);
```

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_001: [**This constructor shall save the provided idScope, registrationId, endorsementKey, storageRootKey, responseCallback and autorizationCallbackContext .**]**

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_002: [**If any of the arguments are null or empty other than the autorizationCallbackContext, this function shall throw an IllegalArgumentException.**]**


### chooseSaslMechanism
```java
public String chooseSaslMechanism(String[] mechanisms);
```

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_003: [**If this handler is not in the state where it is expecting to choose a sasl mechanism, this function shall throw in IllegalStateException.**]**

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_004: [**If the provided mechanisms array does not contain "TPM" then this function shall throw a SecurityException.**]**

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_005: [**This function shall return "TPM".**]**


### buildInitPayload
```java
public byte[] buildInitPayload(String chosenMechanism);
```

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_006: [**If this handler is not in the state where it is expecting to build the init payload, this function shall throw in IllegalStateException.**]**

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_007: [**This function shall return the init payload bytes in the format "control byte + scopeId + null byte + registration id + null byte + base64 decoded endorsement key".**]**


### handleChallenge
```java
public byte[] handleChallenge(byte[] saslChallenge);
```

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_009: [**If the provided saslChallenge is null, this function shall throw an IllegalArgumentException.**]**

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_010: [**If this object is waiting for the first challenge, this function shall validate that this challenge payload contains only a null byte and shall throw an IllegalStateException if it is not.**]**

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_011: [**If this object is waiting for the first challenge, this function shall return a payload in the format "control byte + base64 decoded storage root key".**]**

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_012: [**If this object is waiting for the second challenge, this function shall validate that this challenge payload contains a control byte with the mask 0x80 and shall throw an IllegalStateException if it is not.**]**

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_013: [**If this object is waiting for the second challenge, this function shall read the challenge in the format "control byte + nonce (first half)" and save the nonce portion.**]**

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_014: [**If this object is waiting for the second challenge, this function shall return a payload of one null byte.**]**

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_015: [**If this object is waiting for the third challenge, this function shall validate that this challenge payload contains a control byte with the mask 0xC1 and shall throw an IllegalStateException if it is not.**]**

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_016: [**If this object is waiting for the third challenge, this function shall read the challenge in the format "control byte + nonce (second half)" and save the nonce portion.**]**

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_017: [**If this object is waiting for the third challenge, this function shall put together the full nonce byte array and run the saved responseCallback with the nonce and DPS_REGISTRATION_RECEIVED.**]**

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_018: [**If this object is waiting for the third challenge, after running the saved responseCallback, this function shall wait for the sas token to be set before returning a payload in the format "control byte + sas token".**]**

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_019: [**If this object is waiting for the third challenge, and if the sas token is not provided within 3 minutes of waiting, this function shall throw a SecurityException.**]**

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_020: [**If this object is not waiting for a first, second or third challenge, this function shall throw an IllegalStateException.**]**


### handleOutcome
```java
public void handleOutcome(SaslOutcome outcome);
```

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_021: [**If this object is not waiting for the sasl outcome, this function shall throw an IllegalStateException.**]**

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_022: [**If the sasl outcome is not OK, this function shall throw a SecurityException.**]**


### setSasToken
```java
void setSasToken(String sasToken);
```

**SRS_AMQPSPROVISIONINGSASLHANDLER_34_008: [**This function shall save the provided sas token.**]**
