/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.security.hsm;

import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderTPMEmulator;
import junit.framework.TestCase;
import mockit.*;
import org.junit.Test;
import tss.InByteBuf;
import tss.Tpm;
import tss.TpmFactory;
import tss.TpmHelpers;
import tss.tpm.*;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/*
 *  Unit tests for  SecurityProviderTPMEmulator
 *  Coverage : 100% Method, 100% Lines
 */

public class SecurityProviderTPMEmulatorTest
{
    private static final String[] INVALID_REGISTRATION_IDS = {"UPPERCASE", "UPPERandLowerMix",
            "greaterthan128lengthgreaterthan128lengthgreaterthan128lengthgreaterthan128lengthgreaterthan128lengthgreaterthan128lengthgreaterthan128lengthgreaterthan128lengthgreaterthan128lengthgreaterthan128length",
            "nonalphanumeric&*"
    };
    private static final String[] VALID_REGISTRATION_IDS = {"lowercase", "lowerand1234567890", "withhyphen-", "1234567890-",
            "lower-123456789"};

    @Mocked
    TpmFactory mockedTpmFactory;

    @Mocked
    Tpm mockedTpm;

    @Mocked
    TPM_HANDLE mockedTpmHandle;

    @Mocked
    TPM_RH mockedTpmRH;

    @Mocked
    TPM2B_ID_OBJECT mockedTpm2BIdObject;

    @Mocked
    TPM2B_ENCRYPTED_SECRET mockedTpm2BEncryptedSecret;

    @Mocked
    TPM2B_PRIVATE mockedTpm2BPrivate;

    @Mocked
    TPM2B_PUBLIC mockedTpm2BPublic;

    @Mocked
    TPM2B_DATA mockedTpm2BData;

    @Mocked
    TPMT_SYM_DEF_OBJECT mockedTpmtSymDefObject;

    @Mocked
    TPMT_PUBLIC mockedTpmtPublic;

    @Mocked
    TPMS_SENSITIVE_CREATE mockedTpmsSensitiveCreate;

    @Mocked
    CreateResponse mockedCreateResponse;

    @Mocked
    EncryptDecryptResponse mockedEncryptDecryptResponse;

    @Mocked
    ReadPublicResponse mockedReadPublicResponse;

    @Mocked
    CreatePrimaryResponse mockedCreatePrimaryResponse;

    @Mocked
    StartAuthSessionResponse mockedStartAuthSessionResponse;

    @Mocked
    TpmHelpers mockedTpmHelpers;

    private void clearPersistentExpectations()
    {
        new StrictExpectations()
        {
            {
                mockedTpm._allowErrors();
                mockedTpm.ReadPublic((TPM_HANDLE)any);
                result = mockedReadPublicResponse;
                mockedTpm._getLastResponseCode();
                result = TPM_RC.SUCCESS;
                mockedTpm.EvictControl((TPM_HANDLE)any, (TPM_HANDLE)any, (TPM_HANDLE)any);
            }
        };
    }

    private void createPersistentPrimaryExpectations()
    {
        new StrictExpectations()
        {
            {
                mockedTpm._allowErrors();
                mockedTpm.ReadPublic((TPM_HANDLE)any);
                result = mockedReadPublicResponse;
                mockedTpm._getLastResponseCode();
                result = TPM_RC.HANDLE;
                mockedTpm.CreatePrimary((TPM_HANDLE)any, (TPMS_SENSITIVE_CREATE)any, (TPMT_PUBLIC)any, (byte[])any,
                                        (TPMS_PCR_SELECTION[])any);
                result = mockedCreatePrimaryResponse;
                mockedTpm.EvictControl((TPM_HANDLE)any, (TPM_HANDLE)any, (TPM_HANDLE)any);
                mockedTpm.FlushContext((TPM_HANDLE)any);
            }
        };
    }

    //SRS_SecurityProviderTPMEmulator_25_001: [ The constructor shall start the local TPM Simulator, clear persistent for EK and SRK if it exist, create persistent primary for EK and SRK. ]
    //SRS_SecurityProviderTPMEmulator_25_002: [ The constructor shall set the registration Id to null if none was provided. ]
    @Test
    public void constructorSucceeds() throws Exception
    {
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();

        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();

        new Verifications()
        {
            {
                TpmFactory.localTpmSimulator();
                times = 1;
            }
        };
    }

    @Test
    public void constructorSucceedsWithSuccessHandle() throws Exception
    {
        new NonStrictExpectations()
        {
            {
                mockedTpm._allowErrors();
                mockedTpm.ReadPublic((TPM_HANDLE)any);
                result = mockedReadPublicResponse;
                mockedTpm._getLastResponseCode();
                result = TPM_RC.SUCCESS;
            }
        };

        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();

        new Verifications()
        {
            {
                TpmFactory.localTpmSimulator();
                times = 1;
            }
        };
    }

    @Test (expected = SecurityProviderException.class)
    public void constructorThrowsOnReadPublicResponseNull() throws Exception
    {
        //arrange
        new StrictExpectations()
        {
            {
                mockedTpm._allowErrors();
                mockedTpm.ReadPublic((TPM_HANDLE)any);
                result = null;
            }
        };

        //act
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
    }

