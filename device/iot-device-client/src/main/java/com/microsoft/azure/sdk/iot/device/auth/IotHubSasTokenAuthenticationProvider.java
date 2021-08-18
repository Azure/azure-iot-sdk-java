/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class IotHubSasTokenAuthenticationProvider extends IotHubAuthenticationProvider
{
    /**
     * The number of seconds after which the generated SAS token for a message
     * will become invalid. We also use the expiry time, which is computed as
     * {@code currentTime() + DEVICE_KEY_VALID_LENGTH}, as a salt when generating our
     * SAS token.
     */
    protected long tokenValidSecs = 3600;

    /**
     * The percentage of a sas token's life that will happen before it should be renewed. Between 1 and 100
     */
    int timeBufferPercentage = 85;

    private static final long MILLISECONDS_PER_SECOND = 1000L;
    private static final long MINIMUM_EXPIRATION_TIME_OFFSET = 1L;

    static final String ENCODING_FORMAT_NAME = StandardCharsets.UTF_8.displayName();

    protected IotHubSasToken sasToken;

    public abstract boolean canRefreshToken();
    public abstract char[] getSasToken() throws IOException, TransportException;

    public IotHubSasTokenAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId)
    {
        super(hostname, gatewayHostname, deviceId, moduleId);
    }

    IotHubSasTokenAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId, SSLContext sslContext)
    {
        super(hostname, gatewayHostname, deviceId, moduleId, sslContext);
    }

    public IotHubSasTokenAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId, long tokenValidSecs, int timeBufferPercentage)
    {
        super(hostname, gatewayHostname, deviceId, moduleId);

        //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_015: [This function shall save the provided tokenValidSecs as the number of seconds that created sas tokens are valid for.]
        //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_016: [If the provided tokenValidSecs is less than 1, this function shall throw an IllegalArgumentException.]
        this.setTokenValidSecs(tokenValidSecs);

        if (timeBufferPercentage < 1 || timeBufferPercentage > 100)
        {
            //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [If the provided timeBufferPercentage is less than 1 or greater than 100, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Time buffer percentage must be a percentage between 1 and 100");
        }

        //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_018: [This function shall save the provided timeBufferPercentage.]
        this.timeBufferPercentage = timeBufferPercentage;
    }

    IotHubSasTokenAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId, long tokenValidSecs, int timeBufferPercentage, SSLContext sslContext)
    {
        super(hostname, gatewayHostname, deviceId, moduleId, sslContext);

        this.setTokenValidSecs(tokenValidSecs);

        if (timeBufferPercentage < 1 || timeBufferPercentage > 100)
        {
            throw new IllegalArgumentException("Time buffer percentage must be a percentage between 1 and 100");
        }

        this.timeBufferPercentage = timeBufferPercentage;
    }

    public void setTokenValidSecs(long tokenValidSecs)
    {
        if (tokenValidSecs < 1)
        {
            throw new IllegalArgumentException("tokens must live for more than 1 second");
        }

        //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_012: [This function shall save the provided tokenValidSecs as the number of seconds that created sas tokens are valid for.]
        this.tokenValidSecs = tokenValidSecs;
    }

    /**
     * Returns true if the this authentication provider is no longer valid. If true, users will need to create a new
     * DeviceClient instance to get a new authentication provider. The most common case for this is if the user
     * provides a SAS token, but no symmetric key, and that SAS token has expired. At that point, the user's client
     * won't be able to authenticate anymore.
     * @return true if the this authentication provider is no longer valid. False otherwise
     */
    public boolean isAuthenticationProviderRenewalNecessary()
    {
        //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [If the saved sas token has expired, this function shall return true.]
        return (this.sasToken != null && this.sasToken.isExpired());
    }

    /**
     * Returns true if the saved token should be refreshed
     *
     * @param proactivelyRenew if true, this function will return true even if the saved token has not expired, but only
     *                         if the token has lived beyond its time buffer percentage
     * @return true if the caller should refresh the saved sas token
     */
    public boolean shouldRefreshToken(boolean proactivelyRenew)
    {
        if (this.sasToken.isExpired())
        {
            return true;
        }

        if (proactivelyRenew)
        {
            long expiryTimeSeconds = IotHubSasToken.getExpiryTimeFromToken(this.sasToken.toString());
            long tokenStartTime = expiryTimeSeconds - this.tokenValidSecs;
            long bufferExpiryTime = getMillisecondsBeforeProactiveRenewal() / 100 + tokenStartTime;
            long currentTimeSeconds = System.currentTimeMillis() / 1000;
            //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_019: [This function shall return true if the saved token has lived for longer
            // than its buffered threshold.]
            return bufferExpiryTime < currentTimeSeconds;
        }

        //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_020: [This function shall return false if the saved token has not lived for longer
        // than its buffered threshold.]
        return false;
    }

    public int getMillisecondsBeforeProactiveRenewal()
    {
        int tokenValidMilliseconds = (int) this.tokenValidSecs * 1000; //this.tokenValidSecs is in seconds, but we need milliseconds here
        double timeBufferMultiplier = this.timeBufferPercentage / 100.0; //Convert 85 to .85, for example. Percentage multipliers are in decimal
        return (int) (tokenValidMilliseconds * timeBufferMultiplier);
    }

    public long getTokenValidSecs()
    {
        return this.tokenValidSecs;
    }

    long getExpiryTimeInSeconds()
    {
        //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_001: [This function shall return the number of seconds from the UNIX Epoch that a sas token constructed now would expire.]
        return (System.currentTimeMillis() / MILLISECONDS_PER_SECOND) + this.tokenValidSecs + MINIMUM_EXPIRATION_TIME_OFFSET;
    }
}
