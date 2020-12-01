// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.net.IotHubUri;

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
     * The following strings are all expected to be in a correct SAS Token
     * SharedAccessSignature sig=<signature >&se=<expiryTime>&sr=<resourceURI>
     */
    private static final String SharedAccessSignature = "SharedAccessSignature";
    private static final String FieldPairSeparator = "&";
    private static final String FieldKeyValueSeparator = "=";
    private static final String ExpiryTimeFieldKey = "se";
    private static final String SignatureFieldKey = "sig";
    private static final String ResourceURIFieldKey = "sr";

    /**
     *  KeyValue pairs are extracted from the second segment of the SAS token by finding strings in the
     *  format <key>=<value>. These values are further split around the equals sign into arrays. The below
     *  indices represent which index in each of those arrays represent the key and which represents the value
     */
    private static final int ExpectedNumberOfFieldParts = 2;
    private static final int KeyValuePairKeyIndex = 0;
    private static final int KeyValuePairValueIndex = 1;

    /**
     * In a correctly formatted SAS token, there are two segments separated by a space:
     *      SharedAccessSignature
     * and
     *      sig=<signature >&se=<expiryTime>&sr=<resourceURI>
     */
    private static final int SASTokenConstantSegmentIndex = 0;
    private static final int SASTokenFieldSegmentIndex = 1;
    private static final int ExpectedSASTokenSegments = 2;
    private static final String SASTokenSegmentSeparator = " ";

    /**
     * Constructor. Generates a SAS token that grants access to an IoT Hub for
     * the specified amount of time.
     *
     * @param hostname the hostname of the hub the token is for
     * @param deviceId The id of the device the token is for
     * @param deviceKey The device key for connecting the device to the hub with. May be null if sharedAccessToken is not.
     * @param sharedAccessToken The sas token for connecting the device to the hub with. May be null if deviceKey is not.
     * @param moduleId the module id. May be null if the sas token is not for a module
     * @param expiryTime the time, as a UNIX timestamp, after which the token will become invalid
     */
    public IotHubSasToken(String hostname, String deviceId, String deviceKey, String sharedAccessToken, String moduleId, long expiryTime)
    {
        // Codes_SRS_IOTHUBSASTOKEN_25_005: [**If device key is provided then the signature shall be correctly computed and set.**]**
        if (deviceKey != null)
        {
            // Tests_SRS_IOTHUBSASTOKEN_11_002: [**The constructor shall save all input parameters to member variables.**]
            this.scope = IotHubUri.getResourceUri(hostname, deviceId, moduleId);
            this.expiryTime = expiryTime;

            Signature sig = new Signature(this.scope, this.expiryTime, deviceKey);
            this.signature = sig.toString();
        }
        // Codes_SRS_IOTHUBSASTOKEN_25_007: [**If device key is not provided in config then the SASToken from config shall be used.**]**
        else if(sharedAccessToken != null)
        {
            this.sasToken = sharedAccessToken;
            this.expiryTime = getExpiryTimeFromToken(this.sasToken);

            // Codes_SRS_IOTHUBSASTOKEN_25_008: [**The required format for the SAS Token shall be verified and IllegalArgumentException is thrown if unmatched.**]**
            if (!isSasFormat())
            {
                throw new IllegalArgumentException("SasToken format is invalid");
            }

            // Codes_SRS_IOTHUBSASTOKEN_34_009: [**The SAS Token shall be verified as not expired and SecurityException will be thrown if it is expired.**]**
            if (isExpired(this.sasToken))
            {
                throw new SecurityException("Your SasToken has expired");
            }
        }
        else
        {
            // Codes_SRS_IOTHUBSASTOKEN_34_012: [If their is no deviceKey or sharedAccessToken provided, this function shall
            // throw and IllegalArgumentException.]
            throw new IllegalArgumentException("deviceKey and sharedAccessToken may not both be null");
        }
    }

    /**
     * Creates a shared access token from the provided audience, signature and expiry time
     * @param audience the audience of the token
     * @param signature the signature of the token
     * @param expiry when the token will expire, in seconds since the epoch
     * @return the shared access token string
     */
    public static String buildSharedAccessToken(String audience, String signature, long expiry)
    {
        if (audience == null || audience.isEmpty() || signature == null || signature.isEmpty())
        {
            // Codes_SRS_IOTHUBSASTOKEN_34_015: [If the provided audience or signature is null or empty, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("neither audience nor signature can be null or empty");
        }

        if (expiry < 0)
        {
            // Codes_SRS_IOTHUBSASTOKEN_34_014: [If the provided expiry time is less than 0, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("expiry time must be a non-negative integer");
        }

        // Codes_SRS_IOTHUBSASTOKEN_34_013: [This function shall return a string in the format "SharedAccessSignature sr=<audience>&sig=<signature>&se=<expiry>".]
        return String.format("%s %s=%s&%s=%s&%s=%d",
                SharedAccessSignature,
                ResourceURIFieldKey,
                audience,
                SignatureFieldKey,
                signature,
                ExpiryTimeFieldKey,
                expiry);
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
            {
                return this.sasToken;
            }
            else
            {
                throw new IllegalArgumentException("SasToken format is invalid");
            }
        }
        else if(this.signature != null && this.expiryTime != 0L && this.scope!= null)
        {
            //Codes_SRS_IOTHUBSASTOKEN_25_010: [**If SAS Token was not provided by config it should be built and returned as string **]**
            return buildSasToken();
        }
        else
        {
            return null;
        }
    }

    public String getSasToken()
    {
        // Codes_SRS_IOTHUBSASTOKEN_34_011: [This function shall return the saved sas token.]
        return this.sasToken;
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
                if(fieldValues.containsKey(ExpiryTimeFieldKey)
                        && fieldValues.containsKey(SignatureFieldKey)
                        && fieldValues.containsKey(ResourceURIFieldKey))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns if the provided sasToken has expired yet or not
     *
     * @param sasToken the token to check for expiration
     * @return a boolean true if the SasToken is still valid,
     * or false if it is expired.
     */
    public static boolean isExpired(String sasToken)
    {
        //expiry time is measured in seconds since Unix Epoch
        Long currentTime = System.currentTimeMillis() / 1000;
        Long expiryTime = getExpiryTimeFromToken(sasToken);
        return (currentTime >= expiryTime);
    }

    /**
     * Returns if this token has expired yet or not
     *
     * @return a boolean true if the SasToken is still valid,
     * or false if it is expired.
     */
    boolean isExpired()
    {
        return (System.currentTimeMillis() / 1000) >= this.expiryTime ;
    }

    /**
     * Return the expiry time for the provided sasToken in seconds since the UNIX epoch.
     *
     * @param sasToken the token to return the expiry time for.
     * @return the expiry time for the provided sasToken in seconds since the UNIX epoch
     */
    static Long getExpiryTimeFromToken(String sasToken)
    {
        Map<String, String> fieldValues = extractFieldValues(sasToken);
        return Long.parseLong(fieldValues.get(ExpiryTimeFieldKey));
    }

    private static Map<String, String> extractFieldValues(String sharedAccessSignature)
    {
        String[] lines = sharedAccessSignature.split(SASTokenSegmentSeparator);

        String sasTokenFirstSegment = lines[SASTokenConstantSegmentIndex].trim().toUpperCase();
        boolean sasTokenFirstSegmentMatchesExpected = sasTokenFirstSegment.equals(SharedAccessSignature.toUpperCase());
        if (lines.length != ExpectedSASTokenSegments || !sasTokenFirstSegmentMatchesExpected)
        {
            throw new IllegalArgumentException("Malformed signature");
        }

        Map<String, String> parsedFields = new HashMap<String, String>();
        String[] fields = lines[SASTokenFieldSegmentIndex].trim().split(FieldPairSeparator);

        for (String field : fields)
        {
            if (!field.equals(""))
            {
                String[] fieldParts = field.split(FieldKeyValueSeparator);

                if (fieldParts.length != ExpectedNumberOfFieldParts)
                {
                    throw new IllegalArgumentException("SasToken format is invalid: missing a key or value tied to your field: " + field);
                }

                parsedFields.put(fieldParts[KeyValuePairKeyIndex], fieldParts[KeyValuePairValueIndex]);
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
