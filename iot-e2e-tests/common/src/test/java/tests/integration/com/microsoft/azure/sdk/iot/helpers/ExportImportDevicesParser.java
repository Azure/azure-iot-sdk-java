// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.deps.serializer.ExportImportDeviceParser;

import java.util.ArrayList;

public class ExportImportDevicesParser {
    private static final String DEVICES = "devices";
    @SerializedName(DEVICES)
    Iterable<ExportImportDeviceParser> exportImportDevices;
    private transient static Gson gson = new Gson();

    /**
     * Converts this into json and returns it
     * @return the json representation of this
     */
    public String toJson()
    {
        String json = gson.toJson(exportImportDevices);
        return json;
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
     * @param json the json string to build the ExportImportDevicesParser out of
     */
    public ExportImportDevicesParser(String json)
    {
        if (json == null || json.isEmpty())
        {
            throw new IllegalArgumentException("The provided json cannot be null or empty");
        }

        ExportImportDevicesParser exportImportDevicesParser = null;
        try
        {
            exportImportDevicesParser = gson.fromJson(json, ExportImportDevicesParser.class);
        }
        catch (JsonSyntaxException e)
        {
            throw new IllegalArgumentException("The provided json could not be parsed");
        }

        this.exportImportDevices = exportImportDevicesParser.getExportImportDevices();
    }

    public final Iterable<ExportImportDeviceParser> getExportImportDevices() {
        return exportImportDevices;
    }

    public final void setExportImportDevices(Iterable<ExportImportDeviceParser> exportImportDevices) {
        this.exportImportDevices = exportImportDevices;
    }
}
