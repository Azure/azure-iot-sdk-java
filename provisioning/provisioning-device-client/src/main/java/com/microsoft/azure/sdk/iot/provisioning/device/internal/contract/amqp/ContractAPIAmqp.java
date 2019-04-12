// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.amqp;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.SaslHandler;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceConnectionException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceHubException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceTransportException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.DeviceRegistrationParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.RequestData;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ResponseData;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderX509;

import javax.net.ssl.SSLContext;
import java.io.IOException;

public class ContractAPIAmqp extends ProvisioningDeviceClientContract
{
    private ProvisioningAmqpOperations provisioningAmqpOperations;
    private boolean useWebSockets;

    private String idScope;
    private SaslHandler amqpSaslHandler;

    /**
     * This constructor creates an instance of DpsAPIAmqps class and initializes member variables
     * @param provisioningDeviceClientConfig Config used for provisioning Cannot be {@code null}.
     * @throws ProvisioningDeviceClientException is thrown when any of the input parameters are invalid
     */
    public ContractAPIAmqp(ProvisioningDeviceClientConfig provisioningDeviceClientConfig) throws ProvisioningDeviceClientException
    {
        // SRS_ContractAPIAmqp_07_024: [ If provisioningDeviceClientConfig is null, this method shall throw ProvisioningDeviceClientException. ]
        if (provisioningDeviceClientConfig == null)
        {
            throw new ProvisioningDeviceClientException("ProvisioningDeviceClientConfig cannot be NULL.");
        }
        // SRS_ContractAPIAmqp_07_002: [The constructor shall throw ProvisioningDeviceClientException if either idScope and hostName are null or empty.]
        this.idScope = provisioningDeviceClientConfig.getIdScope();
        if ((this.idScope == null) || (this.idScope.isEmpty()))
        {
            throw new ProvisioningDeviceClientException("The idScope cannot be null or empty.");
        }

        String hostName = provisioningDeviceClientConfig.getProvisioningServiceGlobalEndpoint();
        if ((hostName == null) || (hostName.isEmpty()))
        {
            throw new ProvisioningDeviceClientException("The hostName cannot be null or empty.");
        }

        this.useWebSockets = provisioningDeviceClientConfig.getUseWebSockets();

        // SRS_ContractAPIAmqp_07_001: [The constructor shall save the scope id and hostname.]
        provisioningAmqpOperations = new ProvisioningAmqpOperations(this.idScope, hostName);
    }

    /**
     * Indicates need to open AMQP connection
     * @param requestData Data used for the connection initialization
     * @throws ProvisioningDeviceConnectionException is thrown when any of the input parameters are invalid
     */
    @Override
    public synchronized void open(RequestData requestData) throws ProvisioningDeviceConnectionException
    {
        if (requestData == null)
        {
            //Codes_SRS_ContractAPIAmqp_34_006: [If requestData is null, this function shall throw a ProvisioningDeviceConnectionException.]
            throw new ProvisioningDeviceConnectionException(new IllegalArgumentException("requestData cannot be null"));
        }

        if (requestData.isX509())
        {
            String registrationId = requestData.getRegistrationId();
            if (registrationId == null || registrationId.isEmpty())
            {
                //Codes_SRS_ContractAPIAmqp_34_007: [If requestData contains a null or empty registrationId, this function shall throw a ProvisioningDeviceConnectionException.]
                throw new ProvisioningDeviceConnectionException(new IllegalArgumentException("registration Id cannot be null or empty"));
            }

            SSLContext sslContext = requestData.getSslContext();
            if (sslContext == null)
            {
                //Codes_SRS_ContractAPIAmqp_34_008: [If requestData contains a null SSLContext, this function shall throw a ProvisioningDeviceConnectionException.]
                throw new ProvisioningDeviceConnectionException(new IllegalArgumentException("sslContext cannot be null"));
            }

            //Codes_SRS_ContractAPIAmqp_34_014: [If the requestData is using X509, this function shall open the
            // underlying provisioningAmqpOperations object with the provided registrationId and sslContext.]
            this.provisioningAmqpOperations.open(registrationId, sslContext, this.amqpSaslHandler, this.useWebSockets);
        }
    }

