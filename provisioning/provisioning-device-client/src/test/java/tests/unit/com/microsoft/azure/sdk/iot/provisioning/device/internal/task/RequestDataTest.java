/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *  
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.device.internal.task;

import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.RequestData;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.net.ssl.SSLContext;

import java.util.Random;

import static mockit.Deencapsulation.invoke;
import static mockit.Deencapsulation.newInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/*
    Unit test for RequestData
    Coverage : 100% method, 100% line
 */
@RunWith(JMockit.class)
public class RequestDataTest
{
    private static final byte[] TEST_EK = "testEk".getBytes();
    private static final byte[] TEST_SRK = "testSRk".getBytes();
    private static final String TEST_REG = "testReg";
    private static final String TEST_OP = "testOp";
    private static final String TEST_SASTOKEN = "testSasToken";

    @Mocked
    SSLContext mockedSslContext;

    //SRS_RequestData_25_001: [ Constructor shall save all the parameters and ignore the null parameters. ]
    @Test
    public void constructorOneSucceeds() throws Exception
    {
        //act
        RequestData testRequestData = newInstance(RequestData.class, new Class[] {byte[].class, byte[].class, String.class,
                                                                        SSLContext.class, String.class},
                                                  TEST_EK, TEST_SRK, TEST_REG, mockedSslContext, TEST_SASTOKEN);


        //assert
        assertEquals(TEST_EK, testRequestData.getEndorsementKey());
        assertEquals(TEST_SRK, testRequestData.getStorageRootKey());
        assertEquals(TEST_REG, testRequestData.getRegistrationId());
        assertNull(testRequestData.getOperationId());
        assertEquals(mockedSslContext, testRequestData.getSslContext());
        assertEquals(TEST_SASTOKEN, testRequestData.getSasToken());
    }

    //SRS_RequestData_25_001: [ Constructor shall save all the parameters and ignore the null parameters. ]
    @Test
    public void constructorTwoSucceeds() throws Exception
    {
        //act
        RequestData testRequestData = newInstance(RequestData.class, new Class[] {String.class,
                                                          SSLContext.class, String.class},
                                                  TEST_REG, mockedSslContext, TEST_SASTOKEN);


        //assert
        assertNull(testRequestData.getEndorsementKey());
        assertNull(testRequestData.getStorageRootKey());
        assertEquals(TEST_REG, testRequestData.getRegistrationId());
        assertNull(testRequestData.getOperationId());
        assertEquals(mockedSslContext, testRequestData.getSslContext());
        assertEquals(TEST_SASTOKEN, testRequestData.getSasToken());
    }

    //SRS_RequestData_25_001: [ Constructor shall save all the parameters and ignore the null parameters. ]
    @Test
    public void constructorThreeSucceeds() throws Exception
    {
        //act
        RequestData testRequestData = newInstance(RequestData.class, new Class[] {String.class,
                                                          String.class, SSLContext.class, String.class},
                                                  TEST_REG, TEST_OP, mockedSslContext, TEST_SASTOKEN);


        //assert
        assertNull(testRequestData.getEndorsementKey());
        assertNull(testRequestData.getStorageRootKey());
        assertEquals(TEST_REG, testRequestData.getRegistrationId());
        assertEquals(TEST_OP, testRequestData.getOperationId());
        assertEquals(mockedSslContext, testRequestData.getSslContext());
        assertEquals(TEST_SASTOKEN, testRequestData.getSasToken());
    }

