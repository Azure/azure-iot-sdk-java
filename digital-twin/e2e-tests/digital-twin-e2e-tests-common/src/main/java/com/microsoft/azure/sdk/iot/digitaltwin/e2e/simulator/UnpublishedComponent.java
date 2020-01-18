// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.e2e.simulator;

import com.microsoft.azure.sdk.iot.digitaltwin.device.AbstractDigitalTwinComponent;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnpublishedComponent extends AbstractDigitalTwinComponent {
    public static final String TEST_INTERFACE_ID = "urn:someNamespace:someCompanyName:unpublishedInterface:1";

    public UnpublishedComponent(@NonNull String digitalTwinComponentName) {
        super(digitalTwinComponentName, TEST_INTERFACE_ID);
    }
}