    @Test (expected = SecurityProviderException.class)
    public void constructorThrowsOnResponseCodeNotSuccessNotHandle() throws Exception
    {
        //arrange
        new StrictExpectations()
        {
            {
                mockedTpm._allowErrors();
                mockedTpm.ReadPublic((TPM_HANDLE)any);
                result = mockedReadPublicResponse;
                mockedTpm._getLastResponseCode();
                result = TPM_RC.TESTING;
            }
        };

        //act
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
    }


    @Test (expected = SecurityProviderException.class)
    public void constructorThrowsOnResponseCodeNotSuccessNotHandleOnClear() throws Exception
    {
        //arrange
        new StrictExpectations()
        {
            {
                mockedTpm._allowErrors();
                mockedTpm.ReadPublic((TPM_HANDLE)any);
                result = mockedReadPublicResponse;
                mockedTpm._getLastResponseCode();
                result = TPM_RC.TESTING;
            }
        };

        //act
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
    }

    @Test (expected = SecurityProviderException.class)
    public void constructorThrowsOnCreatePrimaryResponseNull() throws Exception
    {
        //arrange
        new StrictExpectations()
        {
            {
                mockedTpm._allowErrors();
                mockedTpm.ReadPublic((TPM_HANDLE)any);
                result = mockedReadPublicResponse;
                mockedTpm._getLastResponseCode();
                result = TPM_RC.HANDLE;
                mockedTpm.CreatePrimary((TPM_HANDLE)any, (TPMS_SENSITIVE_CREATE)any, (TPMT_PUBLIC)any, (byte[])any,
                                        (TPMS_PCR_SELECTION[])any);
                result = null;
            }
        };

        //act
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
    }

    //SRS_SecurityProviderTPMEmulator_25_005: [ The constructor shall save the registration Id if it was provided. ]
    @Test
    public void constructorSavesValidRegistrationId() throws Exception
    {
        for (String regId : VALID_REGISTRATION_IDS)
        {
            createPersistentPrimaryExpectations();
            createPersistentPrimaryExpectations();

            SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator(regId);

            new Verifications()
            {
                {
                    TpmFactory.localTpmSimulator();
                    times = 1;
                }
            };
        }
    }

    //SRS_SecurityProviderTPMEmulator_25_004: [ The constructor shall validate and throw IllegalArgumentException if registration id is invalid. Valid registration Id
    @Test
    public void constructorThrowsOnInvalidRegistrationId() throws Exception
    {
        for (String regId : INVALID_REGISTRATION_IDS)
        {
            boolean invalidRegId = false;

            try
            {
                SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator(regId);
            }
            catch (IllegalArgumentException e)
            {
                invalidRegId = true;
            }

            assertTrue(regId + " is invalid Registration Id but was found valid ", invalidRegId);
        }
    }

    //SRS_SecurityProviderTPMEmulator_25_003: [ The constructor shall throw IllegalArgumentException if registration id was null or empty. ]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullRegistrationId() throws Exception
    {
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator(null);
    }

    //SRS_SecurityProviderTPMEmulator_25_006: [ This method shall return registration Id if it was provided. ]
    @Test
    public void getterReturnsRegistrationIdIfFound() throws Exception
    {
        //arrange
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator(VALID_REGISTRATION_IDS[0]);
        //act
        String testRegId = securityProviderTPMEmulator.getRegistrationId();
        //assert
        assertEquals(VALID_REGISTRATION_IDS[0], testRegId);

    }

    //SRS_SecurityProviderTPMEmulator_25_007: [ This method shall call its super method if registration Id was not provided. ]
    @Test
    public void getterCallsSuperRegistrationIdIfNotFound() throws Exception
    {
        //arrange
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
        //act
        String testRegId = securityProviderTPMEmulator.getRegistrationId();
        //assert
        TestCase.assertNotNull(testRegId);
    }

