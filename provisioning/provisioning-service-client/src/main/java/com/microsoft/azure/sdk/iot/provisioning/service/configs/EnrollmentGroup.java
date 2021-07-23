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
import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.deps.twin.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.provisioning.service.Tools;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;

import java.util.Collection;
import java.util.Date;

/**
 * Representation of a single Device Provisioning Service enrollment group with a JSON serializer and deserializer.
 *
 * <p> This object is used to send EnrollmentGroup information to the provisioning service, or receive EnrollmentGroup
 *     information from the provisioning service.
 *
 * <p> To create or update an EnrollmentGroup on the provisioning service you should fill this object and call the
 *     public API {@link ProvisioningServiceClient#createOrUpdateEnrollmentGroup(EnrollmentGroup)}.
 *     The minimum information required by the provisioning service is the {@link #enrollmentGroupId} and the
 *     {@link #attestation}.
 *
 * <p> To provision a device using EnrollmentGroup, it must contain a X509 chip with a signingCertificate for the
 *     {@link X509Attestation} mechanism, or use {@link SymmetricKeyAttestation} mechanism.
 *
 * <p> The content of this class will be serialized in a JSON format and sent as a body of the rest API to the
 *     provisioning service.
 *
 * <p> When serialized, an EnrollmentGroup will look like the following example:
 * <pre>
 * {@code
 * {
 *     "enrollmentGroupId":"validEnrollmentGroupId",
 *     "attestation":{
 *         "type":"x509",
 *         "signingCertificates":{
 *             "primary":{
 *                 "certificate":"[valid certificate]"
 *             }
 *         }
 *     },
 *     "iotHubHostName":"ContosoIoTHub.azure-devices.net",
 *     "provisioningStatus":"enabled"
 * }
 * }
 * </pre>
 *
 * <p> The content of this class can be filled by a JSON, received from the provisioning service, as result of a
 *     EnrollmentGroup operation like create, update, or query EnrollmentGroup.
 *
 * <p> The following JSON is a sample of the EnrollmentGroup response, received from the provisioning service.
 * <pre>
 * {@code
 * {
 *     "enrollmentGroupId":"validEnrollmentGroupId",
 *     "attestation":{
 *         "type":"x509",
 *         "signingCertificates":{
 *             "primary":{
 *                 "certificate":"[valid certificate]",
 *                 "info": {
 *                     "subjectName": "CN=ROOT_00000000-0000-0000-0000-000000000000, OU=Azure IoT, O=MSFT, C=US",
 *                     "sha1Thumbprint": "0000000000000000000000000000000000",
 *                     "sha256Thumbprint": "validEnrollmentGroupId",
 *                     "issuerName": "CN=ROOT_00000000-0000-0000-0000-000000000000, OU=Azure IoT, O=MSFT, C=US",
 *                     "notBeforeUtc": "2017-11-14T12:34:18Z",
 *                     "notAfterUtc": "2017-11-20T12:34:18Z",
 *                     "serialNumber": "000000000000000000",
 *                     "version": 3
 *                 }
 *             }
 *         }
 *     },
 *     "iotHubHostName":"ContosoIoTHub.azure-devices.net",
 *     "provisioningStatus":"enabled",
 *     "createdDateTimeUtc": "2017-09-28T16:29:42.3447817Z",
 *     "lastUpdatedDateTimeUtc": "2017-09-28T16:29:42.3447817Z",
 *     "etag": "\"00000000-0000-0000-0000-00000000000\""
 * }
 * }
 * </pre>
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollmentgroup">Device Enrollment Group</a>
 */
@SuppressWarnings("unused") // A number of private members are unused but may be filled in or used by serialization
public class EnrollmentGroup extends Serializable
{
    // the enrollment group identifier
    private static final String ENROLLMENT_GROUP_ID_TAG = "enrollmentGroupId";
    @Expose
    @SerializedName(ENROLLMENT_GROUP_ID_TAG)
    private String enrollmentGroupId;

