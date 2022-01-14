/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.serializers.DeviceParser;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;
import lombok.Getter;

public class Module extends BaseDevice
{
    /**
     * Static create function
     * Creates module object using the given name.
     * If input symmetric key are null then they will be auto generated.
     *
     * @param deviceId - String containing the device name
     * @param moduleId - String containing the module name
     * @param symmetricKey - Device key. If parameter is null, then the key will be auto generated.
     * @return Module object
     * @throws IllegalArgumentException This exception is thrown if {@code deviceId} is {@code null} or empty.
     */
    public static Module createFromId(String deviceId, String moduleId, SymmetricKey symmetricKey)
            throws IllegalArgumentException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_002: [The function shall throw IllegalArgumentException if the input string is empty or null]
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("The provided device Id must not be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_002: [The function shall throw IllegalArgumentException if the input string is empty or null]
        if (Tools.isNullOrEmpty(moduleId))
        {
            throw new IllegalArgumentException("The provided module Id must not be null or empty");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_003: [The function shall create a new instance
        // of Module using the given moduleId for device with deviceId and return it]
        return new Module(deviceId, moduleId, symmetricKey);
    }

    /**
     * Static create function
     * Creates device object using the given name that will use a Certificate Authority signed certificate for authentication.
     *
     * @param deviceId - String containing the device name
     * @param moduleId - String containing the module name
     * @param authenticationType - The type of authentication used by this device.
     * @return Module object
     * @throws IllegalArgumentException This exception is thrown if {@code deviceId} or {@code moduleId} is {@code null} or empty.
     */
    public static Module createModule(String deviceId, String moduleId, AuthenticationType authenticationType)
        throws IllegalArgumentException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_004: [The function shall throw IllegalArgumentException if the provided deviceId, moduleId or authenticationType is empty or null.]
        if (Tools.isNullOrEmpty(deviceId))
        {
            throw new IllegalArgumentException("The provided device Id must not be null or empty");
        }

        if (Tools.isNullOrEmpty(moduleId))
        {
            throw new IllegalArgumentException("The provided module Id must not be null or empty");
        }

        if (authenticationType == null)
        {
            throw new IllegalArgumentException("The provided authentication type must not be null");
        }

        // Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_005: [The function shall create a new instance
        // of Module using the given moduleId for the device with deviceId and return it]
        return new Module(deviceId, moduleId, authenticationType);
    }

    /**
     * Create an Module instance using the given module name
     *
     * @param deviceId Name of the device (used as device id)
     * @param moduleId - ame of the module (used as module id)
     * @param symmetricKey - Device key. If parameter is null, then the key will be auto generated.
     * @throws IllegalArgumentException This exception is thrown if {@code deviceId} or {@code moduleId} is {@code null} or empty.
     */
    private Module(String deviceId, String moduleId, SymmetricKey symmetricKey)
            throws IllegalArgumentException
    {
        super(deviceId, symmetricKey);

        // Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_006: [The function shall throw IllegalArgumentException if the input string is empty or null]
        if (Tools.isNullOrEmpty(moduleId))
        {
            throw new IllegalArgumentException(moduleId);
        }

        // Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_007: [The constructor shall initialize all properties to default values]
        this.id = moduleId;
        this.managedBy = "";
    }

    /**
     * Create an Device instance using the given device name that uses a Certificate Authority signed certificate
     *
     * @param deviceId Name of the device (used as device id)
     * @param authenticationType - The type of authentication used by this device.
     */
    private Module(String deviceId, String moduleId, AuthenticationType authenticationType)
    {
        super(deviceId, authenticationType);

        // Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_008: [The function shall throw IllegalArgumentException if the input string is empty or null]
        if (Tools.isNullOrEmpty(moduleId))
        {
            throw new IllegalArgumentException(moduleId);
        }

        // Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_009: [The constructor shall initialize all properties to default values]
        this.id = moduleId;
        this.managedBy = "";
    }

    // Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_001: [The Module class shall have the following properties: id, deviceId,
    // generationId, Etag, ConnectionState, ConnectionStateUpdatedTime, LastActivityTime, cloudToDeviceMessageCount,
    // authentication, managedBy

    /**
     * Module name
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     */
    @Getter
    private final String id;

    /**
     * Specifies the module's managed by owner
     */
    @Getter
    private final String managedBy;

    Module(DeviceParser parser) throws IllegalArgumentException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_011: [This constructor shall create a new Module object using the values within the provided parser.]
        super(parser);

        if (parser.getModuleId() == null)
        {
            //Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_011: [If the provided parser is missing a value for its authentication or its device Id, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("deviceParser must have a moduleId assigned");
        }

        this.id = parser.getModuleId();
        this.managedBy = parser.getManagedBy();
    }

    /**
     * Converts this into a DeviceParser object. To serialize a Device object, it must first be converted to a DeviceParser object.
     * @return the DeviceParser object that can be serialized.
     */
    DeviceParser toDeviceParser()
    {
        //Codes_SRS_SERVICE_SDK_JAVA_MODULE_28_010: [This method shall return a new instance of a DeviceParser object that is populated using the properties of this.]
        DeviceParser deviceParser = super.toDeviceParser();
        deviceParser.setModuleId(this.id);
        deviceParser.setManagedBy(this.managedBy);

        return  deviceParser;
    }
}
