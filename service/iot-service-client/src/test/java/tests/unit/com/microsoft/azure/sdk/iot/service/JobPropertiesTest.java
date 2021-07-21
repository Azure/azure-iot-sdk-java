// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.deps.serializer.JobPropertiesParser;
import com.microsoft.azure.sdk.iot.service.JobProperties;
import mockit.Deencapsulation;
import org.junit.Test;

import java.util.Date;

import static junit.framework.TestCase.assertEquals;

/**
 * Code Coverage
 * Methods:100%
 * Lines: 100%
 */
public class JobPropertiesTest
{
    //Tests_SRS_SERVICE_SDK_JAVA_JOB_PROPERTIES_34_003: [This method shall convert the provided parser into a JobProperty object and return it.]
    @Test
    public void fromJobPropertiesParser()
    {
        // arrange
        JobPropertiesParser parser = Deencapsulation.newInstance(JobPropertiesParser.class);
        parser.setEndTimeUtc(new Date(System.currentTimeMillis()));
        parser.setStartTimeUtc(new Date(System.currentTimeMillis()));
        parser.setFailureReason("failureReason");
        parser.setInputBlobContainerUri("inputContainerUri");
        parser.setOutputBlobContainerUri("outputContainerUri");
        parser.setProgress(0);
        parser.setExcludeKeysInExport(false);
        parser.setJobId("jobId");
        parser.setStatus(JobProperties.JobStatus.COMPLETED.toString());
        parser.setType(JobProperties.JobType.IMPORT.toString());

        // act
        JobProperties jobProperties = jobPropertiesConstructorWithParser(parser);

        // assert
        assertEquals(parser.getInputBlobContainerUri(), jobProperties.getInputBlobContainerUri());
        assertEquals(parser.getOutputBlobContainerUri(), jobProperties.getOutputBlobContainerUri());
        assertEquals(parser.isExcludeKeysInExport(), jobProperties.getExcludeKeysInExport());
        assertEquals(parser.getType(), jobProperties.getType().toString());
        assertEquals(parser.getStatus(), jobProperties.getStatus().toString());
        assertEquals(parser.getProgress(), jobProperties.getProgress());
        assertEquals(parser.getJobId(), jobProperties.getJobId());
        assertEquals(parser.getFailureReason(), jobProperties.getFailureReason());
        assertEquals(parser.getEndTimeUtc(), jobProperties.getEndTimeUtc());
        assertEquals(parser.getStartTimeUtc(), jobProperties.getStartTimeUtc());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_JOB_PROPERTIES_34_002: [This method shall convert this into a JobPropertiesParser object and return it.]
    @Test
    public void toJobPropertiesParser()
    {
        // arrange
        JobProperties jobProperties = new JobProperties();
        jobProperties.setEndTimeUtc(new Date(System.currentTimeMillis()));
        jobProperties.setStartTimeUtc(new Date(System.currentTimeMillis()));
        jobProperties.setFailureReason("failureReason");
        jobProperties.setInputBlobContainerUri("inputContainerUri");
        jobProperties.setOutputBlobContainerUri("outputContainerUri");
        jobProperties.setProgress(0);
        jobProperties.setExcludeKeysInExport(false);
        jobProperties.setJobId("jobId");
        jobProperties.setStatus(JobProperties.JobStatus.COMPLETED);
        jobProperties.setType(JobProperties.JobType.IMPORT);

        // act
        JobPropertiesParser parser = toJobPropertiesParser(jobProperties);

        // assert
        assertEquals(parser.getInputBlobContainerUri(), jobProperties.getInputBlobContainerUri());
        assertEquals(parser.getOutputBlobContainerUri(), jobProperties.getOutputBlobContainerUri());
        assertEquals(parser.isExcludeKeysInExport(), jobProperties.getExcludeKeysInExport());
        assertEquals(parser.getType().toUpperCase(), jobProperties.getType().toString());
        assertEquals(parser.getStatus(), jobProperties.getStatus().toString());
        assertEquals(parser.getProgress(), jobProperties.getProgress());
        assertEquals(parser.getJobId(), jobProperties.getJobId());
        assertEquals(parser.getFailureReason(), jobProperties.getFailureReason());
        assertEquals(parser.getEndTimeUtc(), jobProperties.getEndTimeUtc());
        assertEquals(parser.getStartTimeUtc(), jobProperties.getStartTimeUtc());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_JOB_PROPERTIES_34_004: [If the provided jobId is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void cannotSetJobIdToNull()
    {
        new JobProperties().setJobId(null);
    }

    private JobPropertiesParser toJobPropertiesParser(JobProperties jobProperties)
    {
        return Deencapsulation.invoke(jobProperties, "toJobPropertiesParser");
    }

    private JobProperties jobPropertiesConstructorWithParser(JobPropertiesParser parser)
    {
        return Deencapsulation.newInstance(JobProperties.class, new Class[] { JobPropertiesParser.class }, parser);
    }
}
