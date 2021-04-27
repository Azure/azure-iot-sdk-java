package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;

import java.util.Set;

/**
 * Convenience class for the sendReportedProperties method in the DeviceClient
 */
public class ReportedPropertiesParameters {
    /**
     * The reported properties to send
     */
    public Set<Property> reportedProperties = null;
    /**
     * The version of the properties
     */
    public Integer version = null;
    /**
     * A correlation callback to monitor the message lifecycle
     */
    public CorrelatingMessageCallback correlatingMessageCallback = null;
    /**
     * The correlation callback context
     */
    public Object correlatingMessageCallbackContext = null;
    /**
     * An event callback to get the status of the event
     */
    public IotHubEventCallback reportedPropertiesCallback = null;
    /**
     * The event callback context
     */
    public Object reportedPropertiesCallbackContext = null;
}