    //SRS_SecurityProviderTPMEmulator_25_009: [ This method shall start Authorization session with TPM. ]
    //SRS_SecurityProviderTPMEmulator_25_011: [ This method shall set the policy secret on to TPM using the endorsement. ]
    //SRS_SecurityProviderTPMEmulator_25_012: [ This method shall activate the credential for the session. ]
    //SRS_SecurityProviderTPMEmulator_25_014: [ This method shall import the activated credential onto TPM. ]
    //SRS_SecurityProviderTPMEmulator_25_016: [ This method shall load SRK onto TPM. ]
    //SRS_SecurityProviderTPMEmulator_25_018: [ This method shall clear the persistent for key role "ID Key" . ]
    //SRS_SecurityProviderTPMEmulator_25_019: [ This method Evict Control once done . ]
    //SRS_SecurityProviderTPMEmulator_25_020: [ This method Flush the context once done . ]
    //SRS_SecurityProviderTPMEmulator_25_022: [ This method shall create TPMS_SENSITIVE_CREATE for the inner wrap key . ]
    //SRS_SecurityProviderTPMEmulator_25_024: [ This method shall load the created response private onto TPM. ]
    //SRS_SecurityProviderTPMEmulator_25_026: [ This method shall Encrypt Decrypt the symmetric Key. ]
    //SRS_SecurityProviderTPMEmulator_25_028: [ This method shall flush the context for the symmetric Key. ]
    @Test
    public void activateIdentityKeySucceeds() throws Exception
    {
        //arrange
        final byte[] testKey = "testKey".getBytes(StandardCharsets.UTF_8);
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
        new NonStrictExpectations()
        {
            {
                TPM2B_PUBLIC.fromTpm((InByteBuf) any);
                result = mockedTpm2BPublic;
                TPM2B_DATA.fromTpm((InByteBuf) any);
                result = mockedTpm2BData;
                mockedTpm.StartAuthSession((TPM_HANDLE)any, (TPM_HANDLE)any, (byte[]) any, (byte[])any, (TPM_SE)any, (TPMT_SYM_DEF)any
                        , (TPM_ALG_ID)any);
                result = mockedStartAuthSessionResponse;

                mockedTpm.PolicySecret((TPM_HANDLE)any, (TPM_HANDLE)any, (byte[])any, (byte[])any, (byte[])any, anyInt);

                mockedTpm._withSessions((TPM_HANDLE)any, mockedStartAuthSessionResponse.handle);
                mockedTpm.ActivateCredential((TPM_HANDLE)any, (TPM_HANDLE)any, (TPMS_ID_OBJECT)any, (byte[])any);
                result = "innerWrapKey".getBytes(StandardCharsets.UTF_8);

                mockedTpm.Import((TPM_HANDLE )any, (byte[] )any, (TPMT_PUBLIC )any, (TPM2B_PRIVATE )any, (byte[])any,
                                 (TPMT_SYM_DEF_OBJECT)any);
                result = mockedTpm2BPrivate;

                mockedTpm.Load((TPM_HANDLE)any, (TPM2B_PRIVATE)any, (TPMT_PUBLIC)any);
                result = mockedTpmHandle;

                //clearPersistentExpectations
                mockedTpm._allowErrors();
                mockedTpm.ReadPublic((TPM_HANDLE)any);
                result = mockedReadPublicResponse;
                mockedTpm._getLastResponseCode();
                result = TPM_RC.SUCCESS;
                mockedTpm.EvictControl((TPM_HANDLE)any, (TPM_HANDLE)any, (TPM_HANDLE)any);


                mockedTpm.EvictControl((TPM_HANDLE) any, (TPM_HANDLE) any, (TPM_HANDLE) any);

                mockedTpm.FlushContext((TPM_HANDLE) any);

                Deencapsulation.setField(mockedTpm2BData, "buffer", "len<10".getBytes(StandardCharsets.UTF_8));

                TpmHelpers.getTpmProperty(mockedTpm, TPM_PT.INPUT_BUFFER);
                result = 10;

                mockedTpm.Create((TPM_HANDLE) any, (TPMS_SENSITIVE_CREATE) any, (TPMT_PUBLIC)any, (byte[])any, (TPMS_PCR_SELECTION[]) any);
                result = mockedCreateResponse;

                mockedTpm.Load((TPM_HANDLE) any, (TPM2B_PRIVATE )any, (TPMT_PUBLIC )any);
                result = mockedTpmHandle;

                mockedTpm.EncryptDecrypt((TPM_HANDLE )any, anyByte, (TPM_ALG_ID )any, (byte[]) any, (byte[] )any);
                result = mockedEncryptDecryptResponse;

                mockedTpm.FlushContext((TPM_HANDLE )any);
            }
        };

        //act
        securityProviderTPMEmulator.activateIdentityKey(testKey);
    }

    //SRS_SecurityProviderTPMEmulator_25_008: [ This method shall throw SecurityProviderException if ID Key Public could not be extracted form TPM. ]
    @Test (expected = SecurityProviderException.class)
    public void activateIdentityKeyThrowsOnNullIdKeyPub() throws Exception
    {
        //arrange
        final byte[] testKey = "testKey".getBytes(StandardCharsets.UTF_8);
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
        new NonStrictExpectations()
        {
            {
                TPM2B_PUBLIC.fromTpm((InByteBuf) any);
                result = null;
            }
        };

        //act
        securityProviderTPMEmulator.activateIdentityKey(testKey);
    }

    //SRS_SecurityProviderTPMEmulator_25_010: [ This method shall throw  SecurityProviderException if Authorization session with TPM could not be started. ]
    @Test (expected = SecurityProviderException.class)
    public void activateIdentityKeyThrowsOnStartAuthSessionFail() throws Exception
    {
        //arrange
        final byte[] testKey = "testKey".getBytes(StandardCharsets.UTF_8);
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
        new NonStrictExpectations()
        {
            {
                TPM2B_PUBLIC.fromTpm((InByteBuf) any);
                result = mockedTpm2BPublic;
                TPM2B_DATA.fromTpm((InByteBuf) any);
                result = mockedTpm2BData;
                mockedTpm.StartAuthSession((TPM_HANDLE)any, (TPM_HANDLE)any, (byte[]) any, (byte[])any, (TPM_SE)any, (TPMT_SYM_DEF)any
                        , (TPM_ALG_ID)any);
                result = null;
            }
        };

        //act
        securityProviderTPMEmulator.activateIdentityKey(testKey);

    }

