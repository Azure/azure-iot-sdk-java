/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.Module;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/** Manages module on IotHub - CRUD operations */
public class ModuleManagerSample
{
    /**
     * A simple sample for doing CRUD operations
     * @param args unused
     * @throws Exception if any exception is thrown
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting sample...");

        System.out.println("Add Module started");
        AddModule(0);
        AddModule(1);
        System.out.println("Add Module finished");

        System.out.println("Get Device started");
        GetModule();
        System.out.println("Get Device finished");

        System.out.println("Update Device started");
        UpdateModule();
        System.out.println("Update Device finished");

        System.out.println("Remove Device started");
        RemoveModule();
        System.out.println("Remove Device finished");

        System.out.println("Shutting down sample...");
    }

    private static void AddModule(int n) throws Exception
    {
        RegistryManager registryManager = RegistryManager.createFromConnectionString(SampleUtils.iotHubConnectionString);

        String moduleId;
        if (n == 0)
        {
            moduleId = SampleUtils.moduleId0;
        }
        else
        {
            moduleId = SampleUtils.moduleId1;
        }
        Module module = Module.createFromId(SampleUtils.deviceId, moduleId, null);

        try
        {
            module = registryManager.addModule(module);

            System.out.println("Module created: " + module.getId());
        }
        catch (IotHubException iote)
        {
            iote.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        registryManager.close();
    }

    private static void GetModule() throws Exception
    {
        RegistryManager registryManager = RegistryManager.createFromConnectionString(SampleUtils.iotHubConnectionString);

        Module returnModule = null;
        try
        {
            returnModule = registryManager.getModule(SampleUtils.deviceId, SampleUtils.moduleId0);

            System.out.println("Module: " + returnModule.getId());
            System.out.println("Module primary key: " + returnModule.getPrimaryKey());
            System.out.println("Module secondary key: " + returnModule.getSecondaryKey());
            System.out.println("Module eTag: " + returnModule.geteTag());
        }
        catch (IotHubException iote)
        {
            iote.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        List<Module> list = registryManager.getModulesOnDevice(SampleUtils.deviceId);
        for (int i = 0; i < list.size(); i++)
        {
            Module module = list.get(i);
            System.out.println("Module: " + module.getId());
            System.out.println("Module primary key: " + module.getPrimaryKey());
            System.out.println("Module secondary key: " + module.getSecondaryKey());
            System.out.println("Module eTag: " + module.geteTag());
        }

        registryManager.close();
    }

    private static void UpdateModule() throws Exception
    {
        String primaryKey = "[New primary key goes here]";
        String secondaryKey = "[New secondary key goes here]";

        RegistryManager registryManager = RegistryManager.createFromConnectionString(SampleUtils.iotHubConnectionString);

        Module module = Module.createFromId(SampleUtils.deviceId, SampleUtils.moduleId0, null);
        module.getSymmetricKey().setPrimaryKeyFinal(primaryKey);
        module.getSymmetricKey().setSecondaryKeyFinal(secondaryKey);
        try
        {
            module = registryManager.updateModule(module);

            System.out.println("Device updated: " + module.getId());
            System.out.println("Device primary key: " + module.getPrimaryKey());
            System.out.println("Device secondary key: " + module.getSecondaryKey());
        }
        catch (IotHubException iote)
        {
            iote.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        registryManager.close();
    }

    private static void RemoveModule() throws Exception
    {
        RegistryManager registryManager = RegistryManager.createFromConnectionString(SampleUtils.iotHubConnectionString);

        try
        {
            registryManager.removeModule(SampleUtils.deviceId, SampleUtils.moduleId0);
            System.out.println("Module removed: " + SampleUtils.moduleId0);
            registryManager.removeModule(SampleUtils.deviceId, SampleUtils.moduleId1);
            System.out.println("Module removed: " + SampleUtils.moduleId1);
        }
        catch (IotHubException iote)
        {
            iote.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        registryManager.close();
    }
}
