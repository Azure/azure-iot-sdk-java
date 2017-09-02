# Base64 Requirements

## Overview

Encode and decode sequence of characters using Base64 format.

## References

The base64 encoding scheme is described in RFC2045 [Internet Message Bodies](https://www.ietf.org/rfc/rfc2045.txt)

## Exposed API

```java
public final class Base64
{
    public static byte[] decodeBase64Local(final byte[] base64Values) throws IllegalArgumentException;

    public static byte[] encodeBase64Local(byte[] dataValues) throws IllegalArgumentException;
    public static String encodeBase64StringLocal(byte[] dataValues) throws IllegalArgumentException, UnsupportedEncodingException;
}
```

### decodeBase64Local
```java
public static byte[] decodeBase64Local(final byte[] base64Values) throws IllegalArgumentException;
```
**SRS_BASE64_21_001: [**The decodeBase64Local shall decode the provided `base64Values` in a byte array using the Base64 format define in the RFC2045.**]**  
**SRS_BASE64_21_002: [**If the `base64Values` is null, the decodeBase64Local shall throw IllegalArgumentException.**]**  
**SRS_BASE64_21_003: [**If the `base64Values` is empty, the decodeBase64Local shall return a empty byte array.**]**  
**SRS_BASE64_21_004: [**If the `base64Values` length is not multiple of 4, the decodeBase64Local shall throw IllegalArgumentException.**]**  

### encodeBase64Local
```java
public static byte[] encodeBase64Local(byte[] dataValues) throws IllegalArgumentException;
```
**SRS_BASE64_21_005: [**The encodeBase64Local shall encoded the provided `dataValues` in a byte array using the Base64 format define in the RFC2045.**]**  
**SRS_BASE64_21_006: [**If the `dataValues` is null, the encodeBase64Local shall throw IllegalArgumentException.**]**  
**SRS_BASE64_21_007: [**If the `dataValues` is empty, the encodeBase64Local shall return a empty byte array.**]**  

### encodeBase64StringLocal
```java
public static String encodeBase64StringLocal(byte[] dataValues) throws IllegalArgumentException, UnsupportedEncodingException;
```
**SRS_BASE64_21_008: [**The encodeBase64StringLocal shall encoded the provided `dataValues` in a string using the Base64 format define in the RFC2045.**]**  
**SRS_BASE64_21_009: [**If the `dataValues` is null, the encodeBase64StringLocal shall throw IllegalArgumentException.**]**  
**SRS_BASE64_21_010: [**If the `dataValues` is empty, the encodeBase64StringLocal shall return a empty string.**]**  
