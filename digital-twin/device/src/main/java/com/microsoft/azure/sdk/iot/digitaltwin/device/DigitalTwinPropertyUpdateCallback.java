package com.microsoft.azure.sdk.iot.digitaltwin.device;

import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinPropertyUpdate;

/**
 * Callback that is invoked by the Digital Twin SDK when a desired property is available from the service.
 * There are two scenarios where this callback may be invoked.  After this interface is initially registered, the Digital Twin SDK will query all desired properties on
 * it and invoke the callback.  The SDK will invoke this callback even if the property has not changed since any previous invocations, since the SDK does not
 * have a persistent cache of state. After this initial update, the SDK will also invoke this callback again whenever any properties change.
 * If multiple properties are available from the service simultaneously, this callback will be called once for each updated property.  There is no attempt to batch multiple properties into one call.
 */
public interface DigitalTwinPropertyUpdateCallback {
    /**
     * Callback that is invoked by the Digital Twin SDK when a desired property is available from the service.
     * @param digitalTwinPropertyUpdate {@link DigitalTwinPropertyUpdate} structure filled in by the SDK with information about the updated property.
     * @param context Context that was specified in {@link AbstractDigitalTwinInterfaceClient} constrictor.
     */
    void onPropertyUpdate(DigitalTwinPropertyUpdate digitalTwinPropertyUpdate, Object context);
}
