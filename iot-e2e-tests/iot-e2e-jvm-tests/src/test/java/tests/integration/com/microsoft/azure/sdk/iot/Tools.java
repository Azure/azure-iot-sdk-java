// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import com.microsoft.azure.sdk.iot.service.devicetwin.DirectMethodsClient;
import com.microsoft.azure.sdk.iot.service.devicetwin.DirectMethodsClientOptions;
import com.microsoft.azure.sdk.iot.service.devicetwin.TwinClient;
import com.microsoft.azure.sdk.iot.service.devicetwin.TwinClientOptions;
import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinClient;
import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinClientOptions;
import com.microsoft.azure.sdk.iot.service.query.QueryClient;
import com.microsoft.azure.sdk.iot.service.query.QueryClientOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class Tools
{
    private static final Map<String, String> ANDROID_ENV_VAR = retrieveAndroidEnvVariables();

    private static String IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME = "IOTHUB_CONNECTION_STRING";
    private static final String IS_BASIC_TIER_HUB_ENV_VAR_NAME = "IS_BASIC_TIER_HUB";

    public static final String iotHubConnectionString = retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
    public static final boolean isBasicTierHub = Boolean.parseBoolean(retrieveEnvironmentVariableValue(IS_BASIC_TIER_HUB_ENV_VAR_NAME));

    private static String MSFT_TENANT_ID_ENV_VAR_NAME = "MSFT_TENANT_ID";
    private static String IOTHUB_CLIENT_ID_ENV_VAR_NAME = "IOTHUB_CLIENT_ID";
    private static String IOTHUB_CLIENT_SECRET_ENV_VAR_NAME = "IOTHUB_CLIENT_SECRET";

    private static final String ANDROID_BUILD_CONFIG_CLASS = "com.iothub.azure.microsoft.com.androide2e.test.BuildConfig";

    public static String retrieveEnvironmentVariableValue(String environmentVariableName)
    {
        String environmentVariableValue;

        if (ANDROID_ENV_VAR.containsKey(environmentVariableName))
        {
            environmentVariableValue = ANDROID_ENV_VAR.get(environmentVariableName);
        }
        else
        {
            environmentVariableValue = System.getenv().get(environmentVariableName);
            if ((environmentVariableValue == null) || environmentVariableValue.isEmpty())
            {
                environmentVariableValue = System.getProperty(environmentVariableName);
            }
        }

        return environmentVariableValue;
    }

    public static Map<String, String> retrieveAndroidEnvVariables()
    {
        Map<String, String> envVariables = new HashMap<>();
        try
        {
            Class buildConfig = Class.forName(ANDROID_BUILD_CONFIG_CLASS);
            Arrays.stream(buildConfig.getFields()).forEach(field -> {
                try
                {
                    envVariables.put(field.getName(), field.get(null).toString());
                }
                catch (IllegalAccessException e)
                {
                    log.error("Cannot access the following field: {}", field.getName(), e);
                }
            });
        }
        catch (ClassNotFoundException e)
        {
            log.debug("Likely running the JVM tests, ignoring ClassNotFoundException\n");
        }

        return envVariables;
    }

    public static ServiceClient buildServiceClientWithTokenCredential(IotHubServiceClientProtocol protocol)
    {
        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);
        TokenCredential tokenCredential = buildTokenCredentialFromEnvironment();
        return new ServiceClient(iotHubConnectionStringObj.getHostName(), tokenCredential, protocol);
    }

    public static DigitalTwinClient buildDigitalTwinClientWithTokenCredential()
    {
        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);
        TokenCredential tokenCredential = buildTokenCredentialFromEnvironment();
        DigitalTwinClientOptions options = DigitalTwinClientOptions.builder().build();
        return new DigitalTwinClient(iotHubConnectionStringObj.getHostName(), tokenCredential, options);
    }

    public static TwinClient buildTwinClientWithTokenCredential()
    {
        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);
        TokenCredential tokenCredential = buildTokenCredentialFromEnvironment();
        TwinClientOptions options = TwinClientOptions.builder().build();
        return new TwinClient(iotHubConnectionStringObj.getHostName(), tokenCredential, options);
    }

    public static QueryClient buildQueryClientWithTokenCredential()
    {
        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);
        TokenCredential tokenCredential = buildTokenCredentialFromEnvironment();
        QueryClientOptions options = QueryClientOptions.builder().build();
        return new QueryClient(iotHubConnectionStringObj.getHostName(), tokenCredential, options);
    }

    public static TokenCredential buildTokenCredentialFromEnvironment()
    {
        String tenantId = retrieveEnvironmentVariableValue(MSFT_TENANT_ID_ENV_VAR_NAME);
        String clientId = retrieveEnvironmentVariableValue(IOTHUB_CLIENT_ID_ENV_VAR_NAME);
        String clientSecret = retrieveEnvironmentVariableValue(IOTHUB_CLIENT_SECRET_ENV_VAR_NAME);

        Objects.requireNonNull(tenantId, MSFT_TENANT_ID_ENV_VAR_NAME + " not found in environment variables");
        Objects.requireNonNull(clientId, IOTHUB_CLIENT_ID_ENV_VAR_NAME + " not found in environment variables");
        Objects.requireNonNull(clientSecret, IOTHUB_CLIENT_SECRET_ENV_VAR_NAME + " not found in environment variables");

        return new ClientSecretCredentialBuilder()
            .clientSecret(clientSecret)
            .clientId(clientId)
            .tenantId(tenantId)
            .build();
    }

    public static DirectMethodsClient buildDeviceMethodClientWithTokenCredential()
    {
        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);
        TokenCredential tokenCredential = buildTokenCredentialFromEnvironment();
        DirectMethodsClientOptions options = DirectMethodsClientOptions.builder().build();
        return new DirectMethodsClient(iotHubConnectionStringObj.getHostName(), tokenCredential, options);
    }
}
