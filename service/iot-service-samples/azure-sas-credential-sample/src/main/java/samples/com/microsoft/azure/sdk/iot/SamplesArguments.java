// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package samples.com.microsoft.azure.sdk.iot;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

@Slf4j
public class SamplesArguments
{
    private final String IOTHUB_HOSTNAME = "IotHubHostName";
    private final String IOTHUB_SHARED_ACCESS_SIGNATURE = "SharedAccessSignature";

    private String iotHubHostName;
    private String sharedAccessSignature;

    public SamplesArguments(String[] args)
    {
        Option hostName = new Option("h", IOTHUB_HOSTNAME, true, "IoT Hub host name (\"my-azure-iot-hub.azure-devices.net\" for example)");
        Option sharedAccessSignature = new Option("s", IOTHUB_SHARED_ACCESS_SIGNATURE, true, "IoT Hub Shared Access Signature");

        hostName.setRequired(true);
        sharedAccessSignature.setRequired(true);

        Options options = new Options()
            .addOption(hostName);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try
        {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e)
        {
            log.error("Failed to parse command line arguments", e);
            formatter.printHelp("java <sampleClass>.jar", options);

            System.exit(0);
        }

        this.iotHubHostName = cmd.getOptionValue(IOTHUB_HOSTNAME);
        this.sharedAccessSignature = cmd.getOptionValue(IOTHUB_SHARED_ACCESS_SIGNATURE);
    }

    public String getIotHubHostName()
    {
        return this.iotHubHostName;
    }

    public String getSharedAccessSignature()
    {
        return this.sharedAccessSignature;
    }
}