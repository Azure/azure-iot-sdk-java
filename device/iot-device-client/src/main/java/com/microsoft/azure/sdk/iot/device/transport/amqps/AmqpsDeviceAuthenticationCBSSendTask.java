package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.CustomLogger;

public final class AmqpsDeviceAuthenticationCBSSendTask implements Runnable
{
    private final CustomLogger logger = new CustomLogger(this.getClass());

    private AmqpsDeviceAuthenticationCBS amqpsDeviceAuthenticationCBS;

    /**
     * Task runner for CBS authentication.
     *
     * @param amqpsDeviceAuthenticationCBS the class contining the function to run.
     */
    public AmqpsDeviceAuthenticationCBSSendTask(AmqpsDeviceAuthenticationCBS amqpsDeviceAuthenticationCBS)
    {
        // Codes_SRS_AMQPSDEVICEAUTHENTICATIONCBSSENDTASK_12_001: [The constructor shall throw IllegalArgumentException if the amqpsDeviceAuthenticationCBS parameter is null.]
        if (amqpsDeviceAuthenticationCBS == null)
        {
            logger.LogError("AmqpsDeviceAuthenticationCBSTokenRenewalTask constructor called with null value for parameter amqpsDeviceAuthenticationCBS");
            throw new IllegalArgumentException("Parameter 'amqpsDeviceAuthenticationCBS' must not be null");
        }

        // Codes_SRS_AMQPSDEVICEAUTHENTICATIONCBSSENDTASK_12_002: [The constructor shall save the amqpsDeviceAuthenticationCBS.]
        this.amqpsDeviceAuthenticationCBS = amqpsDeviceAuthenticationCBS;
    }

    /**
     * The function to run.
     */
    @Override
    public void run()
    {
        try
        {
            // Codes_SRS_AMQPSDEVICEAUTHENTICATIONCBSSENDTASK_12_003: [The function shall call the amqpsDeviceAuthenticationCBS.sendAuthenticationMessages.]
            this.amqpsDeviceAuthenticationCBS.sendAuthenticationMessages();
        }
        catch (Throwable e)
        {
            logger.LogError(e.toString() + ": " + e.getMessage());
            logger.LogDebug("Exception on housekeeping", e);
        }
    }
}
