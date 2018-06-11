/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device;


import com.microsoft.azure.sdk.iot.device.auth.IotHubAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.auth.SignatureProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.hsm.HttpHsmSignatureProvider;
import com.microsoft.azure.sdk.iot.device.hsm.IotHubSasTokenHsmAuthenticationProvider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Public API for communicating from Edge Modules. A ModuleClient can be used to send messages from an Edge module to an EdgeHub or an IotHub.
 * It can also send twin updates and listen for method calls from an EdgeHub or IotHub as well
 */
public class ModuleClient extends InternalClient
{
    private static final String DEFAULT_API_VERSION = "2018-06-28";

    private static long SEND_PERIOD_MILLIS = 10;

    private static long RECEIVE_PERIOD_MILLIS_AMQPS = 10;
    private static long RECEIVE_PERIOD_MILLIS_MQTT = 10;
    private static long RECEIVE_PERIOD_MILLIS_HTTPS = 25 * 60 * 1000; /*25 minutes*/

    private static int DEFAULT_SAS_TOKEN_TIME_TO_LIVE_SECONDS = 60 * 60; //1 hour
    private static int DEFAULT_SAS_TOKEN_BUFFER_PERCENTAGE = 85; //Token will go 85% of its life before renewing

    private static final String IotEdgedUriVariableName = "IOTEDGE_WORKLOADURI";
    private static final String IotHubHostnameVariableName = "IOTEDGE_IOTHUBHOSTNAME";
    private static final String GatewayHostnameVariableName = "IOTEDGE_GATEWAYHOSTNAME";
    private static final String DeviceIdVariableName = "IOTEDGE_DEVICEID";
    private static final String ModuleIdVariableName = "IOTEDGE_MODULEID";
    private static final String ModuleGenerationIdVariableName = "IOTEDGE_MODULEGENERATIONID";
    private static final String AuthSchemeVariableName = "IOTEDGE_AUTHSCHEME";
    private static final String SasTokenAuthScheme = "SasToken";

    private static final String EdgehubConnectionstringVariableName = "EdgeHubConnectionString";
    private static final String IothubConnectionstringVariableName = "IotHubConnectionString";
    
    /**
     * Constructor for a ModuleClient instance.
     * @param connectionString The connection string for the edge module to connect to. Must be in format
     *                         HostName=xxxx;deviceId=xxxx;SharedAccessKey=
     *                         xxxx;moduleId=xxxx;
     *
     *                         or
     *
     *                         HostName=xxxx;DeviceId=xxxx;SharedAccessKey=
     *                         xxxx;moduleId=xxxx;HostNameGateway=xxxx
     * @param protocol The protocol to use when communicating with the module
     * @throws ModuleClientException if an exception is encountered when parsing the connection string
     * @throws UnsupportedOperationException if using any protocol besides MQTT, if the connection string is missing
     * the "moduleId" field, or if the connection string uses x509
     * @throws IllegalArgumentException if the provided connection string is null or empty, or if the provided protocol is null
     * @throws URISyntaxException if the connection string cannot be parsed for a valid hostname
     */
    public ModuleClient(String connectionString, IotHubClientProtocol protocol) throws URISyntaxException, IllegalArgumentException, UnsupportedOperationException, ModuleClientException
    {
        //Codes_SRS_MODULECLIENT_34_006: [This function shall invoke the super constructor.]
        super(new IotHubConnectionString(connectionString), protocol, SEND_PERIOD_MILLIS, getReceivePeriod(protocol));

        //Codes_SRS_MODULECLIENT_34_007: [If the provided protocol is not MQTT, AMQPS, MQTT_WS, or AMQPS_WS, this function shall throw an UnsupportedOperationException.]
        //Codes_SRS_MODULECLIENT_34_004: [If the provided connection string does not contain a module id, this function shall throw an IllegalArgumentException.]
        commonConstructorVerifications(protocol, this.config);
    }

    /**
     * Create a module client instance that uses x509 authentication.
     *
     * <p>Note! Communication from a module to another EdgeHub using x509 authentication is not currently supported and
     * the service will always return "UNAUTHORIZED"</p>
     *
     * <p>Communication from a module directly to the IotHub does support x509 authentication, though.</p>
     * @param connectionString The connection string for the edge module to connect to. Must be in format
     *                         HostName=xxxx;deviceId=xxxx;SharedAccessKey=
     *                         xxxx;moduleId=xxxx;
     *
     *                         or
     *
     *                         HostName=xxxx;DeviceId=xxxx;SharedAccessKey=
     *                         xxxx;moduleId=xxxx;HostNameGateway=xxxx
     * @param protocol The protocol to communicate with
     * @param publicKeyCertificate The PEM formatted string for the public key certificate or the system path to the file containing the PEM.
     * @param isCertificatePath 'false' if the publicKeyCertificate argument is a path to the PEM, and 'true' if it is the PEM string itself,
     * @param privateKey The PEM formatted string for the private key or the system path to the file containing the PEM.
     * @param isPrivateKeyPath 'false' if the privateKey argument is a path to the PEM, and 'true' if it is the PEM string itself,
     * @throws URISyntaxException If the connString cannot be parsed
     */
    public ModuleClient(String connectionString, IotHubClientProtocol protocol, String publicKeyCertificate, boolean isCertificatePath, String privateKey, boolean isPrivateKeyPath) throws URISyntaxException
    {
        super(new IotHubConnectionString(connectionString), protocol, publicKeyCertificate, isCertificatePath, privateKey, isPrivateKeyPath, SEND_PERIOD_MILLIS, getReceivePeriod(protocol));

        //Codes_SRS_MODULECLIENT_34_008: [If the provided protocol is not MQTT, AMQPS, MQTT_WS, or AMQPS_WS, this function shall throw an UnsupportedOperationException.]
        //Codes_SRS_MODULECLIENT_34_009: [If the provided connection string does not contain a module id, this function shall throw an IllegalArgumentException.]
        commonConstructorVerifications(protocol, this.getConfig());
    }

