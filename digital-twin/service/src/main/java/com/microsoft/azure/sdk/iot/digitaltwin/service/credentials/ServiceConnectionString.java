// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.credentials;

public class ServiceConnectionString {
    protected static final String VALUE_PAIR_DELIMITER = ";";
    protected static final String VALUE_PAIR_SEPARATOR = "=";
    protected static final String HOST_NAME_SEPARATOR = ".";

    protected static final String HOST_NAME_PROPERTY_NAME = "HostName";
    protected static final String SHARED_ACCESS_KEY_NAME_PROPERTY_NAME = "SharedAccessKeyName";
    protected static final String SHARED_ACCESS_KEY_PROPERTY_NAME = "SharedAccessKey";
    protected static final String SHARED_ACCESS_SIGNATURE_PROPERTY_NAME = "SharedAccessSignature";

    // Included in the device connection string
    protected String hostName;
    protected String iotHubName;
    protected String httpsEndpoint;
    protected AuthenticationMethod authenticationMethod;
    protected String sharedAccessKeyName;
    protected String sharedAccessKey;
    protected String sharedAccessSignature;

    protected ServiceConnectionString() {
    }

    /**
     * Serialize connection string
     *
     * @return Iot Hub connection string
     */
    @Override
    public String toString() {
        // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBCONNECTIONSTRING_12_007: [The function shall serialize the object to a
        // string using the following format: HostName=HOSTNAME.b.c.d;SharedAccessKeyName=ACCESSKEYNAME;SharedAccessKey=1234567890abcdefghijklmnopqrstvwxyz=;SharedAccessSignature=]
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(HOST_NAME_PROPERTY_NAME);
        stringBuilder.append(VALUE_PAIR_SEPARATOR);
        stringBuilder.append(this.hostName);
        stringBuilder.append(VALUE_PAIR_DELIMITER);

        stringBuilder.append(SHARED_ACCESS_KEY_NAME_PROPERTY_NAME);
        stringBuilder.append(VALUE_PAIR_SEPARATOR);
        stringBuilder.append(this.sharedAccessKeyName);
        stringBuilder.append(VALUE_PAIR_DELIMITER);

        stringBuilder.append(SHARED_ACCESS_KEY_PROPERTY_NAME);
        stringBuilder.append(VALUE_PAIR_SEPARATOR);
        stringBuilder.append(this.sharedAccessKey);
        stringBuilder.append(VALUE_PAIR_DELIMITER);

        stringBuilder.append(SHARED_ACCESS_SIGNATURE_PROPERTY_NAME);
        stringBuilder.append(VALUE_PAIR_SEPARATOR);
        stringBuilder.append(this.sharedAccessSignature);

        return stringBuilder.toString();
    }

    /**
     * Getter for iotHubName
     *
     * @return The iot hub name string
     */
    public String getIotHubName() {
        return this.iotHubName;
    }

    /**
     * Getter for HTTPS endpoint
     *
     * @return The HTTPS endpoint for the IoTHub
     */
    public String getHttpsEndpoint() {
        return this.httpsEndpoint;
    }

    /**
     * Getter for authenticationMethod
     *
     * @return The authenticationMethod object
     */
    public AuthenticationMethod getAuthenticationMethod() {
        return this.authenticationMethod;
    }

    /**
     * Getter for sharedAccessKeyName
     *
     * @return The sharedAccessKeyName string
     */
    public String getSharedAccessKeyName() {
        return this.sharedAccessKeyName;
    }

    /**
     * Getter for sharedAccessKey
     *
     * @return The sharedAccessKey string
     */
    public String getSharedAccessKey() {
        return this.sharedAccessKey;
    }

    /**
     * Getter for sharedAccessSignature
     *
     * @return The sharedAccessSignature string
     */
    public String getSharedAccessSignature() {
        return this.sharedAccessSignature;
    }

    /**
     * Getter for hostName
     *
     * @return The hostName string
     */
    public String getHostName() {
        return this.hostName;
    }

    /**
     * Setter for sharedAccessKeyName
     *
     * @param sharedAccessKeyName The value of the signature to set
     */
    protected void setSharedAccessKeyName(String sharedAccessKeyName) {
        this.sharedAccessKeyName = sharedAccessKeyName;
    }

    /**
     * Setter for sharedAccessKey
     *
     * @param sharedAccessKey The value of the signature to set
     */
    protected void setSharedAccessKey(String sharedAccessKey) {
        this.sharedAccessKey = sharedAccessKey;
    }

    /**
     * Setter for sharedAccessSignature
     *
     * @param sharedAccessSignature The value of the signature to set
     */
    protected void setSharedAccessSignature(String sharedAccessSignature) {
        this.sharedAccessSignature = sharedAccessSignature;
    }
}
