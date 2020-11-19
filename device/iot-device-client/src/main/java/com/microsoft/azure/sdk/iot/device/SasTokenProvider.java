// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device;

/**
 * Interface for allowing users to control SAS token generation. To see an example of how SAS tokens can be generated
 * from device connection strings, see {@link com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken}.
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-security#example">This document</a> for more details on sas tokens.
 */
public interface SasTokenProvider
{
    /**
     * Returns a SAS token to be used for authentication.
     * <p>
     * This function will be called each time the client library needs a SAS token. This will follow different patterns
     * based on which protocol your client object is using.
     * <p>
     * For HTTPS, this function will be called for each HTTPS request made (for instance, once per telemetry send), and does not need
     * to be a unique token each time. Because of that, users may simply cache and return the same SAS token repeatedly here.
     * Alternatively, users can generate very short-lived SAS tokens since they will only need to be valid for a relative short period of time.
     * The user of this API is responsible for tracking when to renew the SAS token based on how long the previous token was valid for.
     * <p>
     * For AMQPS/AMQPS_WS, this function will be called once when first opening the connection, and then will be called again
     * at some point prior to the previous SAS token's expiry time in order to proactively renew the connection's authentication.
     * This proactive renewal takes place at around 85% of the previous SAS token's lifespan.
     * <p>
     * For MQTT/MQTT_WS, this function will be called once when first opening the connection, and again each time the previous
     * SAS token has expired and the client closes and re-opens the connection. MQTT/MQTT_WS does not currently support
     * proactive token renewal.
     * @return a SAS token to be used for authentication.
     */
    public char[] getSasToken();
}
