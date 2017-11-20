/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

public class RegisterResponseTPMParser
{
    /*
    {"message":"Authorization required, resend request using supplied key","authenticationKey":"ADQAIEIaHWyoUFwtwzRFtc/SrcYFlCat3n3kzQwndE58WWwnu7LYeOBURNP25cw63wJ2E0+PAQAhLOL+j81LidbaSuhc+TvWK5mdrTgzJdBgeHxGgRG+hrxRoiOnx6kMyNlqRi2DEsSRauAbCbs+XZwVLN44+inuS+KTshLtMNlobz/ewF0TAo0vhuU4hmKVToaQ/hcFrRgDOJ+8jp/dApYn4miVnJAhYxwfPR/c2lIQXMN++VOoOkcXTbfj1xaAw62tGJuVP2OHk8P+pacTN+FVICmIEOW7VPuzYqyjjMaPMmAbeq1O7kizzBGC8jJ1D1vf1ZzeikyTy3/eXf/LSCm4yNfWR9/TNN+Xl33pkavSGm0sbosxox1VhgZqScMZyPAPY+iV/Am9iGtOy5jPF/3ZTIHbRsuvAI4AIBx+6ufIGZuFjfPAVpP2ekjyhmxjegXsGR9dTFWot++Q4/GVq4YI683bqojkyPO8X/H3z4yGGd9aPZMgT7Z211cPpVlq7ki7GAL3fWVdodw41GqjYsGL9Nn3n27W6djiIQAxkiu6jRvIW26QZYPjGgr1ZC4MDkV+aO1GKCotU43mFZs0jUFZ9D4qAlyrAQBZ/AwCB4F3YmncQTCp764BnVjGI/Xo4wTn+EMOU/VckvgV1xkHBp2IJHj586+BEGYjyTFdPeTkymD9ASGTX7mFCjGgwn8ArwREcT5Kxv6AF4hQINeR1ixZ4cL5GMttXIbDO/WygHjQqefhk3+17Zbv11dopSRhvO0dPF/+wQXml+0C62I3qtNcOco/fWyZiql2WQBbV2tuGChxfrXXkvGYPtiI1kzWrZUf9u8Wsv45q0KX5DbINqgqJ2vgAjvNBe35TR1sEoQBd0A+TPcVS2BeBQEVA01NjaRlijs88woCLe2BUwd5XEiMgDsrHVo8YRozOGs68AnP8nhAVqlMLyzbADAACAALAAQEQAAAAAUACwAgF9L3I1ZVCByjzEOGfp4w4tCGYYLxv1ZGb/Xl+whFEi0AmlJeuO9LZSq3XV2hZGBUfRdkNpfckWlZaY9bLkpJs6vse0VUb8Pu7czw56/kD0Fmrfpmwulo6ZtUlyuZGX0J9ETbYF7XHqTVxt5kyj4Fj67ttpz3B/RmToeTgjDNj5tdHKEtW2gruKz9zuE3qn+mHJhg/PhYGLPQ1h1UpGJd/2mZzLy7jD0M/tExrO+bXVF607AzrzDONRXnMRY=",
    "unencryptedAuthenticationKey":null,"keyName":"registration"}
     */

    private static final String MESSAGE = "message";
    @SerializedName(MESSAGE)
    private String message;

    private static final String AUTHENTICATION_KEY = "authenticationKey";
    @SerializedName(AUTHENTICATION_KEY)
    private String authenticationKey;

    private static final String UNENCRYPTED_AUTHENTICATION_KEY = "unencryptedAuthenticationKey";
    @SerializedName(UNENCRYPTED_AUTHENTICATION_KEY)
    private String unencryptedAuthenticationKey;

    private static final String KEY_NAME = "keyName";
    @SerializedName(KEY_NAME)
    private String keyName;

    private RegisterResponseTPMParser()
    {
    }

    static public RegisterResponseTPMParser createFromJson(String json) throws IllegalArgumentException
    {
        if((json == null) || json.isEmpty())
        {
            throw new IllegalArgumentException("Json is null or empty");
        }

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        RegisterResponseTPMParser registerResponseTPMParser = null;

        try
        {
            registerResponseTPMParser = gson.fromJson(json, RegisterResponseTPMParser.class);
        }
        catch (JsonSyntaxException malformed)
        {
            throw new IllegalArgumentException("Malformed json:" + malformed);
        }

        return registerResponseTPMParser;
    }

    public String getMessage()
    {
        return message;
    }

    public String getAuthenticationKey()
    {
        return authenticationKey;
    }

    public String getUnencryptedAuthenticationKey()
    {
        return unencryptedAuthenticationKey;
    }

    public String getKeyName()
    {
        return keyName;
    }
}