    //SRS_SecurityProviderTPMEmulator_25_013: [ This method shall throw SecurityProviderException if activating the credential for the session fails. ]
    @Test (expected = SecurityProviderException.class)
    public void activateIdentityKeyThrowsOnInnerWrapKeyNull() throws Exception
    {
        //arrange
        final byte[] testKey = "testKey".getBytes(StandardCharsets.UTF_8);
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
        new NonStrictExpectations()
        {
            {
                TPM2B_PUBLIC.fromTpm((InByteBuf) any);
                result = mockedTpm2BPublic;
                TPM2B_DATA.fromTpm((InByteBuf) any);
                result = mockedTpm2BData;
                mockedTpm.StartAuthSession((TPM_HANDLE)any, (TPM_HANDLE)any, (byte[]) any, (byte[])any, (TPM_SE)any, (TPMT_SYM_DEF)any
                        , (TPM_ALG_ID)any);
                result = mockedStartAuthSessionResponse;

                mockedTpm.PolicySecret((TPM_HANDLE)any, (TPM_HANDLE)any, (byte[])any, (byte[])any, (byte[])any, anyInt);

                mockedTpm._withSessions((TPM_HANDLE)any, mockedStartAuthSessionResponse.handle);
                mockedTpm.ActivateCredential((TPM_HANDLE)any, (TPM_HANDLE)any, (TPMS_ID_OBJECT)any, (byte[])any);
                result = null;
            }
        };

        //act
        securityProviderTPMEmulator.activateIdentityKey(testKey);

    }

    //SRS_SecurityProviderTPMEmulator_25_015: [ This method shall throw SecurityProviderException if importing the activated credential onto TPM fails. ]
    @Test (expected = SecurityProviderException.class)
    public void activateIdentityKeyThrowsOnIdKeyPrivateNull() throws Exception
    {
        //arrange
        final byte[] testKey = "testKey".getBytes(StandardCharsets.UTF_8);
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
        new NonStrictExpectations()
        {
            {
                TPM2B_PUBLIC.fromTpm((InByteBuf) any);
                result = mockedTpm2BPublic;
                TPM2B_DATA.fromTpm((InByteBuf) any);
                result = mockedTpm2BData;
                mockedTpm.StartAuthSession((TPM_HANDLE)any, (TPM_HANDLE)any, (byte[]) any, (byte[])any, (TPM_SE)any, (TPMT_SYM_DEF)any
                        , (TPM_ALG_ID)any);
                result = mockedStartAuthSessionResponse;

                mockedTpm.PolicySecret((TPM_HANDLE)any, (TPM_HANDLE)any, (byte[])any, (byte[])any, (byte[])any, anyInt);

                mockedTpm._withSessions((TPM_HANDLE)any, mockedStartAuthSessionResponse.handle);
                mockedTpm.ActivateCredential((TPM_HANDLE)any, (TPM_HANDLE)any, (TPMS_ID_OBJECT)any, (byte[])any);
                result = "innerWrapKey".getBytes(StandardCharsets.UTF_8);

                mockedTpm.Import((TPM_HANDLE )any, (byte[] )any, (TPMT_PUBLIC )any, (TPM2B_PRIVATE )any, (byte[])any,
                                 (TPMT_SYM_DEF_OBJECT)any);
                result = null;
            }
        };

        //act
        securityProviderTPMEmulator.activateIdentityKey(testKey);

    }

    //SRS_SecurityProviderTPMEmulator_25_017: [ This method shall throw SecurityProviderException if loading SRK onto TPM fails. ]
    @Test (expected = SecurityProviderException.class)
    public void activateIdentityKeyThrowsOnHIdKeyNull() throws Exception
    {
        //arrange
        final byte[] testKey = "testKey".getBytes(StandardCharsets.UTF_8);
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
        new NonStrictExpectations()
        {
            {
                TPM2B_PUBLIC.fromTpm((InByteBuf) any);
                result = mockedTpm2BPublic;
                TPM2B_DATA.fromTpm((InByteBuf) any);
                result = mockedTpm2BData;
                mockedTpm.StartAuthSession((TPM_HANDLE)any, (TPM_HANDLE)any, (byte[]) any, (byte[])any, (TPM_SE)any, (TPMT_SYM_DEF)any
                        , (TPM_ALG_ID)any);
                result = mockedStartAuthSessionResponse;

                mockedTpm.PolicySecret((TPM_HANDLE)any, (TPM_HANDLE)any, (byte[])any, (byte[])any, (byte[])any, anyInt);

                mockedTpm._withSessions((TPM_HANDLE)any, mockedStartAuthSessionResponse.handle);
                mockedTpm.ActivateCredential((TPM_HANDLE)any, (TPM_HANDLE)any, (TPMS_ID_OBJECT)any, (byte[])any);
                result = "innerWrapKey".getBytes(StandardCharsets.UTF_8);

                mockedTpm.Import((TPM_HANDLE )any, (byte[] )any, (TPMT_PUBLIC )any, (TPM2B_PRIVATE )any, (byte[])any,
                                 (TPMT_SYM_DEF_OBJECT)any);
                result = mockedTpm2BPrivate;

                mockedTpm.Load((TPM_HANDLE)any, (TPM2B_PRIVATE)any, (TPMT_PUBLIC)any);
                result = null;
            }
        };

        //act
        securityProviderTPMEmulator.activateIdentityKey(testKey);
    }

