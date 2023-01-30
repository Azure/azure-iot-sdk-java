/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.device.InternalClient;

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
        StringBuilder correlationString = new StringBuilder("Hostname: " + hostname);
        if (deviceIds != null && deviceIds.size() > 0)
        {
            correlationString.append(" Device id: ");
            boolean isFirstDevice = true;
            for (String deviceId : deviceIds)
            {
                if (isFirstDevice)
                {
                    correlationString.append(deviceId);
                }
                else
                {
                    correlationString.append(", ").append(deviceId);
                }

                isFirstDevice = false;
            }
        }

        if (moduleIds != null && moduleIds.size() > 0)
        {
            correlationString.append(" Module id: ");
            boolean isFirstModule = true;
            for (String moduleId : moduleIds)
            {
                if (isFirstModule)
                {
                    correlationString.append(moduleId);
                }
                else
                {
                    correlationString.append(", ").append(moduleId);
                }

                isFirstModule = false;
            }
        }

        if (protocol != null && !protocol.isEmpty())
        {
            correlationString.append(" Protocol: ").append(protocol);
        }
        
        correlationString.append(" Timestamp: ").append(timeStamp);

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
}
