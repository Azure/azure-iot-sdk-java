// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.util;

public final class Tools {

    /**
     * Empty private constructor to prevent accidental creation of instances
     */
    private Tools() {

    }

    /**
     * Helper function to check if the input string is null or empty
     *
     * @param value The string to check
     * @return The value true if the input string is empty or null
     */
    public static boolean isNullOrEmpty(String value) {
        return (value == null || value.isEmpty());
    }

    /**
     * Helper function to check if the input string is null or contains only whitespace(s)
     *
     * @param value The string to check
     * @return The value true if the input string is empty or contains only whitespace(s)
     */
    public static boolean isNullOrWhiteSpace(String value) {
        return (value == null || value.trim().isEmpty());
    }
}
