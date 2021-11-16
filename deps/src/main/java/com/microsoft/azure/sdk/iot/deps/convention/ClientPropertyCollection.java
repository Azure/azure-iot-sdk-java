// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.convention;

import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.util.Tools;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * The client property collection that is aware of the {@link PayloadConvention}.
 * <p>
 *     The client property collection is similar to the TwinCollection. It is backed by a Map and you can access properties
 *     by name. But, where it is different is we allow strong typing from the serializer and it is natively component
 *     aware.
 * </p>
 * <p>
 *     For example in the following code snippet we can see there is a complex object defined in the payload. This object
 *     is also part of a component. To get this complex object you can do the following:
 * {@code
 *  propCollection.getValueForComponent("myComponent","myComponentPropety", complexObject.class)
 * }
 * </p>
 */
public class ClientPropertyCollection extends PayloadCollection
{
    // the Twin collection version
    private static final String VERSION_TAG = "$version";

    @Getter
    private Long version;

    // the Twin collection metadata
    private static final String METADATA_TAG = "$metadata";

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private ClientMetadata metadata;

    private final Map<String, ClientMetadata> metadataMap = new HashMap<>();

    /**
     * Default constructor for the class.
     */
    public ClientPropertyCollection()
    {
        super();
    }

    /**
     * Converts a Message payload to a client property collection.
     */
    public ClientPropertyCollection(byte[] clientPropertyMessage, PayloadConvention convention, boolean createWritablePropertyCollection)
    {
        super();
        Convention = convention;
        Map<String, Object> collectionToCopy = Convention.getObjectFromBytes(clientPropertyMessage, ClientPropertyCollection.class);
        if (createWritablePropertyCollection)
        {
            putAllAsWritableStart(collectionToCopy);
        }
        else
        {
            fromMapInternal(collectionToCopy, this);
        }
    }

    /**
     * Constructor that is used to convert a {@link Map} to a ClientPropertyCollection
     * TODO ADD NOTE ABOUT RAW MAP AND THE PROBLEMS THAT ARISE
     * @param map The map you wish to convert.
     */
    public ClientPropertyCollection(Map<? extends String, Object> map)
    {
        this.putAll(map);
    }

