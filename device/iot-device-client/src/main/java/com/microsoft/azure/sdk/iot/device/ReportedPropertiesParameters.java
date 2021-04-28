package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Convenience class for the sendReportedProperties method in the DeviceClient
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
        _reportedProperties = properties;
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
        _version = version;
    }

    /**
     * Set the correlation callback for the sendReportedProperties method
     *
     * @param correlatingMessageCallback A callback that will monitor the message lifecycle. Value can be {@code null}.
     */
    public void setCorrelationCallback(CorrelatingMessageCallback correlatingMessageCallback) {
        this.setCorrelationCallback(correlatingMessageCallback, null);
    }

    /**
     * Set the correlation callback for the sendReportedProperties method
     *
     * @param correlatingMessageCallback A callback that will monitor the message lifecycle. Value can be {@code null}.
     * @param correlatingMessageCallbackContext The context for the callback. Value can be {@code null}.
     */
    public void setCorrelationCallback(CorrelatingMessageCallback correlatingMessageCallback, Object correlatingMessageCallbackContext) {
        this._correlatingMessageCallback = correlatingMessageCallback;
        this._correlatingMessageCallbackContext = correlatingMessageCallbackContext;
    }

    /**
     * Set the event callback for the sendReportedProperties method
     *
     * @param reportedPropertiesCallback A callback that will be executed once the messaage has been sent and acknowledged. Value can be {@code null}.
     */
    public void setReportedPropertiesCallback(IotHubEventCallback reportedPropertiesCallback) {
       this.setReportedPropertiesCallback(reportedPropertiesCallback, null);
    }

    /**
     * Set the event callback for the sendReportedProperties method
     *
     * @param reportedPropertiesCallback A callback that will be executed once the messaage has been sent and acknowledged. Value can be {@code null}.
     * @param reportedPropertiesCallbackContext The context for the callback. Value can be {@code null}.
     */
    public void setReportedPropertiesCallback(IotHubEventCallback reportedPropertiesCallback, Object reportedPropertiesCallbackContext) {
        this._reportedPropertiesCallback = reportedPropertiesCallback;
        this._reportedPropertiesCallbackContext = reportedPropertiesCallbackContext;
    }

    Set<Property> getReportedProperties() {
        return _reportedProperties;
    }

    Integer getVersion() {
        return _version;
    }

    CorrelatingMessageCallback getCorrelatingMessageCallback() {
        return _correlatingMessageCallback;
    }

    IotHubEventCallback getReportedPropertiesCallback() {
        return _reportedPropertiesCallback;
    }

    Object getCorrelatingMessageCallbackContext() {
        return _correlatingMessageCallbackContext;
    }

    Object getReportedPropertiesCallbackContext() {
        return _reportedPropertiesCallbackContext;
    }

    /**
     * The reported properties to send
     */
    private Set<Property> _reportedProperties = null;
    /**
     * The version of the properties
     */
    private Integer _version = null;
    /**
     * A correlation callback to monitor the message lifecycle
     */
    private CorrelatingMessageCallback _correlatingMessageCallback = null;
    /**
     * The correlation callback context
     */
    private Object _correlatingMessageCallbackContext = null;
    /**
     * An event callback to get the status of the event
     */
    private IotHubEventCallback _reportedPropertiesCallback = null;
    /**
     * The event callback context
     */
    private Object _reportedPropertiesCallbackContext = null;
}
