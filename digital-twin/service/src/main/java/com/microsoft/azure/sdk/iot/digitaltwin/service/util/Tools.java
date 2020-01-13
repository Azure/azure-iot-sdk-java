// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import rx.Observable;
import rx.functions.Func1;

import static org.apache.commons.lang3.StringUtils.EMPTY;

public final class Tools {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final Func1<Object, Observable<String>> FUNC_MAP_TO_JSON_STRING = object -> {
        try {
            return Observable.just(objectMapper.writeValueAsString(object));
        }
        catch (JsonProcessingException e) {
            return Observable.error(e);
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
