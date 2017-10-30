package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.CustomLogger;

public final class AmqpsDeviceAuthenticationCBSTokenRenewalTask implements Runnable
{
    private final CustomLogger logger = new CustomLogger(this.getClass());

    private AmqpsSessionDeviceOperation amqpsSessionDeviceOperation;

    /**
     * Task runner for CBS authentication renewal.
     *
     * @param amqpsSessionDeviceOperation the class contining the function to run.
     */
    public AmqpsDeviceAuthenticationCBSTokenRenewalTask(AmqpsSessionDeviceOperation amqpsSessionDeviceOperation)
    {
        // Codes_SRS_AMQPSDEVICEAUTHENTICATIONCBSTOKENRENEWALTASK_12_001: [The constructor shall throw IllegalArgumentException if the amqpsSessionDeviceOperation parameter is null.]
        if (amqpsSessionDeviceOperation == null)
        {
            logger.LogError("AmqpsDeviceAuthenticationCBSTokenRenewalTask constructor called with null value for parameter amqpsDeviceAuthenticationCBS");
            throw new IllegalArgumentException("Parameter 'amqpsDeviceAuthenticationCBS' must not be null");
        }

        // Codes_SRS_AMQPSDEVICEAUTHENTICATIONCBSTOKENRENEWALTASK_002: [The constructor shall save the amqpsSessionDeviceOperation.]
        this.amqpsSessionDeviceOperation = amqpsSessionDeviceOperation;
    }

    /**
     * The function to run.
     */
    @Override
    public void run()
    {
        try
        {
            // Codes_SRS_AMQPSDEVICEAUTHENTICATIONCBSTOKENRENEWALTASK_12_003: [The function shall call the amqpsSessionDeviceOperation.renewToken.]
            this.amqpsSessionDeviceOperation.renewToken();
        }
        catch (Throwable e)
        {
            logger.LogError(e.toString() + ": " + e.getMessage());
            logger.LogDebug("Exception on housekeeping", e);
        }
    }
}
