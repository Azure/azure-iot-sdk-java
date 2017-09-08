// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.util;

public final class Base64
{
    private static final byte BYTE_START_UPPERCASE = 'A';
    private static final byte BYTE_END_UPPERCASE = 'Z';
    private static final byte BYTE_START_LOWERCASE = 'a';
    private static final byte BYTE_END_LOWERCASE = 'z';
    private static final byte BYTE_START_NUMBER = '0';
    private static final byte BYTE_END_NUMBER = '9';
    private static final byte BYTE_PLUS = '+';
    private static final byte BYTE_SLASH = '/';

    private static final int BASE64_END_UPPERCASE = 26;
    private static final int BASE64_END_LOWERCASE = 52;
    private static final int BASE64_END_NUMBER = 62;
    private static final int BASE64_PLUS = 62;
    private static final int BASE64_SLASH = 63;
    private static final byte BASE64_PAD = '=';

    private static final int HALF_NIBBLE = 2;
    private static final int ONE_NIBBLE = 4;
    private static final int ONE_AND_HALF_NIBBLE = 6;
    private static final int ONE_BYTE = 8;
    private static final int TWO_BYTES = 16;
    private static final int THREE_BYTES = 24;

    private static final int ISOLATE_BYTE = 0xFF;
    private static final int ISOLATE_BASE64 = 0x3F;
    private static final int ISOLATE_LSB_BASE64 = 0x0F;
    private static final int ISOLATE_MSB_BASE64 = 0x03;

    private static final int BYTE_GROUP_SIZE = 3;
    private static final int BASE64_GROUP_SIZE = 4;


    private static final int[] BASE64D16_CONVERSION_TABLE =
        {
            ((int)'A' + ((int)'E'<<ONE_BYTE) + ((int)'I'<<TWO_BYTES) + ((int)'M'<<THREE_BYTES)),
            ((int)'Q' + ((int)'U'<<ONE_BYTE) + ((int)'Y'<<TWO_BYTES) + ((int)'c'<<THREE_BYTES)),
            ((int)'g' + ((int)'k'<<ONE_BYTE) + ((int)'o'<<TWO_BYTES) + ((int)'s'<<THREE_BYTES)),
            ((int)'w' + ((int)'0'<<ONE_BYTE) + ((int)'4'<<TWO_BYTES) + ((int)'8'<<THREE_BYTES)),
        };

    private static final int BASE64D8_CONVERSION_TABLE = ((int)'A' + ((int)'Q'<<ONE_BYTE) + ((int)'g'<<TWO_BYTES) + ((int)'w'<<THREE_BYTES));

    private static byte extractBase64FromInteger(final int integerValue, final int bytePosition)
    {
        return (byte)((integerValue >> (bytePosition << 3)) & ISOLATE_BYTE);
    }

    private static byte base64ToByte(final byte base64Value)
    {
        if(base64Value < BASE64_END_UPPERCASE)
        {
            return (byte)(BYTE_START_UPPERCASE + base64Value);
        }

        if(base64Value < BASE64_END_LOWERCASE)
        {
            return (byte)(BYTE_START_LOWERCASE + (base64Value - BASE64_END_UPPERCASE));
        }

        if(base64Value < BASE64_END_NUMBER)
        {
            return (byte)(BYTE_START_NUMBER + (base64Value - BASE64_END_LOWERCASE));
        }

        if(base64Value == BASE64_END_NUMBER)
        {
            return BYTE_PLUS;
        }

        return BYTE_SLASH;
    }

    private static byte base64d16ToByte(final byte base64d16Value)
    {
        return extractBase64FromInteger(BASE64D16_CONVERSION_TABLE[base64d16Value >> HALF_NIBBLE],
            (base64d16Value & ISOLATE_MSB_BASE64));
    }

    private static byte base64d8ToByte(final byte base64d8Value)
    {
        return extractBase64FromInteger(BASE64D8_CONVERSION_TABLE, base64d8Value);
    }

    private static byte byteToBase64(final byte byteValue) throws IllegalArgumentException
    {
        if((byteValue >= BYTE_START_UPPERCASE) && (byteValue <= BYTE_END_UPPERCASE))
        {
            return (byte)(byteValue - BYTE_START_UPPERCASE);
        }

        if((byteValue >= BYTE_START_LOWERCASE) && (byteValue <= BYTE_END_LOWERCASE))
        {
            return (byte)((BYTE_END_UPPERCASE - BYTE_START_UPPERCASE) + 1 +
                (byteValue - BYTE_START_LOWERCASE));
        }

        if((byteValue >= BYTE_START_NUMBER) && (byteValue <= BYTE_END_NUMBER))
        {
            return (byte)((BYTE_END_UPPERCASE - BYTE_START_UPPERCASE) + 1 +
                (BYTE_END_LOWERCASE - BYTE_START_LOWERCASE) + 1 +
                (byteValue - BYTE_START_NUMBER));
        }

        if(byteValue == BYTE_PLUS)
        {
            return BASE64_PLUS;
        }

        if(byteValue == BYTE_SLASH)
        {
            return BASE64_SLASH;
        }

        throw new IllegalArgumentException("provided byte value out of base64 range");
    }

