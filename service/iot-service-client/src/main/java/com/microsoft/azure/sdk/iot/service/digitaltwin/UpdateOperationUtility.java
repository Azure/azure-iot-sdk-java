// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.
package com.microsoft.azure.sdk.iot.service.digitaltwin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A utility to create the JSON patch payload required for update operations such as update digital twin
 */
public final class UpdateOperationUtility {
    private static final String ADD = "add";
    private static final String REPLACE = "replace";
    private static final String REMOVE = "remove";
    private static final ObjectMapper mapper = new ObjectMapper();

    private final List<UpdateOperation> operations = new ArrayList<>();

    /**
     * Gets the JSON patch payload required for update operations.
     * @return The JSON patch payload required for update operations.
     */
    public List<Object> getUpdateOperations() {
        return operations.stream().map(op -> mapper.convertValue(op, Object.class)).collect(Collectors.toList());
    }

    /**
     * Include an add operation for a property.
     * @param path The path to the property to be added.
     * @param value The value to update to.
     * @return The UpdateOperationUtility object itself.
     */
    public UpdateOperationUtility appendAddPropertyOperation(String path, Object value) {

        operations.add(
                new UpdateOperation()
                        .setOperation(ADD)
                        .setPath(path)
                        .setValue(value));

        return this;
    }

    /**
     * Include an add operation for a component.
     * @param path The path to the component to be added.
     * @param properties A collection of properties and their values in the component.
     * @return The UpdateOperationUtility object itself.
     */
    public UpdateOperationUtility appendAddComponentOperation(String path, Map<String, Object> properties) {
        properties.put("$metadata", Collections.emptyMap());
        operations.add(
                new UpdateOperation()
                        .setOperation(ADD)
                        .setPath(path)
                        .setValue(properties));
        return this;
    }

    /**
     * Include a replace operation for a property.
     * @param path The path to the property to be updated.
     * @param value The value to update to.
     * @return The UpdateOperationUtility object itself.
     */
    public UpdateOperationUtility appendReplacePropertyOperation(String path, Object value) {
        operations.add(
                new UpdateOperation()
                        .setOperation(REPLACE)
                        .setPath(path)
                        .setValue(value));

        return this;
    }

    /**
     * Include a replace operation for a component.
     * @param path The path to the component to be updated.
     * @param properties A collection of properties and their values in the component.
     * @return The UpdateOperationUtility object itself.
     */
    public UpdateOperationUtility appendReplaceComponentOperation(String path, Map<String, Object> properties) {
        properties.put("$metadata", Collections.emptyMap());
        operations.add(
                new UpdateOperation()
                        .setOperation(REPLACE)
                        .setPath(path)
                        .setValue(properties));

        return this;
    }

    /**
     * Include a remove operation for a property.
     * @param path The path to the property to be added.
     * @return The UpdateOperationUtility object itself.
     */
    public UpdateOperationUtility appendRemovePropertyOperation(String path) {
        operations.add(
                new UpdateOperation()
                        .setOperation(REMOVE)
                        .setPath(path));

        return this;
    }

    /**
     * Include a remove operation for a component.
     * @param path The path to the component to be removed.
     * @return The UpdateOperationUtility object itself.
     */
    public UpdateOperationUtility appendRemoveComponentOperation(String path) {
        operations.add(
                new UpdateOperation()
                        .setOperation(REMOVE)
                        .setPath(path));

        return this;
    }

    // A static inner class is declared as a static member of another class and it is not tied to any instance of the containing class.
    // An inner class is declared as a non-static member of another class and it is tied to a particular instance of its containing class.
    // An inner class may be static if it doesn't reference its enclosing instance.
    // A static inner class does not keep an implicit reference to its enclosing instance. This prevents a common cause of memory leaks and uses less memory per instance of the class.
    static class UpdateOperation {
        @JsonProperty(value = "op")
        private String operation;

        @JsonProperty(value = "path")
        private String path;

        @JsonProperty(value = "value")
        private Object value;

        String getOperation() {
            return operation;
        }

        UpdateOperation setOperation(String operation) {
            this.operation = operation;
            return this;
        }

        String getPath() {
            return path;
        }

        UpdateOperation setPath(String path) {
            this.path = path;
            return this;
        }

        Object getValue() {
            return value;
        }

        UpdateOperation setValue(Object value) {
            this.value = value;
            return this;
        }
    }

}