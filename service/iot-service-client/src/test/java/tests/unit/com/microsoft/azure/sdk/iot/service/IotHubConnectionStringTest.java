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

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for Iot Hub Connection String class.
 * 64% methods, 94% lines covered
 */
@RunWith(JMockit.class)
public class IotHubConnectionStringTest
{
    private static final String URL_API_VERSION = "api-version=2016-02-03";

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_001: [The function shall serialize the object properties to a string using the following format: SharedAccessKeyName@sas.root.IotHubName]
    @Test
    public void getUserStringGoodCase() throws IOException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = sharedAccessKeyName +  "@sas.root.HOSTNAME";
        
        // act
        String actual = iotHubConnectionString.getUserString();
        
        // assert
        assertEquals("UserString mismatch!", expected, actual);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_002: [The function shall throw IllegalArgumentException if the input string is empty or null]
    // assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlDeviceDeviceNameNull() throws IOException, IllegalArgumentException
    {
        // arrange
        final String deviceId = null;
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        
        // act
        iotHubConnectionString.getUrlDevice(deviceId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_002: [The function shall throw IllegalArgumentException if the input string is empty or null]
    // assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlDeviceDeviceNameEmpty() throws IOException, IllegalArgumentException
    {
        // arrange
        final String deviceId = "";
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        
        // act
        iotHubConnectionString.getUrlDevice(deviceId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_003: [The function shall create a URL object from the given deviceId using the following format: https:hostname/devices/deviceId?api-version=201X-XX-XX]        StringBuilder stringBuilder = new StringBuilder();
    @Test
    public void getUrlDeviceGoodCase() throws IOException, IllegalArgumentException
    {
        // arrange
        final String deviceId = "xxx-device";
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "https://HOSTNAME.b.c.d/devices/xxx-device?api-version=2016-11-14";
        
        // act
        String actual = iotHubConnectionString.getUrlDevice(deviceId).toString();
        
        // assert
        assertEquals("Device URL mismatch!", expected, actual);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_004: [The constructor shall throw NullPointerException if the input integer is null]
    // assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlDeviceListMaxCountNull() throws IOException, IllegalArgumentException
    {
        // arrange
        Integer maxCount = null;
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        
        // act
        iotHubConnectionString.getUrlDeviceList(maxCount);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_004: [The constructor shall throw NullPointerException if the input integer is null]
    // assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlDeviceListMaxCountZero() throws IOException, IllegalArgumentException
    {
        // arrange
        Integer maxCount = 0;
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        
        // act
        iotHubConnectionString.getUrlDeviceList(maxCount);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_005: [The function shall create a URL object from the given integer using the following format: https:hostname/devices/?maxCount=XX&api-version=201X-XX-XX]
    @Test
    public void getUrlDeviceListGoodCase() throws IOException, IllegalArgumentException
    {
        // arrange
        Integer maxCount = 10;
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "https://HOSTNAME.b.c.d/devices/?top=10&api-version=2016-11-14";
        
        // act
        String actual = iotHubConnectionString.getUrlDeviceList(maxCount).toString();
        
        // assert
        assertEquals("DeviceList URL mismatch!", expected, actual);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_006: [The function shall create a URL object from the object properties using the following format: https:hostname/statistics/devices?api-version=201X-XX-XX]
    @Test
    public void getUrlDeviceStatisticsGoodCase() throws IOException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "https://HOSTNAME.b.c.d/statistics/devices?api-version=2016-11-14";
        
        // act
        String actual = iotHubConnectionString.getUrlDeviceStatistics().toString();
        
        // assert
        assertEquals("Device Statistics mismatch!", expected, actual);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_007: [The function shall serialize the object to a string using the following format: HostName=HOSTNAME.b.c.d;SharedAccessKeyName=ACCESSKEYNAME;SharedAccessKey=1234567890abcdefghijklmnopqrstvwxyz=;SharedAccessSignature=]
    @Test
    public void toStringGoodCase() throws IOException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "HostName=HOSTNAME.b.c.d;SharedAccessKeyName=ACCESSKEYNAME;SharedAccessKey=1234567890abcdefghijklmnopqrstvwxyz=;SharedAccessSignature=";
        
        // act
        String actual = iotHubConnectionString.toString();
        
        // assert
        assertEquals("Serialization error!", expected, actual);
    }

    /*
    **Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_25_011: [The function shall create a URL object from the given deviceId using the following format: https:hostname/twins/deviceId?api-version=201X-XX-XX ]
     */
    @Test
    public void getUrlTwinSucceeds() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final String deviceId = "testDevice";
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "https://HOSTNAME.b.c.d/twins/testDevice?api-version=2016-11-14";

        // act
        String actual = iotHubConnectionString.getUrlTwin(deviceId).toString();

        // assert
        assertTrue(actual.equals(expected));

    }

    /*
    **Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_25_010: [The function shall throw IllegalArgumentException if the input string is empty or null ]
     */
    @Test (expected = IllegalArgumentException.class)
    public void getUrlTwinThrowsOnEmptyDeviceID() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final String deviceId = "";
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlTwin(deviceId).toString();

    }

    @Test (expected = IllegalArgumentException.class)
    public void getUrlTwinThrowsOnNullDeviceID() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final String deviceId = null;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlTwin(deviceId).toString();

    }

    /*
    **Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_25_013: [The function shall create a URL object from the given deviceId using the following format: https:hostname/twins/deviceId/tags?api-version=201X-XX-XX ]
     */
    @Test
    public void getUrlTwinTagsSucceeds() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final String deviceId = "testDevice";
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "https://HOSTNAME.b.c.d/twins/testDevice/tags?api-version=2016-11-14";

        // act
        String actual = iotHubConnectionString.getUrlTwinTags(deviceId).toString();

        // assert
        assertTrue(actual.equals(expected));
    }

    /*
    **Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_25_012: [The function shall throw IllegalArgumentException if the input string is empty or null ]
     */
    @Test (expected = IllegalArgumentException.class)
    public void getUrlTwinTagsThrowsOnEmptyDeviceID() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final String deviceId = "";
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlTwinTags(deviceId).toString();


    }

    @Test (expected = IllegalArgumentException.class)
    public void getUrlTwinTagsThrowsOnNullDeviceID() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final String deviceId = null;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlTwinTags(deviceId).toString();

    }

    /*
    **Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_25_015: [The function shall create a URL object from the given deviceId using the following format: https:hostname/twins/deviceId/properties/desired?api-version=201X-XX-XX ]
     */
    @Test
    public void getUrlTwinDesiredSucceeds() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final String deviceId = "testDevice";
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "https://HOSTNAME.b.c.d/twins/testDevice/properties/desired?api-version=2016-11-14";

        // act
        String actual = iotHubConnectionString.getUrlTwinDesired(deviceId).toString();

        // assert
        assertTrue(actual.equals(expected));

    }

