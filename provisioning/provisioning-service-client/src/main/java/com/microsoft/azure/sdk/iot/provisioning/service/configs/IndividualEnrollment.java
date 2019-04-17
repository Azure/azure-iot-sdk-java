// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.twin.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.Tools;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;

import java.util.Collection;
import java.util.Date;

/**
 * Representation of a single Device Provisioning Service enrollment with a JSON serializer and deserializer.
 *
 * <p> This object is used to send IndividualEnrollment information to the provisioning service, or receive IndividualEnrollment
 * information from the provisioning service.</p>
 *
 * <p> To create or update an IndividualEnrollment on the provisioning service you should fill this object and call the
 * public API {@link ProvisioningServiceClient#createOrUpdateIndividualEnrollment(IndividualEnrollment)}.
 * The minimum information required by the provisioning service is the {@code registrationId} and the
 * {@code attestation}.</p>
 *
 * <p> A new device can be provisioned by three attestation mechanisms, Trust Platform Module (see {@link TpmAttestation}),
 * X509 (see {@link X509Attestation}) or {@link SymmetricKeyAttestation} mechanism. The definition of each one you should use depending on the
 * physical authentication hardware that the device contains.</p>
 *
 * <p> The content of this class will be serialized in a JSON format and sent as a body of the rest API to the
 * provisioning service.</p>
 *
 * <p> When serialized, an IndividualEnrollment will look like the following example:</p>
 *
 * <pre>
 * {@code
 * {
 *    "registrationId":"validRegistrationId",
 *    "deviceId":"ContosoDevice-123",
 *    "attestation":{
 *        "type":"tpm",
 *        "tpm":{
 *            "endorsementKey":"validEndorsementKey"
 *        }
 *    },
 *    "iotHubHostName":"ContosoIoTHub.azure-devices.net",
 *    "provisioningStatus":"enabled"
 * }
 * }
 * </pre>
 *
 * <p> The content of this class can be filled by a JSON, received from the provisioning service, as result of a
 * IndividualEnrollment operation like create, update, or query enrollment.</p>
 *
 * <p> The following JSON is a sample or the IndividualEnrollment response, received from the provisioning service.</p>
 *
 * <pre>
 * {@code
 * {
 *    "registrationId":"validRegistrationId",
 *    "deviceId":"ContosoDevice-123",
 *    "attestation":{
 *        "type":"tpm",
 *        "tpm":{
 *            "endorsementKey":"validEndorsementKey"
 *        }
 *    },
 *    "iotHubHostName":"ContosoIoTHub.azure-devices.net",
 *    "provisioningStatus":"enabled"
 *    "createdDateTimeUtc": "2017-09-28T16:29:42.3447817Z",
 *    "lastUpdatedDateTimeUtc": "2017-09-28T16:29:42.3447817Z",
 *    "etag": "\"00000000-0000-0000-0000-00000000000\""
 * }
 * }
 * </pre>
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 */
public class IndividualEnrollment extends Serializable
{
    // the registration identifier
    private static final String REGISTRATION_ID_TAG = "registrationId";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(REGISTRATION_ID_TAG)
    private String registrationId;

    // the device identifier
    private static final String DEVICE_ID_TAG = "deviceId";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(DEVICE_ID_TAG)
    private String deviceId;

    // the device registration state
    private static final String DEVICE_REGISTRATION_STATE_TAG = "registrationState";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(DEVICE_REGISTRATION_STATE_TAG)
    private DeviceRegistrationState registrationState;

    // the attestation
    private static final String ATTESTATION_TAG = "attestation";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(ATTESTATION_TAG)
    private AttestationMechanism attestation;

    // the iothub host name
    private static final String IOTHUB_HOST_NAME_TAG = "iotHubHostName";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(IOTHUB_HOST_NAME_TAG)
    private String iotHubHostName;

    // the initial Twin state identifier (Twin is a special case and will be manually serialized).
    private static final String INITIAL_TWIN_STATE_TAG = "initialTwin";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(INITIAL_TWIN_STATE_TAG)
    private TwinState initialTwin;

