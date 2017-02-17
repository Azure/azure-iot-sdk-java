# CustomLogger Requirements

## Overview

A custom logger to create logs. This class provides logging feature with Info, Debug, Trace, Warn, Error and Fatal levels.

## References

## Exposed API

```java
public final class CustomLogger
{
    private static final int CALLING_METHOD_NAME_DEPTH = 2;

    public CustomLogger(Class<?> clazz);

    public void LogInfo(String message, Object...params);
    public void LogDebug(String message, Object...params);
    public void LogTrace(String message, Object...params);
    public void LogWarn(String message, Object...params);
    public void LogFatal(String message, Object...params);
    public void LogError(String message, Object...params);
    public void LogError(Throwable exception);
    public String getMethodName();
}
```


### CustomLogger

```java
public CustomLogger(Class<?> clazz);
```

**SRS_CUSTOMLOGGER_25_001: [**The constructor shall create a logger object for a class passed as argument.**]**


### LogInfo

```java
public void LogInfo(String message, Object...params);
```

**SRS_CUSTOMLOGGER_25_002: [**The function shall record the message and arguments.**]**

**SRS_CUSTOMLOGGER_25_003: [**If INFO level is not enabled, message will not be recorded.**]**

**SRS_CUSTOMLOGGER_25_004: [**The function shall format the message before recording.**]**


### LogDebug

```java
public void LogDebug(String message, Object...params);
```

**SRS_CUSTOMLOGGER_25_005: [**The function shall record the message and arguments.**]**

**SRS_CUSTOMLOGGER_25_006: [**If DEBUG level is not enabled, message will not be recorded.**]**

**SRS_CUSTOMLOGGER_25_007: [**The function shall format the message before recording.**]**


### LogTrace

```java
public void LogTrace(String message, Object...params);
```

**SRS_CUSTOMLOGGER_25_008: [**The function shall record the message and arguments.**]**

**SRS_CUSTOMLOGGER_25_009: [**If TRACE level is not enabled, message will not be recorded.**]**

**SRS_CUSTOMLOGGER_25_010: [**The function shall format the message before recording.**]**


### LogWarn

```java
public void LogWarn(String message, Object...params);
```

**SRS_CUSTOMLOGGER_25_011: [**The function shall record the message and arguments.**]**

**SRS_CUSTOMLOGGER_25_012: [**If WARN level is not enabled, message will not be recorded.**]**

**SRS_CUSTOMLOGGER_25_013: [**The function shall format the message before recording.**]**


### LogFatal

```java
public void LogFatal(String message, Object...params);
```

**SRS_CUSTOMLOGGER_25_014: [**The function shall record the message and arguments.**]**

**SRS_CUSTOMLOGGER_25_015: [**If FATAL level is not enabled, message will not be recorded.**]**

**SRS_CUSTOMLOGGER_25_016: [**The function shall format the message before recording.**]**


### LogError

```java
public void LogError(String message, Object...params);
```

**SRS_CUSTOMLOGGER_25_017: [**The function shall record the message and arguments.**]**

**SRS_CUSTOMLOGGER_25_018: [**If ERROR level is not enabled, message will not be recorded.**]**

**SRS_CUSTOMLOGGER_25_019: [**The function shall format the message before recording.**]**


### getMethodName

```java
public String getMethodName();
```

**SRS_CUSTOMLOGGER_25_020: [**The function shall return name of the executing method.**]**
