package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.github.monkeywie.proxyee.server.auth.HttpProxyAuthenticationProvider;
import com.github.monkeywie.proxyee.server.auth.model.BasicHttpToken;

import java.util.Base64;

public class BasicProxyAuthenticator implements HttpProxyAuthenticationProvider<BasicHttpToken>
{
    public static final String AUTH_TYPE_BASIC = "BASIC";
    public static final String AUTH_REALM_BASIC = "Access to the staging site";

    public String expectedUsername;
    public String expectedPassword;

    public BasicProxyAuthenticator(String expectedUsername, String expectedPassword)
    {
        this.expectedUsername = expectedUsername;
        this.expectedPassword = expectedPassword;
    }


    public String authType() {
        return AUTH_TYPE_BASIC;
    }

    public String authRealm() {
        return AUTH_REALM_BASIC;
    }

    protected BasicHttpToken authenticate(String usr, String pwd)
    {
        if (this.expectedUsername.equals(usr) && this.expectedPassword.equals(pwd)) {
            return new BasicHttpToken(usr, pwd);
        }
        return null;
    }

    public BasicHttpToken authenticate(String authorization) {
        String usr = "";
        String pwd = "";
        if (authorization != null && authorization.length() > 0) {
            String token = authorization.substring(AUTH_TYPE_BASIC.length() + 1);
            String decode = new String(Base64.getDecoder().decode(token));
            String[] arr = decode.split(":");
            usr = arr.length >= 1 ? arr[0] : "";
            pwd = arr.length >= 2 ? arr[1] : "";
        }
        return authenticate(usr, pwd);
    }
}