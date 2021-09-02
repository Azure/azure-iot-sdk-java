// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.deps.serializer.*;
import com.microsoft.azure.sdk.iot.service.DeviceStatus;
import com.microsoft.azure.sdk.iot.service.ExportImportDevice;
import com.microsoft.azure.sdk.iot.service.ImportMode;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationMechanism;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;
import com.microsoft.azure.sdk.iot.service.auth.X509Thumbprint;
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
        device.seteTag(expectedETag);
        device.setId(expectedId);
        device.setImportMode(expectedImportMode);
        device.setStatus(expectedStatus);
        device.setStatusReason(expectedStatusReason);

        // assert
        assertEquals(expectedAuthentication, device.getAuthenticationFinal());
        assertEquals(expectedETag, device.geteTag());
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
        assertNotNull(device.getAuthenticationFinal());
        assertNotNull(device.getAuthenticationFinal().getSymmetricKey());
        assertEquals(AuthenticationType.SAS, device.getAuthenticationFinal().getAuthenticationType());
    }

    @Test
    public void equalsWorks()
    {
        //arrange
        ExportImportDevice device1 = createTestDevice(new AuthenticationMechanism(AuthenticationType.CERTIFICATE_AUTHORITY), ImportMode.Create, DeviceStatus.Disabled);
        ExportImportDevice device2 = createTestDevice(new AuthenticationMechanism(AuthenticationType.CERTIFICATE_AUTHORITY), ImportMode.Create, DeviceStatus.Disabled);
        ExportImportDevice device3 = createTestDevice(new AuthenticationMechanism(AuthenticationType.CERTIFICATE_AUTHORITY), ImportMode.CreateOrUpdate, DeviceStatus.Disabled);
        ExportImportDevice device4 = createTestDevice(new AuthenticationMechanism(AuthenticationType.CERTIFICATE_AUTHORITY), ImportMode.Create, DeviceStatus.Enabled);
        ExportImportDevice device5 = createTestDevice(new AuthenticationMechanism(AuthenticationType.SELF_SIGNED), ImportMode.Create, DeviceStatus.Enabled);

        //assert
        assertEquals(device1, device2);
        assertNotEquals(device1, device3);
        assertNotEquals(device1, device4);
        assertNotEquals(device1, device5);
        assertNotEquals(device1, 1);
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
        assertEquals(AuthenticationTypeParser.CERTIFICATE_AUTHORITY, parserCA.getAuthenticationFinal().getType());
        assertEquals(AuthenticationTypeParser.SELF_SIGNED, parserSelf.getAuthenticationFinal().getType());
        assertEquals(AuthenticationTypeParser.SAS, parserSAS.getAuthenticationFinal().getType());

        assertEquals(ImportMode.CreateOrUpdate.toString(), parserCA.getImportMode());
        assertEquals(DeviceStatus.Enabled.toString(), parserCA.getStatus());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_052: [This constructor shall use the properties of the provided parser object to set the new ExportImportDevice's properties.]
    @Test
    public void conversionFromDeviceParser()
    {
        // arrange
        ExportImportDeviceParser parserCA = new ExportImportDeviceParser();
        parserCA.setAuthentication(Deencapsulation.newInstance(AuthenticationParser.class));
        parserCA.getAuthenticationFinal().setType(AuthenticationTypeParser.CERTIFICATE_AUTHORITY);
        parserCA.setStatus("Enabled");
        parserCA.setImportMode("Create");
        parserCA.setId("deviceCA");

        ExportImportDeviceParser parserSelf = new ExportImportDeviceParser();
        parserSelf.setAuthentication(Deencapsulation.newInstance(AuthenticationParser.class));
        parserSelf.getAuthenticationFinal().setType(AuthenticationTypeParser.SELF_SIGNED);
        parserSelf.getAuthenticationFinal().setThumbprint(new X509ThumbprintParser(SAMPLE_THUMBPRINT, SAMPLE_THUMBPRINT));
        parserSelf.setId("deviceSelf");

        ExportImportDeviceParser parserSAS = new ExportImportDeviceParser();
        parserSAS.setAuthentication(Deencapsulation.newInstance(AuthenticationParser.class));
        parserSAS.getAuthenticationFinal().setType(AuthenticationTypeParser.SAS);
        parserSAS.getAuthenticationFinal().setSymmetricKey(new SymmetricKeyParser(SAMPLE_THUMBPRINT,SAMPLE_THUMBPRINT));
        parserSAS.setId("deviceSAS");

        // act
        ExportImportDevice deviceCA = reflectivelyInvokeExportImportDeviceParserConstructor(parserCA);
        ExportImportDevice deviceSelf = reflectivelyInvokeExportImportDeviceParserConstructor(parserSelf);
        ExportImportDevice deviceSAS = reflectivelyInvokeExportImportDeviceParserConstructor(parserSAS);

        // assert
        assertEquals(AuthenticationType.CERTIFICATE_AUTHORITY, deviceCA.getAuthenticationFinal().getAuthenticationType());
        assertEquals(AuthenticationType.SELF_SIGNED, deviceSelf.getAuthenticationFinal().getAuthenticationType());
        assertEquals(AuthenticationType.SAS, deviceSAS.getAuthenticationFinal().getAuthenticationType());

        assertEquals(ImportMode.Create, deviceCA.getImportMode());
        assertEquals(DeviceStatus.Enabled, deviceCA.getStatus());
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
        assertEquals(AuthenticationType.CERTIFICATE_AUTHORITY, device.getAuthenticationFinal().getAuthenticationType());
        assertEquals(deviceId, device.getId());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_053: [If the provided parser does not have values for the properties deviceId or authentication, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void conversionFromDeviceParserMissingDeviceIdThrows()
    {
        // arrange
        ExportImportDeviceParser parser = new ExportImportDeviceParser();
        parser.setAuthentication(Deencapsulation.newInstance(AuthenticationParser.class));
        parser.getAuthenticationFinal().setType(AuthenticationTypeParser.CERTIFICATE_AUTHORITY);
        Deencapsulation.setField(parser, "Id", null);

        // act
        reflectivelyInvokeExportImportDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_053: [If the provided parser does not have values for the properties deviceId or authentication, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void conversionFromDeviceParserMissingAuthenticationThrows()
    {
        // arrange
        ExportImportDeviceParser parser = new ExportImportDeviceParser();
        Deencapsulation.setField(parser, "Authentication", null);
        parser.setId("deviceCA");

        // act
        reflectivelyInvokeExportImportDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_056: [If the provided authentication is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void cannotSetIdNull()
    {
        //act
        new ExportImportDevice().setId(null);
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

    //Codes_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_058: [If the provided parser uses SAS authentication and is missing one or both symmetric keys, two new keys will be generated.]
    @Test
    public void constructorWithParserGeneratesMissingSecondaryKeyWhenSASAuthenticated()
    {
        //arrange
        ExportImportDeviceParser parser = new ExportImportDeviceParser();
        parser.setId("someDevice");
        parser.setAuthentication(new AuthenticationParser());
        parser.getAuthenticationFinal().setType(AuthenticationTypeParser.SAS);
        parser.getAuthenticationFinal().setSymmetricKey(new SymmetricKeyParser());
        parser.getAuthenticationFinal().getSymmetricKey().setPrimaryKey(SAMPLE_KEY);

        //act
        ExportImportDevice device = reflectivelyInvokeExportImportDeviceParserConstructor(parser);

        //assert
        assertNotNull(device.getAuthenticationFinal());
        assertNotNull(device.getAuthenticationFinal().getSymmetricKey());
        assertNotNull(device.getAuthenticationFinal().getSymmetricKey().getPrimaryKey());
        assertNotNull(device.getAuthenticationFinal().getSymmetricKey().getSecondaryKey());
        assertNotEquals(SAMPLE_KEY, device.getAuthenticationFinal().getSymmetricKey().getPrimaryKey());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_059: [If the provided parser uses self signed authentication and is missing one or both thumbprints, two new thumbprints will be generated.]
    @Test
    public void constructorWithParserGeneratesMissingSecondaryThumbprintsWhenSelfSigned()
    {
        //arrange
        ExportImportDeviceParser parser = new ExportImportDeviceParser();
        parser.setId("someDevice");
        parser.setAuthentication(new AuthenticationParser());
        parser.getAuthenticationFinal().setType(AuthenticationTypeParser.SELF_SIGNED);
        parser.getAuthenticationFinal().setThumbprint(new X509ThumbprintParser());
        parser.getAuthenticationFinal().getThumbprint().setPrimaryThumbprint(SAMPLE_THUMBPRINT);

        //act
        ExportImportDevice device = reflectivelyInvokeExportImportDeviceParserConstructor(parser);

        //assert
        assertNotNull(device.getAuthenticationFinal());
        assertNotNull(device.getAuthenticationFinal().getPrimaryThumbprint());
        assertNotNull(device.getAuthenticationFinal().getSecondaryThumbprint());
        assertNotEquals(SAMPLE_THUMBPRINT, device.getAuthenticationFinal().getPrimaryThumbprint());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_060: [If this device uses sas authentication, but does not have a primary and secondary symmetric key saved, an IllegalStateException shall be thrown.]
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

    //Tests_SRS_SERVICE_SDK_JAVA_IMPORT_EXPORT_DEVICE_34_060: [If this device uses sas authentication, but does not have a primary and secondary symmetric key saved, an IllegalStateException shall be thrown.]
    @Test (expected = IllegalStateException.class)
    public void toParserIllegalStateThrownWhenUsingSASAuthenticationWithoutPrimaryKeySaved()
    {
        //arrange
        ExportImportDevice device = new ExportImportDevice();
        device.setId("someDevice");
        AuthenticationMechanism authentication = new AuthenticationMechanism(AuthenticationType.SAS);
        SymmetricKey symmetricKey = new SymmetricKey();
        Deencapsulation.setField(symmetricKey, "primaryKey", null);
        Deencapsulation.setField(authentication, "symmetricKey", symmetricKey);
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
     * Uses refelection to invoke the constructor for an ExportImportDevice that takes an ExportImportDeviceParser object as the only argument
     * @param parser the parser to pass into the constructor
     * @return the created ExportImportDevice object
     */
    private ExportImportDevice reflectivelyInvokeExportImportDeviceParserConstructor(ExportImportDeviceParser parser)
    {
        return Deencapsulation.newInstance(ExportImportDevice.class, new Class[] { ExportImportDeviceParser.class }, parser);
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