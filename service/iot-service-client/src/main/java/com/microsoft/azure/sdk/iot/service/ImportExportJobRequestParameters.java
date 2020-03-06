/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.deps.serializer.StorageAuthenticationType;

public class ImportExportJobRequestParameters {

    public ImportExportJobRequestParameters()
    {
    }

    public ImportExportJobRequestParameters(String inputBlobContainerUri, String outputBlobContainerUri)
    {
        this.inputBlobContainerUri = inputBlobContainerUri;
        this.outputBlobContainerUri = outputBlobContainerUri;
    }

    public ImportExportJobRequestParameters(String exportBlobContainerUri, boolean excludeKeysInExport)
    {
        this.exportBlobContainerUri = exportBlobContainerUri;
        this.excludeKeysInExport = excludeKeysInExport;
    }

    private String inputBlobContainerUri;
    private String outputBlobContainerUri;
    private String exportBlobContainerUri;
    private boolean excludeKeysInExport;
    private StorageAuthenticationType storageAuthenticationType;

    /**
     * @return URI containing SAS token to a blob container that contains registry data to sync.
     */
    public String getInputBlobContainerUri() {
        return inputBlobContainerUri;
    }

    /**
     * @param inputBlobContainerUri the input blob container URI.
     */
    public void setInputBlobContainerUri(String inputBlobContainerUri) {
        this.inputBlobContainerUri = inputBlobContainerUri;
    }

    /**
     * @return URI containing SAS token to a blob container.
     * This is used to output the status of the job and the results.
     */
    public String getOutputBlobContainerUri() {
        return outputBlobContainerUri;
    }

    /**
     * @param outputBlobContainerUri the output blob container URI.
     */
    public void setOutputBlobContainerUri(String outputBlobContainerUri) {
        this.outputBlobContainerUri = outputBlobContainerUri;
    }

    /**
     * @return URI containing SAS token to a blob container.
     * This is used to output the status of the job and the results.
     */
    public String getExportBlobContainerUri() {
        return exportBlobContainerUri;
    }

    /**
     * @param exportBlobContainerUri the output blob container URI.
     */
    public void setExportBlobContainerUri(String exportBlobContainerUri) {
        this.exportBlobContainerUri = exportBlobContainerUri;
    }

    /**
     * @return
     */
    public StorageAuthenticationType getStorageAuthenticationType() {
        return storageAuthenticationType;
    }

    /**
     * @param StorageAuthenticationType
     */
    public void setStorageAuthenticationType(StorageAuthenticationType storageAuthenticationType) {
        this.storageAuthenticationType = storageAuthenticationType;
    }

    /**
     * @return whether the keys are included in export or not.
     */
    public boolean getExcludeKeysInExport() {
        return excludeKeysInExport;
    }

    /**
     * @param excludeKeysInExport optional for export jobs; ignored for other jobs.  Default: false.
     * If false, authorization keys are included in export output.  Keys are exported as null otherwise.
     */
    public void setExcludeKeysInExport(boolean excludeKeysInExport) {
        this.excludeKeysInExport = excludeKeysInExport;
    }
}
