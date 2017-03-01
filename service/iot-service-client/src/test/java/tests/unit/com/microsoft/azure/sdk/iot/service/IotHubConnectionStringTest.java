/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JMockit.class)
public class IotHubConnectionStringTest
{
    private static final String URL_API_VERSION = "api-version=2016-02-03";

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_001: [The function shall serialize the object properties to a string using the following format: SharedAccessKeyName@sas.root.IotHubName]
    @Test
    public void getUserString_good_case() throws Exception
    {
        // Arrange
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        String expected = sharedAccessKeyName +  "@sas.root.HOSTNAME";
        // Act
        String actual = iotHubConnectionString.getUserString();
        // Assert
        assertEquals("UserString mismatch!", expected, actual);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_002: [The function shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlDevice_device_name_null() throws Exception
    {
        // Arrange
        String deviceId = null;
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        // Act
        URL urlDevice = iotHubConnectionString.getUrlDevice(deviceId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_002: [The function shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlDevice_device_name_empty() throws Exception
    {
        // Arrange
        String deviceId = "";
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        // Act
        URL urlDevice = iotHubConnectionString.getUrlDevice(deviceId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_003: [The function shall create a URL object from the given deviceId using the following format: https:hostname/devices/deviceId?api-version=201X-XX-XX]        StringBuilder stringBuilder = new StringBuilder();
    @Test
    public void getUrlDevice_good_case() throws Exception
    {
        // Arrange
        String deviceId = "xxx-device";
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        String expected = "https://HOSTNAME.b.c.d/devices/xxx-device?api-version=2016-11-14";
        // Act
        String actual = iotHubConnectionString.getUrlDevice(deviceId).toString();
        // Assert
        assertEquals("Device URL mismatch!", expected, actual);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_004: [The constructor shall throw NullPointerException if the input integer is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlDeviceList_maxCount_null() throws Exception
    {
        // Arrange
        Integer maxCount = null;
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        // Act
        URL urlDevice = iotHubConnectionString.getUrlDeviceList(maxCount);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_004: [The constructor shall throw NullPointerException if the input integer is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlDeviceList_maxCount_zero() throws Exception
    {
        // Arrange
        Integer maxCount = 0;
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        // Act
        URL urlDevice = iotHubConnectionString.getUrlDeviceList(maxCount);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_005: [The function shall create a URL object from the given integer using the following format: https:hostname/devices/?maxCount=XX&api-version=201X-XX-XX]
    @Test
    public void getUrlDeviceList_good_case() throws Exception
    {
        // Arrange
        Integer maxCount = 10;
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        String expected = "https://HOSTNAME.b.c.d/devices/?top=10&api-version=2016-11-14";
        // Act
        String actual = iotHubConnectionString.getUrlDeviceList(maxCount).toString();
        // Assert
        assertEquals("DeviceList URL mismatch!", expected, actual);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_006: [The function shall create a URL object from the object properties using the following format: https:hostname/statistics/devices?api-version=201X-XX-XX]
    @Test
    public void getUrlDeviceStatistics_good_case() throws Exception
    {
        // Arrange
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        String expected = "https://HOSTNAME.b.c.d/statistics/devices?api-version=2016-11-14";
        // Act
        String actual = iotHubConnectionString.getUrlDeviceStatistics().toString();
        // Assert
        assertEquals("Device Statistics mismatch!", expected, actual);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_007: [The function shall serialize the object to a string using the following format: HostName=HOSTNAME.b.c.d;SharedAccessKeyName=ACCESSKEYNAME;SharedAccessKey=1234567890abcdefghijklmnopqrstvwxyz=;SharedAccessSignature=]
    @Test
    public void toString_good_case() throws Exception
    {
        // Arrange
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        String expected = "HostName=HOSTNAME.b.c.d;SharedAccessKeyName=ACCESSKEYNAME;SharedAccessKey=1234567890abcdefghijklmnopqrstvwxyz=;SharedAccessSignature=";
        // Act
        String actual = iotHubConnectionString.toString();
        // Assert
        assertEquals("Serialization error!", expected, actual);
    }

    /*
    **Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_25_011: [** The function shall create a URL object from the given deviceId using the following format: https:hostname/twins/deviceId?api-version=201X-XX-XX **]**
     */
    @Test
    public void getUrlTwin_succeeds() throws Exception
    {
        //arrange
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        String deviceId = "testDevice";
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        String expected = "https://HOSTNAME.b.c.d/twins/testDevice?api-version=2016-11-14";

        //act
        String actual = iotHubConnectionString.getUrlTwin(deviceId).toString();

        //assert
        assertTrue(actual.equals(expected));

    }

    /*
    **Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_25_010: [** The function shall throw IllegalArgumentException if the input string is empty or null **]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void getUrlTwin_throwsOnEmptyDeviceID() throws Exception
    {
        //arrange
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        String deviceId = "";
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        //act
        String actual = iotHubConnectionString.getUrlTwin(deviceId).toString();

    }

    @Test (expected = IllegalArgumentException.class)
    public void getUrlTwin_throwsOnNullDeviceID() throws Exception
    {
        //arrange
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        String deviceId = null;
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        //act
        String actual = iotHubConnectionString.getUrlTwin(deviceId).toString();

    }

    /*
    **Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_25_013: [** The function shall create a URL object from the given deviceId using the following format: https:hostname/twins/deviceId/tags?api-version=201X-XX-XX **]**
     */
    @Test
    public void getUrlTwinTags_succeeds() throws Exception
    {
        //arrange
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        String deviceId = "testDevice";
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        String expected = "https://HOSTNAME.b.c.d/twins/testDevice/tags?api-version=2016-11-14";

        //act
        String actual = iotHubConnectionString.getUrlTwinTags(deviceId).toString();

        //assert
        assertTrue(actual.equals(expected));
    }

    /*
    **Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_25_012: [** The function shall throw IllegalArgumentException if the input string is empty or null **]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void getUrlTwinTags_throwsOnEmptyDeviceID() throws Exception
    {
        //arrange
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        String deviceId = "";
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        //act
        String actual = iotHubConnectionString.getUrlTwinTags(deviceId).toString();


    }

    @Test (expected = IllegalArgumentException.class)
    public void getUrlTwinTags_throwsOnNullDeviceID() throws Exception
    {
        //arrange
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        String deviceId = null;
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        //act
        String actual = iotHubConnectionString.getUrlTwinTags(deviceId).toString();

    }

    /*
    **Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_25_015: [** The function shall create a URL object from the given deviceId using the following format: https:hostname/twins/deviceId/properties/desired?api-version=201X-XX-XX **]**
     */
    @Test
    public void getUrlTwinDesired_succeeds() throws Exception
    {
        //arrange
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        String deviceId = "testDevice";
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        String expected = "https://HOSTNAME.b.c.d/twins/testDevice/properties/desired?api-version=2016-11-14";

        //act
        String actual = iotHubConnectionString.getUrlTwinDesired(deviceId).toString();

        //assert
        assertTrue(actual.equals(expected));

    }

    /*
    **Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_25_014: [** The function shall throw IllegalArgumentException if the input string is empty or null **]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void getUrlTwinDesired_throwsOnEmptyDeviceID() throws Exception
    {
        //arrange
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        String deviceId = "";
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        //act
        String actual = iotHubConnectionString.getUrlTwinDesired(deviceId).toString();


    }

    @Test (expected = IllegalArgumentException.class)
    public void getUrlTwinDesired_throwsOnNullDeviceID() throws Exception
    {
        //arrange
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        String deviceId = null;
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        //act
        String actual = iotHubConnectionString.getUrlTwinDesired(deviceId).toString();

    }




}
