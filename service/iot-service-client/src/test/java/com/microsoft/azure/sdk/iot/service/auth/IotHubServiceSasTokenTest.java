/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.auth;

import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import mockit.Deencapsulation;
import mockit.Expectations;
import org.junit.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author zolvarga
 */
public class IotHubServiceSasTokenTest
{
    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_001: [The constructor shall throw IllegalArgumentException if the input object is null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_input_null()
    {
        // Arrange
        IotHubConnectionString iotHubConnectionString = null;
        // Act
        IotHubServiceSasToken iotHubServiceSasToken = new IotHubServiceSasToken(iotHubConnectionString);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_002: [The constructor shall create a target uri from the url encoded host name)]
    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_003: [The constructor shall create a string to sign by concatenating the target uri and the expiry time string]
    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_004: [The constructor shall create a key from the shared access key signing with HmacSHA256]
    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_005: [The constructor shall compute the final signature by url encoding the signed key]
    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_006: [The constructor shall concatenate the target uri, the signature, the expiry time and the key name using the format: "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s"]
    @Test
    public void constructor_good_case_flow_check() throws Exception
    {
        // Arrange
        String cryptoProvider = "HmacSHA256";
        String charset = "UTF-8";
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = encodeBase64String("1234567890abcdefghijklmnopqrstvwxyz=".getBytes(StandardCharsets.UTF_8));
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;

        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);

        // Assert
        new Expectations()
        {
            URLEncoder urlEncoder;
            System system;
            final SecretKeySpec secretKeySpec;
            Mac mac;
            {
                URLEncoder.encode(hostName.toLowerCase(),String.valueOf(StandardCharsets.UTF_8));
                System.currentTimeMillis();

                // Semmle flags this as sensitive call, but it is a false positive since it is for test purposes
                byte[] body = { 1 }; // lgtm

                secretKeySpec = new SecretKeySpec(body, cryptoProvider);
                Mac.getInstance(cryptoProvider);
            }
        };
        // Act
        IotHubServiceSasToken iotHubServiceSasToken = new IotHubServiceSasToken(iotHubConnectionString);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_002: [The constructor shall create a target uri from the url encoded host name)]
    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_003: [The constructor shall create a string to sign by concatenating the target uri and the expiry time string (one year)]
    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_004: [The constructor shall create a key from the shared access key signing with HmacSHA256]
    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_005: [The constructor shall compute the final signature by url encoding the signed key]
    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_006: [The constructor shall concatenate the target uri, the signature, the expiry time and the key name using the format: "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s"]
    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_007: [The function shall return with the generated token]
    @Test
    public void constructor_good_case_format_check() throws Exception
    {
        // Arrange
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = encodeBase64String("1234567890abcdefghijklmnopqrstvwxyz=".getBytes(StandardCharsets.UTF_8));
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);

        // Act
        IotHubServiceSasToken iotHubServiceSasToken = new IotHubServiceSasToken(iotHubConnectionString);
        String token = iotHubServiceSasToken.toString();

        // Assert
        assertTrue(token.contains("SharedAccessSignature sr=hostname.b.c.d&sig="));
        assertTrue(token.contains("&se="));
        assertTrue(token.contains("&skn=ACCESSKEYNAME"));
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_007: [The constructor shall throw Exception if building the token failed]
    // Assert
    @Test (expected = Exception.class)
    public void constructor_buil_token_failed() throws Exception
    {
        // Arrange
        String iotHubName = "b.c.d";
        String hostName = "HOSTNAME." + iotHubName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = "key";
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;

        // Act
        IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
        Deencapsulation.setField(iotHubConnectionString, "hostName", null);
        IotHubServiceSasToken iotHubServiceSasToken = new IotHubServiceSasToken(iotHubConnectionString);
    }
}