    //SRS_SecurityProviderTPMEmulator_25_021: [ This method shall throw SecurityProviderException if the encoded Uri length is greater than Maximum Uri Length . ]
    @Test (expected = SecurityProviderException.class)
    public void activateIdentityKeyThrowsOnInvalidLengthOfEncUriData() throws Exception
    {
        //arrange
        final byte[] testKey = "testKey".getBytes(StandardCharsets.UTF_8);
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
        new NonStrictExpectations()
        {
            {
                TPM2B_PUBLIC.fromTpm((InByteBuf) any);
                result = mockedTpm2BPublic;
                TPM2B_DATA.fromTpm((InByteBuf) any);
                result = mockedTpm2BData;
                mockedTpm.StartAuthSession((TPM_HANDLE)any, (TPM_HANDLE)any, (byte[]) any, (byte[])any, (TPM_SE)any, (TPMT_SYM_DEF)any
                        , (TPM_ALG_ID)any);
                result = mockedStartAuthSessionResponse;

                mockedTpm.PolicySecret((TPM_HANDLE)any, (TPM_HANDLE)any, (byte[])any, (byte[])any, (byte[])any, anyInt);

                mockedTpm._withSessions((TPM_HANDLE)any, mockedStartAuthSessionResponse.handle);
                mockedTpm.ActivateCredential((TPM_HANDLE)any, (TPM_HANDLE)any, (TPMS_ID_OBJECT)any, (byte[])any);
                result = "innerWrapKey".getBytes(StandardCharsets.UTF_8);

                mockedTpm.Import((TPM_HANDLE )any, (byte[] )any, (TPMT_PUBLIC )any, (TPM2B_PRIVATE )any, (byte[])any,
                                 (TPMT_SYM_DEF_OBJECT)any);
                result = mockedTpm2BPrivate;

                mockedTpm.Load((TPM_HANDLE)any, (TPM2B_PRIVATE)any, (TPMT_PUBLIC)any);
                result = mockedTpmHandle;

                //clearPersistentExpectations
                mockedTpm._allowErrors();
                mockedTpm.ReadPublic((TPM_HANDLE)any);
                result = mockedReadPublicResponse;
                mockedTpm._getLastResponseCode();
                result = TPM_RC.SUCCESS;
                mockedTpm.EvictControl((TPM_HANDLE)any, (TPM_HANDLE)any, (TPM_HANDLE)any);


                mockedTpm.EvictControl((TPM_HANDLE) any, (TPM_HANDLE) any, (TPM_HANDLE) any);

                mockedTpm.FlushContext((TPM_HANDLE) any);

                Deencapsulation.setField(mockedTpm2BData, "buffer", "lenGreaterThan10".getBytes(StandardCharsets.UTF_8));

                TpmHelpers.getTpmProperty(mockedTpm, TPM_PT.INPUT_BUFFER);
                result = 10;
            }
        };

        //act
        securityProviderTPMEmulator.activateIdentityKey(testKey);
    }

    //SRS_SecurityProviderTPMEmulator_25_023: [ This method shall throw SecurityProviderException if creating TPMS_SENSITIVE_CREATE for the inner wrap key fails. ]
    @Test (expected = SecurityProviderException.class)
    public void activateIdentityKeyThrowsOnCreateResponseNull() throws Exception
    {
        //arrange
        final byte[] testKey = "testKey".getBytes(StandardCharsets.UTF_8);
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
        new NonStrictExpectations()
        {
            {
                TPM2B_PUBLIC.fromTpm((InByteBuf) any);
                result = mockedTpm2BPublic;
                TPM2B_DATA.fromTpm((InByteBuf) any);
                result = mockedTpm2BData;
                mockedTpm.StartAuthSession((TPM_HANDLE)any, (TPM_HANDLE)any, (byte[]) any, (byte[])any, (TPM_SE)any, (TPMT_SYM_DEF)any
                        , (TPM_ALG_ID)any);
                result = mockedStartAuthSessionResponse;

                mockedTpm.PolicySecret((TPM_HANDLE)any, (TPM_HANDLE)any, (byte[])any, (byte[])any, (byte[])any, anyInt);

                mockedTpm._withSessions((TPM_HANDLE)any, mockedStartAuthSessionResponse.handle);
                mockedTpm.ActivateCredential((TPM_HANDLE)any, (TPM_HANDLE)any, (TPMS_ID_OBJECT)any, (byte[])any);
                result = "innerWrapKey".getBytes(StandardCharsets.UTF_8);

                mockedTpm.Import((TPM_HANDLE )any, (byte[] )any, (TPMT_PUBLIC )any, (TPM2B_PRIVATE )any, (byte[])any,
                                 (TPMT_SYM_DEF_OBJECT)any);
                result = mockedTpm2BPrivate;

                mockedTpm.Load((TPM_HANDLE)any, (TPM2B_PRIVATE)any, (TPMT_PUBLIC)any);
                result = mockedTpmHandle;

                //clearPersistentExpectations
                mockedTpm._allowErrors();
                mockedTpm.ReadPublic((TPM_HANDLE)any);
                result = mockedReadPublicResponse;
                mockedTpm._getLastResponseCode();
                result = TPM_RC.SUCCESS;
                mockedTpm.EvictControl((TPM_HANDLE)any, (TPM_HANDLE)any, (TPM_HANDLE)any);


                mockedTpm.EvictControl((TPM_HANDLE) any, (TPM_HANDLE) any, (TPM_HANDLE) any);

                mockedTpm.FlushContext((TPM_HANDLE) any);

                Deencapsulation.setField(mockedTpm2BData, "buffer", "len<10".getBytes(StandardCharsets.UTF_8));

                TpmHelpers.getTpmProperty(mockedTpm, TPM_PT.INPUT_BUFFER);
                result = 10;

                mockedTpm.Create((TPM_HANDLE) any, (TPMS_SENSITIVE_CREATE) any, (TPMT_PUBLIC)any, (byte[])any, (TPMS_PCR_SELECTION[]) any);
                result = null;
            }
        };

        //act
        securityProviderTPMEmulator.activateIdentityKey(testKey);
    }

