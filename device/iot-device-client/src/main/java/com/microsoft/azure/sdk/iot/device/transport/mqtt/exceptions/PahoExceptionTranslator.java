/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions;

import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import static org.eclipse.paho.client.mqttv3.MqttException.*;

public class PahoExceptionTranslator
{
    private static final int UNDEFINED_MQTT_CONNECT_CODE_LOWER_BOUND = 6;
    private static final int UNDEFINED_MQTT_CONNECT_CODE_UPPER_BOUND = 255;

    public static ProtocolException convertToMqttException(MqttException pahoException, String errorMessage)
    {
        switch (pahoException.getReasonCode())
        {
            case REASON_CODE_CLIENT_EXCEPTION:
                // MQTT Client encountered an exception, no connect code retrieved from service, so the reason
                // for this connection loss is in the mqttException cause
                if (pahoException.getCause() instanceof UnknownHostException
                        || pahoException.getCause() instanceof NoRouteToHostException
                        || pahoException.getCause() instanceof InterruptedException
                        || pahoException.getCause() instanceof SocketTimeoutException
                        || pahoException.getCause() instanceof SocketException)
                {
                    // Codes_SRS_PahoExceptionTranslator_34_139: [When deriving the TransportException from the provided
                    // MqttException, this function shall map all client exceptions with underlying UnknownHostException
                    // or InterruptedException or SocketTimeoutException to a retryable ProtocolException.]
                    ProtocolException connectionException = new ProtocolException(errorMessage, pahoException);
                    connectionException.setRetryable(true);
                    return connectionException;
                }
                else
                {
                    //Codes_SRS_PahoExceptionTranslator_34_140: [When deriving the TransportException from the provided MqttException, this function shall map all client exceptions without underlying UnknownHostException and InterruptedException to a non retryable ProtocolException.]
                    return new ProtocolException(errorMessage, pahoException);
                }
            case REASON_CODE_INVALID_PROTOCOL_VERSION:
                // Codes_SRS_PahoExceptionTranslator_34_141: [When deriving the TransportException from the provided MqttException, this function shall map REASON_CODE_INVALID_PROTOCOL_VERSION to MqttRejectedProtocolVersionException.]
                return new MqttRejectedProtocolVersionException(errorMessage, pahoException);
            case REASON_CODE_INVALID_CLIENT_ID:
                // Codes_SRS_PahoExceptionTranslator_34_142: [When deriving the TransportException from the provided MqttException, this function shall map REASON_CODE_INVALID_CLIENT_ID to MqttIdentifierRejectedException.]
                return new MqttIdentifierRejectedException(errorMessage, pahoException);
            case REASON_CODE_BROKER_UNAVAILABLE:
                // Codes_SRS_PahoExceptionTranslator_34_143: [When deriving the TransportException from the provided MqttException, this function shall map REASON_CODE_BROKER_UNAVAILABLE to MqttServerUnavailableException.]
                return new MqttServerUnavailableException(errorMessage, pahoException);
            case REASON_CODE_FAILED_AUTHENTICATION:
                // Codes_SRS_PahoExceptionTranslator_34_144: [When deriving the TransportException from the provided MqttException, this function shall map REASON_CODE_FAILED_AUTHENTICATION to MqttBadUsernameOrPasswordException.]
                return new MqttBadUsernameOrPasswordException(errorMessage, pahoException);
            case REASON_CODE_NOT_AUTHORIZED:
                // Codes_SRS_PahoExceptionTranslator_34_145: [When deriving the TransportException from the provided MqttException, this function shall map REASON_CODE_NOT_AUTHORIZED to MqttUnauthorizedException.]
                return new MqttUnauthorizedException(errorMessage, pahoException);
            case REASON_CODE_SUBSCRIBE_FAILED:
            case REASON_CODE_CLIENT_NOT_CONNECTED:
            case REASON_CODE_TOKEN_INUSE:
            case REASON_CODE_CONNECTION_LOST:
            case REASON_CODE_SERVER_CONNECT_ERROR:
            case REASON_CODE_CLIENT_TIMEOUT:
            case REASON_CODE_WRITE_TIMEOUT:
            case REASON_CODE_MAX_INFLIGHT:
            case REASON_CODE_CONNECT_IN_PROGRESS:
                // Codes_SRS_PahoExceptionTranslator_34_146: [When deriving the TransportException from the provided MqttException, this function shall map REASON_CODE_SUBSCRIBE_FAILED, REASON_CODE_CLIENT_NOT_CONNECTED, REASON_CODE_TOKEN_INUSE, REASON_CODE_CONNECTION_LOST, REASON_CODE_SERVER_CONNECT_ERROR, REASON_CODE_CLIENT_TIMEOUT, REASON_CODE_WRITE_TIMEOUT, and REASON_CODE_MAX_INFLIGHT to a retryable ProtocolException.]
                //Client lost internet connection, or server could not be reached, or other retryable connection exceptions
                ProtocolException connectionException = new ProtocolException(errorMessage, pahoException);
                connectionException.setRetryable(true);
                return connectionException;
            default:
                if (pahoException.getReasonCode() >= UNDEFINED_MQTT_CONNECT_CODE_LOWER_BOUND && pahoException.getReasonCode() <= UNDEFINED_MQTT_CONNECT_CODE_UPPER_BOUND)
                {
                    //Mqtt connect codes 6 to 255 are reserved for future MQTT standard codes and are unused as of MQTT 3
                    //Codes_SRS_PahoExceptionTranslator_34_147: [When deriving the TransportException from the provided MqttException, this function shall map any connect codes between 6 and 255 inclusive to MqttUnexpectedErrorException.]
                    return new MqttUnexpectedErrorException(errorMessage, pahoException);
                }
                else
                {
                    //Mqtt connect code was not MQTT standard code, and was not a retryable exception as defined by Paho
                    //Codes_SRS_PahoExceptionTranslator_34_148: [When deriving the TransportException from the provided MqttException, this function shall map all other MqttExceptions to ProtocolException.]
                    return new ProtocolException(errorMessage, pahoException);
                }
        }
    }
}
