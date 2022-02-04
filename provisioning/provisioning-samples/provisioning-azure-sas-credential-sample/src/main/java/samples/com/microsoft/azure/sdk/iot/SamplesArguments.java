// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package samples.com.microsoft.azure.sdk.iot;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

@Slf4j
public class SamplesArguments
{
    private final String DPS_HOSTNAME = "deviceProvisioningServiceHostName";
    private final String PROVISIONING_SHARED_ACCESS_SIGNATURE = "provisioningSharedAccessSignature";

    private String deviceProvisioningServiceHostName;
    private String provisioningSharedAccessSignature;

    public SamplesArguments(String[] args)
    {
        Option hostName = new Option("d", DPS_HOSTNAME, true, "Device Provisioning Service host name (\"my-dps.azure-devices-provisioning.net\") for example");
        Option sharedAccessSignature = new Option("s", PROVISIONING_SHARED_ACCESS_SIGNATURE, true, "Provisioning Shared Access Signature. See this sample's source code for more details on how to create this.");

        hostName.setRequired(true);
        sharedAccessSignature.setRequired(true);

        Options options = new Options()
                .addOption(hostName)
                .addOption(sharedAccessSignature);

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

        this.deviceProvisioningServiceHostName = cmd.getOptionValue(DPS_HOSTNAME);
        this.provisioningSharedAccessSignature = cmd.getOptionValue(PROVISIONING_SHARED_ACCESS_SIGNATURE);
    }
}