    //SRS_SecurityProviderTPMEmulator_25_025: [ This method shall throw if loading the created response private onto TPM fails. ]
    @Test (expected = SecurityProviderException.class)
    public void activateIdentityKeyThrowsOnHSymKeyNull() throws Exception
    {
        //arrange
        final byte[] testKey = "testKey".getBytes(StandardCharsets.UTF_8);
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
        new NonStrictExpectations()
        {
            {
                TPM2B_PUBLIC.fromTpm((InByteBuf) any);
                result = mockedTpm2BPublic;
                TPM2B_DATA.fromTpm((InByteBuf) any);
                result = mockedTpm2BData;
                mockedTpm.StartAuthSession((TPM_HANDLE) any, (TPM_HANDLE) any, (byte[]) any, (byte[]) any, (TPM_SE) any, (TPMT_SYM_DEF) any
                        , (TPM_ALG_ID) any);
                result = mockedStartAuthSessionResponse;

                mockedTpm.PolicySecret((TPM_HANDLE) any, (TPM_HANDLE) any, (byte[]) any, (byte[]) any, (byte[]) any, anyInt);

                mockedTpm._withSessions((TPM_HANDLE) any, mockedStartAuthSessionResponse.handle);
                mockedTpm.ActivateCredential((TPM_HANDLE) any, (TPM_HANDLE) any, (TPMS_ID_OBJECT) any, (byte[]) any);
                result = "innerWrapKey".getBytes(StandardCharsets.UTF_8);

                mockedTpm.Import((TPM_HANDLE) any, (byte[]) any, (TPMT_PUBLIC) any, (TPM2B_PRIVATE) any, (byte[]) any,
                                 (TPMT_SYM_DEF_OBJECT) any);
                result = mockedTpm2BPrivate;
            }
        };

        new StrictExpectations()
        {
            {
                mockedTpm.Load((TPM_HANDLE)any, (TPM2B_PRIVATE)any, (TPMT_PUBLIC)any);
                result = mockedTpmHandle;
            }
        };

        new NonStrictExpectations()
        {
            {
                //clearPersistentExpectations
                mockedTpm._allowErrors();
                mockedTpm.ReadPublic((TPM_HANDLE) any);
                result = mockedReadPublicResponse;
                mockedTpm._getLastResponseCode();
                result = TPM_RC.SUCCESS;
                mockedTpm.EvictControl((TPM_HANDLE) any, (TPM_HANDLE) any, (TPM_HANDLE) any);

                mockedTpm.EvictControl((TPM_HANDLE) any, (TPM_HANDLE) any, (TPM_HANDLE) any);

                mockedTpm.FlushContext((TPM_HANDLE) any);

                Deencapsulation.setField(mockedTpm2BData, "buffer", "len<10".getBytes(StandardCharsets.UTF_8));

                TpmHelpers.getTpmProperty(mockedTpm, TPM_PT.INPUT_BUFFER);
                result = 10;

                mockedTpm.Create((TPM_HANDLE) any, (TPMS_SENSITIVE_CREATE) any, (TPMT_PUBLIC) any, (byte[]) any, (TPMS_PCR_SELECTION[]) any);
                result = mockedCreateResponse;
            }
        };

        new StrictExpectations()
        {
            {

                mockedTpm.Load((TPM_HANDLE) any, (TPM2B_PRIVATE )any, (TPMT_PUBLIC )any);
                result = null;
            }
        };

        //act
        securityProviderTPMEmulator.activateIdentityKey(testKey);
    }

