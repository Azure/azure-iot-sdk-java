// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportPacket;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Device Twin Sample for an IoT Hub. Default protocol is to use
 * MQTT transport.
 */
public class DeviceTwinSample
{
    private enum LIGHTS{ ON, OFF }
    private enum CAMERA{ DETECTED_BURGLAR, SAFELY_WORKING }
    private static final int MAX_EVENTS_TO_REPORT = 5;

    private static final AtomicBoolean Succeed = new AtomicBoolean(false);

    protected static class ReportedPropertiesCorrelation implements CorrelatingMessageCallback {

        private String _message = "";
        public ReportedPropertiesCorrelation(String message) {
            _message = message;
        }
        @Override
        public void onRequestQueued(Message message, IotHubTransportPacket packet, Object callbackContext)
        {
            String messageId = message.getCorrelationId();

            if (message != null)
            {
                System.out.println("==========CORRELATIONCALLBACK["+ messageId+ "] onRequestQueued (" + _message + ") CorrelationId: " + message.getCorrelationId());
            }
            if (callbackContext instanceof ReportedPropertiesContext) {

                ((ReportedPropertiesContext) callbackContext).setCorrelationId(messageId);
            }
        }

        @Override
        public void onRequestSent(Message message, IotHubTransportPacket packet, Object callbackContext)
        {
            String messageId = message.getCorrelationId();

            if (message != null)
            {
                System.out.println("==========CORRELATIONCALLBACK["+messageId+"] onRequestSent (" + _message + ") CorrelationId: " + message.getCorrelationId());
            }
        }

        @Override
        public void onRequestAcknowledged(IotHubTransportPacket packet, Object callbackContext, Throwable e) {
            Message message = packet.getMessage();
            String messageId = message.getCorrelationId();

            if (message != null)
            {
                System.out.println("==========CORRELATIONCALLBACK["+messageId+"] onRequestAcknowledged (" + _message + ") CorrelationId: " + message.getCorrelationId());
            }
            if (e != null) {
                System.out.println("==========CORRELATIONCALLBACK["+messageId+"] onRequestAcknowledged (" + _message + ") ERROR: " + e.getMessage());
            }
        }

        @Override
        public void onResponseAcknowledged(Message message, Object callbackContext, Throwable e) {
            String messageId = message.getCorrelationId();

            if (message != null)
            {
                System.out.println("==========CORRELATIONCALLBACK["+messageId+"] onResponseAcknowledged (" + _message + ") CorrelationId: " + message.getCorrelationId());
            }
            if (e != null) {
                System.out.println("==========CORRELATIONCALLBACK["+messageId+"] onResponseAcknowledged (" + _message + ") ERROR: " + e.getMessage());
            }
        }

        @Override
        public void onResponseReceived(Message message, Object callbackContext, Throwable e) {
            String messageId = message.getCorrelationId();

            if (message != null)
            {
                System.out.println("==========CORRELATIONCALLBACK["+messageId+"] onResponseReceived (" + _message + ") CorrelationId: " + message.getCorrelationId());
            }
        }

        @Override
        public void onUnknownMessageAcknowledged(Message message, Object callbackContext, Throwable e) {
            String messageId = message.getCorrelationId();

            if (message != null)
            {
                System.out.println("==========CORRELATIONCALLBACK["+messageId+"] onUnknownMessageAcknowledged (" + _message + ") CorrelationId: " + message.getCorrelationId());
            }
            if (e != null) {
                System.out.println("==========CORRELATIONCALLBACK["+messageId+"] onUnknownMessageAcknowledged (" + _message + ") ERROR: " + e.getMessage());
            }
        }
    }


    protected static class DeviceTwinStatusCallBack implements IotHubEventCallback
    {
        @Override
        public void execute(IotHubStatusCode status, Object context)
        {
            if (context == null) {
                System.out.println("==========DEVICETWINCALLBACK context null");
            }
            Succeed.set((status == IotHubStatusCode.OK) || (status == IotHubStatusCode.OK_EMPTY));
            System.out.println("==========DEVICETWINCALLBACK IoT Hub responded to device twin operation with status " + status.name());
        }
    }

