// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderSymmetricKey;

import java.nio.charset.StandardCharsets;

/**
 * This sample demonstrates how to derive the symmetric key for a particular device enrollment within an enrollment
 * group. Best security practices dictate that the enrollment group level symmetric key should never be saved to a
 * particular device, so this code is deliberately separate from the {@link ProvisioningSymmetricKeyEnrollmentGroupSample}
 * in this same directory. Users are advised to run this code to generate the derived symmetric key once, and to save
 * the derived key to the device. Users are not advised to derive the device symmetric key from the enrollment group
 * level key within each device as that is insecure.
 */
public class ComputeDerivedSymmetricKeySample
{
    // The symmetric key of the enrollment group. Unlike with individual enrollments, this key cannot be used directly
    // when provisioning a device. Instead, this sample will demonstrate how to derive the symmetric key for your
    // particular device within the enrollment group.

    // Note that this enrollment group level key should NOT be saved to each device. If leaked, this key would allow
    // any device to spoof another device which is insecure. The derived key that this sample generates should be the
    // only key saved to each device.
    private static final String ENROLLMENT_GROUP_SYMMETRIC_KEY = "[Enter your group enrollment symmetric key here]";

    // The Id to assign to this device when it is provisioned to an IoT Hub. This value is arbitrary outside of some
    // character limitations. For sample purposes, this value is filled in for you, but it may be changed.
    private static final String PROVISIONED_DEVICE_ID = "myProvisionedDevice";

    public static void main(String[] args) throws Exception
    {
        // For the sake of security, you shouldn't save keys into String variables as that places them in heap memory. For the sake
        // of simplicity within this sample, though, we will save it as a string. Typically this key would be loaded as byte[] so that
        // it can be removed from stack memory.
        byte[] derivedSymmetricKey =
            SecurityProviderSymmetricKey
                .ComputeDerivedSymmetricKey(
                    ENROLLMENT_GROUP_SYMMETRIC_KEY.getBytes(StandardCharsets.UTF_8),
                    PROVISIONED_DEVICE_ID);

        System.out.println("Your derived symmetric key for group enrollments is: ");
        System.out.println(new String(derivedSymmetricKey, StandardCharsets.UTF_8));
    }
}
