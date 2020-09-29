// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.models.DigitalTwinGetHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.models.DigitalTwinInvokeComponentCommandHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.generated.models.DigitalTwinInvokeRootLevelCommandHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinCommandResponse;
import com.microsoft.azure.sdk.iot.service.digitaltwin.models.DigitalTwinInvokeCommandHeaders;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.rest.ServiceResponseWithHeaders;
import rx.Observable;
import rx.functions.Func1;

public final class Tools {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final Func1<ServiceResponseWithHeaders<Object, DigitalTwinInvokeRootLevelCommandHeaders>, Observable<ServiceResponseWithHeaders<DigitalTwinCommandResponse, DigitalTwinInvokeCommandHeaders>>> FUNC_TO_DIGITAL_TWIN_COMMAND_RESPONSE = object -> {
        try {
            DigitalTwinCommandResponse digitalTwinCommandResponse = new DigitalTwinCommandResponse();
            digitalTwinCommandResponse.setPayload(objectMapper.writeValueAsString(object.body()));
            digitalTwinCommandResponse.setStatus(object.headers().xMsCommandStatuscode());
            DigitalTwinInvokeCommandHeaders digitalTwinInvokeCommandHeaders = new DigitalTwinInvokeCommandHeaders();
            digitalTwinInvokeCommandHeaders.setRequestId(object.headers().xMsRequestId());
            ServiceResponseWithHeaders<DigitalTwinCommandResponse, DigitalTwinInvokeCommandHeaders> result = new ServiceResponseWithHeaders<>(digitalTwinCommandResponse, digitalTwinInvokeCommandHeaders, object.response());
            return Observable.just(result);
        }
        catch (JsonProcessingException e) {
            return Observable.error(new IotHubException("Failed to parse the resonse"));
        }

    };

    public static final Func1<ServiceResponseWithHeaders<Object, DigitalTwinInvokeComponentCommandHeaders>, Observable<ServiceResponseWithHeaders<DigitalTwinCommandResponse, DigitalTwinInvokeCommandHeaders>>> FUNC_TO_DIGITAL_TWIN_COMPONENT_COMMAND_RESPONSE = object -> {
        try {
            DigitalTwinCommandResponse digitalTwinCommandResponse = new DigitalTwinCommandResponse();
            digitalTwinCommandResponse.setPayload(objectMapper.writeValueAsString(object.body()));
            digitalTwinCommandResponse.setStatus(object.headers().xMsCommandStatuscode());
            DigitalTwinInvokeCommandHeaders digitalTwinInvokeCommandHeaders = new DigitalTwinInvokeCommandHeaders();
            digitalTwinInvokeCommandHeaders.setRequestId(object.headers().xMsRequestId());
            ServiceResponseWithHeaders<DigitalTwinCommandResponse, DigitalTwinInvokeCommandHeaders> result = new ServiceResponseWithHeaders<>(digitalTwinCommandResponse, digitalTwinInvokeCommandHeaders, object.response());
            return Observable.just(result);
        }
        catch (JsonProcessingException e) {
            return Observable.error(new IotHubException("Failed to parse the resonse"));
        }

    };

    /**
     * Empty private constructor to prevent accidental creation of instances
     */
    private Tools() {

    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }
}
