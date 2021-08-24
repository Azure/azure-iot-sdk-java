/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.deps.serializer.JobPropertiesParser;
import com.microsoft.azure.sdk.iot.deps.serializer.ManagedIdentity;
import com.microsoft.azure.sdk.iot.deps.serializer.StorageAuthenticationType;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Date;
/**
 * Contains properties of a Job.
 * See online <a href="https://docs.microsoft.com/en-us/rest/api/iothub/service/createimportexportjob">documentation</a> for more information.
*/
public class JobProperties
{
    public JobProperties()
    {
        this.setJobId("");
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

    public enum JobStatus
    {
        UNKNOWN,
        ENQUEUED,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    public enum JobType
    {
        UNKNOWN,
        EXPORT,
        IMPORT
    }

    @NonNull
    @Getter
    @Setter
    private String jobId;

    @Getter
    @Setter
    private Date startTimeUtc;

    @Getter
    @Setter
    private Date endTimeUtc;

    @Getter
    @Setter
    private JobType type;

    @Getter
    @Setter
    private JobStatus status;

    @Getter
    @Setter
    private int progress;

    @Getter
    @Setter
    private String inputBlobContainerUri;

    @Getter
    @Setter
    private String outputBlobContainerUri;

    private boolean excludeKeysInExport;

    @Getter
    @Setter
    private String failureReason;

    @Getter
    @Setter
    private StorageAuthenticationType storageAuthenticationType;

    @Getter
    @Setter
    private ManagedIdentity identity;

    /**
     * Constructs a new JobProperties object using a JobPropertiesParser object
     * @param parser the parser object to convert from
     */
    JobProperties(JobPropertiesParser parser)
    {
        //Codes_SRS_SERVICE_SDK_JAVA_JOB_PROPERTIES_34_003: [This method shall convert the provided parser into a JobProperty object and return it.]
        this.endTimeUtc = parser.getEndTimeUtc();
        this.excludeKeysInExport = parser.isExcludeKeysInExport();
        this.inputBlobContainerUri = parser.getInputBlobContainerUri();
        this.failureReason = parser.getFailureReason();
        this.outputBlobContainerUri = parser.getOutputBlobContainerUri();
        this.storageAuthenticationType = parser.getStorageAuthenticationType();
        this.jobId = parser.getJobId();
        this.progress = parser.getProgress();
        this.startTimeUtc = parser.getStartTimeUtc();
        this.identity = parser.getIdentity();

        if (parser.getStatus() != null)
        {
            this.status = JobStatus.valueOf(parser.getStatus().toUpperCase());
        }

        if (parser.getType() != null)
        {
            this.type = JobType.valueOf(parser.getType().toUpperCase());
        }
    }

    /**
     * Converts this into a JobPropertiesParser object that can be used for serialization and deserialization
     * @return the converted JobPropertiesParser object
     */
    JobPropertiesParser toJobPropertiesParser()
    {
        //Codes_SRS_SERVICE_SDK_JAVA_JOB_PROPERTIES_34_002: [This method shall convert this into a JobPropertiesParser object and return it.]
        JobPropertiesParser jobPropertiesParser = new JobPropertiesParser();
        jobPropertiesParser.setEndTimeUtc(this.endTimeUtc);
        jobPropertiesParser.setExcludeKeysInExport(this.excludeKeysInExport);
        jobPropertiesParser.setFailureReason(this.failureReason);
        jobPropertiesParser.setInputBlobContainerUri(this.inputBlobContainerUri);
        jobPropertiesParser.setOutputBlobContainerUri(this.outputBlobContainerUri);
        jobPropertiesParser.setStorageAuthenticationType(this.storageAuthenticationType);
        jobPropertiesParser.setJobId(this.jobId);
        jobPropertiesParser.setProgress(this.progress);
        jobPropertiesParser.setStartTimeUtc(this.startTimeUtc);
        jobPropertiesParser.setIdentity(this.identity);
        if (this.status != null)
        {
            jobPropertiesParser.setStatus(this.status.toString());
        }

        if (this.type != null)
        {
            jobPropertiesParser.setType(this.type.toString().toLowerCase());
        }

        return jobPropertiesParser;
    }

    /**
     * Creates an instance of JobProperties with parameters ready to start an Import job
     *
     * @param inputBlobContainerUri URI to a blob container that contains registry data to sync.
     *                              Including a SAS token is dependent on the StorageAuthenticationType
     * @param outputBlobContainerUri URI to a blob container. This is used to output the status of the job and the results.
     *                               Including a SAS token is dependent on the StorageAuthenticationType
     * @return An instance of JobProperties
     */
    public static JobProperties createForImportJob(
            String inputBlobContainerUri,
            String outputBlobContainerUri)
    {
        StorageAuthenticationType storageAuthenticationType = StorageAuthenticationType.KEY;
        return createForImportJob(inputBlobContainerUri, outputBlobContainerUri, storageAuthenticationType);
    }

    /**
     * Creates an instance of JobProperties with parameters ready to start an Import job
     *
     * @param inputBlobContainerUri URI to a blob container that contains registry data to sync.
     *                              Including a SAS token is dependent on the StorageAuthenticationType
     * @param outputBlobContainerUri URI to a blob container. This is used to output the status of the job and the results.
     *                               Including a SAS token is dependent on the StorageAuthenticationType
     * @param storageAuthenticationType Specifies authentication type being used for connecting to storage account
     * @return An instance of JobProperties
     */
    public static JobProperties createForImportJob(
            String inputBlobContainerUri,
            String outputBlobContainerUri,
            StorageAuthenticationType storageAuthenticationType)
    {
        JobProperties importJobProperties = new JobProperties();
        importJobProperties.setType(JobProperties.JobType.IMPORT);
        importJobProperties.setInputBlobContainerUri(inputBlobContainerUri);
        importJobProperties.setOutputBlobContainerUri(outputBlobContainerUri);
        importJobProperties.setStorageAuthenticationType(storageAuthenticationType);
        return importJobProperties;
    }

    /**
     * Creates an instance of JobProperties with parameters ready to start an Import job
     *
     * @param inputBlobContainerUri URI to a blob container that contains registry data to sync.
     *                              Including a SAS token is dependent on the StorageAuthenticationType
     * @param outputBlobContainerUri URI to a blob container. This is used to output the status of the job and the results.
     *                               Including a SAS token is dependent on the StorageAuthenticationType
     * @param storageAuthenticationType Specifies authentication type being used for connecting to storage account
     * @param identity the managed identity used to access the storage account for import jobs.
     * @return An instance of JobProperties
     */
    public static JobProperties createForImportJob(
            String inputBlobContainerUri,
            String outputBlobContainerUri,
            StorageAuthenticationType storageAuthenticationType,
            ManagedIdentity identity)
    {
        JobProperties importJobProperties = new JobProperties();
        importJobProperties.setType(JobProperties.JobType.IMPORT);
        importJobProperties.setInputBlobContainerUri(inputBlobContainerUri);
        importJobProperties.setOutputBlobContainerUri(outputBlobContainerUri);
        importJobProperties.setStorageAuthenticationType(storageAuthenticationType);
        importJobProperties.setIdentity(identity);
        return importJobProperties;
    }

    /**
     * Creates an instance of JobProperties with parameters ready to start an Export job
     *
     * @param outputBlobContainerUri URI to a blob container. This is used to output the status of the job and the results.
     *                               Including a SAS token is dependent on the StorageAuthenticationType
     * @param excludeKeysInExport Indicates if authorization keys are included in export output
     * @return An instance of JobProperties
     */
    public static JobProperties createForExportJob(
            String outputBlobContainerUri,
            Boolean excludeKeysInExport)
    {
        StorageAuthenticationType storageAuthenticationType = StorageAuthenticationType.KEY;
        return createForExportJob(outputBlobContainerUri, excludeKeysInExport, storageAuthenticationType);
    }

    /**
     * Creates an instance of JobProperties with parameters ready to start an Export job
     *
     * @param outputBlobContainerUri URI to a blob container. This is used to output the status of the job and the results.
     *                               Including a SAS token is dependent on the StorageAuthenticationType
     * @param excludeKeysInExport Indicates if authorization keys are included in export output
     * @param storageAuthenticationType Specifies authentication type being used for connecting to storage account
     * @return An instance of JobProperties
     */
    public static JobProperties createForExportJob(
            String outputBlobContainerUri,
            Boolean excludeKeysInExport,
            StorageAuthenticationType storageAuthenticationType)
    {
        JobProperties exportJobProperties = new JobProperties();
        exportJobProperties.setType(JobProperties.JobType.EXPORT);
        exportJobProperties.setOutputBlobContainerUri(outputBlobContainerUri);
        exportJobProperties.setExcludeKeysInExport(excludeKeysInExport);
        exportJobProperties.setStorageAuthenticationType(storageAuthenticationType);
        return exportJobProperties;
    }

    /**
     * Creates an instance of JobProperties with parameters ready to start an Export job
     *
     * @param outputBlobContainerUri URI to a blob container. This is used to output the status of the job and the results.
     *                               Including a SAS token is dependent on the StorageAuthenticationType
     * @param excludeKeysInExport Indicates if authorization keys are included in export output
     * @param storageAuthenticationType Specifies authentication type being used for connecting to storage account
     * @param identity the managed identity used to access the storage account for export jobs.
     * @return An instance of JobProperties
     */
    public static JobProperties createForExportJob(
            String outputBlobContainerUri,
            Boolean excludeKeysInExport,
            StorageAuthenticationType storageAuthenticationType,
            ManagedIdentity identity)
    {
        JobProperties exportJobProperties = new JobProperties();
        exportJobProperties.setType(JobProperties.JobType.EXPORT);
        exportJobProperties.setOutputBlobContainerUri(outputBlobContainerUri);
        exportJobProperties.setExcludeKeysInExport(excludeKeysInExport);
        exportJobProperties.setStorageAuthenticationType(storageAuthenticationType);
        exportJobProperties.setIdentity(identity);
        return exportJobProperties;
    }
}