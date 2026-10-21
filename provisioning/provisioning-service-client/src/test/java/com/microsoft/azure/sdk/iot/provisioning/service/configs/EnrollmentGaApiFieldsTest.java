// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnrollmentGaApiFieldsTest
{
    private static final String PRIMARY_KEY = "dmFsaWRQcmltYXJ5S2V5";
    private static final String SECONDARY_KEY = "dmFsaWRTZWNvbmRhcnlLZXk=";

    @Test
    public void individualEnrollment_WithGaFields_RoundTrips()
    {
        IndividualEnrollment enrollment = new IndividualEnrollment(
            "registration-id",
            new SymmetricKeyAttestation(PRIMARY_KEY, SECONDARY_KEY));
        enrollment.setNamespaceName("namespace");
        enrollment.setCertificateAuthorityName("authority");
        enrollment.setCertificatePolicyName("policy");

        JsonObject enrollmentJson = enrollment.toJsonElement().getAsJsonObject();
        assertEquals("namespace", enrollmentJson.get("namespaceName").getAsString());
        assertEquals("authority", enrollmentJson.get("certificateAuthorityName").getAsString());
        assertEquals("policy", enrollmentJson.get("certificatePolicyName").getAsString());

        IndividualEnrollment deserializedEnrollment = new IndividualEnrollment(enrollmentJson.toString());
        assertEquals("namespace", deserializedEnrollment.getNamespaceName());
        assertEquals("authority", deserializedEnrollment.getCertificateAuthorityName());
        assertEquals("policy", deserializedEnrollment.getCertificatePolicyName());
    }

    @Test
    public void enrollmentGroup_WithGaFields_RoundTrips()
    {
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup(
            "enrollment-group-id",
            new SymmetricKeyAttestation(PRIMARY_KEY, SECONDARY_KEY));
        enrollmentGroup.setNamespaceName("namespace");
        enrollmentGroup.setCertificateAuthorityName("authority");
        enrollmentGroup.setCertificatePolicyName("policy");

        JsonObject enrollmentGroupJson = enrollmentGroup.toJsonElement().getAsJsonObject();
        assertEquals("namespace", enrollmentGroupJson.get("namespaceName").getAsString());
        assertEquals("authority", enrollmentGroupJson.get("certificateAuthorityName").getAsString());
        assertEquals("policy", enrollmentGroupJson.get("certificatePolicyName").getAsString());

        EnrollmentGroup deserializedEnrollmentGroup = new EnrollmentGroup(enrollmentGroupJson.toString());
        assertEquals("namespace", deserializedEnrollmentGroup.getNamespaceName());
        assertEquals("authority", deserializedEnrollmentGroup.getCertificateAuthorityName());
        assertEquals("policy", deserializedEnrollmentGroup.getCertificatePolicyName());
    }
}
