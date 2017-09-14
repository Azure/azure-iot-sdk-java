/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.dps.security.hsm;

import com.microsoft.azure.sdk.iot.dps.security.DPSSecurityClientKey;
import com.microsoft.azure.sdk.iot.dps.security.SecurityType;
import tss.*;
import tss.tpm.*;

import java.io.IOException;
import java.util.Arrays;

public class DPSSecurityClientTPMEmulator extends DPSSecurityClientKey
{
    private Tpm tpm = null;
    private TPMT_PUBLIC ekPub = null;
    private TPMT_PUBLIC	srkPub = null;

    TPM2B_ID_OBJECT         credBlob = null;
    TPM2B_ENCRYPTED_SECRET  encSecret = null;
    TPM2B_PRIVATE           idKeyDupBlob = null;
    TPM2B_ENCRYPTED_SECRET  encWrapKey = null;
    TPM2B_PUBLIC    		idKeyPub = null;
    TPM2B_DATA				encUriData = null;

    static final TPM_HANDLE SRK_PersHandle = TPM_HANDLE.persistent(0x00000001);
    static final TPM_HANDLE EK_PersHandle = TPM_HANDLE.persistent(0x00010001);
    static final TPM_HANDLE ID_KEY_PersHandle = TPM_HANDLE.persistent(0x00000100);

    static final TPMT_SYM_DEF_OBJECT Aes128SymDef = new TPMT_SYM_DEF_OBJECT(TPM_ALG_ID.AES, 128, TPM_ALG_ID.CFB);

    static final TPMT_PUBLIC EK_Template = new TPMT_PUBLIC(
            // TPMI_ALG_HASH	nameAlg
            TPM_ALG_ID.SHA256,
            // TPMA_OBJECT  objectAttributes
            new TPMA_OBJECT(TPMA_OBJECT.restricted, TPMA_OBJECT.decrypt, TPMA_OBJECT.fixedTPM, TPMA_OBJECT.fixedParent,
                            TPMA_OBJECT.adminWithPolicy, TPMA_OBJECT.sensitiveDataOrigin),
            // TPM2B_DIGEST authPolicy
            javax.xml.bind.DatatypeConverter.parseHexBinary("837197674484b3f81a90cc8d46a5d724fd52d76e06520b64f2a1da1b331469aa"),
            // TPMU_PUBLIC_PARMS    parameters
            new TPMS_RSA_PARMS(Aes128SymDef, new TPMS_NULL_ASYM_SCHEME(), 2048, 0),
            // TPMU_PUBLIC_ID       unique
            new TPM2B_PUBLIC_KEY_RSA());

    static final TPMT_PUBLIC SRK_Template = new TPMT_PUBLIC(
            // TPMI_ALG_HASH	nameAlg
            TPM_ALG_ID.SHA256,
            // TPMA_OBJECT  objectAttributes
            new TPMA_OBJECT(TPMA_OBJECT.restricted, TPMA_OBJECT.decrypt, TPMA_OBJECT.fixedTPM, TPMA_OBJECT.fixedParent,
                            TPMA_OBJECT.noDA, TPMA_OBJECT.userWithAuth, TPMA_OBJECT.sensitiveDataOrigin),
            // TPM2B_DIGEST authPolicy
            new byte[0],
            // TPMU_PUBLIC_PARMS    parameters
            new TPMS_RSA_PARMS(Aes128SymDef, new TPMS_NULL_ASYM_SCHEME(), 2048, 0),
            // TPMU_PUBLIC_ID       unique
            new TPM2B_PUBLIC_KEY_RSA());

