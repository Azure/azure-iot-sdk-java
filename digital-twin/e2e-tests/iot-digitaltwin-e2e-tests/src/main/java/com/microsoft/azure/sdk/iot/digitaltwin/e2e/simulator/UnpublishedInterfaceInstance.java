// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator;

import com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinInterfaceClient;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnpublishedInterfaceInstance extends AbstractDigitalTwinInterfaceClient {
    public static final String TEST_INTERFACE_ID = "urn:someNamespace:someCompanyName:unpublishedInterface:1";

    private static String interfaceInstanceName;

    public UnpublishedInterfaceInstance(@NonNull String digitalTwinInterfaceInstanceName) {
        super(digitalTwinInterfaceInstanceName, TEST_INTERFACE_ID);
        interfaceInstanceName = digitalTwinInterfaceInstanceName;
    }

    @Override
    public void onRegistered() {
        log.debug("Interface Instance registered with name: {}", interfaceInstanceName);
    }
}