    public static ModuleClient createFromEnvironment() throws ModuleClientException
    {
        return createFromEnvironment(IotHubClientProtocol.AMQPS);
    }

    public static ModuleClient createFromEnvironment(IotHubClientProtocol protocol) throws ModuleClientException
    {
        Map<String, String> envVariables = System.getenv();

        //Codes_SRS_MODULECLIENT_34_013: [This function shall check for a saved edgehub connection string.]
        String connectionString = envVariables.get(EdgehubConnectionstringVariableName);
        if (connectionString == null)
        {
            //Codes_SRS_MODULECLIENT_34_019: [If no edgehub connection string is present, this function shall check for a saved iothub connection string.]
            connectionString = envVariables.get(IothubConnectionstringVariableName);
        }

        // First try to create from connection string and if env variable for connection string is not found try to create from edgedUri
        if (connectionString != null)
        {
            try
            {
                //Codes_SRS_MODULECLIENT_34_020: [If an edgehub or iothub connection string is present, this function shall create a module client instance using that connection string and the provided protocol.]
                return new ModuleClient(connectionString, protocol);
            }
            catch (URISyntaxException e)
            {
                throw new ModuleClientException(e);
            }
        }
        else
        {
            //Codes_SRS_MODULECLIENT_34_014: [This function shall check for environment variables for edgedUri, deviceId, moduleId,
            // hostname, authScheme, gatewayHostname, and apiVersion. If any of these other than apiVersion or gatewayHostname is missing,
            // this function shall throw a ModuleClientException.]
            String edgedUri = envVariables.get(IotEdgedUriVariableName);
            String deviceId = envVariables.get(DeviceIdVariableName);
            String moduleId = envVariables.get(ModuleIdVariableName);
            String hostname = envVariables.get(IotHubHostnameVariableName);
            String authScheme = envVariables.get(AuthSchemeVariableName);
            String gatewayHostname = envVariables.get(GatewayHostnameVariableName);
            String generationId = envVariables.get(ModuleGenerationIdVariableName);

            if (edgedUri == null)
            {
                throw new ModuleClientException("Environment variable " + IotEdgedUriVariableName + " is required.");
            }

            if (deviceId == null)
            {
                throw new ModuleClientException("Environment variable " + DeviceIdVariableName + " is required.");
            }

            if (moduleId == null)
            {
                throw new ModuleClientException("Environment variable " + ModuleIdVariableName + " is required.");
            }
            if (hostname == null)
            {
                throw new ModuleClientException("Environment variable " + IotHubHostnameVariableName + " is required.");
            }

            if (authScheme == null)
            {
                throw new ModuleClientException("Environment variable " + AuthSchemeVariableName + " is required.");
            }

            if (generationId == null)
            {
                throw new ModuleClientException("Environment variable " + ModuleGenerationIdVariableName + " is required");
            }

            if (!authScheme.equals(SasTokenAuthScheme))
            {
                //Codes_SRS_MODULECLIENT_34_030: [If the auth scheme environment variable is not "SasToken", this function shall throw a moduleClientException.]
                throw new ModuleClientException("Unsupported authentication scheme. Supported scheme is " + SasTokenAuthScheme + ".");
            }

            SignatureProvider signatureProvider;
            try
            {
                signatureProvider = new HttpHsmSignatureProvider(edgedUri, DEFAULT_API_VERSION);
            }
            catch (NoSuchAlgorithmException | URISyntaxException e)
            {
                throw new ModuleClientException("Could not use Hsm Signature Provider", e);
            }

            try
            {
                //Codes_SRS_MODULECLIENT_34_017: [This function shall create an authentication provider using the created
                // signature provider, and the environment variables for deviceid, moduleid, hostname, gatewayhostname,
                // and the default time for tokens to live and the default sas token buffer time.]
                IotHubAuthenticationProvider iotHubAuthenticationProvider = IotHubSasTokenHsmAuthenticationProvider.create(signatureProvider, deviceId, moduleId, hostname, gatewayHostname, generationId, DEFAULT_SAS_TOKEN_TIME_TO_LIVE_SECONDS, DEFAULT_SAS_TOKEN_BUFFER_PERCENTAGE);

                //Codes_SRS_MODULECLIENT_34_018: [This function return a new ModuleClient instance built from the created authentication provider and the provided protocol.]
                return new ModuleClient(iotHubAuthenticationProvider, protocol, SEND_PERIOD_MILLIS, getReceivePeriod(protocol));
            }
            catch (IOException | TransportException e)
            {
                throw new ModuleClientException(e);
            }
        }
    }

