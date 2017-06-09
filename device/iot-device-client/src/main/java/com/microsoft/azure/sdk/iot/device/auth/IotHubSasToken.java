// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.net.IotHubUri;

import javax.print.URIException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/** Grants device access to an IoT Hub for the specified amount of time. */
public final class IotHubSasToken
{
    /**
     * The SAS token format. The parameters to be interpolated are, in order:
     * the signature, the expiry time, the key name (device ID), and the
     * resource URI.
     */
    private static final String TOKEN_FORMAT = "SharedAccessSignature sig=%s&se=%s&sr=%s";
    private String sasToken = null;

    /** Components of the SAS token. */
    private String signature = null;
    /** The time, as a UNIX timestamp, before which the token is valid. */
    private long expiryTime = 0L;
    /**
     * The URI for a connection from a device to an IoT Hub. Does not include a
     * protocol.
     */
    private String scope = null;

    /**
     * Constructor. Generates a SAS token that grants access to an IoT Hub for
     * the specified amount of time.
     *
     * @param config the device client config.
     * @param expiryTime the time, as a UNIX timestamp, after which the token
     * will become invalid.
     */
    public IotHubSasToken(DeviceClientConfig config, long expiryTime)
    {
        // Codes_SRS_IOTHUBSASTOKEN_25_005: [**If device key is provided then the signature shall be correctly computed and set.**]**
        if (config.getDeviceKey() != null) {
            // Tests_SRS_IOTHUBSASTOKEN_11_002: [**The constructor shall save all input parameters to member variables.**]
            this.scope = IotHubUri.getResourceUri(config.getIotHubHostname(), config.getDeviceId());
            this.expiryTime = expiryTime;

            Signature sig = new Signature(this.scope, this.expiryTime, config.getDeviceKey());
            this.signature = sig.toString();
        }
        // Codes_SRS_IOTHUBSASTOKEN_25_007: [**If device key is not provided in config then the SASToken from config shall be used.**]**
        else if(config.getSharedAccessToken() != null)
        {
            this.sasToken = config.getSharedAccessToken();
            this.expiryTime = getExpiryTimeFromToken(this.sasToken);

            // Codes_SRS_IOTHUBSASTOKEN_25_008: [**The required format for the SAS Token shall be verified and IllegalArgumentException is thrown if unmatched.**]**
            if (!isSasFormat())
                throw new IllegalArgumentException("SasToken format is invalid");

            // Codes_SRS_IOTHUBSASTOKEN_34_009: [**The SAS Token shall be verified as not expired and SecurityException will be thrown if it is expired.**]**
            if (isSasTokenExpired(this.sasToken))
                throw new SecurityException("Your SasToken has expired");

        }
        else
        {
            this.signature = null;
            this.sasToken = null;
        }
    }

    /**
     * Returns the string representation of the SAS token.
     *
     * @return the string representation of the SAS token.
     */
    @Override
    public String toString()
    {
        // Codes_SRS_IOTHUBSASTOKEN_25_009: [**If SAS Token was provided by config it should be returned as string **]**
        if (this.sasToken != null)
        {
            if(isSasFormat())
                return this.sasToken;
            else
                throw new IllegalArgumentException("SasToken format is invalid");
        }
        else if(this.signature != null && this.expiryTime != 0L && this.scope!= null)
        {
            //Codes_SRS_IOTHUBSASTOKEN_25_010: [**If SAS Token was not provided by config it should be built and returned as string **]**
            return buildSasToken();
        }
        else
            return null;
    }

    private boolean isSasFormat()
    {
        /*
          The SAS token format. The parameters to be interpolated are, in any order:
          the signature, the expiry time, and the resource URI.
         */
        if (this.sasToken != null)
        {
            if(this.sasToken.startsWith("SharedAccessSignature"))
            {

                Map<String, String> fieldValues = extractFieldValues(this.sasToken);
                if(fieldValues.containsKey(SharedAccessSignatureConstants.ExpiryTimeKey)
                        && fieldValues.containsKey(SharedAccessSignatureConstants.SignatureKey)
                        && fieldValues.containsKey(SharedAccessSignatureConstants.ResourceKey)
                        && !fieldValues.get(SharedAccessSignatureConstants.ExpiryTimeKey).isEmpty()
                        && !fieldValues.get(SharedAccessSignatureConstants.SignatureKey).isEmpty()
                        && !fieldValues.get(SharedAccessSignatureConstants.ResourceKey).isEmpty())
                    return true;
            }
        }
        return false;
    }

    public static boolean isSasTokenExpired(String sasToken)
    {
        //expiry time is measured in seconds since Unix Epoch
        Long currentTime = System.currentTimeMillis() / 1000;
        Long expiryTime = getExpiryTimeFromToken(sasToken);
        return (currentTime >= expiryTime);
    }

    public static Long getExpiryTimeFromToken(String sasToken)
    {
        Map<String, String> fieldValues = extractFieldValues(sasToken);
        return Long.parseLong(fieldValues.get(SharedAccessSignatureConstants.ExpiryTimeKey));
    }

    private static Map<String, String> extractFieldValues(String sharedAccessSignature)
    {
        String[] lines = sharedAccessSignature.split(" ");

        if (!lines[0].trim().toUpperCase().equals(SharedAccessSignatureConstants.SharedAccessSignature.toUpperCase()) || lines.length != 2)
        {
            throw new IllegalArgumentException("Malformed signature");
        }

        Map<String, String> parsedFields = new HashMap<String, String>();
        String[] fields = lines[1].trim().split(SharedAccessSignatureConstants.PairSeparator);

        for (String field : fields)
        {
            if (field != "")
            {
                String[] fieldParts = field.split(SharedAccessSignatureConstants.KeyValueSeparator);
                parsedFields.put(fieldParts[0], fieldParts[1]);
            }
        }

        return parsedFields;
    }

    private String buildSasToken()
    {
        // Codes_SRS_IOTHUBSASTOKEN_11_001: [The SAS token shall have the format "SharedAccessSignature sig=<signature >&se=<expiryTime>&sr=<resourceURI>". The params can be in any order.]
        return String.format(TOKEN_FORMAT, this.signature, this.expiryTime, this.scope);
    }

    @SuppressWarnings("unused")
    protected IotHubSasToken()
    {
        this.signature = null;
        this.expiryTime = 0L;
        this.scope = null;
        this.sasToken = null;
    }
}