    //SRS_SecurityProviderTPMEmulator_25_0027: [ This method shall throw if Encrypt Decrypt the symmetric Key fails. ]
    @Test (expected = SecurityProviderException.class)
    public void activateIdentityKeyThrowsOnEncryptDecryptResponseNull() throws Exception
    {
        //arrange
        final byte[] testKey = "testKey".getBytes(StandardCharsets.UTF_8);
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
        new NonStrictExpectations()
        {
            {
                TPM2B_PUBLIC.fromTpm((InByteBuf) any);
                result = mockedTpm2BPublic;
                TPM2B_DATA.fromTpm((InByteBuf) any);
                result = mockedTpm2BData;
                mockedTpm.StartAuthSession((TPM_HANDLE)any, (TPM_HANDLE)any, (byte[]) any, (byte[])any, (TPM_SE)any, (TPMT_SYM_DEF)any
                        , (TPM_ALG_ID)any);
                result = mockedStartAuthSessionResponse;

                mockedTpm.PolicySecret((TPM_HANDLE)any, (TPM_HANDLE)any, (byte[])any, (byte[])any, (byte[])any, anyInt);

                mockedTpm._withSessions((TPM_HANDLE)any, mockedStartAuthSessionResponse.handle);
                mockedTpm.ActivateCredential((TPM_HANDLE)any, (TPM_HANDLE)any, (TPMS_ID_OBJECT)any, (byte[])any);
                result = "innerWrapKey".getBytes(StandardCharsets.UTF_8);

                mockedTpm.Import((TPM_HANDLE )any, (byte[] )any, (TPMT_PUBLIC )any, (TPM2B_PRIVATE )any, (byte[])any,
                                 (TPMT_SYM_DEF_OBJECT)any);
                result = mockedTpm2BPrivate;

                mockedTpm.Load((TPM_HANDLE)any, (TPM2B_PRIVATE)any, (TPMT_PUBLIC)any);
                result = mockedTpmHandle;

                //clearPersistentExpectations
                mockedTpm._allowErrors();
                mockedTpm.ReadPublic((TPM_HANDLE)any);
                result = mockedReadPublicResponse;
                mockedTpm._getLastResponseCode();
                result = TPM_RC.SUCCESS;
                mockedTpm.EvictControl((TPM_HANDLE)any, (TPM_HANDLE)any, (TPM_HANDLE)any);


                mockedTpm.EvictControl((TPM_HANDLE) any, (TPM_HANDLE) any, (TPM_HANDLE) any);

                mockedTpm.FlushContext((TPM_HANDLE) any);

                Deencapsulation.setField(mockedTpm2BData, "buffer", "len<10".getBytes(StandardCharsets.UTF_8));

                TpmHelpers.getTpmProperty(mockedTpm, TPM_PT.INPUT_BUFFER);
                result = 10;

                mockedTpm.Create((TPM_HANDLE) any, (TPMS_SENSITIVE_CREATE) any, (TPMT_PUBLIC)any, (byte[])any, (TPMS_PCR_SELECTION[]) any);
                result = mockedCreateResponse;

                mockedTpm.Load((TPM_HANDLE) any, (TPM2B_PRIVATE )any, (TPMT_PUBLIC )any);
                result = mockedTpmHandle;

                mockedTpm.EncryptDecrypt((TPM_HANDLE )any, anyByte, (TPM_ALG_ID )any, (byte[]) any, (byte[] )any);
                result = null;
            }
        };

        //act
        securityProviderTPMEmulator.activateIdentityKey(testKey);
    }

    //SRS_SecurityProviderTPMEmulator_25_031: [ This method shall sign the device ID data. ]
    @Test
    public void signWithIdentitySucceeds(@Mocked TPM_ALG_ID mockedTpmAlgId,
                                         @Mocked TPMU_ASYM_SCHEME mockedTpmuAsymScheme,
                                         @Mocked TPMU_PUBLIC_PARMS mockedTpmuPublicParms,
                                         @Mocked TPMS_SCHEME_HMAC mockedTpmsSchemeHmac,
                                         @Mocked TPMS_KEYEDHASH_PARMS mockedTpmsKeyedhashParms) throws Exception
    {
        //arrange
        final byte[] deviceIdData = "deviceIdData".getBytes(StandardCharsets.UTF_8);
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
        new NonStrictExpectations()
        {
            {
                // idKeyPub
                Deencapsulation.setField(securityProviderTPMEmulator, "idKeyPub", mockedTpm2BPublic);
                // publicArea
                Deencapsulation.setField(mockedTpm2BPublic, "publicArea", mockedTpmtPublic);
                //parameters
                Deencapsulation.setField(mockedTpmtPublic, "parameters",  mockedTpmsKeyedhashParms);
                // scheme
                Deencapsulation.setField(mockedTpmsKeyedhashParms, "scheme",  mockedTpmsSchemeHmac);
                // hashAlg
                Deencapsulation.setField(mockedTpmsSchemeHmac, "hashAlg",  mockedTpmAlgId);

                TpmHelpers.getTpmProperty(mockedTpm, TPM_PT.INPUT_BUFFER);
                result = 10;
                mockedTpm.HMAC_Start((TPM_HANDLE) any, (byte[] ) any, mockedTpmAlgId);
                result = mockedTpmHandle;

                mockedTpm.SequenceUpdate((TPM_HANDLE) any, (byte[] ) any);

                mockedTpm.SequenceComplete((TPM_HANDLE) any, (byte[]) any, (TPM_HANDLE ) any);
            }
        };

        securityProviderTPMEmulator.signWithIdentity(deviceIdData);
    }

