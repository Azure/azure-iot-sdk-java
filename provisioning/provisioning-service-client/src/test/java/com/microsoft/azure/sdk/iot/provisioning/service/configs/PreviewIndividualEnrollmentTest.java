// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Deterministic, JMockit-free unit tests for the {@code 2026-11-02-preview} DPS service
 * contract additions on {@link IndividualEnrollment}:
 * {@code namespaceName}, {@code certificateAuthorityName}, {@code certificatePolicyName},
 * and {@code deviceTypeRefs}.
 *
 * <p>Also guards the unsupported {@code credentialPolicyName} field: it must never appear in
 * the serialized payload and must have no public accessor.</p>
 */
public class PreviewIndividualEnrollmentTest
{
    private static final String VALID_REGISTRATION_ID = "8be9cd0e-8934-4991-9cbf-cc3b6c7ac647";
    private static final String VALID_ENDORSEMENT_KEY = "76cadbbd-67af-49ab-b112-0c2e6a8445b0";

    private static final String VALID_NAMESPACE_NAME = "contoso-namespace";
    private static final String VALID_CA_NAME = "contoso-ca";
    private static final String VALID_CERT_POLICY_NAME = "contoso-cert-policy";
    private static final String VALID_DEVICE_TYPE_REF = "sensor-v1";

    private static String baselineTpmJson()
    {
        return "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    /* Preview fields present in a service response must be deserialized and exposed via getters. */
    @Test
    public void constructorWithJson_previewFieldsPresent_areDeserialized()
    {
        // arrange
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"namespaceName\": \"" + VALID_NAMESPACE_NAME + "\",\n" +
                "  \"certificateAuthorityName\": \"" + VALID_CA_NAME + "\",\n" +
                "  \"certificatePolicyName\": \"" + VALID_CERT_POLICY_NAME + "\",\n" +
                "  \"deviceTypeRefs\": [\"" + VALID_DEVICE_TYPE_REF + "\"]\n" +
                "}";

        // act
        IndividualEnrollment enrollment = new IndividualEnrollment(json);

        // assert
        assertEquals(VALID_NAMESPACE_NAME, enrollment.getNamespaceName());
        assertEquals(VALID_CA_NAME, enrollment.getCertificateAuthorityName());
        assertEquals(VALID_CERT_POLICY_NAME, enrollment.getCertificatePolicyName());
        Collection<String> refs = enrollment.getDeviceTypeRefs();
        assertNotNull(refs);
        assertTrue(refs.contains(VALID_DEVICE_TYPE_REF));
    }

    /* Preview fields set on the client must be serialized under their exact wire names. */
    @Test
    public void toJsonElement_previewFieldsSet_serializedUnderWireNames()
    {
        // arrange
        IndividualEnrollment enrollment = new IndividualEnrollment(baselineTpmJson());
        enrollment.setNamespaceName(VALID_NAMESPACE_NAME);
        enrollment.setCertificateAuthorityName(VALID_CA_NAME);
        enrollment.setCertificatePolicyName(VALID_CERT_POLICY_NAME);
        enrollment.setDeviceTypeRefs(Arrays.asList(VALID_DEVICE_TYPE_REF));

        // act
        String serialized = enrollment.toJsonElement().toString();

        // assert
        assertTrue("namespaceName missing: " + serialized, serialized.contains("\"namespaceName\""));
        assertTrue("certificateAuthorityName missing: " + serialized, serialized.contains("\"certificateAuthorityName\""));
        assertTrue("certificatePolicyName missing: " + serialized, serialized.contains("\"certificatePolicyName\""));
        assertTrue("deviceTypeRefs missing: " + serialized, serialized.contains("\"deviceTypeRefs\""));
    }

    /* When preview fields are absent, getters are null and nothing is emitted (back-compat). */
    @Test
    public void absentPreviewFields_gettersNull_andOmittedFromJson()
    {
        // act
        IndividualEnrollment enrollment = new IndividualEnrollment(baselineTpmJson());
        String serialized = enrollment.toJsonElement().toString();

        // assert
        assertNull(enrollment.getNamespaceName());
        assertNull(enrollment.getCertificateAuthorityName());
        assertNull(enrollment.getCertificatePolicyName());
        assertNull(enrollment.getDeviceTypeRefs());
        assertFalse(serialized.contains("namespaceName"));
        assertFalse(serialized.contains("certificateAuthorityName"));
        assertFalse(serialized.contains("certificatePolicyName"));
        assertFalse(serialized.contains("deviceTypeRefs"));
    }

    /* Unsupported credentialPolicyName in the input JSON must be ignored and never re-serialized. */
    @Test
    public void credentialPolicyName_inInput_ignoredAndNeverSerialized()
    {
        // arrange
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"tpm\",\n" +
                "    \"tpm\": {\n" +
                "      \"endorsementKey\": \"" + VALID_ENDORSEMENT_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"credentialPolicyName\": \"should-be-ignored\"\n" +
                "}";

        // act
        IndividualEnrollment enrollment = new IndividualEnrollment(json);
        String serialized = enrollment.toJsonElement().toString();

        // assert
        assertFalse("credentialPolicyName must never be serialized: " + serialized,
                serialized.contains("credentialPolicyName"));
    }

    /* Response-only / unsupported guard: no public credentialPolicyName accessor may exist. */
    @Test
    public void credentialPolicyName_hasNoPublicAccessor()
    {
        for (java.lang.reflect.Method m : IndividualEnrollment.class.getMethods())
        {
            assertNotEquals("getCredentialPolicyName", m.getName());
            assertNotEquals("setCredentialPolicyName", m.getName());
        }
    }

    /* Existing construction (no preview fields) remains fully compatible. */
    @Test
    public void existingConstruction_withoutPreviewFields_stillWorks()
    {
        IndividualEnrollment enrollment = new IndividualEnrollment(baselineTpmJson());
        assertEquals(VALID_REGISTRATION_ID, enrollment.getRegistrationId());
    }
}
