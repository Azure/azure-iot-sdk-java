// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.microsoft.azure.sdk.iot.deps.serializer.JobPropertiesParser;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import mockit.Deencapsulation;
import org.junit.Test;

import java.util.Date;

import static junit.framework.TestCase.assertEquals;

/**
 * Code Coverage
 * Methods: 100%
 * Lines: 100%
 */
public class JobPropertiesParserTest
{
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_001: [The constructor shall create and return an instance of a JobPropertiesParser object based off the provided json.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_002: [This method shall return a json representation of this.]
    @Test
    public void testBasicFunctionality()
    {
        // arrange
        Date date = new Date();

        JobPropertiesParser parser = new JobPropertiesParser();
        parser.setEndTimeUtc(date);
        parser.setStartTimeUtc(date);
        parser.setFailureReason("failureReason");
        parser.setInputBlobContainerUri("inputContainerUri");
        parser.setOutputBlobContainerUri("outputContainerUri");
        parser.setProgress(0);
        parser.setExcludeKeysInExport(false);
        parser.setJobId("jobId");
        parser.setStatus("status");
        parser.setType("type");

        // act
        JobPropertiesParser processedParser = new JobPropertiesParser(parser.toJson());

        // assert
        assertEquals(parser.isExcludeKeysInExport(), processedParser.isExcludeKeysInExport());
        assertEquals(parser.getFailureReason(), processedParser.getFailureReason());
        assertEquals(parser.getJobId(), processedParser.getJobId());
        assertEquals(parser.getInputBlobContainerUri(), processedParser.getInputBlobContainerUri());
        assertEquals(parser.getOutputBlobContainerUri(), processedParser.getOutputBlobContainerUri());
        assertEquals(parser.getProgress(), processedParser.getProgress());
        assertEquals(parser.getStatus(), processedParser.getStatus());
        assertEquals(parser.getType(), processedParser.getType());


        String startTimeUtc = Deencapsulation.getField(processedParser, "startTimeUtcString");
        String endTimeUtc = Deencapsulation.getField(processedParser, "startTimeUtcString");
        assertEquals(ParserUtility.getDateStringFromDate(parser.getStartTimeUtc()), startTimeUtc);
        assertEquals(ParserUtility.getDateStringFromDate(parser.getEndTimeUtc()), endTimeUtc);
    }

    //Tests_SRS_JOB_PROPERTIES_PARSER_34_010: [This method shall set the value of this object's JobId equal to the provided value.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_011: [This method shall set the value of this object's type equal to the provided value.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_012: [This method shall return the value of this object's type.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_013: [This method shall set the value of this object's inputBlobContainerUri equal to the provided value.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_014: [This method shall return the value of this object's inputBlobContainerUri.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_015: [This method shall set the value of this object's outputBlobContainerUri equal to the provided value.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_016: [This method shall return the value of this object's outputBlobContainerUri.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_018: [This method shall return the value of this object's jobId.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_019: [This method shall set the value of this object's startTimeUtc equal to the provided value.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_020: [This method shall return the value of this object's startTimeUtc.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_021: [This method shall set the value of this object's endTimeUtc equal to the provided value.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_022: [This method shall return the value of this object's endTimeUtc.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_023: [This method shall set the value of this object's status equal to the provided value.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_024: [This method shall return the value of this object's status.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_025: [This method shall set the value of this object's progress equal to the provided value.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_026: [This method shall return the value of this object's progress.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_027: [This method shall set the value of this object's excludeKeysInExport equal to the provided value.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_028: [This method shall return the value of this object's excludeKeysInExport.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_029: [This method shall set the value of this object's failureReason equal to the provided value.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_030: [This method shall return the value of this object's failureReason.]
    @Test
    public void testGettersAndSetters()
    {
        //arrange
        Date date = new Date();
        String failureReason = "failureReason";
        String inputContainerUri = "inputContainerUri";
        String outputContainerUri = "outputContainerUri";
        int progress = 2;
        boolean excludeKeysInExport = false;
        String jobId = "jobId";
        String status = "status";
        String type = "type";

        JobPropertiesParser parser = new JobPropertiesParser();

        //act
        parser.setEndTimeUtc(date);
        parser.setStartTimeUtc(date);
        parser.setFailureReason(failureReason);
        parser.setInputBlobContainerUri(inputContainerUri);
        parser.setOutputBlobContainerUri(outputContainerUri);
        parser.setProgress(progress);
        parser.setExcludeKeysInExport(excludeKeysInExport);
        parser.setJobId(jobId);
        parser.setStatus(status);
        parser.setType(type);

        //assert
        assertEquals(date, parser.getStartTimeUtc());
        assertEquals(date, parser.getEndTimeUtc());
        assertEquals(failureReason, parser.getFailureReason());
        assertEquals(inputContainerUri, parser.getInputBlobContainerUri());
        assertEquals(outputContainerUri, parser.getOutputBlobContainerUri());
        assertEquals(progress, parser.getProgress());
        assertEquals(excludeKeysInExport, parser.isExcludeKeysInExport());
        assertEquals(jobId, parser.getJobId());
        assertEquals(status, parser.getStatus());
        assertEquals(type, parser.getType());
    }

    //Tests_SRS_JOB_PROPERTIES_PARSER_34_007: [If the provided json is null or empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullJson()
    {
        //act
        new JobPropertiesParser(null);
    }

    //Tests_SRS_JOB_PROPERTIES_PARSER_34_007: [If the provided json is null or empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForEmptyJson()
    {
        //act
        new JobPropertiesParser("");
    }

    //Tests_SRS_JOB_PROPERTIES_PARSER_34_008: [If the provided json cannot be parsed into a JobPropertiesParser object, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForInvalidJson()
    {
        //act
        new JobPropertiesParser("}");
    }

    //Tests_SRS_JOB_PROPERTIES_PARSER_34_005: [If the provided jobId is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void jobIdCannotBeSetToNull()
    {
        //arrange
        JobPropertiesParser parser = new JobPropertiesParser();

        //act
        parser.setJobId(null);
    }

    //Tests_SRS_JOB_PROPERTIES_PARSER_34_009: [If the provided json is missing the field for jobId, or if its value is null or empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorJsonMissingJobIdInJsonThrowsIllegalArgumentException()
    {
        //arrange
        String json = "{\n" +
                "  \"startTimeUtc\": \"Dec 31, 1969 4:00:00 PM\",\n" +
                "  \"endTimeUtc\": \"Dec 31, 1969 4:00:00 PM\",\n" +
                "  \"type\": \"type\",\n" +
                "  \"status\": \"status\",\n" +
                "  \"progress\": 0,\n" +
                "  \"inputBlobContainerUri\": \"inputContainerUri\",\n" +
                "  \"outputBlobContainerUri\": \"outputContainerUri\",\n" +
                "  \"excludeKeysInExport\": false,\n" +
                "  \"failureReason\": \"failureReason\"\n" +
                "}";

        //act
        new JobPropertiesParser(json);
    }

    //Tests_SRS_JOB_PROPERTIES_PARSER_34_009: [If the provided json is missing the field for jobId, or if its value is null or empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorJsonWithJobIdMissingValueInJsonThrowsIllegalArgumentException()
    {
        //arrange
        String json = "{\n" +
                "  \"jobId\": \"\",\n" +
                "  \"startTimeUtc\": \"Dec 31, 1969 4:00:00 PM\",\n" +
                "  \"endTimeUtc\": \"Dec 31, 1969 4:00:00 PM\",\n" +
                "  \"type\": \"type\",\n" +
                "  \"status\": \"status\",\n" +
                "  \"progress\": 0,\n" +
                "  \"inputBlobContainerUri\": \"inputContainerUri\",\n" +
                "  \"outputBlobContainerUri\": \"outputContainerUri\",\n" +
                "  \"excludeKeysInExport\": false,\n" +
                "  \"failureReason\": \"failureReason\"\n" +
                "}";

        //act
        new JobPropertiesParser(json);
    }
}