    //SRS_SecurityProviderTPMEmulator_25_030: [ This method shall throw SecurityProviderException if ID KEY public was not instantiated. ]
    @Test (expected = SecurityProviderException.class)
    public void signWithIdentityThrowsOnNullIdKeyPub() throws Exception
    {
        //arrange
        final byte[] deviceIdData = "deviceIdData".getBytes(StandardCharsets.UTF_8);
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
        new NonStrictExpectations()
        {
            {
                // idKeyPub
                Deencapsulation.setField(securityProviderTPMEmulator, "idKeyPub", null);
            }
        };

        securityProviderTPMEmulator.signWithIdentity(deviceIdData);
    }

    //SRS_SecurityProviderTPMEmulator_25_029: [ This method shall throw IllegalArgumentException if `deviceIdData` is null or empty. ]
    @Test (expected = IllegalArgumentException.class)
    public void signWithIdentityThrowsOnNullDeviceIdData() throws Exception
    {
        //arrange
        final byte[] deviceIdData = null;
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();

        //act
        securityProviderTPMEmulator.signWithIdentity(deviceIdData);
    }

    @Test
    public void signDataReturnsHMACOnSmallerLength(@Mocked TPM_ALG_ID mockedTpmAlgId,
                                                   @Mocked TPMU_ASYM_SCHEME mockedTpmuAsymScheme,
                                                   @Mocked TPMU_PUBLIC_PARMS mockedTpmuPublicParms,
                                                   @Mocked TPMS_SCHEME_HMAC mockedTpmsSchemeHmac,
                                                   @Mocked TPMS_KEYEDHASH_PARMS mockedTpmsKeyedhashParms) throws Exception
    {
        //arrange
        final byte[] deviceIdData = "less<10".getBytes(StandardCharsets.UTF_8);
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
        new NonStrictExpectations()
        {
            {
                // idKeyPub
                Deencapsulation.setField(securityProviderTPMEmulator, "idKeyPub", mockedTpm2BPublic);
                // publicArea
                Deencapsulation.setField(mockedTpm2BPublic, "publicArea", mockedTpmtPublic);
                //parameters
                Deencapsulation.setField(mockedTpmtPublic, "parameters",  mockedTpmsKeyedhashParms);
                // scheme
                Deencapsulation.setField(mockedTpmsKeyedhashParms, "scheme",  mockedTpmsSchemeHmac);
                // hashAlg
                Deencapsulation.setField(mockedTpmsSchemeHmac, "hashAlg",  mockedTpmAlgId);

                TpmHelpers.getTpmProperty(mockedTpm, TPM_PT.INPUT_BUFFER);
                result = 10;
                mockedTpm.HMAC((TPM_HANDLE )any, (byte[] )any, mockedTpmAlgId);
            }
        };

        securityProviderTPMEmulator.signWithIdentity(deviceIdData);
    }

    @Test (expected =  SecurityProviderException.class)
    public void signWithIdentityThrowsOnNullHandle(@Mocked TPM_ALG_ID mockedTpmAlgId,
                                                   @Mocked TPMU_ASYM_SCHEME mockedTpmuAsymScheme,
                                                   @Mocked TPMU_PUBLIC_PARMS mockedTpmuPublicParms,
                                                   @Mocked TPMS_SCHEME_HMAC mockedTpmsSchemeHmac,
                                                   @Mocked TPMS_KEYEDHASH_PARMS mockedTpmsKeyedhashParms) throws Exception
    {
        //arrange
        final byte[] deviceIdData = "deviceIdData".getBytes(StandardCharsets.UTF_8);
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();
        new NonStrictExpectations()
        {
            {
                // idKeyPub
                Deencapsulation.setField(securityProviderTPMEmulator, "idKeyPub", mockedTpm2BPublic);
                // publicArea
                Deencapsulation.setField(mockedTpm2BPublic, "publicArea", mockedTpmtPublic);
                //parameters
                Deencapsulation.setField(mockedTpmtPublic, "parameters",  mockedTpmsKeyedhashParms);
                // scheme
                Deencapsulation.setField(mockedTpmsKeyedhashParms, "scheme",  mockedTpmsSchemeHmac);
                // hashAlg
                Deencapsulation.setField(mockedTpmsSchemeHmac, "hashAlg",  mockedTpmAlgId);

                TpmHelpers.getTpmProperty(mockedTpm, TPM_PT.INPUT_BUFFER);
                result = 10;
                mockedTpm.HMAC_Start((TPM_HANDLE) any, (byte[] ) any, mockedTpmAlgId);
                result = null;
            }
        };

        securityProviderTPMEmulator.signWithIdentity(deviceIdData);
    }

    //SRS_SecurityProviderTPMEmulator_25_032: [ This method shall return the TPM2B_PUBLIC form of EK. ]
    @Test
    public void getEndorsementKeySucceeds() throws Exception
    {
        //arrange
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();

        //act //assert
        TestCase.assertNotNull(securityProviderTPMEmulator.getEndorsementKey());
    }

    //SRS_SecurityProviderTPMEmulator_25_033: [ This method shall return the TPM2B_PUBLIC form of SRK. ]
    @Test
    public void getStorageRootSucceeds() throws Exception
    {
        //arrange
        createPersistentPrimaryExpectations();
        createPersistentPrimaryExpectations();
        SecurityProviderTPMEmulator securityProviderTPMEmulator = new SecurityProviderTPMEmulator();

        //act //assert
        TestCase.assertNotNull(securityProviderTPMEmulator.getStorageRootKey());
    }
}
