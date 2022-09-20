/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.registry;

import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;
import com.microsoft.azure.sdk.iot.service.ParserUtility;
import com.microsoft.azure.sdk.iot.service.registry.serializers.AuthenticationParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.AuthenticationTypeParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.RegistryIdentityParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.SymmetricKeyParser;
import com.microsoft.azure.sdk.iot.service.registry.serializers.X509ThumbprintParser;
import mockit.*;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

public class ModuleTest
{
    private static final String SAMPLE_THUMBPRINT = "0000000000000000000000000000000000000000";
    private static final String SAMPLE_KEY = "000000000000000000000000";

    // Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_001: [The Module class shall have the following properties: id, deviceId,
    // generationId, Etag, ConnectionState, ConnectionStateUpdatedTime, LastActivityTime, cloudToDeviceMessageCount,
    // authentication, managedBy.]
    // Assert
    @Test
    public void module_get_all_properties()
    {
        // Arrange
        String deviceId = "xxx-device";
        String moduleId = "xxx-module";
        SymmetricKey expectedSymmetricKey = new SymmetricKey();
        String expectedPrimaryThumbprint = "0000000000000000000000000000000000000000";
        String expectedSecondaryThumbprint = "1111111111111111111111111111111111111111";

        // Act
        Module module = new Module(deviceId, moduleId);

        module.setSymmetricKey(expectedSymmetricKey);
        assertEquals(expectedSymmetricKey, module.getSymmetricKey());

        module.setThumbprint(expectedPrimaryThumbprint, expectedSecondaryThumbprint);
        assertEquals(expectedPrimaryThumbprint, module.getPrimaryThumbprint());
        assertEquals(expectedSecondaryThumbprint, module.getSecondaryThumbprint());

        module.getId();
        module.getManagedBy();
        module.getPrimaryThumbprint();
        module.getSecondaryThumbprint();
        module.getDeviceId();
        module.getGenerationId();
        module.getPrimaryKey();
        module.getSecondaryKey();
        module.getETag();
        module.getConnectionState();
        module.getConnectionStateUpdatedTime();
        module.getLastActivityTime();
        module.getCloudToDeviceMessageCount();
    }

    // Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_002: [The function shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test(expected = IllegalArgumentException.class)
    public void createFromId_deviceId_null()
    {
        // Act
        new Module(null, "xxx");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_002: [The function shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test(expected = IllegalArgumentException.class)
    public void createFromId_deviceId_empty()
    {
        // Act
        new Module("", "xxx");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_002: [The function shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test(expected = IllegalArgumentException.class)
    public void createFromId_moduleId_null()
    {
        // Act
        new Module("xxx", null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_002: [The function shall throw IllegalArgumentException if the input string is empty or null]
    // Assert
    @Test(expected = IllegalArgumentException.class)
    public void createFromId_moduleId_empty()
    {
        // Act
        new Module(null, "");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_003: [The function shall create a new instance
    // of Module using the given moduleId for device with deviceId and return it]
    // Assert
    @Test
    public void createFromId_success()
    {
        String deviceName = "device-xxx";
        String moduleName = "module-xxx";

        // Act
        Module module = new Module(deviceName, moduleName);

        // Assert
        assertNotNull(module);
        assertEquals(deviceName, module.getDeviceId());
        assertEquals(moduleName, module.getId());
        assertNotNull(module.getSymmetricKey());
    }

    // Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_004: [The function shall throw IllegalArgumentException if the provided deviceId, moduleId or authenticationType is empty or null.]
    // Assert
    @Test(expected = IllegalArgumentException.class)
    public void createModuleThrowsIllegalArgumentExceptionWhenDeviceIdIsNull()
    {
        // Act
        new Module(null, "module-xxx", AuthenticationType.CERTIFICATE_AUTHORITY);
    }

    // Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_004: [The function shall throw IllegalArgumentException if the provided deviceId, moduleId or authenticationType is empty or null.]
    // Assert
    @Test(expected = IllegalArgumentException.class)
    public void createModuleThrowsIllegalArgumentExceptionWhenDeviceIdIsEmpty()
    {
        // Act
        new Module("", "module-xxx", AuthenticationType.CERTIFICATE_AUTHORITY);
    }

    // Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_004: [The function shall throw IllegalArgumentException if the provided deviceId, moduleId or authenticationType is empty or null.]
    // Assert
    @Test(expected = IllegalArgumentException.class)
    public void createModuleThrowsIllegalArgumentExceptionWhenModuleIdIsNull()
    {
        // Act
        new Module("device-xxx", null, AuthenticationType.CERTIFICATE_AUTHORITY);
    }

    // Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_004: [The function shall throw IllegalArgumentException if the provided deviceId, moduleId or authenticationType is empty or null.]
    // Assert
    @Test(expected = IllegalArgumentException.class)
    public void createModuleThrowsIllegalArgumentExceptionWhenModuleIdIsEmpty()
    {
        // Act
        new Module("device-xxx", "", AuthenticationType.CERTIFICATE_AUTHORITY);
    }

    // Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_004: [The function shall throw IllegalArgumentException if the provided deviceId, moduleId or authenticationType is empty or null.]
    // Assert
    @Test(expected = IllegalArgumentException.class)
    public void createModuleThrowsIllegalArgumentExceptionWhenAuthenticationTypeIsNull()
    {
        // Act
        new Module("device-xxx", "module-xxx", null);
    }


    // Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_005: [The function shall create a new instance of Module using the given
    // moduleId for the device with deviceId and return it]
    @Test
    public void createModule_success()
    {
        // Arrange
        String deviceId = "device-xxx";
        String moduleId = "module-xxx";

        // Act
        Module module = new Module(deviceId, moduleId, AuthenticationType.SAS);

        // Assert
        assertNotNull(module);
        assertEquals(deviceId, module.getDeviceId());
        assertEquals(moduleId, module.getId());
        assertNotNull(module.getSymmetricKey());
    }

    // Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_006: [The function shall throw IllegalArgumentException if the input string is empty or null]
    @Test(expected = IllegalArgumentException.class)
    public void constructorIllegalArgumentExceptionWhenModuleIdIsNull()
    {
        // Act
        Deencapsulation.newInstance(Module.class, new Class[]{String.class, String.class, SymmetricKey.class},
                "device-xxx", null, null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_006: [The function shall throw IllegalArgumentException if the input string is empty or null]
    @Test(expected = IllegalArgumentException.class)
    public void constructorIllegalArgumentExceptionWhenDeviceIdIsNull()
    {
        // Act
        Deencapsulation.newInstance(Module.class, new Class[]{String.class, String.class, SymmetricKey.class},
                null, "moduleid", null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_007: [The constructor shall initialize all properties to default values]
    @Test
    public void constructorInitializePropertiesToDefault()
    {
        // Arrange
        String deviceId = "device-xxx";
        String moduleId = "module-xxx";
        String utcTimeDefault = "0001-01-01T00:00:00";
        String offsetTimeDefault = "0001-01-01T00:00:00-00:00";

        // Act
        Module module = new Module(deviceId, moduleId);

        // Assert
        assertEquals(deviceId, module.getDeviceId());
        assertEquals(moduleId, module.getId());
        assertNotNull(module.getSymmetricKey());
        assertEquals("", module.getGenerationId());
        assertEquals("", module.getETag());
        assertEquals(DeviceConnectionState.Disconnected, module.getConnectionState());
        assertEquals(utcTimeDefault, module.getConnectionStateUpdatedTime());
        assertEquals(offsetTimeDefault, module.getLastActivityTime());
        assertEquals("", module.getManagedBy());
    }

    // Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_008: [The function shall throw IllegalArgumentException if the input string is empty or null]
    @Test(expected = IllegalArgumentException.class)
    public void constructor2IllegalArgumentExceptionWhenModuleIdIsNull()
    {
        // Act
        Deencapsulation.newInstance(Module.class, new Class[]{String.class, String.class, AuthenticationType.class},
                "device-xxx", null, AuthenticationType.SAS);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_008: [The function shall throw IllegalArgumentException if the input string is empty or null]
    @Test(expected = IllegalArgumentException.class)
    public void constructor2IllegalArgumentExceptionWhenDeviceIdIsNull()
    {
        // Act
        Deencapsulation.newInstance(Module.class, new Class[]{String.class, String.class, AuthenticationType.class},
                null, "moduleid", AuthenticationType.SAS);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_008: [The function shall throw IllegalArgumentException if the input string is empty or null]
    @Test(expected = IllegalArgumentException.class)
    public void constructor2IllegalArgumentExceptionWhenAuthTypeIsNull()
    {
        // Act
        Deencapsulation.newInstance(Module.class, new Class[]{String.class, String.class, AuthenticationType.class},
                "deviceId", "moduleid", null);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_009: [The constructor shall initialize all properties to default values]
    @Test
    public void constructor2InitializePropertiesToDefault()
    {
        // Arrange
        String deviceId = "device-xxx";
        String moduleId = "module-xxx";
        String utcTimeDefault = "0001-01-01T00:00:00";
        String offsetTimeDefault = "0001-01-01T00:00:00-00:00";

        // Act
        Module module = Deencapsulation.newInstance(Module.class, new Class[]{String.class, String.class, AuthenticationType.class},
                deviceId, moduleId, AuthenticationType.SAS);

        // Assert
        assertEquals(deviceId, module.getDeviceId());
        assertEquals(moduleId, module.getId());
        assertNotNull(module.getSymmetricKey());
        assertEquals("", module.getGenerationId());
        assertEquals("", module.getETag());
        assertEquals(DeviceConnectionState.Disconnected, module.getConnectionState());
        assertEquals(utcTimeDefault, module.getConnectionStateUpdatedTime());
        assertEquals(offsetTimeDefault, module.getLastActivityTime());
        assertEquals(0, module.getCloudToDeviceMessageCount());
        assertEquals("", module.getManagedBy());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_010: [This method shall return a new instance of a RegistryIdentityParser object that is populated using the properties of this.]
    @Test
    public void conversionToDeviceParser()
    {
        // arrange
        String expectedDeviceId = "deviceCA";
        String expectedModuleId = "moduleCA";
        int expectedCloudToDeviceMessageCount = 23;
        DeviceConnectionState expectedConnectionState = DeviceConnectionState.Connected;
        String expectedConnectionStateUpdatedTime = "2001-09-09T09:09:09";
        String expectedETag = "1234";
        String expectedGenerationId = "5678";
        String expectedLastActivityTime = "2001-09-09T09:09:09";
        String expectedManagedBy = "jkik";

        Module module = new Module(expectedDeviceId, expectedModuleId, AuthenticationType.SAS);
        Deencapsulation.setField(module, "cloudToDeviceMessageCount", expectedCloudToDeviceMessageCount);
        Deencapsulation.setField(module, "connectionState", expectedConnectionState);
        Deencapsulation.setField(module, "connectionStateUpdatedTime", expectedConnectionStateUpdatedTime);
        Deencapsulation.setField(module, "eTag", expectedETag);
        Deencapsulation.setField(module, "generationId", expectedGenerationId);
        Deencapsulation.setField(module, "lastActivityTime", expectedLastActivityTime);
        Deencapsulation.setField(module, "managedBy", expectedManagedBy);

        // act
        RegistryIdentityParser parser = reflectivelyInvokeToDeviceParser(module);

        // assert
        assertEquals(AuthenticationTypeParser.SAS, parser.getAuthenticationParser().getType());
        assertEquals(expectedCloudToDeviceMessageCount, parser.getCloudToDeviceMessageCount());
        assertEquals(expectedConnectionState.toString(), parser.getConnectionState());
        assertEquals(ParserUtility.getDateTimeUtc(expectedConnectionStateUpdatedTime), parser.getConnectionStateUpdatedTime());
        assertEquals(expectedDeviceId, parser.getDeviceId());
        assertEquals(expectedModuleId, parser.getModuleId());
        assertEquals(expectedETag, parser.getETag());
        assertEquals(expectedGenerationId, parser.getGenerationId());
        assertEquals(ParserUtility.getDateTimeUtc(expectedLastActivityTime), parser.getLastActivityTime());
        assertEquals(expectedManagedBy, parser.getManagedBy());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_010: [This method shall return a new instance of a RegistryIdentityParser object that is populated using the properties of this.]
    @Test
    public void conversionToDeviceParserWithSelfSignedAuthentication()
    {
        // arrange
        Module moduleSelf = new Module("deviceSelf", "moduleSelf", AuthenticationType.selfSigned);

        // act
        RegistryIdentityParser parserSelf = reflectivelyInvokeToDeviceParser(moduleSelf);

        // assert
        assertEquals(AuthenticationTypeParser.SELF_SIGNED, parserSelf.getAuthenticationParser().getType());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_010: [This method shall return a new instance of a RegistryIdentityParser object that is populated using the properties of this.]
    @Test
    public void conversionToDeviceParserWithSASAuthentication()
    {
        // arrange
        Module moduleSAS = new Module("deviceSAS", "moduleSelf", AuthenticationType.SAS);

        // act
        RegistryIdentityParser parserSAS = reflectivelyInvokeToDeviceParser(moduleSAS);

        // assert
        assertEquals(AuthenticationTypeParser.SAS, parserSAS.getAuthenticationParser().getType());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_011: [This constructor shall create a new Module object using the values within the provided parser.]
    @Test
    public void conversionFromDeviceParserWithCertificateAuthorityAuthentication()
    {
        // arrange
        RegistryIdentityParser parserCA = new RegistryIdentityParser();
        parserCA.setAuthenticationParser(Deencapsulation.newInstance(AuthenticationParser.class));
        parserCA.getAuthenticationParser().setType(AuthenticationTypeParser.CERTIFICATE_AUTHORITY);
        parserCA.setDeviceId("deviceCA");
        parserCA.setModuleId("moduleCA");
        parserCA.setManagedBy("xyz");

        // act
        Module moduleCA = reflectivelyInvokeDeviceParserConstructor(parserCA);

        // assert
        assertNull(moduleCA.getPrimaryThumbprint());
        assertNull(moduleCA.getSecondaryThumbprint());
        assertNull(moduleCA.getSymmetricKey());
        assertEquals(AuthenticationType.CERTIFICATE_AUTHORITY, moduleCA.getAuthenticationType());
        assertEquals("xyz", moduleCA.getManagedBy());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_011: [This constructor shall create a new Module object using the values within the provided parser.]
    @Test
    public void conversionFromDeviceParserWithSelfSignedAuthentication()
    {
        // arrange
        RegistryIdentityParser parserSelf = new RegistryIdentityParser();
        parserSelf.setAuthenticationParser(Deencapsulation.newInstance(AuthenticationParser.class));
        parserSelf.getAuthenticationParser().setType(AuthenticationTypeParser.SELF_SIGNED);
        parserSelf.getAuthenticationParser().setThumbprint(new X509ThumbprintParser(SAMPLE_THUMBPRINT, SAMPLE_THUMBPRINT));
        parserSelf.setDeviceId("deviceSelf");
        parserSelf.setModuleId("moduleSelf");
        parserSelf.setManagedBy("xyz");

        // act
        Module moduleSelf = reflectivelyInvokeDeviceParserConstructor(parserSelf);

        // assert
        assertNull(moduleSelf.getSymmetricKey());
        assertNotNull(moduleSelf.getPrimaryThumbprint());
        assertNotNull(moduleSelf.getSecondaryThumbprint());
        assertEquals(AuthenticationType.selfSigned, moduleSelf.getAuthenticationType());
        assertEquals("xyz", moduleSelf.getManagedBy());
    }


    // Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_011: [This constructor shall create a new Module object using the values within the provided parser.]
    @Test
    public void conversionFromDeviceParserWithSASAuthentication()
    {
        // arrange
        RegistryIdentityParser parserSAS = new RegistryIdentityParser();
        parserSAS.setAuthenticationParser(Deencapsulation.newInstance(AuthenticationParser.class));
        parserSAS.getAuthenticationParser().setType(AuthenticationTypeParser.SAS);
        parserSAS.getAuthenticationParser().setSymmetricKey(new SymmetricKeyParser(SAMPLE_KEY, SAMPLE_KEY));
        parserSAS.setDeviceId("deviceSAS");
        parserSAS.setModuleId("moduleSelf");
        parserSAS.setManagedBy("xyz");

        // act
        Module moduleSAS = reflectivelyInvokeDeviceParserConstructor(parserSAS);

        // assert
        assertNull(moduleSAS.getPrimaryThumbprint());
        assertNull(moduleSAS.getSecondaryThumbprint());
        assertNotNull(moduleSAS.getSymmetricKey());
        assertEquals(AuthenticationType.SAS, moduleSAS.getAuthenticationType());
        assertEquals("xyz", moduleSAS.getManagedBy());
    }

    // Tests_SRS_SERVICE_SDK_JAVA_MODULE_28_011: [If the provided parser is missing a value for its moduleId, an IllegalArgumentException shall be thrown.]
    @Test(expected = IllegalArgumentException.class)
    public void conversionFromDeviceParserWithNoModuleId()
    {
        // arrange
        RegistryIdentityParser parserSAS = new RegistryIdentityParser();
        parserSAS.setAuthenticationParser(Deencapsulation.newInstance(AuthenticationParser.class));
        parserSAS.getAuthenticationParser().setType(AuthenticationTypeParser.SAS);
        parserSAS.getAuthenticationParser().setSymmetricKey(new SymmetricKeyParser(SAMPLE_KEY, SAMPLE_KEY));
        parserSAS.setDeviceId("deviceSAS");

        // act
        reflectivelyInvokeDeviceParserConstructor(parserSAS);
    }

    /**
     * Use reflection to call the Device constructor that takes a RegistryIdentityParser object as its only argument
     *
     * @param parser the parser to pass into the constructor
     * @return the created Device instance
     */
    private Module reflectivelyInvokeDeviceParserConstructor(RegistryIdentityParser parser)
    {
        return Deencapsulation.newInstance(Module.class, new Class[]{RegistryIdentityParser.class}, parser);
    }

    /**
     * Uses reflection to invoke the Device method "toRegistryIdentityParser()"
     *
     * @param module the device to invoke this method on
     * @return RegistryIdentityParser instance result of the invocation
     */
    private RegistryIdentityParser reflectivelyInvokeToDeviceParser(Module module)
    {
        return Deencapsulation.invoke(module, "toRegistryIdentityParser");
    }
}