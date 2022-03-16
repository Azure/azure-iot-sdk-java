package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import org.apache.commons.cli.*;

public class InputParameters
{
    private static final String PROTOCOL = "mqtt";
    private static final String PATH_CERT = null;

    private static final String FOOTER = "\nFor more info, please refer to https://github.com/Azure/azure-iot-sdks";
    private static final String APPEXE = "java -jar target/send-event-{version}-with-deps.jar";

    private static CommandLine cmd = null;

    /**
     * Setup parameters from command line arguments
     * @param args array from main()
     */
    public InputParameters(String[] args)
    {
        // create option for cli input
        Options options = new Options()
                .addOption(
                        Option.builder("h")
                                .longOpt("help")
                                .hasArg(false)
                                .desc("Prints help message")
                                .build()
                )
                .addOption(
                        Option.builder("c")
                                .longOpt("connectionString")
                                .hasArg()
                                .desc("IoT Hub or Edge Hub connection string (required; contains Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key> or HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>;GatewayHostName=<gateway>)")
                                .build()
                )
                .addOption(
                        Option.builder("r")
                                .longOpt("requests")
                                .hasArg()
                                .desc("Number of requests to send (required)")
                                .build()
                )
                .addOption(
                        Option.builder("p")
                                .longOpt("protocol")
                                .hasArg()
                                .desc("Protocol [mqtt | https | amqps | amqps_ws | mqtt_ws] (optional; defaults to \"mqtt\")")
                                .build()
                );

        // parse command line
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try
        {
            // parse the command line arguments
            cmd = parser.parse(options, args);

            // -h option
            if (cmd.hasOption("h"))
            {
                formatter.printHelp(APPEXE, "\nHelp:\n\n", options, FOOTER, true);
                System.exit(0);
            }

            // -c and -r option
            if (!cmd.hasOption("c") || !cmd.hasOption("r"))
            {
                formatter.printHelp(APPEXE, "\nError: ConnectionString and NumberOfRequests are required as arguments\n\nHelp:\n", options, FOOTER, true);
                System.exit(0);
            }
            else
            {
                if (cmd.getOptionValue("c") == null || cmd.getOptionValue("c").trim().isEmpty())
                {
                    formatter.printHelp(APPEXE, "\nError: ConnectionString is empty\n\nHelp:\n", options, FOOTER, true);
                    System.exit(0);
                }

                if (cmd.getOptionValue("r") == null || cmd.getOptionValue("r").trim().isEmpty())
                {
                    formatter.printHelp(APPEXE, "\nError: NumberOfRequests is empty\n\nHelp:\n", options, FOOTER, true);
                    System.exit(0);
                }
            }
        }
        catch (ParseException e)
        {
            //wrong parameters
            formatter.printHelp(APPEXE, "\nError: "+e.getMessage()+"\n\nHelp:\n", options, FOOTER, true);
            System.exit(0);
        }
    }

    /**
     * Get connection string argument from command line.
     * @return string value
     */
    public String getConnectionString()
    {
        return cmd.getOptionValue("c");
    }


    /**
     * Get number of requests argument from command line.
     * @return string value
     */
    public String getNumberOfRequests()
    {
        return cmd.getOptionValue("r");
    }


    /**
     * Get protocol argument from command line.
     * @return enum value
     */
    public IotHubClientProtocol getProtocol()
    {
        IotHubClientProtocol protocol;
        String protocolArg = cmd.getOptionValue("p", PROTOCOL).toLowerCase();
        switch(protocolArg)
        {
            case "https":
                protocol = IotHubClientProtocol.HTTPS;
                break;
            case "amqps":
                protocol = IotHubClientProtocol.AMQPS;
                break;
            case "amqps_ws":
                protocol = IotHubClientProtocol.AMQPS_WS;
                break;
            case "mqtt":
                protocol = IotHubClientProtocol.MQTT;
                break;
            case "mqtt_ws":
                protocol = IotHubClientProtocol.MQTT_WS;
                break;
            default:
                throw new IllegalArgumentException("Unsupported protocol: [" + protocolArg + "]");
        }
        return protocol;
    }


    /**
     * Get path to certificate argument from command line.
     * @return string value
     */
    public String getPathCert()
    {
        return cmd.getOptionValue("pc", PATH_CERT);
    }
}
