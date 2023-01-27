// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.device.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.device.SasTokenProvider;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;

import java.net.URISyntaxException;

/**
 * Basic implementation of the SasTokenProvider interface, for test purposes.
 */
public class SasTokenProviderImpl implements SasTokenProvider
{
    IotHubConnectionString deviceConnectionString;
    int expiryLengthSeconds;

    // Same value as the default SAS token timespan when using SDK defaults.
    private static final int DEFAULT_EXPIRY_LENGTH_SECONDS =  60 * 60; // 1 hour

    public SasTokenProviderImpl(String deviceConnectionString) throws URISyntaxException {
        this(deviceConnectionString, DEFAULT_EXPIRY_LENGTH_SECONDS);
    }

    // expiryLengthSeconds is useful to configure in tests like the token renewal tests where we want shorter
    // lived SAS tokens
    public SasTokenProviderImpl(String deviceConnectionString, int expiryLengthSeconds) throws URISyntaxException {
        this.deviceConnectionString = new IotHubConnectionString(deviceConnectionString);
        this.expiryLengthSeconds = expiryLengthSeconds;
    }

    @Override
    public char[] getSasToken() {
        long expiryTimeSeconds = expiryLengthSeconds + (System.currentTimeMillis() / 1000);
        return new IotHubSasToken(
                deviceConnectionString.getHostName(),
                deviceConnectionString.getDeviceId(),
                deviceConnectionString.getSharedAccessKey(),
                null,
                deviceConnectionString.getModuleId(),
                expiryTimeSeconds).toString().toCharArray();
    }
}
