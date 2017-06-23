# ErrorMessageParser Requirements

## Overview

Representation of a single error message collection with a Json deserializer.

## References


## Exposed API

```java
public class ErrorMessageParser
{
    public static String bestErrorMessage(String fullErrorMessage);
}
```

### bestErrorMessage
```java
public static String bestErrorMessage(String fullErrorMessage);
```
**SRS_ERROR_MESSAGE_PARSER_21_001: [**The bestErrorMessage shall parse the fullErrorMessage as json with format {"Message":"ErrorCode:(error)","ExceptionMessage":"Tracking ID:(tracking id)-TimeStamp:(dateTime)"}.**]**  
**SRS_ERROR_MESSAGE_PARSER_21_002: [**If the bestErrorMessage failed to parse the fullErrorMessage as json, it shall return the fullErrorMessage as is.**]**  
**SRS_ERROR_MESSAGE_PARSER_21_003: [**If the fullErrorMessage contains inner Messages, the bestErrorMessage shall parse the inner message.**]**  
**SRS_ERROR_MESSAGE_PARSER_21_004: [**The bestErrorMessage shall use the most inner message as the root cause.**]**  
**SRS_ERROR_MESSAGE_PARSER_21_005: [**The bestErrorMessage shall return a String with the rootMessage and rootException.**]**  
**SRS_ERROR_MESSAGE_PARSER_21_006: [**If the fullErrorMessage do not have rootException, the bestErrorMessage shall return only the rootMessage.**]**  
**SRS_ERROR_MESSAGE_PARSER_21_007: [**If the inner message do not have rootException, the bestErrorMessage shall use the parent rootException.**]**  
**SRS_ERROR_MESSAGE_PARSER_21_008: [**If the fullErrorMessage is null or empty, the bestErrorMessage shall return an empty String.**]**  
