// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.*;

/**
 * Deterministic, JMockit-free unit tests for the {@code 2026-11-02-preview} DPS service
 * contract additions on {@link EnrollmentGroup}:
 * {@code namespaceName}, {@code certificateAuthorityName}, {@code certificatePolicyName},
 * and {@code deviceTypeRefs}. Also guards the unsupported {@code credentialPolicyName}.
 */
public class PreviewEnrollmentGroupTest
{
    private static final String VALID_ENROLLMENT_GROUP_ID = "8be9cd0e-8934-4991-9cbf-cc3b6c7ac647";
    private static final String VALID_PRIMARY_KEY = encodeBase64String("validPrimaryKey".getBytes(StandardCharsets.UTF_8));
    private static final String VALID_SECONDARY_KEY = encodeBase64String("validSecondaryKey".getBytes(StandardCharsets.UTF_8));

    private static final String VALID_NAMESPACE_NAME = "contoso-namespace";
    private static final String VALID_CA_NAME = "contoso-ca";
    private static final String VALID_CERT_POLICY_NAME = "contoso-cert-policy";
    private static final String VALID_DEVICE_TYPE_REF = "gateway-v2";

    private static String baselineSymmetricKeyJson()
    {
        return "{\n" +
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"symmetricKey\",\n" +
                "    \"symmetricKey\": {\n" +
                "      \"primaryKey\": \"" + VALID_PRIMARY_KEY + "\",\n" +
                "      \"secondaryKey\": \"" + VALID_SECONDARY_KEY + "\"\n" +
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
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"symmetricKey\",\n" +
                "    \"symmetricKey\": {\n" +
                "      \"primaryKey\": \"" + VALID_PRIMARY_KEY + "\",\n" +
                "      \"secondaryKey\": \"" + VALID_SECONDARY_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"namespaceName\": \"" + VALID_NAMESPACE_NAME + "\",\n" +
                "  \"certificateAuthorityName\": \"" + VALID_CA_NAME + "\",\n" +
                "  \"certificatePolicyName\": \"" + VALID_CERT_POLICY_NAME + "\",\n" +
                "  \"deviceTypeRefs\": [\"" + VALID_DEVICE_TYPE_REF + "\"]\n" +
                "}";

        // act
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup(json);

        // assert
        assertEquals(VALID_NAMESPACE_NAME, enrollmentGroup.getNamespaceName());
        assertEquals(VALID_CA_NAME, enrollmentGroup.getCertificateAuthorityName());
        assertEquals(VALID_CERT_POLICY_NAME, enrollmentGroup.getCertificatePolicyName());
        Collection<String> refs = enrollmentGroup.getDeviceTypeRefs();
        assertNotNull(refs);
        assertTrue(refs.contains(VALID_DEVICE_TYPE_REF));
    }

    /* Preview fields set on the client must be serialized under their exact wire names. */
    @Test
    public void toJsonElement_previewFieldsSet_serializedUnderWireNames()
    {
        // arrange
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup(baselineSymmetricKeyJson());
        enrollmentGroup.setNamespaceName(VALID_NAMESPACE_NAME);
        enrollmentGroup.setCertificateAuthorityName(VALID_CA_NAME);
        enrollmentGroup.setCertificatePolicyName(VALID_CERT_POLICY_NAME);
        enrollmentGroup.setDeviceTypeRefs(Arrays.asList(VALID_DEVICE_TYPE_REF));

        // act
        String serialized = enrollmentGroup.toJsonElement().toString();

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
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup(baselineSymmetricKeyJson());
        String serialized = enrollmentGroup.toJsonElement().toString();

        // assert
        assertNull(enrollmentGroup.getNamespaceName());
        assertNull(enrollmentGroup.getCertificateAuthorityName());
        assertNull(enrollmentGroup.getCertificatePolicyName());
        assertNull(enrollmentGroup.getDeviceTypeRefs());
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
                "  \"enrollmentGroupId\": \"" + VALID_ENROLLMENT_GROUP_ID + "\",\n" +
                "  \"attestation\": {\n" +
                "    \"type\": \"symmetricKey\",\n" +
                "    \"symmetricKey\": {\n" +
                "      \"primaryKey\": \"" + VALID_PRIMARY_KEY + "\",\n" +
                "      \"secondaryKey\": \"" + VALID_SECONDARY_KEY + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"credentialPolicyName\": \"should-be-ignored\"\n" +
                "}";

        // act
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup(json);
        String serialized = enrollmentGroup.toJsonElement().toString();

        // assert
        assertFalse("credentialPolicyName must never be serialized: " + serialized,
                serialized.contains("credentialPolicyName"));
    }

    /* Response-only / unsupported guard: no public credentialPolicyName accessor may exist. */
    @Test
    public void credentialPolicyName_hasNoPublicAccessor()
    {
        for (java.lang.reflect.Method m : EnrollmentGroup.class.getMethods())
        {
            assertNotEquals("getCredentialPolicyName", m.getName());
            assertNotEquals("setCredentialPolicyName", m.getName());
        }
    }

    /* Existing construction (no preview fields) remains fully compatible. */
    @Test
    public void existingConstruction_withoutPreviewFields_stillWorks()
    {
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup(baselineSymmetricKeyJson());
        assertEquals(VALID_ENROLLMENT_GROUP_ID, enrollmentGroup.getEnrollmentGroupId());
    }
}
