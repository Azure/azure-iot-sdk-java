/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.device.internal.contract;

import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.UrlPathBuilder;
import mockit.Deencapsulation;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/*
 * Unit tests for UrlPathBuilder
 * Coverage : 100% method, 91% line
 */

public class UrlPathBuilderTest
{
    private static final String TEST_SCOPE = "testScope";
    private static final String TEST_HOST_NAME = "testHostName";
    private static final String TEST_REGISTRATION_ID = "testRegistrationId";
    private static final String TEST_OPERATION_ID = "testOperationId";
    private static final String SERVICE_API_VERSION = "2019-03-31";

    //SRS_UrlPathBuilder_25_001: [ Constructor shall save scope id.]
    @Test
    public void constructorWithScopeOnlySucceeds() throws IllegalArgumentException
    {
        //act
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder(TEST_SCOPE);

        //assert
        assertEquals(TEST_SCOPE, Deencapsulation.getField(urlPathBuilder, "scope"));
    }

    //SRS_UrlPathBuilder_25_002: [ Constructor throw IllegalArgumentException if scope id is null or empty.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithScopeOnlyThrowsOnNullScope() throws IllegalArgumentException
    {
        //act
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder(null);

    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithScopeOnlyThrowsOnEmptyScope() throws IllegalArgumentException
    {
        //act
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder("");
        //assert
    }

    //SRS_UrlPathBuilder_25_004: [ The constructor shall save the scope id or hostName string and protocol. ]
    @Test
    public void constructorSucceeds() throws IllegalArgumentException
    {
        //act
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE, ProvisioningDeviceClientTransportProtocol.HTTPS);
        //assert
        assertEquals(TEST_SCOPE, Deencapsulation.getField(urlPathBuilder, "scope"));
        assertEquals(ProvisioningDeviceClientTransportProtocol.HTTPS, Deencapsulation.getField(urlPathBuilder, "provisioningDeviceClientTransportProtocol"));
    }

    //SRS_UrlPathBuilder_25_003: [ The constructor shall throw IllegalArgumentException if the scope id or hostName string is empty or null or if protocol is null.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorWithNullHostNameThrows() throws IllegalArgumentException
    {
        //arrange

        //act
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder(null, TEST_SCOPE, ProvisioningDeviceClientTransportProtocol.HTTPS);
        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithEmptyHostNameThrows() throws IllegalArgumentException
    {
        //arrange

        //act
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder("", TEST_SCOPE, ProvisioningDeviceClientTransportProtocol.HTTPS);
        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithNullScopeThrows() throws IllegalArgumentException
    {
        //arrange

        //act
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder(TEST_HOST_NAME, null, ProvisioningDeviceClientTransportProtocol.HTTPS);
        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithEmptyScopeThrows() throws IllegalArgumentException
    {
        //arrange

        //act
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder(TEST_HOST_NAME, "", ProvisioningDeviceClientTransportProtocol.HTTPS);
        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithNullProtocolThrows() throws IllegalArgumentException
    {
        //arrange

        //act
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE, null);
        //assert
    }

    //SRS_UrlPathBuilder_25_008: [ This method shall create a String using the following format: HTTP - https://<HostName>/<Scope>/registrations/<Registration ID>/register?api-version=<Service API Version> MQTT - TBD AMQP - TBD ]
    @Test
    public void generateRegisterUrlHttpSucceeds() throws IOException
    {
        //arrange
        //https://testHostName/testScope/registrations/testRegistrationId/register?api-version=2017-08-31-preview
        final String expectedUrl = "https://"+ TEST_HOST_NAME + "/" + TEST_SCOPE + "/registrations/" + TEST_REGISTRATION_ID + "/register?api-version=" + SERVICE_API_VERSION;
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE, ProvisioningDeviceClientTransportProtocol.HTTPS);
        //act
        String generateRequestUrl = urlPathBuilder.generateRegisterUrl(TEST_REGISTRATION_ID);
        //assert
        assertEquals(expectedUrl, generateRequestUrl);
    }

    //SRS_UrlPathBuilder_25_007: [ This method shall throw IllegalArgumentException if the registration id is null or empty. ]
    @Test (expected = IllegalArgumentException.class)
    public void generateRegisterUrlHttpThrowsOnNullRegID() throws IOException
    {
        //arrange
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE, ProvisioningDeviceClientTransportProtocol.HTTPS);
        //act
        urlPathBuilder.generateRegisterUrl(null);

    }

    @Test (expected = IllegalArgumentException.class)
    public void generateRegisterUrlHttpThrowsOnEmptyRegID() throws IOException
    {
        //arrange
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE, ProvisioningDeviceClientTransportProtocol.HTTPS);
        //act
        urlPathBuilder.generateRegisterUrl("");
    }

