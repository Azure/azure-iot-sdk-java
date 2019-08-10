package com.microsoft.azure.sdk.iot.digitaltwin.device;

/**
 * User specified callback that will be invoked on async operation completion or failure.
 */
public interface DigitalTwinCallback {
    /**
     * Function to be invoked when the async operation is successfully or fails.
     * @param digitalTwinClientResult Result for the async operation
     * @param context Context passed in when async operation is invoked.
     */
    void onResult(DigitalTwinClientResult digitalTwinClientResult, Object context);
}