    // the provisioning status
    private static final String PROVISIONING_STATUS_TAG = "provisioningStatus";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(PROVISIONING_STATUS_TAG)
    private ProvisioningStatus provisioningStatus;

    // the datetime this resource was created
    private static final String CREATED_DATETIME_UTC_TAG = "createdDateTimeUtc";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(CREATED_DATETIME_UTC_TAG)
    private String createdDateTimeUtc = null;
    @Expose(serialize = false, deserialize = false)
    private Date createdDateTimeUtcDate;

    // the datetime this resource was last updated
    private static final String LAST_UPDATED_DATETIME_UTC_TAG = "lastUpdatedDateTimeUtc";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(LAST_UPDATED_DATETIME_UTC_TAG)
    private String lastUpdatedDateTimeUtc = null;
    private transient Date lastUpdatedDateTimeUtcDate;

    // the eTag
    private static final String ETAG_TAG = "etag";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(ETAG_TAG)
    private String etag;

    private static final String DEVICE_CAPABILITIES_TAG = "capabilities";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(DEVICE_CAPABILITIES_TAG)
    private DeviceCapabilities capabilities = new DeviceCapabilities();

    // the reprovisioning policy
    private static final String REPROVISION_POLICY_TAG = "reprovisionPolicy";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(REPROVISION_POLICY_TAG)
    private ReprovisionPolicy reprovisionPolicy;

    // the custom allocation definition
    private static final String CUSTOM_ALLOCATION_DEFINITION_TAG = "customAllocationDefinition";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(CUSTOM_ALLOCATION_DEFINITION_TAG)
    private CustomAllocationDefinition customAllocationDefinition;

    // the allocation policy of the resource. overrides the tenant level allocation policy
    private static final String ALLOCATION_POLICY_TAG = "allocationPolicy";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(ALLOCATION_POLICY_TAG)
    private AllocationPolicy allocationPolicy;

    // the list of names of IoT hubs the device in this resource can be allocated to. Must be a subset of tenant level list of IoT hubs
    private static final String IOT_HUBS_TAG = "iotHubs";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(IOT_HUBS_TAG)
    private Collection<String> iotHubs;

