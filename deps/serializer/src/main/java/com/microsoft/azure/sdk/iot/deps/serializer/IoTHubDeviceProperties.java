// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * IoT Hub device properties representation as described on session 7.4.1 of IoTGatewayPMspecs
 */
public class IoTHubDeviceProperties
{
    public enum DeviceStatusEnum {
        enabled,
        disabled
    }

    private static final Gson gson = new GsonBuilder().create();

    private static final String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'";
    private static final String TIMEZONE = "UTC";

    /**
     * required
     *
     * A case-sensitive string ( up to 128 char long) of ASCII 7-bit alphanumeric
     * chars + {'-', ':', '/', '\', '.', '+', '%', '_', '#', '*', '?', '!', '(',
     * ')', ',', '=', '@', ';', '$', '''}. Non-alphanumeric characters are from URN RFC.
     */
    protected String deviceId;

    /**
     * required
     *
     * A case-sensitive string (up to 128 char long). This is used to identify devices with
     * the same deviceId when they have been deleted and recreated. Currently implemented as GUID.
     */
    protected String generationId;

    /**
     * required
     *
     * A string representing a weak etag version of this json description. This is a hash.
     */
    protected String etag;
    private static Integer etagval;

    /**
     * required
     *
     * “Enabled”, “Disabled”. If “Enabled”, this device is authorized to connect. If “Disabled”
     * this device cannot receive or send messages, and statusReason has to be set. Note: Service
     * can still send C2D msgs to the device.
     */
    protected DeviceStatusEnum status;

    /**
     * optional
     *
     * A 128 char long string storing the reason of suspension. (all UTF-8 char allowed)
     */
    protected String statusReason;

    /**
     * optional
     *
     * Datetime of last time the state was updated
     */
    protected String statusUpdateTime;


    public IoTHubDeviceProperties(String deviceId, String generationId) throws IllegalArgumentException
    {
        /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_040: [All strings shall not be null.] */
        if((deviceId == null) || (generationId == null)){
            throw new IllegalArgumentException("Provided string not initialized");
        }

        /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_003: [The constructor shall set the etag as `1`.] */
        etagval = 0;
        etag = "0";   //SetDevice() will add 1.

        /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_001: [The constructor shall receive the deviceId and store it using the SetDevice.] */
        /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_002: [The constructor shall receive the generationId and store it using the SetGeneration.] */
        /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_007: [If one of the parameters do not fit the criteria, the constructor shall throw IllegalArgumentException.] */
        SetDevice(deviceId, generationId);

        /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_004: [The constructor shall set the device status as enabled.] */
        status = DeviceStatusEnum.enabled;

        /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_005: [The constructor shall store the string `provisioned` in the statusReason.] */
        statusReason = "provisioned";

        /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_006: [The constructor shall store the current date and time in statusUpdateTime.] */
        UpdateStatusTime();
    }


    public void SetDevice(String deviceId, String generationId) throws IllegalArgumentException {

        /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_011: [If the provided name is null, the SetDevice not change the deviceId.] */
        if(deviceId != null) {
            /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_012: [If the provided name do not fits the json criteria the SetDevice shall throw IllegalArgumentException.] */
            ValidateString(deviceId);

            /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_008: [The SetDevice shall receive the device name and store copy it into the deviceId.] */
            this.deviceId = deviceId;
        }

        /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_014: [If the provided generation is null, the SetDevice shall not change the generationId.] */
        if(generationId != null) {
            /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_015: [If the provided generation do not fits the json criteria, the SetDevice shall throw IllegalArgumentException.] */
            ValidateString(generationId);

            /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_009: [The SetDevice shall receive the device generation and store copy it into the generationId.] */
            this.generationId = generationId;
        }

        /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_010: [The SetDevice shall increment the etag by `1`.] */
        this.etagval++;
        this.etag  = this.etagval.toString();
    }

