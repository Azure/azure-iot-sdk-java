/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import org.littleshoot.proxy.ProxyAuthenticator;

public class BasicProxyAuthenticator implements ProxyAuthenticator
{
    private final String expectedUsername;
    private final String expectedPassword;

    public BasicProxyAuthenticator(String expectedUsername, char[] expectedPassword)
    {
        this.expectedUsername = expectedUsername;
        this.expectedPassword = new String(expectedPassword);
    }

    @Override
    public boolean authenticate(String username, String password)
    {
        if (username == null || password == null)
        {
            return false;
        }

        return username.equals(expectedUsername) && password.equals(expectedPassword);
    }

    @Override
    public String getRealm()
    {
        return null;
    }
}