    //SRS_RequestData_25_001: [ Constructor shall save all the parameters and ignore the null parameters. ]
    @Test
    public void constructorFourSucceeds() throws Exception
    {
        //act
        RequestData testRequestData = newInstance(RequestData.class, new Class[] {String.class,
                                                          SSLContext.class, boolean.class},
                                                  TEST_REG, mockedSslContext, true);


        //assert
        assertNull(testRequestData.getEndorsementKey());
        assertNull(testRequestData.getStorageRootKey());
        assertEquals(TEST_REG, testRequestData.getRegistrationId());
        assertNull(testRequestData.getOperationId());
        assertEquals(mockedSslContext, testRequestData.getSslContext());
        assertNull(testRequestData.getSasToken());
        assertTrue(testRequestData.isX509());
    }
    //SRS_RequestData_25_002: [ This method shall retrieve endorsementKey. ]
    //SRS_RequestData_25_003: [ This method shall set endorsementKey. ]
    //SRS_RequestData_25_004: [ This method shall retrieve storageRootKey. ]
    //SRS_RequestData_25_005: [ This method shall set storageRootKey. ]
    //SRS_RequestData_25_006: [ This method shall retrieve registrationId. ]
    //SRS_RequestData_25_007: [ This method shall set registrationId. ]
    //SRS_RequestData_25_008: [ This method shall retrieve operationId. ]
    //SRS_RequestData_25_009: [ This method shall set operationId. ]
    //SRS_RequestData_25_010: [ This method shall retrieve sslContext. ]
    //SRS_RequestData_25_011: [ This method shall set sslContext. ]
    //SRS_RequestData_25_012: [ This method shall retrieve sasToken. ]
    //SRS_RequestData_25_013: [ This method shall set sasToken. ]
    @Test
    public void setAndGetEkAndSRKSucceeds() throws Exception
    {
        String random = new Random().toString();
        //act
        //act
        RequestData testRequestData = newInstance(RequestData.class, new Class[] {byte[].class, byte[].class, String.class,
                                                          SSLContext.class, String.class},
                                                  TEST_EK, TEST_SRK, TEST_REG, mockedSslContext, TEST_SASTOKEN);

        //act
        invoke(testRequestData, "setSasToken", TEST_SASTOKEN + random);
        invoke(testRequestData, "setSslContext", mockedSslContext);


        //assert
        assertEquals(TEST_EK, testRequestData.getEndorsementKey());
        assertEquals(TEST_SRK, testRequestData.getStorageRootKey());
        assertEquals(TEST_REG, testRequestData.getRegistrationId());
        assertNull(testRequestData.getOperationId());
        assertEquals(TEST_SASTOKEN + random, testRequestData.getSasToken());
        assertEquals(mockedSslContext, testRequestData.getSslContext());
    }

    @Test
    public void setAndGetWithOpIdSucceeds() throws Exception
    {
        String random = new Random().toString();
        //act
        //act
        RequestData testRequestData = newInstance(RequestData.class, new Class[] {String.class,
                                                          String.class, SSLContext.class, String.class},
                                                  TEST_REG, TEST_OP, mockedSslContext, TEST_SASTOKEN);



        //act
        invoke(testRequestData, "setSasToken", TEST_SASTOKEN + random);
        invoke(testRequestData, "setSslContext", mockedSslContext);

        //assert
        assertNull(testRequestData.getEndorsementKey());
        assertNull(testRequestData.getStorageRootKey());
        assertEquals(TEST_REG, testRequestData.getRegistrationId());
        assertEquals(TEST_OP, testRequestData.getOperationId());
        assertEquals(mockedSslContext, testRequestData.getSslContext());
        assertEquals(TEST_SASTOKEN + random, testRequestData.getSasToken());
    }

    @Test
    public void setAndGetWithRegIdSucceeds() throws Exception
    {
        String random = new Random().toString();
        //act
        //act
        RequestData testRequestData = newInstance(RequestData.class, new Class[] {String.class,
                                                          SSLContext.class, String.class},
                                                  TEST_REG, mockedSslContext, TEST_SASTOKEN);

        //act
        invoke(testRequestData, "setSasToken", TEST_SASTOKEN + random);
        invoke(testRequestData, "setSslContext", mockedSslContext);

        //assert
        assertNull(testRequestData.getEndorsementKey());
        assertNull(testRequestData.getStorageRootKey());
        assertEquals(TEST_REG, testRequestData.getRegistrationId());
        assertNull(testRequestData.getOperationId());
        assertEquals(mockedSslContext, testRequestData.getSslContext());
        assertEquals(TEST_SASTOKEN + random, testRequestData.getSasToken());
        assertEquals(mockedSslContext, testRequestData.getSslContext());
    }
}
