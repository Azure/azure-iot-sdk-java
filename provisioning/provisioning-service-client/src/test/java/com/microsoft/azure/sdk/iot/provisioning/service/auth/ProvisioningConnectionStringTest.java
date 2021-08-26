/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.auth;

import com.microsoft.azure.sdk.iot.provisioning.service.auth.AuthenticationMethod;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningConnectionString;
import mockit.Deencapsulation;
import mockit.Mocked;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for Provisioning Connection String
 * 100% methods, 100% lines covered
 */
public class ProvisioningConnectionStringTest
{
    @Mocked
    AuthenticationMethod mockedAuthenticationMethod;

    private static final String VALID_HOST_NAME_PROPERTY_NAME = "validdeviceprovisioningservicename.azure.net";
    private static final String VALID_SHARED_ACCESS_KEY_NAME_PROPERTY_NAME = "SharedAccessKeyName";
    private static final String VALID_SHARED_ACCESS_KEY_PROPERTY_NAME = "SharedAccessKey";
    private static final String VALID_SHARED_ACCESS_SIGNATURE_PROPERTY_NAME = "SharedAccessSignature";
    private static final String VALID_DEVICE_PROVISIONING_SERVICE_NAME = "validdeviceprovisioningservicename";

    ProvisioningConnectionString buildProvisioningConnectionString()
    {
        ProvisioningConnectionString provisioningConnectionString = Deencapsulation.newInstance(ProvisioningConnectionString.class);

        Deencapsulation.setField(provisioningConnectionString, "hostName", VALID_HOST_NAME_PROPERTY_NAME);
        Deencapsulation.setField(provisioningConnectionString, "sharedAccessKeyName", VALID_SHARED_ACCESS_KEY_NAME_PROPERTY_NAME);
        Deencapsulation.setField(provisioningConnectionString, "sharedAccessKey", VALID_SHARED_ACCESS_KEY_PROPERTY_NAME);
        Deencapsulation.setField(provisioningConnectionString, "sharedAccessSignature", VALID_SHARED_ACCESS_SIGNATURE_PROPERTY_NAME);
        Deencapsulation.setField(provisioningConnectionString, "deviceProvisioningServiceName", VALID_DEVICE_PROVISIONING_SERVICE_NAME);
        Deencapsulation.setField(provisioningConnectionString, "authenticationMethod", mockedAuthenticationMethod);

        return provisioningConnectionString;
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_21_001: [The getUserString shall serialize the object properties to a string using the following format: SharedAccessKeyName@SAS.root.deviceProvisioningServiceName] */
    @Test
    public void getUserStringSucceeded()
    {
        // arrange
        ProvisioningConnectionString provisioningConnectionString = buildProvisioningConnectionString();

        // act
        String result = provisioningConnectionString.getUserString();

        // assert
        assertEquals(VALID_SHARED_ACCESS_KEY_NAME_PROPERTY_NAME + "@SAS.root." + VALID_DEVICE_PROVISIONING_SERVICE_NAME, result);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_21_002: [The toString shall serialize the object to a string using the following format: HostName=HOSTNAME.b.c.d;SharedAccessKeyName=ACCESSKEYNAME;SharedAccessKey=1234567890abcdefghijklmnopqrstvwxyz=;SharedAccessSignature=] */
    @Test
    public void toStringSucceeded()
    {
        // arrange
        ProvisioningConnectionString provisioningConnectionString = buildProvisioningConnectionString();

        // act
        String result = provisioningConnectionString.toString();

        // assert
        assertEquals(
                "HostName=" + VALID_HOST_NAME_PROPERTY_NAME +
                ";SharedAccessKeyName=" + VALID_SHARED_ACCESS_KEY_NAME_PROPERTY_NAME +
                ";SharedAccessKey=" + VALID_SHARED_ACCESS_KEY_PROPERTY_NAME +
                ";SharedAccessSignature=" + VALID_SHARED_ACCESS_SIGNATURE_PROPERTY_NAME, result);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_21_003: [The getDeviceProvisioningServiceName shall return the stored deviceProvisioningServiceName.] */
    @Test
    public void getDeviceProvisioningServiceNameSucceeded()
    {
        // arrange
        ProvisioningConnectionString provisioningConnectionString = buildProvisioningConnectionString();

        // act
        String result = provisioningConnectionString.getDeviceProvisioningServiceName();

        // assert
        assertEquals(VALID_DEVICE_PROVISIONING_SERVICE_NAME, result);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_21_004: [The getAuthenticationMethod shall return the stored authenticationMethod.] */
    @Test
    public void getAuthenticationMethodSucceeded()
    {
        // arrange
        ProvisioningConnectionString provisioningConnectionString = buildProvisioningConnectionString();

        // act
        AuthenticationMethod result = provisioningConnectionString.getAuthenticationMethod();

        // assert
        assertEquals(mockedAuthenticationMethod, result);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_21_005: [The getSharedAccessKeyName shall return the stored sharedAccessKeyName.] */
    @Test
    public void getSharedAccessKeyNameSucceeded()
    {
        // arrange
        ProvisioningConnectionString provisioningConnectionString = buildProvisioningConnectionString();

        // act
        String result = provisioningConnectionString.getSharedAccessKeyName();

        // assert
        assertEquals(VALID_SHARED_ACCESS_KEY_NAME_PROPERTY_NAME, result);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_21_006: [The getSharedAccessKey shall return the stored sharedAccessKey.] */
    @Test
    public void getSharedAccessKeySucceeded()
    {
        // arrange
        ProvisioningConnectionString provisioningConnectionString = buildProvisioningConnectionString();

        // act
        String result = provisioningConnectionString.getSharedAccessKey();

        // assert
        assertEquals(VALID_SHARED_ACCESS_KEY_PROPERTY_NAME, result);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_21_007: [The getSharedAccessSignature shall return the stored sharedAccessSignature.] */
    @Test
    public void getSharedAccessSignatureSucceeded()
    {
        // arrange
        ProvisioningConnectionString provisioningConnectionString = buildProvisioningConnectionString();

        // act
        String result = provisioningConnectionString.getSharedAccessSignature();

        // assert
        assertEquals(VALID_SHARED_ACCESS_SIGNATURE_PROPERTY_NAME, result);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_21_008: [The getHostName shall return the stored hostName.] */
    @Test
    public void getHostNameSucceeded()
    {
        // arrange
        ProvisioningConnectionString provisioningConnectionString = buildProvisioningConnectionString();

        // act
        String result = provisioningConnectionString.getHostName();

        // assert
        assertEquals(VALID_HOST_NAME_PROPERTY_NAME, result);
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_21_009: [The setSharedAccessKeyName shall update the sharedAccessKeyName by the provided one.] */
    @Test
    public void setSharedAccessKeyNameSucceeded()
    {
        // arrange
        ProvisioningConnectionString provisioningConnectionString = buildProvisioningConnectionString();
        final String newValue = "newSharedAccessKeyName";

        // act
        Deencapsulation.invoke(provisioningConnectionString, "setSharedAccessKeyName", newValue);

        // assert
        assertEquals(newValue, provisioningConnectionString.getSharedAccessKeyName());
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_21_010: [The setSharedAccessKey shall update the sharedAccessKey by the provided one.] */
    @Test
    public void setSharedAccessKeySucceeded()
    {
        // arrange
        ProvisioningConnectionString provisioningConnectionString = buildProvisioningConnectionString();
        final String newValue = "newSharedAccessKey";

        // act
        Deencapsulation.invoke(provisioningConnectionString, "setSharedAccessKey", newValue);

        // assert
        assertEquals(newValue, provisioningConnectionString.getSharedAccessKey());
    }

    /* Tests_SRS_PROVISIONINGCONNECTIONSTRING_21_011: [The setSharedAccessSignature shall update the sharedAccessSignature by the provided one.] */
    @Test
    public void setSharedAccessSignatureSucceeded()
    {
        // arrange
        ProvisioningConnectionString provisioningConnectionString = buildProvisioningConnectionString();
        final String newValue = "newSharedAccessSignature";

        // act
        Deencapsulation.invoke(provisioningConnectionString, "setSharedAccessSignature", newValue);

        // assert
        assertEquals(newValue, provisioningConnectionString.getSharedAccessSignature());
    }

}
