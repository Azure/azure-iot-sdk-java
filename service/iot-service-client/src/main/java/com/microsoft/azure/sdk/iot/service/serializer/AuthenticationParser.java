// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.serializer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

public class AuthenticationParser
{
    private static final String SYMMETRIC_KEY_NAME = "symmetricKey";
    @Expose
    @SerializedName(SYMMETRIC_KEY_NAME)
    @Getter
    @Setter
    private SymmetricKeyParser symmetricKey;

    private static final String X509_THUMBPRINT_NAME = "x509Thumbprint";
    @Expose
    @SerializedName(X509_THUMBPRINT_NAME)
    @Getter
    @Setter
    private X509ThumbprintParser thumbprint;

    private static final String AUTHENTICATION_TYPE_NAME = "type";
    @Expose
    @SerializedName(AUTHENTICATION_TYPE_NAME)
    @Getter
    @Setter
    private AuthenticationTypeParser type;

    public AuthenticationParser()
    {
        //Codes_SRS_AUTHENTICATION_PARSER_34_001: [This Constructor shall create a new instance of an authenticationParser object and return it.]
        //do nothing
    }

}
