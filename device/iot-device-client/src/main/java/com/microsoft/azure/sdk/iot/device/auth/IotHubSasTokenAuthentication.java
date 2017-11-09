/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device.auth;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class IotHubSasTokenAuthentication
{
    /**
     * The number of seconds after which the generated SAS token for a message
     * will become invalid. We also use the expiry time, which is computed as
     * {@code currentTime() + DEVICE_KEY_VALID_LENGTH}, as a salt when generating our
     * SAS token.
     */
    protected long tokenValidSecs = 3600;

    protected static final long MILLISECONDS_PER_SECOND = 1000L;
    protected static final long MINIMUM_EXPIRATION_TIME_OFFSET = 1L;

    protected static final String ENCODING_FORMAT_NAME = StandardCharsets.UTF_8.displayName();

    protected IotHubSasToken sasToken;
    protected IotHubSSLContext iotHubSSLContext;

    protected String hostname;
    protected String deviceId;

    protected boolean sslContextNeedsUpdate;

    public abstract void setPathToIotHubTrustedCert(String pathToCertificate);
    public abstract void setIotHubTrustedCert(String certificate);

    public abstract SSLContext getSSLContext() throws IOException;


    public abstract String getRenewedSasToken() throws IOException;

    /**
     * Getter for SasToken. If the saved token has expired, this method shall not renew it even if possible
     *
     * @return The value of SasToken
     */
    public String getCurrentSasToken()
    {
        //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_018: [This function shall return the current sas token without renewing it.]
        return this.sasToken.toString();
    }

    public void setTokenValidSecs(long tokenValidSecs)
    {
        //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_012: [This function shall save the provided tokenValidSecs as the number of seconds that created sas tokens are valid for.]
        this.tokenValidSecs = tokenValidSecs;
    }

    Long getExpiryTimeInSeconds()
    {
        //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_001: [This function shall return the number of seconds from the UNIX Epoch that a sas token constructed now would expire.]
        return (System.currentTimeMillis() / MILLISECONDS_PER_SECOND) + this.tokenValidSecs + MINIMUM_EXPIRATION_TIME_OFFSET;
    }

    public boolean isRenewalNecessary()
    {
        //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [If the saved sas token has expired, this function shall return true.]
        return (this.sasToken != null && this.sasToken.isExpired());
    }

    public long getTokenValidSecs()
    {
        return this.tokenValidSecs;
    }
}
