/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.microsoft.azure.sdk.iot.deps.serializer.*;
import mockit.Deencapsulation;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Code coverage:
 * 100% Methods
 * 100% lines
 */
@RunWith(JMockit.class)
public class DeviceParserTest
{
    private static Date validDate;
    private static String validDateString;
    private static final String SIMPLEDATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private static final String SAS_JSON_VALUE = "sas";
    private static final String CERTIFICATE_AUTHORITY_JSON_VALUE = "certificateAuthority";
    private static final String SELF_SIGNED_JSON_VALUE = "selfSigned";

    private static final String expectedPrimaryThumbprint = "0000000000000000000000000000000000000000";
    private static final String expectedSecondaryThumbprint = "1111111111111111111111111111111111111111";

    @Before
    public void setUp() throws ParseException
    {
        validDate = new Date();
        validDateString =  new SimpleDateFormat(SIMPLEDATEFORMAT).format(validDate);
    }

    //Tests_SRS_DEVICE_PARSER_34_002: [This constructor shall create a DeviceParser object based off of the provided json.]
    @Test
    public void fromJsonWithSelfSignedAuthentication()
    {
        //arrange
        String json = "{\n" +
                "  \"encryptionMethod\": \"AES\",\n" +
                "  \"deviceId\": \"deviceId1234\",\n" +
                "  \"generationId\": \"\",\n" +
                "  \"etag\": \"\",\n" +
                "  \"status\": \"enabled\",\n" +
                "  \"statusReason\": \"\",\n" +
                "  \"statusUpdatedTime\": \"" + validDateString + "\",\n" +
                "  \"connectionState\": \"Disconnected\",\n" +
                "  \"connectionStateUpdatedTime\": \"" + validDateString + "\",\n" +
                "  \"lastActivityTime\": \"" + validDateString + "\",\n" +
                "  \"cloudToDeviceMessageCount\": 0,\n" +
                "  \"managedBy\": \"someentity\",\n" +
                "  \"capabilities\": {\n" +
                "    \"iotEdge\": true },\n" +
                "  \"authentication\": {\n" +
                "    \"type\": \"" + SELF_SIGNED_JSON_VALUE + "\",\n" +
                "    \"x509Thumbprint\": {\n" +
                "      \"primaryThumbprint\": \"" + expectedPrimaryThumbprint + "\",\n" +
                "      \"secondaryThumbprint\": \"" + expectedSecondaryThumbprint + "\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        //act
        DeviceParser parser = new DeviceParser(json);

        //assert
        assertEquals(expectedPrimaryThumbprint, parser.getAuthenticationParser().getThumbprint().getPrimaryThumbprintFinal());
        assertEquals(expectedSecondaryThumbprint, parser.getAuthenticationParser().getThumbprint().getSecondaryThumbprintFinal());
        assertNull(parser.getAuthenticationParser().getSymmetricKey());
        assertEquals(AuthenticationTypeParser.SELF_SIGNED, parser.getAuthenticationParser().getType());
    }

    //Tests_SRS_DEVICE_PARSER_34_002: [This constructor shall create a DeviceParser object based off of the provided json.]
    @Test
    public void fromJsonWithSASAuthentication()
    {
        //arrange
        String expectedPrimaryKey = "000000000000000000000000";
        String expectedSecondaryKey = "000000000000000000000000";

        String json = "{\n" +
                "  \"encryptionMethod\": \"AES\",\n" +
                "  \"deviceId\": \"deviceId1234\",\n" +
                "  \"generationId\": \"\",\n" +
                "  \"etag\": \"\",\n" +
                "  \"status\": \"enabled\",\n" +
                "  \"statusReason\": \"\",\n" +
                "  \"statusUpdatedTime\": \"" + validDateString + "\",\n" +
                "  \"connectionState\": \"Disconnected\",\n" +
                "  \"connectionStateUpdatedTime\": \"" + validDateString + "\",\n" +
                "  \"lastActivityTime\": \"" + validDateString + "\",\n" +
                "  \"cloudToDeviceMessageCount\": 0,\n" +
                "  \"forceUpdate\": false,\n" +
                "  \"managedBy\": \"someentity\",\n" +
                "  \"capabilities\": {\n" +
                "    \"iotEdge\": true },\n" +
                "  \"authentication\": {\n" +
                "    \"type\": \"" + SAS_JSON_VALUE + "\",\n" +
                "    \"symmetricKey\": {\n" +
                "      \"primaryKey\": \"" + expectedPrimaryKey + "\",\n" +
                "      \"secondaryKey\": \"" + expectedSecondaryKey + "\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        //act
        DeviceParser parser = new DeviceParser(json);

        //assert
        assertEquals(expectedPrimaryKey, parser.getAuthenticationParser().getSymmetricKey().getPrimaryKeyFinal());
        assertEquals(expectedSecondaryKey, parser.getAuthenticationParser().getSymmetricKey().getSecondaryKeyFinal());
        assertNull(parser.getAuthenticationParser().getThumbprint());
        assertEquals(AuthenticationTypeParser.SAS, parser.getAuthenticationParser().getType());
    }

