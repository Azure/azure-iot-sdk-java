/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.microsoft.azure.sdk.iot.deps.serializer.JobQueryResponseError;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/*
    Unit tests for JobQueryResponseError
    Coverage : method 100%, line 100%
 */
public class JobQueryResponseErrorTest
{
    @Test
    public void fromJsonSucceeds() throws IOException
    {
        final String errorJson =
                "{" +
                    "\"code\":\"JobRunPreconditionFailed\"," +
                    "\"description\":\"The job did not start within specified period: either device did not come online or invalid endTime specified.\"" +
                 "}";


        JobQueryResponseError jobQueryResponseError = new JobQueryResponseError().fromJson(errorJson);
    }

    //Tests_SRS_JOB_QUERY_RESPONSE_ERROR_25_006: [This method shall throw IOException if parsing of json fails for any reason.]
    @Test (expected = IOException.class)
    public void fromJsonThrowsOnInvalidJson() throws IOException
    {
        final String errorJson =
                "" +
                        "\"code\":\"JobRunPreconditionFailed\"," +
                        "\"description\":\"The job did not start within specified period: either device did not come online or invalid endTime specified.\"" +
                        "}";


        JobQueryResponseError jobQueryResponseError = new JobQueryResponseError().fromJson(errorJson);
    }

    //Tests_SRS_JOB_QUERY_RESPONSE_ERROR_25_007: [If the input json is null or empty then this method shall throw IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void fromJsonThrowsNullJson() throws IOException
    {
        final String errorJson = null;

        JobQueryResponseError jobQueryResponseError = new JobQueryResponseError().fromJson(errorJson);
    }

    @Test  (expected = IllegalArgumentException.class)
    public void fromJsonThrowsEmptyJson() throws IOException
    {
        final String errorJson = "";

        JobQueryResponseError jobQueryResponseError = new JobQueryResponseError().fromJson(errorJson);
    }

    //Tests_SRS_JOB_QUERY_RESPONSE_ERROR_25_005: [This method shall throw IOException if either code and description is not present in the json.]
    @Test (expected = IllegalArgumentException.class)
    public void fromJsonThrowsOnNoCode() throws IOException
    {
        final String errorJson = "{" +
                "\"description\":\"The job did not start within specified period: either device did not come online or invalid endTime specified.\"" +
                "}";

        JobQueryResponseError jobQueryResponseError = new JobQueryResponseError().fromJson(errorJson);
    }

    @Test (expected = IllegalArgumentException.class)
    public void fromJsonThrowsOnNoDescription() throws IOException
    {
        final String errorJson = "{" +
                "\"code\":\"JobRunPreconditionFailed\"" +
                "}";

        JobQueryResponseError jobQueryResponseError = new JobQueryResponseError().fromJson(errorJson);
    }

    //Tests_SRS_JOB_QUERY_RESPONSE_ERROR_25_003: [The method shall build the json with the values provided to this object.]
    @Test
    public void toJsonSucceeds() throws IOException
    {
        final String errorJson =
                "{" +
                        "\"code\":\"JobRunPreconditionFailed\"," +
                        "\"description\":\"The job did not start within specified period: either device did not come online or invalid endTime specified.\"" +
                        "}";

        JobQueryResponseError jobQueryResponseError = new JobQueryResponseError().fromJson(errorJson);

        assertEquals(jobQueryResponseError.toJson(), errorJson);
    }

    //Tests_SRS_JOB_QUERY_RESPONSE_ERROR_25_001: [The getCode shall return the value of the code.]
    //Tests_SRS_JOB_QUERY_RESPONSE_ERROR_25_002: [The getDescription shall return the value of the Description.]
    @Test
    public void gettersSucceeds() throws IOException
    {
        final String errorJson =
                "{" +
                        "\"code\":\"JobRunPreconditionFailed\"," +
                        "\"description\":\"The job did not start within specified period: either device did not come online or invalid endTime specified.\"" +
                        "}";

        JobQueryResponseError jobQueryResponseError = new JobQueryResponseError().fromJson(errorJson);
        assertNotNull(jobQueryResponseError.getCode());
        assertNotNull(jobQueryResponseError.getDescription());
    }
}
