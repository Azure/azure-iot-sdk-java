/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.device.InternalClient;
import org.junit.ComparisonFailure;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class CorrelationDetailsLoggingAssert
{
    String hostname;
    Collection<String> deviceIds;
    String protocol;
    Collection<String> moduleIds;

    public CorrelationDetailsLoggingAssert(String hostname, String deviceId, String protocol, String moduleId)
    {
        this.hostname = hostname;
        this.deviceIds = new ArrayList<>();
        this.deviceIds.add(deviceId);
        this.protocol = protocol;

        if (moduleId != null && !moduleId.isEmpty())
        {
            this.moduleIds = new ArrayList<>();
            this.moduleIds.add(moduleId);
        }
    }

    public CorrelationDetailsLoggingAssert(String hostname, Collection<String> deviceIds, String protocol, Collection<String> moduleIds)
    {
        this.hostname = hostname;
        this.deviceIds = deviceIds;
        this.protocol = protocol;
        this.moduleIds = moduleIds;
    }

    public CorrelationDetailsLoggingAssert(InternalClient internalClient)
    {
        this(internalClient.getConfig().getIotHubHostname(),
                internalClient.getConfig().getDeviceId(),
                internalClient.getConfig().getProtocol().toString(),
                internalClient.getConfig().getModuleId());
    }

    public static String buildExceptionMessage(String baseMessage, Collection<String> deviceIds, String protocol, String hostname, Collection<String> moduleIds)
    {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String correlationString = "Hostname: " + hostname;
        if (deviceIds != null && deviceIds.size() > 0)
        {
            correlationString += " Device id: ";
            boolean isFirstDevice = true;
            for (String deviceId : deviceIds)
            {
                if (isFirstDevice)
                {
                    correlationString += deviceId;
                }
                else
                {
                    correlationString += ", " + deviceId;
                }

                isFirstDevice = false;
            }
        }

        if (moduleIds != null && moduleIds.size() > 0)
        {
            correlationString += " Module id: ";
            boolean isFirstModule = true;
            for (String moduleId : moduleIds)
            {
                if (isFirstModule)
                {
                    correlationString += moduleId;
                }
                else
                {
                    correlationString += ", " + moduleId;
                }

                isFirstModule = false;
            }
        }

        if (protocol != null && !protocol.isEmpty())
        {
            correlationString += " Protocol: " + protocol;
        }
        
        correlationString += " Timestamp: " + timeStamp;

        return baseMessage + "\n(Correlation details: <" + correlationString + ">)";
    }

    public static String buildExceptionMessage(String baseMessage, String deviceId, String protocol, String hostname, String moduleId)
    {
        Collection<String> deviceIds = new ArrayList<>();
        deviceIds.add(deviceId);

        Collection<String> moduleIds = new ArrayList<>();
        if (moduleId != null && !moduleId.isEmpty())
        {
            moduleIds.add(moduleId);
        }

        return buildExceptionMessage(baseMessage, deviceIds, protocol, hostname, moduleIds);
    }

    public static String buildExceptionMessage(String baseMessage, InternalClient client)
    {
        if (client == null || client.getConfig() == null)
        {
            throw new IllegalArgumentException("client and config must not be null");
        }

        return buildExceptionMessage(
                baseMessage,
                client.getConfig().getDeviceId(),
                client.getConfig().getProtocol().toString(),
                client.getConfig().getIotHubHostname(),
                client.getConfig().getModuleId());
    }

    public static String buildExceptionMessage(String baseMessage, Collection<InternalClient> clients)
    {
        String hostname = "";
        String protocol = "";
        Collection<String> deviceIds = new ArrayList<>();
        Collection<String> moduleIds = new ArrayList<>();

        for (InternalClient client : clients)
        {
            hostname = client.getConfig().getIotHubHostname();
            protocol = client.getConfig().getProtocol().toString();
            deviceIds.add(client.getConfig().getDeviceId());

            if (client.getConfig().getModuleId() != null && !client.getConfig().getModuleId().isEmpty())
            {
                moduleIds.add(client.getConfig().getModuleId());
            }
        }

        return buildExceptionMessage(
                baseMessage,
                deviceIds,
                protocol,
                hostname,
                moduleIds);
    }

    public static String buildExceptionMessage(String baseMessage, String hostname)
    {
        return buildExceptionMessage(
                baseMessage,
                new ArrayList<>(),
                null,
                hostname,
                new ArrayList<>());
    }

    public static String buildExceptionMessageIndividualEnrollment(String baseMessage, String registrationId, String dpsHostname)
    {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String correlationString = "DPS Hostname: " + dpsHostname;
        correlationString += " registrationId: " + registrationId;

        correlationString += " Timestamp: " + timeStamp;

        return baseMessage + "\n(Correlation details: <" + correlationString + ">)";
    }

    public static String buildExceptionMessageEnrollmentGroup(String baseMessage, String groupId, String dpsHostname)
    {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String correlationString = "DPS Hostname: " + dpsHostname;
        correlationString += " groupId: " + groupId;

        correlationString += " Timestamp: " + timeStamp;

        return baseMessage + "\n(Correlation details: <" + correlationString + ">)";
    }

    public static String buildExceptionMessageDpsHostnameOnly(String baseMessage, String dpsHostname)
    {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String correlationString = "DPS Hostname: " + dpsHostname;

        correlationString += " Timestamp: " + timeStamp;

        return baseMessage + "\n(Correlation details: <" + correlationString + ">)";
    }

    public static String buildExceptionMessageDpsIndividualOrGroup(String baseMessage, String dpsHostname, String groupId, String registrationId)
    {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String correlationString = "DPS Hostname: " + dpsHostname;

        if (groupId != null && !groupId.isEmpty())
        {
            correlationString += " groupId: " + groupId;
        }

        if (registrationId != null && !registrationId.isEmpty())
        {
            correlationString += " registrationId: " + registrationId;
        }

        correlationString += " Timestamp: " + timeStamp;

        return baseMessage + "\n(Correlation details: <" + correlationString + ">)";
    }


    public void assertTrue(String message, boolean condition)
    {
        if (!condition)
        {
            fail(message);
        }
    }

    public void assertTrue(boolean condition)
    {
        assertTrue(null, condition);
    }

    public void assertFalse(String message, boolean condition)
    {
        assertTrue(message, !condition);
    }

    public void assertFalse(boolean condition)
    {
        assertFalse(null, condition);
    }

    public void fail(String message)
    {
        if (message == null)
        {
            throw new AssertionError(buildExceptionMessage("", this.deviceIds, this.protocol, this.hostname, this.moduleIds));
        }

        throw new AssertionError(buildExceptionMessage(message, this.deviceIds, this.protocol, this.hostname, this.moduleIds));
    }

    public void fail()
    {
        fail(null);
    }

    public void assertEquals(String message, Object expected, Object actual)
    {
        if (equalsRegardingNull(expected, actual))
        {
            return;
        }
        else if (expected instanceof String && actual instanceof String)
        {
            String cleanMessage = message == null ? "" : message;
            throw new ComparisonFailure(cleanMessage, (String) expected, (String) actual);
        }
        else
        {
            failNotEquals(message, expected, actual);
        }
    }

    public void assertEquals(Object expected, Object actual)
    {
        assertEquals(null, expected, actual);
    }

    private boolean equalsRegardingNull(Object expected, Object actual)
    {
        if (expected == null)
        {
            return actual == null;
        }

        return isEquals(expected, actual);
    }

    private void failNotEquals(String message, Object expected, Object actual)
    {
        fail(format(message, expected, actual));
    }

    String format(String message, Object expected, Object actual)
    {
        String formatted = "";
        if (message != null && !message.equals(""))
        {
            formatted = message + " ";
        }
        String expectedString = String.valueOf(expected);
        String actualString = String.valueOf(actual);
        if (expectedString.equals(actualString))
        {
            return formatted + "expected: " + formatClassAndValue(expected, expectedString) + " but was: " + formatClassAndValue(actual, actualString);
        }
        else
        {
            return formatted + "expected:<" + expectedString + "> but was:<" + actualString + ">";
        }
    }

    private String formatClassAndValue(Object value, String valueString)
    {
        String className = value == null ? "null" : value.getClass().getName();
        return className + "<" + valueString + ">";
    }

    private boolean isEquals(Object expected, Object actual)
    {
        return expected.equals(actual);
    }
}