    private ModuleClient(IotHubAuthenticationProvider iotHubAuthenticationProvider, IotHubClientProtocol protocol, long sendPeriodMillis, long receivePeriodMillis) throws IOException, TransportException
    {
        super(iotHubAuthenticationProvider, protocol, sendPeriodMillis, receivePeriodMillis);
    }

    /**
     * Sends a message to a particular outputName asynchronously
     *
     * @param outputName the outputName to route the message to
     * @param message the message to send
     * @param callback the callback to be fired when the message is acknowledged by the service
     * @param callbackContext the context to be included in the callback when fired
     * @throws IllegalArgumentException if the provided outputName is null or empty
     */
    public void sendEventAsync(String outputName, Message message, IotHubEventCallback callback, Object callbackContext) throws IllegalArgumentException
    {
        if (outputName == null || outputName.isEmpty())
        {
            //Codes_SRS_MODULECLIENT_34_001: [If the provided outputName is null or empty, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("outputName cannot be null or empty");
        }

        //Codes_SRS_MODULECLIENT_34_002: [This function shall set the provided message with the provided outputName, device id, and module id properties.]
        this.setModuleProperties(message);
        message.setOutputName(outputName);

        //Codes_SRS_MODULECLIENT_34_003: [This function shall invoke super.sendEventAsync(message, callback, callbackContext).]
        super.sendEventAsync(message, callback, callbackContext);
    }

    /**
     * Sets the message callback.
     *
     * @param callback the message callback. Can be {@code null}.
     * @param context the context to be passed to the callback. Can be {@code null}.
     *
     * @return itself, for fluent setting.
     *
     * @throws IllegalArgumentException if the callback is {@code null} but a context is
     * passed in.
     * @throws IllegalStateException if the callback is set after the client is
     * closed.
     */
    public ModuleClient setMessageCallback(MessageCallback callback, Object context)
    {
        this.setInternalMessageCallback(callback, context);
        return this;
    }

    /**
     * Sets the message callback to be fired when a telemetry message arrives on the specified input channel. All other
     * messages will trigger the default message callback in setMessageCallback(MessageCallback callback, Object context).
     * Any message that triggers this callback will not also trigger the default callback.
     *
     * @param inputName the input name channel to listen for.
     * @param callback the message callback. Can be {@code null}.
     * @param context the context to be passed to the callback. Can be {@code null}.
     *
     * @return this object, for fluent setting
     */
    public ModuleClient setMessageCallback(String inputName, MessageCallback callback, Object context)
    {
        if (inputName == null || inputName.isEmpty())
        {
            //Codes_SRS_MODULECLIENT_34_011: [If the provided inputName is null or empty, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("InputName must not be null or empty");
        }

        if (callback == null && context != null)
        {
            //Codes_SRS_MODULECLIENT_34_010: [If the provided callback is null and the provided context is not null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Cannot give non-null context for a null callback.");
        }

        //Codes_SRS_MODULECLIENT_34_012: [This function shall save the provided callback with context in config tied to the provided inputName.]
        this.config.setMessageCallback(inputName, callback, context);
        return this;
    }

    private void setModuleProperties(Message message)
    {
        String deviceId = this.getConfig().getDeviceId();
        String moduleId = this.getConfig().getModuleId();

        message.setUserId(deviceId + "/" + moduleId);
        message.setConnectionModuleId(moduleId);
        message.setConnectionDeviceId(deviceId);
    }

    private static long getReceivePeriod(IotHubClientProtocol protocol)
    {
        switch (protocol)
        {
            case HTTPS:
                return RECEIVE_PERIOD_MILLIS_HTTPS;
            case AMQPS:
            case AMQPS_WS:
                return RECEIVE_PERIOD_MILLIS_AMQPS;
            case MQTT:
            case MQTT_WS:
                return RECEIVE_PERIOD_MILLIS_MQTT;
            default:
                // should never happen.
                throw new IllegalStateException(
                        "Invalid client protocol specified.");
        }
    }

    private static void commonConstructorVerifications(IotHubClientProtocol protocol, DeviceClientConfig config)
    {
        if (protocol != IotHubClientProtocol.MQTT && protocol != IotHubClientProtocol.AMQPS
                && protocol != IotHubClientProtocol.MQTT_WS && protocol != IotHubClientProtocol.AMQPS_WS)
        {
            throw new UnsupportedOperationException("Only MQTT, MQTT_WS, AMQPS and AMQPS_WS are supported for ModuleClient.");
        }

        if (config.getModuleId() == null || config.getModuleId().isEmpty())
        {
            throw new IllegalArgumentException("Connection string must contain field for ModuleId");
        }
    }
}
