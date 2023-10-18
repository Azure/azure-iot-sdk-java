/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device;


import com.microsoft.azure.sdk.iot.device.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.auth.IotHubAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.auth.SignatureProvider;
import com.microsoft.azure.sdk.iot.device.edge.HttpsHsmTrustBundleProvider;
import com.microsoft.azure.sdk.iot.device.edge.DirectMethodRequest;
import com.microsoft.azure.sdk.iot.device.edge.DirectMethodResponse;
import com.microsoft.azure.sdk.iot.device.edge.TrustBundleProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubServiceException;
import com.microsoft.azure.sdk.iot.device.transport.TransportException;
import com.microsoft.azure.sdk.iot.device.hsm.HttpHsmSignatureProvider;
import com.microsoft.azure.sdk.iot.device.hsm.IotHubSasTokenHsmAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.hsm.UnixDomainSocketChannel;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;

/**
 * Public API for communicating from Edge Modules. A ModuleClient can be used to send messages from an Edge module to an EdgeHub or an IotHub.
 * It can also send twin updates and listen for method calls from an EdgeHub or IotHub as well
 */
@Slf4j
public class ModuleClient extends InternalClient
{
    private static final String DEFAULT_API_VERSION = "2018-06-28";

    private static final int DEFAULT_SAS_TOKEN_TIME_TO_LIVE_SECONDS = 60 * 60; //1 hour
    private static final int DEFAULT_SAS_TOKEN_BUFFER_PERCENTAGE = 85; //Token will go 85% of its life before renewing

    private static final String IotEdgedUriVariableName = "IOTEDGE_WORKLOADURI";
    private static final String IotHubHostnameVariableName = "IOTEDGE_IOTHUBHOSTNAME";
    private static final String GatewayHostnameVariableName = "IOTEDGE_GATEWAYHOSTNAME";
    private static final String DeviceIdVariableName = "IOTEDGE_DEVICEID";
    private static final String ModuleIdVariableName = "IOTEDGE_MODULEID";
    private static final String ModuleGenerationIdVariableName = "IOTEDGE_MODULEGENERATIONID";
    private static final String AuthSchemeVariableName = "IOTEDGE_AUTHSCHEME";
    private static final String SasTokenAuthScheme = "sasToken";
    private static final String EdgehubConnectionstringVariableName = "EdgeHubConnectionString";
    private static final String IothubConnectionstringVariableName = "IotHubConnectionString";
    private static final String EdgeCaCertificateFileVariableName = "EdgeModuleCACertificateFile";

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
     * @throws UnsupportedOperationException if using any protocol besides MQTT, if the connection string is missing
     * the "moduleId" field, or if the connection string uses x509
     * @throws IllegalArgumentException if the provided connection string is null or empty, or if the provided protocol is null
     */
    public ModuleClient(String connectionString, IotHubClientProtocol protocol) throws IllegalArgumentException, UnsupportedOperationException
    {
        super(new IotHubConnectionString(connectionString), protocol, null);

        commonConstructorVerifications(protocol, this.config);
        commonConstructorSetup();
    }

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
     * @param clientOptions The options that allow configuration of the module client instance during initialization
     * @throws UnsupportedOperationException if using any protocol besides MQTT, if the connection string is missing
     * the "moduleId" field, or if the connection string uses x509
     * @throws IllegalArgumentException if the provided connection string is null or empty, or if the provided protocol is null
     */
    public ModuleClient(String connectionString, IotHubClientProtocol protocol, ClientOptions clientOptions) throws IllegalArgumentException, UnsupportedOperationException
    {
        super(new IotHubConnectionString(connectionString), protocol, clientOptions);
        commonConstructorVerifications(protocol, this.config);
        commonConstructorSetup();
    }

    /**
     * Constructor that allows for the client's SAS token generation to be controlled by the user. Note that options in
     * this client such as setting the SAS token expiry time will throw {@link UnsupportedOperationException} since
     * the SDK no longer controls that when this constructor is used.
     *
     * @param hostName The host name of the IoT hub that this client will connect to.
     * @param deviceId The Id of the device containing the module that the connection will identify as.
     * @param moduleId The Id of the module that the connection will identify as.
     * @param sasTokenProvider The provider of all SAS tokens that are used during authentication.
     * @param protocol The protocol that the client will connect over.
     */
    public ModuleClient(String hostName, String deviceId, String moduleId, SasTokenProvider sasTokenProvider, IotHubClientProtocol protocol)
    {
        this(hostName, deviceId, moduleId, sasTokenProvider, protocol, null);
    }

