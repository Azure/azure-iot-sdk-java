/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions;

import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions.*;
import mockit.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class PahoExceptionTranslatorTest
{
    @Mocked
    MqttException mockedMqttException;
    
    // Tests_SRS_PahoExceptionTranslator_34_141: [When deriving the TransportException from the provided MqttException, this function shall map REASON_CODE_INVALID_PROTOCOL_VERSION to MqttRejectedProtocolVersionException.]
    @Test
    public void onConnectionLostMapsInvalidProtocolVersionException()
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedMqttException.getReasonCode();
                result = MqttException.REASON_CODE_INVALID_PROTOCOL_VERSION;
            }
        };

        //act
        Exception e = PahoExceptionTranslator.convertToMqttException(new MqttException(MqttException.REASON_CODE_INVALID_PROTOCOL_VERSION), "");

        //assert
        assertTrue(e instanceof MqttRejectedProtocolVersionException);
    }

    // Tests_SRS_PahoExceptionTranslator_34_142: [When deriving the TransportException from the provided MqttException, this function shall map REASON_CODE_INVALID_CLIENT_ID to MqttIdentifierRejectedException.]
    @Test
    public void onConnectionLostMapsInvalidClientIdException()
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedMqttException.getReasonCode();
                result = MqttException.REASON_CODE_INVALID_CLIENT_ID;
            }
        };

        //act
        Exception e = PahoExceptionTranslator.convertToMqttException(new MqttException(MqttException.REASON_CODE_INVALID_CLIENT_ID), "");

        //assert
        assertTrue(e instanceof MqttIdentifierRejectedException);
    }

    // Tests_SRS_PahoExceptionTranslator_34_143: [When deriving the TransportException from the provided MqttException, this function shall map REASON_CODE_BROKER_UNAVAILABLE to MqttServerUnavailableException.]
    @Test
    public void onConnectionLostMapsBrokerUnavailableException() throws IOException, TransportException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedMqttException.getReasonCode();
                result = MqttException.REASON_CODE_BROKER_UNAVAILABLE;
            }
        };

        //act
        Exception e = PahoExceptionTranslator.convertToMqttException(new MqttException(MqttException.REASON_CODE_BROKER_UNAVAILABLE), "");

        //assert
        assertTrue(e instanceof MqttServerUnavailableException);
    }

    // Tests_SRS_PahoExceptionTranslator_34_144: [When deriving the TransportException from the provided MqttException, this function shall map REASON_CODE_FAILED_AUTHENTICATION to MqttBadUsernameOrPasswordException.]
    @Test
    public void onConnectionLostMapsFailedAuthenticationException() throws IOException, TransportException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedMqttException.getReasonCode();
                result = MqttException.REASON_CODE_FAILED_AUTHENTICATION;
            }
        };

        //act
        Exception e = PahoExceptionTranslator.convertToMqttException(new MqttException(MqttException.REASON_CODE_FAILED_AUTHENTICATION), "");

        //assert
        assertTrue(e instanceof MqttBadUsernameOrPasswordException);
    }

    // Tests_SRS_PahoExceptionTranslator_34_145: [When deriving the TransportException from the provided MqttException, this function shall map REASON_CODE_NOT_AUTHORIZED to MqttUnauthorizedException.]
    @Test
    public void onConnectionLostMapsNotAuthorizedException() throws IOException, TransportException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedMqttException.getReasonCode();
                result = MqttException.REASON_CODE_NOT_AUTHORIZED;
            }
        };

        //act
        Exception e = PahoExceptionTranslator.convertToMqttException(new MqttException(MqttException.REASON_CODE_NOT_AUTHORIZED), "");

        //assert
        assertTrue(e instanceof MqttUnauthorizedException);
    }

    //Tests_SRS_PahoExceptionTranslator_34_147: [When deriving the TransportException from the provided MqttException, this function shall map any connect codes between 6 and 255 inclusive to MqttUnexpectedErrorException.]
    @Test
    public void onConnectionLostMapsUnexpectedConnectCodeException() throws IOException, TransportException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedMqttException.getReasonCode();
                result = MqttException.REASON_CODE_UNEXPECTED_ERROR;
            }
        };

        //act
        Exception e = PahoExceptionTranslator.convertToMqttException(new MqttException(MqttException.REASON_CODE_UNEXPECTED_ERROR), "");

        //assert
        assertTrue(e instanceof MqttUnexpectedErrorException);
    }

    //Tests_SRS_PahoExceptionTranslator_34_148: [When deriving the TransportException from the provided MqttException, this function shall map all other MqttExceptions to ProtocolException.]
    @Test
    public void onConnectionLostMapsUnknownPahoException() throws IOException, TransportException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedMqttException.getReasonCode();
                result = MqttException.REASON_CODE_SSL_CONFIG_ERROR;
            }
        };

        //act
        Exception e = PahoExceptionTranslator.convertToMqttException(new MqttException(MqttException.REASON_CODE_SSL_CONFIG_ERROR), "");

        //assert
        assertTrue(e instanceof ProtocolException);
    }

    // Tests_SRS_Mqtt_34_046: [When deriving the TransportException from the provided MqttException, this function shall map REASON_CODE_SUBSCRIBE_FAILED, REASON_CODE_CLIENT_NOT_CONNECTED, REASON_CODE_TOKEN_INUSE, REASON_CODE_CONNECTION_LOST, REASON_CODE_SERVER_CONNECT_ERROR, REASON_CODE_CLIENT_TIMEOUT, REASON_CODE_WRITE_TIMEOUT, and REASON_CODE_MAX_INFLIGHT to a retryable ProtocolException.]
    @Test
    public void onConnectionLostMapsRetryableExceptionsCorrectly() throws IOException, TransportException
    {
        //arrange
        new StrictExpectations()
        {
            {
                mockedMqttException.getReasonCode();
                result = MqttException.REASON_CODE_SUBSCRIBE_FAILED;

                mockedMqttException.getReasonCode();
                result = MqttException.REASON_CODE_CLIENT_NOT_CONNECTED;

                mockedMqttException.getReasonCode();
                result = MqttException.REASON_CODE_TOKEN_INUSE;

                mockedMqttException.getReasonCode();
                result = MqttException.REASON_CODE_CONNECTION_LOST;

                mockedMqttException.getReasonCode();
                result = MqttException.REASON_CODE_SERVER_CONNECT_ERROR;

                mockedMqttException.getReasonCode();
                result = MqttException.REASON_CODE_CLIENT_TIMEOUT;

                mockedMqttException.getReasonCode();
                result = MqttException.REASON_CODE_WRITE_TIMEOUT;

                mockedMqttException.getReasonCode();
                result = MqttException.REASON_CODE_MAX_INFLIGHT;
            }
        };

        //assert
        assertTrue(PahoExceptionTranslator.convertToMqttException(mockedMqttException, "").isRetryable());
        assertTrue(PahoExceptionTranslator.convertToMqttException(mockedMqttException, "").isRetryable());
        assertTrue(PahoExceptionTranslator.convertToMqttException(mockedMqttException, "").isRetryable());
        assertTrue(PahoExceptionTranslator.convertToMqttException(mockedMqttException, "").isRetryable());
        assertTrue(PahoExceptionTranslator.convertToMqttException(mockedMqttException, "").isRetryable());
        assertTrue(PahoExceptionTranslator.convertToMqttException(mockedMqttException, "").isRetryable());
        assertTrue(PahoExceptionTranslator.convertToMqttException(mockedMqttException, "").isRetryable());
        assertTrue(PahoExceptionTranslator.convertToMqttException(mockedMqttException, "").isRetryable());
    }
}
