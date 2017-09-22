# IotHubConnectionString Requirements

## Overview

Parser for the Iothub connection string.

## References

## Exposed API

```java
public class IotHubConnectionString
{
    public IotHubConnectionString(String connectionString) 
            throws URISyntaxException, IllegalArgumentException;

    public IotHubConnectionString(String hostName, String deviceId,
                                   String sharedAccessKey, String sharedAccessToken)
            throws URISyntaxException, IllegalArgumentException;

    public String getHostName();
    public String getHubName();
    public String getDeviceId();
    public String getSharedAccessKey();
    public String getSharedAccessToken();
    
    void setSharedAccessToken(String sharedAccessToken) throws IllegalArgumentException;
    
    public boolean isUsingX509();
}
```

## Validation
**SRS_IOTHUB_CONNECTIONSTRING_21_001: [**A valid `hostName` shall not be null or empty.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_002: [**A valid `hostName` shall be a valid URI.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_003: [**A valid `hostName` shall contain at least one `.`.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_004: [**A valid `deviceId` shall not be null or empty.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_005: [**A valid connectionString shall contain a `sharedAccessToken` or a `sharedAccessKey` unless using x509 Authentication.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_006: [**If provided, the `sharedAccessToken` shall not be null or empty.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_007: [**If provided, the `sharedAccessKey` shall not be null or empty.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_008: [**A valid connectionString shall not contain both `sharedAccessToken` and `sharedAccessKey` at the same time.**]**  


### IotHubConnectionString
```java
public IotHubConnectionString(String connectionString) 
        throws URISyntaxException, IllegalArgumentException;
```
**SRS_IOTHUB_CONNECTIONSTRING_21_010: [**The constructor shall interpret the connection string as a set of key-value pairs delimited by `;`, with keys and values separated by `=`.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_011: [**The constructor shall save the IoT Hub hostname as the value of `hostName` in the connection string.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_012: [**The constructor shall save the first part of the IoT Hub hostname as the value of `hubName`, hostname split by `.`.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_013: [**The constructor shall save the device ID as the UTF-8 URL-decoded value of `deviceId` in the connection string.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_014: [**The constructor shall save the device key as the value of `sharedAccessKey` in the connection string.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_015: [**The constructor shall save the shared access token as the value of `sharedAccessToken` in the connection string.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_016: [**If the connection string is null or empty, the constructor shall throw an IllegalArgumentException.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_017: [**If the connection string is not valid, the constructor shall throw an IllegalArgumentException.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_34_035: [**If the connection string contains an expired SAS Token, throw a SecurityException.**]**


### IotHubConnectionString
```java
public IotHubConnectionString(String hostName, String deviceId, 
                              String sharedAccessKey, String sharedAccessToken) 
        throws URISyntaxException, IllegalArgumentException;
```
**SRS_IOTHUB_CONNECTIONSTRING_21_020: [**The constructor shall save the IoT Hub hostname as the value of `hostName` in the connection string.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_021: [**The constructor shall save the first part of the IoT Hub hostname as the value of `hubName`, hostname split by `.`.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_022: [**The constructor shall save the device ID as the UTF-8 URL-decoded value of `deviceId` in the connection string.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_023: [**The constructor shall save the device key as the value of `sharedAccessKey` in the connection string.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_024: [**The constructor shall save the shared access token as the value of `sharedAccessToken` in the connection string.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_21_025: [**If the parameters for the connection string is not valid, the constructor shall throw an IllegalArgumentException.**]**  
**SRS_IOTHUB_CONNECTIONSTRING_34_036: [**If the SAS Token has expired, throw a SecurityException.**]**

### getHostName
```java
    public String getHostName()
```
**SRS_IOTHUB_CONNECTIONSTRING_21_030: [**The getHostName shall return the stored host name.**]**  

### getHubName
```java
    public String getHubName()
```
**SRS_IOTHUB_CONNECTIONSTRING_21_031: [**The getHubName shall return the stored hub name, which is the first part of the hostName.**]**  

### getDeviceId
```java
    public String getDeviceId()
```
**SRS_IOTHUB_CONNECTIONSTRING_21_032: [**The getDeviceId shall return the stored device id.**]**  

### getSharedAccessKey
```java
    public String getSharedAccessKey()
```
**SRS_IOTHUB_CONNECTIONSTRING_21_033: [**The getSharedAccessKey shall return the stored shared access key.**]**  

### getSharedAccessToken
```java
    public String getSharedAccessToken()
```
**SRS_IOTHUB_CONNECTIONSTRING_21_034: [**The getSharedAccessToken shall return the stored shared access token.**]**  


### setSharedAccessToken
```java
    void setSharedAccessToken(String sharedAccessToken) throws IllegalArgumentException;
```

**SRS_IOTHUB_CONNECTIONSTRING_34_037: [**If the provided shared access token is null or empty, an IllegalArgumentException shall be thrown.**]**

**SRS_IOTHUB_CONNECTIONSTRING_34_038: [**This function shall set the value of this object's shared access token to the provided value.**]**

### isUsingX509
```java
public boolean isUsingX509();
```

**SRS_IOTHUB_CONNECTIONSTRING_34_039: [**If the connection string passed in the constructor contains the string 'x509=true' then this function shall return true.**]**
