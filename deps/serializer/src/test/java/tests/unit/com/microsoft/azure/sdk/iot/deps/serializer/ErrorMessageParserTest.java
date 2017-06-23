// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;
import com.microsoft.azure.sdk.iot.deps.serializer.ErrorMessageParser;
import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

/**
 * Unit tests for jobs serializer
 * 100% methods, 95% lines covered
 */
public class ErrorMessageParserTest
{
    /* Codes_SRS_ERROR_MESSAGE_PARSER_21_001: [The bestErrorMessage shall parse the fullErrorMessage as json with format {"Message":"ErrorCode:[error]","ExceptionMessage":"Tracking ID:[tracking id]-TimeStamp:[dateTime]"}.] */
    /* Codes_SRS_ERROR_MESSAGE_PARSER_21_005: [The bestErrorMessage shall return a String with the rootMessage and rootException.] */
    @Test
    public void bestErrorMessageParseErrorMessage()
    {
        // arrange
        final String errorReason = "{\"Message\":\"ErrorCode:IotHubUnauthorizedAccess;Unauthorized\",\"ExceptionMessage\":\"Tracking ID:(tracking id)-TimeStamp:12/14/2016 03:15:17\"}";

        // act
        String bestMessage = ErrorMessageParser.bestErrorMessage(errorReason);

        // assert
        assertEquals("ErrorCode:IotHubUnauthorizedAccess;Unauthorized Tracking ID:(tracking id)-TimeStamp:12/14/2016 03:15:17", bestMessage);
    }

    /* Codes_SRS_ERROR_MESSAGE_PARSER_21_002: [If the bestErrorMessage failed to parse the fullErrorMessage as json, it shall return the fullErrorMessage as is.] */
    @Test
    public void bestErrorMessageContainsUnformattedError()
    {
        // arrange
        final String errorReason = "ErrorCode:IotHubUnauthorizedAccess;Unauthorized";

        // act
        String bestMessage = ErrorMessageParser.bestErrorMessage(errorReason);

        // assert
        assertEquals("ErrorCode:IotHubUnauthorizedAccess;Unauthorized", bestMessage);
    }

    /* Codes_SRS_ERROR_MESSAGE_PARSER_21_003: [If the fullErrorMessage contains inner Messages, the bestErrorMessage shall parse the inner message.] */
    /* Codes_SRS_ERROR_MESSAGE_PARSER_21_004: [The bestErrorMessage shall use the most inner message as the root cause.] */
    @Test
    public void bestErrorMessageContainsInnerErrorMessage()
    {
        // arrange
        final String errorReason = "{\"Message\":\"ErrorCode:ArgumentInvalid;Error: BadRequest {\\\"Message\\\":\\\"ErrorCode:ArgumentInvalid;Missing or invalid etag for job type ScheduleUpdateTwin. ScheduleUpdateTwin job type is a force update, which only accepts '*' as the Etag.\\\",\\\"ExceptionMessage\\\":\\\"Tracking ID:1234-TimeStamp:06/26/2017 20:56:33\\\"}\",\"ExceptionMessage\":\"Tracking ID:5678-G:10-TimeStamp:06/26/2017 20:56:33\"}";

        // act
        String bestMessage = ErrorMessageParser.bestErrorMessage(errorReason);

        // assert
        assertEquals("ErrorCode:ArgumentInvalid;Missing or invalid etag for job type ScheduleUpdateTwin. ScheduleUpdateTwin job type is a force update, which only accepts '*' as the Etag. Tracking ID:1234-TimeStamp:06/26/2017 20:56:33", bestMessage);
    }

    /* Codes_SRS_ERROR_MESSAGE_PARSER_21_006: [If the fullErrorMessage do not have rootException, the bestErrorMessage shall return only the rootMessage.] */
    @Test
    public void bestErrorMessageParseErrorMessageWithNoExceptionMessage()
    {
        // arrange
        final String errorReason = "{\"Message\":\"ErrorCode:IotHubUnauthorizedAccess;Unauthorized\"}";

        // act
        String bestMessage = ErrorMessageParser.bestErrorMessage(errorReason);

        // assert
        assertEquals("ErrorCode:IotHubUnauthorizedAccess;Unauthorized", bestMessage);
    }

    /* Codes_SRS_ERROR_MESSAGE_PARSER_21_007: [If the inner message do not have rootException, the bestErrorMessage shall use the parent rootException.] */
    @Test
    public void bestErrorMessageContainsInnerErrorMessageWithNoExceptionMessage()
    {
        // arrange
        final String errorReason = "{\"Message\":\"ErrorCode:ArgumentInvalid;Error: BadRequest {\\\"Message\\\":\\\"ErrorCode:ArgumentInvalid;Missing or invalid etag for job type ScheduleUpdateTwin. ScheduleUpdateTwin job type is a force update, which only accepts '*' as the Etag.\\\"}\",\"ExceptionMessage\":\"Tracking ID:5678-G:10-TimeStamp:06/26/2017 20:56:33\"}";

        // act
        String bestMessage = ErrorMessageParser.bestErrorMessage(errorReason);

        // assert
        assertEquals("ErrorCode:ArgumentInvalid;Missing or invalid etag for job type ScheduleUpdateTwin. ScheduleUpdateTwin job type is a force update, which only accepts '*' as the Etag. Tracking ID:5678-G:10-TimeStamp:06/26/2017 20:56:33", bestMessage);
    }

    /* Codes_SRS_ERROR_MESSAGE_PARSER_21_008: [If the fullErrorMessage is null or empty, the bestErrorMessage shall return as is (null or empty).] */
    @Test
    public void bestErrorMessageNullErrorMessage()
    {
        // arrange
        final String errorReason = null;

        // act
        String bestMessage = ErrorMessageParser.bestErrorMessage(errorReason);

        // assert
        assertEquals("", bestMessage);
    }

    /* Codes_SRS_ERROR_MESSAGE_PARSER_21_008: [If the fullErrorMessage is null or empty, the bestErrorMessage shall return an empty String.] */
    @Test
    public void bestErrorMessageEmptyErrorMessage()
    {
        // arrange
        final String errorReason = "";

        // act
        String bestMessage = ErrorMessageParser.bestErrorMessage(errorReason);

        // assert
        assertEquals("", bestMessage);
    }
}
