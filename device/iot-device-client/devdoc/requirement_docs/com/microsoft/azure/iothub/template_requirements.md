# Template Requirements

## Overview

A template that demonstrates adding a new collection to a set.

## References

Exposed API

```java
public final class Template
{
    Template(Object tag) throws IllegalArgumentException;

    void open();
    void close();

    public Object getTemplateTestPrivate();
    public Set<?> getUnionSet();

    public void addToSet(Set<?> collection) throws IllegalArgumentException;
} 
```


### Template

```java
Template(Object tag) throws IllegalArgumentException;
```

**SRS_TEMPLATE_99_001: [**The constructor shall save the input parameters.**]**

**SRS_TEMPLATE_99_002: [**If the input parameter is null, the constructor shall throw an IllegalArgumentException.**]**

**SRS_TEMPLATE_99_003: [**The constructor shall create a new instance of the public and private objects.**]**


### open

```java
void open();
```

**SRS_TEMPLATE_99_004: [**The method shall create a new instance of the unionSet.**]**

**SRS_TEMPLATE_99_005: [**If open is already called then this method shall do nothing and return.**]**


### close

```java
void close();
```

**SRS_TEMPLATE_99_006: [**This method shall clear the unionSet and set all the members ready for garbage collection.**]**

**SRS_TEMPLATE_99_007: [**If close is already called then this method shall do nothing and return.**]**


### getTemplateTestPrivate

```java
public Object getTemplateTestPrivate();
```

**SRS_TEMPLATE_99_008: [**The method shall return the private member object.**]**

### getUnionSet

```java
public Set<?> getUnionSet();
```

**SRS_TEMPLATE_99_009: [**The method shall return the current instance of the union set.**]**


### addToSet

```java
public void addToSet(Set<?> collection) throws IllegalArgumentException;
```

**SRS_TEMPLATE_99_010: [**The method shall add the collection to the union set.**]**

**SRS_TEMPLATE_99_011: [**The method shall throw IllegalArgumentException if the collection to be added was either empty or null.**]**

**SRS_TEMPLATE_99_012: [**The method shall throw IllegalStateException if it is called before calling open.**]**