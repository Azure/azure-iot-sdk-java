// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.service.serializer.ExportImportDeviceParser;

import java.util.ArrayList;

public class ExportImportDevicesParser {
    private static final String DEVICES = "devices";
    @SerializedName(DEVICES)
    Iterable<ExportImportDeviceParser> exportImportDevices;
    private final transient static Gson gson = new Gson();

    /**
     * Converts this into json and returns it
     * @return the json representation of this
     */
    public String toJson()
    {
        return gson.toJson(exportImportDevices);
    }

    /**
     * Empty constructor: Used only to keep GSON happy.
     */
    public ExportImportDevicesParser()
    {
        exportImportDevices = new ArrayList<>();
    }

    /**
     * Constructor for an ExportImportDevicesParser that is built using the provided json
     * @param json the json string used to build the ExportImportDevicesParser
     */
    public ExportImportDevicesParser(String json)
    {
        if (json == null || json.isEmpty())
        {
            throw new IllegalArgumentException("The provided json cannot be null or empty");
        }

        try
        {
            this.exportImportDevices = gson.fromJson(json, ExportImportDevicesParser.class).getExportImportDevices();
        }
        catch (JsonSyntaxException e)
        {
            throw new IllegalArgumentException("The provided json could not be parsed");
        }
    }

    public final Iterable<ExportImportDeviceParser> getExportImportDevices() {
        return exportImportDevices;
    }

    public final void setExportImportDevices(Iterable<ExportImportDeviceParser> exportImportDevices) {
        this.exportImportDevices = exportImportDevices;
    }
}
