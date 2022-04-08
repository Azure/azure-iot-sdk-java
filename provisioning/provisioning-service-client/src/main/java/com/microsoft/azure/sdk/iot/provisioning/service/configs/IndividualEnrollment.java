// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import lombok.Getter;
import lombok.Setter;

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
@SuppressWarnings("unused") // A number of private members are unused but may be filled in or used by serialization
public class IndividualEnrollment extends Serializable
{
    private static final String REGISTRATION_ID_TAG = "registrationId";

    /**
     * The registration Id for this individual enrollment.
     *
     * <p>A valid registration Id shall follow this criteria.
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.</p>
     */
    @Expose
    @SerializedName(REGISTRATION_ID_TAG)
    @Getter
    @Setter
    private String registrationId;

    // the device identifier
    private static final String DEVICE_ID_TAG = "deviceId";
    @Expose
    @SerializedName(DEVICE_ID_TAG)
    @Getter
    @Setter
    private String deviceId;

    // the device registration state
    private static final String DEVICE_REGISTRATION_STATE_TAG = "registrationState";
    @Expose
    @SerializedName(DEVICE_REGISTRATION_STATE_TAG)
    @Getter
    @Setter
    private DeviceRegistrationState deviceRegistrationState;

    // optional device information.
    private static final String OPTIONAL_DEVICE_INFORMATION_TAG = "optionalDeviceInformation";
    @Expose
    @SerializedName(OPTIONAL_DEVICE_INFORMATION_TAG)
    private TwinCollection optionalDeviceInformation;

    // the attestation
    private static final String ATTESTATION_TAG = "attestation";
    @Expose
    @SerializedName(ATTESTATION_TAG)
    private AttestationMechanism attestation;

    private static final String IOTHUB_HOST_NAME_TAG = "iotHubHostName";

    /**
     * The hostname of the IoT hub for this individual enrollment.
     *
     * <p>
     * A valid IoT hub host name shall follow this criteria.
     * A case-sensitive string (up to 128 char long)
     * of ASCII 7-bit alphanumeric chars
     * + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     * A valid host name shall have, at least 2 parts separated by '.'.
     * </p>
     */
    @Expose
    @SerializedName(IOTHUB_HOST_NAME_TAG)
    @Getter
    @Setter
    private String iotHubHostName;

    // the initial Twin state identifier (Twin is a special case and will be manually serialized).
    private static final String INITIAL_TWIN_STATE_TAG = "initialTwin";
    @Expose
    @SerializedName(INITIAL_TWIN_STATE_TAG)
    @Getter
    @Setter
    private TwinState initialTwin;

    // the provisioning status
    private static final String PROVISIONING_STATUS_TAG = "provisioningStatus";
    @Expose
    @SerializedName(PROVISIONING_STATUS_TAG)
    @Getter
    @Setter
    private ProvisioningStatus provisioningStatus;

