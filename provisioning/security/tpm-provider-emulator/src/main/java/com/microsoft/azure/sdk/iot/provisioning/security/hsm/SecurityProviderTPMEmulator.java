/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.security.hsm;

import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import tss.*;
import tss.tpm.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class SecurityProviderTPMEmulator extends SecurityProviderTpm
{
    private static final String REGEX_FOR_VALID_REGISTRATION_ID = "^[a-z0-9-]{1,128}$";
    private static final TPM_HANDLE SRK_PERSISTENT_HANDLE = TPM_HANDLE.persistent(0x00000001);
    private static final TPM_HANDLE EK_PERSISTENT_HANDLE = TPM_HANDLE.persistent(0x00010001);
    private static final TPM_HANDLE ID_KEY_PERSISTENT_HANDLE = TPM_HANDLE.persistent(0x00000100);
    private static final TPMT_SYM_DEF_OBJECT AES_128_SYM_DEF = new TPMT_SYM_DEF_OBJECT(TPM_ALG_ID.AES, 128, TPM_ALG_ID.CFB);
    private static final TPMT_PUBLIC EK_TEMPLATE = new TPMT_PUBLIC(
            // TPMI_ALG_HASH	nameAlg
            TPM_ALG_ID.SHA256,
            // TPMA_OBJECT  objectAttributes
            new TPMA_OBJECT(TPMA_OBJECT.restricted, TPMA_OBJECT.decrypt, TPMA_OBJECT.fixedTPM, TPMA_OBJECT.fixedParent,
                            TPMA_OBJECT.adminWithPolicy, TPMA_OBJECT.sensitiveDataOrigin),
            // TPM2B_DIGEST authPolicy
            javax.xml.bind.DatatypeConverter.parseHexBinary("837197674484b3f81a90cc8d46a5d724fd52d76e06520b64f2a1da1b331469aa"),
            // TPMU_PUBLIC_PARMS    parameters
            new TPMS_RSA_PARMS(AES_128_SYM_DEF, new TPMS_NULL_ASYM_SCHEME(), 2048, 0),
            // TPMU_PUBLIC_ID       unique
            new TPM2B_PUBLIC_KEY_RSA());
    private static final TPMT_PUBLIC SRK_TEMPLATE = new TPMT_PUBLIC(
            // TPMI_ALG_HASH	nameAlg
            TPM_ALG_ID.SHA256,
            // TPMA_OBJECT  objectAttributes
            new TPMA_OBJECT(TPMA_OBJECT.restricted, TPMA_OBJECT.decrypt, TPMA_OBJECT.fixedTPM, TPMA_OBJECT.fixedParent,
                            TPMA_OBJECT.noDA, TPMA_OBJECT.userWithAuth, TPMA_OBJECT.sensitiveDataOrigin),
            // TPM2B_DIGEST authPolicy
            new byte[0],
            // TPMU_PUBLIC_PARMS    parameters
            new TPMS_RSA_PARMS(AES_128_SYM_DEF, new TPMS_NULL_ASYM_SCHEME(), 2048, 0),
            // TPMU_PUBLIC_ID       unique
            new TPM2B_PUBLIC_KEY_RSA());
    private final String registrationId;
    private Tpm tpm = null;
    private TPMT_PUBLIC ekPublic = null;
    private TPMT_PUBLIC srkPublic = null;
    private TPM2B_PUBLIC idKeyPub = null;

    /**
     * Constructor for creating a Security Provider on TPM Simulator
     * @throws SecurityProviderException If the constructor could not start the TPM
     */
    public SecurityProviderTPMEmulator() throws SecurityProviderException
    {
        //SRS_SecurityProviderTPMEmulator_25_001: [ The constructor shall start the local TPM Simulator, clear persistent for EK and SRK if it exist, create persistent primary for EK and SRK. ]
        tpm = TpmFactory.localTpmSimulator();
        clearPersistent(tpm, EK_PERSISTENT_HANDLE, "EK");
        clearPersistent(tpm, SRK_PERSISTENT_HANDLE, "SRK");
        ekPublic = createPersistentPrimary(tpm, EK_PERSISTENT_HANDLE, TPM_RH.OWNER, EK_TEMPLATE, "EK");
        srkPublic = createPersistentPrimary(tpm, SRK_PERSISTENT_HANDLE, TPM_RH.OWNER, SRK_TEMPLATE, "SRK");
        //SRS_SecurityProviderTPMEmulator_25_002: [ The constructor shall set the registration Id to null if none was provided. ]
        this.registrationId = null;
    }

    /**
     * Constructor for creating a Security Provider on TPM Simulator with the supplied Registration ID
     * @param registrationId A non {@code null} or empty value tied to this registration
     * @throws SecurityProviderException If the constructor could not start the TPM
     */
    public SecurityProviderTPMEmulator(String registrationId) throws SecurityProviderException
    {
        if (registrationId == null || registrationId.isEmpty())
        {
            //SRS_SecurityProviderTPMEmulator_25_003: [ The constructor shall throw IllegalArgumentException if registration id was null or empty. ]
            throw new IllegalArgumentException("Registration Id cannot be null or empty");
        }
        if (!registrationId.matches(REGEX_FOR_VALID_REGISTRATION_ID))
        {
            //SRS_SecurityProviderTPMEmulator_25_004: [ The constructor shall validate and throw IllegalArgumentException if registration id is invalid. Valid registration Id
            // shall be alphanumeric, lowercase, and may contain hyphens. Max characters allowed is 128 . ]
            throw new IllegalArgumentException("The registration ID is alphanumeric, lowercase, and may contain hyphens. Max characters allowed is 128.");
        }

        //SRS_SecurityProviderTPMEmulator_25_005: [ The constructor shall save the registration Id if it was provided. ]
        this.registrationId = registrationId;
        tpm = TpmFactory.localTpmSimulator();
        clearPersistent(tpm, EK_PERSISTENT_HANDLE, "EK");
        clearPersistent(tpm, SRK_PERSISTENT_HANDLE, "SRK");
        ekPublic = createPersistentPrimary(tpm, EK_PERSISTENT_HANDLE, TPM_RH.OWNER, EK_TEMPLATE, "EK");
        srkPublic = createPersistentPrimary(tpm, SRK_PERSISTENT_HANDLE, TPM_RH.OWNER, SRK_TEMPLATE, "SRK");
    }

    /**
     * Constructor for creating a Security Provider on TPM Simulator with the supplied Registration ID and
     * ip address of the the remote where TPM simulator is running
     * @param registrationId A non {@code null} or empty value tied to this registration
     * @param ipAddressSimulator A non {@code null} or empty value of the ip address on which simulator is running.
     * @throws SecurityProviderException If the constructor could not start the TPM
     */
    public SecurityProviderTPMEmulator(String registrationId, String ipAddressSimulator) throws SecurityProviderException
    {
        if (registrationId == null || registrationId.isEmpty())
        {
            //SRS_SecurityProviderTPMEmulator_25_003: [ The constructor shall throw IllegalArgumentException if registration id was null or empty. ]
            throw new IllegalArgumentException("Registration Id cannot be null or empty");
        }
        if (!registrationId.matches(REGEX_FOR_VALID_REGISTRATION_ID))
        {
            //SRS_SecurityProviderTPMEmulator_25_004: [ The constructor shall validate and throw IllegalArgumentException if registration id is invalid. Valid registration Id
            // shall be alphanumeric, lowercase, and may contain hyphens. Max characters allowed is 128 . ]
            throw new IllegalArgumentException("The registration ID is alphanumeric, lowercase, and may contain hyphens. Max characters allowed is 128.");
        }
        if (ipAddressSimulator == null || ipAddressSimulator.isEmpty())
        {
            throw new IllegalArgumentException("ipAddress of the simulator cannot be null or empty");
        }

        InetAddress inetAddress;
        try
        {
            inetAddress = InetAddress.getByName(ipAddressSimulator);
        }
        catch (UnknownHostException e)
        {
            throw new SecurityProviderException(e);
        }

        //SRS_SecurityProviderTPMEmulator_25_005: [ The constructor shall save the registration Id if it was provided. ]
        this.registrationId = registrationId;
        tpm = TpmFactory.remoteTpmSimulator(inetAddress.getHostName());
        clearPersistent(tpm, EK_PERSISTENT_HANDLE, "EK");
        clearPersistent(tpm, SRK_PERSISTENT_HANDLE, "SRK");
        ekPublic = createPersistentPrimary(tpm, EK_PERSISTENT_HANDLE, TPM_RH.OWNER, EK_TEMPLATE, "EK");
        srkPublic = createPersistentPrimary(tpm, SRK_PERSISTENT_HANDLE, TPM_RH.OWNER, SRK_TEMPLATE, "SRK");
    }

    /**
     * Closes the simulator if it were running already
     * @throws SecurityProviderException if simulator could not be closed for any reason.
     */
    public void shutDown() throws SecurityProviderException
    {
        if (tpm != null)
        {
            try
            {
                tpm.close();
            }
            catch (IOException e)
            {
                throw new SecurityProviderException(e);
            }
        }
    }

    /**
     * Getter for the Registration ID if it was provided. Default is returned otherwise.
     * @return The registration ID tied to this registration instance
     * @throws SecurityProviderException If registration ID could not be extracted
     */
    @Override
    public String getRegistrationId() throws SecurityProviderException
    {
        if (this.registrationId != null)
        {
            //SRS_SecurityProviderTPMEmulator_25_006: [ This method shall return registration Id if it was provided. ]
            return this.registrationId;
        }
        else
        {
            //SRS_SecurityProviderTPMEmulator_25_007: [ This method shall call its super method if registration Id was not provided. ]
            return super.getRegistrationId();
        }
    }

    private TPMT_PUBLIC createPersistentPrimary(Tpm tpm, TPM_HANDLE hPersistent, TPM_RH hierarchy, TPMT_PUBLIC inPub, String primaryRole) throws SecurityProviderException
    {
        ReadPublicResponse rpResp = tpm._allowErrors().ReadPublic(hPersistent);
        if (rpResp == null)
        {
            throw new SecurityProviderException("ReadPublicResponse cannot be null");
        }
        TPM_RC	rc = tpm._getLastResponseCode();

        if (rc == TPM_RC.SUCCESS)
        {
            // TODO: Check if the public area of the existing key matches the requested one
            return rpResp.outPublic;
        }
        if (rc != TPM_RC.HANDLE)
        {
            throw new SecurityProviderException("Unexpected failure {" +  rc.name() + "} of TPM2_ReadPublic for {" + primaryRole + "}");
        }

        TPMS_SENSITIVE_CREATE sens = new TPMS_SENSITIVE_CREATE(new byte[0], new byte[0]);
        CreatePrimaryResponse cpResp = tpm.CreatePrimary(TPM_HANDLE.from(hierarchy), sens, inPub,
                                                         new byte[0], new TPMS_PCR_SELECTION[0]);

        if (cpResp == null)
        {
            throw new SecurityProviderException("CreatePrimaryResponse cannot be null");
        }

        tpm.EvictControl(TPM_HANDLE.from(TPM_RH.OWNER), cpResp.handle, hPersistent);
        tpm.FlushContext(cpResp.handle);
        return cpResp.outPublic;
    }

    private void clearPersistent(Tpm tpm, TPM_HANDLE hPersistent, String keyRole) throws SecurityProviderException
    {
        tpm._allowErrors().ReadPublic(hPersistent);
        TPM_RC	rc = tpm._getLastResponseCode();
        if (rc == TPM_RC.SUCCESS)
        {
            tpm.EvictControl(TPM_HANDLE.from(TPM_RH.OWNER), hPersistent, hPersistent);
        }
        else if (rc != TPM_RC.HANDLE)
        {
            throw new SecurityProviderException("Unexpected failure for {" + rc.name() + "} of TPM2_ReadPublic for " + keyRole + " 0x" + hPersistent.handle);
        }
    }

    // NOTE: For now only HMAC signing is supported.
    private byte[] signData(Tpm tpm, TPMT_PUBLIC idKeyPub, byte[] tokenData) throws SecurityProviderException
    {
        TPM_ALG_ID	idKeyHashAlg = ((TPMS_SCHEME_HMAC)((TPMS_KEYEDHASH_PARMS)idKeyPub.parameters).scheme).hashAlg;
        int 		MaxInputBuffer = TpmHelpers.getTpmProperty(tpm, TPM_PT.INPUT_BUFFER);

        if (tokenData.length <= MaxInputBuffer)
        {
            return tpm.HMAC(ID_KEY_PERSISTENT_HANDLE, tokenData, idKeyHashAlg);
        }

        int curPos = 0;
        int bytesLeft = tokenData.length;

        TPM_HANDLE  hSeq = tpm.HMAC_Start(ID_KEY_PERSISTENT_HANDLE, new byte[0], idKeyHashAlg);

        if (hSeq == null)
        {
            throw new SecurityProviderException("hSeq cannot be null");
        }

        do
        {
            tpm.SequenceUpdate(hSeq, Arrays.copyOfRange(tokenData, curPos, curPos + MaxInputBuffer));
            bytesLeft -= MaxInputBuffer;
            curPos += MaxInputBuffer;
        } while (bytesLeft > MaxInputBuffer);

        return tpm.SequenceComplete(hSeq, Arrays.copyOfRange(tokenData, curPos, curPos + bytesLeft), TPM_HANDLE.from(TPM_RH.NULL)).result;
    }

    /**
     * Activates the Identity with the nonce provided from the service
     * @param key Key for activating the TPM
     * @return {@code null} value is returned. Place holder for eventual returns.
     * @throws SecurityProviderException If activation was not successful.
     */
    @Override
    public byte[] activateIdentityKey(byte[] key) throws SecurityProviderException
    {
        InByteBuf actBlob = new InByteBuf(Arrays.copyOfRange(key, 0, key.length));

        TPM2B_ID_OBJECT         credBlob = TPM2B_ID_OBJECT.fromTpm(actBlob);
        TPM2B_ENCRYPTED_SECRET  encSecret = TPM2B_ENCRYPTED_SECRET.fromTpm(actBlob);
        TPM2B_PRIVATE           idKeyDupBlob = TPM2B_PRIVATE.fromTpm(actBlob);
        TPM2B_ENCRYPTED_SECRET  encWrapKey = TPM2B_ENCRYPTED_SECRET.fromTpm(actBlob);
        idKeyPub = TPM2B_PUBLIC.fromTpm(actBlob);
        TPM2B_DATA				encUriData = TPM2B_DATA.fromTpm(actBlob);

        if (idKeyPub == null)
        {
            //SRS_SecurityProviderTPMEmulator_25_008: [ This method shall throw SecurityProviderException if ID Key Public could not be extracted form TPM. ]
            throw new SecurityProviderException("Id Key Public cannot be null");
        }

        //
        // Prepare a policy session to be used with ActivateCredential()
        //
        //SRS_SecurityProviderTPMEmulator_25_009: [ This method shall start Authorization session with TPM. ]
        StartAuthSessionResponse sasResp = tpm.StartAuthSession(TPM_HANDLE.NULL, TPM_HANDLE.NULL,
                                                                Helpers.getRandom(20), new byte[0], TPM_SE.POLICY,
                                                                new TPMT_SYM_DEF(TPM_ALG_ID.NULL, 0, TPM_ALG_ID.NULL), TPM_ALG_ID.SHA256);

        if (sasResp == null)
        {
            //SRS_SecurityProviderTPMEmulator_25_010: [ This method shall throw  SecurityProviderException if Authorization session with TPM could not be started. ]
            throw new SecurityProviderException("StartAuthSessionResponse cannot be null");
        }

        //SRS_SecurityProviderTPMEmulator_25_011: [ This method shall set the policy secret on to TPM using the endorsement. ]
        tpm.PolicySecret(TPM_HANDLE.from(TPM_RH.ENDORSEMENT), sasResp.handle,
                         new byte[0], new byte[0], new byte[0], 0);

        // Use ActivateCredential() to decrypt symmetric key that is used as an inner protector
        // of the duplication blob of the new Device ID key generated by Service.
        //SRS_SecurityProviderTPMEmulator_25_012: [ This method shall activate the credential for the session. ]
        byte[] innerWrapKey = tpm._withSessions(TPM_HANDLE.pwSession(new byte[0]), sasResp.handle)
                .ActivateCredential(SRK_PERSISTENT_HANDLE, EK_PERSISTENT_HANDLE, credBlob.credential, encSecret.secret);

        if (innerWrapKey == null)
        {
            //SRS_SecurityProviderTPMEmulator_25_013: [ This method shall throw SecurityProviderException if activating the credential for the session fails. ]
            throw new SecurityProviderException("innerWrapKey cannot be null");
        }

        // Initialize parameters of the symmetric key used by Service
        // Note that the client uses the key size chosen by Service, but other parameters are fixes (an AES key in CFB mode).
        TPMT_SYM_DEF_OBJECT symDef = new TPMT_SYM_DEF_OBJECT(TPM_ALG_ID.AES, innerWrapKey.length * 8, TPM_ALG_ID.CFB);

        //
        // Import the new Device ID key issued by Service into the device's TPM
        //
        //SRS_SecurityProviderTPMEmulator_25_014: [ This method shall import the activated credential onto TPM. ]
        TPM2B_PRIVATE idKeyPrivate = tpm.Import(SRK_PERSISTENT_HANDLE, innerWrapKey, idKeyPub.publicArea, idKeyDupBlob, encWrapKey.secret, symDef);

        if (idKeyPrivate == null)
        {
            //SRS_SecurityProviderTPMEmulator_25_015: [ This method shall throw SecurityProviderException if importing the activated credential onto TPM fails. ]
            throw new SecurityProviderException("idKeyPrivate cannot be null");
        }

        //
        // Load and persist new Device ID key issued by Service
        //
        //SRS_SecurityProviderTPMEmulator_25_016: [ This method shall load SRK onto TPM. ]
        TPM_HANDLE hIdKey = tpm.Load(SRK_PERSISTENT_HANDLE, idKeyPrivate, idKeyPub.publicArea);

        if (hIdKey == null)
        {
            //SRS_SecurityProviderTPMEmulator_25_017: [ This method shall throw SecurityProviderException if loading SRK onto TPM fails. ]
            throw new SecurityProviderException("hIdKey cannot be null");
        }

        //SRS_SecurityProviderTPMEmulator_25_018: [ This method shall clear the persistent for key role "ID Key" . ]
        clearPersistent(tpm, ID_KEY_PERSISTENT_HANDLE, "ID Key");

        //SRS_SecurityProviderTPMEmulator_25_019: [ This method Evict Control once done . ]
        tpm.EvictControl(TPM_HANDLE.from(TPM_RH.OWNER), hIdKey, ID_KEY_PERSISTENT_HANDLE);

        //SRS_SecurityProviderTPMEmulator_25_020: [ This method Flush the context once done . ]
        tpm.FlushContext(hIdKey);

        //
        // Decrypt URI data using TPM.
        // A recommended alternative for the actual SDK code is to use the symmetric algorithm from a software crypto library
        //

        int maxUriDataSize = TpmHelpers.getTpmProperty(tpm, TPM_PT.INPUT_BUFFER);

        if (encUriData.buffer.length > maxUriDataSize)
        {
            //SRS_SecurityProviderTPMEmulator_25_021: [ This method shall throw SecurityProviderException if the encoded Uri length is greater than Maximum Uri Length . ]
            throw new SecurityProviderException("Too long encrypted URI data string. Max supported length is " + Integer.toString(maxUriDataSize));
        }

        // The template of the symmetric key used by the Service
        TPMT_PUBLIC symTemplate = new TPMT_PUBLIC(
                // TPMI_ALG_HASH	nameAlg
                TPM_ALG_ID.SHA256,
                // TPMA_OBJECT  objectAttributes
                new TPMA_OBJECT(TPMA_OBJECT.decrypt, TPMA_OBJECT.fixedTPM, TPMA_OBJECT.fixedParent, TPMA_OBJECT.userWithAuth),
                // TPM2B_DIGEST authPolicy
                new byte[0],
                // TPMU_PUBLIC_PARMS    parameters
                new TPMS_SYMCIPHER_PARMS(symDef),
                // TPMU_PUBLIC_ID       unique
                new TPM2B_DIGEST_Symcipher());

        // URI data are encrypted with the same symmetric key used as the inner protector of the new Device ID key duplication blob.
        TPMS_SENSITIVE_CREATE sensCreate = new TPMS_SENSITIVE_CREATE (new byte[0], innerWrapKey);
        //SRS_SecurityProviderTPMEmulator_25_022: [ This method shall create TPMS_SENSITIVE_CREATE for the inner wrap key . ]
        CreateResponse crResp = tpm.Create(SRK_PERSISTENT_HANDLE, sensCreate, symTemplate, new byte[0], new TPMS_PCR_SELECTION[0]);

        if (crResp == null)
        {
            //SRS_SecurityProviderTPMEmulator_25_023: [ This method shall throw SecurityProviderException if creating TPMS_SENSITIVE_CREATE for the inner wrap key fails. ]
            throw new SecurityProviderException("CreateResponse cannot be null");
        }

        //SRS_SecurityProviderTPMEmulator_25_024: [ This method shall load the created response private onto TPM. ]
        TPM_HANDLE hSymKey = tpm.Load(SRK_PERSISTENT_HANDLE, crResp.outPrivate, crResp.outPublic);


        if (hSymKey == null)
        {
            //SRS_SecurityProviderTPMEmulator_25_025: [ This method shall throw if loading the created response private onto TPM fails. ]
            throw new SecurityProviderException("hSymKey cannot be null");
        }

        byte[] iv = new byte[innerWrapKey.length];

        //SRS_SecurityProviderTPMEmulator_25_026: [ This method shall Encrypt Decrypt the symmetric Key. ]
        //TODO : Use software encryption/decryption using AES instead of TPM command to support international markets.
        EncryptDecrypt2Response edResp = tpm.EncryptDecrypt2(hSymKey, encUriData.buffer, (byte)1, TPM_ALG_ID.CFB, iv);

        if (edResp == null)
        {
            //SRS_SecurityProviderTPMEmulator_25_0027: [ This method shall throw if Encrypt Decrypt the symmetric Key fails. ]
            throw new SecurityProviderException("EncryptDecryptResponse cannot be null");
        }

        //SRS_SecurityProviderTPMEmulator_25_028: [ This method shall flush the context for the symmetric Key. ]
        tpm.FlushContext(hSymKey);
        return null;
    }

    /**
     * This method signs the TPM with the provided device ID
     * @param deviceIdData A non {@code null} or empty value for the device ID
     * @return The signature after signing data.
     * @throws SecurityProviderException If signing was not successful
     */
    @Override
    public byte[] signWithIdentity(byte[] deviceIdData) throws SecurityProviderException
    {
        if (deviceIdData == null || deviceIdData.length == 0)
        {
            //SRS_SecurityProviderTPMEmulator_25_029: [ This method shall throw IllegalArgumentException if `deviceIdData` is null or empty. ]
            throw new IllegalArgumentException("deviceIdData cannot be null or empty");
        }

        if (idKeyPub == null)
        {
            //SRS_SecurityProviderTPMEmulator_25_030: [ This method shall throw SecurityProviderException if ID KEY public was not instantiated. ]
            throw new SecurityProviderException("activateIdentityKey first before signing");
        }
        //
        // Generate token data, and sign it using the new Device ID key
        //
        //SRS_SecurityProviderTPMEmulator_25_031: [ This method shall sign the device ID data. ]
        return signData(tpm, idKeyPub.publicArea, deviceIdData);
    }

    /**
     * Getter for extracting EndorsementKey from TPM
     * @return The Endorsement Key from TPM
     * @throws SecurityProviderException If endorsement key could not be extracted.
     */
    @Override
    public byte[] getEndorsementKey() throws SecurityProviderException
    {
        //SRS_SecurityProviderTPMEmulator_25_032: [ This method shall return the TPM2B_PUBLIC form of EK. ]
        return (new TPM2B_PUBLIC(ekPublic)).toTpm();
    }

    /**
     * Getter for extracting StorageRootKey from TPM
     * @return The StorageRootKey from TPM
     * @throws SecurityProviderException If StorageRootKey could not be extracted.
     */
    @Override
    public byte[] getStorageRootKey() throws SecurityProviderException
    {
        //SRS_SecurityProviderTPMEmulator_25_033: [ This method shall return the TPM2B_PUBLIC form of SRK. ]
        return (new TPM2B_PUBLIC(srkPublic)).toTpm();
    }
}
