// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

/**
 * Interface for receiving desired property callbacks one property at a time. See {@link TwinPropertiesCallback} for the
 * interface for receiving desired property callbacks all at once.
 */
public interface TwinPropertyCallback
{
    @SuppressWarnings("MethodNameSameAsClassName") //This is a public interface, renaming this method would be a breaking-change
    void TwinPropertyCallback(Property property, Object context);
}