    /**
     * Requests hub to provide a device key to begin authentication over AMQP (Only for TPM)
     * @param requestData A non {@code null} value for the RequestData to be used
     * @param responseCallback A non {@code null} value for the callback
     * @param authorizationCallbackContext An object for context. Can be {@code null}
     * @throws ProvisioningDeviceClientException If any of the parameters are invalid ({@code null} or empty)
     * @throws ProvisioningDeviceTransportException If any of the API calls to transport fail
     * @throws ProvisioningDeviceHubException If hub responds back with an invalid status
     */
    public synchronized void requestNonceForTPM(RequestData requestData, String customPayload, ResponseCallback responseCallback, Object authorizationCallbackContext) throws ProvisioningDeviceClientException
    {
        if (requestData == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("requestData cannot be null"));
        }
        if (responseCallback == null)
        {
            throw new ProvisioningDeviceClientException("responseCallback cannot be null");
        }
        String registrationId = requestData.getRegistrationId();
        if (registrationId == null || registrationId.isEmpty())
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("registration Id cannot be null or empty"));
        }
        byte[] endorsementKey = requestData.getEndorsementKey();
        if (endorsementKey == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("Endorsement key cannot be null"));
        }
        byte[] storageRootKey = requestData.getStorageRootKey();
        if (storageRootKey == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("Storage root key cannot be null"));
        }
        if (requestData.getSslContext() == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("sslContext cannot be null"));
        }

        SSLContext sslContext = requestData.getSslContext();
        this.amqpSaslHandler = new AmqpsProvisioningSaslHandler(this.idScope, requestData.getRegistrationId(), requestData.getEndorsementKey(), requestData.getStorageRootKey(), responseCallback, authorizationCallbackContext);
        this.provisioningAmqpOperations.open(registrationId, sslContext, this.amqpSaslHandler, this.useWebSockets);
    }

    /**
     * Requests hub to authenticate this connection and start the registration process over AMQP
     * @param requestData A non {@code null} value with all the required request data
     * @param responseCallback A non {@code null} value for the callback
     * @param callbackContext An object for context. Can be {@code null}
     * @throws ProvisioningDeviceClientException If any of the parameters are invalid ({@code null} or empty)
     * @throws ProvisioningDeviceTransportException If any of the API calls to transport fail
     * @throws ProvisioningDeviceHubException If hub responds back with an invalid status
     */
    public synchronized void authenticateWithProvisioningService(RequestData requestData, String customPayload, ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException
    {
        //SRS_ContractAPIAmqp_07_003: [If responseCallback is null, this method shall throw ProvisioningDeviceClientException.]
        if (responseCallback == null)
        {
            throw new ProvisioningDeviceClientException("responseCallback cannot be null");
        }

        if (!requestData.isX509() && this.amqpSaslHandler == null)
        {
            //SRS_ContractAPIAmqp_34_020: [If the request is not x509, but the sasl handler is null, this function shall assume SymmetricKey authentication
            // and it shall open the connection with the request's sas token.]
            this.amqpSaslHandler = new AmqpsProvisioningSymmetricKeySaslHandler(this.idScope, requestData.getRegistrationId(), requestData.getSasToken());
            this.provisioningAmqpOperations.open(requestData.getRegistrationId(), requestData.getSslContext(), this.amqpSaslHandler, this.useWebSockets);
        }

        //if using TPM auth, amqpSaslHandler will not be null and will need the sas token
        if (this.amqpSaslHandler != null)
        {
            this.amqpSaslHandler.setSasToken(requestData.getSasToken());
        }

        byte[] payload = new DeviceRegistrationParser(requestData.getRegistrationId(), customPayload).toJson().getBytes();

        // SRS_ContractAPIAmqp_07_005: [This method shall send an AMQP message with the property of iotdps-register.]
        this.provisioningAmqpOperations.sendRegisterMessage(responseCallback, callbackContext, payload);
    }

    /**
     * Gets the registration status over AMQP
     * @param requestData A non {@code null} value with all the request data
     * @param responseCallback A non {@code null} value for the callback
     * @param callbackContext An object for context. Can be {@code null}
     * @throws ProvisioningDeviceClientException If any of the parameters are invalid ({@code null} or empty)
     * @throws ProvisioningDeviceTransportException If any of the API calls to transport fail
     * @throws ProvisioningDeviceHubException If hub responds back with an invalid status
     */
    public synchronized void getRegistrationStatus(RequestData requestData, ResponseCallback responseCallback, Object callbackContext) throws ProvisioningDeviceClientException
    {
        // SRS_ContractAPIAmqp_07_009: [If requestData is null this method shall throw ProvisioningDeviceClientException.]
        if (requestData == null)
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("requestData cannot be null"));
        }
        // SRS_ContractAPIAmqp_07_010: [If requestData.getOperationId() is null or empty, this method shall throw ProvisioningDeviceClientException.]
        String operationId = requestData.getOperationId();
        if (operationId == null || operationId.isEmpty())
        {
            throw new ProvisioningDeviceClientException(new IllegalArgumentException("operationId cannot be null or empty"));
        }
        // SRS_ContractAPIAmqp_07_011: [If responseCallback is null, this method shall throw ProvisioningDeviceClientException.]
        if (responseCallback == null)
        {
            throw new ProvisioningDeviceClientException("responseCallback cannot be null");
        }

        // SRS_ContractAPIAmqp_07_012: [If amqpConnection is null or not connected, this method shall throw ProvisioningDeviceConnectionException.]
        try
        {
            if (!this.provisioningAmqpOperations.isAmqpConnected())
            {
                throw new ProvisioningDeviceConnectionException("Amqp is not connected");
            }
        }
        catch (Exception e)
        {
            throw new ProvisioningDeviceClientException(e);
        }

        // SRS_ContractAPIAmqp_07_013: [This method shall send an AMQP message with the property of iotdps-get-operationstatus and the OperationId.]
        this.provisioningAmqpOperations.sendStatusMessage(operationId, responseCallback, callbackContext);
    }

    /**
     * Indicates to close the connection
     * @throws ProvisioningDeviceConnectionException If close could not succeed
     */
    public synchronized void close() throws ProvisioningDeviceConnectionException
    {
        try
        {
            this.provisioningAmqpOperations.close();
        }
        catch (IOException ex)
        {
            throw new ProvisioningDeviceConnectionException("Closing amqp failed", ex);
        }
    }
}
