/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.deps.serializer.*;
import com.microsoft.azure.sdk.iot.deps.twin.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.DeviceConnectionState;
import com.microsoft.azure.sdk.iot.service.DeviceStatus;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;
import mockit.*;
import org.junit.Test;

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

    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_12_001: [The Device class shall have the following properties:
    // deviceId, Etag, Status, StatusReason, StatusUpdatedTime, ConnectionState, CloudToDeviceMessageCount
    // ConnectionStateUpdatedTime, LastActivityTime, symmetricKey, thumbprint, authentication]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void device_get_all_properties()
    {
        // Arrange
        String deviceId = "xxx-device";
        SymmetricKey expectedSymmetricKey = new SymmetricKey();
        String expectedPrimaryThumbprint = "0000000000000000000000000000000000000000";
        String expectedSecondaryThumbprint = "1111111111111111111111111111111111111111";

        // Act
        Device device = Device.createFromId(deviceId, null, null);

        device.setSymmetricKey(expectedSymmetricKey);
        assertEquals(expectedSymmetricKey, device.getSymmetricKey());

        device.setThumbprintFinal(expectedPrimaryThumbprint, expectedSecondaryThumbprint);
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
        DeviceCapabilities cap = new DeviceCapabilities();

        cap.setIotEdge(true);
        device.setCapabilities(cap);
        assertEquals((Boolean)true, device.getCapabilities().isIotEdge());

        device.setForceUpdate(true);
        device.setForceUpdate(null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_12_002: [The function shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void createFromId_input_null()
    {
        // Arrange
        String deviceId = null;

        // Act
        Device.createFromId(deviceId, null, null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_12_002: [The constructor shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void createFromId_input_empty()
    {
        // Arrange
        String deviceId = "";

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

    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_12_003: [The constructor shall create a new instance of Device using the given deviceId and return with it]
    @Test
    public void createFromId_success()
    {
        // Arrange
        String deviceId = "xxx-device";
        new Expectations()
        {
            {
                Deencapsulation.newInstance(Device.class, new Class[]{String.class, DeviceStatus.class, SymmetricKey.class},
                        deviceId, null, null);
            }
        };

        // Act
        Device device = Device.createFromId(deviceId, null, null);

        // Assert
        assertNotEquals(device, null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_28_001: [The constructor shall set the deviceId, status and symmetricKey.]
    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_12_006: [The constructor shall initialize all properties to default values]
    @Test
    public void constructor_create_initialize_properties()
    {
        // Arrange
        String deviceId = "xxx-device";
        DeviceStatus expectedDeviceStatus = DeviceStatus.Enabled;
        String utcTimeDefault = "0001-01-01T00:00:00";
        String offsetTimeDefault = "0001-01-01T00:00:00-00:00";

        // Act
        Device device = Deencapsulation.newInstance(Device.class, new Class[]{String.class, DeviceStatus.class, SymmetricKey.class},
                deviceId, null, null);

        // Assert
        assertEquals(deviceId, device.getDeviceId());
        assertNotEquals(null, device.getSymmetricKey());
        assertEquals("", device.getGenerationId());
        assertEquals("", device.geteTag());
        assertEquals(DeviceStatus.Enabled, device.getStatus());
        assertEquals("", device.getStatusReason());
        assertEquals(utcTimeDefault, device.getStatusUpdatedTime());
        assertEquals(DeviceConnectionState.Disconnected, device.getConnectionState());
        assertEquals(utcTimeDefault, device.getConnectionStateUpdatedTime());
        assertEquals(offsetTimeDefault, device.getLastActivityTime());
        assertEquals(0, device.getCloudToDeviceMessageCount());
        assertEquals(null, device.getCapabilities());
    }

    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_28_001: [The constructor shall set the deviceId, status and symmetricKey.]
    @Test
    public void constructor_sets_status_and_symmetrickey() throws NoSuchAlgorithmException
    {
        // Arrange
        String deviceId = "xxx-device";
        DeviceStatus expectedDeviceStatus = DeviceStatus.Disabled;
        SymmetricKey expectedSymmetricKey = new SymmetricKey();

        // Act
        Device device = Deencapsulation.newInstance(Device.class, new Class[]{String.class, DeviceStatus.class, SymmetricKey.class},
                deviceId, expectedDeviceStatus, expectedSymmetricKey);

        // Assert
        assertEquals(deviceId, device.getDeviceId());
        assertEquals(expectedDeviceStatus, device.getStatus());
        assertEquals(expectedSymmetricKey, device.getSymmetricKey());
    }

    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_28_002: [The constructor shall set the deviceId and symmetricKey.]
    // Tests_SRS_SERVICE_SDK_JAVA_DEVICE_28_003: [The constructor shall initialize all properties to default values]
    @Test
    public void constructor2_create_initialize_properties()
    {
        // Arrange
        String deviceId = "xxx-device";
        String utcTimeDefault = "0001-01-01T00:00:00";
        String offsetTimeDefault = "0001-01-01T00:00:00-00:00";

        // Act
        Device device = Deencapsulation.newInstance(Device.class, new Class[]{String.class, AuthenticationType.class},
                deviceId, AuthenticationType.SAS);

        // Assert
        assertEquals(deviceId, device.getDeviceId());
        assertNotNull( device.getSymmetricKey());
        assertEquals("", device.getGenerationId());
        assertEquals("", device.geteTag());
        assertEquals(DeviceStatus.Enabled, device.getStatus());
        assertEquals("", device.getStatusReason());
        assertEquals(utcTimeDefault, device.getStatusUpdatedTime());
        assertEquals(DeviceConnectionState.Disconnected, device.getConnectionState());
        assertEquals(utcTimeDefault, device.getConnectionStateUpdatedTime());
        assertEquals(offsetTimeDefault, device.getLastActivityTime());
        assertEquals(0, device.getCloudToDeviceMessageCount());
        assertEquals(null, device.getCapabilities());
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
        Boolean expectedCapabilities = false;
        String scope = "scope";

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
        Deencapsulation.setField(device, "capabilities", new DeviceCapabilities());
        Deencapsulation.setField(device, "scope", scope);
        device.getCapabilities().setIotEdge(expectedCapabilities);

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
        assertEquals("\"" + expectedETag + "\"", parser.geteTag());
        assertEquals(expectedGenerationId, parser.getGenerationId());
        assertEquals(ParserUtility.getDateTimeUtc(expectedLastActivityTime), parser.getLastActivityTime());
        assertEquals(expectedCapabilities, parser.getCapabilities().getIotEdge());
        assertEquals(parser.getScope(), scope);
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
        String scope = "scope";
        DeviceParser parserSAS = new DeviceParser();
        parserSAS.setAuthenticationParser(Deencapsulation.newInstance(AuthenticationParser.class));
        parserSAS.getAuthenticationParser().setType(AuthenticationTypeParser.SAS);
        parserSAS.getAuthenticationParser().setSymmetricKey(new SymmetricKeyParser(SAMPLE_KEY, SAMPLE_KEY));
        parserSAS.setDeviceId("deviceSAS");
        parserSAS.setScope(scope);

        // act
        Device deviceSAS = reflectivelyInvokeDeviceParserConstructor(parserSAS);

        // assert
        assertNull(deviceSAS.getPrimaryThumbprint());
        assertNull(deviceSAS.getSecondaryThumbprint());
        assertNotNull(deviceSAS.getSymmetricKey());
        assertEquals(AuthenticationType.SAS, deviceSAS.getAuthenticationType());
        assertEquals(scope, deviceSAS.getScope());
    }

    @Test
    public void setScopeSetsScope()
    {
        //arrange
        String scope = "scope";
        Device device = Device.createDevice("device", AuthenticationType.SAS);

        //act
        device.setScope(scope);

        //assert
        String actualScope = Deencapsulation.getField(device, "scope");
        assertEquals(scope, actualScope);
    }

    @Test
    public void getScopeGetsScope()
    {
        //arrange
        String scope = "scope";
        Device device = Device.createDevice("device", AuthenticationType.SAS);
        Deencapsulation.setField(device, "scope", scope);

        //act
        String actualScope = device.getScope();

        //assert
        assertEquals(scope, actualScope);
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