    //Tests_SRS_DEVICE_PARSER_34_002: [This constructor shall create a DeviceParser object based off of the provided json.]
    @Test
    public void fromJsonWithCAAuthentication() throws ParseException
    {
        //arrange
        String deviceId = "deviceId";
        String generationId = "1234";
        String ETag = "5678";
        String status = "Enabled";
        String statusReason = "no reason";
        String statusUpdatedTime = validDateString;
        String connectionState = "Disconnected";
        String connectionStateUpdatedTime = validDateString;
        String managedBy = "someentity";
        String lastActivityTime = ParserUtility.getDateStringFromDate(new Date());
        int cloudToDeviceMessageCount = 20;
        String scope = "securityscope";

        String json = "{\n" +
                "  \"encryptionMethod\": \"AES\",\n" +
                "  \"deviceId\": \"" + deviceId + "\",\n" +
                "  \"generationId\": \"" + generationId + "\",\n" +
                "  \"etag\": \"" + ETag + "\",\n" +
                "  \"status\": \"" + status + "\",\n" +
                "  \"statusReason\": \"" + statusReason + "\",\n" +
                "  \"statusUpdatedTime\": \"" + statusUpdatedTime + "\",\n" +
                "  \"connectionState\": \"" + connectionState + "\",\n" +
                "  \"connectionStateUpdatedTime\": \"" + connectionStateUpdatedTime + "\",\n" +
                "  \"lastActivityTime\": \"" + lastActivityTime + "\",\n" +
                "  \"cloudToDeviceMessageCount\": \"" + cloudToDeviceMessageCount + "\",\n" +
                "  \"forceUpdate\": \"false\",\n" +
                "  \"managedBy\": \"" + managedBy + "\",\n" +
                "  \"capabilities\": {\n" +
                "   \"iotEdge\": true },\n" +
                "  \"deviceScope\": \"" + scope + "\",\n" +
                "  \"authentication\": {\n" +
                "    \"type\": \"" + CERTIFICATE_AUTHORITY_JSON_VALUE + "\"\n" +
                "  }\n" +
                "}";

        //act
        DeviceParser parser = new DeviceParser(json);

        //assert
        assertNull(parser.getAuthenticationParser().getThumbprint());
        assertNull(parser.getAuthenticationParser().getSymmetricKey());
        assertEquals(AuthenticationTypeParser.CERTIFICATE_AUTHORITY, parser.getAuthenticationParser().getType());
        assertEquals(deviceId, parser.getDeviceId());
        assertEquals(generationId, parser.getGenerationId());
        assertEquals("\"" + ETag + "\"", parser.geteTag());
        assertEquals(status, parser.getStatus());
        assertEquals(statusReason, parser.getStatusReason());
        assertEquals(connectionState, parser.getConnectionState());
        assertEquals(cloudToDeviceMessageCount, parser.getCloudToDeviceMessageCount());
        assertEquals(managedBy, parser.getManagedBy());
        assertTrue(parser.getCapabilities().getIotEdge());

        String actualStatusUpdatedTime = Deencapsulation.getField(parser, "statusUpdatedTimeString");
        String actualConnectionStateUpdatedTime = Deencapsulation.getField(parser, "connectionStateUpdatedTimeString");
        String actualLastActivityTime = Deencapsulation.getField(parser, "lastActivityTimeString");
        assertEquals(statusUpdatedTime, actualStatusUpdatedTime);
        assertEquals(connectionStateUpdatedTime, actualConnectionStateUpdatedTime);
        assertEquals(lastActivityTime, actualLastActivityTime);
        assertEquals(scope, parser.getScope());
    }

    //Tests_SRS_DEVICE_PARSER_34_001: [This method shall return a json representation of this.]
    @Test
    public void toJsonForCASignedDevice()
    {
        // arrange
        DeviceParser parser = new DeviceParser();
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.CERTIFICATE_AUTHORITY);