    private TPMT_PUBLIC CreatePersistentPrimary(Tpm tpm, TPM_HANDLE hPers, TPM_RH hierarchy, TPMT_PUBLIC inPub, String primaryRole)
    {
        ReadPublicResponse rpResp = tpm._allowErrors().ReadPublic(hPers);
        TPM_RC	rc = tpm._getLastResponseCode();
        if (rc == TPM_RC.SUCCESS)
        {
            // TODO: Check if the public area of the existing key matches the requested one
            System.out.println( primaryRole + " already exists\r\n");
            return rpResp.outPublic;
        }
        if (rc != TPM_RC.HANDLE)
        {
            System.out.println("Unexpected failure {" +  rc.name() + "} of TPM2_ReadPublic for { " + primaryRole + " } 0xhPers");
            return null; // TPM_RH_NULL
        }

        TPMS_SENSITIVE_CREATE sens = new TPMS_SENSITIVE_CREATE(new byte[0], new byte[0]);
        CreatePrimaryResponse cpResp = tpm.CreatePrimary(TPM_HANDLE.from(hierarchy), sens, inPub,
                                                         new byte[0], new TPMS_PCR_SELECTION[0]);

        System.out.println(primaryRole + " Successfully created transient " + cpResp.handle.handle +"0x%08X\r\n");

        tpm.EvictControl(TPM_HANDLE.from(TPM_RH.OWNER), cpResp.handle, hPers);
        System.out.println(primaryRole + " Successfully persisted " + hPers.handle+ " as 0x%08X\r\n");

        tpm.FlushContext(cpResp.handle);
        return cpResp.outPublic;
    }

    static void ClearPersistent(Tpm tpm, TPM_HANDLE hPers, String keyRole)
    {
        tpm._allowErrors().ReadPublic(hPers);
        TPM_RC	rc = tpm._getLastResponseCode();
        if (rc == TPM_RC.SUCCESS)
        {
            System.out.println("Deleting persistent" + keyRole + " 0x%08hPers.handle");
            tpm.EvictControl(TPM_HANDLE.from(TPM_RH.OWNER), hPers, hPers);
            System.out.println("Successfully deleted persistent " + keyRole + " 0x%08" + hPers.handle);
        }
        else if (rc == TPM_RC.HANDLE)
        {
            System.out.println(keyRole + " 0x%08" + hPers.handle + " does not exist");
        }
        else
            System.out.println("Unexpected failure <"+ rc +"> of TPM2_ReadPublic for " + keyRole + "%s 0x%08" + hPers.handle);
    }

    // NOTE: For now only HMAC signing is supported.
    static byte[] SignData(Tpm tpm, TPMT_PUBLIC idKeyPub, byte[] tokenData)
    {
        TPM_ALG_ID	idKeyHashAlg = ((TPMS_SCHEME_HMAC)((TPMS_KEYEDHASH_PARMS)idKeyPub.parameters).scheme).hashAlg;
        int 		MaxInputBuffer = TpmHelpers.getTpmProperty(tpm, TPM_PT.INPUT_BUFFER);

        if (tokenData.length <= MaxInputBuffer)
        {
            return tpm.HMAC(ID_KEY_PersHandle, tokenData, idKeyHashAlg);
        }

        int curPos = 0;
        int bytesLeft = tokenData.length;

        TPM_HANDLE  hSeq = tpm.HMAC_Start(ID_KEY_PersHandle, new byte[0], idKeyHashAlg);

        do {
            tpm.SequenceUpdate(hSeq, Arrays.copyOfRange(tokenData, curPos, curPos + MaxInputBuffer));

            bytesLeft -= MaxInputBuffer;
            curPos += MaxInputBuffer;
        } while (bytesLeft > MaxInputBuffer);

        return tpm.SequenceComplete(hSeq, Arrays.copyOfRange(tokenData, curPos, curPos + bytesLeft), TPM_HANDLE.from(TPM_RH.NULL)).result;
    }

