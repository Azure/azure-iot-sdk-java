/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.device.internal.task;

import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.ProvisioningDeviceClientConfig;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.RegistrationOperationStatusParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.TpmRegistrationResultParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.RequestData;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderX509;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ProvisioningDeviceClientContract;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.UrlPathBuilder;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.DeviceRegistrationParser;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.Authorization;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.RegisterTask;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ResponseData;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.json.JsonException;
import javax.net.ssl.SSLContext;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import static com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ContractState.DPS_REGISTRATION_RECEIVED;
import static com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ContractState.DPS_REGISTRATION_UNKNOWN;
import static org.junit.Assert.assertNotNull;

/*
    Unit Test for Register Task
    Coverage : 88% Method, 92% Line (private classes are non testable)
 */
@RunWith(JMockit.class)
public class RegisterTaskTest
{
    private static final String TEST_REGISTRATION_ID = "testRegistrationId";
    private static final String TEST_EK = "testEK";
    private static final String TEST_SRK = "testSRK";
    private static final String TEST_AUTH_KEY = "testAuthKey";

    @Mocked
    SecurityProvider mockedSecurityProvider;
    @Mocked
    SecurityProviderTpm mockedSecurityProviderTpm;
    @Mocked
    SecurityProviderX509 mockedDpsSecurityProviderX509;
    @Mocked
    ProvisioningDeviceClientContract mockedProvisioningDeviceClientContract;
    @Mocked
    ProvisioningDeviceClientConfig mockedProvisioningDeviceClientConfig;
    @Mocked
    Authorization mockedAuthorization;
    @Mocked
    DeviceRegistrationParser mockedDeviceRegistrationParser;
    @Mocked
    UrlPathBuilder mockedUrlPathBuilder;
    @Mocked
    Base64 mockedBase64;
    @Mocked
    URLEncoder mockedUrlEncoder;
    @Mocked
    TpmRegistrationResultParser mockedTpmRegistrationResultParser;
    @Mocked
    SSLContext mockedSslContext;
    @Mocked
    RegistrationOperationStatusParser mockedRegistrationOperationStatusParser;
    @Mocked
    ResponseCallback mockedResponseCallback;
    @Mocked
    ResponseData mockedResponseData;
    @Mocked
    RequestData mockedRequestData;

