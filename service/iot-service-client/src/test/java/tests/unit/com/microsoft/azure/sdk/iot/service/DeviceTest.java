/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.deps.serializer.*;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.DeviceConnectionState;
import com.microsoft.azure.sdk.iot.service.DeviceStatus;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationMechanism;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;
import com.microsoft.azure.sdk.iot.service.auth.X509Thumbprint;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.NonStrictExpectations;
import org.junit.Test;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

/**
 * Code coverage:
 * 100% Methods
 * 87% lines
 */
public class DeviceTest
{
    private static final String SAMPLE_THUMBPRINT = "0000000000000000000000000000000000000000";
    private static final String SAMPLE_KEY = "000000000000000000000000";

    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_12_001: [The Device class shall have the following properties: Id, Etag, Authentication.SymmetricKey, State, StateReason, StateUpdatedTime, ConnectionState, ConnectionStateUpdatedTime, LastActivityTime, symmetricKey, thumbprint, status, authentication]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void device_get_all_properties() throws NoSuchAlgorithmException
    {
        // Arrange
        String deviceId = "xxx-device";
        SymmetricKey expectedSymmetricKey = new SymmetricKey();
        String expectedPrimaryThumbprint = "0000000000000000000000000000000000000000";
        String expectedSecondaryThumbprint = "1111111111111111111111111111111111111111";
        AuthenticationMechanism expectedAuthentication = new AuthenticationMechanism(expectedPrimaryThumbprint, expectedSecondaryThumbprint);

        // Act
        Device device = Device.createFromId(deviceId, null, null);

        device.setSymmetricKey(expectedSymmetricKey);
        assertEquals(expectedSymmetricKey, device.getSymmetricKey());

        device.setThumbprint(expectedPrimaryThumbprint, expectedSecondaryThumbprint);
        assertEquals(expectedPrimaryThumbprint, device.getPrimaryThumbprint());
        assertEquals(expectedSecondaryThumbprint, device.getSecondaryThumbprint());

        device.setStatus(DeviceStatus.Enabled);
        assertEquals(DeviceStatus.Enabled, device.getStatus());

        device.getPrimaryThumbprint();
        device.getSecondaryThumbprint();
        device.getDeviceId();
        device.getGenerationId();
        device.getPrimaryKey();
        device.getSecondaryKey();
        device.geteTag();
        device.getStatus();
        device.getStatusReason();
        device.getStatusUpdatedTime();
        device.getConnectionState();
        device.getConnectionStateUpdatedTime();
        device.getLastActivityTime();
        device.getCloudToDeviceMessageCount();

        device.setForceUpdate(true);
        device.setForceUpdate(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_12_002: [The constructor shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void createFromId_input_null() throws NoSuchAlgorithmException
    {
        // Arrange
        String deviceId = null;

        // Act
        Device.createFromId(deviceId, null, null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_009: [The function shall throw IllegalArgumentException if the provided deviceId or authenticationType is empty or null.]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void createDeviceThrowsIllegalArgumentExceptionWhenGivenNullDeviceId()
    {
        // Arrange
        String deviceId = null;

        // Act
        Device.createDevice(deviceId, AuthenticationType.CERTIFICATE_AUTHORITY);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_009: [The function shall throw IllegalArgumentException if the provided deviceId or authenticationType is empty or null.]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void createDeviceThrowsIllegalArgumentExceptionWhenGivenNullAuthenticationType()
    {
        // Act
        Device.createDevice("someDevice", null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_12_002: [The constructor shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void createFromId_input_empty() throws NoSuchAlgorithmException
    {
        // Arrange
        String deviceId = "";

        // Act
        Device.createFromId(deviceId, null, null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_12_003: [The constructor shall create a new instance of Device using the given deviceId and return with it]
    @Test
    public void createFromId_good_case() throws NoSuchAlgorithmException
    {
        // Arrange
        String deviceId = "xxx-device";
        new Expectations()
        {
            {
                Deencapsulation.newInstance(Device.class, deviceId, DeviceStatus.class, SymmetricKey.class);
            }
        };

        // Act
        Device device = Device.createFromId(deviceId, null, null);

        // Assert
        assertNotEquals(device, null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_12_004: [The constructor shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_string_null() throws NoSuchAlgorithmException
    {
        // Arrange
        String deviceId = null;

        // Act
        Device device = Deencapsulation.newInstance(Device.class, deviceId, null, null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_12_004: [The constructor shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_string_empty() throws NoSuchAlgorithmException
    {
        // Arrange
        String deviceId = "";

        // Act
        Device device = Deencapsulation.newInstance(Device.class, deviceId, null, null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_12_005: [If the input symmetric key is empty, the constructor shall create
    // a new SymmetricKey instance using AES encryption and store it into a member variable]
    @Test
    public void constructor_create_symmetrickey() throws NoSuchAlgorithmException
    {
        // Arrange
        String encryptionMethod = "AES";
        String deviceId = "xxx-device";
        new NonStrictExpectations()
        {
            {
                SymmetricKey symmetricKey = new SymmetricKey();
                KeyGenerator.getInstance(encryptionMethod);
            }
        };

        // Act
        Device device = Deencapsulation.newInstance(Device.class, deviceId, DeviceStatus.class, SymmetricKey.class);

        // Assert
        assertNotEquals(device.getSymmetricKey(), null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_15_007: [The constructor shall store
    // the input device status and symmetric key into a member variable]
    @Test
    public void constructor_sets_status_and_symmetrickey() throws NoSuchAlgorithmException
    {
        // Arrange
        String deviceId = "xxx-device";
        DeviceStatus expectedDeviceStatus = DeviceStatus.Disabled;
        SymmetricKey expectedSymmetricKey = new SymmetricKey();

        // Act
        Device device = Deencapsulation.newInstance(Device.class, deviceId, expectedDeviceStatus, expectedSymmetricKey); 
        
        // Assert
        assertEquals(expectedDeviceStatus, device.getStatus());
        assertEquals(expectedSymmetricKey, device.getSymmetricKey());
    }

    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_12_006: [The constructor shall initialize all properties to default value]
    @Test
    public void constructor_initialize_properties() throws NoSuchAlgorithmException
    {
        // Arrange
        String utcTimeDefault = "0001-01-01T00:00:00";
        String offsetTimeDefault = "0001-01-01T00:00:00-00:00";
        String deviceId = "xxx-device";

        // Act
        Device device = Deencapsulation.newInstance(Device.class, deviceId, DeviceStatus.class, SymmetricKey.class);

        // Assert
        assertNotEquals(null, device);
        assertNotEquals(device.getSymmetricKey(), null);

        assertEquals("xxx-device", device.getDeviceId());
        assertEquals("", device.getGenerationId());
        assertEquals("", device.geteTag());
        assertEquals(DeviceStatus.Enabled, device.getStatus());
        assertEquals("", device.getStatusReason());
        assertEquals(utcTimeDefault, device.getStatusUpdatedTime());
        assertEquals(DeviceConnectionState.Disconnected, device.getConnectionState());
        assertEquals(utcTimeDefault, device.getStatusUpdatedTime());
        assertEquals(utcTimeDefault, device.getConnectionStateUpdatedTime());
        assertEquals(offsetTimeDefault, device.getLastActivityTime());
        assertEquals(0, device.getCloudToDeviceMessageCount());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_018: [This method shall return a new instance of a DeviceParser object that is populated using the properties of this.]
    @Test
    public void conversionToDeviceParser()
    {
        // arrange
        String expectedDeviceId = "deviceCA";
        DeviceStatus expectedDeviceStatus = DeviceStatus.Disabled;
        boolean expectedForceUpdate = false;
        String expectedStatusReason = "no reason";
        String expectedStatusUpdatedTime = "2001-09-09T09:09:09";
        int expectedCloudToDeviceMessageCount = 23;
        DeviceConnectionState expectedConnectionState = DeviceConnectionState.Connected;
        String expectedConnectionStateUpdatedTime = "2001-09-09T09:09:09";
        String expectedETag = "1234";
        String expectedGenerationId = "5678";
        String expectedLastActivityTime = "2001-09-09T09:09:09";

        Device device = Device.createDevice(expectedDeviceId, AuthenticationType.CERTIFICATE_AUTHORITY);
        device.setStatus(expectedDeviceStatus);
        device.setForceUpdate(expectedForceUpdate);
        Deencapsulation.setField(device, "statusReason", expectedStatusReason);
        Deencapsulation.setField(device, "statusUpdatedTime", expectedStatusUpdatedTime);
        Deencapsulation.setField(device, "cloudToDeviceMessageCount", expectedCloudToDeviceMessageCount);
        Deencapsulation.setField(device, "connectionState", expectedConnectionState);
        Deencapsulation.setField(device, "connectionStateUpdatedTime", expectedConnectionStateUpdatedTime);
        Deencapsulation.setField(device, "eTag", expectedETag);
        Deencapsulation.setField(device, "generationId", expectedGenerationId);
        Deencapsulation.setField(device, "lastActivityTime", expectedLastActivityTime);

        // act
        DeviceParser parser = reflectivelyInvokeToDeviceParser(device);

        // assert
        assertEquals(AuthenticationTypeParser.CERTIFICATE_AUTHORITY, parser.getAuthenticationParser().getType());
        assertEquals(expectedDeviceStatus.toString(), parser.getStatus());
        assertEquals(expectedDeviceStatus.toString(), parser.getStatus());
        assertEquals(expectedStatusReason, parser.getStatusReason());
        assertEquals(ParserUtility.getDateTimeUtc(expectedStatusUpdatedTime), parser.getStatusUpdatedTime());
        assertEquals(expectedCloudToDeviceMessageCount, parser.getCloudToDeviceMessageCount());
        assertEquals(expectedConnectionState.toString(), parser.getConnectionState());
        assertEquals(ParserUtility.getDateTimeUtc(expectedConnectionStateUpdatedTime), parser.getConnectionStateUpdatedTime());
        assertEquals(expectedDeviceId, parser.getDeviceId());
        assertEquals(expectedETag, parser.geteTag());
        assertEquals(expectedGenerationId, parser.getGenerationId());
        assertEquals(ParserUtility.getDateTimeUtc(expectedLastActivityTime), parser.getLastActivityTime());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_018: [This method shall return a new instance of a DeviceParser object that is populated using the properties of this.]
    @Test
    public void conversionToDeviceParserWithSelfSignedAuthentication()
    {
        // arrange
        Device deviceSelf = Device.createDevice("deviceSelf", AuthenticationType.SELF_SIGNED);

        // act
        DeviceParser parserSelf = reflectivelyInvokeToDeviceParser(deviceSelf);

        // assert
        assertEquals(AuthenticationTypeParser.SELF_SIGNED, parserSelf.getAuthenticationParser().getType());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_018: [This method shall return a new instance of a DeviceParser object that is populated using the properties of this.]
    @Test
    public void conversionToDeviceParserWithSASAuthentication()
    {
        // arrange
        Device deviceSAS = Device.createDevice("deviceSAS", AuthenticationType.SAS);

        // act
        DeviceParser parserSAS = reflectivelyInvokeToDeviceParser(deviceSAS);

        // assert
        assertEquals(AuthenticationTypeParser.SAS, parserSAS.getAuthenticationParser().getType());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_014: [This constructor shall create a new Device object using the values within the provided parser.]
    @Test
    public void conversionFromDeviceParserWithCertificateAuthorityAuthentication()
    {
        // arrange
        DeviceParser parserCA = new DeviceParser();
        parserCA.setAuthenticationParser(Deencapsulation.newInstance(AuthenticationParser.class));
        parserCA.getAuthenticationParser().setType(AuthenticationTypeParser.CERTIFICATE_AUTHORITY);
        parserCA.setDeviceId("deviceCA");

        // act
        Device deviceCA = reflectivelyInvokeDeviceParserConstructor(parserCA);

        // assert
        assertNull(deviceCA.getPrimaryThumbprint());
        assertNull(deviceCA.getSecondaryThumbprint());
        assertNull(deviceCA.getSymmetricKey());
        assertEquals(AuthenticationType.CERTIFICATE_AUTHORITY, deviceCA.getAuthenticationType());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_014: [This constructor shall create a new Device object using the values within the provided parser.]
    @Test
    public void conversionFromDeviceParserWithSelfSignedAuthentication()
    {
        // arrange
        DeviceParser parserSelf = new DeviceParser();
        parserSelf.setAuthenticationParser(Deencapsulation.newInstance(AuthenticationParser.class));
        parserSelf.getAuthenticationParser().setType(AuthenticationTypeParser.SELF_SIGNED);
        parserSelf.getAuthenticationParser().setThumbprint(new X509ThumbprintParser(SAMPLE_THUMBPRINT, SAMPLE_THUMBPRINT));
        parserSelf.setDeviceId("deviceSelf");

        // act
        Device deviceSelf = reflectivelyInvokeDeviceParserConstructor(parserSelf);

        // assert
        assertNull(deviceSelf.getSymmetricKey());
        assertNotNull(deviceSelf.getPrimaryThumbprint());
        assertNotNull(deviceSelf.getSecondaryThumbprint());
        assertEquals(AuthenticationType.SELF_SIGNED, deviceSelf.getAuthenticationType());
    }


    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_014: [This constructor shall create a new Device object using the values within the provided parser.]
    @Test
    public void conversionFromDeviceParserWithSASAuthentication()
    {
        // arrange
        DeviceParser parserSAS = new DeviceParser();
        parserSAS.setAuthenticationParser(Deencapsulation.newInstance(AuthenticationParser.class));
        parserSAS.getAuthenticationParser().setType(AuthenticationTypeParser.SAS);
        parserSAS.getAuthenticationParser().setSymmetricKey(new SymmetricKeyParser(SAMPLE_KEY, SAMPLE_KEY));
        parserSAS.setDeviceId("deviceSAS");

        // act
        Device deviceSAS = reflectivelyInvokeDeviceParserConstructor(parserSAS);

        // assert
        assertNull(deviceSAS.getPrimaryThumbprint());
        assertNull(deviceSAS.getSecondaryThumbprint());
        assertNotNull(deviceSAS.getSymmetricKey());
        assertEquals(AuthenticationType.SAS, deviceSAS.getAuthenticationType());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_015: [If the provided parser is missing a value for its authentication or its device Id, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void conversionFromDeviceWithoutDeviceIdThrowsIllegalArgumentException()
    {
        // arrange
        DeviceParser parser = new DeviceParser();
        parser.setAuthenticationParser(Deencapsulation.newInstance(AuthenticationParser.class));
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.CERTIFICATE_AUTHORITY);

        // act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_015: [If the provided parser is missing a value for its authentication or its device Id, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void conversionFromDeviceWithoutAuthenticationTypeThrowsIllegalArgumentException()
    {
        // arrange
        DeviceParser parser = new DeviceParser();
        parser.setDeviceId("someDevice");

        // act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_011: [If the provided authenticationType is certificate authority, no symmetric key shall be generated and no thumbprint shall be generated]
    @Test
    public void deviceConstructorWithCertificateAuthorityGeneratesKeysCorrectly()
    {
        // act
        Device device = Deencapsulation.newInstance(Device.class, new Class[] { String.class, AuthenticationType.class }, "someDevice", AuthenticationType.CERTIFICATE_AUTHORITY);

        // assert
        assertNull(device.getPrimaryThumbprint());
        assertNull(device.getSecondaryThumbprint());
        assertNull(device.getSymmetricKey());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_012: [If the provided authenticationType is SAS, a symmetric key shall be generated but no thumbprint shall be generated]
    @Test
    public void deviceConstructorWithSharedAccessSignatureGeneratesKeysCorrectly()
    {
        // act
        Device device = Deencapsulation.newInstance(Device.class, new Class[] { String.class, AuthenticationType.class }, "someDevice", AuthenticationType.SAS);

        // assert
        assertNull(device.getPrimaryThumbprint());
        assertNull(device.getSecondaryThumbprint());
        assertNotNull(device.getSymmetricKey());
        assertNotNull(device.getPrimaryKey());
        assertNotNull(device.getSecondaryKey());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_013: [If the provided authenticationType is self signed, a thumbprint shall be generated but no symmetric key shall be generated]
    @Test
    public void deviceConstructorWithSelfSignedGeneratesKeysCorrectly()
    {
        // act
        Device device = Deencapsulation.newInstance(Device.class, new Class[] { String.class, AuthenticationType.class }, "someDevice", AuthenticationType.SELF_SIGNED);

        // assert
        assertNotNull(device.getPrimaryThumbprint());
        assertNotNull(device.getSecondaryThumbprint());
        assertNull(device.getSymmetricKey());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_009: [The function shall throw IllegalArgumentException if the provided deviceId or authenticationType is empty or null.]
    @Test (expected = IllegalArgumentException.class)
    public void createDeviceThrowsIllegalArgumentExceptionForNullDeviceId()
    {
        //act
        Device.createDevice(null, AuthenticationType.CERTIFICATE_AUTHORITY);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_009: [The function shall throw IllegalArgumentException if the provided deviceId or authenticationType is empty or null.]
    @Test (expected = IllegalArgumentException.class)
    public void createDeviceThrowsIllegalArgumentExceptionForEmptyDeviceId()
    {
        //act
        Device.createDevice("", AuthenticationType.CERTIFICATE_AUTHORITY);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_009: [The function shall throw IllegalArgumentException if the provided deviceId or authenticationType is empty or null.]
    @Test (expected = IllegalArgumentException.class)
    public void createDeviceThrowsIllegalArgumentExceptionForNullAuthenticationType()
    {
        //act
        Device.createDevice("someDevice", null);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_016: [If the provided parser uses SAS authentication and is missing one or both symmetric keys, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithParserThrowsForMissingSymmetricKeyWhenSASAuthenticated()
    {
        //arrange
        DeviceParser parser = new DeviceParser();
        parser.setDeviceId("someDevice");
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.SAS);

        //act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_016: [If the provided parser uses SAS authentication and is missing one or both symmetric keys, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithParserThrowsForMissingKeysWhenSASAuthenticated()
    {
        //arrange
        DeviceParser parser = new DeviceParser();
        parser.setDeviceId("someDevice");
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.SAS);
        parser.getAuthenticationParser().setSymmetricKey(new SymmetricKeyParser());

        //act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_016: [If the provided parser uses SAS authentication and is missing one or both symmetric keys, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithParserThrowsForMissingPrimaryKeyWhenSASAuthenticated()
    {
        //arrange
        DeviceParser parser = new DeviceParser();
        parser.setDeviceId("someDevice");
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.SAS);
        parser.getAuthenticationParser().setSymmetricKey(new SymmetricKeyParser());
        parser.getAuthenticationParser().getSymmetricKey().setSecondaryKey(SAMPLE_KEY);

        //act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_016: [If the provided parser uses SAS authentication and is missing one or both symmetric keys, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithParserThrowsForMissingSecondaryKeyWhenSASAuthenticated()
    {
        //arrange
        DeviceParser parser = new DeviceParser();
        parser.setDeviceId("someDevice");
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.SAS);
        parser.getAuthenticationParser().setSymmetricKey(new SymmetricKeyParser());
        parser.getAuthenticationParser().getSymmetricKey().setPrimaryKey(SAMPLE_KEY);

        //act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_017: [If the provided parser uses SELF_SIGNED authentication and is missing one or both thumbprint, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithParserThrowsForMissingX509ThumbprintWhenSelfSigned()
    {
        //arrange
        DeviceParser parser = new DeviceParser();
        parser.setDeviceId("someDevice");
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.SELF_SIGNED);

        //act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_017: [If the provided parser uses SELF_SIGNED authentication and is missing one or both thumbprint, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithParserThrowsForMissingBothThumbprintsWhenSelfSigned()
    {
        //arrange
        DeviceParser parser = new DeviceParser();
        parser.setDeviceId("someDevice");
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.SELF_SIGNED);
        parser.getAuthenticationParser().setThumbprint(new X509ThumbprintParser());

        //act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_017: [If the provided parser uses SELF_SIGNED authentication and is missing one or both thumbprint, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithParserThrowsForMissingPrimaryThumbprintsWhenSelfSigned()
    {
        //arrange
        DeviceParser parser = new DeviceParser();
        parser.setDeviceId("someDevice");
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.SELF_SIGNED);
        parser.getAuthenticationParser().setThumbprint(new X509ThumbprintParser());
        parser.getAuthenticationParser().getThumbprint().setSecondaryThumbprint(SAMPLE_THUMBPRINT);

        //act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_017: [If the provided parser uses SELF_SIGNED authentication and is missing one or both thumbprint, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithParserThrowsForMissingSecondaryThumbprintsWhenSelfSigned()
    {
        //arrange
        DeviceParser parser = new DeviceParser();
        parser.setDeviceId("someDevice");
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.SELF_SIGNED);
        parser.getAuthenticationParser().setThumbprint(new X509ThumbprintParser());
        parser.getAuthenticationParser().getThumbprint().setPrimaryThumbprint(SAMPLE_THUMBPRINT);

        //act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_019: [If this device uses sas authentication, but does not have a primary and secondary symmetric key saved, an IllegalStateException shall be thrown.]
    @Test (expected = IllegalStateException.class)
    public void toParserIllegalStateThrownWhenUsingSASAuthenticationWithoutSymmetricKeySaved()
    {
        //arrange
        Device device = Device.createDevice("someDevice", AuthenticationType.SAS);
        AuthenticationMechanism authenticationMechanism = new AuthenticationMechanism(new SymmetricKey());
        Deencapsulation.setField(authenticationMechanism.getSymmetricKey(), "primaryKey", null);
        Deencapsulation.setField(device, "authentication", authenticationMechanism);

        //act
        reflectivelyInvokeToDeviceParser(device);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_019: [If this device uses sas authentication, but does not have a primary and secondary symmetric key saved, an IllegalStateException shall be thrown.]
    @Test (expected = IllegalStateException.class)
    public void toParserIllegalStateThrownWhenUsingSASAuthenticationWithoutPrimaryKeySaved()
    {
        //arrange
        Device device = Device.createDevice("someDevice", AuthenticationType.SAS);
        SymmetricKey symmetricKey = new SymmetricKey();
        Deencapsulation.setField(symmetricKey, "primaryKey", null);
        AuthenticationMechanism authenticationMechanism = new AuthenticationMechanism(symmetricKey);
        Deencapsulation.setField(device, "authentication", authenticationMechanism);

        //act
        reflectivelyInvokeToDeviceParser(device);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_020: [If this device uses self signed authentication, but does not have a primary and secondary thumbprint saved, an IllegalStateException shall be thrown.]
    @Test (expected = IllegalStateException.class)
    public void toParserIllegalStateThrownWhenUsingSelfSignedAuthenticationWithoutThumbprintSaved()
    {
        //arrange
        Device device = Device.createDevice("someDevice", AuthenticationType.SELF_SIGNED);
        AuthenticationMechanism authenticationMechanism = new AuthenticationMechanism(SAMPLE_THUMBPRINT, SAMPLE_THUMBPRINT);
        X509Thumbprint thumbprint = Deencapsulation.getField(authenticationMechanism, "thumbprint");
        Deencapsulation.setField(thumbprint, "primaryThumbprint", null);
        Deencapsulation.setField(device, "authentication", authenticationMechanism);

        //act
        reflectivelyInvokeToDeviceParser(device);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_DEVICE_34_020: [If this device uses self signed authentication, but does not have a primary and secondary thumbprint saved, an IllegalStateException shall be thrown.]
    @Test (expected = IllegalStateException.class)
    public void toParserIllegalStateThrownWhenUsingSelfSignedAuthenticationWithoutPrimaryThumbprintSaved()
    {
        //arrange
        Device device = Device.createDevice("someDevice", AuthenticationType.SELF_SIGNED);
        X509Thumbprint thumbprint = Deencapsulation.newInstance(X509Thumbprint.class);
        Deencapsulation.setField(thumbprint, "primaryThumbprint", null);
        AuthenticationMechanism authentication = new AuthenticationMechanism(SAMPLE_THUMBPRINT,SAMPLE_THUMBPRINT);
        Deencapsulation.setField(authentication, "thumbprint", thumbprint);
        Deencapsulation.setField(device, "authentication", authentication);

        //act
        reflectivelyInvokeToDeviceParser(device);
    }

    /**
     * Use reflection to call the Device constructor that takes a DeviceParser object as its only argument
     * @param parser the parser to pass into the constructor
     * @return the created Device instance
     */
    private Device reflectivelyInvokeDeviceParserConstructor(DeviceParser parser)
    {
        return Deencapsulation.newInstance(Device.class, new Class[] { DeviceParser.class }, parser);
    }

    /**
     * Uses reflection to invoke the Device method "toDeviceParser()"
     * @param device the device to invoke this method on
     * @return DeviceParser instance result of the invocation
     */
    private DeviceParser reflectivelyInvokeToDeviceParser(Device device)
    {
        return Deencapsulation.invoke(device, "toDeviceParser");
    }
}