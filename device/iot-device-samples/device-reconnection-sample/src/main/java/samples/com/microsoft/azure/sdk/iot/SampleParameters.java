// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package samples.com.microsoft.azure.sdk.iot;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.net.URISyntaxException;


public class SampleParameters {

    private static final String DEVICE_CONNECTION_STRING = System.getenv("IOTHUB_DEVICE_CONNECTION_STRING");
    private static final String TRANSPORT = "mqtt";

    private static final String FOOTER = "\nFor more info, please refer to https://github.com/Azure/azure-iot-sdks";
    private static final String APPEXE = "java -jar ";

    private static CommandLine cmd = null;
    private static final String[] connections = new String[2];

    /**
     * Setup parameters from command line arguments
     * @param args string array from main()
     */
    public SampleParameters(String[] args) {

        //create options for cli
        Options options = new Options()
            .addOption(
                Option.builder("h")
                    .longOpt("help")
                    .hasArg(false)
                    .desc("Prints this message")
                    .build()
            )
            .addOption(
                Option.builder("p")
                    .longOpt("primaryConnectionString")
                    .hasArg()
                    .desc("Primary device connection string; required argument unless setup with environment variable \"IOTHUB_DEVICE_CONNECTION_STRING\"")
                    .build()
            )
            .addOption(
                Option.builder("t")
                    .longOpt("transportProtocol")
                    .hasArg()
                    .desc("Transport protocol [mqtt | https | amqps| amqps_ws | mqtt_ws] (optional); defaults to \"mqtt\"")
                    .build()
            );

        //Get command line string
        String cmdLine = APPEXE;
        try {
            String jarPath = Options.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath();
            String jarName = jarPath.substring(jarPath.lastIndexOf("/") + 1);
            cmdLine += jarName;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            cmdLine += "<sample.jar>";
        }

        //Parsing command line
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            //parse the command line arguments
            cmd = parser.parse(options, args);

            //Help option
            if (cmd.hasOption("h"))
            {
                formatter.printHelp(cmdLine, "\nHelp:\n\n", options, FOOTER, true);
                System.exit(0);
            }

            //Connection String, required unless there is environment variable
            if (cmd.hasOption("p"))
            {
                connections[0] = cmd.getOptionValue("p");
                if (connections[0] == null || connections[0].trim().isEmpty())
                {
                    formatter.printHelp(cmdLine, "\nError: Connection is empty\n\n", options, FOOTER, true);
                    System.exit(0);
                }
            }
            else
            {
                connections[0] = DEVICE_CONNECTION_STRING;
                if (connections[0] == null || connections[0].trim().isEmpty())
                {
                    formatter.printHelp(cmdLine, "\nError: Connection is required as argument or as environment variable\n\n", options, FOOTER, true);
                    System.exit(0);
                }
            }

            //Connection String (secondary), optional
            if (cmd.hasOption("s"))
            {
                connections[1] = cmd.getOptionValue("s");
            }

        } catch (ParseException e) {
            //wrong parameters
            formatter.printHelp(cmdLine, "\nError: "+e.getMessage()+"\n\n", options, FOOTER, true);

            System.exit(0);
        }
    }

    /**
     * get connection string argument from command line
     * @return string array of 2 (primary and secondary)
     */
    public String[] getConnectionStrings()
    {
        return connections;
    }

     /**
     * get transport argument from command line
     * @return string value
     */
    public String getTransport()
    {
        return cmd.getOptionValue("t", TRANSPORT);
    }
}