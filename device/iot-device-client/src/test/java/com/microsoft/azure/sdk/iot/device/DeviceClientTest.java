// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.ClientOptions;
import com.microsoft.azure.sdk.iot.device.DeviceClientType;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.DeviceIO;
import com.microsoft.azure.sdk.iot.device.InternalClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.device.ProductInfo;
import com.microsoft.azure.sdk.iot.device.auth.IotHubAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.FileUpload;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.microsoft.azure.sdk.iot.device.DeviceClientType.SINGLE_CLIENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for DeviceClient.
 * Methods: 100%
 * Lines: 98%
 */
public class DeviceClientTest
{
    @Mocked
    DeviceClientConfig mockConfig;

    @Mocked
    IotHubConnectionString mockIotHubConnectionString;

    @Mocked
    ClientOptions mockClientOptions;

    @Mocked
    DeviceIO mockDeviceIO;

    @Mocked
    FileUpload mockFileUpload;

    @Mocked
    IotHubAuthenticationProvider mockIotHubAuthenticationProvider;

    @Mocked
    IotHubSasTokenAuthenticationProvider mockIotHubSasTokenAuthenticationProvider;

    @Mocked
    SecurityProvider mockSecurityProvider;

    @Mocked
    IotHubConnectionStatusChangeCallback mockedIotHubConnectionStatusChangeCallback;

    @Mocked
    ProductInfo mockedProductInfo;

    private static final long SEND_PERIOD_MILLIS = 10L;
    private static final long RECEIVE_PERIOD_MILLIS_AMQPS = 10L;
    private static final long RECEIVE_PERIOD_MILLIS_HTTPS = 25*60*1000; /*25 minutes*/

    @Test
    public void constructorWithClientOptionsSuccess(@Mocked final ClientOptions mockedClientOptions) throws URISyntaxException
    {
        // arrange
        final String connString =
                "HostName=iothub.device.com;deviceId=testdevice;x509=true";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS_WS;

        new Expectations()
        {
            {
                new IotHubConnectionString(connString);
                result = mockIotHubConnectionString;
            }
        };

        // act
        final DeviceClient client = new DeviceClient(connString, protocol, mockedClientOptions);

        // assert
        new Verifications()
        {
            {
                DeviceClientType deviceClientType = Deencapsulation.getField(client, "deviceClientType");
                assertEquals(SINGLE_CLIENT, deviceClientType);

                new DeviceClientConfig(mockIotHubConnectionString, mockedClientOptions);
            }
        };
    }

    //Tests_SRS_DEVICECLIENT_34_065: [The provided uri and device id will be used to create an iotHubConnectionString that will be saved in config.]
    //Tests_SRS_DEVICECLIENT_34_066: [The provided security provider will be saved in config.]
    //Tests_SRS_DEVICECLIENT_34_067: [The constructor shall initialize the IoT Hub transport for the protocol specified, creating a instance of the deviceIO.]
    @Test
    public void createFromSecurityProviderUsesUriAndDeviceIdAndSavesSecurityProviderAndCreatesDeviceIO() throws URISyntaxException, IOException
    {
        //arrange
        final String expectedUri = "some uri";
        final String expectedDeviceId = "some device id";
        final IotHubClientProtocol expectedProtocol = IotHubClientProtocol.HTTPS;

        //act
        DeviceClient.createFromSecurityProvider(expectedUri, expectedDeviceId, mockSecurityProvider, expectedProtocol, null);

        //assert
        new Verifications()
        {
            {
                //TODO add check for super() call
            }
        };
    }