    protected static class ReportedPropertiesContext
    {
        private boolean calledFromEvent = false;
        private String _correlationId = "";
        private String _message = "";

        public ReportedPropertiesContext(String message) { this._message = message;}
        public void setCorrelationId(String message) { _correlationId = message; }
        public String getcorrelationId() { return _correlationId; }
        public String getMessageOfContext() { return _message; }
    }

    protected static class ReportedPropertiesCallback implements IotHubEventCallback
    {
        private String _message = "";
        public ReportedPropertiesCallback(String message) {
            this._message = message;
        }

        @Override
        public void execute(IotHubStatusCode status, Object context)
        {
            if (context instanceof ReportedPropertiesContext)
            {
                ReportedPropertiesContext myContext = (ReportedPropertiesContext)context;
                String messageId = myContext.getcorrelationId();
                System.out.println("==========REPORTEDPROPERTYCALLBACK["+messageId+"] Executing reported properties callback for " + _message);
                System.out.println("==========REPORTEDPROPERTYCALLBACK["+messageId+"] Found ReportedPropertiesContext with message " + myContext.getMessageOfContext());
                System.out.println("==========REPORTEDPROPERTYCALLBACK["+messageId+"] Found ReportedPropertiesContext with correlationId " + myContext.getcorrelationId());
                System.out.println("==========REPORTEDPROPERTYCALLBACK["+messageId+"] This context was set by ReportedPropertiesCallback " + status.name());
                System.out.println("==========REPORTEDPROPERTYCALLBACK["+messageId+"] IoT Hub responded to device twin reported properties with status " + status.name());
            }

        }
    }

    /*
     * If you don't care about version, you can use the PropertyCallBack.
     */
    protected static class provaCallback implements TwinPropertyCallBack
    {
        @Override
        public void TwinPropertyCallBack(Property property, Object context)
        {
            System.out.println(
                    "provaCallback change " + property.getKey() +
                            " to " + property.getValue() +
                            ", Properties version:" + property.getVersion());
        }
    }
   
    protected static class onProperty implements TwinPropertyCallBack
    {
        @Override
        public void TwinPropertyCallBack(Property property, Object context)
        {
            System.out.println(
                    "onProperty callback for " + (property.getIsReported()?"reported": "desired") +
                            " property " + property.getKey() +
                            " to " + property.getValue() +
                            ", Properties version:" + property.getVersion());
        }
    }

    protected static class IotHubConnectionStatusChangeCallbackLogger implements IotHubConnectionStatusChangeCallback
    {
        @Override
        public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
        {
            System.out.println();
            System.out.println("==========CONNECTION STATUS UPDATE: " + status);
            System.out.println("==========CONNECTION STATUS REASON: " + statusChangeReason);
            System.out.println("==========CONNECTION STATUS THROWABLE: " + (throwable == null ? "null" : throwable.getMessage()));
            System.out.println();

            if (throwable != null)
            {
                throwable.printStackTrace();
            }

            if (status == IotHubConnectionStatus.DISCONNECTED)
            {
                System.out.println("==========The connection was lost, and is not being re-established." +
                        " Look at provided exception for how to resolve this issue." +
                        " Cannot send messages until this issue is resolved, and you manually re-open the device client");
            }
            else if (status == IotHubConnectionStatus.DISCONNECTED_RETRYING)
            {
                System.out.println("==========The connection was lost, but is being re-established." +
                        " Can still send messages, but they won't be sent until the connection is re-established");
            }
            else if (status == IotHubConnectionStatus.CONNECTED)
            {
                System.out.println("==========The connection was successfully established. Can send messages.");
            }
        }
    }

