/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.unit.com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.service.devicetwin.QueryOptions;
import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for QueryOptions.java
 * Code coverage:
 * 100% Methods
 * 100% lines
 */
public class QueryOptionsTest
{
    //Tests_SRS_QUERYOPTIONS_34_001: [This constructor shall initialize a QueryOptions object with a default page size of 100 and no continuation token.]
    @Test
    public void constructorSuccess()
    {
        //act
        QueryOptions options = new QueryOptions();

        //assert
        Integer actualPageSize = Deencapsulation.getField(options, "pageSize");
        String actualContinuationToken = Deencapsulation.getField(options, "continuationToken");

        assertEquals(new Integer(100), actualPageSize);
        assertNull(actualContinuationToken);
    }


    //Tests_SRS_QUERYOPTIONS_34_002: [This function shall return the saved continuation token.]
    @Test
    public void getContinuationTokenSuccess()
    {
        //arrange
        String expectedContinuationToken = "someToken";
        QueryOptions options = new QueryOptions();
        Deencapsulation.setField(options, "continuationToken", expectedContinuationToken);

        //act
        String actualContinuationToken = options.getContinuationToken();

        //assert
        assertEquals(expectedContinuationToken, actualContinuationToken);
    }

    //Tests_SRS_QUERYOPTIONS_34_004: [If the provided continuation token is null or empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void setContinuationTokenThrowsForNullContinuationToken()
    {
        //act
        new QueryOptions().setContinuationToken(null);
    }

    //Tests_SRS_QUERYOPTIONS_34_004: [If the provided continuation token is null or empty, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void setContinuationTokenThrowsForEmptyContinuationToken()
    {
        //act
        new QueryOptions().setContinuationToken("");
    }

    //Tests_SRS_QUERYOPTIONS_34_006: [This function shall save the provided continuation token string.]
    @Test
    public void setContinuationTokenSuccess()
    {
        //arrange
        String expectedContinuationToken = "someToken";
        QueryOptions options = new QueryOptions();

        //act
        options.setContinuationToken(expectedContinuationToken);

        //assert
        String actualContinuationToken = Deencapsulation.getField(options, "continuationToken");
        assertEquals(expectedContinuationToken, actualContinuationToken);
    }

    //Tests_SRS_QUERYOPTIONS_34_003: [This function shall return the saved page size.]
    @Test
    public void getPageSizeSuccess()
    {
        //arrange
        Integer expectedPageSize = new Integer(758);
        QueryOptions options = new QueryOptions();
        Deencapsulation.setField(options, "pageSize", expectedPageSize);

        //act
        Integer actualPageSize = options.getPageSize();

        //assert
        assertEquals(expectedPageSize, actualPageSize);
    }

    //Tests_SRS_QUERYOPTIONS_34_005: [If the provided page size is null or is not a positive integer, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void setPageSizeThrowsForNullPageSize()
    {
        //act
        new QueryOptions().setPageSize(null);
    }

    //Tests_SRS_QUERYOPTIONS_34_005: [If the provided page size is null or is not a positive integer, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void setPageSizeThrowsForZeroPageSize()
    {
        //act
        new QueryOptions().setPageSize(new Integer(0));
    }

    //Tests_SRS_QUERYOPTIONS_34_005: [If the provided page size is null or is not a positive integer, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void setPageSizeThrowsForNegativePageSize()
    {
        //act
        new QueryOptions().setPageSize(new Integer(-25));
    }

    //Tests_SRS_QUERYOPTIONS_34_007: [This function shall save the provided page size.]
    @Test
    public void setPageSizeSuccess()
    {
        //arrange
        Integer expectedPageSize = new Integer(758);
        QueryOptions options = new QueryOptions();

        //act
        options.setPageSize(expectedPageSize);

        //assert
        Integer actualPageSize = Deencapsulation.getField(options, "pageSize");
        assertEquals(expectedPageSize, actualPageSize);
    }
}
