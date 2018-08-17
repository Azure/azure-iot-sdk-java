// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.microsoft.azure.sdk.iot.deps.serializer.*;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;

import org.junit.Test;

import java.security.NoSuchAlgorithmException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Code Coverage:
 * Methods: 100%
 * Lines: 100%
 */
public class ExportImportDeviceParserTest
{
    private static final String SAS_JSON_VALUE = "sas";
    private static final String CERTIFICATE_AUTHORITY_JSON_VALUE = "certificateAuthority";
    private static final String SELF_SIGNED_JSON_VALUE = "selfSigned";

    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_002: [The parser shall look for the authentication of the serialized export import device and save it to the returned ExportImportDeviceParser instance]
    @Test
    public void fromJsonWithCASignedAuthentication()
    {
        //arrange
        String json = "{\n" +
                "  \"id\": \"test\",\n" +
                "  \"eTag\": \"MA==\",\n" +
                "  \"status\": \"enabled\",\n" +
                "  \"authentication\": {\n" +
                "    \"symmetricKey\": {\n" +
                "      \"primaryKey\": null,\n" +
                "      \"secondaryKey\": null\n" +
                "    },\n" +
                "    \"x509Thumbprint\": {\n" +
                "      \"primaryThumbprint\":null,\n" +
                "      \"secondaryThumbprint\": null\n" +
                "    },\n" +
                "    \"type\": \"" + CERTIFICATE_AUTHORITY_JSON_VALUE + "\"\n" +
                "  },\n" +
                "  \"twinETag\": \"AAAAAAAAAAE=\",\n" +
                "  \"tags\": {},\n" +
                "  \"properties\": {\n" +
                "    \"desired\": {\n" +
                "      \"$metadata\": {\n" +
                "        \"$lastUpdated\": \"0001-01-01T00:00:00Z\"\n" +
                "      },\n" +
                "      \"$version\": 1\n" +
                "    },\n" +
                "    \"reported\": {\n" +
                "      \"$metadata\": {\n" +
                "        \"$lastUpdated\": \"2017-07-26T21:44:15.80668Z\"\n" +
                "      },\n" +
                "      \"$version\": 1\n" +
                "    }\n" +
                "  }\n" +
                "}";

        //act
        ExportImportDeviceParser parser = new ExportImportDeviceParser(json);

        //assert
        assertNull(parser.getAuthentication().getThumbprint().getPrimaryThumbprint());
        assertNull(parser.getAuthentication().getThumbprint().getSecondaryThumbprint());
        assertNull(parser.getAuthentication().getSymmetricKey().getPrimaryKey());
        assertNull(parser.getAuthentication().getSymmetricKey().getSecondaryKey());
        assertEquals(AuthenticationTypeParser.CERTIFICATE_AUTHORITY, parser.getAuthentication().getType());
    }

    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_002: [The parser shall look for the authentication of the serialized export import device and save it to the returned ExportImportDeviceParser instance]
    @Test
    public void fromJsonWithSelfSignedAuthentication()
    {
        //arrange
        String expectedPrimaryThumbprint = "0000000000000000000000000000000000000000";
        String expectedSecondaryThumbprint = "1111111111111111111111111111111111111111";
        String json = "{\n" +
                "  \"id\": \"test\",\n" +
                "  \"eTag\": \"MA==\",\n" +
                "  \"status\": \"enabled\",\n" +
                "  \"authentication\": {\n" +
                "    \"symmetricKey\": {\n" +
                "      \"primaryKey\": null,\n" +
                "      \"secondaryKey\": null\n" +
                "    },\n" +
                "    \"x509Thumbprint\": {\n" +
                "      \"primaryThumbprint\":\"" + expectedPrimaryThumbprint + "\",\n" +
                "      \"secondaryThumbprint\":\"" + expectedSecondaryThumbprint + "\"\n" +
                "    },\n" +
                "    \"type\": \"" + SELF_SIGNED_JSON_VALUE + "\"\n" +
                "  },\n" +
                "  \"twinETag\": \"AAAAAAAAAAE=\",\n" +
                "  \"tags\": {},\n" +
                "  \"properties\": {\n" +
                "    \"desired\": {\n" +
                "      \"$metadata\": {\n" +
                "        \"$lastUpdated\": \"0001-01-01T00:00:00Z\"\n" +
                "      },\n" +
                "      \"$version\": 1\n" +
                "    },\n" +
                "    \"reported\": {\n" +
                "      \"$metadata\": {\n" +
                "        \"$lastUpdated\": \"2017-07-26T21:44:15.80668Z\"\n" +
                "      },\n" +
                "      \"$version\": 1\n" +
                "    }\n" +
                "  }\n" +
                "}";

        //act
        ExportImportDeviceParser parser = new ExportImportDeviceParser(json);

        //assert
        assertEquals(expectedPrimaryThumbprint, parser.getAuthentication().getThumbprint().getPrimaryThumbprint());
        assertEquals(expectedSecondaryThumbprint, parser.getAuthentication().getThumbprint().getSecondaryThumbprint());
        assertEquals(AuthenticationTypeParser.SELF_SIGNED, parser.getAuthentication().getType());
    }

    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_002: [The parser shall look for the authentication of the serialized export import device and save it to the returned ExportImportDeviceParser instance]
    @Test
    public void fromJsonWithSASAuthentication()
    {
        //arrange
        String expectedPrimaryKey = "000000000000000000000000";
        String expectedSecondaryKey = "000000000000000000000000";
        String json = "{\n" +
                "  \"id\": \"test\",\n" +
                "  \"eTag\": \"MA==\",\n" +
                "  \"status\": \"enabled\",\n" +
                "  \"authentication\": {\n" +
                "    \"symmetricKey\": {\n" +
                "      \"primaryKey\": \"" + expectedPrimaryKey + "\",\n" +
                "      \"secondaryKey\": \"" + expectedSecondaryKey + "\"\n" +
                "    },\n" +
                "    \"x509Thumbprint\": {\n" +
                "      \"primaryThumbprint\": null,\n" +
                "      \"secondaryThumbprint\": null\n" +
                "    },\n" +
                "    \"type\": \"" + SAS_JSON_VALUE + "\"\n" +
                "  },\n" +
                "  \"twinETag\": \"AAAAAAAAAAE=\",\n" +
                "  \"tags\": {},\n" +
                "  \"properties\": {\n" +
                "    \"desired\": {\n" +
                "      \"$metadata\": {\n" +
                "        \"$lastUpdated\": \"0001-01-01T00:00:00Z\"\n" +
                "      },\n" +
                "      \"$version\": 1\n" +
                "    },\n" +
                "    \"reported\": {\n" +
                "      \"$metadata\": {\n" +
                "        \"$lastUpdated\": \"2017-07-26T21:44:15.80668Z\"\n" +
                "      },\n" +
                "      \"$version\": 1\n" +
                "    }\n" +
                "  }\n" +
                "}";

        //act
        ExportImportDeviceParser parser = new ExportImportDeviceParser(json);

        //assert
        assertEquals(expectedPrimaryKey, parser.getAuthentication().getSymmetricKey().getPrimaryKey());
        assertEquals(expectedSecondaryKey, parser.getAuthentication().getSymmetricKey().getSecondaryKey());
        assertEquals(AuthenticationTypeParser.SAS, parser.getAuthentication().getType());
    }
    
