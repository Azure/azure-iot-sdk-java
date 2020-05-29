/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.deps.serializer.JobPropertiesParser;
import com.microsoft.azure.sdk.iot.deps.serializer.StorageAuthenticationType;

import java.util.Date;
/**
 * Contains properties of a Job.
 * See online <a href="https://docs.microsoft.com/en-us/rest/api/iothub/service/createimportexportjob">documentation</a> for more infomration.
*/
public class JobProperties
{
    public JobProperties()
    {
        this.setJobIdFinal("");
    }

    /**
     * @return the type of job to execute.
     */
    public JobType getType() {
        return type;
    }

    /**
     * @param type the type of job to execute.
     */
    public void setType(JobType type) {
        this.type = type;
    }

    /**
     * @return the system generated job id. Ignored at creation.
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * @param jobId the job id
     * @throws IllegalArgumentException if the provided jobId is null
     */
    @Deprecated
    public void setJobId(String jobId) throws IllegalArgumentException
    {
        //Codes_SRS_SERVICE_SDK_JAVA_JOB_PROPERTIES_34_004: [If the provided jobId is null, an IllegalArgumentException shall be thrown.]
        if (jobId == null)
        {
            throw new IllegalArgumentException("jobId cannot be null");
        }

        this.jobId = jobId;
    }

    /**
     * @param jobId the job id
     * @throws IllegalArgumentException if the provided jobId is null
     */
    public final void setJobIdFinal(String jobId) throws IllegalArgumentException
    {
        //Codes_SRS_SERVICE_SDK_JAVA_JOB_PROPERTIES_34_004: [If the provided jobId is null, an IllegalArgumentException shall be thrown.]
        if (jobId == null)
        {
            throw new IllegalArgumentException("jobId cannot be null");
        }

        this.jobId = jobId;
    }

    /**
     * @return the system generated UTC job start time. Ignored at creation.
     */
    public Date getStartTimeUtc() {
        return startTimeUtc;
    }

    /**
     * @param startTimeUtc the UTC job start time.
     */
    public void setStartTimeUtc(Date startTimeUtc) {
        this.startTimeUtc = startTimeUtc;
    }

    /**
     * @return the UTC job end time. Ignored at creation.
     * Represents the time the job stopped processing.
     */
    public Date getEndTimeUtc() {
        return endTimeUtc;
    }

    /**
     * @param endTimeUtc the UTC job end time.
     */
    public void setEndTimeUtc(Date endTimeUtc) {
        this.endTimeUtc = endTimeUtc;
    }

    /**
     * @return the system generated job status. Ignored at creation.
     */
    public JobStatus getStatus() {
        return status;
    }

    /**
     * @param status the job status.
     */
    public void setStatus(JobStatus status) {
        this.status = status;
    }

    /**
     * @return the system generated job progress. Ignored at creation.
     * Represents the completion percentage.
     */
    public int getProgress() {
        return progress;
    }

    /**
     * @param progress the job progress.
     */
    public void setProgress(int progress) {
        this.progress = progress;
    }

    /**
     * @return System generated. Ignored at creation.
     * If status == failure, this represents a string containing the reason.
     */
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * @param failureReason the failure reason.
     */
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    /**
     * @return URI to a blob container that contains registry data to sync.
     * Including a SAS token is dependent on the StorageAuthenticationType
     */
    public String getInputBlobContainerUri() {
        return inputBlobContainerUri;
    }

    /**
     * @param inputBlobContainerUri URI to a blob container that contains registry data to sync.
     *                              Including a SAS token is dependent on the StorageAuthenticationType
     */
    public void setInputBlobContainerUri(String inputBlobContainerUri) {
        this.inputBlobContainerUri = inputBlobContainerUri;
    }

    /**
     * @return URI to a blob container. This is used to output the status of the job and the results.
     * Including a SAS token is dependent on the StorageAuthenticationType
     */
    public String getOutputBlobContainerUri() {
        return outputBlobContainerUri;
    }

    /**
     * @param outputBlobContainerUri URI to a blob container. This is used to output the status of the job and the results.
     *                               Including a SAS token is dependent on the StorageAuthenticationType
     */
    public void setOutputBlobContainerUri(String outputBlobContainerUri) {
        this.outputBlobContainerUri = outputBlobContainerUri;
    }

    /**
     * @return authentication type being used for connecting to storage account
     */
    public StorageAuthenticationType getStorageAuthenticationType() {
        return storageAuthenticationType;
    }

    /**
     * @param storageAuthenticationType Specifies authentication type being used for connecting to storage account
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

    // CODES_SRS_SERVICE_SDK_JAVA_JOB_PROPERTIES_34_001: [The JobProperties class shall have the following properties: jobId,
    // startTimeUtc, endTimeUtc, JobType, JobStatus, progress, inputBlobContainerUri, outputBlobContainerUri,
    // excludeKeysInExport, failureReason.]
    private String jobId;
    private Date startTimeUtc;
    private Date endTimeUtc;
    private JobType type;
    private JobStatus status;
    private int progress;
    private String inputBlobContainerUri;
    private String outputBlobContainerUri;
    private boolean excludeKeysInExport;
    private String failureReason;
    private StorageAuthenticationType storageAuthenticationType;

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
        this.jobId = parser.getJobIdFinal();
        this.progress = parser.getProgress();
        this.startTimeUtc = parser.getStartTimeUtc();

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
}