// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.auth.AuthenticationMechanism;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.X509Thumbprint;
import com.microsoft.azure.sdk.iot.service.serializers.AuthenticationTypeParser;
import com.microsoft.azure.sdk.iot.service.serializers.ExportImportDeviceParser;
import mockit.Deencapsulation;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Code coverage:
 * 94% Methods
 * 96% lines
 */
public class ExportImportDeviceTest
{
    private static final String SAMPLE_THUMBPRINT = "0000000000000000000000000000000000000000";
    private static final String SAMPLE_KEY = "000000000000000000000000";

    // Tests_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_15_001: [The ExportImportDevice class shall have the following properties: Id, Etag, ImportMode, Status, StatusReason, Authentication]
    @Test
    public void gettersAndSettersWork()
    {
        // arrange
        ExportImportDevice device = Deencapsulation.newInstance(ExportImportDevice.class, new Class[]{});
        AuthenticationMechanism expectedAuthentication = new AuthenticationMechanism(AuthenticationType.CERTIFICATE_AUTHORITY);
        String expectedETag = "etag";
        String expectedId = "id";
        ImportMode expectedImportMode = ImportMode.Create;
        DeviceStatus expectedStatus = DeviceStatus.Disabled;
        String expectedStatusReason = "test";

        // act
        device.setAuthentication(expectedAuthentication);
        device.setETag(expectedETag);
        device.setId(expectedId);
        device.setImportMode(expectedImportMode);
        device.setStatus(expectedStatus);
        device.setStatusReason(expectedStatusReason);

        // assert
        assertEquals(expectedAuthentication, device.getAuthentication());
        assertEquals(expectedETag, device.getETag());
        assertEquals(expectedId, device.getId());
        assertEquals(expectedImportMode, device.getImportMode());
        assertEquals(expectedStatus, device.getStatus());
        assertEquals(expectedStatusReason, device.getStatusReason());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_050: [This constructor shall automatically set the authentication type of this object to be SAS, and shall generate a deviceId and symmetric key.]
    @Test
    public void emptyConstructorGeneratesDeviceIdSymmetricKeyAndAuthType()
    {
        //act
        ExportImportDevice device = new ExportImportDevice();

        //assert
        assertNotNull(device.getAuthentication());
        assertNotNull(device.getAuthentication().getSymmetricKey());
        assertEquals(AuthenticationType.SAS, device.getAuthentication().getAuthenticationType());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_054: [This method shall convert this into an ExportImportDeviceParser object and return it.]
    @Test
    public void conversionToExportImportDeviceParser()
    {
        // arrange
        ExportImportDevice deviceCA = new ExportImportDevice();
        deviceCA.setId("deviceCA");
        deviceCA.setAuthentication(new AuthenticationMechanism(AuthenticationType.CERTIFICATE_AUTHORITY));
        deviceCA.setImportMode(ImportMode.CreateOrUpdate);
        deviceCA.setStatus(DeviceStatus.Enabled);

        ExportImportDevice deviceSelf = new ExportImportDevice();
        deviceSelf.setId("deviceSelf");
        deviceSelf.setAuthentication(new AuthenticationMechanism(AuthenticationType.SELF_SIGNED));

        ExportImportDevice deviceSAS = new ExportImportDevice();
        deviceSAS.setId("deviceSAS");
        deviceSAS.setAuthentication(new AuthenticationMechanism(AuthenticationType.SAS));

        // act
        ExportImportDeviceParser parserCA = reflectivelyInvokeToExportImportDeviceParser(deviceCA);
        ExportImportDeviceParser parserSelf = reflectivelyInvokeToExportImportDeviceParser(deviceSelf);
        ExportImportDeviceParser parserSAS = reflectivelyInvokeToExportImportDeviceParser(deviceSAS);

        // assert
        assertEquals(AuthenticationTypeParser.CERTIFICATE_AUTHORITY, parserCA.getAuthentication().getType());
        assertEquals(AuthenticationTypeParser.SELF_SIGNED, parserSelf.getAuthentication().getType());
        assertEquals(AuthenticationTypeParser.SAS, parserSAS.getAuthentication().getType());

        assertEquals(ImportMode.CreateOrUpdate.toString(), parserCA.getImportMode());
        assertEquals(DeviceStatus.Enabled.toString(), parserCA.getStatus());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_051: [This constructor shall save the provided deviceId and authenticationType to itself.]
    @Test
    public void constructorSavesDeviceIdAndAuthType()
    {
        //arrange
        String deviceId = "someDevice";

        //act
        ExportImportDevice device = new ExportImportDevice(deviceId, AuthenticationType.CERTIFICATE_AUTHORITY);

        //assert
        assertEquals(AuthenticationType.CERTIFICATE_AUTHORITY, device.getAuthentication().getAuthenticationType());
        assertEquals(deviceId, device.getId());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_055: [If the provided id is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void cannotSetAuthenticationNull()
    {
        //act
        new ExportImportDevice().setAuthentication(null);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_057: [If either the provided deviceId or authenticationType is null or empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorRejectsNullDeviceId()
    {
        //act
        new ExportImportDevice(null, AuthenticationType.CERTIFICATE_AUTHORITY);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_057: [If either the provided deviceId or authenticationType is null or empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorRejectsEmptyDeviceId()
    {
        //act
        new ExportImportDevice("", AuthenticationType.CERTIFICATE_AUTHORITY);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_057: [If either the provided deviceId or authenticationType is null or empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorRejectsNullAuthenticationType()
    {
        //act
        new ExportImportDevice("someDevice", null);
    }

    @Test (expected = IllegalStateException.class)
    public void toParserIllegalStateThrownWhenUsingSASAuthenticationWithoutSymmetricKeySaved()
    {
        //arrange
        ExportImportDevice device = new ExportImportDevice();
        device.setId("someDevice");
        AuthenticationMechanism authentication = new AuthenticationMechanism(AuthenticationType.SAS);
        Deencapsulation.setField(authentication, "symmetricKey", null);
        device.setAuthentication(authentication);

        //act
        reflectivelyInvokeToExportImportDeviceParser(device);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_061: [If this device uses self signed authentication, but does not have a primary and secondary thumbprint saved, an IllegalStateException shall be thrown.]
    @Test (expected = IllegalStateException.class)
    public void toParserIllegalStateThrownWhenUsingSelfSignedAuthenticationWithoutThumbprintSaved()
    {
        //arrange
        ExportImportDevice device = new ExportImportDevice();
        device.setId("someDevice");
        AuthenticationMechanism authentication = new AuthenticationMechanism(AuthenticationType.SELF_SIGNED);
        Deencapsulation.setField(authentication, "thumbprint", null);
        device.setAuthentication(authentication);

        //act
        reflectivelyInvokeToExportImportDeviceParser(device);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_061: [If this device uses self signed authentication, but does not have a primary and secondary thumbprint saved, an IllegalStateException shall be thrown.]
    @Test (expected = IllegalStateException.class)
    public void toParserIllegalStateThrownWhenUsingSelfSignedAuthenticationWithoutPrimaryThumbprintSaved()
    {
        //arrange
        ExportImportDevice device = new ExportImportDevice();
        device.setId("someDevice");
        AuthenticationMechanism authentication = new AuthenticationMechanism(AuthenticationType.SELF_SIGNED);
        X509Thumbprint thumbprint = Deencapsulation.newInstance(X509Thumbprint.class);
        Deencapsulation.setField(thumbprint, "primaryThumbprint", null);
        Deencapsulation.setField(authentication, "thumbprint", thumbprint);
        device.setAuthentication(authentication);

        //act
        reflectivelyInvokeToExportImportDeviceParser(device);
    }

    /**
     * Uses reflection to create an ExportImportDevice and sets the provided properties
     * @param authentication the authentication the device uses
     * @param importMode the import mode the device uses
     * @param status the status of the device
     * @return the created ExportImportDevice object
     */
    private ExportImportDevice createTestDevice(AuthenticationMechanism authentication, ImportMode importMode, DeviceStatus status)
    {
        ExportImportDevice device = Deencapsulation.newInstance(ExportImportDevice.class);
        device.setAuthentication(authentication);
        device.setImportMode(importMode);
        device.setStatus(status);

        return device;
    }

    /**
     * Uses reflection to invoke the ExportImportDevice method "toExportImportDeviceParser()"
     * @param device the device to invoke this on
     * @return the returned value from the invocation.
     */
    private ExportImportDeviceParser reflectivelyInvokeToExportImportDeviceParser(ExportImportDevice device)
    {
        return Deencapsulation.invoke(device, "toExportImportDeviceParser");
    }
}