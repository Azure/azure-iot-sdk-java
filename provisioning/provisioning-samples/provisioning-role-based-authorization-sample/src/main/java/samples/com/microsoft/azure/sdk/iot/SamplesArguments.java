// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package samples.com.microsoft.azure.sdk.iot;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

@Slf4j
public class SamplesArguments
{
    private final String DPS_HOSTNAME = "deviceProvisioningServiceHostName";
    private final String TENANT_ID = "tenantId";
    private final String CLIENT_ID = "clientId";
    private final String CLIENT_SECRET = "clientSecret";

    private String deviceProvisioningServiceHostName;
    private String tenantId;
    private String clientId;
    private String clientSecret;

    public SamplesArguments(String[] args)
    {
        Option hostName = new Option("h", DPS_HOSTNAME, true, "IoT Hub host name (\"my-azure-iot-hub.azure-devices.net\" for example)");
        Option tenantId = new Option("t", TENANT_ID, true, "AAD Tenant Id");
        Option clientId = new Option("c", CLIENT_ID, true, "AAD Client Id");
        Option clientSecret = new Option("s", CLIENT_SECRET, true, "AAD Client Secret");

        hostName.setRequired(true);
        tenantId.setRequired(true);
        clientId.setRequired(true);
        clientSecret.setRequired(true);

        Options options = new Options()
                .addOption(hostName)
                .addOption(tenantId)
                .addOption(clientId)
                .addOption(clientSecret);

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
        this.tenantId = cmd.getOptionValue(TENANT_ID);
        this.clientId = cmd.getOptionValue(CLIENT_ID);
        this.clientSecret = cmd.getOptionValue(CLIENT_SECRET);
    }

    public String getIotHubHostName()
    {
        return this.deviceProvisioningServiceHostName;
    }

    public String getTenantId()
    {
        return this.tenantId;
    }

    public String getClientId()
    {
        return this.clientId;
    }

    public String getClientSecret()
    {
        return this.clientSecret;
    }
}