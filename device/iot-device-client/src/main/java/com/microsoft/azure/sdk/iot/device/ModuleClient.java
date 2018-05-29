/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;

import java.net.URISyntaxException;
import java.util.Map;

/**
 * Public API for communicating from Edge Modules. A ModuleClient can be used to send messages from an Edge module to an EdgeHub or an IotHub.
 * It can also send twin updates and listen for method calls from an EdgeHub or IotHub as well
 */
public class ModuleClient extends InternalClient
{
    private static long SEND_PERIOD_MILLIS = 10;

    private static long RECEIVE_PERIOD_MILLIS_AMQPS = 10;
    private static long RECEIVE_PERIOD_MILLIS_MQTT = 10;
    private static long RECEIVE_PERIOD_MILLIS_HTTPS = 25 * 60 * 1000; /*25 minutes*/

    private static final String IotEdgedUriVariableName = "IOTEDGE_IOTEDGEDURI";
    private static final String IotEdgedApiVersionVariableName = "IOTEDGE_IOTEDGEDVERSION";
    private static final String IotHubHostnameVariableName = "IOTEDGE_IOTHUBHOSTNAME";
    private static final String GatewayHostnameVariableName = "IOTEDGE_GATEWAYHOSTNAME";
    private static final String DeviceIdVariableName = "IOTEDGE_DEVICEID";
    private static final String ModuleIdVariableName = "IOTEDGE_MODULEID";
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
     */
    public ModuleClient(String connectionString, IotHubClientProtocol protocol) throws URISyntaxException, IllegalArgumentException, UnsupportedOperationException
    {
        //Codes_SRS_MODULECLIENT_34_006: [This function shall invoke the super constructor.]
        super(new IotHubConnectionString(connectionString), protocol, SEND_PERIOD_MILLIS, getReceivePeriod(protocol));

        //Codes_SRS_MODULECLIENT_34_007: [If the provided protocol is not MQTT, AMQPS, MQTT_WS, or AMQPS_WS, this function shall throw an UnsupportedOperationException.]
        //Codes_SRS_MODULECLIENT_34_004: [If the provided connection string does not contain a module id, this function shall throw an IllegalArgumentException.]
        commonConstructorVerifications(protocol, this.getConfig());
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

        String connectionString = envVariables.get(EdgehubConnectionstringVariableName);

        if (connectionString == null)
        {
            connectionString = envVariables.get(IothubConnectionstringVariableName);
        }

        // First try to create from connection string and if env variable for connection string is not found try to create from edgedUri
        if (connectionString != null)
        {
            try
            {
                return new ModuleClient(connectionString, IotHubClientProtocol.AMQPS);
            }
            catch (URISyntaxException e)
            {
                throw new ModuleClientException(e);
            }
        }
        else
        {
            throw new UnsupportedOperationException("createFromEnvironment does not support using HSM for authentication. Please use a connection string with a sas token or shared access key");

            /*
            String edgedUri = envVariables.get(IotEdgedUriVariableName);
            String deviceId = envVariables.get(DeviceIdVariableName);
            String moduleId = envVariables.get(ModuleIdVariableName);
            String hostname = envVariables.get(IotHubHostnameVariableName);
            String authScheme = envVariables.get(AuthSchemeVariableName);
            String gatewayHostname = envVariables.get(GatewayHostnameVariableName);
            String apiVersion = envVariables.get(IotEdgedApiVersionVariableName);

            if (edgedUri == null)
            {
                throw new ModuleClientException("Environment variable" + IotEdgedUriVariableName + " is required.");
            }

            if (deviceId == null)
            {
                throw new ModuleClientException("Environment variable" + DeviceIdVariableName + " is required.");
            }

            if (moduleId == null)
            {
                throw new ModuleClientException("Environment variable" + ModuleIdVariableName + " is required.");
            }
            if (hostname == null)
            {
                throw new ModuleClientException("Environment variable" + IotHubHostnameVariableName + " is required.");
            }

            if (authScheme == null)
            {
                throw new ModuleClientException("Environment variable" + AuthSchemeVariableName + " is required.");
            }

            if (!authScheme.equals(SasTokenAuthScheme))
            {
                throw new ModuleClientException("Unsupported authentication scheme. Supported scheme is " + SasTokenAuthScheme + ".");
            }

            SignatureProvider signatureProvider;
            if (apiVersion == null || apiVersion.isEmpty())
            {
                signatureProvider = new HttpHsmSignatureProvider(edgedUri);
            }
            else
            {
                signatureProvider = new HttpHsmSignatureProvider(edgedUri, apiVersion);
            }

            try
            {
                AuthenticationMethod authenticationMethod = new ModuleAuthenticationWithHsm(signatureProvider, deviceId, moduleId, hostname, gatewayHostname);
                return new ModuleClient(authenticationMethod, protocol, SEND_PERIOD_MILLIS, getReceivePeriod(protocol));
            }
            catch (IOException e)
            {
                throw new ModuleClientException(e);
            }
            */
        }
    }

    //private ModuleClient(AuthenticationMethod authenticationMethod, IotHubClientProtocol protocol, long sendPeriodMillis, long receivePeriodMillis) throws IOException
    //{
    //    super(authenticationMethod, protocol, sendPeriodMillis, receivePeriodMillis);
    //}


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
            throw new UnsupportedOperationException("Only MQTT and AMQPS are supported for ModuleClient.");
        }

        if (config.getModuleId() == null || config.getModuleId().isEmpty())
        {
            throw new IllegalArgumentException("Connection string must contain field for ModuleId");
        }
    }
}
