// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.convention;

import com.microsoft.azure.sdk.iot.deps.convention.PayloadConvention;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * The device command request sent from the cloud.
 * <p>
 *     The device command callback will be returned with this class as a parameter. The {@link DeviceCommandRequest#componentName},
 *     {@link DeviceCommandRequest#commandName}, {@link DeviceCommandRequest#payload} and {@link DeviceCommandRequest#payloadConvention}
 *     will all be set by the SDK.
 * </p>
 *  <p>
 *      Example usage inside of the {@link DeviceCommandCallback}
 *      <pre>
*{@code
 *public DeviceCommandResponse call(DeviceCommandRequest deviceCommandRequest)
 *{
 *   // In this instance we will deal with the default component
 *   if (deviceCommandRequest.getComponentName() == null  && deviceCommandRequest.getCommandName().equalsIgnoreCase(commandName))
 *   {
 *      commandRequest request = deviceCommandRequest.GetPayloadAsObject(commandRequest.class);
 *      // Do work here
 *   }
 *}}
 *      </pre>
 *  </p>
 */
@AllArgsConstructor
public class DeviceCommandRequest
{
    @Getter
    /**
     * The component this command is for.
     */
    private String componentName;

    @Getter
    /**
     * The name of the command.
     */
    private String commandName;

    /**
     * The payload of the command.
     */
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    byte[] payload;

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    PayloadConvention payloadConvention;

    /**
     * Gets the payload deserialized as the specified object type
     * @param typeOfData The type of the class you want to serialize to.
     * @param <T> The outut type. Inferred from the typeOfData parameter
     * @return The object specified by the typeOfData
     */
    public <T> T GetPayloadAsObject(Class<T> typeOfData) {
        return payloadConvention.getObjectFromBytes(payload, typeOfData);
    }

    /**
     * Returns the raw bytes of the payload.
     * @return A byte array.
     */
    public byte[] GetPayloadAsBytes() {
        return payload;
    }

    /**
     * Returns the payload decoded as a string
     * @return A string.
     */
    public String GetPayloadAsString() {
        return payloadConvention.getPayloadEncoder().decodeByteArrayToString(payload);
    }

}


