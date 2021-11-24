// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package samples.com.microsoft.azure.sdk.iot;

import com.azure.core.http.policy.HttpLogDetailLevel;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.net.URISyntaxException;
import java.io.*;


public class SampleParameters {

    private static final String _DEVICE_CONNECTION_STRING = System.getenv("IOTHUB_DEVICE_CONNECTION_STRING");
    private static final String _TRANSPORT = "mqtt";
    private static final String _NUM_REQUESTS = "3";
    private static final String _SLEEP_DURATION_IN_SECONDS = "10";
    private static final String _TIMEOUT_IN_MINUTES = "1";

    private static final String _FOOTER = "\nFor more info, please refer to https://github.com/Azure/azure-iot-sdks";
    private static final String _APPEXE = "java -jar ";

    private static CommandLine cmd = null;
    private static String[] connections = new String[2];

    /**
     * Setup parameters from command line arguments
     * @param string array from main()
     */
    public SampleParameters(String[] args) {

        //create options for cli
        Options options = new Options()
            .addOption(
                Option.builder("h")
                    .longOpt("help")
                    .hasArg(false)
                    .desc("Print this message")
                    .build()
            )
            .addOption(
                Option.builder("c")
                    .longOpt("connection")
                    .hasArg()
                    .desc("Device connection string (Primary), required argument unless setup with environment variable \"IOTHUB_DEVICE_CONNECTION_STRING\"")
                    .build()
            )
            .addOption(
                Option.builder("d")
                    .longOpt("connection2")
                    .hasArg()
                    .desc("Device connection string (Secondary), optional")
                    .build()
            )
            .addOption(
                Option.builder("t")
                    .longOpt("transport")
                    .hasArg()
                    .desc("Protocol choice [mqtt | https | amqps| amqps_ws | mqtt_ws], optinal, default to \"mqtt\"")
                    .build()
            )
            .addOption(
                Option.builder("r")
                    .longOpt("requests")
                    .hasArg()
                    .desc("Number of requests, optional, default to \"3\"")
                    .build()
            )
            .addOption(
                Option.builder("s")
                    .longOpt("sleep")
                    .hasArg()
                    .desc("Sleep duration between requests (in seconds), optional, default to \"10\"")
                    .build()
            )
            .addOption(
                Option.builder("o")
                    .longOpt("timeout")
                    .hasArg()
                    .desc("Timeout for each requests (in minutes), optional, default to \"1\"")
                    .build()
            );

        //Get command line string
        String cmdLine = _APPEXE;
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
                formatter.printHelp(cmdLine, "\nHelp:\n\n", options, _FOOTER, true);
                System.exit(0);
            }

            
            //Connection String, required unless there is evnvironment variable
            if (cmd.hasOption("c"))
            {
                connections[0] = cmd.getOptionValue("c");
                if (connections[0] == null || connections[0].trim().isEmpty())
                {
                    formatter.printHelp(cmdLine, "\nError: Connection is empty\n\n", options, _FOOTER, true);
                    System.exit(0);
                }
            }
            else
            {
                connections[0] = _DEVICE_CONNECTION_STRING;
                if (connections[0] == null || connections[0].trim().isEmpty())
                {
                    formatter.printHelp(cmdLine, "\nError: Connection is required as argument or as environment variable\n\n", options, _FOOTER, true);
                    System.exit(0);
                }
            }

            //Connection String (secondary), optional
            if (cmd.hasOption("d"))
            {
                connections[1] = cmd.getOptionValue("d");
            }

        } catch (ParseException e) {
            //wrong parameters
            formatter.printHelp(cmdLine, "\nError: "+e.getMessage()+"\n\n", options, _FOOTER, true);

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
        return cmd.getOptionValue("t", _TRANSPORT);
    }

    /**
     * get number of requests argument from command line
     * @return string value
     */
    public String getNumRequests()
    {
        return cmd.getOptionValue("r", _NUM_REQUESTS);
    }

    /**
     * get sleep duration between requests argument from command line
     * @return string value (seconds)
     */
    public String getSleepDuration()
    {
        return cmd.getOptionValue("s", _SLEEP_DURATION_IN_SECONDS);
    }

    /**
     * get timeout argument from command line
     * @return string value (minutes)
     */
    public String getTimeout()
    {
        return cmd.getOptionValue("t", _TIMEOUT_IN_MINUTES);
    }

}