package com.microsoft.azure.sdk.iot.provisioning.device.internal.transport;

/**
 * This class enables mocking of the base class wait and notify functions
 */
public class ObjectLock
{
    public void waitLock(long timeout) throws InterruptedException
    {
        this.wait(timeout);
    }

    public void notifyLock()
    {
        this.notify();
    }
}