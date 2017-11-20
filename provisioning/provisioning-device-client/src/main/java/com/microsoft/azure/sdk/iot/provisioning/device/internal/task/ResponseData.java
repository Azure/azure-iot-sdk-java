/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.task;

public class ResponseData
{
    private byte[] responseData;
    private ContractState contractState;
    private long waitForStatusInMS;

    /**
     * Constructor to create null data and Unknown contract state
     */
    ResponseData()
    {
        //SRS_ResponseData_25_001: [ Constructor shall create null responseData and set the contractState to DPS_REGISTRATION_UNKNOWN. ]
        this.responseData = null;
        this.contractState = ContractState.DPS_REGISTRATION_UNKNOWN;
        this.waitForStatusInMS = 0L;
    }

    /**
     * Contrautor for Response Data
     * @param responseData response data value. Can be {@code null}
     * @param contractState contract state value. Can be {@code null}
     * @param waitForStatusInMS Maximum time to wait before query status. Can be {@code null}
     */
    public ResponseData(byte[] responseData, ContractState contractState, long waitForStatusInMS)
    {
        this.responseData = responseData;
        this.contractState = contractState;
        this.waitForStatusInMS = waitForStatusInMS;
    }

    /**
     * Getter for response data
     * @return  Returns the byte array of response data
     */
    byte[] getResponseData()
    {
        //SRS_ResponseData_25_003: [ This method shall return the saved value of responseData. ]
        return responseData;
    }

    /**
     * Setter for Response data
     * @param responseData Value of response data to be set
     */
    public void setResponseData(byte[] responseData)
    {
        //SRS_ResponseData_25_002: [ This method shall save the value of responseData. ]
        this.responseData = responseData;
    }

    /**
     * Getter for the contract state
     * @return Returns the value of contract state
     */
    ContractState getContractState()
    {
        //SRS_ResponseData_25_005: [ This method shall return the saved value of contractState. ]
        return contractState;
    }

    /**
     * Setter for the contract state
     * @param contractState Sets the value of Contract state
     */
    public void setContractState(ContractState contractState)
    {
        //SRS_ResponseData_25_004: [ This method shall save the value of contractState. ]
        this.contractState = contractState;
    }

    /**
     * Getter for the Maximum Time in MilliSeconds
     * @return Maximum time value.
     */
    long getWaitForStatusInMS()
    {
        return waitForStatusInMS;
    }

    /**
     * Setter for Maximum Time in MilliSeconds
     * @param waitForStatusInMS Maximum time value. Can be {@code null}
     */
    public void setWaitForStatusInMS(long waitForStatusInMS)
    {
        this.waitForStatusInMS = waitForStatusInMS;
    }
}
