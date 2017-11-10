// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.util;

/** This class enables mocking of the base class wait and notify functions
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
