// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.ModuleClient;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.device.hsm.UnixDomainSocketChannel;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;


/**
 * Sample code that demonstrates how to implement the {@link UnixDomainSocketChannel} interface using JNR Unixsocket.
 *
 * Read more about JNR Unixsocket here: https://github.com/jnr/jnr-unixsocket
 */
public class UnixDomainSocketSample
{
    public static class UnixDomainSocketChannelImpl implements UnixDomainSocketChannel
    {
        UnixSocketAddress unixSocketAddress;
        UnixSocketChannel channel;

        @Override
        public void open(String address) throws IOException
        {
            this.unixSocketAddress = new UnixSocketAddress(address);
            this.channel = UnixSocketChannel.open(this.unixSocketAddress);
        }

        @Override
        public void write(byte[] output) throws IOException
        {
            this.channel.write(ByteBuffer.wrap(output));
        }

        @Override
        public int read(byte[] inputBuffer) throws IOException
        {
            ByteBuffer inputByteBuffer = ByteBuffer.wrap(inputBuffer);
            int bytesRead = this.channel.read(inputByteBuffer);
            System.arraycopy(inputByteBuffer.array(), 0, inputBuffer, 0, inputByteBuffer.capacity());
            return bytesRead;
        }

        @Override
        public void close() throws IOException
        {
            this.channel.close();
        }
    }

    public static void main(String[] args)
        throws IOException, URISyntaxException, ModuleClientException
    {
        UnixDomainSocketChannel unixDomainSocketChannel = new UnixDomainSocketChannelImpl();

        ModuleClient moduleClient = ModuleClient.createFromEnvironment(unixDomainSocketChannel);
        moduleClient.open(false);

        // perform module operations. See other samples for examples of what this looks like

        moduleClient.close();
    }
}