    public void EnableDevice()
    {
        /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_013: [If the device is already enable, the EnableDevice shall not do anything.] */
        if(status == DeviceStatusEnum.disabled)
        {
            /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_017: [The EnableDevice shall set the device as `enabled`.] */
            status = DeviceStatusEnum.enabled;

            /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_018: [The EnableDevice shall store the string `provisioned` in the statusReason.] */
            statusReason = "provisioned";

            /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_019: [The EnableDevice shall increment the etag by `1`.] */
            etagval++;
            etag  = etagval.toString();

            /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_020: [The EnableDevice shall store the current date and time in statusUpdateTime.] */
            UpdateStatusTime();
        }
    }

    public void DisableDevice(String reason) throws IllegalArgumentException
    {
        /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_025: [If the provided reason is null, the DisableDevice shall throw IllegalArgumentException.] */
        if(reason == null) {
            throw new IllegalArgumentException("Provided string not initialized");
        }

        /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_026: [If the provided reason do not fits the json criteria, the DisableDevice shall throw IllegalArgumentException.] */
        ValidateString(reason);

        /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_027: [If the device is already disabled, the DisableDevice shall not do anything.] */
        if(status == DeviceStatusEnum.enabled)
        {
            /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_021: [The DisableDevice shall set the device as `disabled`.] */
            status = DeviceStatusEnum.disabled;

            /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_022: [The DisableDevice shall store the provided reason in the statusReason.] */
            statusReason = reason;

            /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_023: [The DisableDevice shall increment the etag by `1`.] */
            etagval++;
            etag  = etagval.toString();

            /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_024: [The DisableDevice shall store the current date and time in statusUpdateTime.] */
            UpdateStatusTime();
        }
    }

    public String toJson()
    {
        /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_028: [The toJson shall create a String with information in the IoTHubDeviceProperties using json format.] */
        /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_029: [The toJson shall not include null fields.] */
        return gson.toJson(this);
    }

    /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_030: [The fromJson shall fill the fields in IoTHubDeviceProperties with the values provided in the json string.] */
    /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_031: [The fromJson shall not change fields that is not reported in the json string.] */  
    public void fromJson(String json)
    {
        IoTHubDeviceProperties newValues = gson.fromJson(json, IoTHubDeviceProperties.class);
        copy(newValues);
    }

    public void copy(IoTHubDeviceProperties newValues)
    {
        deviceId = newValues.GetDeviceId();
        generationId = newValues.GetGenerationId();
        etag = newValues.GetETag();
        status = newValues.GetStatus();
        statusReason = newValues.GetStatusReason();
        statusUpdateTime = newValues.GetStatusUpdateTime();
    }

    private void ValidateString(String str)
    {
        /* Tests_SRS_IOTHUB_DEVICEPROPERTIES_21_039: [All strings shall be up to 128 char long.] */
        if(str.length()>128) {
            throw new IllegalArgumentException("Provided string is too big for json");
        }
        // TODO: Add test for URN chars in the deviceId.
    }

    private void UpdateStatusTime()
    {
        /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_038: [All data and time shall use ISO8601 UTC format.] */
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        statusUpdateTime = dateFormat.format(new Date());
    }

    /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_032: [The GetDeviceId shall return a string with the device name stored in the deviceId.] */
    public String GetDeviceId(){
        return this.deviceId;
    }

    /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_033: [The GetGenerationId shall return a string with the device generation stored in the generationId.] */
    public String GetGenerationId(){
        return this.generationId;
    }

    /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_034: [The GetETag shall return a string with the last message ETag stored in the etag.] */
    public String GetETag(){
        return this.etag;
    }

    /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_035: [The GetStatus shall return the device status stored in the status.] */
    public DeviceStatusEnum GetStatus(){
        return this.status;
    }

    /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_036: [The GetStatusReason shall return a string with the status reason stored in the statusReason.] */
    public String GetStatusReason(){
        return this.statusReason;
    }

    /* Codes_SRS_IOTHUB_DEVICEPROPERTIES_21_037: [The GetStatusUpdateTime shall return a string with the last status update time stored in the statusUpdateTime.] */
    public String GetStatusUpdateTime(){
        return this.statusUpdateTime;
    }

}
