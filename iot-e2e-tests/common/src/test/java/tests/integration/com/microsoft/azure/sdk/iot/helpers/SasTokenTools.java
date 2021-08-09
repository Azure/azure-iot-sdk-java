// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

/**
 * Tools class for SAS token helper functions
 */
public class SasTokenTools
{
    private static final String KEY_VALUE_PAIR_SEPARATOR = "&";
    private static final String EXPIRY_TIME_KEY = KEY_VALUE_PAIR_SEPARATOR + "se=";

    /**
     * Takes in a valid SAS token and returns an expired version of that same SAS token. This function works for both
     * device and service SAS tokens.
     * @param sasToken a valid SAS token to make expired.
     * @return an expired SAS token.
     */
    public static String makeSasTokenExpired(String sasToken)
    {
        String expiryTimeSubstring = sasToken.substring(sasToken.indexOf(EXPIRY_TIME_KEY) + EXPIRY_TIME_KEY.length());

        // Shared access signatures are a set of key value pairs that can be in any order. If the expiry time key value pair
        // is the final key value pair, then the below if block won't execute.
        //
        // For instance, given "SharedAccessSignature sr=<hostname>&sig=<signature>&se=<expiryTime>&skn=<keyName>", the
        // above would set expiryTimeSubstring = "<expiryTime>&skn=<keyName>", so we still need to remove any remaining
        // key value pairs from the substring so it is just "<expiryTime>"
        if (expiryTimeSubstring.contains(KEY_VALUE_PAIR_SEPARATOR))
        {
            expiryTimeSubstring = expiryTimeSubstring.substring(0, expiryTimeSubstring.indexOf(KEY_VALUE_PAIR_SEPARATOR));
        }

        // replace the expiry time in the original SAS token with "0" so that it expired back in 1970
        return sasToken.replace(expiryTimeSubstring, "0");
    }
}