    private static int numberOfValidBase64BytesWithoutPad(final byte[] bytesToEncode) throws IllegalArgumentException
    {
        int validLength = bytesToEncode.length;

        if(bytesToEncode[validLength-1] == BASE64_PAD)
        {
            validLength--;
        }

        if(bytesToEncode[validLength-1] == BASE64_PAD)
        {
            validLength--;
        }

        return validLength;
    }

    private static int base64EstimatedLength(final byte[] base64sToDecode)
    {
        int estimatedLength;

        if(base64sToDecode.length == 0)
        {
            return 0;
        }

        estimatedLength = base64sToDecode.length / BASE64_GROUP_SIZE * BYTE_GROUP_SIZE;
        if(base64sToDecode[base64sToDecode.length - 1] == BASE64_PAD)
        {
            if(base64sToDecode[base64sToDecode.length - 2] == BASE64_PAD)
            {
                estimatedLength --;
            }
            estimatedLength --;
        }

        return estimatedLength;
    }

    /**
     * Convert a array of base64 encoded byte in a array of bytes, returning the bytes
     * original values.
     * <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>.
     *
     * Base64 only uses 6 bits, so fits each set of 4 base64 in 3 bytes
     *     Base64  |     c1    |     c2    |     c3    |     c4    |
     *             |7 6 5 4 3 2 1 0:7 6 5 4 3 2 1 0:7 6 5 4 3 2 1 0|
     *     Byte    |       b1      |       b2      |       b3      |
     *
     * @param base64Values is an array of base64 encoded values
     * @return an array of bytes with the original values
     * @throws IllegalArgumentException if the provided base64 values are null, or do not fits the required length
     */
    public static byte[] decodeBase64Local(final byte[] base64Values) throws IllegalArgumentException
    {
        /* Codes_SRS_BASE64_21_002: [If the `base64Values` is null, the decodeBase64Local shall throw IllegalArgumentException.] */
        if(base64Values == null)
        {
            throw new IllegalArgumentException("null or empty base64Values");
        }

        /* Codes_SRS_BASE64_21_003: [If the `base64Values` is empty, the decodeBase64Local shall return a empty byte array.] */
        if(base64Values.length == 0)
        {
            return new byte[0];
        }

        /* Codes_SRS_BASE64_21_004: [If the `base64Values` length is not multiple of 4, the decodeBase64Local shall throw IllegalArgumentException.] */
        if((base64Values.length % BASE64_GROUP_SIZE) != 0)
        {
            throw new IllegalArgumentException("invalid base64Values length");
        }

        /* Codes_SRS_BASE64_21_001: [The decodeBase64Local shall decode the provided `base64Values` in a byte array using the Base64 format define in the RFC2045.] */
        int numberOfEncodedBytes = numberOfValidBase64BytesWithoutPad(base64Values);
        int indexOfFirstEncodedByte = 0;
        int decodedIndex = 0;
        byte[] decodedResult = new byte[base64EstimatedLength(base64Values)];

        while(numberOfEncodedBytes >= BASE64_GROUP_SIZE)
        {
            byte c1 = byteToBase64(base64Values[indexOfFirstEncodedByte++]);
            byte c2 = byteToBase64(base64Values[indexOfFirstEncodedByte++]);
            byte c3 = byteToBase64(base64Values[indexOfFirstEncodedByte++]);
            byte c4 = byteToBase64(base64Values[indexOfFirstEncodedByte++]);
            decodedResult[decodedIndex++] = (byte)((c1 << HALF_NIBBLE) | (c2 >> ONE_NIBBLE));
            decodedResult[decodedIndex++] = (byte)((c2 << ONE_NIBBLE) | (c3 >> HALF_NIBBLE));
            decodedResult[decodedIndex++] = (byte)((c3 << ONE_AND_HALF_NIBBLE) | c4);
            numberOfEncodedBytes -= BASE64_GROUP_SIZE;
        }

        if(numberOfEncodedBytes == 3)
        {
            byte c1 = byteToBase64(base64Values[indexOfFirstEncodedByte++]);
            byte c2 = byteToBase64(base64Values[indexOfFirstEncodedByte++]);
            byte c3 = byteToBase64(base64Values[indexOfFirstEncodedByte]);
            decodedResult[decodedIndex++] = (byte)((c1 << HALF_NIBBLE) | (c2 >> ONE_NIBBLE));
            decodedResult[decodedIndex] = (byte)((c2 << ONE_NIBBLE) | (c3 >> HALF_NIBBLE));
        }

        if(numberOfEncodedBytes == 2)
        {
            byte c1 = byteToBase64(base64Values[indexOfFirstEncodedByte++]);
            byte c2 = byteToBase64(base64Values[indexOfFirstEncodedByte]);
            decodedResult[decodedIndex] = (byte)((c1 << HALF_NIBBLE) | (c2 >> ONE_NIBBLE));
        }

        return decodedResult;
    }