    // the attestation
    private static final String ATTESTATION_TAG = "attestation";
    @Expose
    @SerializedName(ATTESTATION_TAG)
    private AttestationMechanism attestation;

    // the iothub host name
    private static final String IOTHUB_HOST_NAME_TAG = "iotHubHostName";
    @Expose
    @SerializedName(IOTHUB_HOST_NAME_TAG)
    private String iotHubHostName;

    // the initial Twin state identifier (Twin is a special case and will be manually serialized).
    private static final String INITIAL_TWIN_STATE_TAG = "initialTwin";
    @Expose
    @SerializedName(INITIAL_TWIN_STATE_TAG)
    private TwinState initialTwin;

    // the provisioning status
    private static final String PROVISIONING_STATUS_TAG = "provisioningStatus";
    @Expose
    @SerializedName(PROVISIONING_STATUS_TAG)
    private ProvisioningStatus provisioningStatus;

    // the datetime this resource was created
    private static final String CREATED_DATETIME_UTC_TAG = "createdDateTimeUtc";
    @Expose
    @SerializedName(CREATED_DATETIME_UTC_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String createdDateTimeUtc;
    private transient Date createdDateTimeUtcDate;

    // the datetime this resource was last updated
    private static final String LAST_UPDATED_DATETIME_UTC_TAG = "lastUpdatedDateTimeUtc";
    @Expose
    @SerializedName(LAST_UPDATED_DATETIME_UTC_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String lastUpdatedDateTimeUtc;
    private transient Date lastUpdatedDateTimeUtcDate;

    // the eTag
    private static final String ETAG_TAG = "etag";
    @Expose
    @SerializedName(ETAG_TAG)
    private String etag;

    // the reprovisioning policy
    private static final String REPROVISION_POLICY_TAG = "reprovisionPolicy";
    @Expose
    @SerializedName(REPROVISION_POLICY_TAG)
    private ReprovisionPolicy reprovisionPolicy;

    // the custom allocation definition
    private static final String CUSTOM_ALLOCATION_DEFINITION_TAG = "customAllocationDefinition";
    @Expose
    @SerializedName(CUSTOM_ALLOCATION_DEFINITION_TAG)
    private CustomAllocationDefinition customAllocationDefinition;

    // the allocation policy of the resource. overrides the tenant level allocation policy
    private static final String ALLOCATION_POLICY_TAG = "allocationPolicy";
    @Expose
    @SerializedName(ALLOCATION_POLICY_TAG)
    private AllocationPolicy allocationPolicy;

    // the list of names of IoT hubs the device in this resource can be allocated to. Must be a subset of tenant level list of IoT hubs
    private static final String IOT_HUBS_TAG = "iotHubs";
    @Expose
    @SerializedName(IOT_HUBS_TAG)
    private Collection<String> iotHubs;

    private static final String DEVICE_CAPABILITIES_TAG = "capabilities";
    @Expose
    @SerializedName(DEVICE_CAPABILITIES_TAG)
    private DeviceCapabilities capabilities;

    /**
     * CONSTRUCTOR
     *
     * <p> This constructor creates an instance of the enrollment group with
     *     the minimum set of information required by the provisioning service.
     *
     * <p> When serialized, an EnrollmentGroup will look like the following example:
     * <pre>
     * {@code
     * {
     *     "enrollmentGroupId":"validEnrollmentGroupId",
     *     "attestation":{
     *         "type":"x509",
     *         "signingCertificates":{
     *             "primary":{
     *                 "certificate":"[valid certificate]"
     *             }
     *         }
     *     }
     * }
     * }
     * </pre>
     *
     * @param enrollmentGroupId the {@code String} with an unique id for this enrollment group.
     * @param attestation the {@link Attestation} mechanism that shall be {@code signedCertificate} of {@link X509Attestation} or {@code symmetricKey} of {@link SymmetricKeyAttestation}
     * @throws IllegalArgumentException If one of the provided parameters is not correct.
     */
    public EnrollmentGroup(
            String enrollmentGroupId,
            Attestation attestation)
    {
        this.setEnrollmentGroupId(enrollmentGroupId);
        this.setAttestation(attestation);
    }

    /**
     * CONSTRUCTOR
     *
     * <p> This constructor creates an instance of the enrollment group filling
     *     the class with the information provided in the JSON.
     *
     * <p> The following JSON is a sample of the EnrollmentGroup response, received from the provisioning service.
     * <pre>
     * {@code
     * {
     *     "enrollmentGroupId":"validEnrollmentGroupId",
     *     "attestation":{
     *         "type":"x509",
     *         "signingCertificates":{
     *             "primary":{
     *                 "certificate":"[valid certificate]",
     *                 "info": {
     *                     "subjectName": "CN=ROOT_00000000-0000-0000-0000-000000000000, OU=Azure IoT, O=MSFT, C=US",
     *                     "sha1Thumbprint": "0000000000000000000000000000000000",
     *                     "sha256Thumbprint": "validEnrollmentGroupId",
     *                     "issuerName": "CN=ROOT_00000000-0000-0000-0000-000000000000, OU=Azure IoT, O=MSFT, C=US",
     *                     "notBeforeUtc": "2017-11-14T12:34:18Z",
     *                     "notAfterUtc": "2017-11-20T12:34:18Z",
     *                     "serialNumber": "000000000000000000",
     *                     "version": 3
     *                 }
     *             }
     *         }
     *     },
     *     "iotHubHostName":"ContosoIoTHub.azure-devices.net",
     *     "provisioningStatus":"enabled"
     *     "createdDateTimeUtc": "2017-09-28T16:29:42.3447817Z",
     *     "lastUpdatedDateTimeUtc": "2017-09-28T16:29:42.3447817Z",
     *     "etag": "\"00000000-0000-0000-0000-00000000000\""
     * }
     * }
     * </pre>
     *
     * @param json the {@code String} with the JSON received from the provisioning service.
     * @throws IllegalArgumentException If the provided JSON is null, empty, or invalid.
     */
    public EnrollmentGroup(String json)
    {
        if (Tools.isNullOrEmpty(json))
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        EnrollmentGroup result = gson.fromJson(json, EnrollmentGroup.class);

        this.setEnrollmentGroupId(result.enrollmentGroupId);
        this.setAttestation(result.attestation);

        if (result.iotHubHostName != null)
        {
            this.setIotHubHostName(result.iotHubHostName);
        }
        if (result.provisioningStatus != null)
        {
            this.setProvisioningStatus(result.provisioningStatus);
        }
        if (result.initialTwin != null)
        {
            this.setInitialTwin(result.initialTwin);
        }

        if (result.createdDateTimeUtc != null)
        {
            this.setCreatedDateTimeUtc(result.createdDateTimeUtc);
        }

        if (result.lastUpdatedDateTimeUtc != null)
        {
            this.setLastUpdatedDateTimeUtc(result.lastUpdatedDateTimeUtc);
        }

        if (result.etag != null)
        {
            this.setEtag(result.etag);
        }

        this.setIotHubs(result.getIotHubs());
        this.setAllocationPolicy(result.getAllocationPolicy());
        this.setCustomAllocationDefinition(result.getCustomAllocationDefinition());
        this.setReprovisionPolicy(result.getReprovisionPolicy());
        this.setCapabilities(result.getCapabilities());
    }

    /**
     * Serializer
     *
     * <p>
     *     Creates a {@code JsonElement}, which the content represents
     *     the information in this class and its subclasses in a JSON format.
     *
     *     This is useful if the caller will integrate this JSON with jsons from
     *     other classes to generate a consolidated JSON.
     * </p>

     * @return The {@code JsonElement} with the content of this class.
     */
    public JsonElement toJsonElement()
    {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        JsonObject enrollmentGroupJson = gson.toJsonTree(this).getAsJsonObject();

        if (initialTwin != null)
        {
            enrollmentGroupJson.add(INITIAL_TWIN_STATE_TAG, initialTwin.toJsonElement());
        }

        return enrollmentGroupJson;
    }

    /**
     * Getter for the enrollmentGroupId.
     *
     * @return The {@code String} with the enrollmentGroupId content. It cannot be {@code null} or empty.
     */
    public String getEnrollmentGroupId()
    {
        return this.enrollmentGroupId;
    }

    /**
     * Setter for the enrollmentGroupId.
     *
     * <p>
     *     A valid enrollment group Id shall follow this criteria.
     *         A case-sensitive string (up to 128 char long)
     *         of ASCII 7-bit alphanumeric chars
     *         + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     * </p>
     *
     * @param enrollmentGroupId the {@code String} with the new enrollmentGroupId. It cannot be {@code null}, empty, or invalid.
     * @throws IllegalArgumentException If the provided enrollmentGroupId is {@code null}, empty, or invalid.
     */
    protected final void setEnrollmentGroupId(String enrollmentGroupId)
    {
        this.enrollmentGroupId = enrollmentGroupId;
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
     *     Attestation mechanism is mandatory parameter that provides the mechanism
     *     type and the necessary keys/certificates
     *
     *     @see AttestationMechanism
     * </p>
     *
     * @param attestationMechanism the {@code AttestationMechanism} with the new attestation mechanism. It can be `tpm`, `x509` or 'symmetricKey'.
     * @throws IllegalArgumentException If the provided attestation mechanism is {@code null}.
     */
    protected final void setAttestation(AttestationMechanism attestationMechanism)
    {
        if (attestationMechanism == null)
        {
            throw new IllegalArgumentException("attestationMechanism cannot be null");
        }

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
     * <p> Attestation mechanism is mandatory parameter that provides the mechanism
     *     type and the necessary certificates.
     *
     * <p> EnrollmentGroup only accept {@link X509Attestation} with the RootCertificates, or {@link SymmetricKeyAttestation}
     *     with Primary and Secondary Keys. You can create an {@link X509Attestation} by providing the <b>.pem</b> content to
     *     {@link X509Attestation#createFromRootCertificates(String, String)}. You can create a {@link SymmetricKeyAttestation}
     *     by providing the Primary and Secondary Keys in Base64 format.
     *
     * @see Attestation
     * @see X509Attestation
     * @see SymmetricKeyAttestation
     *
     * @param attestation the {@link Attestation} with the new attestation mechanism. It shall be {@link X509Attestation} or {@link SymmetricKeyAttestation}
     * @throws IllegalArgumentException If the provided attestation mechanism is {@code null} or invalid.
     */
    public void setAttestation(Attestation attestation)
    {
        if (attestation == null)
        {
            throw new IllegalArgumentException("attestation cannot be null");
        }
        else if (!(attestation instanceof X509Attestation) && !(attestation instanceof SymmetricKeyAttestation))
        {
            throw new IllegalArgumentException("attestation for EnrollmentGroup shall be X509 or SymmetricKey");
        }

        if (attestation instanceof X509Attestation)
        {
            if (((X509Attestation)attestation).getRootCertificates() == null)
            {
                throw new IllegalArgumentException("X509 attestation for EnrollmentGroup does not contains a valid certificate.");
            }
        }

        this.attestation = new AttestationMechanism(attestation);
    }

    /**
     * Getter for the iotHubHostName.
     *
     * @return The {@code String} with the iotHubHostName content. It cannot be {@code null} or empty.
     */
    public String getIotHubHostName()
    {
        return this.iotHubHostName;
    }

    /**
     * Setter for the iotHubHostName.
     *
     * <p>
     *     A valid iothub host name shall follow this criteria.
     *         A case-sensitive string (up to 128 char long)
     *         of ASCII 7-bit alphanumeric chars
     *         + {'-', ':', '.', '+', '%', '_', '#', '*', '?', '!', '(', ')', ',', '=', '@', ';', '$', '''}.
     *     A valid host name shall have, at least 2 parts separated by '.'.
     * </p>
     *
     * @param iotHubHostName the {@code String} with the new iotHubHostName. It cannot be {@code null}, empty, or invalid.
     * @throws IllegalArgumentException If the provided iotHubHostName is {@code null}, empty, or invalid.
     */
    public final void setIotHubHostName(String iotHubHostName)
    {
        this.iotHubHostName = iotHubHostName;
    }

    /**
     * Getter for the initialTwin.
     *
     * @return The {@code TwinState} with the initialTwin content. Its optional and can be {@code null}.
     */
    public TwinState getInitialTwin()
    {
        return this.initialTwin;
    }

    /**
     * Setter for the initialTwin.
     *
     * <p>
     *     It provides a Twin precondition for the provisioned device.
     * </p>
     *
     * @param initialTwin the {@code TwinState} with the new initialTwin. It cannot be {@code null}.
     * @throws IllegalArgumentException If the provided initialTwin is {@code null}.
     */
    public final void setInitialTwin(TwinState initialTwin)
    {
        if (initialTwin == null)
        {
            throw new IllegalArgumentException("initialTwin cannot be null");
        }

        this.initialTwin = initialTwin;
    }

    /**
     * Getter for the provisioningStatus.
     *
     * @return The {@code ProvisioningStatus} with the provisioningStatus content. It can be 'enabled' or 'disabled'.
     */
    public ProvisioningStatus getProvisioningStatus()
    {
        return this.provisioningStatus;
    }

    /**
     * Setter for the provisioningStatus.
     *
     * <p>
     *     It provides a Status precondition for the provisioned device.
     * </p>
     *
     * @param provisioningStatus the {@code ProvisioningStatus} with the new provisioningStatus. It cannot be {@code null}.
     * @throws IllegalArgumentException If the provided provisioningStatus is {@code null}.
     */
    public final void setProvisioningStatus(ProvisioningStatus provisioningStatus)
    {
        if (provisioningStatus == null)
        {
            throw new IllegalArgumentException("provisioningStatus cannot be null");
        }

        this.provisioningStatus = provisioningStatus;
    }

    /**
     * Getter for the createdDateTimeUtcDate.
     *
     * @return The {@code Date} with the createdDateTimeUtcDate content. It can be {@code null}.
     */
    public Date getCreatedDateTimeUtc()
    {
        return this.createdDateTimeUtcDate;
    }

    /**
     * Setter for the createdDateTimeUtc.
     *
     * <p>
     *     This Date and Time is provided by the provisioning service. If the enrollmentGroup is not created yet,
     *     this string can represent an invalid Date. In this case, it will be ignored.
     *
     *     Example of the expected format:
     *         "2016-06-01T21:22:43.7996883Z"
     * </p>
     *
     * @param createdDateTimeUtc the {@code String} with the new createdDateTimeUtc. It can be {@code null}, empty or not valid.
     */
    protected final void setCreatedDateTimeUtc(String createdDateTimeUtc)
    {
        this.createdDateTimeUtcDate = ParserUtility.getDateTimeUtc(createdDateTimeUtc);
    }

    /**
     * Getter for the lastUpdatedDateTimeUtcDate.
     *
     * @return The {@code Date} with the lastUpdatedDateTimeUtcDate content. It can be {@code null}.
     */
    public Date getLastUpdatedDateTimeUtc()
    {
        return this.lastUpdatedDateTimeUtcDate;
    }

    /**
     * Setter for the lastUpdatedDateTimeUtc.
     *
     * <p>
     *     This Date and Time is provided by the provisioning service. If the enrollmentGroup is not created yet,
     *     this string can represent an invalid Date. In this case, it will be ignored.
     *
     *     Example of the expected format:
     *         "2016-06-01T21:22:43.7996883Z"
     * </p>
     *
     * @param lastUpdatedDateTimeUtc the {@code String} with the new lastUpdatedDateTimeUtc. It can be {@code null}, empty or not valid.
     */
    protected final void setLastUpdatedDateTimeUtc(String lastUpdatedDateTimeUtc)
    {
        this.lastUpdatedDateTimeUtcDate = ParserUtility.getDateTimeUtc(lastUpdatedDateTimeUtc);
    }

    /**
     * Getter for the etag.
     *
     * @return The {@code String} with the etag content. It can be {@code null}.
     */
    public String getEtag()
    {
        return this.etag;
    }

    /**
     * Setter for the etag.
     *
     * @param etag the {@code String} with the new etag. It cannot be {@code null}, empty or invalid.
     * @throws IllegalArgumentException If the provided etag is {@code null}, empty or invalid.
     */
    public final void setEtag(String etag)
    {
        this.etag = etag;
    }

    public DeviceCapabilities getCapabilities()
    {
        return this.capabilities;
    }

    /**
     * @param capabilities the device capabilities to set
     */
    public final void setCapabilities(DeviceCapabilities capabilities)
    {
        this.capabilities = capabilities;
    }


    /**
     * Getter for the reprovision policy.
     *
     * @return The {@code ReprovisionPolicy} with the reprovisionPolicy content.
     */
    public ReprovisionPolicy getReprovisionPolicy()
    {
        return this.reprovisionPolicy;
    }

    /**
     * Setter for the reprovision policy.
     *
     * @param reprovisionPolicy the {@code ReprovisionPolicy} with the behavior when a device is re-provisioned to an IoT hub.
     */
    public void setReprovisionPolicy(ReprovisionPolicy reprovisionPolicy)
    {
        this.reprovisionPolicy = reprovisionPolicy;
    }

    /**
     * Getter for the allocation policy.
     *
     * @return The {@code AllocationPolicy} with the allocationPolicy content.
     */
    public AllocationPolicy getAllocationPolicy()
    {
        return this.allocationPolicy;
    }

    /**
     * Setter for the allocation policy.
     *
     * @param allocationPolicy the {@code AllocationPolicy} with the allocation policy of this resource. Overrides the tenant level allocation policy.
     */
    public void setAllocationPolicy(AllocationPolicy allocationPolicy)
    {
        this.allocationPolicy = allocationPolicy;
    }

    /**
     * Getter for the list of IoTHub names that the device can be allocated to..
     *
     * @return The {@code AllocationPolicy} with the allocationPolicy content.
     */
    public Collection<String> getIotHubs()
    {
        return this.iotHubs;
    }

    /**
     * Setter for the list of IotHubs available for allocation.
     *
     * @param iotHubs the {@code List<String>} of names of IoT hubs the device(s) in this resource can be allocated to. Must be a subset of tenant level list of IoT hubs
     */
    public void setIotHubs(Collection<String> iotHubs)
    {
        this.iotHubs = iotHubs;
    }

    /**
     * Getter for the custom allocation definition policy.
     *
     * @return The {@code CustomAllocationDefinition} policy.
     */
    public CustomAllocationDefinition getCustomAllocationDefinition()
    {
        return this.customAllocationDefinition;
    }

    /**
     * Setter for the custom allocation definition policy.
     *
     * @param customAllocationDefinition the {@code CustomAllocationDefinition} with the custom allocation policy of this resource.
     */
    public void setCustomAllocationDefinition(CustomAllocationDefinition customAllocationDefinition)
    {
        this.customAllocationDefinition = customAllocationDefinition;
    }

    /**
     * Empty constructor
     *
     * <p>
     *     Used only by the tools that will deserialize this class.
     * </p>
     */
    @SuppressWarnings("unused")
    EnrollmentGroup()
    {
        // Empty constructor for gson to use when deserializing
    }
}
