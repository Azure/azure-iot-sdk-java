# HttpsIotHubConnection Requirements

## Overview

An HTTPS connection between a device and an IoT Hub.

## References

## Exposed API

```java
public class HttpsIotHubConnection
{
    public HttpsIotHubConnection(DeviceClientConfig config);

    public ResponseMessage sendEvent(HttpsMessage msg) throws TransportException;
    public ResponseMessage sendHttpsMessage(HttpsMessage httpsMessage, HttpsMethod httpsMethod, String httpsPath) throws TransportException;

    public Message receiveMessage() throws TransportException;
    public void sendMessageResult(IotHubMessageResult result) throws TransportException;

    @Override
    public void addListener(IotHubListener listener) throws TransportException
}
```


### HttpsIotHubConnection

```java
public HttpsIotHubConnection(DeviceClientConfig config);
```

**SRS_HTTPSIOTHUBCONNECTION_11_001: [**The constructor shall save the client configuration.**]**


### sendEvent

```Java
public ResponseMessage sendEvent(HttpsMessage msg) throws IOException;
```

**SRS_HTTPSIOTHUBCONNECTION_11_002: [**The function shall send a request to the URL https://[iotHubHostname]/devices/[deviceId]/messages/events?api-version=2016-02-03.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_003: [**The function shall send a POST request.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_004: [**The function shall set the request body to the message body.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_005: [**The function shall write each message property as a request header.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_006: [**The function shall set the request read timeout to be the configuration parameter readTimeoutMillis.**]**

**SRS_HTTPSIOTHUBCONNECTION_25_040: [**The function shall set the IotHub SSL context by calling setSSLContext on the request.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_007: [**The function shall set the header field 'authorization' to be a valid SAS token generated from the configuration parameters.**]**  

**SRS_HTTPSIOTHUBCONNECTION_11_008: [**The function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/events'.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_009: [**The function shall set the header field 'content-type' to be the message content type.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_010: [**The function shall return a ResponseMessage with the status and payload.**]**

**SRS_HTTPSIOTHUBCONNECTION_34_059: [**If this config is using x509 authentication, this function shall retrieve its sslcontext from its x509 Authentication object.**]**

**SRS_HTTPSIOTHUBCONNECTION_34_067: [**If the response from the service is OK or OK_EMPTY, this function shall notify its listener that a message was sent with no exception.**]**

**SRS_HTTPSIOTHUBCONNECTION_34_068: [**If the response from the service not OK or OK_EMPTY, this function shall notify its listener that a message was with the mapped IotHubServiceException.**]**


### sendHttpsMessage

```Java
public ResponseMessage sendHttpsMessage(HttpsMessage httpsMessage, HttpsMethod httpsMethod, String httpsPath) throws IOException
```

**SRS_HTTPSIOTHUBCONNECTION_21_041: [**The function shall send a request to the URL https://[iotHubHostname]/devices/[deviceId]/[path]?api-version=2016-02-03.**]**

**SRS_HTTPSIOTHUBCONNECTION_21_042: [**The function shall send a `httpsMethod` request.**]**

**SRS_HTTPSIOTHUBCONNECTION_21_043: [**The function shall set the request body to the message body.**]**

**SRS_HTTPSIOTHUBCONNECTION_21_044: [**The function shall write each message property as a request header.**]**

**SRS_HTTPSIOTHUBCONNECTION_21_045: [**The function shall set the request read timeout to be the configuration parameter readTimeoutMillis.**]**

**SRS_HTTPSIOTHUBCONNECTION_21_046: [**The function shall set the IotHub SSL context by calling setSSLContext on the request.**]**

**SRS_HTTPSIOTHUBCONNECTION_21_047: [**The function shall set the header field 'authorization' to be a valid SAS token generated from the configuration parameters.**]**  

**SRS_HTTPSIOTHUBCONNECTION_21_048: [**The function shall set the header field 'iothub-to' to be '/devices/[deviceId]/[path]'.**]**

**SRS_HTTPSIOTHUBCONNECTION_21_049: [**The function shall set the header field 'content-type' to be the message content type.**]**

**SRS_HTTPSIOTHUBCONNECTION_21_050: [**The function shall return a ResponseMessage with the status and payload.**]**

**SRS_HTTPSIOTHUBCONNECTION_21_051: [**If the IoT Hub could not be reached, the function shall throw a ProtocolException.**]**

**SRS_HTTPSIOTHUBCONNECTION_34_056: [**This function shall retrieve a sas token from its config to use in the https request header.**]**

**SRS_HTTPSIOTHUBCONNECTION_34_060: [**If this config is using x509 authentication, this function shall retrieve its sslcontext from its x509 Authentication object.**]**


### receiveMessage

```java
public Message receiveMessage() throws IOException;
```

**SRS_HTTPSIOTHUBCONNECTION_11_013: [**The function shall send a request to the URL https://[iotHubHostname]/devices/[deviceId]/messages/devicebound?api-version=2016-02-03.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_014: [**The function shall send a GET request.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_015: [**The function shall set the request read timeout to be the configuration parameter readTimeoutMillis.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_016: [**The function shall set the header field 'authorization' to be a valid SAS token generated from the configuration parameters.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_017: [**The function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/devicebound'.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_018: [**The function shall set the header field 'iothub-messagelocktimeout' to be the configuration parameter messageLockTimeoutSecs.**]**

**SRS_HTTPSIOTHUBCONNECTION_25_041: [**The function shall set the IotHub SSL context by calling setSSLContext on the request.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_019: [**If a response with IoT Hub status code OK is received, the function shall return the IoT Hub message included in the response.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_020: [**If a response with IoT Hub status code OK is received, the function shall save the response header field 'etag'.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_021: [**If a response with IoT Hub status code OK is not received, the function shall return null.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_023: [**If the IoT Hub could not be reached, the function shall throw a ProtocolException.**]**

**SRS_HTTPSIOTHUBCONNECTION_34_057: [**This function shall retrieve a sas token from its config to use in the https request header.**]**

**SRS_HTTPSIOTHUBCONNECTION_34_061: [**If this config is using x509 authentication, this function shall retrieve its sslcontext from its x509 Authentication object.**]**


### sendMessageResult

```java
public void sendMessageResult(IotHubMessageResult result) throws IOException;
```

**SRS_HTTPSIOTHUBCONNECTION_11_024: [**If the result is COMPLETE, the function shall send a request to the URL https://[iotHubHostname]/devices/[deviceId]/messages/devicebound/[eTag]?api-version=2016-02-03.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_025: [**If the result is COMPLETE, the function shall send a DELETE request.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_026: [**If the result is COMPLETE, the function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/devicebound/[eTag]'.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_027: [**If the result is ABANDON, the function shall send a request to the URL https://[iotHubHostname]/devices/[deviceId]/messages/devicebound/[eTag]/abandon?api-version=2016-02-03.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_028: [**If the result is ABANDON, the function shall send a POST request.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_029: [**If the result is ABANDON, the function shall set the header field 'iothub-to' to be /devices/[deviceId]/messages/devicebound/[eTag]/abandon.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_030: [**If the result is REJECT, the function shall send a request to the URL https://[iotHubHostname]/devices/[deviceId]/messages/devicebound/[eTag]??reject=true&api-version=2016-02-03 (the query parameters can be in any order).**]**

**SRS_HTTPSIOTHUBCONNECTION_11_031: [**If the result is REJECT, the function shall send a DELETE request.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_032: [**If the result is REJECT, the function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/devicebound/[eTag]'.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_033: [**The function shall set the request read timeout to be the configuration parameter readTimeoutMillis.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_034: [**The function shall set the header field 'authorization' to be a valid SAS token generated from the configuration parameters.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_035: [**The function shall set the header field 'if-match' to be the e-tag saved when receiveMessage() was previously called.**]**

**SRS_HTTPSIOTHUBCONNECTION_25_042: [**The function shall set the IotHub SSL context by calling setSSLContext on the request.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_037: [**If the IoT Hub could not be reached, the function shall throw a ProtocolException.**]**

**SRS_HTTPSIOTHUBCONNECTION_11_038: [**If the IoT Hub status code in the response is not OK_EMPTY, the function shall throw an IllegalStateException.**]**

**SRS_HTTPSIOTHUBCONNECTION_34_069: [**If the IoT Hub status code in the response is OK_EMPTY or OK, the function shall remove the sent eTag from its map.]**

**SRS_HTTPSIOTHUBCONNECTION_11_039: [**If the function is called before receiveMessage() returns a message, the function shall throw an IllegalStateException.**]**

**SRS_HTTPSIOTHUBCONNECTION_34_062: [**If this config is using x509 authentication, this function shall retrieve its sslcontext from its x509 Authentication object.**]**


### addListener
```java
public void addListener(IotHubListener listener) throws TransportException
```

**SRS_HTTPSIOTHUBCONNECTION_34_065: [**If the provided listener object is null, this function shall throw an IllegalArgumentException.**]**

**SRS_HTTPSIOTHUBCONNECTION_34_066: [**This function shall save the provided listener object.**]**