    /**
     * Constructor that allows for the client's SAS token generation to be controlled by the user. Note that options in
     * this client such as setting the SAS token expiry time will throw {@link UnsupportedOperationException} since
     * the SDK no longer controls that when this constructor is used.
     *
     * @param hostName The host name of the IoT hub that this client will connect to.
     * @param deviceId The Id of the device containing the module that the connection will identify as.
     * @param moduleId The Id of the module that the connection will identify as.
     * @param sasTokenProvider The provider of all SAS tokens that are used during authentication.
     * @param protocol The protocol that the client will connect over.
     * @param clientOptions The options that allow configuration of the module client instance during initialization.
     */
    public ModuleClient(String hostName, String deviceId, String moduleId, SasTokenProvider sasTokenProvider, IotHubClientProtocol protocol, ClientOptions clientOptions)
    {
        super(hostName, deviceId, moduleId, sasTokenProvider, protocol, clientOptions);
        commonConstructorVerifications(protocol, this.getConfig());
        commonConstructorSetup();
    }

    /**
     * Create a module client instance from your environment variables
     * @param unixDomainSocketChannel the implementation of the {@link UnixDomainSocketChannel} interface that will be used if any
     * unix domain socket communication is required. May be null if no unix domain socket communication is required. If
     * this argument is null and unix domain socket communication is required, this method will through an {@link IllegalArgumentException}.
     * To check if unix domain socket communication is required for your Edge runtime, check its "IOTEDGE_WORKLOADURI"
     * environment variable. If it is not present, or its value is prefixed with "HTTP" or "HTTPS", then no unix domain
     * socket communication is required, and this argument can be set to null. If its value is present and is prefixed
     * with "unix", then unix domain socket communication will be required, and this argument must not be null.
     * @return the created module client instance
     * @throws IotHubClientException if the module client cannot be created
     */
    public static ModuleClient createFromEnvironment(UnixDomainSocketChannel unixDomainSocketChannel) throws IotHubClientException
    {
        return createFromEnvironment(unixDomainSocketChannel, IotHubClientProtocol.AMQPS);
    }

    /**
     * Create a module client instance from your environment variables
     * @param unixDomainSocketChannel the implementation of the {@link UnixDomainSocketChannel} interface that will be used if any
     * unix domain socket communication is required. May be null if no unix domain socket communication is required. If
     * this argument is null and unix domain socket communication is required, this method will through an {@link IllegalArgumentException}.
     * To check if unix domain socket communication is required for your Edge runtime, check its "IOTEDGE_WORKLOADURI"
     * environment variable. If it is not present, or its value is prefixed with "HTTP" or "HTTPS", then no unix domain
     * socket communication is required, and this argument can be set to null. If its value is present and is prefixed
     * with "unix", then unix domain socket communication will be required, and this argument must not be null.
     * @param protocol the protocol the module client instance will use
     * @return the created module client instance
     * @throws IotHubClientException if the module client cannot be created
     */
    public static ModuleClient createFromEnvironment(UnixDomainSocketChannel unixDomainSocketChannel, IotHubClientProtocol protocol) throws IotHubClientException
    {
        return createFromEnvironment(unixDomainSocketChannel, protocol, null);
    }

