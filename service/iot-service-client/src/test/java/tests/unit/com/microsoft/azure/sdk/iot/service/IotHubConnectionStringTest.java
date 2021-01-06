/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import mockit.Deencapsulation;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.MalformedURLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for Iot Hub Connection String class.
 * 64% methods, 94% lines covered
 */
@RunWith(JMockit.class)
public class IotHubConnectionStringTest
{
    private static final String URL_API_VERSION = Deencapsulation.getField(IotHubConnectionString.class, "URL_API_VERSION");

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_001: [The function shall serialize the object properties to a string using the following format: SharedAccessKeyName@SAS.root.IotHubName]
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
        final String expected = sharedAccessKeyName +  "@SAS.root.HOSTNAME";
        
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
        final String expected = "https://HOSTNAME.b.c.d/devices/xxx-device?"+URL_API_VERSION;
        
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
        final String expected = "https://HOSTNAME.b.c.d/devices/?top=10&" + URL_API_VERSION;
        
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
        final String expected = "https://HOSTNAME.b.c.d/statistics/devices?" + URL_API_VERSION;
        
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
        final String expected = "https://HOSTNAME.b.c.d/twins/testDevice?" + URL_API_VERSION;

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
        final String expected = "https://HOSTNAME.b.c.d/twins/testDevice/methods?" + URL_API_VERSION;

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
        final String expected = "https://HOSTNAME.b.c.d/jobs/v2/testJobId?" + URL_API_VERSION;

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
        final String expected = "https://HOSTNAME.b.c.d/jobs/v2/testJobId/cancel?" + URL_API_VERSION;

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
        final String expected = "https://HOSTNAME.b.c.d/jobs/v2/query?jobType=jobType&jobStatus=jobStatus&" + URL_API_VERSION;
        final String jobType = "jobType";
        final String jobStatus = "jobStatus";

        // act
        String actual = iotHubConnectionString.getUrlQuery(jobType, jobStatus).toString();

        // assert
        assertTrue(actual.equals(expected));
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_21_024: [** If the jobType is null or empty, the function shall not include the jobType in the URL **]**
    @Test
    public void getUrlQueryWithoutJobTypeSucceeds() throws IOException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "https://HOSTNAME.b.c.d/jobs/v2/query?jobStatus=jobStatus&" + URL_API_VERSION;
        final String jobType = null;
        final String jobStatus = "jobStatus";

        // act
        String actual = iotHubConnectionString.getUrlQuery(jobType, jobStatus).toString();

        // assert
        assertTrue(actual.equals(expected));
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_21_025: [** If the jobStatus is null or empty, the function shall not include the jobStatus in the URL **]**
    @Test
    public void getUrlQueryWithoutJobStatusSucceeds() throws IOException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "https://HOSTNAME.b.c.d/jobs/v2/query?jobType=jobType&" + URL_API_VERSION;
        final String jobType = "jobType";
        final String jobStatus = null;

        // act
        String actual = iotHubConnectionString.getUrlQuery(jobType, jobStatus).toString();