    /**
     * Converts an existing {@link Map} to a ClientPropertyCollection
     *
     * @param mapToConvert The map you wish to convert.
     * @return A ClientPropertyCollection initialized with values from the existing Map.
     */
    public static ClientPropertyCollection fromMap(Map<String, Object> mapToConvert)
    {
        ClientPropertyCollection clientPropertyCollection = new ClientPropertyCollection();
        clientPropertyCollection.fromMapInternal(mapToConvert, clientPropertyCollection);
        return clientPropertyCollection;
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
    public final void putAll(Map<? extends String, ?> map)
    {
        if ((map == null) || map.isEmpty())
        {
            throw new IllegalArgumentException("map to add cannot be null or empty.");
        }

        for (Map.Entry<? extends String, ?> entry : map.entrySet())
        {
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
     * @param propertyName  the {@code String} that represents the name of the new entry. It cannot be {@code null} or empty.
     * @param propertyValue the {@code Object} that represents the value of the new entry. It cannot be user defined type or array.
     * @return The {@code Object} that corresponds to the last value of this key. It will be {@code null} if there is no previous value.
     */
    @Override
    public final Object put(String propertyName, Object propertyValue)
    {
        if (propertyName == null || propertyName.isEmpty())
        {
            throw new NullPointerException("propertyName cannot be null or empty");
        }

        Object last = get(propertyName);
        if (propertyValue instanceof Map)
        {
            super.put(propertyName, new ClientPropertyCollection((Map<? extends String, Object>) propertyValue));
        }
        else
        {
            super.put(propertyName, propertyValue);
        }

        if (!propertyName.equals(VERSION_TAG) && !propertyName.equals(METADATA_TAG))
        {
            ParserUtility.validateMap(this);
        }

        return last;
    }

    /**
     * Gets the value with the specified key cast to a specific type.
     *
     * <p>This will use the underlying serializer to ensure proper deserialization of the object</p>
     *
     * @param propertyName The key of the property to get.
     * @param <T>          The type to cast the object to.
     * @return The specified value cast as {@link T}. {@code null} if the key is not found.
     */
    public <T> T getValue(String propertyName, Class<T> typeOfT)
    {
        Object objectToGet = this.get(propertyName);
        return convertFromObject(objectToGet, typeOfT);
    }

    /**
     * Gets the property nested in the component with the specified key cast to a specific type.
     *
     * <p>This will use the underlying serializer to ensure proper deserialization of the object. This will also validate that the property you're trying to get is a component.</p>
     *
     * @param componentName The name of the component to get.
     * @param propertyName  The name of the property to get.
     * @param <T>           The type to cast the object to.
     * @return The specified value cast as {@link T}. {@code null} if the component is not found.
     */
    public <T> T getValueForComponent(String componentName, String propertyName, Class<T> typeOfT)
    {
        // Check to see if the component is a map
        Map<String, Object> objectToGet = getComponentMapFromInternalMap(componentName);

        // If so, try to convert from the object
        if (objectToGet != null)
        {
            return convertFromObject(objectToGet.get((propertyName)), typeOfT);
        }

        // If it's not a Map<> we're likely looking at a JsonObject
        return Convention.getPayloadSerializer().getNestedObjectValue(this.get(componentName), propertyName, typeOfT);
    }

    /**
     * Add a single new entry in the ClientPropertyCollection nested under a component.
     *
     * <p> Override {@code HashMap.put(String, Object)}.
     *
     * <p> This function will add a single pair key value to the ClientPropertyCollection.
     *
     * @param componentName The name of the component to get.
     * @param propertyName  the {@code String} that represents the key of the new entry. It cannot be {@code null} or empty.
     * @param propertyValue the {@code Object} that represents the value of the new entry. It cannot be user defined type or array.
     */
    public final void putComponentProperty(String componentName, String propertyName, Object propertyValue)
    {
        if (!propertyName.equals(ConventionConstants.COMPONENT_IDENTIFIER_KEY))
        {
            // Check to see if the component is a map
            ClientPropertyCollection mapToAdd = getComponentMapFromInternalMap(componentName);
            if (mapToAdd == null)
            {
                mapToAdd = new ClientPropertyCollection();
                mapToAdd.put(ConventionConstants.COMPONENT_IDENTIFIER_KEY, ConventionConstants.COMPONENT_IDENTIFIER_VALUE);
            }
            mapToAdd.put(propertyName, propertyValue);
            put(componentName, mapToAdd);
        }
    }

    /**
     * Add a single new entry in the ClientPropertyCollection nested under a component.
     *
     * <p> Override {@code HashMap.put(String, Object)}.
     *
     * <p> This function will add a single pair key value to the ClientPropertyCollection.
     *
     * @param componentName The name of the component to add data to.
     * @param propertyValue the {@code Object} that represents the value of the new entry. It cannot be user defined type or array.
     */
    public final void putComponent(String componentName, Object propertyValue)
    {
        // Check to see if the component is a map
        ClientPropertyCollection mapToAdd = getComponentMapFromInternalMap(componentName);
        if (mapToAdd == null)
        {
            if (propertyValue instanceof Map)
            {
                mapToAdd = (ClientPropertyCollection) propertyValue;
            }
            else
            {
                mapToAdd = convertFromObject(propertyValue, ClientPropertyCollection.class);
            }
        }
        mapToAdd.put(ConventionConstants.COMPONENT_IDENTIFIER_KEY, ConventionConstants.COMPONENT_IDENTIFIER_VALUE);
        put(componentName, mapToAdd);
    }

    private ClientPropertyCollection getComponentMapFromInternalMap(String componentName)
    {
        return getComponentMapFromMap(componentName, this);
    }

    private ClientPropertyCollection getComponentMapFromMap(String componentName, Map mapToCheck)
    {
        Object componentCheck = mapToCheck.get(componentName);
        if (componentCheck instanceof Map)
        {
            ClientPropertyCollection componentMap = (ClientPropertyCollection) componentCheck;
            String identifier = (String) componentMap.get(ConventionConstants.COMPONENT_IDENTIFIER_KEY);
            if (identifier != null && identifier.equals(ConventionConstants.COMPONENT_IDENTIFIER_VALUE))
            {
                return componentMap;
            }
        }
        return null;
    }

    private ClientPropertyCollection getPropertyMapFromMap(String propertyName, Map mapToCheck)
    {
        Object propertyCheck = mapToCheck.get(propertyName);
        if (propertyCheck instanceof Map)
        {
            ClientPropertyCollection componentMap = (ClientPropertyCollection) propertyCheck;
            return componentMap;
        }
        return null;
    }

    private static void addMetadata(ClientPropertyCollection clientPropertyCollection, Map<? extends String, Object> metadata)
    {
        String lastUpdated = null;
        Integer lastUpdatedVersion = null;
        String lastUpdatedBy = null;
        String lastUpdatedByDigest = null;
        for (Map.Entry<? extends String, Object> entry : metadata.entrySet())
        {
            String key = entry.getKey();
            if (key.equals(ClientMetadata.LAST_UPDATE_TAG))
            {
                lastUpdated = (String) entry.getValue();
            }
            else if ((key.equals(ClientMetadata.LAST_UPDATE_VERSION_TAG)) && (entry.getValue() instanceof Number))
            {
                lastUpdatedVersion = ((Number) entry.getValue()).intValue();
            }
            else if (key.equals(ClientMetadata.LAST_UPDATED_BY))
            {
                lastUpdatedBy = (String) entry.getValue();
            }
            else if (key.equals(ClientMetadata.LAST_UPDATED_BY_DIGEST))
            {
                lastUpdatedByDigest = (String) entry.getValue();
            }
            else
            {
                Object valueInCollection = clientPropertyCollection.get(key);
                if (valueInCollection == null)
                {
                    // If the property (key) exists in metadata but not in twinCollection metadata is inconsistent.
                    throw new IllegalArgumentException("Twin metadata is inconsistent for property: " + key);
                }

                ClientMetadata clientMetadata = ClientMetadata.tryExtractFromMap(entry.getValue());
                if (clientMetadata != null)
                {
                    clientPropertyCollection.metadataMap.put(key, clientMetadata);
                }

                if (valueInCollection instanceof ClientPropertyCollection)
                {
                    clientPropertyCollection.addMetadata((ClientPropertyCollection) valueInCollection, (Map<? extends String, Object>) entry.getValue());
                }
            }
        }

        if ((lastUpdatedVersion != null) || !Tools.isNullOrEmpty(lastUpdated))
        {
            clientPropertyCollection.setMetadata(new ClientMetadata(lastUpdated, lastUpdatedVersion, lastUpdatedBy, lastUpdatedByDigest));
        }
    }

    private <T> T convertFromObject(Object objectToGet, Class<T> typeOfT)
    {
        // Check to see if this is the type we're looking for.
        // Some things to note about this is we're actively not type checking EVERY single conversion.
        // It's assumed that the client writer will know the type coming from the deserialized object.
        // We do handle odities where a customer might ask for a boolean
        if (typeOfT.isInstance(objectToGet))
        {
            return (T) objectToGet;
        }
        else if (ReflectionUtility.INSTANCE.canCastPrimitive(typeOfT) == ReflectionUtility.TypeToReflect.CHAR)
        {
            return (T) (Character) objectToGet;
        }
        else if (ReflectionUtility.INSTANCE.canCastPrimitive(typeOfT) == ReflectionUtility.TypeToReflect.BOOL)
        {
            return (T) (Boolean) objectToGet;
        }
        else if (ReflectionUtility.INSTANCE.canCastPrimitive(typeOfT) != null)
        {
            // If we're here the type to cast to was a numeric primitive
            // We will first convert to a number and then try to get the cast type.
            // There is boxing going on in here but there's no way around it if we want to use generics
            Number num = (Number) objectToGet;
            switch (ReflectionUtility.INSTANCE.canCastPrimitive(typeOfT))
            {
                case INT:
                    return (T) (Integer) num.intValue();
                case BYTE:
                    return (T) (Byte) num.byteValue();
                case DOUBLE:
                    return (T) (Double) num.doubleValue();
                case FLOAT:
                    return (T) (Float) num.floatValue();
                case LONG:
                    return (T) (Long) num.longValue();
                case SHORT:
                    return (T) (Short) num.shortValue();
            }
        }

        // if it's not we should try to deserialize it with our convention serializer
        if (objectToGet != null && Convention != null)
        {
            return Convention.getPayloadSerializer().convertFromObject(objectToGet, typeOfT);
        }

        return null;
    }

    private void putAllAsWritableStart(Map<? extends String, ?> map)
    {
        if ((map == null) || map.isEmpty())
        {
            throw new IllegalArgumentException("map to add cannot be null or empty.");
        }
        this.version = ((Number) map.get(VERSION_TAG)).longValue();

        // Loop through the toplevel properties
        for (Map.Entry<? extends String, ?> rootOfMap : map.entrySet())
        {
            // Loop over all properties ignoring the version and metadata
            if (!rootOfMap.getKey().equals(VERSION_TAG) && !rootOfMap.getKey().equals(METADATA_TAG))
            {
                // Check to see if the entry is a component
                Map<String, Object> componentMapFromMap = getComponentMapFromMap(rootOfMap.getKey(), map);
                // If so, try to add all properties for the component
                if (componentMapFromMap != null)
                {
                    for (Map.Entry<? extends String, ?> componentProperty : componentMapFromMap.entrySet())
                    {
                        this.putComponentProperty(rootOfMap.getKey(), componentProperty.getKey(), Convention.createWritablePropertyResponse(componentProperty.getValue(), 0, this.version));
                    }
                }
                else
                {
                    this.put(rootOfMap.getKey(), Convention.createWritablePropertyResponse(rootOfMap.getValue(), 0, this.version));
                }
            }
        }
        Map<? extends String, Object> metadata = (Map<? extends String, Object>) map.get(METADATA_TAG);
        if (metadata != null)
        {
            ClientPropertyCollection.addMetadata(this, metadata);
        }

    }

    private void fromMapInternal(Map<String, Object> mapToConvert, ClientPropertyCollection clientPropertyCollection)
    {
        Map<? extends String, Object> metadata = null;

        for (Map.Entry<? extends String, Object> entry : mapToConvert.entrySet())
        {
            if (entry.getKey().equals(VERSION_TAG))
            {
                if (!(entry.getValue() instanceof Number))
                {
                    throw new IllegalArgumentException("version is not a number");
                }

                clientPropertyCollection.version = ((Number) entry.getValue()).longValue();
            }
            else if (entry.getKey().equals(METADATA_TAG))
            {
                metadata = (Map<? extends String, Object>) entry.getValue();
            }
            else
            {
                clientPropertyCollection.put(entry.getKey(), entry.getValue());
            }
        }

        if (metadata != null)
        {
            ClientPropertyCollection.addMetadata(clientPropertyCollection, metadata);
        }
    }

}