    //SRS_RegisterTask_25_001: [ Constructor shall save provisioningDeviceClientConfig , securityProvider, provisioningDeviceClientContract and authorization.]
    @Test
    public void constructorSucceeds() throws ProvisioningDeviceClientException
    {
        //arrange

        //act
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig, mockedSecurityProvider,
                                                                mockedProvisioningDeviceClientContract, mockedAuthorization);
        //assert
        assertNotNull(Deencapsulation.getField(registerTask, "provisioningDeviceClientConfig"));
        assertNotNull(Deencapsulation.getField(registerTask, "securityProvider"));
        assertNotNull(Deencapsulation.getField(registerTask, "provisioningDeviceClientContract"));
        assertNotNull(Deencapsulation.getField(registerTask, "authorization"));
        assertNotNull(Deencapsulation.getField(registerTask, "responseCallback"));
    }

    //SRS_RegisterTask_25_002: [ Constructor throw ProvisioningDeviceClientException if provisioningDeviceClientConfig , securityProvider, authorization or provisioningDeviceClientContract is null.]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullConfig() throws ProvisioningDeviceClientException
    {
        //arrange
        //act
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, new Class[]{ProvisioningDeviceClientConfig.class,
                                                                        SecurityProvider.class, ProvisioningDeviceClientContract.class,
                                                                        Authorization.class},
                                                                null, mockedSecurityProvider,
                                                                mockedProvisioningDeviceClientContract, mockedAuthorization);
        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullSecurityProvider() throws ProvisioningDeviceClientException
    {
        //arrange
        //act
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, new Class[]{ProvisioningDeviceClientConfig.class,
                                                                        SecurityProvider.class, ProvisioningDeviceClientContract.class,
                                                                        Authorization.class},
                                                                mockedProvisioningDeviceClientConfig, null,
                                                                mockedProvisioningDeviceClientContract, mockedAuthorization);
        //assert
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullContract() throws ProvisioningDeviceClientException
    {
        //act
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, new Class[]{ProvisioningDeviceClientConfig.class,
                                                                        SecurityProvider.class, ProvisioningDeviceClientContract.class,
                                                                        Authorization.class},
                                                                mockedProvisioningDeviceClientConfig, mockedSecurityProvider,
                                                                null, mockedAuthorization);
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void constructorThrowsOnNullAuthorization() throws ProvisioningDeviceClientException
    {
        //act
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, new Class[]{ProvisioningDeviceClientConfig.class,
                                                                        SecurityProvider.class, ProvisioningDeviceClientContract.class,
                                                                        Authorization.class},
                                                                mockedProvisioningDeviceClientConfig, mockedSecurityProvider,
                                                                mockedProvisioningDeviceClientContract, null);
    }

    //SRS_RegisterTask_25_006: [ If the provided security client is for X509 then, this method shall trigger authenticateWithProvisioningService on the contract API and wait for response and return it. ]
    @Test
    public void authenticateWithX509Succeeds() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedDpsSecurityProviderX509, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedDpsSecurityProviderX509.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedDpsSecurityProviderX509.getSSLContext();
                result = mockedSslContext;
                Deencapsulation.invoke(mockedResponseData, "getResponseData");
                result = "NonNullValue".getBytes();
                Deencapsulation.invoke(mockedResponseData, "getContractState");
                result = DPS_REGISTRATION_RECEIVED;
            }
        };
        //act
        registerTask.call();

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedAuthorization, "setSslContext", mockedSslContext);
                times = 1;
                mockedProvisioningDeviceClientContract.authenticateWithProvisioningService((RequestData) any,
                                                                                           (ResponseCallback)any, any);
                times = 1;
            }
        };
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithX509ThrowsOnNonExistentType() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProvider, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        //act
        registerTask.call();
    }


    //SRS_RegisterTask_25_003: [ If the provided security client is for X509 then, this method shall throw ProvisioningDeviceClientException if registration id is null. ]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithX509ThrowsOnNullRegId() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedDpsSecurityProviderX509, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedDpsSecurityProviderX509.getRegistrationId();
                result = null;
            }
        };
        //act
        registerTask.call();

    }

    //SRS_RegisterTask_25_004: [ If the provided security client is for X509 then, this method shall save the SSL context to Authorization if it is not null and throw ProvisioningDeviceClientException otherwise. ]
    @Test (expected = ProvisioningDeviceSecurityException.class)
    public void authenticateWithX509ThrowsOnNullSSLContext() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedDpsSecurityProviderX509, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedDpsSecurityProviderX509.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedDpsSecurityProviderX509.getSSLContext();
                result = null;
            }
        };
        //act
        registerTask.call();
    }

    //SRS_RegisterTask_25_007: [ If the provided security client is for X509 then, this method shall throw ProvisioningDeviceClientException if null response is received. ]
    @Test (expected = ProvisioningDeviceTransportException.class)
    public void authenticateWithX509ThrowsOnAuthenticateWithDPSFail() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedDpsSecurityProviderX509, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedDpsSecurityProviderX509.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedDpsSecurityProviderX509.getSSLContext();
                result = mockedSslContext;
                mockedProvisioningDeviceClientContract.authenticateWithProvisioningService((RequestData) any,
                                                                                           (ResponseCallback)any, any);
                result = new ProvisioningDeviceTransportException("test transport exception");
            }
        };
        //act
        registerTask.call();
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithX509ThrowsIfNoResponseReceivedInMaxTime() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedDpsSecurityProviderX509, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedDpsSecurityProviderX509.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedDpsSecurityProviderX509.getSSLContext();
                result = mockedSslContext;
            }
        };
        //act
        registerTask.call();
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithX509ThrowsIfNullResponseReceivedInMaxTime() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedDpsSecurityProviderX509, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedDpsSecurityProviderX509.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedDpsSecurityProviderX509.getSSLContext();
                result = mockedSslContext;
                Deencapsulation.invoke(mockedResponseData, "getResponseData");
                result = null;
                Deencapsulation.invoke(mockedResponseData, "getContractState");
                result = DPS_REGISTRATION_UNKNOWN;
            }
        };
        //act
        registerTask.call();
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithX509ThrowsOnThreadInterruptedException() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedDpsSecurityProviderX509, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedDpsSecurityProviderX509.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedDpsSecurityProviderX509.getSSLContext();
                result = new InterruptedException();
            }
        };
        //act
        registerTask.call();
    }

    @Test
    public void authenticateWithSasTokenSucceeds() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedSecurityProviderTpm.getEndorsementKey();
                result = TEST_EK.getBytes();
                mockedSecurityProviderTpm.getStorageRootKey();
                result = TEST_SRK.getBytes();
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedSecurityProviderTpm.getSSLContext();
                result = mockedSslContext;
                Deencapsulation.invoke(mockedResponseData, "getResponseData");
                result = "NonNullValue".getBytes();
                Deencapsulation.invoke(mockedResponseData, "getContractState");
                result = DPS_REGISTRATION_RECEIVED;
                mockedTpmRegistrationResultParser.getAuthenticationKey();
                result = TEST_AUTH_KEY;
                mockedUrlPathBuilder.generateSasTokenUrl(TEST_REGISTRATION_ID);
                result = "testUrl";
                mockedSecurityProviderTpm.signWithIdentity((byte[])any);
                result = "testToken".getBytes();
            }
        };
        //act
        registerTask.call();

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedAuthorization, "setSslContext", mockedSslContext);
                times = 1;
                Deencapsulation.invoke(mockedAuthorization, "setSasToken", anyString);
                times = 1;
                mockedProvisioningDeviceClientContract.requestNonceForTPM((RequestData) any,
                        (ResponseCallback)any, any);
                times = 1;
                mockedSecurityProviderTpm.activateIdentityKey((byte[])any);
                times = 1;
                mockedProvisioningDeviceClientContract.authenticateWithProvisioningService((RequestData) any,
                                                                                           (ResponseCallback)any, any);
                times = 1;
            }
        };
    }

    //SRS_RegisterTask_25_008: [ If the provided security client is for Key then, this method shall throw ProvisioningDeviceClientException if registration id or endorsement key or storage root key are null. ]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithSasTokenNonceThrowsOnNullRegId() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = null;
            }
        };
        //act
        registerTask.call();
    }

    @Test (expected = ProvisioningDeviceSecurityException.class)
    public void authenticateWithSasTokenNonceThrowsOnNullEk() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedSecurityProviderTpm.getEndorsementKey();
                result = null;
            }
        };
        //act
        registerTask.call();
    }

    @Test (expected = ProvisioningDeviceSecurityException.class)
    public void authenticateWithSasTokenNonceThrowsOnNullSRK() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedSecurityProviderTpm.getEndorsementKey();
                result = TEST_EK.getBytes();
                mockedSecurityProviderTpm.getStorageRootKey();
                result = null;
            }
        };
        //act
        registerTask.call();
    }

    //SRS_RegisterTask_25_009: [ If the provided security client is for Key then, this method shall save the SSL context to Authorization if it is not null and throw ProvisioningDeviceClientException otherwise. ]
    @Test (expected = ProvisioningDeviceSecurityException.class)
    public void authenticateWithSasTokenNonceThrowsOnNullSSLContext() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedSecurityProviderTpm.getEndorsementKey();
                result = TEST_EK.getBytes();
                mockedSecurityProviderTpm.getStorageRootKey();
                result = TEST_SRK.getBytes();
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedSecurityProviderTpm.getSSLContext();
                result = null;
            }
        };
        //act
        registerTask.call();
    }

    //SRS_RegisterTask_25_011: [ If the provided security client is for Key then, this method shall trigger requestNonceForTPM on the contract API and wait for Authentication Key and decode it from Base64. Also this method shall pass the exception back to the user if it fails. ]
    @Test (expected = ProvisioningDeviceHubException.class)
    public void authenticateWithSasTokenNonceThrowsOnRequestNonceFail() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedSecurityProviderTpm.getEndorsementKey();
                result = TEST_EK.getBytes();
                mockedSecurityProviderTpm.getStorageRootKey();
                result = TEST_SRK.getBytes();
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedSecurityProviderTpm.getSSLContext();
                result = mockedSslContext;
                mockedProvisioningDeviceClientContract.requestNonceForTPM((RequestData) any,
                        (ResponseCallback)any, any);
                result = new ProvisioningDeviceHubException("test exception");
            }
        };
        //act
        registerTask.call();
    }

    //SRS_RegisterTask_25_012: [ If the provided security client is for Key then, this method shall throw ProvisioningDeviceClientException if null response is received. ]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithSasTokenNonceThrowsIfNoResponseReceivedInMaxTimeForNonce() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedSecurityProviderTpm.getEndorsementKey();
                result = TEST_EK.getBytes();
                mockedSecurityProviderTpm.getStorageRootKey();
                result = TEST_SRK.getBytes();
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedSecurityProviderTpm.getSSLContext();
                result = mockedSslContext;
            }
        };
        //act
        registerTask.call();
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithSasTokenNonceThrowsIfNullResponseReceivedInMaxTimeForNonce() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedSecurityProviderTpm.getEndorsementKey();
                result = TEST_EK.getBytes();
                mockedSecurityProviderTpm.getStorageRootKey();
                result = TEST_SRK.getBytes();
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedSecurityProviderTpm.getSSLContext();
                result = mockedSslContext;
                Deencapsulation.invoke(mockedResponseData, "getResponseData");
                result = null;
            }
        };
        //act
        registerTask.call();
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithSasTokenThrowsOnThreadInterruptedException() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedSecurityProviderTpm.getEndorsementKey();
                result = TEST_EK.getBytes();
                mockedSecurityProviderTpm.getStorageRootKey();
                result = TEST_SRK.getBytes();
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedSecurityProviderTpm.getSSLContext();
                result = new InterruptedException();
            }
        };
        //act
        registerTask.call();
    }

    //SRS_RegisterTask_25_013: [ If the provided security client is for Key then, this method shall throw ProvisioningDeviceClientException if Authentication Key received is null. ]
    @Test (expected = ProvisioningDeviceClientAuthenticationException.class)
    public void authenticateWithSasTokenThrowsOnNullAuthKey() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedSecurityProviderTpm.getEndorsementKey();
                result = TEST_EK.getBytes();
                mockedSecurityProviderTpm.getStorageRootKey();
                result = TEST_SRK.getBytes();
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedSecurityProviderTpm.getSSLContext();
                result = mockedSslContext;
                Deencapsulation.invoke(mockedResponseData, "getContractState");
                result = DPS_REGISTRATION_RECEIVED;
                Deencapsulation.invoke(mockedResponseData, "getResponseData");
                result = null;
            }
        };
        //act
        registerTask.call();
    }

    //SRS_RegisterTask_25_018: [ If the provided security client is for Key then, this method shall import the Base 64 encoded Authentication Key into the HSM using the security client and pass the exception to the user on failure. ]
    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithSasTokenThrowsImportKeyFailure() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedSecurityProviderTpm.getEndorsementKey();
                result = TEST_EK.getBytes();
                mockedSecurityProviderTpm.getStorageRootKey();
                result = TEST_SRK.getBytes();
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedSecurityProviderTpm.getSSLContext();
                result = mockedSslContext;
                Deencapsulation.invoke(mockedResponseData, "getResponseData");
                result = "NonNullValue".getBytes();
                Deencapsulation.invoke(mockedResponseData, "getContractState");
                result = DPS_REGISTRATION_RECEIVED;
                mockedTpmRegistrationResultParser.getAuthenticationKey();
                result = TEST_AUTH_KEY;
                mockedSecurityProviderTpm.activateIdentityKey((byte[])any);
                result = new ProvisioningDeviceClientException("test exception");
            }
        };
        //act
        registerTask.call();
    }

    /*SRS_RegisterTask_25_014: [ If the provided security client is for Key then, this method shall construct SasToken by doing the following

            1. Build a tokenScope of format <scopeid>/registrations/<registrationId>
            2. Sign the HSM with the string of format <tokenScope>/n<expiryTime> and receive a token
            3. Encode the token to Base64 format and UrlEncode it to generate the signature. ]*/
    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithSasTokenThrowsConstructTokenFailure() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedSecurityProviderTpm.getEndorsementKey();
                result = TEST_EK.getBytes();
                mockedSecurityProviderTpm.getStorageRootKey();
                result = TEST_SRK.getBytes();
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedSecurityProviderTpm.getSSLContext();
                result = mockedSslContext;
                Deencapsulation.invoke(mockedResponseData, "getResponseData");
                result = "NonNullValue".getBytes();
                Deencapsulation.invoke(mockedResponseData, "getContractState");
                result = DPS_REGISTRATION_RECEIVED;
                mockedTpmRegistrationResultParser.getAuthenticationKey();
                result = TEST_AUTH_KEY;
                mockedUrlPathBuilder.generateSasTokenUrl(TEST_REGISTRATION_ID);
                result = new MalformedURLException("test exception");
            }
        };
        //act
        registerTask.call();

    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithSasTokenThrowsUrlBuilderReturnsNull() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedSecurityProviderTpm.getEndorsementKey();
                result = TEST_EK.getBytes();
                mockedSecurityProviderTpm.getStorageRootKey();
                result = TEST_SRK.getBytes();
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedSecurityProviderTpm.getSSLContext();
                result = mockedSslContext;
                Deencapsulation.invoke(mockedResponseData, "getResponseData");
                result = "NonNullValue".getBytes();
                Deencapsulation.invoke(mockedResponseData, "getContractState");
                result = DPS_REGISTRATION_RECEIVED;
                mockedTpmRegistrationResultParser.getAuthenticationKey();
                result = TEST_AUTH_KEY;
                mockedUrlPathBuilder.generateSasTokenUrl(TEST_REGISTRATION_ID);
                result = null;
            }
        };
        //act
        registerTask.call();
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithSasTokenThrowsUrlBuilderReturnsEmpty() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedSecurityProviderTpm.getEndorsementKey();
                result = TEST_EK.getBytes();
                mockedSecurityProviderTpm.getStorageRootKey();
                result = TEST_SRK.getBytes();
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedSecurityProviderTpm.getSSLContext();
                result = mockedSslContext;
                Deencapsulation.invoke(mockedResponseData, "getResponseData");
                result = "NonNullValue".getBytes();
                Deencapsulation.invoke(mockedResponseData, "getContractState");
                result = DPS_REGISTRATION_RECEIVED;
                mockedTpmRegistrationResultParser.getAuthenticationKey();
                result = TEST_AUTH_KEY;
                mockedUrlPathBuilder.generateSasTokenUrl(TEST_REGISTRATION_ID);
                result = "";
            }
        };
        //act
        registerTask.call();
    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithSasTokenThrowsConstructTokenSignDataFailure() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedSecurityProviderTpm.getEndorsementKey();
                result = TEST_EK.getBytes();
                mockedSecurityProviderTpm.getStorageRootKey();
                result = TEST_SRK.getBytes();
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedSecurityProviderTpm.getSSLContext();
                result = mockedSslContext;
                Deencapsulation.invoke(mockedResponseData, "getResponseData");
                result = "NonNullValue".getBytes();
                Deencapsulation.invoke(mockedResponseData, "getContractState");
                result = DPS_REGISTRATION_RECEIVED;
                mockedTpmRegistrationResultParser.getAuthenticationKey();
                result = TEST_AUTH_KEY;
                mockedUrlPathBuilder.generateSasTokenUrl(TEST_REGISTRATION_ID);
                result = "testUrl";
                mockedSecurityProviderTpm.signWithIdentity((byte[])any);
                result = new ProvisioningDeviceClientException("test Exception");
            }
        };
        //act
        registerTask.call();

    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithSasTokenThrowsConstructTokenSignDataReturnsNull() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedSecurityProviderTpm.getEndorsementKey();
                result = TEST_EK.getBytes();
                mockedSecurityProviderTpm.getStorageRootKey();
                result = TEST_SRK.getBytes();
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedSecurityProviderTpm.getSSLContext();
                result = mockedSslContext;
                Deencapsulation.invoke(mockedResponseData, "getResponseData");
                result = "NonNullValue".getBytes();
                Deencapsulation.invoke(mockedResponseData, "getContractState");
                result = DPS_REGISTRATION_RECEIVED;
                mockedTpmRegistrationResultParser.getAuthenticationKey();
                result = TEST_AUTH_KEY;
                mockedUrlPathBuilder.generateSasTokenUrl(TEST_REGISTRATION_ID);
                result = "testUrl";
                mockedSecurityProviderTpm.signWithIdentity((byte[])any);
                result = null;
            }
        };
        //act
        registerTask.call();

    }

    @Test (expected = ProvisioningDeviceClientException.class)
    public void authenticateWithSasTokenThrowsConstructTokenSignDataReturnsEmpty() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedSecurityProviderTpm.getEndorsementKey();
                result = TEST_EK.getBytes();
                mockedSecurityProviderTpm.getStorageRootKey();
                result = TEST_SRK.getBytes();
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedSecurityProviderTpm.getSSLContext();
                result = mockedSslContext;
                Deencapsulation.invoke(mockedResponseData, "getResponseData");
                result = "NonNullValue".getBytes();
                Deencapsulation.invoke(mockedResponseData, "getContractState");
                result = DPS_REGISTRATION_RECEIVED;
                mockedTpmRegistrationResultParser.getAuthenticationKey();
                result = TEST_AUTH_KEY;
                mockedUrlPathBuilder.generateSasTokenUrl(TEST_REGISTRATION_ID);
                result = "testUrl";
                mockedSecurityProviderTpm.signWithIdentity((byte[])any);
                result = "".getBytes();
            }
        };
        //act
        registerTask.call();

    }

    //SRS_RegisterTask_25_016: [ If the provided security client is for Key then, this method shall trigger authenticateWithProvisioningService on the contract API using the sasToken generated and wait for response and return it. ]
    @Test (expected = ProvisioningDeviceTransportException.class)
    public void authenticateWithSasTokenThrowsOnAuthenticateWithDPSFail() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedSecurityProviderTpm.getEndorsementKey();
                result = TEST_EK.getBytes();
                mockedSecurityProviderTpm.getStorageRootKey();
                result = TEST_SRK.getBytes();
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedSecurityProviderTpm.getSSLContext();
                result = mockedSslContext;
                Deencapsulation.invoke(mockedResponseData, "getResponseData");
                result = "NonNullValue".getBytes();
                Deencapsulation.invoke(mockedResponseData, "getContractState");
                result = DPS_REGISTRATION_RECEIVED;
                mockedTpmRegistrationResultParser.getAuthenticationKey();
                result = TEST_AUTH_KEY;
                mockedUrlPathBuilder.generateSasTokenUrl(TEST_REGISTRATION_ID);
                result = "testUrl";
                mockedSecurityProviderTpm.signWithIdentity((byte[])any);
                result = "testToken".getBytes();
                mockedProvisioningDeviceClientContract.authenticateWithProvisioningService((RequestData) any,
                                                                                           (ResponseCallback)any, any);
                result = new ProvisioningDeviceTransportException("test transport exception");
            }
        };
        //act
        registerTask.call();
    }

    //SRS_RegisterTask_25_017: [ If the provided security client is for Key then, this method shall throw ProvisioningDeviceClientException if null response to authenticateWithProvisioningService is received. ]
    @Test (expected = ProvisioningDeviceClientAuthenticationException.class)
    public void authenticateWithSasTokenThrowsIfNoResponseReceivedInMaxTime() throws Exception
    {
        //arrange
        RegisterTask registerTask = Deencapsulation.newInstance(RegisterTask.class, mockedProvisioningDeviceClientConfig,
                                                                mockedSecurityProviderTpm, mockedProvisioningDeviceClientContract,
                                                                mockedAuthorization);

        new NonStrictExpectations()
        {
            {
                mockedSecurityProviderTpm.getRegistrationId();
                result = TEST_REGISTRATION_ID;
                mockedSecurityProviderTpm.getEndorsementKey();
                result = TEST_EK.getBytes();
                mockedSecurityProviderTpm.getStorageRootKey();
                result = TEST_SRK.getBytes();
                mockedDeviceRegistrationParser.toJson();
                result = "testJson";
                mockedSecurityProviderTpm.getSSLContext();
                result = mockedSslContext;
                mockedProvisioningDeviceClientContract.requestNonceForTPM((RequestData) any, (ResponseCallback)any, any);
            }
        };

        new StrictExpectations()
        {
            {
                Deencapsulation.newInstance(ResponseData.class);
                result = mockedResponseData;
                Deencapsulation.invoke(mockedResponseData, "getContractState");
                result = DPS_REGISTRATION_RECEIVED;
                Deencapsulation.invoke(mockedResponseData, "getContractState");
                result = DPS_REGISTRATION_RECEIVED;
            }
        };

        new NonStrictExpectations()
        {
            {
                mockedTpmRegistrationResultParser.getAuthenticationKey();
                result = TEST_AUTH_KEY;
                mockedUrlPathBuilder.generateSasTokenUrl(TEST_REGISTRATION_ID);
                result = "testUrl";
                mockedSecurityProviderTpm.signWithIdentity((byte[])any);
                result = "testToken".getBytes();
                mockedProvisioningDeviceClientContract.authenticateWithProvisioningService((RequestData) any,
                                                                                           (ResponseCallback)any, any);
            }
        };

        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedResponseData, "getResponseData");
                result = null;
            }
        };
        //act
        registerTask.call();
    }
}