    /* Tests_SRS_DEVICECLIENT_21_001: [The constructor shall interpret the connection string as a set of key-value pairs delimited by ';', using the object IotHubConnectionString.] */
    @Test
    public void constructorBadConnectionStringThrows() throws URISyntaxException, IOException
    {
        // arrange
        final String connString =
                "HostName=iothub.device.com;CredentialType=SharedAccessKey;CredentialScope=Device;deviceId=testdevice;SharedAccessKey=adjkl234j52=;";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        //assert
        new Expectations()
        {
            {
                Deencapsulation.newInstance(IotHubConnectionString.class, connString);
                result = new IllegalArgumentException();
            }
        };

        // act
        try
        {
            new DeviceClient(connString, protocol);
        }
        catch (IllegalArgumentException expected)
        {
            // Don't do anything, throw expected.
        }

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(DeviceClientConfig.class, new Class[] {IotHubConnectionString.class}, (IotHubConnectionString) any);
                times = 0;
                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                        new Class[] {DeviceClientConfig.class, long.class, long.class},
                        any, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);
                times = 0;
            }
        };
    }

    /* Tests_SRS_DEVICECLIENT_21_002: [The constructor shall initialize the IoT Hub transport for the protocol specified, creating a instance of the deviceIO.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorBadDeviceIOThrows() throws URISyntaxException, IOException
    {
        // arrange
        final String connString =
                "HostName=iothub.device.com;CredentialType=SharedAccessKey;CredentialScope=Device;deviceId=testdevice;SharedAccessKey=adjkl234j52=;";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        // act
        new DeviceClient(connString, protocol);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubConnectionString.class, connString);
                times = 1;
                Deencapsulation.newInstance(DeviceClientConfig.class, (IotHubConnectionString)any, DeviceClientConfig.AuthType.SAS_TOKEN);
                times = 1;
                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                        new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                        (DeviceClientConfig)any, protocol, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICECLIENT_21_003: [The constructor shall save the connection configuration using the object DeviceClientConfig.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorBadDeviceClientConfigThrows() throws URISyntaxException, IOException
    {
        // arrange
        final String connString =
                "HostName=iothub.device.com;CredentialType=SharedAccessKey;CredentialScope=Device;deviceId=testdevice;SharedAccessKey=adjkl234j52=;";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        // act
        new DeviceClient(connString, protocol);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(DeviceClientConfig.class, (IotHubConnectionString)any, DeviceClientConfig.AuthType.SAS_TOKEN);
                times = 1;
                Deencapsulation.newInstance("com.microsoft.azure.sdk.iot.device.DeviceIO",
                        new Class[] {DeviceClientConfig.class, IotHubClientProtocol.class, long.class, long.class},
                        (DeviceClientConfig)any, protocol, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS_AMQPS);
                times = 0;
            }
        };
    }

    // Tests_SRS_DEVICECLIENT_21_006: [The open shall invoke super.open().]
    @Test
    public void openOpensDeviceIOSuccess(final @Mocked InternalClient mockedInternalClient) throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.open();

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedInternalClient, "open", false);
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICECLIENT_34_040: [If this object is not using a transport client, it shall invoke super.close().]
    @Test
    public void closeClosesTransportSuccess(final @Mocked InternalClient mockedInternalClient) throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.close();

        // assert
        new Verifications()
        {
            {
                mockedInternalClient.close();
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICECLIENT_34_041: [If this object is not using a transport client, it shall invoke super.closeNow().]
    @Test
    public void closeNowClosesTransportSuccess(final @Mocked InternalClient mockedInternalClient) throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.closeNow();

        // assert
        new Verifications()
        {
            {
                mockedInternalClient.closeNow();
                times = 1;
            }
        };
    }

    // Tests_SRS_DEVICECLIENT_02_015: [If optionName is null or not an option handled by the client, then it shall throw IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setOptionWithNullOptionNameThrows()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol);

        long someMilliseconds = 4;

        // act
        client.setOption(null, someMilliseconds);
    }

    // Tests_SRS_DEVICECLIENT_02_015: [If optionName is null or not an option handled by the client, then it shall throw IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setOptionWithUnknownOptionNameThrows()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol);

        long someMilliseconds = 4;

        // act
        client.setOption("thisIsNotAHandledOption", someMilliseconds);
    }

    //Tests_SRS_DEVICECLIENT_02_017: [Available for all protocols]
    @Test
    public void setOptionSetReceiveIntervalWithMQTTsucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
        final long someMilliseconds = 4L;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.MQTT;
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol, (ClientOptions) null);

        // act
        client.setOption("SetReceiveInterval", someMilliseconds);

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.setReceivePeriodInMilliseconds(someMilliseconds);
            }
        };
    }

    //Tests_SRS_DEVICECLIENT_02_018: [Value needs to have type long].
    @Test (expected = IllegalArgumentException.class)
    public void setOptionMinimumPollingIntervalWithStringInsteadOfLongFails()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol, (ClientOptions) null);

        // act
        client.setOption("SetMinimumPollingInterval", "thisIsNotALong");
    }

    //Tests_SRS_DEVICECLIENT_02_016: ["SetMinimumPollingInterval" - time in milliseconds between 2 consecutive polls.]
    @Test
    public void setOptionMinimumPollingIntervalSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol, (ClientOptions) null);
        final long value = 3L;

        // act
        client.setOption("SetMinimumPollingInterval", value);

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.setReceivePeriodInMilliseconds(value);
            }
        };
    }

    // Tests_SRS_DEVICECLIENT_21_040: ["SetSendInterval" - time in milliseconds between 2 consecutive message sends.]
    @Test
    public void setOptionSendIntervalSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol, (ClientOptions) null);
        final long value = 3L;

        // act
        client.setOption("SetSendInterval", value);

        // assert
        new Verifications()
        {
            {
                mockDeviceIO.setSendPeriodInMilliseconds(value);
            }
        };
    }

    @Test (expected = IllegalArgumentException.class)
    public void setOptionSendIntervalWithStringInsteadOfLongFails()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.setOption("SetSendInterval", "thisIsNotALong");
    }

    @Test (expected = IllegalArgumentException.class)
    public void setOptionValueNullThrows()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol);

        // act
        client.setOption("", null);
    }

    //Tests_SRS_DEVICECLIENT_25_022: [**"SetSASTokenExpiryTime" should have value type long.]
    @Test (expected = IllegalArgumentException.class)
    public void setOptionSASTokenExpiryTimeWithStringInsteadOfLongFails()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol, (ClientOptions) null);

        // act
        client.setOption("SetSASTokenExpiryTime", "thisIsNotALong");
    }

    //Tests_SRS_DEVICECLIENT_25_021: ["SetSASTokenExpiryTime" - time in seconds after which SAS Token expires.]
    @Test
    public void setOptionSASTokenExpiryTimeHTTPSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };
        DeviceClient client = new DeviceClient(connString, protocol, (ClientOptions) null);
        final long value = 60;

        // act
        client.setOption("SetSASTokenExpiryTime", value);

        // assert
        new Verifications()
        {
            {
                mockConfig.getSasTokenAuthentication().setTokenValidSecs(value);
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICECLIENT_25_021: ["SetSASTokenExpiryTime" - time in seconds after which SAS Token expires.]
    @Test
    public void setOptionSASTokenExpiryTimeAfterClientOpenHTTPSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getSasTokenAuthentication().canRefreshToken();
                result = true;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;

        DeviceClient client = new DeviceClient(connString, protocol, (ClientOptions) null);
        client.open();
        final long value = 60;

        // act
        client.setOption("SetSASTokenExpiryTime", value);

        // assert
        new Verifications()
        {
            {
                mockConfig.getSasTokenAuthentication().setTokenValidSecs(value);
                times = 1;
            }
        };
    }

    /*Tests_SRS_DEVICECLIENT_25_024: ["SetSASTokenExpiryTime" shall restart the transport
                                    1. If the device currently uses device key and
                                    2. If transport is already open
                                    after updating expiry time.]
    */
    @Test
    public void setOptionSASTokenExpiryTimeAfterClientOpenTransportWithSasTokenSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockIotHubSasTokenAuthenticationProvider.canRefreshToken();
                result = true;
            }
        };
        final String connString = "some string";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;

        DeviceClient client = new DeviceClient(connString, protocol, (ClientOptions) null);
        client.open();
        final long value = 60;

        // act
        client.setOption("SetSASTokenExpiryTime", value);

        // assert
        new Verifications()
        {
            {
                mockConfig.getSasTokenAuthentication().setTokenValidSecs(value);
                times = 1;
            }
        };
    }
    //Tests_SRS_DEVICECLIENT_25_021: ["SetSASTokenExpiryTime" - Time in secs to specify SAS Token Expiry time.]
    @Test
    public void setOptionSASTokenExpiryTimeAMQPSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };

        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        DeviceClient client = new DeviceClient(connString, protocol, (ClientOptions) null);
        final long value = 60;

        // act
        client.setOption("SetSASTokenExpiryTime", value);

        // assert
        new Verifications()
        {
            {
                mockConfig.getSasTokenAuthentication().setTokenValidSecs(value);
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICECLIENT_25_021: ["SetSASTokenExpiryTime" - time in seconds after which SAS Token expires.]
    //Tests_SRS_DEVICECLIENT_25_024: ["SetSASTokenExpiryTime" shall restart the transport if transport is already open after updating expiry time.]
    @Test
    public void setOptionSASTokenExpiryTimeAfterClientOpenAMQPSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getSasTokenAuthentication().canRefreshToken();
                result = true;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;

        DeviceClient client = new DeviceClient(connString, protocol, (ClientOptions) null);
        client.open();
        final long value = 60;

        // act
        client.setOption("SetSASTokenExpiryTime", value);

        // assert
        new Verifications()
        {
            {
                mockConfig.getSasTokenAuthentication().setTokenValidSecs(value);
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICECLIENT_25_021: [**"SetSASTokenExpiryTime" - Time in secs to specify SAS Token Expiry time.]
    @Test
    public void setOptionSASTokenExpiryTimeMQTTSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations() {
            {
                mockDeviceIO.isOpen();
                result = false;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getSasTokenAuthentication().canRefreshToken();
                result = true;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

        DeviceClient client = new DeviceClient(connString, protocol, (ClientOptions) null);
        final long value = 60;

        // act
        client.setOption("SetSASTokenExpiryTime", value);

        // assert
        new Verifications()
        {
            {
                mockConfig.getSasTokenAuthentication().setTokenValidSecs(value);
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICECLIENT_25_021: ["SetSASTokenExpiryTime" - time in seconds after which SAS Token expires.]
    //Tests_SRS_DEVICECLIENT_25_024: ["SetSASTokenExpiryTime" shall restart the transport if transport is already open after updating expiry time.]
    @Test
    public void setOptionSASTokenExpiryTimeAfterClientOpenMQTTSucceeds()
            throws IOException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getSasTokenAuthentication().canRefreshToken();
                result = true;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

        DeviceClient client = new DeviceClient(connString, protocol, (ClientOptions) null);
        client.open();
        final long value = 60;

        // act
        client.setOption("SetSASTokenExpiryTime", value);

        // assert
        new Verifications()
        {
            {
                mockConfig.getSasTokenAuthentication().setTokenValidSecs(value);
                times = 1;
            }
        };
    }

    // Tests_SRS_DEVICECLIENT_34_075: [If the provided connection string contains a module id field, this function shall throw an UnsupportedOperationException.]
    @Test (expected = UnsupportedOperationException.class)
    public void ConstructorThrowsIfConnStringContainsModuleIdField() throws URISyntaxException
    {
        // arrange
        final String connString = "HostName=iothub.device.com;CredentialType=SharedAccessKey;deviceId=testdevice;"
                + "SharedAccessKey=adjkl234j52=";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        new NonStrictExpectations()
        {
            {
                mockDeviceIO.isOpen();
                result = true;
                mockDeviceIO.getProtocol();
                result = IotHubClientProtocol.HTTPS;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.X509_CERTIFICATE;
                mockConfig.getModuleId();
                result = "any module id";
            }
        };

        //act
        new DeviceClient(connString, protocol);
    }

    // Tests_SRS_DEVICECLIENT_12_028: [The constructor shall shall set the config, deviceIO and tranportClient to null.]
    @Test
    public void unusedConstructor()
    {
        // act
        DeviceClient client = Deencapsulation.newInstance(DeviceClient.class);

        // assert
        assertNull(Deencapsulation.getField(client, "config"));
        assertNull(Deencapsulation.getField(client, "deviceIO"));
    }

    @Test
    public void setAmqpOpenAuthenticationSessionTimeout()
    {
        //arrange
        final int timeoutInSeconds = 10;
        DeviceClient client = Deencapsulation.newInstance(DeviceClient.class);
        Deencapsulation.setField(client, "config", mockConfig);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);

        new NonStrictExpectations()
        {
            {
                mockConfig.getProtocol();
                result = IotHubClientProtocol.AMQPS;

                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };

        //act
        client.setOption("SetAmqpOpenAuthenticationSessionTimeout", timeoutInSeconds);

        //assert
        new Verifications() {
            {
                Deencapsulation.invoke(mockConfig, "setAmqpOpenAuthenticationSessionTimeout", timeoutInSeconds);
                times = 1;
            }
        };
    }

    @Test
    public void setAmqpWsOpenAuthenticationSessionTimeout()
    {
        //arrange
        final int timeoutInSeconds = 10;
        DeviceClient client = Deencapsulation.newInstance(DeviceClient.class);
        Deencapsulation.setField(client, "config", mockConfig);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);

        new NonStrictExpectations()
        {
            {
                mockConfig.getProtocol();
                result = IotHubClientProtocol.AMQPS_WS;

                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };

        //act
        client.setOption("SetAmqpOpenAuthenticationSessionTimeout", timeoutInSeconds);

        //assert
        new Verifications() {
            {
                Deencapsulation.invoke(mockConfig, "setAmqpOpenAuthenticationSessionTimeout", timeoutInSeconds);
                times = 1;
            }
        };
    }

    @Test
    public void setNullAmqpWsOpenAuthenticationSessionTimeoutThrows()
    {
        //arrange
        DeviceClient client = Deencapsulation.newInstance(DeviceClient.class);
        Deencapsulation.setField(client, "config", mockConfig);

        try {
            //act
            client.setOption("setAmqpOpenAuthenticationSessionTimeout", null);
        } catch (IllegalArgumentException expected) {
            //assert
            assertEquals("value is null", expected.getMessage());
        }
    }

    @Test
    public void setIncorrectAmqpOpenAuthenticationSessionTimeoutThrows()
    {
        //arrange
        String timeoutInSeconds = "ten";
        DeviceClient client = Deencapsulation.newInstance(DeviceClient.class);
        Deencapsulation.setField(client, "config", mockConfig);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);

        new NonStrictExpectations()
        {
            {
                mockConfig.getProtocol();
                result = IotHubClientProtocol.AMQPS;

                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };

        try {
            //act
            client.setOption("SetAmqpOpenAuthenticationSessionTimeout", timeoutInSeconds);
        } catch (IllegalArgumentException expected) {
            //assert
            assertEquals("value is not int = " + timeoutInSeconds, expected.getMessage());
        }
    }

    @Test
    public void setAmqpOpenAuthenticationSessionTimeoutForHttpThrows()
    {
        //arrange
        int timeoutInSeconds = 10;
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        DeviceClient client = Deencapsulation.newInstance(DeviceClient.class);
        Deencapsulation.setField(client, "config", mockConfig);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);

        new NonStrictExpectations()
        {
            {
                mockConfig.getProtocol();
                result = protocol;
            }
        };

        try {
            //act
            client.setOption("SetAmqpOpenAuthenticationSessionTimeout", timeoutInSeconds);
        } catch (UnsupportedOperationException expected) {
            //assert
            assertEquals("Cannot set the open authentication session timeout when using protocol " + protocol, expected.getMessage());
        }
    }

    @Test
    public void setAmqpOpenAuthenticationSessionTimeoutForMqttThrows()
    {
        //arrange
        int timeoutInSeconds = 10;
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
        DeviceClient client = Deencapsulation.newInstance(DeviceClient.class);
        Deencapsulation.setField(client, "config", mockConfig);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);

        new NonStrictExpectations()
        {
            {
                mockConfig.getProtocol();
                result = protocol;
            }
        };

        try {
            //act
            client.setOption("SetAmqpOpenAuthenticationSessionTimeout", timeoutInSeconds);
        } catch (UnsupportedOperationException expected) {
            //assert
            assertEquals("Cannot set the open authentication session timeout when using protocol " + protocol, expected.getMessage());
        }
    }

    @Test
    public void setAmqpOpenAuthenticationSessionTimeoutForMqttWsThrows()
    {
        //arrange
        int timeoutInSeconds = 10;
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT_WS;
        DeviceClient client = Deencapsulation.newInstance(DeviceClient.class);
        Deencapsulation.setField(client, "config", mockConfig);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);

        new NonStrictExpectations()
        {
            {
                mockConfig.getProtocol();
                result = protocol;
            }
        };

        try {
            //act
            client.setOption("SetAmqpOpenAuthenticationSessionTimeout", timeoutInSeconds);
        } catch (UnsupportedOperationException expected) {
            //assert
            assertEquals("Cannot set the open authentication session timeout when using protocol " + protocol, expected.getMessage());
        }
    }

    @Test
    public void setAmqpOpenAuthenticationSessionTimeoutForX509Throws()
    {
        //arrange
        int timeoutInSeconds = 10;
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final DeviceClientConfig.AuthType authType = DeviceClientConfig.AuthType.X509_CERTIFICATE;
        DeviceClient client = Deencapsulation.newInstance(DeviceClient.class);
        Deencapsulation.setField(client, "config", mockConfig);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);

        new NonStrictExpectations()
        {
            {
                mockConfig.getProtocol();
                result = protocol;

                mockConfig.getAuthenticationType();
                result = authType;
            }
        };

        try {
            //act
            client.setOption("SetAmqpOpenAuthenticationSessionTimeout", timeoutInSeconds);
        } catch (UnsupportedOperationException expected) {
            //assert
            assertEquals("Cannot set the open authentication session timeout when using authentication type " + authType, expected.getMessage());
        }
    }

    @Test
    public void setOpenDeviceSessionsTimeout()
    {
        //arrange
        final int timeoutInSeconds = 10;
        DeviceClient client = Deencapsulation.newInstance(DeviceClient.class);
        Deencapsulation.setField(client, "config", mockConfig);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);

        new NonStrictExpectations()
        {
            {
                mockConfig.getProtocol();
                result = IotHubClientProtocol.AMQPS;
            }
        };

        //act
        client.setOption("SetAmqpOpenDeviceSessionsTimeout", timeoutInSeconds);

        //assert
        new Verifications() {
            {
                Deencapsulation.invoke(mockConfig, "setAmqpOpenDeviceSessionsTimeout", timeoutInSeconds);
                times = 1;
            }
        };
    }

    @Test
    public void setNullOpenDeviceSessionsTimeoutThrows()
    {
        //arrange
        DeviceClient client = Deencapsulation.newInstance(DeviceClient.class);
        Deencapsulation.setField(client, "config", mockConfig);

        try {
            //act
            client.setOption("SetAmqpOpenDeviceSessionsTimeout", null);
        } catch (IllegalArgumentException expected) {
            //assert
            assertEquals("value is null", expected.getMessage());
        }
    }

    @Test
    public void setIncorrectOpenDeviceSessionsTimeoutThrows()
    {
        //arrange
        String timeoutInSeconds = "ten";
        DeviceClient client = Deencapsulation.newInstance(DeviceClient.class);
        Deencapsulation.setField(client, "config", mockConfig);
        Deencapsulation.setField(client, "deviceIO", mockDeviceIO);

        new NonStrictExpectations()
        {
            {
                mockConfig.getProtocol();
                result = IotHubClientProtocol.AMQPS;
            }
        };

        try {
            //act
            client.setOption("SetAmqpOpenDeviceSessionsTimeout", timeoutInSeconds);
        } catch (IllegalArgumentException expected) {
            //assert
            assertEquals("value is not int = " + timeoutInSeconds, expected.getMessage());
        }
    }

    public void setOpenDeviceSessionsTimeoutForHttpThrows()
    {
        //arrange
        String timeoutInSeconds = "ten";
        final IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        DeviceClient client = Deencapsulation.newInstance(DeviceClient.class);
        Deencapsulation.setField(client, "config", mockConfig);

        new NonStrictExpectations()
        {
            {
                mockConfig.getProtocol();
                result = protocol;
            }
        };

        try {
            //act
            client.setOption("SetAmqpOpenDeviceSessionsTimeout", timeoutInSeconds);
        } catch (IllegalArgumentException expected) {
            //assert
            assertEquals("Cannot set the open device session timeout when using protocol " + protocol, expected.getMessage());
        }
    }

    public void setOpenDeviceSessionsTimeoutForMqttThrows()
    {
        //arrange
        String timeoutInSeconds = "ten";
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
        DeviceClient client = Deencapsulation.newInstance(DeviceClient.class);
        Deencapsulation.setField(client, "config", mockConfig);

        new NonStrictExpectations()
        {
            {
                mockConfig.getProtocol();
                result = protocol;
            }
        };

        try {
            //act
            client.setOption("SetAmqpOpenDeviceSessionsTimeout", timeoutInSeconds);
        } catch (IllegalArgumentException expected) {
            //assert
            assertEquals("Cannot set the open device session timeout when using protocol " + protocol, expected.getMessage());
        }
    }

    public void setOpenDeviceSessionsTimeoutForMqttWsThrows()
    {
        //arrange
        String timeoutInSeconds = "ten";
        final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT_WS;
        DeviceClient client = Deencapsulation.newInstance(DeviceClient.class);
        Deencapsulation.setField(client, "config", mockConfig);

        new NonStrictExpectations()
        {
            {
                mockConfig.getProtocol();
                result = protocol;
            }
        };

        try {
            //act
            client.setOption("SetAmqpOpenDeviceSessionsTimeout", timeoutInSeconds);
        } catch (IllegalArgumentException expected) {
            //assert
            assertEquals("Cannot set the open device session timeout when using protocol " + protocol, expected.getMessage());
        }
    }

    private void deviceClientInstanceExpectation(final IotHubClientProtocol protocol)
    {
        final long receivePeriod;
        if (protocol == IotHubClientProtocol.HTTPS)
        {
            receivePeriod = RECEIVE_PERIOD_MILLIS_HTTPS;
        }
        else
        {
            receivePeriod = RECEIVE_PERIOD_MILLIS_AMQPS;
        }

        new Expectations()
        {
            {
                Deencapsulation.newInstance(DeviceClientConfig.class, mockIotHubConnectionString);
                result = mockConfig;
                Deencapsulation.newInstance(DeviceIO.class,
                        mockConfig, SEND_PERIOD_MILLIS, receivePeriod);
                result = mockDeviceIO;
            }
        };
    }
}
