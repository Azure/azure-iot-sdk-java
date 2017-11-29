/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.deps.transport.mqtt;

import com.microsoft.azure.sdk.iot.deps.transport.mqtt.MqttMessage;
import com.microsoft.azure.sdk.iot.deps.transport.mqtt.MqttQos;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/** Unit tests for MqttMessage.
 * Coverage : 100% method, 100% line */
@RunWith(JMockit.class)
public class MqttMessageTest
{
    private static final String TEST_TOPIC = "testTopic";
    private static final String TEST_TOPIC_2 = "testTopic_2";

    @Mocked
    private String mockedtopic;

    @Mocked
    private byte[] mockedPayload;

    @Mocked
    private org.eclipse.paho.client.mqttv3.MqttMessage mockedMqttMessage;

    @Test
    public void MqttMessageConstructorTopicSucceeds()
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
            }
        };

        // Act
        MqttMessage mqttMessage = new MqttMessage(TEST_TOPIC);

        //assert
        assertEquals(TEST_TOPIC, Deencapsulation.getField(mqttMessage, "topic"));
    }

    @Test
    public void MqttMessageConstructorMessage0Succeeds()
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                mockedMqttMessage.getPayload();
                result = mockedPayload;

                mockedMqttMessage.getQos();
                result = 0;
            }
        };

        // Act
        MqttMessage mqttMessage = new MqttMessage(TEST_TOPIC, mockedMqttMessage);

        //assert
        assertEquals(TEST_TOPIC, Deencapsulation.getField(mqttMessage, "topic"));
        assertEquals(MqttQos.DELIVER_AT_MOST_ONCE, Deencapsulation.getField(mqttMessage, "qos"));
    }

    @Test
    public void MqttMessageConstructorMessage1Succeeds()
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                mockedMqttMessage.getPayload();
                result = mockedPayload;

                mockedMqttMessage.getQos();
                result = 1;
            }
        };

        // Act
        MqttMessage mqttMessage = new MqttMessage(TEST_TOPIC, mockedMqttMessage);

        //assert
        assertEquals(TEST_TOPIC, Deencapsulation.getField(mqttMessage, "topic"));
        assertEquals(MqttQos.DELIVER_FAILURE, Deencapsulation.getField(mqttMessage, "qos"));
    }

    @Test
    public void MqttMessageConstructorMessage2Succeeds()
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                mockedMqttMessage.getPayload();
                result = mockedPayload;

                mockedMqttMessage.getQos();
                result = 2;
            }
        };

        // Act
        MqttMessage mqttMessage = new MqttMessage(TEST_TOPIC, mockedMqttMessage);

        //assert
        assertEquals(TEST_TOPIC, Deencapsulation.getField(mqttMessage, "topic"));
        assertEquals(MqttQos.DELIVER_EXACTLY_ONCE, Deencapsulation.getField(mqttMessage, "qos"));
    }

    @Test
    public void MqttMessageConstructorPayloadSucceeds()
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
            }
        };

        // Act
        MqttMessage mqttMessage = new MqttMessage(TEST_TOPIC, mockedPayload);

        //assert
        assertEquals(TEST_TOPIC, Deencapsulation.getField(mqttMessage, "topic"));
    }

    @Test
    public void retrieveQosValueSucceeds()
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
            }
        };

        // Act
        MqttMessage.retrieveQosValue(MqttQos.DELIVER_UNKNOWN);

        //assert
        assertEquals(128, MqttMessage.retrieveQosValue(MqttQos.DELIVER_UNKNOWN));
        assertEquals(0, MqttMessage.retrieveQosValue(MqttQos.DELIVER_AT_MOST_ONCE));
        assertEquals(1, MqttMessage.retrieveQosValue(MqttQos.DELIVER_AT_LEAST_ONCE));
        assertEquals(2, MqttMessage.retrieveQosValue(MqttQos.DELIVER_EXACTLY_ONCE));
        assertEquals(128, MqttMessage.retrieveQosValue(MqttQos.DELIVER_FAILURE));
    }

    @Test
    public void setTopicSucceeds()
    {
        // Arrange
        MqttMessage mqttMessage = new MqttMessage(TEST_TOPIC);

        new NonStrictExpectations()
        {
            {
            }
        };

        // Act
        mqttMessage.setTopic(TEST_TOPIC_2);

        //assert
        assertEquals(TEST_TOPIC_2, Deencapsulation.getField(mqttMessage, "topic"));
    }

    @Test
    public void getTopicSucceeds()
    {
        // Arrange
        MqttMessage mqttMessage = new MqttMessage(TEST_TOPIC);

        new NonStrictExpectations()
        {
            {
            }
        };

        // Act

        //assert
        assertEquals(TEST_TOPIC, mqttMessage.getTopic());
    }

    @Test
    public void getPayloadSucceeds()
    {
        // Arrange
        MqttMessage mqttMessage = new MqttMessage(TEST_TOPIC);

        new NonStrictExpectations()
        {
            {
            }
        };

        // Act

        //assert
        Assert.assertNull(mqttMessage.getPayload());
    }

    @Test
    public void setPayloadSucceeds()
    {
        // Arrange
        MqttMessage mqttMessage = new MqttMessage(TEST_TOPIC);

        new NonStrictExpectations()
        {
            {
            }
        };

        // Act
        mqttMessage.setPayload(mockedPayload);

        //assert
        assertEquals(mockedPayload, mqttMessage.getPayload());
    }

    @Test
    public void setQosSucceeds()
    {
        // Arrange
        MqttMessage mqttMessage = new MqttMessage(TEST_TOPIC);

        new NonStrictExpectations()
        {
            {
            }
        };

        // Act
        mqttMessage.setQos(MqttQos.DELIVER_AT_MOST_ONCE);

        //assert
        assertEquals(MqttQos.DELIVER_AT_MOST_ONCE, mqttMessage.getQos());
    }

    @Test
    public void getMqttMessageNoPayloadSucceeds()
    {
        // Arrange
        MqttMessage mqttMessage = new MqttMessage(TEST_TOPIC);

        new NonStrictExpectations()
        {
            {
                new org.eclipse.paho.client.mqttv3.MqttMessage();
                result = mockedMqttMessage;
            }
        };

        // Act
        org.eclipse.paho.client.mqttv3.MqttMessage pahoMessage = mqttMessage.getMqttMessage();

        //assert
        Assert.assertNotNull(pahoMessage);
    }
}
