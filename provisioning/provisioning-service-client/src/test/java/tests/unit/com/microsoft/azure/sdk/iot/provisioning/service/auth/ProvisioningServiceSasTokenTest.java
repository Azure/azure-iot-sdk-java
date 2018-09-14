/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.auth;

import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningConnectionString;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningSasToken;
import mockit.Deencapsulation;
import mockit.Expectations;
import com.microsoft.azure.sdk.iot.deps.util.Base64;
import org.junit.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for Provisioning Service SasToken
 * 100% methods, 100% lines covered
 */
public class ProvisioningServiceSasTokenTest
{
    // Tests_SRS_PROVISIONING_SERVICE_SASTOKEN_12_001: [The constructor shall throw IllegalArgumentException if the input object is null]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullConnectionString() throws IllegalArgumentException
    {
        // Arrange
        ProvisioningConnectionString provisioningConnectionString = null;
        // Act
        ProvisioningSasToken provisioningServiceSasToken = new ProvisioningSasToken(provisioningConnectionString);
    }

    // Tests_SRS_PROVISIONING_SERVICE_SASTOKEN_12_002: [The constructor shall create a target uri from the url encoded host name)]
    // Tests_SRS_PROVISIONING_SERVICE_SASTOKEN_12_003: [The constructor shall create a string to sign by concatenating the target uri and the expiry time string]
    // Tests_SRS_PROVISIONING_SERVICE_SASTOKEN_12_004: [The constructor shall create a key from the shared access key signing with HmacSHA256]
    // Tests_SRS_PROVISIONING_SERVICE_SASTOKEN_12_005: [The constructor shall compute the final signature by url encoding the signed key]
    // Tests_SRS_PROVISIONING_SERVICE_SASTOKEN_12_006: [The constructor shall concatenate the target uri, the signature, the expiry time and the key name using the format: "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s"]
    @Test
    public void constructorSucceeded() throws Exception
    {
        // Arrange
        String cryptoProvider = "HmacSHA256";
        String charset = "UTF-8";
        String deviceProvisioningServiceName = "b.c.d";
        String hostName = "HOSTNAME." + deviceProvisioningServiceName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = Base64.encodeBase64StringLocal("key".getBytes());
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;

        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);

        // Assert
        new Expectations()
        {
            URLEncoder urlEncoder;
            Base64 base64;
            System system;
            SecretKeySpec secretKeySpec;
            Mac mac;
            {
                urlEncoder.encode(hostName.toLowerCase(),String.valueOf(StandardCharsets.UTF_8));
                system.currentTimeMillis();
                Base64.decodeBase64Local(sharedAccessKey.getBytes(charset));
                byte[] body = { 1 };
                secretKeySpec = new SecretKeySpec(body, cryptoProvider);
                mac.getInstance(cryptoProvider);
            }
        };
        // Act
        ProvisioningSasToken provisioningServiceSasToken = new ProvisioningSasToken(provisioningConnectionString);
    }

    // Tests_SRS_PROVISIONING_SERVICE_SASTOKEN_12_002: [The constructor shall create a target uri from the url encoded host name)]
    // Tests_SRS_PROVISIONING_SERVICE_SASTOKEN_12_003: [The constructor shall create a string to sign by concatenating the target uri and the expiry time string (one year)]
    // Tests_SRS_PROVISIONING_SERVICE_SASTOKEN_12_004: [The constructor shall create a key from the shared access key signing with HmacSHA256]
    // Tests_SRS_PROVISIONING_SERVICE_SASTOKEN_12_005: [The constructor shall compute the final signature by url encoding the signed key]
    // Tests_SRS_PROVISIONING_SERVICE_SASTOKEN_12_006: [The constructor shall concatenate the target uri, the signature, the expiry time and the key name using the format: "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s"]
    // Tests_SRS_PROVISIONING_SERVICE_SASTOKEN_12_008: [The function shall return with the generated token]
    @Test
    public void constructorCheckFormatSucceeded() throws Exception
    {
        // Arrange
        String deviceProvisioningServiceName = "b.c.d";
        String hostName = "HOSTNAME." + deviceProvisioningServiceName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = Base64.encodeBase64StringLocal("key".getBytes());
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);

        // Act
        ProvisioningSasToken provisioningServiceSasToken = new ProvisioningSasToken(provisioningConnectionString);
        String token = provisioningServiceSasToken.toString();

        // Assert
        assertEquals(token.contains("SharedAccessSignature sr=hostname.b.c.d&sig="), true);
        assertEquals(token.contains("&se="), true);
        assertEquals(token.contains("&skn=ACCESSKEYNAME"), true);
    }

    // Tests_SRS_PROVISIONING_SERVICE_SASTOKEN_12_007: [The constructor shall throw Exception if building the token failed]
    @Test (expected = Exception.class)
    public void constructorThrowsOnBuildToken() throws Exception
    {
        // Arrange
        String deviceProvisioningServiceName = "b.c.d";
        String hostName = "HOSTNAME." + deviceProvisioningServiceName;
        String sharedAccessKeyName = "ACCESSKEYNAME";
        String policyName = "SharedAccessKey";
        String sharedAccessKey = Base64.encodeBase64StringLocal("key".getBytes());
        String connectionString = "HostName=" + hostName + ";SharedAccessKeyName=" + sharedAccessKeyName + ";" + policyName + "=" + sharedAccessKey;

        // Act
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);
        Deencapsulation.setField(provisioningConnectionString, "hostName", null);
        ProvisioningSasToken provisioningServiceSasToken = new ProvisioningSasToken(provisioningConnectionString);
    }
}
