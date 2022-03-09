package com.microsoft.azure.sdk.iot.service.registry;

import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;
import com.microsoft.azure.sdk.iot.service.ParserUtility;
import com.microsoft.azure.sdk.iot.service.registry.serializers.AuthenticationParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.AuthenticationTypeParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.RegistryIdentityParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.SymmetricKeyParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.X509ThumbprintParser;
import mockit.Deencapsulation;
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
public class RegistryIdentityTest
{
    private static final String SAMPLE_THUMBPRINT = "0000000000000000000000000000000000000000";
    private static final String SAMPLE_KEY = "000000000000000000000000";

    // Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_12_001: [The RegistryIdentity class shall have the following properties: deviceId, Etag,
    // SymmetricKey, ConnectionState, ConnectionStateUpdatedTime, LastActivityTime, symmetricKey, thumbprint, authentication]
    @Test(expected = IllegalArgumentException.class)
    public void device_get_all_properties()
    {
        // Arrange
        String deviceId = "xxx-device";
        SymmetricKey expectedSymmetricKey = new SymmetricKey();
        String expectedPrimaryThumbprint = "0000000000000000000000000000000000000000";
        String expectedSecondaryThumbprint = "1111111111111111111111111111111111111111";

        // Act
        RegistryIdentity device = Deencapsulation.newInstance(RegistryIdentity.class, deviceId, null, null);

        device.setSymmetricKey(expectedSymmetricKey);
        assertEquals(expectedSymmetricKey, device.getSymmetricKey());

        device.setThumbprint(expectedPrimaryThumbprint, expectedSecondaryThumbprint);
        assertEquals(expectedPrimaryThumbprint, device.getPrimaryThumbprint());
        assertEquals(expectedSecondaryThumbprint, device.getSecondaryThumbprint());

        device.getPrimaryThumbprint();
        device.getSecondaryThumbprint();
        device.getDeviceId();
        device.getGenerationId();
        device.getPrimaryKey();
        device.getSecondaryKey();
        device.getETag();
        device.getConnectionState();
        device.getConnectionStateUpdatedTime();
        device.getLastActivityTime();
        device.getCloudToDeviceMessageCount();
    }

    // Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_12_004: [The constructor shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_string_null() throws IllegalArgumentException
    {
        // Arrange
        String deviceId = null;

        // Act
        Deencapsulation.newInstance(RegistryIdentity.class, deviceId, null, null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_12_004: [The constructor shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test (expected = IllegalArgumentException.class)
    public void constructor_string_empty() throws NoSuchAlgorithmException
    {
        // Arrange
        String deviceId = "";

        // Act
        Deencapsulation.newInstance(RegistryIdentity.class, deviceId, null, null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_12_005: [If the input symmetric key is empty, the constructor shall create
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
        RegistryIdentity device = new RegistryIdentity(deviceId, AuthenticationType.SAS);

        // Assert
        assertNotEquals(device.getSymmetricKey(), null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_15_007: [The constructor shall store
    // the input device status and symmetric key into a member variable]
    @Test
    public void constructor_sets_status_and_symmetrickey() throws NoSuchAlgorithmException
    {
        // Arrange
        String deviceId = "xxx-device";
        SymmetricKey expectedSymmetricKey = new SymmetricKey();

        // Act
        RegistryIdentity device = new RegistryIdentity(deviceId, AuthenticationType.SAS);
        device.setSymmetricKey(expectedSymmetricKey);

        // Assert
        assertEquals(expectedSymmetricKey, device.getSymmetricKey());
    }

    // Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_12_006: [The constructor shall initialize all properties to default value]
    @Test
    public void constructor_initialize_properties()
    {
        // Arrange
        String utcTimeDefault = "0001-01-01T00:00:00";
        String offsetTimeDefault = "0001-01-01T00:00:00-00:00";
        String deviceId = "xxx-device";

        // Act
        RegistryIdentity device = new RegistryIdentity(deviceId, AuthenticationType.SAS);

        // Assert
        assertNotEquals(null, device);
        assertNotEquals(device.getSymmetricKey(), null);

        assertEquals("xxx-device", device.getDeviceId());
        assertEquals("", device.getGenerationId());
        assertEquals("", device.getETag());
        assertEquals(DeviceConnectionState.Disconnected, device.getConnectionState());
        assertEquals(utcTimeDefault, device.getConnectionStateUpdatedTime());
        assertEquals(offsetTimeDefault, device.getLastActivityTime());
        assertEquals(0, device.getCloudToDeviceMessageCount());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_018: [This method shall return a new instance of a RegistryIdentityParser object that is populated using the properties of this.]
    @Test
    public void conversionToDeviceParser()
    {
        // arrange
        String expectedDeviceId = "deviceCA";
        boolean expectedForceUpdate = false;
        int expectedCloudToDeviceMessageCount = 23;
        DeviceConnectionState expectedConnectionState = DeviceConnectionState.Connected;
        String expectedConnectionStateUpdatedTime = "2001-09-09T09:09:09";
        String expectedETag = "1234";
        String expectedGenerationId = "5678";
        String expectedLastActivityTime = "2001-09-09T09:09:09";

        RegistryIdentity device = Deencapsulation.newInstance(RegistryIdentity.class, expectedDeviceId, AuthenticationType.CERTIFICATE_AUTHORITY);
        Deencapsulation.setField(device, "cloudToDeviceMessageCount", expectedCloudToDeviceMessageCount);
        Deencapsulation.setField(device, "connectionState", expectedConnectionState);
        Deencapsulation.setField(device, "connectionStateUpdatedTime", expectedConnectionStateUpdatedTime);
        Deencapsulation.setField(device, "eTag", expectedETag);
        Deencapsulation.setField(device, "generationId", expectedGenerationId);
        Deencapsulation.setField(device, "lastActivityTime", expectedLastActivityTime);

        // act
        RegistryIdentityParser parser = reflectivelyInvokeToDeviceParser(device);

        // assert
        assertEquals(AuthenticationTypeParser.CERTIFICATE_AUTHORITY, parser.getAuthenticationParser().getType());
        assertEquals(expectedCloudToDeviceMessageCount, parser.getCloudToDeviceMessageCount());
        assertEquals(expectedConnectionState.toString(), parser.getConnectionState());
        assertEquals(expectedDeviceId, parser.getDeviceId());
        assertEquals(expectedETag, parser.getETag());
        assertEquals(expectedGenerationId, parser.getGenerationId());
        assertEquals(ParserUtility.getDateTimeUtc(expectedLastActivityTime), parser.getLastActivityTime());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_018: [This method shall return a new instance of a RegistryIdentityParser object that is populated using the properties of this.]
    @Test
    public void conversionToDeviceParserWithSelfSignedAuthentication()
    {
        // arrange
        RegistryIdentity deviceSelf = Deencapsulation.newInstance(RegistryIdentity.class, "deviceSelf", AuthenticationType.SELF_SIGNED);

        // act
        RegistryIdentityParser parserSelf = reflectivelyInvokeToDeviceParser(deviceSelf);

        // assert
        assertEquals(AuthenticationTypeParser.SELF_SIGNED, parserSelf.getAuthenticationParser().getType());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_018: [This method shall return a new instance of a RegistryIdentityParser object that is populated using the properties of this.]
    @Test
    public void conversionToDeviceParserWithSASAuthentication()
    {
        // arrange
        RegistryIdentity deviceSAS =  Deencapsulation.newInstance(RegistryIdentity.class, "deviceSAS", AuthenticationType.SAS);

        // act
        RegistryIdentityParser parserSAS = reflectivelyInvokeToDeviceParser(deviceSAS);

        // assert
        assertEquals(AuthenticationTypeParser.SAS, parserSAS.getAuthenticationParser().getType());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_014: [This constructor shall create a new Device object using the values within the provided parser.]
    @Test
    public void conversionFromDeviceParserWithCertificateAuthorityAuthentication()
    {
        // arrange
        RegistryIdentityParser parserCA = new RegistryIdentityParser();
        parserCA.setAuthenticationParser(Deencapsulation.newInstance(AuthenticationParser.class));
        parserCA.getAuthenticationParser().setType(AuthenticationTypeParser.CERTIFICATE_AUTHORITY);
        parserCA.setDeviceId("deviceCA");

        // act
        RegistryIdentity deviceCA = reflectivelyInvokeDeviceParserConstructor(parserCA);

        // assert
        assertNull(deviceCA.getPrimaryThumbprint());
        assertNull(deviceCA.getSecondaryThumbprint());
        assertNull(deviceCA.getSymmetricKey());
        assertEquals(AuthenticationType.CERTIFICATE_AUTHORITY, deviceCA.getAuthenticationType());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_014: [This constructor shall create a new Device object using the values within the provided parser.]
    @Test
    public void conversionFromDeviceParserWithSelfSignedAuthentication()
    {
        // arrange
        RegistryIdentityParser parserSelf = new RegistryIdentityParser();
        parserSelf.setAuthenticationParser(Deencapsulation.newInstance(AuthenticationParser.class));
        parserSelf.getAuthenticationParser().setType(AuthenticationTypeParser.SELF_SIGNED);
        parserSelf.getAuthenticationParser().setThumbprint(new X509ThumbprintParser(SAMPLE_THUMBPRINT, SAMPLE_THUMBPRINT));
        parserSelf.setDeviceId("deviceSelf");

        // act
        RegistryIdentity deviceSelf = reflectivelyInvokeDeviceParserConstructor(parserSelf);

        // assert
        assertNull(deviceSelf.getSymmetricKey());
        assertNotNull(deviceSelf.getPrimaryThumbprint());
        assertNotNull(deviceSelf.getSecondaryThumbprint());
        assertEquals(AuthenticationType.SELF_SIGNED, deviceSelf.getAuthenticationType());
    }


    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_014: [This constructor shall create a new Device object using the values within the provided parser.]
    @Test
    public void conversionFromDeviceParserWithSASAuthentication()
    {
        // arrange
        RegistryIdentityParser parserSAS = new RegistryIdentityParser();
        parserSAS.setAuthenticationParser(Deencapsulation.newInstance(AuthenticationParser.class));
        parserSAS.getAuthenticationParser().setType(AuthenticationTypeParser.SAS);
        parserSAS.getAuthenticationParser().setSymmetricKey(new SymmetricKeyParser(SAMPLE_KEY, SAMPLE_KEY));
        parserSAS.setDeviceId("deviceSAS");

        // act
        RegistryIdentity deviceSAS = reflectivelyInvokeDeviceParserConstructor(parserSAS);

        // assert
        assertNull(deviceSAS.getPrimaryThumbprint());
        assertNull(deviceSAS.getSecondaryThumbprint());
        assertNotNull(deviceSAS.getSymmetricKey());
        assertEquals(AuthenticationType.SAS, deviceSAS.getAuthenticationType());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEEVICE_34_015: [If the provided parser is missing a value for its authentication or its device Id, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void conversionFromDeviceWithoutDeviceIdThrowsIllegalArgumentException()
    {
        // arrange
        RegistryIdentityParser parser = new RegistryIdentityParser();
        parser.setAuthenticationParser(Deencapsulation.newInstance(AuthenticationParser.class));
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.CERTIFICATE_AUTHORITY);

        // act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_015: [If the provided parser is missing a value for its authentication or its device Id, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void conversionFromDeviceWithoutAuthenticationTypeThrowsIllegalArgumentException()
    {
        // arrange
        RegistryIdentityParser parser = new RegistryIdentityParser();
        parser.setDeviceId("someDevice");

        // act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_011: [If the provided authenticationType is certificate authority, no symmetric key shall be generated and no thumbprint shall be generated]
    @Test
    public void deviceConstructorWithCertificateAuthorityGeneratesKeysCorrectly()
    {
        // act
        RegistryIdentity device = Deencapsulation.newInstance(RegistryIdentity.class, new Class[] { String.class, AuthenticationType.class }, "someDevice", AuthenticationType.CERTIFICATE_AUTHORITY);

        // assert
        assertNull(device.getPrimaryThumbprint());
        assertNull(device.getSecondaryThumbprint());
        assertNull(device.getSymmetricKey());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_012: [If the provided authenticationType is SAS, a symmetric key shall be generated but no thumbprint shall be generated]
    @Test
    public void deviceConstructorWithSharedAccessSignatureGeneratesKeysCorrectly()
    {
        // act
        RegistryIdentity device = Deencapsulation.newInstance(RegistryIdentity.class, new Class[] { String.class, AuthenticationType.class }, "someDevice", AuthenticationType.SAS);

        // assert
        assertNull(device.getPrimaryThumbprint());
        assertNull(device.getSecondaryThumbprint());
        assertNotNull(device.getSymmetricKey());
        assertNotNull(device.getPrimaryKey());
        assertNotNull(device.getSecondaryKey());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_013: [If the provided authenticationType is self signed, a thumbprint shall be generated but no symmetric key shall be generated]
    @Test
    public void deviceConstructorWithSelfSignedGeneratesKeysCorrectly()
    {
        // act
        RegistryIdentity device = Deencapsulation.newInstance(RegistryIdentity.class, new Class[] { String.class, AuthenticationType.class }, "someDevice", AuthenticationType.SELF_SIGNED);

        // assert
        assertNotNull(device.getPrimaryThumbprint());
        assertNotNull(device.getSecondaryThumbprint());
        assertNull(device.getSymmetricKey());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_009: [The function shall throw IllegalArgumentException if the provided deviceId or authenticationType is empty or null.]
    @Test (expected = IllegalArgumentException.class)
    public void createDeviceThrowsIllegalArgumentExceptionForNullDeviceId()
    {
        //act
        Deencapsulation.newInstance(RegistryIdentity.class, new Class[] { String.class, AuthenticationType.class }, null, AuthenticationType.CERTIFICATE_AUTHORITY);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_009: [The function shall throw IllegalArgumentException if the provided deviceId or authenticationType is empty or null.]
    @Test (expected = IllegalArgumentException.class)
    public void createDeviceThrowsIllegalArgumentExceptionForEmptyDeviceId()
    {
        //act
        Deencapsulation.newInstance(RegistryIdentity.class, new Class[] { String.class, AuthenticationType.class }, "", AuthenticationType.CERTIFICATE_AUTHORITY);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_016: [If the provided parser uses SAS authentication and is missing one or both symmetric keys, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithParserThrowsForMissingSymmetricKeyWhenSASAuthenticated()
    {
        //arrange
        RegistryIdentityParser parser = new RegistryIdentityParser();
        parser.setDeviceId("someDevice");
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.SAS);

        //act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_016: [If the provided parser uses SAS authentication and is missing one or both symmetric keys, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithParserThrowsForMissingKeysWhenSASAuthenticated()
    {
        //arrange
        RegistryIdentityParser parser = new RegistryIdentityParser();
        parser.setDeviceId("someDevice");
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.SAS);
        parser.getAuthenticationParser().setSymmetricKey(new SymmetricKeyParser());

        //act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_016: [If the provided parser uses SAS authentication and is missing one or both symmetric keys, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithParserThrowsForMissingPrimaryKeyWhenSASAuthenticated()
    {
        //arrange
        RegistryIdentityParser parser = new RegistryIdentityParser();
        parser.setDeviceId("someDevice");
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.SAS);
        parser.getAuthenticationParser().setSymmetricKey(new SymmetricKeyParser());
        parser.getAuthenticationParser().getSymmetricKey().setSecondaryKey(SAMPLE_KEY);

        //act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_016: [If the provided parser uses SAS authentication and is missing one or both symmetric keys, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithParserThrowsForMissingSecondaryKeyWhenSASAuthenticated()
    {
        //arrange
        RegistryIdentityParser parser = new RegistryIdentityParser();
        parser.setDeviceId("someDevice");
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.SAS);
        parser.getAuthenticationParser().setSymmetricKey(new SymmetricKeyParser());
        parser.getAuthenticationParser().getSymmetricKey().setPrimaryKey(SAMPLE_KEY);

        //act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_017: [If the provided parser uses SELF_SIGNED authentication and is missing one or both thumbprint, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithParserThrowsForMissingX509ThumbprintWhenSelfSigned()
    {
        //arrange
        RegistryIdentityParser parser = new RegistryIdentityParser();
        parser.setDeviceId("someDevice");
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.SELF_SIGNED);

        //act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_017: [If the provided parser uses SELF_SIGNED authentication and is missing one or both thumbprint, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithParserThrowsForMissingBothThumbprintsWhenSelfSigned()
    {
        //arrange
        RegistryIdentityParser parser = new RegistryIdentityParser();
        parser.setDeviceId("someDevice");
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.SELF_SIGNED);
        parser.getAuthenticationParser().setThumbprint(new X509ThumbprintParser());

        //act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_017: [If the provided parser uses SELF_SIGNED authentication and is missing one or both thumbprint, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithParserThrowsForMissingPrimaryThumbprintsWhenSelfSigned()
    {
        //arrange
        RegistryIdentityParser parser = new RegistryIdentityParser();
        parser.setDeviceId("someDevice");
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.SELF_SIGNED);
        parser.getAuthenticationParser().setThumbprint(new X509ThumbprintParser());
        parser.getAuthenticationParser().getThumbprint().setSecondaryThumbprint(SAMPLE_THUMBPRINT);

        //act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_BASEDEVICE_34_017: [If the provided parser uses SELF_SIGNED authentication and is missing one or both thumbprint, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithParserThrowsForMissingSecondaryThumbprintsWhenSelfSigned()
    {
        //arrange
        RegistryIdentityParser parser = new RegistryIdentityParser();
        parser.setDeviceId("someDevice");
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.SELF_SIGNED);
        parser.getAuthenticationParser().setThumbprint(new X509ThumbprintParser());
        parser.getAuthenticationParser().getThumbprint().setPrimaryThumbprint(SAMPLE_THUMBPRINT);

        //act
        reflectivelyInvokeDeviceParserConstructor(parser);
    }

    /**
     * Use reflection to call the Device constructor that takes a RegistryIdentityParser object as its only argument
     * @param parser the parser to pass into the constructor
     * @return the created Device instance
     */
    private RegistryIdentity reflectivelyInvokeDeviceParserConstructor(RegistryIdentityParser parser)
    {
        return Deencapsulation.newInstance(RegistryIdentity.class, new Class[] { RegistryIdentityParser.class }, parser);
    }

    /**
     * Uses reflection to invoke the Device method "toRegistryIdentityParser()"
     * @param device the device to invoke this method on
     * @return RegistryIdentityParser instance result of the invocation
     */
    private RegistryIdentityParser reflectivelyInvokeToDeviceParser(RegistryIdentity device)
    {
        return Deencapsulation.invoke(device, "toRegistryIdentityParser");
    }
}