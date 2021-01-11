/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.auth;

import com.microsoft.azure.sdk.iot.provisioning.service.auth.AuthenticationMethod;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningConnectionString;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningConnectionStringBuilder;
import mockit.Deencapsulation;
import mockit.Expectations;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit test for Provisioning Connection String Builder
 * 100% methods, 100% lines covered
 */
public class ProvisioningConnectionStringBuilderTest
{
    private static final String HOST_NAME_PROPERTY_NAME = "HostName";
    private static final String SHARED_ACCESS_KEY_NAME_PROPERTY_NAME = "SharedAccessKeyName";
    private static final String SHARED_ACCESS_KEY_PROPERTY_NAME = "SharedAccessKey";
    private static final String SHARED_ACCESS_SIGNATURE_PROPERTY_NAME = "SharedAccessSignature";

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_001: [The function shall throw IllegalArgumentException if the input string is empty or null.] */
    @Test (expected = IllegalArgumentException.class)
    public void createConnectionStringThrowsOnNullConnectionString() throws Exception
    {
        // arrange
        String connectionString = null;
        
        // act
        ProvisioningConnectionStringBuilder.createConnectionString(connectionString);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_001: [The function shall throw IllegalArgumentException if the input string is empty or null.] */
    @Test (expected = IllegalArgumentException.class)
    public void createConnectionStringThrowsOnEmpyConnectionString() throws Exception
    {
        // arrange
        String connectionString = "";

        // act
        ProvisioningConnectionStringBuilder.createConnectionString(connectionString);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_002: [The function shall create a new ProvisioningConnectionString object deserializing the given string.] */
    @Test
    public void createConnectionStringDeserializerSucceeded() throws Exception
    {
        // arrange
        String deviceProvisioningServiceName = "PROVISIONINGNAME";
        String iotHubHostName = "HOSTNAME";
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + deviceProvisioningServiceName + "." + iotHubHostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        String userString = sharedAccessKeyName +  "@SAS.root." + deviceProvisioningServiceName;

        // act
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);

        // assert
        assertEquals("Parser error: HostName mismatch!", deviceProvisioningServiceName + "." + iotHubHostName, provisioningConnectionString.getHostName());
        assertEquals("Parser error: SharedAccessKeyName mismatch!", sharedAccessKeyName, provisioningConnectionString.getSharedAccessKeyName());
        assertEquals("Parser error: SharedAccessKey mismatch!", provisioningConnectionString.getSharedAccessKey(), sharedAccessKey);
        assertEquals("Parser error: SharedAccessSignature mismatch!", "", provisioningConnectionString.getSharedAccessSignature());
        assertEquals("Parser error: DeviceProvisioningServiceName mismatch!", deviceProvisioningServiceName, provisioningConnectionString.getDeviceProvisioningServiceName());
        assertEquals("Parser error: UserString mismatch!", userString, provisioningConnectionString.getUserString());
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_003: [The function shall throw IllegalArgumentException if the input string is empty or null.] */
    @Test (expected = IllegalArgumentException.class)
    public void createConnectionStringThrowsOnNullHostName() throws Exception
    {
        // arrange
        AuthenticationMethod authenticationMethod = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyKey",
                "myPolicy", "<key>");

        // act
        ProvisioningConnectionStringBuilder.createConnectionString(null, authenticationMethod);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_003: [The function shall throw IllegalArgumentException if the input string is empty or null.] */
    @Test (expected = IllegalArgumentException.class)
    public void createConnectionStringThrowsOnEmptyHostName() throws Exception
    {
        // arrange
        AuthenticationMethod authenticationMethod = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyKey",
                "myPolicy", "<key>");

        // act
        ProvisioningConnectionStringBuilder.createConnectionString("", authenticationMethod);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_004: [The function shall throw IllegalArgumentException if the input authenticationMethod is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void createConnectionStringThrowsOnNullAuthenticationMethod() throws Exception
    {
        // arrange
        AuthenticationMethod authenticationMethod = null;

        // act
        ProvisioningConnectionStringBuilder.createConnectionString("test", authenticationMethod);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_005: [The function shall create a new ProvisioningConnectionString object using the given hostname and authenticationMethod.] */
    @Test
    public void createConnectionStringWithPolicyKeySucceeded() throws Exception
    {
        // arrange
        AuthenticationMethod auth = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyKey",
                "myPolicy", "<key>");

        // act
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString("hostname", auth);

        // assert
        String connString = provisioningConnectionString.toString();
        assertEquals("Connection string mismatch!", "HostName=hostname;SharedAccessKeyName=myPolicy;SharedAccessKey=<key>;SharedAccessSignature=null", connString);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_005: [The function shall create a new ProvisioningConnectionString object using the given hostname and authenticationMethod.] */
    @Test
    public void createConnectionStringWithPolicyTokenSucceeded() throws Exception
    {
        // arrange
        AuthenticationMethod auth = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyToken",
                "myPolicy", "<token>");

        // act
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString("hostname", auth);

        // assert
        String connString = provisioningConnectionString.toString();
        assertEquals("Connection string mismatch!", "HostName=hostname;SharedAccessKeyName=myPolicy;SharedAccessKey=null;SharedAccessSignature=<token>", connString);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_006: [The function shall throw IllegalArgumentException if the input string is empty or null.] */
    @Test (expected = IllegalArgumentException.class)
    public void parserThrowsOnNullConnectionString() throws Exception
    {
        // arrange
        String deviceProvisioningServiceName = "PROVISIONINGNAME";
        String iotHubHostName = "HOSTNAME";
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + deviceProvisioningServiceName + "." + iotHubHostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);

        // act
        Deencapsulation.invoke(provisioningConnectionString, "parse", String.class, provisioningConnectionString);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_006: [The function shall throw IllegalArgumentException if the input string is empty or null.] */
    @Test (expected = IllegalArgumentException.class)
    public void parserThrowsOnEmptyConnectionString() throws Exception
    {
        // arrange
        String deviceProvisioningServiceName = "PROVISIONINGNAME";
        String iotHubHostName = "HOSTNAME";
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + deviceProvisioningServiceName + "." + iotHubHostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);

        // act
        Deencapsulation.invoke(provisioningConnectionString, "parse", "" , provisioningConnectionString);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_007: [The function shall throw IllegalArgumentException if the input target itoHubConnectionString is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void parserThrowsOnNullProvisioningConnectionString() throws Exception
    {
        // arrange
        String deviceProvisioningServiceName = "PROVISIONINGNAME";
        String iotHubHostName = "HOSTNAME";
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + deviceProvisioningServiceName + "." + iotHubHostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);

        // act
        Deencapsulation.invoke(provisioningConnectionString, "parse", connectionString, ProvisioningConnectionString.class);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_008: [The function shall throw exception if tokenizing or parsing failed.] */
    @Test (expected = Exception.class)
    public void parserThrowsOnInvalidHostName() throws Exception
    {
        // arrange
        String deviceProvisioningServiceName = "b.c.d";
        String hostName = "@" + deviceProvisioningServiceName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;

        // act
        ProvisioningConnectionStringBuilder.createConnectionString(connectionString);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_008: [The function shall throw exception if tokenizing or parsing failed.] */
    @Test (expected = Exception.class)
    public void parserThrowsOnKeyMissing() throws Exception
    {
        // arrange
        String connectionString = "a=A;b=B;HostName=";

        // act
        ProvisioningConnectionStringBuilder.createConnectionString(connectionString);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_008: [The function shall throw exception if tokenizing or parsing failed.] */
    @Test (expected = Exception.class)
    public void parserThrowsOnMissingDeviceProvisioningServiceName() throws Exception
    {
        // arrange
        String deviceProvisioningServiceName = "";
        String hostName = "" + deviceProvisioningServiceName;
        String sharedAccessKeyName = "XXX";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String sharedAccessSignature = "";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey + "SharedAccessSignature=" + sharedAccessSignature;

        // act
        ProvisioningConnectionStringBuilder.createConnectionString(connectionString);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_009: [The function shall tokenize and parse the given connection string and fill up the target ProvisioningConnectionString object with proper values.] */
    @Test
    public void parserSimpleHostNameSucceeded() throws Exception
    {
        // arrange
        String deviceProvisioningServiceName = "";
        String hostName = "HOSTNAME" + deviceProvisioningServiceName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        String userString = sharedAccessKeyName +  "@SAS.root." + deviceProvisioningServiceName;

        // act
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);

        // assert
        assertEquals("Parser error: HostName mismatch!", hostName, provisioningConnectionString.getHostName());
        assertEquals("Parser error: SharedAccessKeyName mismatch!", sharedAccessKeyName, provisioningConnectionString.getSharedAccessKeyName());
        assertEquals("Parser error: SharedAccessKey mismatch!", provisioningConnectionString.getSharedAccessKey(), sharedAccessKey);
        assertEquals("Parser error: SharedAccessSignature mismatch!", "", provisioningConnectionString.getSharedAccessSignature());
        assertEquals("Parser error: DeviceProvisioningServiceName mismatch!", deviceProvisioningServiceName, provisioningConnectionString.getDeviceProvisioningServiceName());
        assertEquals("Parser error: UserString mismatch!", userString, provisioningConnectionString.getUserString());
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_009: [The function shall tokenize and parse the given connection string and fill up the target ProvisioningConnectionString object with proper values.] */
    @Test
    public void parserSucceeded() throws Exception
    {
        // arrange
        String deviceProvisioningServiceName = "PROVISIONINGNAME";
        String iotHubHostName = "HOSTNAME";
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + deviceProvisioningServiceName + "." + iotHubHostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        String userString = sharedAccessKeyName +  "@SAS.root." + deviceProvisioningServiceName;
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);

        // act
        Deencapsulation.invoke(provisioningConnectionString, "parse", connectionString, provisioningConnectionString);

        // assert
        assertEquals("Parser error: HostName mismatch!", deviceProvisioningServiceName + "." + iotHubHostName, provisioningConnectionString.getHostName());
        assertEquals("Parser error: SharedAccessKeyName mismatch!", sharedAccessKeyName, provisioningConnectionString.getSharedAccessKeyName());
        assertEquals("Parser error: SharedAccessKey mismatch!", provisioningConnectionString.getSharedAccessKey(), sharedAccessKey);
        assertEquals("Parser error: SharedAccessSignature mismatch!", "", provisioningConnectionString.getSharedAccessSignature());
        assertEquals("Parser error: DeviceProvisioningServiceName mismatch!", deviceProvisioningServiceName, provisioningConnectionString.getDeviceProvisioningServiceName());
        assertEquals("Parser error: UserString mismatch!", userString, provisioningConnectionString.getUserString());
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_010: [The function shall create a new ServiceAuthenticationWithSharedAccessPolicyToken and set the authenticationMethod if sharedAccessKey is not defined.] */
    @Test
    public void parserWithSharedAccessKeyNotDefinedSucceeded() throws Exception
    {
        // arrange
        String deviceProvisioningServiceName = "b.c.d";
        String hostName = "HOSTNAME." + deviceProvisioningServiceName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessSignature";
        String sharedAccessSignature = "1234567890abcdefghijklmnopqrstvwxyz";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessSignature;

        // assert
        new Expectations()
        {
            {
                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyToken", policyName, sharedAccessSignature);
            }
        };

        // act
        ProvisioningConnectionStringBuilder.createConnectionString(connectionString);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_011: [The function shall create a new ServiceAuthenticationWithSharedAccessPolicyKey and set the authenticationMethod if the sharedAccessSignature is not defined.] */
    @Test
    public void parserWithSharedAccessSignatureNotDefinedSucceeded() throws Exception
    {
        // arrange
        String deviceProvisioningServiceName = "b.c.d";
        String hostName = "HOSTNAME." + deviceProvisioningServiceName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;

        // assert
        new Expectations()
        {
            {
                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyKey", policyName, sharedAccessKey);
            }
        };

        // act
        ProvisioningConnectionStringBuilder.createConnectionString(connectionString);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_012: [The function shall validate the connection string object.] */
    @Test
    public void parserValidateConnectionStringSucceeded() throws Exception
    {
        // arrange
        String deviceProvisioningServiceName = "b.c.d";
        String hostName = "HOSTNAME." + deviceProvisioningServiceName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);

        // assert
        new Expectations()
        {
            {
                Deencapsulation.invoke(provisioningConnectionString, "validate", provisioningConnectionString);
            }
        };

        // act
        Deencapsulation.invoke(provisioningConnectionString, "parse", connectionString, provisioningConnectionString);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_013: [The function shall return the substring of the host name until the first `.` character.] */
    @Test
    public void parserDeviceProvisioningServiceNameWithOneSeparatorSucceeded() throws Exception
    {
        // arrange
        String deviceProvisioningServiceName = "PROVISIONINGNAME";
        String iotHubHostName = "HOSTNAME";
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + deviceProvisioningServiceName + "." + iotHubHostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        String userString = sharedAccessKeyName +  "@SAS.root." + deviceProvisioningServiceName;
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);

        // act
        String actual = Deencapsulation.invoke(provisioningConnectionString, "parseDeviceProvisioningServiceName", provisioningConnectionString);

        // assert
        assertEquals("Parser error: DeviceProvisioningServiceName mismatch!", deviceProvisioningServiceName, actual);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_013: [The function shall return the substring of the host name until the first `.` character.] */
    @Test
    public void parserDeviceProvisioningServiceNameWithMultipleSeparatorSucceeded() throws Exception
    {
        // arrange
        String deviceProvisioningServiceName = "PROVISIONINGNAME";
        String iotHubHostName = "HOSTNAME.POSTFIX1.POSTFIX2";
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + deviceProvisioningServiceName + "." + iotHubHostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        String userString = sharedAccessKeyName +  "@SAS.root." + deviceProvisioningServiceName;
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);

        // act
        String actual = Deencapsulation.invoke(provisioningConnectionString, "parseDeviceProvisioningServiceName", provisioningConnectionString);

        // assert
        assertEquals("Parser error: DeviceProvisioningServiceName mismatch!", deviceProvisioningServiceName, actual);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_014: [The function shall return empty string if `.` character was not found.] */
    @Test
    public void parserDeviceProvisioningServiceNameWithoutSeparatorSucceeded() throws Exception
    {
        // arrange
        String deviceProvisioningServiceName = "PROVISIONINGNAME";
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + deviceProvisioningServiceName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);
        String expected = "";

        // act
        String actual = Deencapsulation.invoke(provisioningConnectionString, "parseDeviceProvisioningServiceName", provisioningConnectionString);

        // assert
        assertEquals("Parser error: DeviceProvisioningServiceName mismatch!", expected, actual);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_015: [The function shall throw IllegalArgumentException if the sharedAccessKeyName of the input itoHubConnectionString is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateThrowsOnEmptySharedAccessKeyName() throws Exception
    {
        // arrange
        ProvisioningConnectionString provisioningConnectionString = Deencapsulation.newInstance(ProvisioningConnectionString.class);
        Deencapsulation.setField(provisioningConnectionString, "hostName", "PROVISIONINGNAME.azure.net");
        Deencapsulation.setField(provisioningConnectionString, "sharedAccessKeyName", "");
        Deencapsulation.setField(provisioningConnectionString, "sharedAccessKey", "SharedAccessKey");
        Deencapsulation.setField(provisioningConnectionString, "sharedAccessSignature", "1234567890abcdefghijklmnopqrstvwxyz=");
        Deencapsulation.setField(provisioningConnectionString, "deviceProvisioningServiceName", "PROVISIONINGNAME");

        // act
        Deencapsulation.invoke(provisioningConnectionString, "validate", provisioningConnectionString);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_016: [The function shall throw IllegalArgumentException if either of the sharedAccessKey or the sharedAccessSignature of the input itoHubConnectionString is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateThrowsOnEmptySharedAccessKeyAndSignature() throws Exception
    {
        // arrange
        ProvisioningConnectionString provisioningConnectionString = Deencapsulation.newInstance(ProvisioningConnectionString.class);
        Deencapsulation.setField(provisioningConnectionString, "hostName", "PROVISIONINGNAME.azure.net");
        Deencapsulation.setField(provisioningConnectionString, "sharedAccessKeyName", "ACCESSKEYNAME");
        Deencapsulation.setField(provisioningConnectionString, "sharedAccessKey", "");
        Deencapsulation.setField(provisioningConnectionString, "sharedAccessSignature", "");
        Deencapsulation.setField(provisioningConnectionString, "deviceProvisioningServiceName", "PROVISIONINGNAME");

        // act
        Deencapsulation.invoke(provisioningConnectionString, "validate", provisioningConnectionString);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_017: [The function shall call property validation functions for hostname, sharedAccessKeyName, sharedAccessKey, sharedAccessSignature.] */
    @Test
    public void validateValidatesFormatsSucceeded() throws Exception
    {
        // arrange
        String deviceProvisioningServiceName = "b.c.d";
        String hostName = "HOSTNAME." + deviceProvisioningServiceName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);

        // assert
        new Expectations()
        {
            {
                Deencapsulation.invoke(provisioningConnectionString, "validateFormat", anyString, HOST_NAME_PROPERTY_NAME, anyString);
                Deencapsulation.invoke(provisioningConnectionString, "validateFormatIfSpecified", anyString, SHARED_ACCESS_KEY_NAME_PROPERTY_NAME, anyString);
                Deencapsulation.invoke(provisioningConnectionString, "validateFormatIfSpecified", anyString, SHARED_ACCESS_KEY_PROPERTY_NAME, anyString);
                Deencapsulation.invoke(provisioningConnectionString, "validateFormatIfSpecified", anyString, SHARED_ACCESS_SIGNATURE_PROPERTY_NAME, anyString);
            }
        };

        // act
        Deencapsulation.invoke(provisioningConnectionString, "validate", provisioningConnectionString);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_018: [The function shall validate the property value against the given regex.] */
    @Test
    public void validateFormatSucceeded() throws Exception
    {
        // arrange
        String regex = "[a-zA-Z0-9_\\-\\.]+$";
        String deviceProvisioningServiceName = "b.c.d";
        String hostName = "HOSTNAME." + deviceProvisioningServiceName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);

        // act - assert
        Deencapsulation.invoke(provisioningConnectionString, "validateFormat", hostName, "hostName", regex);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_019: [The function shall throw IllegalArgumentException if the value did not match with the pattern.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateFormatThrowsIfNotMatch() throws Exception
    {
        // arrange
        String regex = "[a-zA-Z0-9_\\-\\.]+$";
        String deviceProvisioningServiceName = "b.c.d";
        String hostName = "HOSTNAME." + deviceProvisioningServiceName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);

        // act - assert
        Deencapsulation.invoke(provisioningConnectionString, "validateFormat", "+++", "hostName", regex);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_020: [The function shall validate the property value against the given regex if the value is not null or empty.] */
    @Test
    public void validateFormatIfSpecifiedSucceeded() throws Exception
    {
        // arrange
        String regex = "[a-zA-Z0-9_\\-\\.]+$";
        String deviceProvisioningServiceName = "b.c.d";
        String hostName = "HOSTNAME." + deviceProvisioningServiceName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);

        // assert
        new Expectations()
        {
            {
                Deencapsulation.invoke(provisioningConnectionString, "validateFormat", anyString, HOST_NAME_PROPERTY_NAME, anyString);
            }
        };

        // act
        Deencapsulation.invoke(provisioningConnectionString, "validateFormatIfSpecified", hostName, "hostName", regex);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_020: [The function shall validate the property value against the given regex if the value is not null or empty.] */
    @Test
    public void validateFormatIfSpecifiedWithEmptyValueSucceeded() throws Exception
    {
        // arrange
        String regex = "[a-zA-Z0-9_\\-\\.]+$";
        String deviceProvisioningServiceName = "b.c.d";
        String hostName = "HOSTNAME." + deviceProvisioningServiceName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);

        // act - assert
        Deencapsulation.invoke(provisioningConnectionString, "validateFormatIfSpecified", "", "hostName", regex);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_021: [The function shall validate the given hostName.] */
    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_022: [The function shall parse and set the hostname to the given target provisioningConnectionString object.] */
    @Test
    public void setHostNameSucceeded() throws Exception
    {
        // arrange
        String regex = "[a-zA-Z0-9_\\-\\.]+$";
        String deviceProvisioningServiceName = "b.c.d";
        String hostName = "HOSTNAME." + deviceProvisioningServiceName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);
        new Expectations()
        {
            {
                Deencapsulation.invoke(provisioningConnectionString, "validateFormat", hostName, HOST_NAME_PROPERTY_NAME, regex);
            }
        };

        // act
        Deencapsulation.invoke(provisioningConnectionString, "setHostName", hostName, provisioningConnectionString);

        // assert
        String actualHostName = Deencapsulation.getField(provisioningConnectionString, "hostName");
        assertEquals(hostName, actualHostName);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_023: [The function shall populate and set the authenticationMethod on the given target provisioningConnectionString object.] */
    @Test
    public void setAuthenticationMethodWithKeySucceeded() throws Exception
    {
        // arrange
        String regex = "[a-zA-Z0-9_\\-\\.]+$";
        String deviceProvisioningServiceName = "b.c.d";
        String hostName = "HOSTNAME." + deviceProvisioningServiceName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);
        String newPolicyName = "XXX";
        String newPolicyKey = "YYY";
        AuthenticationMethod auth = Deencapsulation.newInstance(
                "com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyKey", newPolicyName, newPolicyKey);
        new Expectations()
        {
            {
                Deencapsulation.invoke(provisioningConnectionString, "validateFormat", hostName, HOST_NAME_PROPERTY_NAME, regex);
            }
        };

        // act
        Deencapsulation.invoke(provisioningConnectionString, "setAuthenticationMethod", auth, provisioningConnectionString);

        // assert
        assertEquals(newPolicyName, provisioningConnectionString.getSharedAccessKeyName());
        assertEquals(newPolicyKey, provisioningConnectionString.getSharedAccessKey());
        assertNull(provisioningConnectionString.getSharedAccessSignature());
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_BUILDER_21_023: [The function shall populate and set the authenticationMethod on the given target provisioningConnectionString object.] */
    @Test
    public void setAuthenticationMethodWithTokenSucceeded() throws Exception
    {
        // arrange
        String regex = "[a-zA-Z0-9_\\-\\.]+$";
        String deviceProvisioningServiceName = "b.c.d";
        String hostName = "HOSTNAME." + deviceProvisioningServiceName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "1234567890abcdefghijklmnopqrstvwxyz=";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);
        String newPolicyName = "XXX";
        String newPolicyKey = "YYY";
        AuthenticationMethod auth = Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.provisioning.service.auth.ServiceAuthenticationWithSharedAccessPolicyToken", newPolicyName, newPolicyKey);
        new Expectations()
        {
            {
                Deencapsulation.invoke(provisioningConnectionString, "validateFormat", hostName, HOST_NAME_PROPERTY_NAME, regex);
            }
        };

        // act
        Deencapsulation.invoke(provisioningConnectionString, "setAuthenticationMethod", auth, provisioningConnectionString);

        // assert
        assertEquals(auth, provisioningConnectionString.getAuthenticationMethod());
        assertEquals(newPolicyName, provisioningConnectionString.getSharedAccessKeyName());
        assertEquals(newPolicyKey, provisioningConnectionString.getSharedAccessSignature());
        assertNull(provisioningConnectionString.getSharedAccessKey());
    }
}
