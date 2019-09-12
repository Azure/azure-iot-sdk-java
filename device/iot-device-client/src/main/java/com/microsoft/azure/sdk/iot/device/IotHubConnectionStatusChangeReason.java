/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device;

public enum IotHubConnectionStatusChangeReason
{
    // The SAS token used to open the connection has expired
    EXPIRED_SAS_TOKEN,

    // The SAS token/Certificates used to open the connection are rejected by the service with UNAUTHORIZED
    BAD_CREDENTIAL,

    // The SDK has exhausted its retry policy and has stopped retrying
    RETRY_EXPIRED,

    // The SDK encountered a retryable exception (IOExceptions, etc.)
    NO_NETWORK,

    // The default case, only given when no other change reason is applicable. See the thrown exception for more details
    COMMUNICATION_ERROR,

    // The SDK opened the connection successfully
    CONNECTION_OK,

    // The SDK closed the connection because the user closed the client that was using it
    CLIENT_CLOSE
}