    @Test
    public void fromJsonWithTags()
    {
        //arrange
        String expectedPrimaryKey = "000000000000000000000000";
        String expectedSecondaryKey = "000000000000000000000000";
        String json = "{\n" +
                "  \"id\": \"test\",\n" +
                "  \"eTag\": \"MA==\",\n" +
                "  \"status\": \"enabled\",\n" +
                "  \"authentication\": {\n" +
                "    \"symmetricKey\": {\n" +
                "      \"primaryKey\": \"" + expectedPrimaryKey + "\",\n" +
                "      \"secondaryKey\": \"" + expectedSecondaryKey + "\"\n" +
                "    },\n" +
                "    \"x509Thumbprint\": {\n" +
                "      \"primaryThumbprint\": null,\n" +
                "      \"secondaryThumbprint\": null\n" +
                "    },\n" +
                "    \"type\": \"" + SAS_JSON_VALUE + "\"\n" +
                "  },\n" +
                "  \"twinETag\": \"AAAAAAAAAAE=\",\n" +
                "  \"tags\": { \"test01\" : \"firstvalue\", \"test02\" : \"secondvalue\"},\n" +
                "  \"properties\": {\n" +
                "    \"desired\": {\n" +
                "      \"$metadata\": {\n" +
                "        \"$lastUpdated\": \"0001-01-01T00:00:00Z\"\n" +
                "      },\n" +
                "      \"$version\": 1\n" +
                "    },\n" +
                "    \"reported\": {\n" +
                "      \"$metadata\": {\n" +
                "        \"$lastUpdated\": \"2017-07-26T21:44:15.80668Z\"\n" +
                "      },\n" +
                "      \"$version\": 1\n" +
                "    }\n" +
                "  }\n" +
                "}";

        //act
        ExportImportDeviceParser parser = new ExportImportDeviceParser(json);

        //assert
        assertEquals(expectedPrimaryKey, parser.getAuthentication().getSymmetricKey().getPrimaryKey());
        assertEquals(expectedSecondaryKey, parser.getAuthentication().getSymmetricKey().getSecondaryKey());
        assertEquals(AuthenticationTypeParser.SAS, parser.getAuthentication().getType());
        assertNotNull(parser.getTags());
        assertEquals("firstvalue", parser.getTags().get("test01"));
        assertEquals("secondvalue", parser.getTags().get("test02"));
    }   
    

    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_001: [The parser shall save the ExportImportDeviceParser's authentication to the returned json representation]
    @Test
    public void toJsonForCASignedDevice()
    {
        // arrange
        ExportImportDeviceParser parser = new ExportImportDeviceParser();
        parser.setAuthentication(new AuthenticationParser());
        parser.getAuthentication().setType(AuthenticationTypeParser.CERTIFICATE_AUTHORITY);

        String certificateAuthoritySignedDeviceAuthenticationJson = "\"type\":\"" + CERTIFICATE_AUTHORITY_JSON_VALUE + "\"";

        // act
        String serializedDevice = parser.toJson();

        // assert
        assertTrue(serializedDevice.contains(certificateAuthoritySignedDeviceAuthenticationJson));
    }

    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_001: [The parser shall save the ExportImportDeviceParser's authentication to the returned json representation]
    @Test
    public void toJsonForSelfSignedDevice()
    {
        // arrange
        ExportImportDeviceParser parser = new ExportImportDeviceParser();
        parser.setAuthentication(new AuthenticationParser());
        parser.getAuthentication().setType(AuthenticationTypeParser.SELF_SIGNED);
        parser.getAuthentication().setThumbprint(new X509ThumbprintParser("", ""));

        String selfSignedDeviceAuthenticationJson = "\"type\":\"" + SELF_SIGNED_JSON_VALUE + "\"";

        // act
        String serializedDevice = parser.toJson();

        // assert
        assertTrue(serializedDevice.contains(selfSignedDeviceAuthenticationJson));
    }

    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_001: [The parser shall save the ExportImportDeviceParser's authentication to the returned json representation]
    @Test
    public void toJsonForSymmetricKeySecuredDevice() throws NoSuchAlgorithmException
    {
        // arrange
        ExportImportDeviceParser parser = new ExportImportDeviceParser();
        parser.setAuthentication(new AuthenticationParser());
        parser.getAuthentication().setType(AuthenticationTypeParser.SAS);
        parser.getAuthentication().setSymmetricKey(new SymmetricKeyParser("", ""));

        String expectedJson = "{\"authentication\":{\"symmetricKey\":{\"primaryKey\":\"\",\"secondaryKey\":\"\"},\"type\":\"" + SAS_JSON_VALUE + "\"}}";

        // act
        String serializedDevice = parser.toJson();

        // assert
        assertEquals(expectedJson, serializedDevice);
    }
    
    @Test
    public void toJsonForTaggedDevice() throws NoSuchAlgorithmException
    {
        // arrange
        ExportImportDeviceParser parser = new ExportImportDeviceParser();
        parser.setAuthentication(new AuthenticationParser());
        parser.getAuthentication().setType(AuthenticationTypeParser.SAS);
        parser.getAuthentication().setSymmetricKey(new SymmetricKeyParser("", ""));
        TwinCollection tags = new TwinCollection();
        tags.put("test01", "firstvalue"); tags.put("test02", "secondvalue");
        parser.setTags(tags);

        //String expectedJson = "{\"authentication\":{\"symmetricKey\":{\"primaryKey\":\"\",\"secondaryKey\":\"\"},\"type\":\"" + SAS_JSON_VALUE + "\"},\"tags\":{\"test01\":\"firstvalue\",\"test02\":\"secondvalue\"}}";

        // act
        String serializedDevice = parser.toJson();

        // assert
        //assertEquals(expectedJson, serializedDevice);
        assertTrue(serializedDevice.contains("\"tags\":{"));
    }    

    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_023: [This method shall set the value of this object's AuthenticationParser equal to the provided value.]
    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_012: [This method shall return the value of this object's AuthenticationParser.]
    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_022: [This method shall set the value of this object's Id equal to the provided value.]
    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_013: [This method shall return the value of this object's Id.]
    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_014: [This method shall set the value of this object's eTag equal to the provided value.]
    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_015: [This method shall return the value of this object's eTag.]
    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_016: [This method shall set the value of this object's importMode equal to the provided value.]
    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_017: [This method shall return the value of this object's importMode.]
    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_018: [This method shall set the value of this object's status equal to the provided value.]
    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_019: [This method shall return the value of this object's status.]
    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_020: [This method shall set the value of this object's statusReason equal to the provided value.]
    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_021: [This method shall return the value of this object's statusReason.]
    @Test
    public void testGettersAndSetters()
    {
        //arrange
        String deviceId = "someDevice";
        String eTag = "etag";
        String status = "Enabled";
        String statusReason = "no reason";
        String importMode = "import";
        ExportImportDeviceParser parser = new ExportImportDeviceParser();

        //act
        parser.setAuthentication(new AuthenticationParser());
        parser.setId(deviceId);
        parser.setETag(eTag);
        parser.setStatus(status);
        parser.setStatusReason(statusReason);
        parser.setImportMode(importMode);

        //assert
        assertEquals(new AuthenticationParser().getType(), parser.getAuthentication().getType());
        assertEquals(new AuthenticationParser().getSymmetricKey(), parser.getAuthentication().getSymmetricKey());
        assertEquals(new AuthenticationParser().getThumbprint(), parser.getAuthentication().getThumbprint());
        assertEquals(deviceId, parser.getId());
        assertEquals(eTag, parser.getETag());
        assertEquals(status, parser.getStatus());
        assertEquals(statusReason, parser.getStatusReason());
        assertEquals(importMode, parser.getImportMode());
    }

    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_005: [This constructor shall take the provided json and convert it into a new ExportImportDeviceParser and return it.]
    @Test
    public void testJsonConstructor()
    {
        //arrange
        String primaryKey = "123451234512345123451234";
        String secondaryKey = "123451234512345123451234";
        AuthenticationTypeParser authType = AuthenticationTypeParser.SAS;
        String expectedId = "DEVICE_ID";
        String json = "{\"id\" : \"" + expectedId + "\", \"authentication\":{\"symmetricKey\":{\"primaryKey\":\"" + primaryKey + "\",\"secondaryKey\":\"" + secondaryKey +"\"},\"type\":\"" + SAS_JSON_VALUE + "\"}}";

        //act
        ExportImportDeviceParser parser = new ExportImportDeviceParser(json);

        //assert
        assertEquals(primaryKey, parser.getAuthentication().getSymmetricKey().getPrimaryKey());
        assertEquals(secondaryKey, parser.getAuthentication().getSymmetricKey().getSecondaryKey());
        assertEquals(authType, parser.getAuthentication().getType());
        assertEquals(expectedId, parser.getId());
    }

    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_007: [If the provided id is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void idSetterCannotTakeNullArgument()
    {
        //act
        new ExportImportDeviceParser().setId(null);
    }

    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_006: [If the provided authentication is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void authenticationSetterCannotTakeNullArgument()
    {
        //act
        new ExportImportDeviceParser().setAuthentication(null);
    }

    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_009: [If the provided json is missing the Authentication field, or its value is empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForMissingAuthentication()
    {
        //arrange
        String json = "{\n" +
                "  \"id\": \"test\",\n" +
                "  \"eTag\": \"MA==\",\n" +
                "  \"status\": \"enabled\",\n" +
                "  \"twinETag\": \"AAAAAAAAAAE=\",\n" +
                "  \"tags\": {},\n" +
                "  \"properties\": {\n" +
                "    \"desired\": {\n" +
                "      \"$metadata\": {\n" +
                "        \"$lastUpdated\": \"0001-01-01T00:00:00Z\"\n" +
                "      },\n" +
                "      \"$version\": 1\n" +
                "    },\n" +
                "    \"reported\": {\n" +
                "      \"$metadata\": {\n" +
                "        \"$lastUpdated\": \"2017-07-26T21:44:15.80668Z\"\n" +
                "      },\n" +
                "      \"$version\": 1\n" +
                "    }\n" +
                "  }\n" +
                "}";

        //act
        new ExportImportDeviceParser(json);
    }


    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_008: [If the provided json is missing the Id field, or its value is empty, an IllegalArgumentException shall be thrown]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForMissingId()
    {
        //arrange
        String json = "{\n" +
                "  \"eTag\": \"MA==\",\n" +
                "  \"status\": \"enabled\",\n" +
                "  \"authentication\": {\n" +
                "    \"symmetricKey\": {\n" +
                "      \"primaryKey\": null,\n" +
                "      \"secondaryKey\": null\n" +
                "    },\n" +
                "    \"x509Thumbprint\": {\n" +
                "      \"primaryThumbprint\":null,\n" +
                "      \"secondaryThumbprint\":null\n" +
                "    },\n" +
                "    \"type\": \"" + CERTIFICATE_AUTHORITY_JSON_VALUE + "\"\n" +
                "  },\n" +
                "  \"twinETag\": \"AAAAAAAAAAE=\",\n" +
                "  \"tags\": {},\n" +
                "  \"properties\": {\n" +
                "    \"desired\": {\n" +
                "      \"$metadata\": {\n" +
                "        \"$lastUpdated\": \"0001-01-01T00:00:00Z\"\n" +
                "      },\n" +
                "      \"$version\": 1\n" +
                "    },\n" +
                "    \"reported\": {\n" +
                "      \"$metadata\": {\n" +
                "        \"$lastUpdated\": \"2017-07-26T21:44:15.80668Z\"\n" +
                "      },\n" +
                "      \"$version\": 1\n" +
                "    }\n" +
                "  }\n" +
                "}";

        //act
        new ExportImportDeviceParser(json);
    }

    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_011: [If the provided json is null, empty, or cannot be parsed into an ExportImportDeviceParser object, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForInvalidJson()
    {
        //arrange
        String json = "}";

        //act
        new ExportImportDeviceParser(json);
    }

    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_011: [If the provided json is null, empty, or cannot be parsed into an ExportImportDeviceParser object, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForEmptyJson()
    {
        //arrange
        String json = "";

        //act
        new ExportImportDeviceParser(json);
    }

    //Tests_SRS_EXPORTIMPORTDEVICE_PARSER_34_011: [If the provided json is null, empty, or cannot be parsed into an ExportImportDeviceParser object, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullJson()
    {
        //arrange
        String json = null;

        //act
        new ExportImportDeviceParser(json);
    }
}