    /**
     * Create a module client instance from your environment variables
     * @param unixDomainSocketChannel the implementation of the {@link UnixDomainSocketChannel} interface that will be used if any
     * unix domain socket communication is required. May be null if no unix domain socket communication is required. If
     * this argument is null and unix domain socket communication is required, this method will through an {@link IllegalArgumentException}.
     * To check if unix domain socket communication is required for your Edge runtime, check its "IOTEDGE_WORKLOADURI"
     * environment variable. If it is not present, or its value is prefixed with "HTTP" or "HTTPS", then no unix domain
     * socket communication is required, and this argument can be set to null. If its value is present and is prefixed
     * with "unix", then unix domain socket communication will be required, and this argument must not be null.
     * @param protocol the protocol the module client instance will use
     * @param clientOptions The options that allow configuration of the module client instance during initialization
     * @return the created module client instance
     * @throws IotHubClientException if the module client cannot be created
     */
    public static ModuleClient createFromEnvironment(UnixDomainSocketChannel unixDomainSocketChannel, IotHubClientProtocol protocol, ClientOptions clientOptions) throws IotHubClientException
    {
        log.info("Creating module client from environment with protocol {}...", protocol);
        Map<String, String> envVariables = System.getenv();

        log.debug("Checking for an edgehub connection string...");
        String connectionString = envVariables.get(EdgehubConnectionstringVariableName);
        if (connectionString == null)
        {
            log.debug("No edgehub connection string was configured, checking for an IoT hub connection string...");
            connectionString = envVariables.get(IothubConnectionstringVariableName);
        }

        // First try to create from connection string and if env variable for connection string is not found try to create from edgedUri
        if (connectionString != null)
        {
            log.debug("Creating module client with the provided connection string");

            //Check for a different default cert to be used
            String alternativeDefaultTrustedCert = envVariables.get(EdgeCaCertificateFileVariableName);
            SSLContext sslContext;
            if (alternativeDefaultTrustedCert != null && !alternativeDefaultTrustedCert.isEmpty())
            {
                log.debug("Configuring module client to use the configured alternative trusted certificate");
                try
                {
                    sslContext = IotHubSSLContext.getSSLContextFromFile(alternativeDefaultTrustedCert);
                }
                catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e)
                {
                    throw new IotHubClientException(IotHubStatusCode.ERROR, "Failed to create an SSLContext instance from the provided trusted cert file path", e);
                }
            }
            else
            {
                sslContext = new IotHubSSLContext().getSSLContext();
            }

            if (clientOptions != null && clientOptions.getSslContext() == null)
            {
                // Clone the existing client options, but with the new SSLContext
                clientOptions = clientOptions.toBuilder().sslContext(sslContext)
                        .build();
            }
            else if (clientOptions == null)
            {
                // only override the client options completely if the user didn't provide any
                clientOptions = ClientOptions.builder().sslContext(sslContext).build();
            }
            else
            {
                log.debug("Ignoring trusted certs saved in {} environment variable because custom SSLContext was provided in client options.", EdgeCaCertificateFileVariableName);
            }

            return new ModuleClient(connectionString, protocol, clientOptions);
        }
        else
        {
            log.info("No connection string was configured for this module, so it will get its credentials from the edgelet");
            String edgedUri = envVariables.get(IotEdgedUriVariableName);
            String deviceId = envVariables.get(DeviceIdVariableName);
            String moduleId = envVariables.get(ModuleIdVariableName);
            String hostname = envVariables.get(IotHubHostnameVariableName);
            String authScheme = envVariables.get(AuthSchemeVariableName);
            String gatewayHostname = envVariables.get(GatewayHostnameVariableName);
            String generationId = envVariables.get(ModuleGenerationIdVariableName);

            if (edgedUri == null)
            {
                throw new IllegalStateException("Environment variable " + IotEdgedUriVariableName + " is required.");
            }

            if (deviceId == null)
            {
                throw new IllegalStateException("Environment variable " + DeviceIdVariableName + " is required.");
            }

            if (moduleId == null)
            {
                throw new IllegalStateException("Environment variable " + ModuleIdVariableName + " is required.");
            }

            if (hostname == null)
            {
                throw new IllegalStateException("Environment variable " + IotHubHostnameVariableName + " is required.");
            }

            if (authScheme == null)
            {
                throw new IllegalStateException("Environment variable " + AuthSchemeVariableName + " is required.");
            }

            if (generationId == null)
            {
                throw new IllegalStateException("Environment variable " + ModuleGenerationIdVariableName + " is required");
            }

            if (!authScheme.equalsIgnoreCase(SasTokenAuthScheme))
            {
                throw new IllegalStateException("Unsupported authentication scheme. Supported scheme is " + SasTokenAuthScheme + ".");
            }

            SignatureProvider signatureProvider;
            try
            {
                signatureProvider = new HttpHsmSignatureProvider(edgedUri, DEFAULT_API_VERSION, unixDomainSocketChannel);
            }
            catch (NoSuchAlgorithmException | URISyntaxException e)
            {
                throw new IotHubClientException(IotHubStatusCode.ERROR, "Could not use Hsm Signature Provider", e);
            }

            try
            {
                SSLContext sslContext;
                if (gatewayHostname != null && !gatewayHostname.isEmpty())
                {
                    TrustBundleProvider trustBundleProvider = new HttpsHsmTrustBundleProvider();
                    String trustCertificates = trustBundleProvider.getTrustBundleCerts(edgedUri, DEFAULT_API_VERSION, unixDomainSocketChannel);
                    sslContext = IotHubSSLContext.getSSLContextFromString(trustCertificates);
                }
                else
                {
                    sslContext = new IotHubSSLContext().getSSLContext();
                }

                IotHubAuthenticationProvider iotHubAuthenticationProvider =
                    IotHubSasTokenHsmAuthenticationProvider
                        .create(
                            signatureProvider,
                            deviceId,
                            moduleId,
                            hostname,
                            gatewayHostname,
                            generationId,
                            DEFAULT_SAS_TOKEN_TIME_TO_LIVE_SECONDS,
                            DEFAULT_SAS_TOKEN_BUFFER_PERCENTAGE,
                            sslContext);

                return new ModuleClient(iotHubAuthenticationProvider, protocol);
            }
            catch (URISyntaxException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e)
            {
                throw new IotHubClientException(IotHubStatusCode.ERROR, "Failed to handle the provided certificates", e);
            }
            catch (TransportException e)
            {
                throw e.toIotHubClientException();
            }
            catch (IOException e)
            {
                throw new IotHubClientException(IotHubStatusCode.IO_ERROR, e);
            }
        }
    }

    private ModuleClient(IotHubAuthenticationProvider iotHubAuthenticationProvider, IotHubClientProtocol protocol)
    {
        super(iotHubAuthenticationProvider, protocol);
        commonConstructorSetup();
    }

    /**
     * Synchronously sends a message to to a particular outputName.
     *
     * @param message the message to be sent.
     * @param outputName the outputName to route the message to
     *
     * @throws InterruptedException if the operation is interrupted while waiting on the telemetry to be acknowledged by the service.
     * @throws IllegalStateException if the client has not been opened yet or is already closed.
     * @throws IotHubClientException if the request is rejected by the service for any reason of if the synchronous operation times out.
     */
    public void sendEvent(Message message, String outputName)
            throws IllegalStateException, InterruptedException, IotHubClientException
    {
        this.sendEvent(message, outputName, DEFAULT_TIMEOUT_MILLISECONDS);
    }

    /**
     * Synchronously sends a message to to a particular outputName.
     *
     * @param message the message to be sent.
     * @param outputName the outputName to route the message to
     * @param timeoutMilliseconds The maximum number of milliseconds to wait for the service to acknowledge this message.
     * If 0, then it will wait indefinitely.
     *
     * @throws InterruptedException if the operation is interrupted while waiting on the telemetry to be acknowledged by the service.
     * @throws IllegalStateException if the client has not been opened yet or is already closed.
     * @throws IotHubClientException if the request is rejected by the service for any reason of if the synchronous operation times out.
     */
    public void sendEvent(Message message, String outputName, int timeoutMilliseconds)
            throws IllegalStateException, InterruptedException, IotHubClientException
    {
        if (outputName == null || outputName.isEmpty())
        {
            throw new IllegalArgumentException("outputName cannot be null or empty");
        }

        message.setOutputName(outputName);
        this.sendEvent(message, timeoutMilliseconds);
    }

    @Override
    public void sendEvent(Message message)
            throws IllegalArgumentException, InterruptedException, IotHubClientException
    {
        message.setConnectionModuleId(this.config.getModuleId());
        super.sendEvent(message);
    }

    /**
     * Sends a message to a particular outputName asynchronously
     *
     * @param outputName the outputName to route the message to
     * @param message the message to send
     * @param callback the callback to be fired when the message is acknowledged by the service
     * @param callbackContext the context to be included in the callback when fired
     * @throws IllegalStateException if the client has not been opened yet or is already closed.
     */
    public void sendEventAsync(Message message, MessageSentCallback callback, Object callbackContext, String outputName) throws IllegalStateException
    {
        if (outputName == null || outputName.isEmpty())
        {
            throw new IllegalArgumentException("outputName cannot be null or empty");
        }

        message.setOutputName(outputName);
        this.sendEventAsync(message, callback, callbackContext);
    }

    @Override
    public void sendEventAsync(Message message, MessageSentCallback callback, Object callbackContext) throws IllegalArgumentException
    {
        message.setConnectionModuleId(this.config.getModuleId());
        super.sendEventAsync(message, callback, callbackContext);
    }

    /**
     * Invoke a method on a device
     * @param deviceId the device to invoke a method on
     * @param directMethodRequest the request containing the method to invoke on the device
     * @return the result of the method call
     * @throws IotHubClientException if the method cannot be invoked
     * @throws IllegalArgumentException if deviceId is null or empty
     */
    public DirectMethodResponse invokeMethod(String deviceId, DirectMethodRequest directMethodRequest) throws IotHubClientException
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("DeviceId cannot be null or empty");
        }

        try
        {
            HttpsTransportManager httpsTransportManager = new HttpsTransportManager(this.config);
            return httpsTransportManager.invokeMethod(directMethodRequest, deviceId, "");
        }
        catch (IotHubServiceException e)
        {
            throw new IotHubClientException(e.getStatusCode(), "Could not invoke method", e);
        }
        catch (TransportException e)
        {
            throw new IotHubClientException(IotHubStatusCode.IO_ERROR, "Could not invoke method", e);
        }
        catch (IOException e)
        {
            throw new IotHubClientException(IotHubStatusCode.IO_ERROR, e);
        }
    }

    /**
     * Invoke a method on a module
     * @param deviceId the device the module belongs to
     * @param moduleId the module to invoke the method on
     * @param directMethodRequest the request containing the method to invoke on the device
     * @return the result of the method call
     * @throws IotHubClientException if the method cannot be invoked
     * @throws IllegalArgumentException if deviceId is null or empty, or if moduleId is null or empty
     */
    public DirectMethodResponse invokeMethod(String deviceId, String moduleId, DirectMethodRequest directMethodRequest) throws IotHubClientException
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("DeviceId cannot be null or empty");
        }

        if (moduleId == null || moduleId.isEmpty())
        {
            throw new IllegalArgumentException("DeviceId cannot be null or empty");
        }

        try
        {
            HttpsTransportManager httpsTransportManager = new HttpsTransportManager(this.config);
            return httpsTransportManager.invokeMethod(directMethodRequest, deviceId, moduleId);
        }
        catch (IotHubServiceException e)
        {
            throw new IotHubClientException(e.getStatusCode(), "Could not invoke method", e);
        }
        catch (TransportException e)
        {
            throw new IotHubClientException(IotHubStatusCode.IO_ERROR, "Could not invoke method", e);
        }
        catch (IOException e)
        {
            throw new IotHubClientException(IotHubStatusCode.IO_ERROR, e);
        }
    }

    /**
     * Sets the message callback.
     * <p>
     * This should be set before opening the client. If it is set after opening the client, any messages sent by the
     * service may be missed.
     * </p>
     * <p>
     * This callback is preserved between reconnection attempts and preserved after re-opening a previously closed client.
     * </p>
     * @param callback the message callback. Can be {@code null}.
     * @param context the context to be passed to the callback. Can be {@code null}.
     *
     * @return itself, for fluent setting.
     *
     * @throws IllegalArgumentException if the callback is {@code null} but a context is
     * passed in.
     */
    public ModuleClient setMessageCallback(MessageCallback callback, Object context)
    {
        this.setMessageCallbackInternal(callback, context);
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
            throw new IllegalArgumentException("InputName must not be null or empty");
        }

        if (callback == null && context != null)
        {
            throw new IllegalArgumentException("Cannot give non-null context for a null callback.");
        }

        this.config.setMessageCallback(inputName, callback, context);
        return this;
    }

    private static void commonConstructorVerifications(IotHubClientProtocol protocol, ClientConfiguration config)
    {
        if (protocol == IotHubClientProtocol.HTTPS)
        {
            throw new UnsupportedOperationException("Only MQTT, MQTT_WS, AMQPS and AMQPS_WS are supported for ModuleClient.");
        }

        if (config.getModuleId() == null || config.getModuleId().isEmpty())
        {
            throw new IllegalArgumentException("Connection string must contain field for ModuleId");
        }
    }

    private static void commonConstructorSetup()
    {
        log.debug("Initialized a ModuleClient instance using SDK version {}", TransportUtils.CLIENT_VERSION);
    }
}
