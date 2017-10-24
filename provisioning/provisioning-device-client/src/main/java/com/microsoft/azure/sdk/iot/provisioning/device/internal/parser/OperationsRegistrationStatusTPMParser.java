/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.parser;

import com.google.gson.annotations.SerializedName;

public class OperationsRegistrationStatusTPMParser
{
    /*
    {"authenticationKey":"ADQAIBza8eEgzPYtx4v7H6zsqpGjHrNZOQP9pJWUEjf7vVTDcy6ufm1ybwtixoqcdBcWVsXRAQCs3K91JNqMDf9AtN6w92+vyyfYOeSsd+z3f/8/qar5ErdjDHql+tXL/Qi2jWKi6INdBv2HvX1JpGXPx2qSFoRnph6iZ8+EMS09ZNqsMBWvPzb3+ayZS0C6GdCuMwgzmEbdBDMCEPJwg37KJCnI6jOTFGDbnPDuHSDyVKcIIBcdigztgUg2BEflppCuUyiuJidh3Xj/w+zxnYx4G8Dx5fRUBMCeQ1XPInqSfUiltkPzF9H6j9FQRYLuZcxOlMR7P2OI4ouZAgNsA15Ibpov2HjirlKeG3AW3Gn5oPZ2f0tC7JORjfuQPtAibvQSzEp+CFLGp2KgZx7L+HGXyi2jzvSvAI4AIAjG001xRmE9EUrbee4GbiCz/AxpRTswVTJ/GaJX4TA/9Hg95BkAjyHWtrPPATRQ+712diRXRRjEl/U9HSoxrCgLbMkEH2nYoL26f8g/m/vE3i7FSZGgss2qeQQuEnH7zJRhr6kw4U7vwHQgUNP9uZAOc8iZyxnURlXtUTQRGS/cpGz1mUGBhn9f+WvFAQCxAJYZ06lBd+2/WEQDRpYoQ/X3yl6SRTc4tyJkPqtddqCVsEFhfiKmEvv4zhfVvdjLf3DRgRhN8INIWFIYBQxeP6ACFvp/uNTJ1fpVWDh/2gAlGpq8wgzcnJXT+6h1/9QaOZTMsMbxovBt1szhHXRSD+DBtHLnkoI6UtXVF/NI0VVayeXz9S64T7UvSKBUteeOBEPxHYxGWHvGvDJWRh7F/CyQ+iTrDhZWtG2p26XC97h/SfYPzgUCwV3jdYBE1m8HX3zZmv4zW0OpEqk86HGj7Qs5+1s5ieGze7uh33OJmgfUQYFzamyQg1Fmsyh8AloE2UJJp1XcX+xUhTwQyaxjADAACAALAAQEQAAAAAUACwAg0Eljuu4pLFi1zEjQuK8JExRpLaXOrUSjJ5MQN39ZT18AbEK+gzmSrhTIjwk0hQcOEjcjDW1QLJN1HZyKKJiPk2Weo9pcwBXR1oCRZgdHrXxXkd90dku1fgW+1qEoisHBpitNIn9zkRQy+zuzx982tZjE614LX/dlFAmSxCRtInngy5i7Y5R1PCcKzLo6Iw=="}
     */

    private static final String AUTHENTICATION_KEY = "authenticationKey";
    @SerializedName(AUTHENTICATION_KEY)
    private String authenticationKey;

    public String getAuthenticationKey()
    {
        return authenticationKey;
    }
}
