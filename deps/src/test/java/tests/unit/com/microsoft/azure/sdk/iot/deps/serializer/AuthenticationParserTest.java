// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.microsoft.azure.sdk.iot.deps.serializer.AuthenticationParser;
import com.microsoft.azure.sdk.iot.deps.serializer.AuthenticationTypeParser;
import com.microsoft.azure.sdk.iot.deps.serializer.SymmetricKeyParser;
import com.microsoft.azure.sdk.iot.deps.serializer.X509ThumbprintParser;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Code Coverage:
 * Methods: 100%
 * Lines: 100%
 */
public class AuthenticationParserTest
{
    //Tests_SRS_AUTHENTICATION_PARSER_34_001: [This Constructor shall create a new instance of an authenticationParser object and return it.]
    @Test
    public void testConstructor()
    {
        //act
        AuthenticationParser parser = new AuthenticationParser();

        //assert
        assertNotNull(parser);
    }

    //Tests_SRS_AUTHENTICATION_PARSER_34_002: [This method shall return the value of this object's authenticationTypeParser.]
    //Tests_SRS_AUTHENTICATION_PARSER_34_008: [This method shall set the value of this object's authentication type equal to the provided value.]
    @Test
    public void testAuthenticationTypeParserProperty()
    {
        //arrange
        AuthenticationParser parser = new AuthenticationParser();

        //act
        parser.setType(AuthenticationTypeParser.SAS);

        //assert
        assertEquals(AuthenticationTypeParser.SAS, parser.getType());
    }


    //Tests_SRS_AUTHENTICATION_PARSER_34_004: [This method shall return the value of this object's thumbprint.]
    //Tests_SRS_AUTHENTICATION_PARSER_34_005: [This method shall set the value of this object's thumbprint equal to the provided value.]
    @Test
    public void testThumbprintParserProperty()
    {
        //arrange
        AuthenticationParser parser = new AuthenticationParser();
        X509ThumbprintParser thumbprintParser = new X509ThumbprintParser();
        thumbprintParser.setPrimaryThumbprint("1234");
        thumbprintParser.setSecondaryThumbprint("5678");

        //act
        parser.setThumbprint(thumbprintParser);

        //assert
        assertEquals(thumbprintParser, parser.getThumbprint());
    }

    //Tests_SRS_AUTHENTICATION_PARSER_34_006: [This method shall return the value of this object's symmetricKey.]
    //Tests_SRS_AUTHENTICATION_PARSER_34_007: [This method shall set the value of symmetricKey equal to the provided value.]
    @Test
    public void testSymmetricKeyParserProperty()
    {
        //arrange
        AuthenticationParser parser = new AuthenticationParser();
        SymmetricKeyParser symmetricKeyParser = new SymmetricKeyParser();
        symmetricKeyParser.setPrimaryKey("1234");
        symmetricKeyParser.setPrimaryKey("5678");

        //act
        parser.setSymmetricKey(symmetricKeyParser);

        //assert
        assertEquals(symmetricKeyParser, parser.getSymmetricKey());
    }


    //Tests_SRS_AUTHENTICATION_PARSER_34_003: [If the provided type is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void cannotSetTypeToNull()
    {
        //act
        new AuthenticationParser().setType(null);
    }
}