        // assert
        assertTrue(actual.equals(expected));
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_21_024: [** If the jobType is null or empty, the function shall not include the jobType in the URL **]**
    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_21_025: [** If the jobStatus is null or empty, the function shall not include the jobStatus in the URL **]**
    @Test
    public void getUrlQueryWithoutJobTypeAndJobStatusSucceeds() throws IOException
    {
        // arrange
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "https://HOSTNAME.b.c.d/jobs/v2/query?" + URL_API_VERSION;
        final String jobType = null;
        final String jobStatus = null;

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
        final String expected = "https://HOSTNAME.b.c.d/devices/query?" + URL_API_VERSION;

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
        final String expected = "https://HOSTNAME.b.c.d/jobs/create?" + URL_API_VERSION;

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
        final String expected = "https://HOSTNAME.b.c.d/jobs/testJobId?" + URL_API_VERSION;

        // act
        String actual = iotHubConnectionString.getUrlImportExportJob(jobId).toString();

        // assert
        assertTrue(actual.equals(expected));
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_28_001: [The function shall throw IllegalArgumentException if the deviceId string is empty or null]
    // assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlModuleDeviceNameNull() throws IOException, IllegalArgumentException
    {
        // arrange
        final String deviceId = null;
        final String moduleId = "somemodule";
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlModule(deviceId, moduleId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_28_001: [The function shall throw IllegalArgumentException if the deviceId string is empty or null]
    // assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlModuleDeviceNameEmpty() throws IOException, IllegalArgumentException
    {
        // arrange
        final String deviceId = "";
        final String moduleId = "somemodule";
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlModule(deviceId, moduleId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_28_002: [The function shall throw IllegalArgumentException if the moduleId string is empty or null]
    // assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlModuleModuleNameNull() throws IOException, IllegalArgumentException
    {
        // arrange
        final String deviceId = "somedevice";
        final String moduleId = null;
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlModule(deviceId, moduleId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_28_002: [The function shall throw IllegalArgumentException if the moduleId string is empty or null]
    // assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlModuleModuleNameEmpty() throws IOException, IllegalArgumentException
    {
        // arrange
        final String deviceId = "somedevice";
        final String moduleId = "";
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlModule(deviceId, moduleId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_003: [The function shall create a URL object from the given deviceId using the following format: https:hostname/devices/deviceId?api-version=201X-XX-XX]        StringBuilder stringBuilder = new StringBuilder();
    @Test
    public void getUrlModuleGoodCase() throws IOException, IllegalArgumentException
    {
        // arrange
        final String deviceId = "xxx-device";
        final String moduleId = "somemodule";
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "https://HOSTNAME.b.c.d/devices/xxx-device/modules/somemodule?"+URL_API_VERSION;

        // act
        String actual = iotHubConnectionString.getUrlModule(deviceId, moduleId).toString();

        // assert
        assertEquals("Device URL mismatch!", expected, actual);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_28_004: [The function shall throw IllegalArgumentException if the input string is empty or null]
    // assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlConfigurationConfigurationNameNull() throws IOException, IllegalArgumentException
    {
        // arrange
        final String configurationId = null;
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlConfiguration(configurationId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_28_004: [The function shall throw IllegalArgumentException if the input string is empty or null]
    // assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlConfigurationConfigurationNameEmpty() throws IOException, IllegalArgumentException
    {
        // arrange
        final String configurationId = "";
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlConfiguration(configurationId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_28_005: [The function shall create a URL object from the given configurationId
    // using the following format: https:hostname/configurations/configurationId?api-version=201X-XX-XX]
    @Test
    public void getUrlConfigurationGoodCase() throws IOException, IllegalArgumentException
    {
        // arrange
        final String configurationId = "xxx-config";
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "https://HOSTNAME.b.c.d/configurations/xxx-config?"+URL_API_VERSION;

        // act
        String actual = iotHubConnectionString.getUrlConfiguration(configurationId).toString();

        // assert
        assertEquals("Device URL mismatch!", expected, actual);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_28_006: [The constructor shall throw NullPointerException if the input integer is null]
    // assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlConfigurationListMaxCountNull() throws IOException, IllegalArgumentException
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
        iotHubConnectionString.getUrlConfigurationsList(maxCount);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_28_006: [The constructor shall throw NullPointerException if the input integer is 0]
    // assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlConfigurationListMaxCountZero() throws IOException, IllegalArgumentException
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
        iotHubConnectionString.getUrlConfigurationsList(maxCount);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_28_007: [The function shall create a URL object from the given
    // integer using the following format: https:hostname/configurations/?maxCount=XX&api-version=201X-XX-XX]
    @Test
    public void getUrlConfigurationListGoodCase() throws IOException, IllegalArgumentException
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
        final String expected = "https://HOSTNAME.b.c.d/configurations?top=10&" + URL_API_VERSION;

        // act
        String actual = iotHubConnectionString.getUrlConfigurationsList(maxCount).toString();

        // assert
        assertEquals("ConfigurationList URL mismatch!", expected, actual);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_28_008: [The function shall throw IllegalArgumentException if the moduleId string is empty or null]
    // assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlModuleOnDeviceDeviceNameNull() throws IOException, IllegalArgumentException
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
        iotHubConnectionString.getUrlModulesOnDevice(deviceId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_28_008: [The function shall throw IllegalArgumentException if the moduleId string is empty or null]
    // assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlModuleOnDeviceDeviceNameEmpty() throws IOException, IllegalArgumentException
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
        iotHubConnectionString.getUrlModulesOnDevice(deviceId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_28_009: [The function shall create a URL object from the given
    // deviceId using the following format: https:hostname/devices/deviceId/modules?api-version=201X-XX-XX]
    @Test
    public void getUrlModuleOnDeviceDeviceGoodCase() throws IOException, IllegalArgumentException
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
        final String expected = "https://HOSTNAME.b.c.d/devices/xxx-device/modules?"+URL_API_VERSION;

        // act
        String actual = iotHubConnectionString.getUrlModulesOnDevice(deviceId).toString();

        // assert
        assertEquals("Device URL mismatch!", expected, actual);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_34_026: [The function shall throw IllegalArgumentException
    // if the deviceId string is empty or null]
    @Test (expected = IllegalArgumentException.class)
    public void getUrlApplyConfigurationContentThrowsIfDeviceIdNull() throws MalformedURLException
    {
        //arrange
        String hostname = "somehostname.com";
        IotHubConnectionString connectionString = Deencapsulation.newInstance(IotHubConnectionString.class);
        Deencapsulation.setField(connectionString, "hostName", hostname);

        //act
        connectionString.getUrlApplyConfigurationContent(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_34_026: [The function shall return a URL in the format
    // "https:[hostname]/devices/[deviceId]/applyConfigurationContent?api-version=201X-XX-XX"]
    @Test
    public void getUrlApplyConfigurationContentSuccess() throws MalformedURLException
    {
        //arrange
        String hostname = "somehostname.com";
        String deviceId = "someDevice";
        String expectedUrl = "https://" + hostname + "/devices/" + deviceId + "/applyConfigurationContent?" + URL_API_VERSION;
        IotHubConnectionString connectionString = Deencapsulation.newInstance(IotHubConnectionString.class);
        Deencapsulation.setField(connectionString, "hostName", hostname);

        //act
        String actualUrl = connectionString.getUrlApplyConfigurationContent(deviceId).toString();

        //assert
        assertEquals(expectedUrl, actualUrl);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_28_010: [The function shall throw IllegalArgumentException if the deviceId string is empty or null]
    // assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlModuleTwinDeviceNameNull() throws IOException, IllegalArgumentException
    {
        // arrange
        final String deviceId = null;
        final String moduleId = "somemodule";
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlModuleTwin(deviceId, moduleId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_28_010: [The function shall throw IllegalArgumentException if the deviceId string is empty or null]
    // assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlModuleTwinDeviceNameEmpty() throws IOException, IllegalArgumentException
    {
        // arrange
        final String deviceId = "";
        final String moduleId = "somemodule";
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlModuleTwin(deviceId, moduleId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_28_011: [The function shall throw IllegalArgumentException if the moduleId string is empty or null]
    // assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlModuleTwinModuleNameNull() throws IOException, IllegalArgumentException
    {
        // arrange
        final String deviceId = "somedevice";
        final String moduleId = null;
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlModuleTwin(deviceId, moduleId);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_28_011: [The function shall throw IllegalArgumentException if the moduleId string is empty or null]
    // assert
    @Test (expected = IllegalArgumentException.class)
    public void getUrlModuleTwinModuleNameEmpty() throws IOException, IllegalArgumentException
    {
        // arrange
        final String deviceId = "somedevice";
        final String moduleId = "";
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        // act
        iotHubConnectionString.getUrlModuleTwin(deviceId, moduleId);
    }

   //Tests_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_28_012: [** The function shall create a URL object from the given deviceId and moduleId
   // using the following format: https:hostname/twins/deviceId/modules/moduleId?api-version=201X-XX-XX **]**
    @Test
    public void getUrlModuleTwinGoodCase() throws IOException, IllegalArgumentException
    {
        // arrange
        final String deviceId = "xxx-device";
        final String moduleId = "somemodule";
        final String iotHubName = "b.c.d";
        final String hostName = "HOSTNAME." + iotHubName;
        final String sharedAccessKeyName = "ACCESSKEYNAME";
        final String policyName = "SharedAccessKey";
        final String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        final String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);
        final String expected = "https://HOSTNAME.b.c.d/twins/xxx-device/modules/somemodule?"+URL_API_VERSION;

        // act
        String actual = iotHubConnectionString.getUrlModuleTwin(deviceId, moduleId).toString();

        // assert
        assertEquals("Device URL mismatch!", expected, actual);
    }
}
