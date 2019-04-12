/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.parser;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Class that represents the REST API format for DeviceRegistrationResult
 * Format : https://docs.microsoft.com/en-us/rest/api/iot-dps/RuntimeRegistration/RegisterDevice#definitions_deviceregistrationresult
 */
public class DeviceRegistrationResultParser
{
    private static final String REGISTRATION_ID = "registrationId";
    @SerializedName(REGISTRATION_ID)
    private String registrationId;

    private static final String CREATED_DATE_TIME_UTC = "createdDateTimeUtc";
    @SerializedName(CREATED_DATE_TIME_UTC)
    private String createdDateTimeUtc;

    private static final String ASSIGNED_HUB = "assignedHub";
    @SerializedName(ASSIGNED_HUB)
    private String assignedHub;

    private static final String DEVICE_ID = "deviceId";
    @SerializedName(DEVICE_ID)
    private String deviceId;

    private static final String STATUS = "status";
    @SerializedName(STATUS)
    private String status;

    private static final String ETAG = "etag";
    @SerializedName(ETAG)
    private String eTag;

    private static final String LAST_UPDATES_DATE_TIME_UTC = "lastUpdatedDateTimeUtc";
    @SerializedName(LAST_UPDATES_DATE_TIME_UTC)
    private String lastUpdatesDateTimeUtc;

    private static final String ERROR_CODE = "errorCode";
    @SerializedName(ERROR_CODE)
    private Integer errorCode;

    private static final String ERROR_MESSAGE = "errorMessage";
    @SerializedName(ERROR_MESSAGE)
    private String errorMessage;

    private static final String TPM = "tpm";
    @SerializedName(TPM)
    private TpmRegistrationResultParser tpm;

    private static final String X509 = "x509";
    @SerializedName(X509)
    private X509RegistrationResultParser x509;

    private static final String PAYLOAD = "payload";
    @SerializedName(PAYLOAD)
    private JsonObject jsonPayload;

    //empty constructor for Gson
    DeviceRegistrationResultParser()
    {
    }

    /**
     * Getter for Registration Id
     * @return Getter for Registration Id
     */
    public String getRegistrationId()
    {
        //SRS_DeviceRegistrationResultParser_25_001: [ This method shall return the parsed registrationId. ]
        return registrationId;
    }

    /**
     * Getter for CreatedDateTimeUtc
     * @return Getter for CreatedDateTimeUtc
     */
    public String getCreatedDateTimeUtc()
    {
        //SRS_DeviceRegistrationResultParser_25_002: [ This method shall return the parsed createdDateTimeUtc. ]
        return createdDateTimeUtc;
    }

    /**
     * Getter for Assigned Iot Hub
     * @return Getter for Assigned Iot Hub
     */
    public String getAssignedHub()
    {
        //SRS_DeviceRegistrationResultParser_25_003: [ This method shall return the parsed assignedHub. ]
        return assignedHub;
    }

    /**
     * Getter for Device Id
     * @return Getter for Device Id
     */
    public String getDeviceId()
    {
        //SRS_DeviceRegistrationResultParser_25_004: [ This method shall return the parsed deviceId. ]
        return deviceId;
    }

    /**
     * Getter for Status
     * @return Getter for Status
     */
    public String getStatus()
    {
        //SRS_DeviceRegistrationResultParser_25_005: [ This method shall return the parsed status. ]
        return status;
    }

    /**
     * Getter for Etag
     * @return Getter for Etag
     */
    public String getEtag()
    {
        //SRS_DeviceRegistrationResultParser_25_006: [ This method shall return the parsed eTag. ]
        return eTag;
    }

    /**
     * Getter for LastUpdatesDateTimeUtc
     * @return Getter for LastUpdatesDateTimeUtc
     */
    public String getLastUpdatesDateTimeUtc()
    {
        //SRS_DeviceRegistrationResultParser_25_007: [ This method shall return the parsed lastUpdatesDateTimeUtc. ]
        return lastUpdatesDateTimeUtc;
    }

    /**
     * Getter for the object TpmRegistrationResultParser
     * https://docs.microsoft.com/en-us/rest/api/iot-dps/RuntimeRegistration/RegisterDevice#definitions_tpmregistrationresult
     * @return Getter for the object TpmRegistrationResultParser
     */
    public TpmRegistrationResultParser getTpm()
    {
        //SRS_DeviceRegistrationResultParser_25_008: [ This method shall return the parsed TpmRegistrationResultParser Object. ]
        return tpm;
    }

    /**
     * Getter for the object X509RegistrationResultParser
     * https://docs.microsoft.com/en-us/rest/api/iot-dps/RuntimeRegistration/RegisterDevice#definitions_x509registrationresult
     * @return   Getter for the object X509RegistrationResultParser
     */
    public X509RegistrationResultParser getX509()
    {
        //SRS_DeviceRegistrationResultParser_25_009: [ This method shall return the parsed X509RegistrationResultParser object. ]
        return x509;
    }

    /**
     * Getter for Error Code
     * @return Getter for Error Code
     */
    public Integer getErrorCode()
    {
        //SRS_DeviceRegistrationResultParser_25_010: [ This method shall return the parsed errorCode. ]
        return errorCode;
    }

    /**
     * Getter for Error Message
     * @return Getter for Error Message
     */
    public String getErrorMessage()
    {
        //SRS_DeviceRegistrationResultParser_25_011: [ This method shall return the parsed errorMessage. ]
        return errorMessage;
    }

    public String getPayload()
    {
        return jsonPayload.toString();
    }
}