    /**
     * Convert a array of bytes in a array of MIME   Base64 values.
     * <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>.
     *
     * @param dataValues is an array of bytes with the original values
     * @return an array of base64 encoded values
     * @throws IllegalArgumentException if the provided base64 values are null, or do not fits the required length
     */
    public static byte[] encodeBase64Local(byte[] dataValues) throws IllegalArgumentException
    {
        /* Codes_SRS_BASE64_21_006: [If the `dataValues` is null, the encodeBase64Local shall throw IllegalArgumentException.] */
        if(dataValues == null)
        {
            throw new IllegalArgumentException("null or empty dataValues");
        }

        /* Codes_SRS_BASE64_21_007: [If the `dataValues` is empty, the encodeBase64Local shall return a empty byte array.] */
        if(dataValues.length == 0)
        {
            return new byte[0];
        }

        /* Codes_SRS_BASE64_21_005: [The encodeBase64Local shall encoded the provided `dataValues` in a byte array using the Base64 format define in the RFC2045.] */
        return encodeBase64Internal(dataValues);
    }

    /**
     * Convert a array of bytes in a array of MIME Base64 values.
     * <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>.
     *
     * @param dataValues is an array of bytes with the original values
     * @return a string with the base64 encoded values
     * @throws IllegalArgumentException if the provided base64 values are null, or do not fits the required length
     */
    public static String encodeBase64StringLocal(byte[] dataValues) throws IllegalArgumentException
    {
        /* Codes_SRS_BASE64_21_009: [If the `dataValues` is null, the encodeBase64StringLocal shall throw IllegalArgumentException.] */
        if(dataValues == null)
        {
            throw new IllegalArgumentException("null or empty dataValues");
        }

        /* Codes_SRS_BASE64_21_010: [If the `dataValues` is empty, the encodeBase64StringLocal shall return a empty string.] */
        if(dataValues.length == 0)
        {
            return new String();
        }

        /* Codes_SRS_BASE64_21_008: [The encodeBase64StringLocal shall encoded the provided `dataValues` in a string using the Base64 format define in the RFC2045.] */
        return new String(encodeBase64Internal(dataValues));
    }

    private static byte[] encodeBase64Internal(byte[] dataValues) throws IllegalArgumentException
    {
        int encodedLength = (((dataValues.length - 1) / BYTE_GROUP_SIZE) + 1) * BASE64_GROUP_SIZE;
        int destinationPosition = 0;
        int currentPosition = 0;

        byte[] encodedResult = new byte[encodedLength];

        while((dataValues.length - currentPosition) >= BYTE_GROUP_SIZE)
        {
            encodedResult[destinationPosition++] = base64ToByte((byte)((dataValues[currentPosition] >> HALF_NIBBLE) & ISOLATE_BASE64));
            encodedResult[destinationPosition++] = base64ToByte((byte)(((dataValues[currentPosition] << ONE_NIBBLE) & ISOLATE_BASE64) |
                ((dataValues[currentPosition + 1] >> ONE_NIBBLE) & ISOLATE_LSB_BASE64)));
            encodedResult[destinationPosition++] = base64ToByte((byte)(((dataValues[currentPosition + 1] << HALF_NIBBLE) & ISOLATE_BASE64) |
                ((dataValues[currentPosition + 2] >> ONE_AND_HALF_NIBBLE) & ISOLATE_MSB_BASE64)));
            encodedResult[destinationPosition++] = base64ToByte((byte)(dataValues[currentPosition + 2] & ISOLATE_BASE64));
            currentPosition += BYTE_GROUP_SIZE;
        }

        if((dataValues.length - currentPosition) == 2)
        {
            encodedResult[destinationPosition++] = base64ToByte((byte)((dataValues[currentPosition] >> HALF_NIBBLE) & ISOLATE_BASE64));
            encodedResult[destinationPosition++] = base64ToByte((byte)(((dataValues[currentPosition] << ONE_NIBBLE) & ISOLATE_BASE64) |
                ((dataValues[currentPosition + 1] >> ONE_NIBBLE) & ISOLATE_LSB_BASE64)));
            encodedResult[destinationPosition++] = base64d16ToByte((byte)(dataValues[currentPosition + 1] & ISOLATE_LSB_BASE64));
            encodedResult[destinationPosition] = BASE64_PAD;
        }

        if((dataValues.length - currentPosition) == 1)
        {
            encodedResult[destinationPosition++] = base64ToByte((byte)((dataValues[currentPosition] >> HALF_NIBBLE) & ISOLATE_BASE64));
            encodedResult[destinationPosition++] = base64d8ToByte((byte)(dataValues[currentPosition] & ISOLATE_MSB_BASE64));
            encodedResult[destinationPosition++] = BASE64_PAD;
            encodedResult[destinationPosition] = BASE64_PAD;
        }

        return encodedResult;
    }
}
