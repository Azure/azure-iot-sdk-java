// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.hsm;

import java.io.IOException;

/**
 * The interface definition for a readable and writable unix socket channel
 */
public interface UnixDomainSocketChannel
{
    public void setAddress(String address);
    public void open() throws IOException;
    public void write(byte[] output) throws IOException;
    public int read(byte[] inputBuffer) throws IOException;
    public void close() throws IOException;
}
