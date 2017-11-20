/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.dps.device.internal.parser;

import com.google.gson.JsonParseException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.ResponseParser;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ResponseParserTest
{
    @Test
    public void constructorWithoutRegistrationStatusSucceed() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"RIoT_COMMON_device.e55fdd28-aac7-4b1c-8aa4-4206d55735f3\"," +
                "\"status\":\"assigning\"}";

        ResponseParser operationsResponseParser = ResponseParser.createFromJson(json);

        assertNotNull(operationsResponseParser.getOperationId());
        assertNotNull(operationsResponseParser.getStatus());
        assertNull(operationsResponseParser.getRegistrationStatus());
    }

    @Test
    public void constructorWithNoHSMJsonSucceed() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"RIoT_COMMON_device.e55fdd28-aac7-4b1c-8aa4-4206d55735f3\"," +
                "\"status\":\"assigning\"," +
                "\"registrationStatus\":" +
                    "{\"registrationId\":\"RIoT_COMMON_device\"," +
                    "\"status\":\"assigning\"}" +
                "}";
        ResponseParser operationsResponseParser = ResponseParser.createFromJson(json);

        assertNotNull(operationsResponseParser.getOperationId());
        assertNotNull(operationsResponseParser.getRegistrationStatus());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getRegistrationId());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getStatus());
        assertNotNull(operationsResponseParser.getStatus());
        assertNull(operationsResponseParser.getRegistrationStatus().getTpm());
        assertNull(operationsResponseParser.getRegistrationStatus().getX509());
    }

    @Test
    public void constructorWithX509JsonSucceed() throws IllegalArgumentException, JsonParseException
    {
        final String json = "{\"operationId\":\"RIoT_COMMON_device.e55fdd28-aac7-4b1c-8aa4-4206d55735f3\"," +
                "\"status\":\"assigned\"," +
                "\"registrationStatus\":" +
                    "{\"x509\":" +
                        "{\"certificateInfo\":" +
                            "{\"subjectName\":\"CN=RIoT_COMMON_device, C=US, O=MSR_TEST\"," +
                            "\"sha1Thumbprint\":\"1F67F7C5F4E86103E8D3E27BF96BD901AFF35D03\"," +
                            "\"sha256Thumbprint\":\"2CECA2C307FB65CD58C54A598018B8267DA3588C7613612A5C387450000313A9\"," +
                            "\"issuerName\":\"CN=RIoT_Signer_Core, C=US, O=MSR_TEST\"," +
                            "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                            "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                            "\"serialNumber\":\"0A0B0C0D0E\"," +
                            "\"version\":3}," +
                        "\"enrollmentGroupId\":\"DPS_REGISTRATION_GROUP\"," +
                        "\"signingCertificateInfo\":" +
                            "{\"subjectName\":\"CN=RIoT_Signer_Core, C=US, O=MSR_TEST\"," +
                            "\"sha1Thumbprint\":\"3B07D9203B9F68BF532E520B46A897A3F9AAADA9\"," +
                            "\"sha256Thumbprint\":\"99D988984AC326D4D62B5752AB4CC4BAD64387CEDAF4351FCFFAB10A69198B98\"," +
                            "\"issuerName\":\"CN=RIoT_Signer_Core, C=US, O=MSR_TEST\"," +
                            "\"notBeforeUtc\":\"2017-01-01T00:00:00Z\"," +
                            "\"notAfterUtc\":\"2037-01-01T00:00:00Z\"," +
                            "\"serialNumber\":\"0E0D0C0B0A\",\"version\":3}" +
                        "}," +
                    "\"registrationId\":\"RIoT_COMMON_device\"," +
                    "\"registrationDateTimeUtc\":\"2017-07-21T20:56:19.3109747Z\"," +
                    "\"assignedHub\":\"iot-dps-ci-eus-2.df.azure-devices-int.net\"," +
                    "\"deviceId\":\"RIoT_COMMON_device\"," +
                    "\"status\":\"assigned\"," +
                    "\"generationId\":\"172d963d-ec26-41ba-84c4-afe5f6c93ef4\"," +
                    "\"lastUpdatedDateTimeUtc\":\"2017-07-21T20:56:19.7978138Z\"}}\n";

        ResponseParser operationsResponseParser = ResponseParser.createFromJson(json);

        assertNotNull(operationsResponseParser.getOperationId());
        assertNotNull(operationsResponseParser.getRegistrationStatus());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getRegistrationId());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getStatus());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getX509());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getX509().getCertificateInfo());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getX509().getCertificateInfo().getSubjectName());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getX509().getCertificateInfo().getSha1Thumbprint());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getX509().getCertificateInfo().getSha256Thumbprint());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getX509().getCertificateInfo().getIssuerName());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getX509().getCertificateInfo().getNotBeforeUtc());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getX509().getCertificateInfo().getNotAfterUtc());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getX509().getCertificateInfo().getSerialNumber());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getX509().getEnrollmentGroupId());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getX509().getSigningCertificateInfo());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getX509().getSigningCertificateInfo().getSubjectName());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getX509().getSigningCertificateInfo().getSha1Thumbprint());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getX509().getSigningCertificateInfo().getSha256Thumbprint());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getX509().getSigningCertificateInfo().getIssuerName());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getX509().getSigningCertificateInfo().getNotBeforeUtc());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getX509().getSigningCertificateInfo().getNotAfterUtc());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getX509().getSigningCertificateInfo().getSerialNumber());
        assertNotNull(operationsResponseParser.getStatus());
    }

    @Test
    public void constructorWithTPMJsonSucceed() throws IllegalArgumentException, JsonParseException
    {
        final String json = "" +
                "{" +
                    "\"operationId\":\"qToVs4wjJWaTfm7CP5WptArEKqNk7LgQCH4s9Ez-P+8=.dd0ac813-ee50-4fa2-8bd9-3b4aff603a20\"," +
                    "\"status\":\"assigned\"," +
                    "\"registrationStatus\":" +
                    "{" +
                        "\"tpm\":{" +
                        "\"authenticationKey\":\"ADQAIBza8eEgzPYtx4v7H6zsqpGjHrNZOQP9pJWUEjf7vVTDcy6ufm1ybwtixoqcdBcWVsXRAQCs3K91JNqMDf9AtN6w92+vyyfYOeSsd+z3f/8/qar5ErdjDHql+tXL/Qi2jWKi6INdBv2HvX1JpGXPx2qSFoRnph6iZ8+EMS09ZNqsMBWvPzb3+ayZS0C6GdCuMwgzmEbdBDMCEPJwg37KJCnI6jOTFGDbnPDuHSDyVKcIIBcdigztgUg2BEflppCuUyiuJidh3Xj/w+zxnYx4G8Dx5fRUBMCeQ1XPInqSfUiltkPzF9H6j9FQRYLuZcxOlMR7P2OI4ouZAgNsA15Ibpov2HjirlKeG3AW3Gn5oPZ2f0tC7JORjfuQPtAibvQSzEp+CFLGp2KgZx7L+HGXyi2jzvSvAI4AIAjG001xRmE9EUrbee4GbiCz/AxpRTswVTJ/GaJX4TA/9Hg95BkAjyHWtrPPATRQ+712diRXRRjEl/U9HSoxrCgLbMkEH2nYoL26f8g/m/vE3i7FSZGgss2qeQQuEnH7zJRhr6kw4U7vwHQgUNP9uZAOc8iZyxnURlXtUTQRGS/cpGz1mUGBhn9f+WvFAQCxAJYZ06lBd+2/WEQDRpYoQ/X3yl6SRTc4tyJkPqtddqCVsEFhfiKmEvv4zhfVvdjLf3DRgRhN8INIWFIYBQxeP6ACFvp/uNTJ1fpVWDh/2gAlGpq8wgzcnJXT+6h1/9QaOZTMsMbxovBt1szhHXRSD+DBtHLnkoI6UtXVF/NI0VVayeXz9S64T7UvSKBUteeOBEPxHYxGWHvGvDJWRh7F/CyQ+iTrDhZWtG2p26XC97h/SfYPzgUCwV3jdYBE1m8HX3zZmv4zW0OpEqk86HGj7Qs5+1s5ieGze7uh33OJmgfUQYFzamyQg1Fmsyh8AloE2UJJp1XcX+xUhTwQyaxjADAACAALAAQEQAAAAAUACwAg0Eljuu4pLFi1zEjQuK8JExRpLaXOrUSjJ5MQN39ZT18AbEK+gzmSrhTIjwk0hQcOEjcjDW1QLJN1HZyKKJiPk2Weo9pcwBXR1oCRZgdHrXxXkd90dku1fgW+1qEoisHBpitNIn9zkRQy+zuzx982tZjE614LX/dlFAmSxCRtInngy5i7Y5R1PCcKzLo6Iw==\"" +
                        "}," +
                        "\"registrationId\":\"qToVs4wjJWaTfm7CP5WptArEKqNk7LgQCH4s9Ez-P+8=\"," +
                        "\"registrationDateTimeUtc\":\"2017-07-19T23:49:34.2703441Z\"," +
                        "\"assignedHub\":\"iot-dps-ci-eus-2.df.azure-devices-int.net\"," +
                        "\"deviceId\":\"0000toercola\"," +
                        "\"status\":\"assigned\"," +
                        "\"generationId\":\"0fc5c614-bbea-4808-91d5-1e9435277f01\"," +
                        "\"lastUpdatedDateTimeUtc\":\"2017-07-19T23:49:34.6624739Z\"" +
                    "}" +
                "}";

        ResponseParser operationsResponseParser = ResponseParser.createFromJson(json);

        assertNotNull(operationsResponseParser.getOperationId());
        assertNotNull(operationsResponseParser.getRegistrationStatus());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getRegistrationId());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getRegistrationDateTimeUtc());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getAssignedHub());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getDeviceId());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getStatus());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getGenerationId());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getLastUpdatesDateTimeUtc());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getTpm());
        assertNotNull(operationsResponseParser.getRegistrationStatus().getTpm().getAuthenticationKey());
        assertNotNull(operationsResponseParser.getStatus());
    }
}