    //SRS_UrlPathBuilder_25_010: [ This method shall create a String using the following format: HTTP - https://<HostName>/<Scope>/registrations/<Registration ID>/operations/<operationId>?api-version=<Service API Version> MQTT - TBD AMQP - TBD ]
    @Test
    public void generateRequestUrlHttpSucceeds() throws IOException
    {
        //arrange
        //https://testHostName/testScope/registrations/testRegistrationId/operations/testOperationId?api-version=2017-08-31-preview
        final String expectedUrl = "https://"+ TEST_HOST_NAME + "/" + TEST_SCOPE + "/registrations/" + TEST_REGISTRATION_ID + "/operations/" + TEST_OPERATION_ID + "?api-version=" + SERVICE_API_VERSION;

        UrlPathBuilder urlPathBuilder = new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE, ProvisioningDeviceClientTransportProtocol.HTTPS);
        //act
        String generateRequestUrl = urlPathBuilder.generateRequestUrl(TEST_REGISTRATION_ID, TEST_OPERATION_ID);
        //assert
        assertEquals(expectedUrl, generateRequestUrl);
    }

    //SRS_UrlPathBuilder_25_009: [ This method shall throw IllegalArgumentException if the registration id or operation id is null or empty. ]
    @Test (expected = IllegalArgumentException.class)
    public void generateRequestUrlHttpThrowsOnNullRegID() throws IOException
    {
        //arrange
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE, ProvisioningDeviceClientTransportProtocol.HTTPS);
        //act
        urlPathBuilder.generateRequestUrl(null, TEST_OPERATION_ID);
        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void generateRequestUrlHttpThrowsOnEmptyRegID() throws IOException
    {
        //arrange
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE, ProvisioningDeviceClientTransportProtocol.HTTPS);
        //act
        urlPathBuilder.generateRequestUrl("", TEST_OPERATION_ID);

    }

    @Test (expected = IllegalArgumentException.class)
    public void generateRequestUrlHttpThrowsOnNullOpID() throws IOException
    {
        //arrange
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE, ProvisioningDeviceClientTransportProtocol.HTTPS);
        //act
        urlPathBuilder.generateRequestUrl(TEST_REGISTRATION_ID, null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void generateRequestUrlHttpThrowsOnEmptyOpID() throws IOException
    {
        //arrange
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE, ProvisioningDeviceClientTransportProtocol.HTTPS);
        //act
        urlPathBuilder.generateRequestUrl(TEST_REGISTRATION_ID, "");
    }

    //SRS_UrlPathBuilder_25_006: [ This method shall create a String using the following format after Url Encoding: <scopeid>/registrations/<registrationId> ]
    @Test
    public void generateSasTokenUrlSucceeds() throws IOException
    {
        //arrange
        //testScope%2Fregistrations%2FtestRegistrationId
        final String expectedSasTokenUrl = TEST_SCOPE + "%2Fregistrations%2F" + TEST_REGISTRATION_ID;
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE, ProvisioningDeviceClientTransportProtocol.HTTPS);
        //act
        String sastokenUrl = urlPathBuilder.generateSasTokenUrl(TEST_REGISTRATION_ID);
        //assert
        assertEquals(expectedSasTokenUrl, sastokenUrl);
    }

    //SRS_UrlPathBuilder_25_005: [ This method shall throw IllegalArgumentException if the registration id is null or empty. ]
    @Test (expected = IllegalArgumentException.class)
    public void generateSasTokenUrlThrowsOnNullRegID() throws IOException
    {
        //arrange
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE, ProvisioningDeviceClientTransportProtocol.HTTPS);
        //act
        urlPathBuilder.generateSasTokenUrl(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void generateSasTokenUrlThrowsOnEmptyRegID() throws IOException
    {
        //arrange
        UrlPathBuilder urlPathBuilder = new UrlPathBuilder(TEST_HOST_NAME, TEST_SCOPE, ProvisioningDeviceClientTransportProtocol.HTTPS);
        //act
        urlPathBuilder.generateSasTokenUrl("");
    }
}
