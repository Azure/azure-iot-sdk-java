/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * Parses JSON which represent the RegistrationOperationStatus object.
 * Format : https://docs.microsoft.com/en-us/rest/api/iot-dps/RuntimeRegistration/RegisterDevice#definitions_registrationoperationstatus
 */
public class RegistrationOperationStatusParser
{
    private static final String OPERATION_ID = "operationId";
    @SerializedName(OPERATION_ID)
    private String operationId;

    private static final String STATUS = "status";
    @SerializedName(STATUS)
    private String status;

    private static final String REGISTRATION_STATE = "registrationState";
    @SerializedName(REGISTRATION_STATE)
    private DeviceRegistrationResultParser registrationState;

    //empty constructor for Gson
    private RegistrationOperationStatusParser()
    {
    }

    /**
     * Parses JSON which is of the following
     * format https://docs.microsoft.com/en-us/rest/api/iot-dps/RuntimeRegistration/RegisterDevice#definitions_registrationoperationstatus
     *
     * @param json JSON input to be parsed. Cannot be {@code null} or empty
     * @return The object which contains parsed information `RegistrationOperationStatusParser`
     * @throws IllegalArgumentException If any of the input parameters are invalid
     */
    public static RegistrationOperationStatusParser createFromJson(String json) throws IllegalArgumentException
    {
        if((json == null) || json.isEmpty())
        {
            //SRS_RegistrationOperationStatusParser_25_001: [ This method shall throw IllegalArgumentException if provided Json is null or empty. ]
            throw new IllegalArgumentException("JSON cannot be null or empty");
        }

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        RegistrationOperationStatusParser registrationOperationStatusParser = null;

        try
        {
            //SRS_RegistrationOperationStatusParser_25_002: [ This method shall parse the provided Json. ]
            registrationOperationStatusParser = gson.fromJson(json, RegistrationOperationStatusParser.class);
        }
        catch (JsonSyntaxException malformed)
        {
            //SRS_RegistrationOperationStatusParser_25_003: [ This method shall throw IllegalArgumentException if Json cannot be parsed. ]
            throw new IllegalArgumentException("Malformed JSON", malformed);
        }

        if (registrationOperationStatusParser.operationId == null || registrationOperationStatusParser.status == null)
        {
            //SRS_RegistrationOperationStatusParser_25_004: [ This method shall throw IllegalArgumentException if operationId cannot be parsed. ]
            throw new IllegalArgumentException("JSON does not contain Operation Id or Status");
        }

        if (registrationOperationStatusParser.registrationState != null)
        {
            if (registrationOperationStatusParser.registrationState.getRegistrationId() == null)
            {
                //SRS_RegistrationOperationStatusParser_25_005: [ This method shall throw IllegalArgumentException if Registration Id cannot be parsed. ]
                throw new IllegalArgumentException("Registration Id cannot be null in the result");
            }

            if (registrationOperationStatusParser.registrationState.getStatus() == null)
            {
                //SRS_RegistrationOperationStatusParser_25_006: [ This method shall throw IllegalArgumentException if status cannot be parsed. ]
                throw new IllegalArgumentException("Status cannot be null in the result");
            }

            if (registrationOperationStatusParser.registrationState.getX509() != null &&
                    registrationOperationStatusParser.registrationState.getX509().getCertificateInfo() != null )
            {
                X509RegistrationResultParser.X509CertificateInfo X509CertificateInfo = registrationOperationStatusParser.registrationState.getX509().getCertificateInfo();
                if (X509CertificateInfo.getIssuerName() == null)
                {
                    //SRS_RegistrationOperationStatusParser_25_007: [ This method shall throw IllegalArgumentException if Issuer Name from X509 Certificate Info cannot be parsed. ]
                    throw new IllegalArgumentException("Issuer Name is required for X509 flow");
                }
                if (X509CertificateInfo.getSubjectName() == null)
                {
                    //SRS_RegistrationOperationStatusParser_25_008: [ This method shall throw IllegalArgumentException if Subject Name from X509 Certificate Info cannot be parsed. ]
                    throw new IllegalArgumentException("Subject Name is required for X509 flow");
                }
                if (X509CertificateInfo.getSha1Thumbprint() == null)
                {
                    //SRS_RegistrationOperationStatusParser_25_009: [ This method shall throw IllegalArgumentException if Sha1 Thumbprint from X509 Certificate Info cannot be parsed. ]
                    throw new IllegalArgumentException("SHA1 Thumbprint is required for X509 flow");
                }

                if (X509CertificateInfo.getSha256Thumbprint() == null)
                {
                    //SRS_RegistrationOperationStatusParser_25_010: [ This method shall throw IllegalArgumentException if SHA256 Thumbprint  from X509 Certificate Info cannot be parsed. ]
                    throw new IllegalArgumentException("SHA256 Thumbprint is required for X509 flow");
                }

                if (X509CertificateInfo.getNotBeforeUtc() == null)
                {
                    //SRS_RegistrationOperationStatusParser_25_011: [ This method shall throw IllegalArgumentException if NotBeforeUtc from X509 Certificate Info cannot be parsed. ]
                    throw new IllegalArgumentException("Not before UTC time is required for X509 flow");
                }

                if (X509CertificateInfo.getNotAfterUtc() == null)
                {
                    //SRS_RegistrationOperationStatusParser_25_012: [ This method shall throw IllegalArgumentException if NotAfterUtc from X509 Certificate Info cannot be parsed. ]
                    throw new IllegalArgumentException("Not After UTC is required for X509 flow");
                }

                if (X509CertificateInfo.getSerialNumber() == null)
                {
                    //SRS_RegistrationOperationStatusParser_25_013: [ This method shall throw IllegalArgumentException if Serial Number from X509 Certificate Info cannot be parsed. ]
                    throw new IllegalArgumentException("Serial Number is required for X509 flow");
                }

                if (X509CertificateInfo.getVersion() == null)
                {
                    //SRS_RegistrationOperationStatusParser_25_014: [ This method shall throw IllegalArgumentException if version from X509 Certificate Info cannot be parsed. ]
                    throw new IllegalArgumentException("Version is required for X509 flow");
                }
            }

            if (registrationOperationStatusParser.registrationState.getX509() != null &&
                    registrationOperationStatusParser.registrationState.getX509().getSigningCertificateInfo() != null )
            {

                X509RegistrationResultParser.X509CertificateInfo X509CertificateInfo = registrationOperationStatusParser.registrationState.getX509().getSigningCertificateInfo();
                if (X509CertificateInfo.getIssuerName() == null)
                {
                    //SRS_RegistrationOperationStatusParser_25_015: [ This method shall throw IllegalArgumentException if Issuer Name from X509 Signing Certificate Info cannot be parsed. ]
                    throw new IllegalArgumentException("Issuer Name is required for X509 flow");
                }
                if (X509CertificateInfo.getSubjectName() == null)
                {
                    //SRS_RegistrationOperationStatusParser_25_016: [ This method shall throw IllegalArgumentException if Subject Name from X509 Signing Certificate Info cannot be parsed. ]
                    throw new IllegalArgumentException("Subject Name is required for X509 flow");
                }
                if (X509CertificateInfo.getSha1Thumbprint() == null)
                {
                    //SRS_RegistrationOperationStatusParser_25_017: [ This method shall throw IllegalArgumentException if Sha1 Thumbprint from X509 Signing Certificate Info cannot be parsed. ]
                    throw new IllegalArgumentException("SHA1 Thumbprint is required for X509 flow");
                }

                if (X509CertificateInfo.getSha256Thumbprint() == null)
                {
                    //SRS_RegistrationOperationStatusParser_25_018: [ This method shall throw IllegalArgumentException if SHA256 Thumbprint from X509 Signing Certificate Info cannot be parsed. ]
                    throw new IllegalArgumentException("SHA256 Thumbprint is required for X509 flow");
                }

                if (X509CertificateInfo.getNotBeforeUtc() == null)
                {
                    //SRS_RegistrationOperationStatusParser_25_019: [ This method shall throw IllegalArgumentException if Not before UTC time from X509 Signing Certificate Info cannot be parsed. ]
                    throw new IllegalArgumentException("Not before UTC time is required for X509 flow");
                }

                if (X509CertificateInfo.getNotAfterUtc() == null)
                {
                    //SRS_RegistrationOperationStatusParser_25_020: [ This method shall throw IllegalArgumentException if Not After UTC  from X509 Signing Certificate Info cannot be parsed. ]
                    throw new IllegalArgumentException("Not After UTC is required for X509 flow");
                }

                if (X509CertificateInfo.getSerialNumber() == null)
                {
                    //SRS_RegistrationOperationStatusParser_25_021: [ This method shall throw IllegalArgumentException if Serial Number from X509 Signing Certificate Info cannot be parsed. ]
                    throw new IllegalArgumentException("Serial Number is required for X509 flow");
                }

                if (X509CertificateInfo.getVersion() == null)
                {
                    //SRS_RegistrationOperationStatusParser_25_022: [ This method shall throw IllegalArgumentException if Version from X509 Signing Certificate Info cannot be parsed. ]
                    throw new IllegalArgumentException("Version is required for X509 flow");
                }
            }
        }
        return registrationOperationStatusParser;
    }

    /**
     * Getter for the Operation Id
     * @return Operation Id. Cannot be {@code null}
     */
    public String getOperationId()
    {
        //SRS_RegistrationOperationStatusParser_25_023: [ This method shall return operationId. ]
        return operationId;
    }

    /**
     * Getter for the Status
     * @return Status retrieved after parsing. Cannot be {@code null}
     */
    public String getStatus()
    {
        //SRS_RegistrationOperationStatusParser_25_024: [ This method shall return status . ]
        return status;
    }

    /**
     * Returns DeviceRegistrationResultParser object.
     * Format : https://docs.microsoft.com/en-us/rest/api/iot-dps/RuntimeRegistration/RegisterDevice#definitions_deviceregistrationresult
     *
     * @return DeviceRegistrationResultParser object
     */
    public DeviceRegistrationResultParser getRegistrationState()
    {
        //SRS_RegistrationOperationStatusParser_25_025: [ This method shall return DeviceRegistrationResultParser Object. ]
        return registrationState;
    }
}