    /*
    **Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_25_014: [The function shall throw IllegalArgumentException if the input string is empty or null ]
     */
    @Test (expected = IllegalArgumentException.class)
    public void getUrlTwinDesiredThrowsOnEmptyDeviceID() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final String deviceId = "";
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlTwinDesired(deviceId).toString();
    }

    @Test (expected = IllegalArgumentException.class)
    public void getUrlTwinDesiredThrowsOnNullDeviceID() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final String deviceId = null;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlTwinDesired(deviceId).toString();
    }

    /*
    **Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_21_017: [The function shall create a URL object from the given deviceId using the following format: https:hostname/twins/deviceId/methods/ ]
     */
    @Test
    public void getUrlMethodSucceeds() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final String deviceId = "testDevice";
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "https://HOSTNAME.b.c.d/twins/testDevice/methods?api-version=2016-11-14";

        // act
        String actual = iotHubConnectionString.getUrlMethod(deviceId).toString();

        // assert
        assertTrue(actual.equals(expected));

    }

    /*
    **Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_21_016: [The function shall throw IllegalArgumentException if the input string is empty or null ]
     */
    @Test (expected = IllegalArgumentException.class)
    public void getUrlMethodThrowsOnEmptyDeviceID() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final String deviceId = "";
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlMethod(deviceId).toString();
    }

    @Test (expected = IllegalArgumentException.class)
    public void getUrlMethodThrowsOnNullDeviceID() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final String deviceId = null;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlMethod(deviceId).toString();
    }

    /*
    ** Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_21_019: [The function shall create a URL object from the given jobId using the following format: `https:hostname/jobs/v2/jobId?api-version=2016-11-14` ]
     */
    @Test
    public void getUrlJobsSucceeds() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final String jobId = "testJobId";
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "https://HOSTNAME.b.c.d/jobs/v2/testJobId?api-version=2016-11-14";

        // act
        String actual = iotHubConnectionString.getUrlJobs(jobId).toString();

        // assert
        assertTrue(actual.equals(expected));
    }

    /*
    ** Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_21_018: [The function shall throw IllegalArgumentException if the input string is empty or null ]
     */
    @Test (expected = IllegalArgumentException.class)
    public void getUrlJobsThrowsOnEmptyJobID() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final String jobId = "";
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlJobs(jobId).toString();
    }

    @Test (expected = IllegalArgumentException.class)
    public void getUrlJobsThrowsOnNullJobID() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final String jobId = null;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlJobs(jobId).toString();
    }

    /*
    ** Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_21_021: [The function shall create a URL object from the given jobId using the following format: `https:hostname/jobs/v2/jobId/cancel?api-version=2016-11-14` ]
     */
    @Test
    public void getUrlJobsCancelSucceeds() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final String jobId = "testJobId";
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "https://HOSTNAME.b.c.d/jobs/v2/testJobId/cancel?api-version=2016-11-14";

        // act
        String actual = iotHubConnectionString.getUrlJobsCancel(jobId).toString();

        // assert
        assertTrue(actual.equals(expected));
    }

    /*
    ** Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_21_020: [The function shall throw IllegalArgumentException if the input string is empty or null ]
     */
    @Test (expected = IllegalArgumentException.class)
    public void getUrlJobsCancelThrowsOnEmptyJobID() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final String jobId = "";
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlJobsCancel(jobId).toString();
    }

    @Test (expected = IllegalArgumentException.class)
    public void getUrlJobsCancelThrowsOnNullJobID() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final String jobId = null;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlJobsCancel(jobId).toString();
    }

    /*
    ** Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_21_022: [The function shall create a URL object from the given jobId using the following format: `https:[hostname]/jobs/v2/query?jobType=<>&jobStatus=<>&api-version=2016-11-14` ]
     */
    @Test
    public void getUrlQuerySucceeds() throws IOException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "https://HOSTNAME.b.c.d/jobs/v2/query?jobType=jobType&jobStatus=jobStatus&api-version=2016-11-14";
        final String jobType = "jobType";
        final String jobStatus = "jobStatus";

        // act
        String actual = iotHubConnectionString.getUrlQuery(jobType, jobStatus).toString();

        // assert
        assertTrue(actual.equals(expected));
    }

    //Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_25_023: [ The function shall create a URL object from the given jobId using the following format: https:[hostname]/devices/query?api-version=2016-11-14 ]
    @Test
    public void getUrlTwinQuerySucceeds() throws IOException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "https://HOSTNAME.b.c.d/devices/query?api-version=2016-11-14";

        // act
        String actual = iotHubConnectionString.getUrlTwinQuery().toString();

        // assert
        assertTrue(actual.equals(expected));
    }

    /*
    ** Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_15_008: [The function shall create a URL object from the object properties using the following format: https:hostname/jobs/create?api-version=201X-XX-XX.]
     */
    @Test
    public void getUrlCreateExportImportJobSucceeds() throws IOException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "https://HOSTNAME.b.c.d/jobs/create?api-version=2016-11-14";

        // act
        String actual = iotHubConnectionString.getUrlCreateExportImportJob().toString();

        // assert
        assertTrue(actual.equals(expected));
    }

    /*
    ** Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_15_009: [The function shall create a URL object from the object properties using the following format: https:hostname/jobs/jobId?api-version=201X-XX-XX.]
     */
    @Test
    public void getUrlImportExportJobSucceeds() throws IOException, IllegalArgumentException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String jobId = "testJobId";
        final String expected = "https://HOSTNAME.b.c.d/jobs/testJobId?api-version=2016-11-14";

        // act
        String actual = iotHubConnectionString.getUrlImportExportJob(jobId).toString();

        // assert
        assertTrue(actual.equals(expected));
    }
}
