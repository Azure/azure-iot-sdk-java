/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.connection;

public class PrimitiveRetry implements ReconnectionPolicy
{

    private static int retryCount = 0;

    //TODO delete this retry, it is only a sample
    @Override
    public boolean waitAndRetry()
    {
        if (retryCount > 5)
        {
            retryCount = 0;
            return false;
        }

        try
        {
            retryCount++;
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {

        }

        return true;
    }
}
