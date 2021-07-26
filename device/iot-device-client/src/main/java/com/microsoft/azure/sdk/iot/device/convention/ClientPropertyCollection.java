package com.microsoft.azure.sdk.iot.device.convention;

import com.microsoft.azure.sdk.iot.deps.convention.ConventionConstants;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.twin.TwinMetadata;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.PayloadCollection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class ClientPropertyCollection extends PayloadCollection
{
    // the Twin collection version
    private static final String VERSION_TAG = "$version";

    @Getter
    private Integer version;

    // the Twin collection metadata
    private static final String METADATA_TAG = "$metadata";

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private TwinMetadata metadata;

    private final Map<String, TwinMetadata> metadataMap = new HashMap<>();

    /**
     * Default constructor for the class.
     */
    public ClientPropertyCollection()
    {
        super();
    }

    /**
     * Constructor that is used to convert a {@link Map<String, Object>} to a ClientPropertyCollection
     * @param map The map you wish to convert.
     */
    public ClientPropertyCollection(Map<? extends String, Object> map)
    {
        this.putAll(map);
    }

    /**
     * Converts an existing {@link Map<String, Object>} to a ClientPropertyCollection
     * @param mapToConvert The map you wish to convert.
     * @return A ClientPropertyCollection initialized with values from the existing Map.
     */
    public static ClientPropertyCollection fromMap(Map<String,Object> mapToConvert)
    {
        return new ClientPropertyCollection(mapToConvert);
    }

    /**
     * Add all information in the provided Map to the ClientPropertyCollection.
     *
     * <p> This function will add all entries in the Map to the ClientPropertyCollection. If the provided
     * key already exists, it will replace the value by the new one. This function will not
     * delete or change the content of the other keys in the Map.
     *
     * <p> As defined by the Twin, the value of a entry can be an inner Map. TwinCollection will
     * accept up to 5 levels of inner Maps.
     *
     * @param map A {@code Map} of entries to add to the TwinCollection.
     */
    @Override
    public final void putAll(Map<? extends String, ?> map) {
        if ((map == null) || map.isEmpty()) {
            throw new IllegalArgumentException("map to add cannot be null or empty.");
        }

        for (Map.Entry<? extends String, ?> entry : map.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Add a single new entry in the ClientPropertyCollection.
     *
     * <p> Override {@code HashMap.put(String, Object)}.
     *
     * <p> This function will add a single pair key value to the ClientPropertyCollection.
     *
     * @param propertyName   the {@code String} that represents the name of the new entry. It cannot be {@code null} or empty.
     * @param propertyValue the {@code Object} that represents the value of the new entry. It cannot be user defined type or array.
     * @return The {@code Object} that corresponds to the last value of this key. It will be {@code null} if there is no previous value.
     */
    @Override
    public final Object put(String propertyName, Object propertyValue)
    {
        if (propertyName == null || propertyName.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }

        Object last = get(propertyName);
        if (propertyValue instanceof Map) {
            super.put(propertyName, new ClientPropertyCollection((Map<? extends String, Object>) propertyValue));
        } else {
            super.put(propertyName, propertyValue);
        }

        if (!propertyName.equals(VERSION_TAG) && !propertyName.equals(METADATA_TAG)) {
            ParserUtility.validateMap(this);
        }

        return last;
    }

    /**
     * Gets the entries of the ClientPropertyCollection as a set of Properties to be sent to the IoT hub service.
     * @return
     */
    public Set<Property> getCollectionAsSetOfProperty()
    {
        HashSet<Property> toReturn = new HashSet<>();
        for (Map.Entry<String, Object> entry : this.entrySet())
        {
            toReturn.add(new Property(entry.getKey(), entry.getValue()));
        }
        return toReturn;
    }

    /**
     * Gets the value with the specified key cast to a specific type.
     *
     * <p>This will use the underlying serializer to ensure proper deserialization of the object</p>
     * @param propertyName The key of the property to get.
     * @param <T> The type to cast the object to.
     * @return The specified value cast as {@link T}. {@code null} if the key is not found.
     */
    public <T> T getValue(String propertyName)
    {
        Object objectToGet = this.get(propertyName);
        if (objectToGet != null)
        {
            return Convention.getPayloadSerializer().convertFromObject(objectToGet);
        }
        return null;
    }

    /**
     * Gets the component with the specified key cast to a specific type.
     *
     * <p>This will use the underlying serializer to ensure proper deserialization of the object. This will also validate that the property you're trying to get is a component.</p>
     * @param componentName The key of the component to get.
     * @param <T> The type to cast the object to.
     * @return The specified value cast as {@link T}. {@code null} if the component is not found.
     */
    public <T> T getComponent(String componentName)
    {
        Object objectToGet = this.get(componentName);
        if (objectToGet != null  && Convention.getPayloadSerializer().getNestedObjectValue(objectToGet, ConventionConstants.COMPONENT_IDENTIFIER_KEY) != null)
        {
            return Convention.getPayloadSerializer().convertFromObject(objectToGet);
        }
        return null;
    }

    /**
     * Gets the property nested in the component with the specified key cast to a specific type.
     *
     * <p>This will use the underlying serializer to ensure proper deserialization of the object. This will also validate that the property you're trying to get is a component.</p>
     * @param componentName The name of the component to get.
     * @param propertyName The name of the property to get.
     * @param <T> The type to cast the object to.
     * @return The specified value cast as {@link T}. {@code null} if the component is not found.
     */
    public <T> T getValueForComponent(String componentName, String propertyName)
    {
        Object objectToGet = this.getComponent(componentName);
        if (objectToGet != null)
        {
            return Convention.getPayloadSerializer().getNestedObjectValue(objectToGet, propertyName);
        }
        return null;
    }

    /**
     * Add a single new entry in the ClientPropertyCollection nested under a component.
     *
     * <p> Override {@code HashMap.put(String, Object)}.
     *
     * <p> This function will add a single pair key value to the ClientPropertyCollection.
     *
     * @param componentName The name of the component to get.
     * @param propertyName   the {@code String} that represents the key of the new entry. It cannot be {@code null} or empty.
     * @param propertyValue the {@code Object} that represents the value of the new entry. It cannot be user defined type or array.
     * @return The {@code Object} that corresponds to the last value of this key. It will be {@code null} if there is no previous value.
     */
    public final void putComponentProperty(String componentName, String propertyName, Object propertyValue)
    {
        HashMap<String, Object> mapToAdd = new HashMap<>();
        mapToAdd.put(ConventionConstants.COMPONENT_IDENTIFIER_KEY, ConventionConstants.COMPONENT_IDENTIFIER_VALUE);
        mapToAdd.put(propertyName, propertyValue);
        put(componentName, mapToAdd);
    }
}
