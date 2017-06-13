package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.microsoft.azure.sdk.iot.deps.serializer.JobsParser;
import com.microsoft.azure.sdk.iot.deps.serializer.JobsType;
import com.microsoft.azure.sdk.iot.deps.serializer.MethodParser;
import com.microsoft.azure.sdk.iot.deps.serializer.TwinParser;

import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

/**
 * Unit tests for jobs serializer
 * 100% methods, 100% lines covered
 */
public class JobsParserTest
{

    private MethodParser makeMethodSample()
    {
        String methodName = "testMethodName";
        Long responseTimeout = 100L;
        Long connectTimeout = 5L;
        Object payload = "{\"testPayload\":\"Success\"}";

        return new MethodParser(methodName, responseTimeout, connectTimeout, payload);
    }


    private TwinParser makeTwinSample()
    {
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        twinParser.updateTwin("{\"tags\":{" +
                "\"tag1\":{\"KeyChar\":\"c\",\"KeyBool\":true,\"keyString\":\"value1\",\"keyEnum\":\"val1\",\"keyDouble\":1234.456}}," +
                "\"properties\":{" +
                "\"desired\":{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}," +
                "\"reported\":{\"key1\":\"value1\",\"key3\":\"value3\"}}}");
        return twinParser;
    }

    /* Tests_SRS_JOBSPARSER_21_001: [The constructor shall evaluate and store the commons parameters using the internal function commonFields.] */
    /* Tests_SRS_JOBSPARSER_21_014: [The commonFields shall store the jobId, queryCondition, and maxExecutionTimeInSeconds.] */
    @Test
    public void constructorMethodCommonParsSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        assertEquals(jobId, Deencapsulation.getField(jobsParser, "jobId"));
        assertEquals(queryCondition, Deencapsulation.getField(jobsParser, "queryCondition"));
        assertEquals(maxExecutionTimeInSeconds, Deencapsulation.getField(jobsParser, "maxExecutionTimeInSeconds"));
    }

    /* Tests_SRS_JOBSPARSER_21_001: [The constructor shall evaluate and store the commons parameters using the internal function commonFields.] */
    /* Tests_SRS_JOBSPARSER_21_018: [The commonFields shall format startTime as a String and store it.] */
    @Test
    public void constructorMethodFormatStartTimeSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        Helpers.assertDateWithError(startTime, (String)Deencapsulation.getField(jobsParser, "startTime"));
    }

    /* Tests_SRS_JOBSPARSER_21_002: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_015: [If the jobId is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorMethodNullJobIdThrows()
    {
        // Arrange
        String jobId = null;
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_002: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_015: [If the jobId is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorMethodEmptyJobIdThrows()
    {
        // Arrange
        String jobId = "";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_002: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_015: [If the jobId is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorMethodInvalidJobIdThrows()
    {
        // Arrange
        String jobId = "test\u1234JobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_002: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_016: [If the queryCondition is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorMethodNullQueryConditionThrows()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = null;
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_002: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_016: [If the queryCondition is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorMethodEmptyQueryConditionThrows()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_002: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_016: [If the queryCondition is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorMethodInvalidQueryConditionThrows()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "test\u1234DeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_002: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_017: [If the maxExecutionTimeInSeconds is negative, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorMethodInvalidMaxExecutionTimeInSecondsThrows()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = -10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_002: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_019: [If the startTime is null, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorMethodNullStartTimeThrows()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = null;
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_003: [The constructor shall store the JsonElement for the cloudToDeviceMethod.] */
    @Test
    public void constructorMethodStoreCloudToDeviceMethodSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        Helpers.assertJson(cloudToDeviceMethod.toJsonElement().toString(), Deencapsulation.getField(jobsParser, "cloudToDeviceMethod").toString());
    }

    /* Tests_SRS_JOBSPARSER_21_004: [If the cloudToDeviceMethod is null, the constructor shall throws IllegalArgumentException.] */
    @Test(expected = IllegalArgumentException.class)
    public void constructorMethodNullCloudToDeviceMethodThrows()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = null;

        // Act
        JobsParser jobsParser= new JobsParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_005: [The constructor shall set the jobType as scheduleDirectRequest.] */
    @Test
    public void constructorMethodSetJobTypeSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        assertEquals(JobsType.scheduleDirectRequest, Deencapsulation.getField(jobsParser, "jobType"));
    }

    /* Tests_SRS_JOBSPARSER_21_006: [The constructor shall set the updateTwin as null.] */
    @Test
    public void constructorMethodSetUpdateTwinSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        assertNull(Deencapsulation.getField(jobsParser, "updateTwin"));
    }

    /* Tests_SRS_JOBSPARSER_21_013: [The toJson shall return a String with a json that represents the content of this class.] */
    @Test
    public void toJsonMethodSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();
        JobsParser jobsParser= new JobsParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);

        String expectedJson =
                "{" +
                        "\"jobId\":\"" + jobId + "\"," +
                        "\"type\":\"scheduleDirectRequest\"," +
                        "\"cloudToDeviceMethod\":" + cloudToDeviceMethod.toJson() + "," +
                        "\"queryCondition\":\"" + queryCondition + "\"," +
                        "\"startTime\":\"" + Helpers.formatUTC(startTime) + "\"," +
                        "\"maxExecutionTimeInSeconds\":" + maxExecutionTimeInSeconds +
                "}";

        // Act
        String json = jobsParser.toJson();

        // Assert
        Helpers.assertJson(json,expectedJson);
    }




    /* Tests_SRS_JOBSPARSER_21_007: [The constructor shall evaluate and store the commons parameters using the internal function commonFields.] */
    /* Tests_SRS_JOBSPARSER_21_014: [The commonFields shall store the jobId, queryCondition, and maxExecutionTimeInSeconds.] */
    @Test
    public void ConstructorTwinCommonParsSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinParser twinParser = makeTwinSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, twinParser, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        assertEquals(jobId, Deencapsulation.getField(jobsParser, "jobId"));
        assertEquals(queryCondition, Deencapsulation.getField(jobsParser, "queryCondition"));
        assertEquals(maxExecutionTimeInSeconds, Deencapsulation.getField(jobsParser, "maxExecutionTimeInSeconds"));
    }

    /* Tests_SRS_JOBSPARSER_21_007: [The constructor shall evaluate and store the commons parameters using the internal function commonFields.] */
    /* Tests_SRS_JOBSPARSER_21_018: [The commonFields shall format startTime as a String and store it.] */
    @Test
    public void ConstructorTwinFormatStartTimeSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinParser twinParser = makeTwinSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, twinParser, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        Helpers.assertDateWithError(startTime, (String)Deencapsulation.getField(jobsParser, "startTime"));
    }

    /* Tests_SRS_JOBSPARSER_21_008: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_015: [If the jobId is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void ConstructorTwinNullJobIdThrows()
    {
        // Arrange
        String jobId = null;
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinParser twinParser = makeTwinSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, twinParser, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_008: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_015: [If the jobId is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void ConstructorEmptyJobIdThrows()
    {
        // Arrange
        String jobId = "";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinParser twinParser = makeTwinSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, twinParser, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_008: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_015: [If the jobId is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void ConstructorInvalidJobIdThrows()
    {
        // Arrange
        String jobId = "test\u1234JobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinParser twinParser = makeTwinSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, twinParser, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_008: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_016: [If the queryCondition is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void ConstructorNullQueryConditionThrows()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = null;
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinParser twinParser = makeTwinSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, twinParser, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_008: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_016: [If the queryCondition is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void ConstructorEmptyQueryConditionThrows()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinParser twinParser = makeTwinSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, twinParser, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_008: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_016: [If the queryCondition is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void ConstructorInvalidQueryConditionThrows()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "test\u1234DeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinParser twinParser = makeTwinSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, twinParser, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_008: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_017: [If the maxExecutionTimeInSeconds is negative, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void ConstructorInvalidMaxExecutionTimeInSecondsThrows()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = -10L;
        TwinParser twinParser = makeTwinSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, twinParser, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_008: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_019: [If the startTime is null, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void ConstructorNullStartTimeThrows()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = null;
        long maxExecutionTimeInSeconds = 10L;
        TwinParser twinParser = makeTwinSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, twinParser, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_009: [The constructor shall store the JsonElement for the updateTwin.] */
    @Test
    public void ConstructorTwinStoreUpdateTwinSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinParser twinParser = makeTwinSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, twinParser, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        Helpers.assertJson(twinParser.toJsonElement().toString(), Deencapsulation.getField(jobsParser, "updateTwin").toString());
    }

    /* Tests_SRS_JOBSPARSER_21_010: [If the updateTwin is null, the constructor shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void ConstructorNullUpdateTwinThrows()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinParser twinParser = null;

        // Act
        JobsParser jobsParser= new JobsParser(jobId, twinParser, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_011: [The constructor shall set the jobType as scheduleTwinUpdate.] */
    @Test
    public void constructorTwinSetJobTypeSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinParser twinParser = makeTwinSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, twinParser, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        assertEquals(JobsType.scheduleTwinUpdate, Deencapsulation.getField(jobsParser, "jobType"));
    }

    /* Tests_SRS_JOBSPARSER_21_012: [The constructor shall set the cloudToDeviceMethod as null.] */
    @Test
    public void ConstructorTwinSetCloudToDeviceMethodAsNullSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinParser twinParser = makeTwinSample();

        // Act
        JobsParser jobsParser= new JobsParser(jobId, twinParser, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        assertNull(Deencapsulation.getField(jobsParser, "cloudToDeviceMethod"));
    }

    /* Tests_SRS_JOBSPARSER_21_013: [The toJson shall return a String with a json that represents the content of this class.] */
    @Test
    public void toJsonTwinSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinParser twinParser = makeTwinSample();
        JobsParser jobsParser= new JobsParser(jobId, twinParser, queryCondition, startTime, maxExecutionTimeInSeconds);

        String expectedJson =
                "{" +
                        "\"jobId\":\"" + jobId + "\"," +
                        "\"type\":\"scheduleTwinUpdate\"," +
                        "\"cloudToDeviceMethod\":" + twinParser.toJson() + "," +
                        "\"queryCondition\":\"" + queryCondition + "\"," +
                        "\"startTime\":\"" + Helpers.formatUTC(startTime) + "\"," +
                        "\"maxExecutionTimeInSeconds\":" + maxExecutionTimeInSeconds +
                "}";

        // Act
        String json = jobsParser.toJson();

        // Assert
        Helpers.assertJson(json,expectedJson);
    }

}