    public DPSSecurityClientTPMEmulator()
    {
        tpm = TpmFactory.localTpmSimulator();
        ekPub = CreatePersistentPrimary(tpm, EK_PersHandle, TPM_RH.OWNER, EK_Template, "EK");
        srkPub = CreatePersistentPrimary(tpm, SRK_PersHandle, TPM_RH.OWNER, SRK_Template, "SRK");
        this.setSecurityType(SecurityType.Key);
    }
    @Override
    public byte[] importKey(byte[] key) throws IOException
    {
        InByteBuf actBlob = new InByteBuf(Arrays.copyOfRange(key, 0, key.length));

        credBlob = TPM2B_ID_OBJECT.fromTpm(actBlob);
        encSecret = TPM2B_ENCRYPTED_SECRET.fromTpm(actBlob);
        idKeyDupBlob = TPM2B_PRIVATE.fromTpm(actBlob);
        encWrapKey = TPM2B_ENCRYPTED_SECRET.fromTpm(actBlob);
        idKeyPub = TPM2B_PUBLIC.fromTpm(actBlob);
        encUriData = TPM2B_DATA.fromTpm(actBlob);

        //
        // Prepare a policy session to be used with ActivateCredential()
        //
        StartAuthSessionResponse sasResp = tpm.StartAuthSession(TPM_HANDLE.NULL, TPM_HANDLE.NULL,
                                                                Helpers.getRandom(20), new byte[0], TPM_SE.POLICY,
                                                                new TPMT_SYM_DEF(TPM_ALG_ID.NULL, 0, TPM_ALG_ID.NULL), TPM_ALG_ID.SHA256);

        tpm.PolicySecret(TPM_HANDLE.from(TPM_RH.ENDORSEMENT), sasResp.handle,
                         new byte[0], new byte[0], new byte[0], 0);

        // Use ActivateCredential() to decrypt symmetric key that is used as an inner protector
        // of the duplication blob of the new Device ID key generated by DRS.
        byte[] innerWrapKey = tpm._withSessions(TPM_HANDLE.pwSession(new byte[0]), sasResp.handle)
                .ActivateCredential(SRK_PersHandle, EK_PersHandle, credBlob.credential, encSecret.secret);

        // Initialize parameters of the symmetric key used by DRS
        // Note that the client uses the key size chosen by DRS, but other parameters are fixes (an AES key in CFB mode).
        TPMT_SYM_DEF_OBJECT symDef = new TPMT_SYM_DEF_OBJECT(TPM_ALG_ID.AES, innerWrapKey.length * 8, TPM_ALG_ID.CFB);

        //
        // Import the new Device ID key issued by DRS into the device's TPM
        //
        TPM2B_PRIVATE idKeyPriv = tpm.Import(SRK_PersHandle, innerWrapKey, idKeyPub.publicArea, idKeyDupBlob, encWrapKey.secret, symDef);

        //
        // Load and persist new Device ID key issued by DRS
        //

        // TSS_Create could not be located in java at this step

        TPM_HANDLE hIdkey = tpm.Load(SRK_PersHandle, idKeyPriv, idKeyPub.publicArea);

        ClearPersistent(tpm, ID_KEY_PersHandle, "ID Key");

        tpm.EvictControl(TPM_HANDLE.from(TPM_RH.OWNER), hIdkey, ID_KEY_PersHandle);
        System.out.println("Successfully created persistent ID Key 0x%08" + ID_KEY_PersHandle.handle);

        tpm.FlushContext(hIdkey);

        // end of insert key into TPM

        //
        // Decrypt URI data using TPM.
        // A recommended alternative for the actual SDK code is to use the symmetric algorithm from a software crypto library
        //
        // digestsize is missing in Java

        int maxUriDataSize = TpmHelpers.getTpmProperty(tpm, TPM_PT.INPUT_BUFFER);
        if (encUriData.buffer.length > maxUriDataSize)

            throw new IOException("Too long encrypted URI data string. Max supported length is " + Integer.toString(maxUriDataSize));


        // The template of the symmetric key used by the DRS
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
        CreateResponse crResp = tpm.Create(SRK_PersHandle, sensCreate, symTemplate, new byte[0], new TPMS_PCR_SELECTION[0]);

        TPM_HANDLE hSymKey = tpm.Load(SRK_PersHandle, crResp.outPrivate, crResp.outPublic);

        byte[] iv = new byte[innerWrapKey.length];

        EncryptDecryptResponse edResp = tpm.EncryptDecrypt(hSymKey, (byte)1, TPM_ALG_ID.CFB, iv, encUriData.buffer);
        System.out.println("Decrypted URI data size: " + edResp.outData.length);

        // why ... not similar in C Sample
        tpm.FlushContext(hSymKey);
        return null;
    }

    @Override
    public byte[] signData(byte[] data)
    {
        //
        // Generate token data, and sign it using the new Device ID key
        // (Note that this sample simply generates a random buffer in lieu of a valid token)
        //

        // get the hash
        // deviceIdData is the  <idscope/registration/regid/> {url encoded} expirytime
        byte[] deviceIdData = data;
        //Helpers.getRandom(2550);

        return SignData(tpm, idKeyPub.publicArea, deviceIdData);
    }

    @Override
    public byte[] getDeviceEk()
    {
        return ((TPM2B_PUBLIC_KEY_RSA)ekPub.unique).buffer;
    }

    @Override
    public byte[] getDeviceSRK()
    {
        return ((TPM2B_PUBLIC_KEY_RSA)srkPub.unique).buffer;
    }
}
