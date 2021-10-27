// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks;

import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import samples.com.microsoft.azure.sdk.iot.DeviceClientManager;
import samples.com.microsoft.azure.sdk.iot.MultiplexingClientManager;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.TestParameters;

public interface MultiplexingClientLonghaulTestAddOn
{
    /**
     * Callback that is executed by the longhaul test once right before the client connection has opened to allow
     * for setting of SAS token renewal time, polling intervals, and other similar settings.
     */
    void setupClientBeforeOpen(MultiplexingClientManager multiplexingClientUnderTest, DeviceClientManager[] multiplexedClientsUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception;

    /**
     * Callback that is executed by the longhaul test once right after the client connection has opened to allow
     * for subscribing to twin/method/c2d messages.
     */
    void setupClientAfterOpen(MultiplexingClientManager multiplexingClientUnderTest, DeviceClientManager[] multiplexedClientsUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception;

    /**
     * Callback that is executed by the longhaul test periodically to allow for some statistics from the test to be logged.
     * For instance, logging unacknowledged c2d message count, logging method invocation failure count, etc.
     */
    void performPeriodicStatusReport(TestParameters testParameters);

    /**
     * Callback that is executed by the multiplexing longhaul test periodically to allow for some testable action to be run.
     * For example, sending d2c telemetry, receive a direct method, etc.
     */
    void performPeriodicTestableAction(MultiplexingClientManager multiplexingClientUnderTest, DeviceClientManager[] multiplexedClientsUnderTest, ServiceClient serviceClient, RegistryManager registryManager, TestParameters testParameters) throws Exception;

    /**
     * Callback that is executed at the end of the test that allows for further validation that all the testable actions
     * completed as expected, or completed within reasonable expectations.
     * @return true if all expectations passed, false if any expectations failed.
     */
    boolean validateExpectations(TestParameters testParameters);
}