    /**
     * CONSTRUCTOR
     *
     * <p> This constructor creates an instance of the enrollment with the minimum set of information
     * required by the provisioning service. A valid enrollment must contain the registrationId,
     * which uniquely identify this enrollment, and the attestation mechanism, which can be TPM, X509 or SymmetricKey.</p>
     *
     * <p> Other parameters can be added by calling the setters on this class.</p>
     *
     * <p> When serialized, an IndividualEnrollment will look like the following example:</p>
     *
     * <pre>
     * {@code
     * {
     *    "registrationId":"validRegistrationId",
     *    "attestation":{
     *        "type":"tpm",
     *        "tpm":{
     *            "endorsementKey":"validEndorsementKey"
     *        }
     *    }
     * }
     * }
     * </pre>
     *
     * @param registrationId the {@code String} with an unique id for this enrollment.
     * @param attestation    the {@link Attestation} mechanism that can be {@link TpmAttestation}, {@link X509Attestation} or {@link SymmetricKeyAttestation}.
     * @throws IllegalArgumentException If one of the provided parameters is not correct.
     */
    public IndividualEnrollment(String registrationId, Attestation attestation)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_001: [The constructor shall judge and store the provided parameters using the IndividualEnrollment setters.] */
        this.setRegistrationId(registrationId);
        this.setAttestation(attestation);
    }

    /**
     * CONSTRUCTOR
     *
     * <p> This constructor creates an instance of the enrollment filling the class with the information
     * provided in the JSON. It is used by the SDK to parse enrollment responses from the provisioning service.</p>
     *
     * <p> The following JSON is a sample of the IndividualEnrollment response, received from the provisioning service.</p>
     *
     * <pre>
     * {@code
     * {
     *    "registrationId":"validRegistrationId",
     *    "deviceId":"ContosoDevice-123",
     *    "attestation":{
     *        "type":"tpm",
     *        "tpm":{
     *            "endorsementKey":"validEndorsementKey"
     *        }
     *    },
     *    "iotHubHostName":"ContosoIoTHub.azure-devices.net",
     *    "provisioningStatus":"enabled"
     *    "createdDateTimeUtc": "2017-09-28T16:29:42.3447817Z",
     *    "lastUpdatedDateTimeUtc": "2017-09-28T16:29:42.3447817Z",
     *    "etag": "\"00000000-0000-0000-0000-00000000000\""
     * }
     * }
     * </pre>
     *
     * @param json the {@code String} with the JSON received from the provisioning service.
     * @throws IllegalArgumentException If the provided JSON is null, empty, or invalid.
     */
    public IndividualEnrollment(String json)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_002: [The constructor shall throw IllegalArgumentException if the JSON is null or empty.] */
        if (Tools.isNullOrEmpty(json))
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_21_003: [The constructor shall throw JsonSyntaxException if the JSON is invalid.] */
        /* SRS_INDIVIDUAL_ENROLLMENT_21_004: [The constructor shall deserialize the provided JSON for the enrollment class and subclasses.] */
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        IndividualEnrollment result = gson.fromJson(json, IndividualEnrollment.class);

        /* SRS_INDIVIDUAL_ENROLLMENT_21_005: [The constructor shall judge and store the provided mandatory parameters `registrationId` and `attestation` using the IndividualEnrollment setters.] */
        this.setRegistrationId(result.registrationId);
        this.setAttestation(result.attestation);

        /* SRS_INDIVIDUAL_ENROLLMENT_21_006: [If the `deviceId`, `iotHubHostName`, `provisioningStatus`, or `registrationState` is not null, the constructor shall judge and store it using the IndividualEnrollment setter.] */
        if (result.deviceId != null)
        {
            this.setDeviceIdFinal(result.deviceId);
        }
        if (result.iotHubHostName != null)
        {
            this.setIotHubHostNameFinal(result.iotHubHostName);
        }
        if (result.provisioningStatus != null)
        {
            this.setProvisioningStatusFinal(result.provisioningStatus);
        }
        if (result.registrationState != null)
        {
            this.setRegistrationState(result.registrationState);
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_21_007: [If the initialTwin is not null, the constructor shall convert the raw Twin and store it.] */
        if (result.initialTwin != null)
        {
            /*
             * During the deserialization process, the GSON will convert both tags and
             * properties to a raw Map, which will includes the $version and $metadata
             * as part of the collection. So, we need to reorganize this map using the
             * TwinCollection format. This constructor will do that.
             */
            this.initialTwin = new TwinState(result.initialTwin.getTags(), result.initialTwin.getDesiredProperty());
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_21_009: [If the createdDateTimeUtc is not null, the constructor shall judge and store it using the IndividualEnrollment setter.] */
        if (result.createdDateTimeUtc != null)
        {
            this.setCreatedDateTimeUtc(result.createdDateTimeUtc);
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_21_010: [If the lastUpdatedDateTimeUtc is not null, the constructor shall judge and store it using the IndividualEnrollment setter.] */
        if (result.lastUpdatedDateTimeUtc != null)
        {
            this.setLastUpdatedDateTimeUtc(result.lastUpdatedDateTimeUtc);
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_21_011: [If the etag is not null, the constructor shall judge and store it using the IndividualEnrollment setter.] */
        if (result.etag != null)
        {
            this.setEtagFinal(result.etag);
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_34_052: [If the device capabilities is not null, the constructor shall judge and store it using the IndividualEnrollment setter.] */
        if (result.capabilities != null)
        {
            this.setCapabilitiesFinal(result.capabilities);
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_34_066: [This function shall set the iothubs list to the value from the json.] */
        this.setIotHubs(result.getIotHubs());

        /* SRS_INDIVIDUAL_ENROLLMENT_34_067: [This function shall set the allocation policy to the value from the json.] */
        this.setAllocationPolicy(result.getAllocationPolicy());

        /* SRS_INDIVIDUAL_ENROLLMENT_34_068: [This function shall set the custom allocation definition to the value from the json.] */
        this.setCustomAllocationDefinition(result.getCustomAllocationDefinition());

        /* SRS_INDIVIDUAL_ENROLLMENT_34_069: [This function shall set the reprovision policy to the value from the json.] */
        this.setReprovisionPolicy(result.getReprovisionPolicy());
    }

    /**
     * Serializer
     *
     * <p>Creates a {@code JsonElement}, which the content represents
     * the information in this class and its subclasses in a JSON format.</p>
     *
     * <p>This is useful if the caller will integrate this JSON with JSON from
     * other classes to generate a consolidated JSON.</p>
     *
     * @return The {@code JsonElement} with the content of this class.
     */
    public JsonElement toJsonElement()
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_013: [The toJsonElement shall return a JsonElement with the information in this class in a JSON format.] */
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        JsonObject enrollmentJson = gson.toJsonTree(this).getAsJsonObject();

        /* SRS_INDIVIDUAL_ENROLLMENT_21_014: [If the initialTwin is not null, the toJsonElement shall include its content in the final JSON.] */
        if (initialTwin != null)
        {
            enrollmentJson.add(INITIAL_TWIN_STATE_TAG, initialTwin.toJsonElement());
        }

        return enrollmentJson;
    }

    /**
     * Getter for the registrationId.
     *
     * @return The {@code String} with the registrationID content. It cannot be {@code null} or empty.
     */
    public String getRegistrationId()
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_016: [The getRegistrationId shall return a String with the stored registrationId.] */
        return this.registrationId;
    }

    /**
     * Setter for the registrationId.
     *
     * <p>A valid registration Id shall follow this criteria.
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.</p>
     *
     * @param registrationId the {@code String} with the new registrationId. It cannot be {@code null}, empty, or invalid.
     * @throws IllegalArgumentException If the provided registrationId is {@code null}, empty, or invalid.
     */
    protected final void setRegistrationId(String registrationId)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_017: [The setRegistrationId shall throw IllegalArgumentException if the provided registrationId is null, empty, or invalid.] */
        ParserUtility.validateId(registrationId);

        /* SRS_INDIVIDUAL_ENROLLMENT_21_018: [The setRegistrationId shall store the provided registrationId.] */
        this.registrationId = registrationId;
    }

    /**
     * Getter for the deviceId.
     *
     * @return The {@code String} with the deviceID content. It cannot be {@code null} or empty.
     */
    public String getDeviceId()
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_019: [The getDeviceId shall return a String with the stored deviceId.] */
        return this.deviceId;
    }

    /**
     * Setter for the deviceId.
     *
     *
     * @deprecated as of provisioning-service-client version 1.3.3, please use {@link #setDeviceIdFinal(String)}
     *
     * @param deviceId the {@code String} with the new deviceID. It cannot be {@code null}, empty, or invalid.
     * @throws IllegalArgumentException If the provided deviceId is {@code null}, empty, or invalid.
     */
    @Deprecated
    public void setDeviceId(String deviceId)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_021: [The setDeviceId shall store the provided deviceId.] */
        setDeviceIdFinal(deviceId);
    }

    /**
     * Setter for the deviceId.
     *
     *
     * @param deviceId the {@code String} with the new deviceID. It cannot be {@code null}, empty, or invalid.
     * @throws IllegalArgumentException If the provided deviceId is {@code null}, empty, or invalid.
     */
    public final void setDeviceIdFinal(String deviceId)
    {
        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("Invalid deviceId parameter specified");
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_21_021: [The setDeviceId shall store the provided deviceId.] */
        this.deviceId = deviceId;
    }

    /**
     * Getter for the registrationState.
     *
     * @return The {@code DeviceRegistrationState} with the registrationState content. It can be {@code null}.
     */
    public DeviceRegistrationState getDeviceRegistrationState()
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_022: [The getDeviceRegistrationState shall return a DeviceRegistrationState with the stored registrationState.] */
        return this.registrationState;
    }

    /**
     * Setter for the registrationState.
     *
     * @param registrationState the {@code DeviceRegistrationState} with the new registrationState. It cannot be {@code null}.
     * @throws IllegalArgumentException If the provided registrationState is {@code null}.
     * @see DeviceRegistrationState
     */
    protected final void setRegistrationState(DeviceRegistrationState registrationState)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_023: [The setRegistrationState shall throw IllegalArgumentException if the provided registrationState is null.] */
        ParserUtility.validateObject(registrationState);

        /* SRS_INDIVIDUAL_ENROLLMENT_21_024: [The setRegistrationState shall store the provided registrationState.] */
        this.registrationState = registrationState;
    }

    /**
     * Getter for the attestation mechanism.
     *
     * @return The {@code Attestation} with the attestation content. It cannot be {@code null}.
     * @throws ProvisioningServiceClientException If the type of the attestation mechanism is unknown.
     */
    public Attestation getAttestation() throws ProvisioningServiceClientException
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_025: [The getAttestation shall return a AttestationMechanism with the stored attestation.] */
        return this.attestation.getAttestation();
    }

    /**
     * Setter for the attestation.
     *
     * <p>
     * Attestation mechanism is a mandatory parameter that provides the mechanism
     * type and the necessary keys/certificates</p>
     *
     * @param attestationMechanism the {@code AttestationMechanism} with the new attestation mechanism. It can be `tpm`, `x509` or `SymmetricKey`.
     * @throws IllegalArgumentException If the provided attestation mechanism is {@code null} or invalid.
     * @see AttestationMechanism
     */
    protected final void setAttestation(AttestationMechanism attestationMechanism)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_026: [The setAttestation shall throw IllegalArgumentException if the attestation is null or invalid.] */
        ParserUtility.validateObject(attestationMechanism);

        /* SRS_INDIVIDUAL_ENROLLMENT_21_027: [The setAttestation shall store the provided attestation.] */
        try
        {
            this.setAttestation(attestationMechanism.getAttestation());
        }
        catch (ProvisioningServiceClientException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Setter for the attestation.
     *
     * <p>
     * Attestation mechanism is a mandatory parameter that provides the mechanism
     * type and the necessary keys/certificates</p>
     *
     * @param attestation the {@link Attestation} with the new attestation mechanism. It can be {@link TpmAttestation}, {@link X509Attestation} or {@link SymmetricKeyAttestation}.
     * @throws IllegalArgumentException If the provided attestation mechanism is {@code null}.
     * @see Attestation
     * @see TpmAttestation
     * @see X509Attestation
     * @see SymmetricKeyAttestation
     */
    public void setAttestation(Attestation attestation)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_050: [The setAttestation shall throw IllegalArgumentException if the attestation is null.] */
        if (attestation == null)
        {
            throw new IllegalArgumentException("attestation cannot be null");
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_21_051: [The setAttestation shall store the provided attestation using the AttestationMechanism object.] */
        this.attestation = new AttestationMechanism(attestation);
    }

    /**
     * Getter for the iotHubHostName.
     *
     * @return The {@code String} with the iotHubHostName content. It cannot be {@code null} or empty.
     */
    public String getIotHubHostName()
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_028: [The getIotHubHostName shall return a String with the stored iotHubHostName.] */
        return this.iotHubHostName;
    }

    /**
     * Setter for the iotHubHostName.
     *
     * <p>
     * A valid iothub host name shall follow this criteria.
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     * A valid host name shall have, at least 2 parts separated by '.'.
     * </p>
     *
     * @deprecated as of provisioning-service-client version 1.3.3, please use {@link #setIotHubHostNameFinal(String)}
     *
     * @param iotHubHostName the {@code String} with the new iotHubHostName. It cannot be {@code null}, empty, or invalid.
     * @throws IllegalArgumentException If the provided iotHubHostName is {@code null}, empty, or invalid.
     */
    @Deprecated
    public void setIotHubHostName(String iotHubHostName)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_029: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
        ParserUtility.validateHostName(iotHubHostName);

        /* SRS_INDIVIDUAL_ENROLLMENT_21_030: [The setIotHubHostName shall store the provided iotHubHostName.] */
        this.iotHubHostName = iotHubHostName;
    }

    /**
     * Setter for the iotHubHostName.
     *
     * <p>
     * A valid iothub host name shall follow this criteria.
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     * A valid host name shall have, at least 2 parts separated by '.'.
     * </p>
     *
     * @param iotHubHostName the {@code String} with the new iotHubHostName. It cannot be {@code null}, empty, or invalid.
     * @throws IllegalArgumentException If the provided iotHubHostName is {@code null}, empty, or invalid.
     */
    public final void setIotHubHostNameFinal(String iotHubHostName)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_029: [The setIotHubHostName shall throw IllegalArgumentException if the iotHubHostName is null, empty, or invalid.] */
        ParserUtility.validateHostName(iotHubHostName);

        /* SRS_INDIVIDUAL_ENROLLMENT_21_030: [The setIotHubHostName shall store the provided iotHubHostName.] */
        this.iotHubHostName = iotHubHostName;
    }

    /**
     * Getter for the initialTwin.
     *
     * @return The {@code TwinState} with the initialTwin content. Its optional and can be {@code null}.
     */
    public TwinState getInitialTwin()
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_031: [The getInitialTwin shall return a TwinState with the stored initialTwin.] */
        return this.initialTwin;
    }

    /**
     * Setter for the initialTwin.
     *
     * <p>
     * It provides a Twin precondition for the provisioned device.
     * </p>
     *
     * @param initialTwin the {@code TwinState} with the new initialTwin. It cannot be {@code null}.
     * @throws IllegalArgumentException If the provided initialTwin is {@code null}.
     */
    public void setInitialTwin(TwinState initialTwin)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_032: [The setInitialTwin shall throw IllegalArgumentException if the initialTwin is null.] */
        ParserUtility.validateObject(initialTwin);

        /* SRS_INDIVIDUAL_ENROLLMENT_21_033: [The setInitialTwin shall store the provided initialTwin.] */
        this.initialTwin = initialTwin;
    }

    /**
     * Getter for the provisioningStatus.
     *
     * @return The {@code ProvisioningStatus} with the provisioningStatus content. It can be 'enabled' or 'disabled'.
     */
    public ProvisioningStatus getProvisioningStatus()
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_034: [The getProvisioningStatus shall return a TwinState with the stored provisioningStatus.] */
        return this.provisioningStatus;
    }

    /**
     * Setter for the provisioningStatus.
     *
     * <p>
     * It provides a Status precondition for the provisioned device.
     * </p>
     *
     * @deprecated as of provisioning-service-client version 1.3.3, please use {@link #setProvisioningStatusFinal(ProvisioningStatus)}
     *
     * @param provisioningStatus the {@code ProvisioningStatus} with the new provisioningStatus. It cannot be {@code null}.
     * @throws IllegalArgumentException If the provided provisioningStatus is {@code null}.
     */
    @Deprecated
    public void setProvisioningStatus(ProvisioningStatus provisioningStatus)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_035: [The setProvisioningStatus shall throw IllegalArgumentException if the provisioningStatus is null.] */
        ParserUtility.validateObject(provisioningStatus);

        /* SRS_INDIVIDUAL_ENROLLMENT_21_036: [The setProvisioningStatus shall store the provided provisioningStatus.] */
        this.provisioningStatus = provisioningStatus;
    }

    /**
     * Setter for the provisioningStatus.
     *
     * <p>
     * It provides a Status precondition for the provisioned device.
     * </p>
     *
     * @param provisioningStatus the {@code ProvisioningStatus} with the new provisioningStatus. It cannot be {@code null}.
     * @throws IllegalArgumentException If the provided provisioningStatus is {@code null}.
     */
    public final void setProvisioningStatusFinal(ProvisioningStatus provisioningStatus)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_035: [The setProvisioningStatus shall throw IllegalArgumentException if the provisioningStatus is null.] */
        ParserUtility.validateObject(provisioningStatus);

        /* SRS_INDIVIDUAL_ENROLLMENT_21_036: [The setProvisioningStatus shall store the provided provisioningStatus.] */
        this.provisioningStatus = provisioningStatus;
    }

    /**
     * Getter for the createdDateTimeUtcDate.
     *
     * @return The {@code Date} with the createdDateTimeUtcDate content. It can be {@code null}.
     */
    public Date getCreatedDateTimeUtc()
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_037: [The getCreatedDateTimeUtc shall return a Date with the stored createdDateTimeUtcDate.] */
        return this.createdDateTimeUtcDate;
    }

    /**
     * Setter for the createdDateTimeUtc.
     *
     * <p>
     * This Date and Time is provided by the provisioning service. If the enrollment is not created yet,
     * this string can represent an invalid Date. In this case, it will be ignored.</p>
     *
     * <p>
     * Example of the expected format:
     * {@code "2016-06-01T21:22:43.7996883Z"}
     * </p>
     *
     * @param createdDateTimeUtc the {@code String} with the new createdDateTimeUtc. It can be {@code null}, empty or not valid.
     */
    protected final void setCreatedDateTimeUtc(String createdDateTimeUtc)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_038: [The setCreatedDateTimeUtc shall parse the provided String as a Data and Time UTC.] */
        /* SRS_INDIVIDUAL_ENROLLMENT_21_039: [The setCreatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided createdDateTimeUtc] */
        this.createdDateTimeUtcDate = ParserUtility.getDateTimeUtc(createdDateTimeUtc);
    }

    /**
     * Getter for the lastUpdatedDateTimeUtcDate.
     *
     * @return The {@code Date} with the lastUpdatedDateTimeUtcDate content. It can be {@code null}.
     */
    public Date getLastUpdatedDateTimeUtc()
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_040: [The getLastUpdatedDateTimeUtc shall return a Date with the stored lastUpdatedDateTimeUtcDate.] */
        return this.lastUpdatedDateTimeUtcDate;
    }

    /**
     * Setter for the lastUpdatedDateTimeUtc.
     *
     * <p>
     * This Date and Time is provided by the provisioning service. If the enrollment is not created yet,
     * this string can represent an invalid Date. In this case, it will be ignored.</p>
     *
     * <p>
     * Example of the expected format:
     * {@code "2016-06-01T21:22:43.7996883Z"}
     * </p>
     *
     * @param lastUpdatedDateTimeUtc the {@code String} with the new lastUpdatedDateTimeUtc. It can be {@code null}, empty or not valid.
     */
    protected final void setLastUpdatedDateTimeUtc(String lastUpdatedDateTimeUtc)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_041: [The setLastUpdatedDateTimeUtc shall parse the provided String as a Data and Time UTC.] */
        /* SRS_INDIVIDUAL_ENROLLMENT_21_042: [The setLastUpdatedDateTimeUtc shall throw IllegalArgumentException if it cannot parse the provided lastUpdatedDateTimeUtc] */
        this.lastUpdatedDateTimeUtcDate = ParserUtility.getDateTimeUtc(lastUpdatedDateTimeUtc);
    }

    /**
     * Getter for the etag.
     *
     * @return The {@code String} with the etag content. It can be {@code null}.
     */
    public String getEtag()
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_046: [The getEtag shall return a String with the stored etag.] */
        return this.etag;
    }

    /**
     * Setter for the etag.
     *
     * @deprecated as of provisioning-service-client version 1.3.3, please use {@link #setEtagFinal(String)}
     *
     * @param etag the {@code String} with the new etag. It cannot be {@code null}, empty or invalid.
     * @throws IllegalArgumentException If the provided etag is {@code null}, empty or invalid.
     */
    @Deprecated
    public void setEtag(String etag)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_047: [The setEtag shall throw IllegalArgumentException if the etag is null, empty, or invalid.] */
        ParserUtility.validateStringUTF8(etag);

        /* SRS_INDIVIDUAL_ENROLLMENT_21_048: [The setEtag shall store the provided etag.] */
        this.etag = etag;
    }

    /**
     * Setter for the etag.
     *
     * @param etag the {@code String} with the new etag. It cannot be {@code null}, empty or invalid.
     * @throws IllegalArgumentException If the provided etag is {@code null}, empty or invalid.
     */
    public final void setEtagFinal(String etag)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_047: [The setEtag shall throw IllegalArgumentException if the etag is null, empty, or invalid.] */
        ParserUtility.validateStringUTF8(etag);

        /* SRS_INDIVIDUAL_ENROLLMENT_21_048: [The setEtag shall store the provided etag.] */
        this.etag = etag;
    }

    public DeviceCapabilities getCapabilities()
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_34_054: [This function shall return the saved capabilities.] */
        return this.capabilities;
    }

    /**
     * @deprecated as of provisioning-service-client version 1.3.3, please use {@link #setCapabilitiesFinal(DeviceCapabilities)}
     *
     * @param capabilities the device capabilities to set
     */
    @Deprecated
    public void setCapabilities(DeviceCapabilities capabilities)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_34_053: [This function shall save the provided capabilities.] */
        this.capabilities = capabilities;
    }

    /**
     * @param capabilities the device capabilities to set
     */
    public final void setCapabilitiesFinal(DeviceCapabilities capabilities)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_34_053: [This function shall save the provided capabilities.] */
        this.capabilities = capabilities;
    }

    /**
     * Getter for the reprovision policy.
     *
     * @return The {@code ReprovisionPolicy} with the reprovisionPolicy content.
     */
    public ReprovisionPolicy getReprovisionPolicy()
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_34_057: [This function shall get the reprovision policy.] */
        return this.reprovisionPolicy;
    }

    /**
     * Setter for the reprovision policy.
     *
     * @param reprovisionPolicy the {@code ReprovisionPolicy} with the behavior when a device is re-provisioned to an IoT hub.
     */
    public void setReprovisionPolicy(ReprovisionPolicy reprovisionPolicy)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_34_058: [This function shall set the reprovision policy.] */
        this.reprovisionPolicy = reprovisionPolicy;
    }

    /**
     * Getter for the allocation policy.
     *
     * @return The {@code AllocationPolicy} with the allocationPolicy content.
     */
    public AllocationPolicy getAllocationPolicy()
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_34_059: [This function shall get the allocation policy.] */
        return this.allocationPolicy;
    }

    /**
     * Setter for the allocation policy.
     *
     * @param allocationPolicy the {@code AllocationPolicy} with the allocation policy of this resource. Overrides the tenant level allocation policy.
     */
    public void setAllocationPolicy(AllocationPolicy allocationPolicy)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_34_060: [This function shall set the allocation policy.] */
        this.allocationPolicy = allocationPolicy;
    }

    /**
     * Getter for the list of IoTHub names that the device can be allocated to..
     *
     * @return The {@code AllocationPolicy} with the allocationPolicy content.
     */
    public Collection<String> getIotHubs()
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_34_061: [This function shall get the iothubs list.] */
        return this.iotHubs;
    }

    /**
     * Setter for the list of IotHubs available for allocation.
     *
     * @param iotHubs the {@code List<String>} of names of IoT hubs the device(s) in this resource can be allocated to. Must be a subset of tenant level list of IoT hubs
     */
    public void setIotHubs(Collection<String> iotHubs)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_34_062: [This function shall set the iothubs list.] */
        this.iotHubs = iotHubs;
    }

    /**
     * Getter for the custom allocation definition policy.
     *
     * @return The {@code CustomAllocationDefinition} policy.
     */
    public CustomAllocationDefinition getCustomAllocationDefinition()
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_34_063: [This function shall get the custom allocation definition.] */
        return this.customAllocationDefinition;
    }

    /**
     * Setter for the custom allocation definition policy.
     *
     * @param customAllocationDefinition the {@code CustomAllocationDefinition} with the custom allocation policy of this resource.
     */
    public void setCustomAllocationDefinition(CustomAllocationDefinition customAllocationDefinition)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_34_064: [This function shall set the custom allocation definition.] */
        this.customAllocationDefinition = customAllocationDefinition;
    }
    
    /**
     * Empty constructor
     * <p>
     * Used only by the tools that will deserialize this class.
     * </p>
     */
    @SuppressWarnings("unused")
    IndividualEnrollment()
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_21_049: [The IndividualEnrollment shall provide an empty constructor to make GSON happy.] */
    }
}
