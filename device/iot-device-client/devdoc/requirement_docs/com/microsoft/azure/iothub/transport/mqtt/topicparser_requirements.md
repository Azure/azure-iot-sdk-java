# TopicParser Requirements

## Overview

TopicParser parses Mqtt Topics to get version, request id or method name if present.

## References

## Exposed API

```java
public class TopicParser
{
    public TopicParser(String topic);

    protected String getStatus(int tokenIndexStatus) throws IOException;
    protected String getRequestId(int tokenIndexReqID) throws IOException;
    protected String getVersion(int tokenIndexVersion) throws IOException;
    protected String getMethodName(int tokenIndexMethod) throws IOException;

}
```

### TopicParser

```java
public TopicParser(String topic);
```

**SRS_TOPICPARSER_25_001: [**The constructor shall spilt the topic by "/" and save the tokens.**]**

**SRS_TOPICPARSER_25_002: [**The constructor shall throw TransportException if topic is null or empty.**]**



### getStatus

```java
protected String getStatus(int tokenIndexStatus) throws IOException;
```

**SRS_TOPICPARSER_25_003: [**If tokenIndexStatus is not valid i.e less than or equal to zero or greater then token length then getStatus shall throw TransportException.**]**

**SRS_TOPICPARSER_25_004: [**This method shall return the status corresponding to the tokenIndexStatus from tokens if it is not null.**]**

**SRS_TOPICPARSER_25_005: [**If token corresponding to tokenIndexStatus is null then this method shall throw TransportException.**]**


### getRequestId

```java
protected String getRequestId(int tokenIndexReqID) throws IOException;
```

**SRS_TOPICPARSER_25_006: [**If tokenIndexReqID is not valid i.e less than or equal to zero or greater then token length then getRequestId shall throw TransportException.**]**

**SRS_TOPICPARSER_25_007: [**This method shall return the request ID value corresponding to the tokenIndexReqID from tokens.**]**

**SRS_TOPICPARSER_25_008: [**If the topic token does not contain request id then this method shall return null.**]**

### getVersion

```java
protected String getVersion(int tokenIndexVersion) throws IOException;
```

**SRS_TOPICPARSER_25_009: [**If tokenIndexVersion is not valid i.e less than or equal to zero or greater then token length then getVersion shall throw TransportException.**]**

**SRS_TOPICPARSER_25_010: [**This method shall return the version value(if present) corresponding to the tokenIndexVersion from tokens.**]**

**SRS_TOPICPARSER_25_011: [**If the topic token does not contain version then this method shall return null.**]**

### getMethodName

```java
protected String getMethodName(int tokenIndexMethod) throws IOException;
```

**SRS_TOPICPARSER_25_012: [**If tokenIndexMethod is not valid i.e less than or equal to zero or greater then token length then getMethodName shall throw TransportException.**]**

**SRS_TOPICPARSER_25_013: [**This method shall return the method name(if present) corresponding to the tokenIndexMethod from tokens.**]**

**SRS_TOPICPARSER_25_014: [**If the topic token does not contain method name or is null then this method shall throw TransportException.**]**