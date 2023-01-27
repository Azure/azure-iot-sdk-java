// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.hsm;

import java.io.IOException;

/**
 * The definition for a readable and writable unix domain socket channel that may be needed in module creation depending
 * on the Edge runtime.
 * @see <a href="https://github.com/Azure/azure-iot-sdk-java/tree/main/iothub/device/iot-device-samples/unix-domain-socket-sample">An example implementation using JNR Unixsocket</a>
 */
public interface UnixDomainSocketChannel
{
    /**
     * Open the unix domain socket for the provided address
     * @param address the address to open the connection to.
     * @throws IOException if the connection fails to open for any reason.
     */
    void open(String address) throws IOException;

    /**
     * Write the provided bytes to the unix domain socket.
     * @param output the bytes to write to the unix domain socket.
     * @throws IOException if the bytes fail to write for any reason.
     */
    void write(byte[] output) throws IOException;

    /**
     * Read from the unix domain socket into the provided input buffer.
     * @param inputBuffer the empty byte array to be written to with the read bytes from the unix domain socket.
     * @return the number of bytes read from the unix domain socket during this call.
     * @throws IOException if the unix domain socket cannot be read from for any reason.
     */
    int read(byte[] inputBuffer) throws IOException;

    /**
     * Close the unix domain socket.
     * @throws IOException if the unix domain socket close fails for any reason.
     */
    void close() throws IOException;
}