    // the datetime this resource was created
    private static final String CREATED_DATETIME_UTC_TAG = "createdDateTimeUtc";
    @Expose
    @SerializedName(CREATED_DATETIME_UTC_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String createdDateTimeUtcString;

    @Getter
    @Expose(serialize = false, deserialize = false)
    private transient Date createdDateTimeUtc;

    // the datetime this resource was last updated
    private static final String LAST_UPDATED_DATETIME_UTC_TAG = "lastUpdatedDateTimeUtc";
    @Expose
    @SerializedName(LAST_UPDATED_DATETIME_UTC_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String lastUpdatedDateTimeUtcString;

    @Getter
    @Expose(serialize = false, deserialize = false)
    private transient Date lastUpdatedDateTimeUtc;

    // the eTag
    private static final String ETAG_TAG = "etag";
    @Expose
    @SerializedName(ETAG_TAG)
    @Getter
    @Setter
    private String etag;

    private static final String DEVICE_CAPABILITIES_TAG = "capabilities";
    @Expose
    @SerializedName(DEVICE_CAPABILITIES_TAG)
    @Getter
    @Setter
    private DeviceCapabilities capabilities = new DeviceCapabilities();

    // the reprovisioning policy
    private static final String REPROVISION_POLICY_TAG = "reprovisionPolicy";
    @Expose
    @SerializedName(REPROVISION_POLICY_TAG)
    @Getter
    @Setter
    private ReprovisionPolicy reprovisionPolicy;

    // the custom allocation definition
    private static final String CUSTOM_ALLOCATION_DEFINITION_TAG = "customAllocationDefinition";
    @Expose
    @SerializedName(CUSTOM_ALLOCATION_DEFINITION_TAG)
    @Getter
    @Setter
    private CustomAllocationDefinition customAllocationDefinition;

    // the allocation policy of the resource. overrides the tenant level allocation policy
    private static final String ALLOCATION_POLICY_TAG = "allocationPolicy";
    @Expose
    @SerializedName(ALLOCATION_POLICY_TAG)
    @Getter
    @Setter
    private AllocationPolicy allocationPolicy;

    // the list of names of IoT hubs the device in this resource can be allocated to. Must be a subset of tenant level list of IoT hubs
    private static final String IOT_HUBS_TAG = "iotHubs";
    @Expose
    @SerializedName(IOT_HUBS_TAG)
    @Getter
    @Setter
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
        if (json == null || json.isEmpty())
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        IndividualEnrollment result = gson.fromJson(json, IndividualEnrollment.class);

        this.setRegistrationId(result.registrationId);
        this.setAttestation(result.attestation);

        if (result.deviceId != null)
        {
            this.setDeviceId(result.deviceId);
        }
        if (result.iotHubHostName != null)
        {
            this.setIotHubHostName(result.iotHubHostName);
        }
        if (result.provisioningStatus != null)
        {
            this.setProvisioningStatus(result.provisioningStatus);
        }
        if (result.deviceRegistrationState != null)
        {
            this.setDeviceRegistrationState(result.deviceRegistrationState);
        }

        if (result.initialTwin != null)
        {
            /*
             * During the deserialization process, the GSON will convert both tags and
             * properties to a raw Map, which will includes the $version and $metadata
             * as part of the collection. So, we need to reorganize this map using the
             * TwinCollection format. This constructor will do that.
             */
            this.initialTwin = new TwinState(result.initialTwin.getTags(), result.initialTwin.getDesiredProperties());
        }

        if (result.createdDateTimeUtcString != null)
        {
            this.setCreatedDateTimeUtcString(result.createdDateTimeUtcString);
        }

        if (result.lastUpdatedDateTimeUtcString != null)
        {
            this.setLastUpdatedDateTimeUtcString(result.lastUpdatedDateTimeUtcString);
        }

        if (result.etag != null)
        {
            this.setEtag(result.etag);
        }

        if (result.capabilities != null)
        {
            this.setCapabilities(result.capabilities);
        }

        this.setIotHubs(result.getIotHubs());
        this.setAllocationPolicy(result.getAllocationPolicy());
        this.setCustomAllocationDefinition(result.getCustomAllocationDefinition());
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
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        JsonObject enrollmentJson = gson.toJsonTree(this).getAsJsonObject();

        if (initialTwin != null)
        {
            enrollmentJson.add(INITIAL_TWIN_STATE_TAG, initialTwin.toJsonElement());
        }

        return enrollmentJson;
    }

    /**
     * Getter for the attestation mechanism.
     *
     * @return The {@code Attestation} with the attestation content. It cannot be {@code null}.
     * @throws ProvisioningServiceClientException If the type of the attestation mechanism is unknown.
     */
    public Attestation getAttestation() throws ProvisioningServiceClientException
    {
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
        if (attestation == null)
        {
            throw new IllegalArgumentException("attestation cannot be null");
        }

        this.attestation = new AttestationMechanism(attestation);
    }

    /**
     * Setter for the createdDateTimeUtcString.
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
     * @param createdDateTimeUtcString the {@code String} with the new createdDateTimeUtcString. It can be {@code null}, empty or not valid.
     */
    private void setCreatedDateTimeUtcString(String createdDateTimeUtcString)
    {
        this.createdDateTimeUtc = ParserUtility.getDateTimeUtc(createdDateTimeUtcString);
    }

    /**
     * Setter for the lastUpdatedDateTimeUtcString.
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
     * @param lastUpdatedDateTimeUtcString the {@code String} with the new lastUpdatedDateTimeUtcString. It can be {@code null}, empty or not valid.
     */
    private void setLastUpdatedDateTimeUtcString(String lastUpdatedDateTimeUtcString)
    {
        this.lastUpdatedDateTimeUtc = ParserUtility.getDateTimeUtc(lastUpdatedDateTimeUtcString);
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
        // Empty constructor for gson to use when deserializing
    }
}
