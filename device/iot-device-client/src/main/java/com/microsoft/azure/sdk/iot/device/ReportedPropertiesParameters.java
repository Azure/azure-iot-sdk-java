package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.twin.Property;

import java.util.Set;

/**
 * Convenience class for the sendReportedPropertiesAsync method in the DeviceClient
 */
public class ReportedPropertiesParameters {

    /**
     * Creates the class with the properties to send
     *
     * @param properties the properties to be reported. Value can not be {@code null}.
     * @throws IllegalArgumentException when the properties parmeter is null.
     */
    public ReportedPropertiesParameters(Set<Property> properties) throws IllegalArgumentException
    {
        if (properties == null) {
            throw new IllegalArgumentException("Properties cannot be null.");
        }
        reportedProperties = properties;
    }

    /**
     * Creates the class with the properties to send and sets version
     *
     * @param properties Properties to be reported. Value can not be {@code null}.
     * @param version Version to set for properties. Value can not be {@code null}.
     * @throws IllegalArgumentException when either the properties or version parmeters are null.
     */
    public ReportedPropertiesParameters(Set<Property> properties, Integer version) throws IllegalArgumentException
    {
        this(properties);
        if (version == null) {
            throw new IllegalArgumentException("Version cannot be null. Please use the constructor without the version parameter.");
        }
        this.version = version;
    }

    /**
     * Set the correlation callback for the sendReportedPropertiesAsync method
     *
     * @param twinMessageStatusCallback A callback that will monitor the message lifecycle. Value can be {@code null}.
     */
    public void setCorrelationCallback(TwinMessageStatusCallback twinMessageStatusCallback) {
        this.setCorrelationCallback(twinMessageStatusCallback, null);
    }

    /**
     * Set the correlation callback for the sendReportedPropertiesAsync method
     *
     * @param twinMessageStatusCallback A callback that will monitor the message lifecycle. Value can be {@code null}.
     * @param correlatingMessageCallbackContext The context for the callback. Value can be {@code null}.
     */
    public void setCorrelationCallback(TwinMessageStatusCallback twinMessageStatusCallback, Object correlatingMessageCallbackContext) {
        this.twinMessageStatusCallback = twinMessageStatusCallback;
        this.correlatingMessageCallbackContext = correlatingMessageCallbackContext;
    }

    /**
     * Set the event callback for the sendReportedPropertiesAsync method
     *
     * @param reportedPropertiesCallback A callback that will be executed once the messaage has been sent and acknowledged. Value can be {@code null}.
     */
    public void setReportedPropertiesCallback(IotHubEventCallback reportedPropertiesCallback) {
       this.setReportedPropertiesCallback(reportedPropertiesCallback, null);
    }

    /**
     * Set the event callback for the sendReportedPropertiesAsync method
     *
     * @param reportedPropertiesCallback A callback that will be executed once the messaage has been sent and acknowledged. Value can be {@code null}.
     * @param reportedPropertiesCallbackContext The context for the callback. Value can be {@code null}.
     */
    public void setReportedPropertiesCallback(IotHubEventCallback reportedPropertiesCallback, Object reportedPropertiesCallbackContext) {
        this.reportedPropertiesCallback = reportedPropertiesCallback;
        this.reportedPropertiesCallbackContext = reportedPropertiesCallbackContext;
    }

    Set<Property> getReportedProperties() {
        return reportedProperties;
    }

    Integer getVersion() {
        return version;
    }

    TwinMessageStatusCallback getTwinMessageStatusCallback() {
        return twinMessageStatusCallback;
    }

    IotHubEventCallback getReportedPropertiesCallback() {
        return reportedPropertiesCallback;
    }

    Object getCorrelatingMessageCallbackContext() {
        return correlatingMessageCallbackContext;
    }

    Object getReportedPropertiesCallbackContext() {
        return reportedPropertiesCallbackContext;
    }

    /**
     * The reported properties to send
     */
    private final Set<Property> reportedProperties;
    /**
     * The version of the properties
     */
    private Integer version = null;
    /**
     * A correlation callback to monitor the message lifecycle
     */
    private TwinMessageStatusCallback twinMessageStatusCallback = null;
    /**
     * The correlation callback context
     */
    private Object correlatingMessageCallbackContext = null;
    /**
     * An event callback to get the status of the event
     */
    private IotHubEventCallback reportedPropertiesCallback = null;
    /**
     * The event callback context
     */
    private Object reportedPropertiesCallbackContext = null;
}
