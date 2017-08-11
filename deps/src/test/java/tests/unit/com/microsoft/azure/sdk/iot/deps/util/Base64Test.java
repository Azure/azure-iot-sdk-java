// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.util;

import com.microsoft.azure.sdk.iot.deps.util.Base64;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for Base64
 * 100% methods, 98% lines covered
 */
public class Base64Test
{
    /* Tests_SRS_BASE64_21_001: [The decodeBase64 shall decode the provided `base64Values` in a byte array using the Base64 format define in the RFC2045.] */
    @Test
    public void decodeBase64WithMultipleOf4CharactersSuccess() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        String base64ToDecode = "VGhpcyBpcyBhIHZhbGlkIHRlc3Q+Pj4+Pz8/PyhhQmNEZUZnSGlKS0xtbm9QcVJzdHVWV1h5eikwMTIzNDU2Nzg5";
        String expectedTextResult = "This is a valid test>>>>????(aBcDeFgHiJKLmnoPqRstuVWXyz)0123456789";

        // act
        byte[] result = Base64.decodeBase64(base64ToDecode.getBytes());

        // assert
        assertEquals(expectedTextResult, new String(result));
    }

    /* Tests_SRS_BASE64_21_001: [The decodeBase64 shall decode the provided `base64Values` in a byte array using the Base64 format define in the RFC2045.] */
    @Test
    public void decodeBase64WithMultipleOf4Minus1CharactersSuccess() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        String base64ToDecode = "VGhpcyBpcyBhIHZhbGlkIHRlc3QgKGFCY0RlRmdIaUpLTG1ub1BxUnN0dVZXWHl6KS0wMTIzNDU2Nzg=";
        String expectedTextResult = "This is a valid test (aBcDeFgHiJKLmnoPqRstuVWXyz)-012345678";

        // act
        byte[] result = Base64.decodeBase64(base64ToDecode.getBytes());

        // assert
        assertEquals(expectedTextResult, new String(result));
    }

    /* Tests_SRS_BASE64_21_001: [The decodeBase64 shall decode the provided `base64Values` in a byte array using the Base64 format define in the RFC2045.] */
    @Test
    public void decodeBase64WithMultipleOf4Minus2CharactersSuccess() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        String base64ToDecode = "VGhpcyBpcyBhIHZhbGlkIHRlc3QgKGFCY0RlRmdIaUpLTG1ub1BxUnN0dVZXWHl6KS0wMTIzNDU2Nw==";
        String expectedTextResult = "This is a valid test (aBcDeFgHiJKLmnoPqRstuVWXyz)-01234567";

        // act
        byte[] result = Base64.decodeBase64(base64ToDecode.getBytes());

        // assert
        assertEquals(expectedTextResult, new String(result));
    }

    /* Tests_SRS_BASE64_21_002: [If the `base64Values` is null, the decodeBase64 shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void decodeBase64ThrowsOnNullByteArray() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        byte[] base64ToDecode = null;

        // act
        Base64.decodeBase64(base64ToDecode);
    }

    /* Tests_SRS_BASE64_21_003: [If the `base64Values` is empty, the decodeBase64 shall return a empty byte array.] */
    @Test
    public void decodeBase64WithEmptyByteArraySuccess() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        String base64ToDecode = "";
        String expectedTextResult = "";

        // act
        byte[] result = Base64.decodeBase64(base64ToDecode.getBytes());

        // assert
        assertEquals(expectedTextResult, new String(result));
    }

    /* Tests_SRS_BASE64_21_004: [If the `base64Values` length is not multiple of 4, the decodeBase64 shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void decodeBase64ThrowsOnInvalidByteArrayLength() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        String base64ToDecode = "VGhpcyBpcyBhIHZhbGlkIHRlc3QgKGFCY0RlRmdIaUpLTG1ub1BxUnN0dVZXWHl6KS0wMTIzNDU2Nw";

        // act
        Base64.decodeBase64(base64ToDecode.getBytes());
    }

    /* Tests_SRS_BASE64_21_004: [If the `base64Values` length is not multiple of 4, the decodeBase64 shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void decodeBase64ThrowsOnLostPad() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        String base64ToDecode = "VGhpcyBpcyBhIHZhbGlkIHRlc3QgKGFCY0RlRmdIaUpLTG1ub1BxUnN0dVZXWHl6KS0wMTIzNDU2Nw=";

        // act
        Base64.decodeBase64(base64ToDecode.getBytes());
    }

    /* Tests_SRS_BASE64_21_004: [If the `base64Values` length is not multiple of 4, the decodeBase64 shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void decodeBase64ThrowsOnExtraPad() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        String base64ToDecode = "VGhpcyBpcyBhIHZhbGlkIHRlc3QgKGFCY0RlRmdIaUpLTG1ub1BxUnN0dVZXWHl6KS0wMTIzNDU2N===";

        // act
        Base64.decodeBase64(base64ToDecode.getBytes());
    }

    /* Tests_SRS_BASE64_21_004: [If the `base64Values` length is not multiple of 4, the decodeBase64 shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void decodeBase64ThrowsOnInvalidPad() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        String base64ToDecode = "VGhpcyBpcyBhIHZhbGlkIHRlc3QgKGFCY0RlRmdIaUpLTG1ub1BxUnN0dVZXWHl6KS0wMTIzNDU2Nw=*";

        // act
        Base64.decodeBase64(base64ToDecode.getBytes());
    }


    /* Tests_SRS_BASE64_21_005: [The encodeBase64 shall encoded the provided `dataValues` in a byte array using the Base64 format define in the RFC2045.] */
    @Test
    public void encodeBase64WithMultipleOf4CharactersSuccess() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        String textToEncode = "This is a valid test>>>>????(aBcDeFgHiJKLmnoPqRstuVWXyz)0123456789";
        String expectedBase64Result = "VGhpcyBpcyBhIHZhbGlkIHRlc3Q+Pj4+Pz8/PyhhQmNEZUZnSGlKS0xtbm9QcVJzdHVWV1h5eikwMTIzNDU2Nzg5";

        // act
        byte[] result = Base64.encodeBase64(textToEncode.getBytes());

        // assert
        assertEquals(expectedBase64Result, new String(result));
    }

    /* Tests_SRS_BASE64_21_005: [The encodeBase64 shall encoded the provided `dataValues` in a byte array using the Base64 format define in the RFC2045.] */
    @Test
    public void encodeBase64MultipleOf4Minus1CharactersSuccess() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        String textToEncode = "This is a valid test (aBcDeFgHiJKLmnoPqRstuVWXyz)-012345678";
        String expectedBase64Result = "VGhpcyBpcyBhIHZhbGlkIHRlc3QgKGFCY0RlRmdIaUpLTG1ub1BxUnN0dVZXWHl6KS0wMTIzNDU2Nzg=";

        // act
        byte[] result = Base64.encodeBase64(textToEncode.getBytes());

        // assert
        assertEquals(expectedBase64Result, new String(result));
    }

    /* Tests_SRS_BASE64_21_005: [The encodeBase64 shall encoded the provided `dataValues` in a byte array using the Base64 format define in the RFC2045.] */
    @Test
    public void encodeBase64MultipleOf4Minus2CharactersSuccess() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        String textToEncode = "This is a valid test (aBcDeFgHiJKLmnoPqRstuVWXyz)-01234567";
        String expectedBase64Result = "VGhpcyBpcyBhIHZhbGlkIHRlc3QgKGFCY0RlRmdIaUpLTG1ub1BxUnN0dVZXWHl6KS0wMTIzNDU2Nw==";

        // act
        byte[] result = Base64.encodeBase64(textToEncode.getBytes());

        // assert
        assertEquals(expectedBase64Result, new String(result));
    }

    /* Tests_SRS_BASE64_21_006: [If the `dataValues` is null, the encodeBase64 shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void encodeBase64ThrowsOnNullDataValues() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        byte[] textToEncode = null;

        // act
        Base64.encodeBase64(textToEncode);
    }

    /* Tests_SRS_BASE64_21_007: [If the `dataValues` is empty, the encodeBase64 shall return a empty byte array.] */
    @Test
    public void encodeBase64EmptyCharactersSuccess() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        String textToEncode = "";
        String expectedBase64Result = "";

        // act
        byte[] result = Base64.encodeBase64(textToEncode.getBytes());

        // assert
        assertEquals(expectedBase64Result, new String(result));
    }


    /* Tests_SRS_BASE64_21_008: [The encodeBase64String shall encoded the provided `dataValues` in a string using the Base64 format define in the RFC2045.] */
    @Test
    public void encodeBase64StringMultipleOf4CharactersSuccess() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        String textToEncode = "This is a valid test>>>>????(aBcDeFgHiJKLmnoPqRstuVWXyz)0123456789";
        String expectedBase64Result = "VGhpcyBpcyBhIHZhbGlkIHRlc3Q+Pj4+Pz8/PyhhQmNEZUZnSGlKS0xtbm9QcVJzdHVWV1h5eikwMTIzNDU2Nzg5";

        // act
        String result = Base64.encodeBase64String(textToEncode.getBytes());

        // assert
        assertEquals(expectedBase64Result, result);
    }

    /* Tests_SRS_BASE64_21_008: [The encodeBase64String shall encoded the provided `dataValues` in a string using the Base64 format define in the RFC2045.] */
    @Test
    public void encodeBase64StringMultipleOf4Minus1CharactersSuccess() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        String textToEncode = "This is a valid test (aBcDeFgHiJKLmnoPqRstuVWXyz)-012345678";
        String expectedBase64Result = "VGhpcyBpcyBhIHZhbGlkIHRlc3QgKGFCY0RlRmdIaUpLTG1ub1BxUnN0dVZXWHl6KS0wMTIzNDU2Nzg=";

        // act
        String result = Base64.encodeBase64String(textToEncode.getBytes());

        // assert
        assertEquals(expectedBase64Result, result);
    }

    /* Tests_SRS_BASE64_21_008: [The encodeBase64String shall encoded the provided `dataValues` in a string using the Base64 format define in the RFC2045.] */
    @Test
    public void encodeBase64StringMultipleOf4Minus2CharactersSuccess() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        String textToEncode = "This is a valid test (aBcDeFgHiJKLmnoPqRstuVWXyz)-01234567";
        String expectedBase64Result = "VGhpcyBpcyBhIHZhbGlkIHRlc3QgKGFCY0RlRmdIaUpLTG1ub1BxUnN0dVZXWHl6KS0wMTIzNDU2Nw==";

        // act
        String result = Base64.encodeBase64String(textToEncode.getBytes());

        // assert
        assertEquals(expectedBase64Result, result);
    }

    /* Tests_SRS_BASE64_21_009: [If the `dataValues` is null, the encodeBase64String shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void encodeBase64StringThrowsOnNullDataValues() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        byte[] textToEncode = null;

        // act
        Base64.encodeBase64String(textToEncode);
    }

    /* Tests_SRS_BASE64_21_010: [If the `dataValues` is empty, the encodeBase64String shall return a empty string.] */
    @Test
    public void encodeBase64StringEmptyCharactersSuccess() throws UnsupportedEncodingException, IllegalArgumentException
    {
        // arrange
        String textToEncode = "";
        String expectedBase64Result = "";

        // act
        String result = Base64.encodeBase64String(textToEncode.getBytes());

        // assert
        assertEquals(expectedBase64Result, result);
    }
}