    /**
     * Reports properties to IotHub, receives desired property notifications from IotHub. Default protocol is to use
     * use MQTT transport.
     *
     * @param args 
     * args[0] = IoT Hub connection string
     */
    public static void main(String[] args)
            throws IOException, URISyntaxException
    {
        System.out.println("==========Starting...");
        System.out.println("==========Beginning setup.");


        if (args.length < 1)
        {
            System.out.format(
                    "Expected the following argument but received: %d.\n"
                            + "The program should be called with the following args: \n"
                            + "[Device connection string] - String containing Hostname, Device Id & Device Key in the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>\n"
                            + "[Protocol] - (mqtt | amqps | amqps_ws)\n",
                    args.length);
            return;
        }

        String connString = args[0];

        IotHubClientProtocol protocol;
        if (args.length == 1)
        {
            protocol = IotHubClientProtocol.MQTT;
        }
        else
        {
            String protocolStr = args[1];
            if (protocolStr.equals("amqps"))
            {
                protocol = IotHubClientProtocol.AMQPS;
            }
            else if (protocolStr.equals("mqtt"))
            {
                protocol = IotHubClientProtocol.MQTT;
            }
            else if (protocolStr.equals("amqps_ws"))
            {
                protocol = IotHubClientProtocol.AMQPS_WS;
            }
            else if (protocolStr.equals("mqtt_ws"))
            {
                protocol = IotHubClientProtocol.MQTT_WS;
            }
            else
            {
                System.out.format(
                        "Expected argument 2 to be one of 'mqtt', 'https', 'amqps' , 'mqtt_ws' or 'amqps_ws' but received %s\n"
                                + "The program should be called with the following args: \n"
                                + "1. [Device connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>\n"
                                + "2. (mqtt | amqps | amqps_ws | mqtt_ws)\n",
                        protocolStr);
                return;
            }
        }

        System.out.println("==========Successfully read input parameters.");
        System.out.format("Using communication protocol %s.\n",
                protocol.name());

        DeviceClient client = new DeviceClient(connString, protocol);
        System.out.println("==========Successfully created an IoT Hub client.");

        client.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallbackLogger(), new Object());

        try
        {
            System.out.println("==========Open connection to IoT Hub.");
            client.open();

            System.out.println("==========Start device Twin and get remaining properties...");
            // Properties already set in the Service will shows up in the generic onProperty callback, with value and version.
            Succeed.set(false);
            client.startDeviceTwin(new DeviceTwinStatusCallBack(), null, new onProperty(), null);
            do
            {
                Thread.sleep(1000);
            }
            while(!Succeed.get());


            System.out.println("==========Subscribe to Desired properties on device Twin...");
            Map<Property, Pair<TwinPropertyCallBack, Object>> desiredProperties = new HashMap<Property, Pair<TwinPropertyCallBack, Object>>()
            {
                {
                    put(new Property("prova", null), new Pair<TwinPropertyCallBack, Object>(new provaCallback(), null));
                    put(new Property("version", null), new Pair<TwinPropertyCallBack, Object>(new onProperty(), null));
                }
            };
            client.subscribeToTwinDesiredProperties(desiredProperties);

            System.out.println("==========Get device Twin...");
            client.getDeviceTwin(); // For each desired property in the Service, the SDK will call the appropriate callback with the value and version.

            System.out.println("==========Update reported properties...");
            Set<Property> reportProperties = new HashSet<Property>()
            {
                {
                    add(new Property("prova", 1));
                    add(new Property("version", 1));
                }
            };

            ReportedPropertiesParameters params = new ReportedPropertiesParameters(reportProperties);
            ReportedPropertiesContext sharableContext = new ReportedPropertiesContext("Send All Params Reported Properties Context Message");
            params.setCorrelationCallback(new ReportedPropertiesCorrelation("SendAllParams"), sharableContext);
            params.setReportedPropertiesCallback(new ReportedPropertiesCallback("SendAllParams"), sharableContext);

            client.sendReportedProperties(params);

            System.out.println("==========Waiting for Desired properties");
        }
        catch (Exception e)
        {
            System.out.println("==========On exception, shutting down \n" + " Cause: " + e.getCause() + " \n" +  e.getMessage());
            client.closeNow();
            System.out.println("==========Shutting down...");
        }

        System.out.println("==========Press any key to exit...");

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        client.closeNow();

        System.out.println("==========Shutting down...");

    }
}
