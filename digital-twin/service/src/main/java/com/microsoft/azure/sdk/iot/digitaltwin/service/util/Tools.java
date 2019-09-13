// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.util;

import rx.functions.Func1;

import static org.apache.commons.lang3.StringUtils.EMPTY;

public final class Tools {

    public static final Func1<Object, String> FUNC_MAP_TO_STRING = new Func1<Object, String>() {

        @Override
        public String call(Object object) {
            return object == null ? null : object.toString();
        }
    };

    /**
     * Empty private constructor to prevent accidental creation of instances
     */
    private Tools() {

    }

    public static String nullToEmpty(String value) {
        return value == null ? EMPTY : value;
    }
}