        String certificateAuthoritySignedDeviceAuthenticationJson = "\"type\":\"" + CERTIFICATE_AUTHORITY_JSON_VALUE + "\"";

        // act
        String serializedDevice = parser.toJson();

        // assert
        assertTrue(serializedDevice.contains(certificateAuthoritySignedDeviceAuthenticationJson));
    }

    //Tests_SRS_DEVICE_PARSER_34_001: [This method shall return a json representation of this.]
    @Test
    public void toJsonForSelfSignedDevice()
    {
        // arrange
        DeviceParser parser = new DeviceParser();
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.SELF_SIGNED);
        String sampleThumbprint = "0000000000000000000000000000000000000000";
        parser.getAuthenticationParser().setThumbprint(new X509ThumbprintParser(sampleThumbprint, sampleThumbprint));

        String selfSignedDeviceAuthenticationJson = "\"type\":\"" + SELF_SIGNED_JSON_VALUE + "\"";

        // act
        String serializedDevice = parser.toJson();

        // assert
        assertTrue(serializedDevice.contains(selfSignedDeviceAuthenticationJson));
    }

    //Tests_SRS_DEVICE_PARSER_34_001: [This method shall return a json representation of this.]
    @Test
    public void toJsonForSymmetricKeySecuredDevice() throws NoSuchAlgorithmException
    {
        // arrange
        DeviceParser parser = new DeviceParser();
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.getAuthenticationParser().setType(AuthenticationTypeParser.SAS);
        String sampleKey = "000000000000000000000000";
        parser.getAuthenticationParser().setSymmetricKey(new SymmetricKeyParser(sampleKey, sampleKey));

        String symmetricKeySecuredDeviceAuthenticationJson = "\"type\":\"" + SAS_JSON_VALUE + "\"";

        // act
        String serializedDevice = parser.toJson();

        // assert
        assertTrue(serializedDevice.contains(symmetricKeySecuredDeviceAuthenticationJson));
    }

    //Tests_SRS_DEVICE_PARSER_34_009: [This method shall set the value of deviceId to the provided value.]
    //Tests_SRS_DEVICE_PARSER_34_007: [This method shall set the value of authenticationParser to the provided value.]
    //Tests_SRS_DEVICE_PARSER_34_013: [This method shall set the value of this object's ETag equal to the provided value.]
    //Tests_SRS_DEVICE_PARSER_34_014: [This method shall return the value of this object's ETag.]
    //Tests_SRS_DEVICE_PARSER_34_015: [This method shall set the value of this object's Generation Id equal to the provided value.]
    //Tests_SRS_DEVICE_PARSER_34_016: [This method shall return the value of this object's Generation Id.]
    //Tests_SRS_DEVICE_PARSER_34_017: [This method shall set the value of this object's Status equal to the provided value.]
    //Tests_SRS_DEVICE_PARSER_34_018: [This method shall return the value of this object's Status.]
    //Tests_SRS_DEVICE_PARSER_34_019: [This method shall set the value of this object's Status Reason equal to the provided value.]
    //Tests_SRS_DEVICE_PARSER_34_020: [This method shall return the value of this object's Status Reason.]
    //Tests_SRS_DEVICE_PARSER_34_021: [This method shall set the value of this object's statusUpdatedTime equal to the provided value.]
    //Tests_SRS_DEVICE_PARSER_34_022: [This method shall return the value of this object's statusUpdatedTime.]
    //Tests_SRS_DEVICE_PARSER_34_023: [This method shall set the value of this object's connectionState equal to the provided value.]
    //Tests_SRS_DEVICE_PARSER_34_024: [This method shall return the value of this object's connectionState.]
    //Tests_SRS_DEVICE_PARSER_34_025: [This method shall set the value of this object's connectionStateUpdatedTime equal to the provided value.]
    //Tests_SRS_DEVICE_PARSER_34_026: [This method shall return the value of this object's connectionStateUpdatedTime.]
    //Tests_SRS_DEVICE_PARSER_34_027: [This method shall set the value of this object's lastActivityTime equal to the provided value.]
    //Tests_SRS_DEVICE_PARSER_34_028: [This method shall return the value of this object's lastActivityTime.]
    //Tests_SRS_DEVICE_PARSER_34_029: [This method shall set the value of this object's cloudToDeviceMessageCount equal to the provided value.]
    //Tests_SRS_DEVICE_PARSER_34_030: [This method shall return the value of this object's cloudToDeviceMessageCount.]
    //Tests_SRS_DEVICE_PARSER_34_031: [This method shall return the value of this object's AuthenticationParser.]
    //Tests_SRS_DEVICE_PARSER_34_032: [This method shall return the value of this object's DeviceId.]
    @Test
    public void testGettersAndSetters()
    {
        //arrange
        String connectionState = "connectionState";
        DeviceParser parser = new DeviceParser();
        String deviceId = "someDevice";
        String eTag = "etag";
        Date connectionStateUpdatedTime = new Date();
        Date lastActivityTime = new Date();
        Date statusUpdatedTime = new Date();
        String generationId = "1234";
        String status = "Enabled";
        String statusReason = "no reason";
        long cloudToDeviceMessageCount = 2;
        String managedBy = "someentity";
        String scope = "securityscope";

        //act
        parser.setAuthenticationParser(new AuthenticationParser());
        parser.setConnectionState(connectionState);
        parser.setDeviceId(deviceId);
        parser.seteTag(eTag);
        parser.setConnectionStateUpdatedTime(connectionStateUpdatedTime);
        parser.setLastActivityTime(lastActivityTime);
        parser.setGenerationId(generationId);
        parser.setStatus(status);
        parser.setStatusReason(statusReason);
        parser.setCloudToDeviceMessageCount(cloudToDeviceMessageCount);
        parser.setStatusUpdatedTime(statusUpdatedTime);
        parser.setManagedBy(managedBy);
        parser.setCapabilities(new DeviceCapabilitiesParser());
        parser.setScope(scope);

        //assert
        assertEquals(new AuthenticationParser().getType(), parser.getAuthenticationParser().getType());
        assertEquals(new AuthenticationParser().getSymmetricKey(), parser.getAuthenticationParser().getSymmetricKey());
        assertEquals(new AuthenticationParser().getThumbprint(), parser.getAuthenticationParser().getThumbprint());
        assertEquals(connectionState, parser.getConnectionState());
        assertEquals(connectionStateUpdatedTime, parser.getConnectionStateUpdatedTime());
        assertEquals(deviceId, parser.getDeviceId());
        assertEquals("\"" + eTag + "\"", parser.geteTag());
        assertEquals(generationId, parser.getGenerationId());
        assertEquals(lastActivityTime, parser.getLastActivityTime());
        assertEquals(status, parser.getStatus());
        assertEquals(statusReason, parser.getStatusReason());
        assertEquals(cloudToDeviceMessageCount, parser.getCloudToDeviceMessageCount());
        assertEquals(statusUpdatedTime, parser.getStatusUpdatedTime());
        assertEquals(managedBy, parser.getManagedBy());
        assertEquals(new DeviceCapabilitiesParser().getIotEdge(), parser.getCapabilities().getIotEdge());
        assertEquals(scope, parser.getScope());
    }

    //Tests_SRS_DEVICE_PARSER_34_004: [For each of this parser's properties, if the setter is called with a null argument, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void cantSetAuthenticationParserNull()
    {
        //act
        new DeviceParser().setAuthenticationParser(null);
    }

    //Tests_SRS_DEVICE_PARSER_34_004: [For each of this parser's properties, if the setter is called with a null argument, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void cantSetDeviceIdNull()
    {
        //act
        new DeviceParser().setDeviceId(null);
    }

    //Tests_SRS_DEVICE_PARSER_34_005: [If the provided json is null or empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullJson()
    {
        //act
        new DeviceParser(null);
    }

    //Tests_SRS_DEVICE_PARSER_34_005: [If the provided json is null or empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForEmptyJson()
    {
        //act
        new DeviceParser("");
    }

    //Tests_SRS_DEVICE_PARSER_34_006: [If the provided json cannot be parsed into a DeviceParser object, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForInvalidJson()
    {
        //arrange
        String json = "{";

        //act
        new DeviceParser(json);
    }

    //Tests_SRS_DEVICE_PARSER_34_012: [If the provided json is missing the authentication field or its value is empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForJsonMissingAuthenticationParser()
    {
        //arrange
        String json = "{\n" +
                "  \"encryptionMethod\": \"AES\",\n" +
                "  \"deviceId\": \"deviceId1234\",\n" +
                "  \"generationId\": \"\",\n" +
                "  \"etag\": \"\",\n" +
                "  \"status\": \"enabled\",\n" +
                "  \"statusReason\": \"\",\n" +
                "  \"statusUpdatedTime\": \"" + validDateString + "\",\n" +
                "  \"connectionState\": \"Disconnected\",\n" +
                "  \"connectionStateUpdatedTime\": \"" + validDateString + "\",\n" +
                "  \"lastActivityTime\": \"" + validDateString + "\",\n" +
                "  \"cloudToDeviceMessageCount\": 0,\n" +
                "  \"forceUpdate\": false\n" +
                "  \"managedBy\": \"someentity\",\n" +
                "  \"capabilities\": {\n" +
                "    \"iotEdge\": true },\n" +
                "}";

        //act
        new DeviceParser(json);
    }

    //Tests_SRS_DEVICE_PARSER_34_011: [If the provided json is missing the DeviceId field or its value is empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForJsonMissingDeviceId()
    {
        //arrange
        String json = "{\n" +
                "  \"encryptionMethod\": \"AES\",\n" +
                "  \"generationId\": \"\",\n" +
                "  \"etag\": \"\",\n" +
                "  \"status\": \"enabled\",\n" +
                "  \"statusReason\": \"\",\n" +
                "  \"statusUpdatedTime\": \"" + validDateString + "\",\n" +
                "  \"connectionState\": \"Disconnected\",\n" +
                "  \"connectionStateUpdatedTime\": \"" + validDateString + "\",\n" +
                "  \"lastActivityTime\": \"" + validDateString + "\",\n" +
                "  \"cloudToDeviceMessageCount\": 0,\n" +
                "  \"forceUpdate\": false,\n" +
                "  \"managedBy\": \"someentity\",\n" +
                "  \"capabilities\": {\n" +
                "    \"iotEdge\": true },\n" +
                "  \"authentication\": {\n" +
                "    \"type\": \"" + SELF_SIGNED_JSON_VALUE + "\",\n" +
                "    \"x509Thumbprint\": {\n" +
                "      \"primaryThumbprint\": \"" + expectedPrimaryThumbprint + "\",\n" +
                "      \"secondaryThumbprint\": \"" + expectedSecondaryThumbprint + "\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        //act
        new DeviceParser(json);
    }

    //Tests_SRS_DEVICE_PARSER_34_011: [If the provided json is missing the DeviceId field or its value is empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForJsonEmptyDeviceId()
    {
        //arrange
        String json = "{\n" +
                "  \"encryptionMethod\": \"AES\",\n" +
                "  \"deviceId\": \"\",\n" +
                "  \"generationId\": \"\",\n" +
                "  \"etag\": \"\",\n" +
                "  \"status\": \"enabled\",\n" +
                "  \"statusReason\": \"\",\n" +
                "  \"statusUpdatedTime\": \"" + validDateString + "\",\n" +
                "  \"connectionState\": \"Disconnected\",\n" +
                "  \"connectionStateUpdatedTime\": \"" + validDateString + "\",\n" +
                "  \"lastActivityTime\": \"" + validDateString + "\",\n" +
                "  \"cloudToDeviceMessageCount\": 0,\n" +
                "  \"forceUpdate\": false,\n" +
                "  \"managedBy\": \"someentity\",\n" +
                "  \"capabilities\": {\n" +
                "    \"iotEdge\": true },\n" +
                "  \"authentication\": {\n" +
                "    \"type\": \"" + SELF_SIGNED_JSON_VALUE + "\",\n" +
                "    \"x509Thumbprint\": {\n" +
                "      \"primaryThumbprint\": \"" + expectedPrimaryThumbprint + "\",\n" +
                "      \"secondaryThumbprint\": \"" + expectedSecondaryThumbprint + "\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        //act
        new DeviceParser(json);
    }

    //Tests_SRS_DEVICE_PARSER_34_010: [If the provided deviceId value is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void setterForDeviceIdCalledWithNullThrowsIllegalArgumentException()
    {
        //arrange
        DeviceParser parser = new DeviceParser();

        //act
        parser.setDeviceId(null);
    }

    //Tests_SRS_DEVICE_PARSER_34_010: [If the provided deviceId value is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void setterForDeviceIdCalledWithEmptyStringThrowsIllegalArgumentException()
    {
        //arrange
        DeviceParser parser = new DeviceParser();

        //act
        parser.setDeviceId("");
    }

    //Tests_SRS_DEVICE_PARSER_34_008: [If the provided authenticationParser value is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void setterForAuthenticationCalledWithNullThrowsIllegalArgumentException()
    {
        //arrange
        DeviceParser parser = new DeviceParser();

        //act
        parser.setAuthenticationParser(null);
    }
}
