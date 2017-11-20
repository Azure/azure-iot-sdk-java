/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.service.devicetwin;

public class QueryOptions
{
    private static final Integer DEFAULT_PAGE_SIZE = 100;
    private Integer pageSize;

    private String continuationToken;

    /**
     * Constructor for the default QueryOptions object. No continuation token is used, and a default page size is set
     */
    public QueryOptions()
    {
        //Codes_SRS_QUERYOPTIONS_34_001: [This constructor shall initialize a QueryOptions object with a default page size of 100 and no continuation token.]
        this.continuationToken = null;
        this.pageSize = DEFAULT_PAGE_SIZE;
    }

    /**
     * Getter for ContinuationToken
     *
     * @return The value of ContinuationToken
     */
    public String getContinuationToken()
    {
        //Codes_SRS_QUERYOPTIONS_34_002: [This function shall return the saved continuation token.]
        return continuationToken;
    }

    /**
     * Setter for ContinuationToken
     *
     * @param continuationToken the value to set
     * @throws IllegalArgumentException if continuationToken is null
     */
    public void setContinuationToken(String continuationToken) throws IllegalArgumentException
    {
        //Codes_SRS_QUERYOPTIONS_34_004: [If the provided continuation token is null or empty, an IllegalArgumentException shall be thrown.]
        if (continuationToken == null || continuationToken.isEmpty())
        {
            throw new IllegalArgumentException("continuationToken cannot be null");
        }

        //Codes_SRS_QUERYOPTIONS_34_006: [This function shall save the provided continuation token string.]
        this.continuationToken = continuationToken;
    }

    /**
     * Getter for PageSize
     *
     * @return The value of PageSize
     */
    public Integer getPageSize()
    {
        //Codes_SRS_QUERYOPTIONS_34_003: [This function shall return the saved page size.]
        return pageSize;
    }

    /**
     * Setter for PageSize
     *
     * @param pageSize the value to set
     * @throws IllegalArgumentException if pageSize is null or not a positive integer
     */
    public void setPageSize(Integer pageSize) throws IllegalArgumentException
    {
        //Codes_SRS_QUERYOPTIONS_34_005: [If the provided page size is null or is not a positive integer, an IllegalArgumentException shall be thrown.]
        if (pageSize == null || pageSize < 1)
        {
            throw new IllegalArgumentException("pageSize");
        }

        //Codes_SRS_QUERYOPTIONS_34_007: [This function shall save the provided page size.]
        this.pageSize = pageSize;
    }
}
