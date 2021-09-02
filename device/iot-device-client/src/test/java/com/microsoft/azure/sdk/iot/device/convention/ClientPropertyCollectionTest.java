package tests.unit.com.microsoft.azure.sdk.iot.device.convention;

import com.google.gson.GsonBuilder;
import com.microsoft.azure.sdk.iot.deps.convention.ConventionConstants;
import com.microsoft.azure.sdk.iot.deps.convention.DefaultPayloadConvention;
import com.microsoft.azure.sdk.iot.deps.convention.GsonWritablePropertyResponse;
import com.microsoft.azure.sdk.iot.deps.convention.WritablePropertyResponse;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.convention.ClientMetadata;
import com.microsoft.azure.sdk.iot.device.convention.ClientPropertyCollection;
import jnr.ffi.annotations.In;
import lombok.Builder;
import lombok.Data;
import org.junit.Test;

import javax.swing.text.html.parser.Parser;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@Data
@Builder
class complexObject {
    private String stringValue;
    private Integer integerValue;
    private Double doubleValue;
    private boolean booleanValue;

    public boolean equals(Object obj) {
        //null instanceof Object will always return false
        if (!(obj instanceof complexObject))
            return false;
        if (obj == this)
            return true;
        return  this.stringValue.equals(((complexObject) obj).stringValue) &&
                this.integerValue.equals(((complexObject) obj).integerValue) &&
                this.doubleValue.equals(((complexObject) obj).doubleValue) &&
                this.booleanValue == ((complexObject) obj).booleanValue;
    }
}

public class ClientPropertyCollectionTest
{
    public static final String LAST_UPDATED_TIME = "2021-07-27T13:45:44.238Z";
    public static final String VERSION = "42";

    private static final String FULL_RESPONSE = "" +
            "{\n" +
            "      \"stringValue\": \"defaultComponentString\",\n" +
            "      \"complexValue\": {\n" +
            "         \"stringValue\": \"defaultComponentString\",\n" +
            "         \"integerValue\": 12345,\n" +
            "         \"doubleValue\": 54321.12345,\n" +
            "         \"booleanValue\": true\n" +
            "      },\n" +
            "      \"integerValue\": 12345,\n" +
            "      \"booleanValue\": true,\n" +
            "      \"doubleValue\": 54321.12345,\n" +
            "      \"longValue\": 5432115321461121,\n" +
            "      \"shortValue\": 5055,\n" +
            "      \"writableProperty\": {\n" +
            "         \"value\": \"Value to get back\",\n" +
            "         \"ac\": 200,\n" +
            "         \"av\": 5,\n" +
            "         \"ad\": \"Longer description for the fun of it.\"\n" +
            "      },\n" +
            "   \"$version\":" + VERSION + "\n" +
            "}";

    private static final String FULL_RESPONSE_WITH_COMPONENT = "" +
            "{\n" +
            "   \"testableComponent\": {\n" +
            "      \"stringValue\": \"defaultComponentString\",\n" +
            "      \"complexValue\": {\n" +
            "         \"stringValue\": \"defaultComponentString\",\n" +
            "         \"integerValue\": 12345,\n" +
            "         \"doubleValue\": 54321.12345,\n" +
            "         \"booleanValue\": true\n" +
            "      },\n" +
            "      \"__t\": \"c\",\n" +
            "      \"integerValue\": 12345,\n" +
            "      \"booleanValue\": true,\n" +
            "      \"doubleValue\": 54321.12345,\n" +
            "      \"longValue\": 5432115321461121,\n" +
            "      \"shortValue\": 5055,\n" +
            "      \"writableProperty\": {\n" +
            "         \"value\": \"Value to get back\",\n" +
            "         \"ac\": 200,\n" +
            "         \"av\": 5,\n" +
            "         \"ad\": \"Longer description for the fun of it.\"\n" +
            "      }\n" +
            "   },\n" +
            "   \"$version\": " + VERSION + "\n" +
            "}";

    private static final String DEFAULT_COMPONENT_STRING_VALUE = "defaultComponentString";
    private static final Integer DEFAULT_COMPONENT_INTEGER_VALUE = 12345;
    private static final Double DEFAULT_COMPONENT_DOUBLE_VALUE = 54321.12345;
    private static final Boolean DEFAULT_COMPONENT_BOOLEAN_VALUE = true;
    private static final Long DEFAULT_COMPONENT_LONG_VALUE = 5432115321461121L;
    private static final Short DEFAULT_COMPONENT_SHORT_VALUE = 505_5;

    private static final long DEFAULT_COMPONENT_LONG_VALUE_PRIMITIVE = 5432115321461121L;
    private static final short DEFAULT_COMPONENT_SHORT_VALUE_PRIMITIVE = 505_5;
    private static final double DEFAULT_COMPONENT_DOUBLE_VALUE_PRIMITIVE = 54321.12345;
    private static final boolean DEFAULT_COMPONENT_BOOLEAN_VALUE_PRIMITIVE = true;

    private static final String DEFAULT_COMPONENT_STRING_KEY = "stringValue";
    private static final String DEFAULT_COMPONENT_INTEGER_KEY = "integerValue";
    private static final String DEFAULT_COMPONENT_DOUBLE_KEY = "doubleValue";
    private static final String DEFAULT_COMPONENT_BOOLEAN_KEY = "booleanValue";
    private static final String DEFAULT_COMPONENT_LONG_KEY = "longValue";
    private static final String DEFAULT_COMPONENT_SHORT_KEY = "shortValue";
    private static final String DEFAULT_COMPONENT_COMPLEX_KEY = "complexValue";

    private static final String DEFAULT_COMPONENT_WRITABLE_KEY = "writableProperty";
    private static final int WRITABLE_PROPERTY_ACK = 200;
    private static final long WRITABLE_PROPERTY_VERSION = 5;
    private static final String WRITABLE_PROPERTY_VALUE = "Value to get back";
    private static final String WRITABLE_PROPERTY_DESCRIPTION = "Longer description for the fun of it.";

    private static final String TESTABLE_COMPONENT_KEY = "testableComponent";

    private HashMap<String, Object> defaultComponentMap()
    {
        // Generate a map that creates the default components
        HashMap<String, Object> map = new HashMap<>();
        generateMap(map);
        return map;
    }

    private HashMap<String, Object> testableComponentMap()
    {
        // Generate a map with a component object
        HashMap<String, Object> map = new HashMap<>();
        generateComponentMap(map);
        return map;
    }


    private ClientPropertyCollection generateClientPropertyCollectionFromJSON(String jsonToConvert)
    {
        ClientPropertyCollection clientPropertyCollection =  DefaultPayloadConvention.getInstance().getPayloadSerializer().deserializeToType(jsonToConvert, ClientPropertyCollection.class);
        return ClientPropertyCollection.fromMap(clientPropertyCollection);
    }

    private void generateMap(HashMap<String, Object> map)
    {
        // Generate a standard map
        map.put(DEFAULT_COMPONENT_STRING_KEY, DEFAULT_COMPONENT_STRING_VALUE);
        map.put(DEFAULT_COMPONENT_INTEGER_KEY, DEFAULT_COMPONENT_INTEGER_VALUE);
        map.put(DEFAULT_COMPONENT_DOUBLE_KEY, DEFAULT_COMPONENT_DOUBLE_VALUE);
        map.put(DEFAULT_COMPONENT_BOOLEAN_KEY, DEFAULT_COMPONENT_BOOLEAN_VALUE);
        map.put(DEFAULT_COMPONENT_LONG_KEY, DEFAULT_COMPONENT_LONG_VALUE);
        map.put(DEFAULT_COMPONENT_SHORT_KEY, DEFAULT_COMPONENT_SHORT_VALUE);
        map.put(DEFAULT_COMPONENT_COMPLEX_KEY, defaultComplexObject());
        map.put(DEFAULT_COMPONENT_WRITABLE_KEY, defaultWritableObject() );
    }

    private void generateComponentMap(HashMap<String, Object> map)
    {
        // Generate a component object so we can test the passing scenario
        HashMap componentMap = new HashMap<String, Object>();
        generateMap(componentMap);
        componentMap.put(ConventionConstants.COMPONENT_IDENTIFIER_KEY, ConventionConstants.COMPONENT_IDENTIFIER_VALUE);
        map.put(TESTABLE_COMPONENT_KEY, componentMap);
    }

    private void generateNestedObjectMap(HashMap<String, Object> map)
    {
        // Generate a non-component object so we can test the failing scenario
        HashMap componentMap = new HashMap<String, Object>();
        generateMap(componentMap);
        map.put(TESTABLE_COMPONENT_KEY, componentMap);
    }

    private void generateComponentMapWithPutComponent(ClientPropertyCollection map)
    {
        // Generate a standard map
        map.putComponentProperty(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_STRING_KEY, DEFAULT_COMPONENT_STRING_VALUE);
        map.putComponentProperty(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_INTEGER_KEY, DEFAULT_COMPONENT_INTEGER_VALUE);
        map.putComponentProperty(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_DOUBLE_KEY, DEFAULT_COMPONENT_DOUBLE_VALUE);
        map.putComponentProperty(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_BOOLEAN_KEY, DEFAULT_COMPONENT_BOOLEAN_VALUE);
        map.putComponentProperty(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_LONG_KEY, DEFAULT_COMPONENT_LONG_VALUE);
        map.putComponentProperty(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_SHORT_KEY, DEFAULT_COMPONENT_SHORT_VALUE);
        map.putComponentProperty(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_WRITABLE_KEY, defaultWritableObject());
        map.putComponentProperty(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_COMPLEX_KEY, defaultComplexObject());

    }

    private complexObject defaultComplexObject()
    {
        return complexObject.builder().stringValue(DEFAULT_COMPONENT_STRING_VALUE).integerValue(DEFAULT_COMPONENT_INTEGER_VALUE).doubleValue(DEFAULT_COMPONENT_DOUBLE_VALUE).booleanValue(DEFAULT_COMPONENT_BOOLEAN_VALUE).build();
    }

    private GsonWritablePropertyResponse defaultWritableObject()
    {
        return GsonWritablePropertyResponse.builder().Value(WRITABLE_PROPERTY_VALUE).ackVersion(WRITABLE_PROPERTY_VERSION).ackCode(WRITABLE_PROPERTY_ACK).ackDescription(WRITABLE_PROPERTY_DESCRIPTION).build();
    }


    @Test
    public void fromMap()
    {
        ClientPropertyCollection propCollection = ClientPropertyCollection.fromMap(defaultComponentMap());
        assertPropCollectionWithGetForDefaultComponent(propCollection);
    }

    private void assertPropCollectionWithGetForDefaultComponent(ClientPropertyCollection propCollection)
    {
        assertEquals(8, propCollection.size());
        assertEquals(DEFAULT_COMPONENT_STRING_VALUE, propCollection.get(DEFAULT_COMPONENT_STRING_KEY));
        assertEquals(DEFAULT_COMPONENT_INTEGER_VALUE, propCollection.get(DEFAULT_COMPONENT_INTEGER_KEY));
        assertEquals(DEFAULT_COMPONENT_DOUBLE_VALUE, propCollection.get(DEFAULT_COMPONENT_DOUBLE_KEY));
        assertEquals(DEFAULT_COMPONENT_BOOLEAN_VALUE, propCollection.get(DEFAULT_COMPONENT_BOOLEAN_KEY));
        assertEquals(DEFAULT_COMPONENT_LONG_VALUE, propCollection.get(DEFAULT_COMPONENT_LONG_KEY));
        assertEquals(DEFAULT_COMPONENT_SHORT_VALUE, propCollection.get(DEFAULT_COMPONENT_SHORT_KEY));
        assertEquals(defaultWritableObject(), propCollection.get(DEFAULT_COMPONENT_WRITABLE_KEY));
        assertEquals(defaultComplexObject(), propCollection.get(DEFAULT_COMPONENT_COMPLEX_KEY));
    }

    private void assertPropCollectionWithGeForTestableComponent(ClientPropertyCollection propCollection)
    {
        assertEquals(1, propCollection.size());
        assertTrue(propCollection.get(TESTABLE_COMPONENT_KEY) instanceof Map);

        Map testableMap = (Map<String, Object>)propCollection.get(TESTABLE_COMPONENT_KEY);

        assertEquals(8, testableMap.size());
        assertEquals(ConventionConstants.COMPONENT_IDENTIFIER_VALUE, testableMap.get(ConventionConstants.COMPONENT_IDENTIFIER_KEY));
        assertEquals(DEFAULT_COMPONENT_STRING_VALUE, testableMap.get(DEFAULT_COMPONENT_STRING_KEY));
        assertEquals(DEFAULT_COMPONENT_INTEGER_VALUE, testableMap.get(DEFAULT_COMPONENT_INTEGER_KEY));
        assertEquals(DEFAULT_COMPONENT_DOUBLE_VALUE, testableMap.get(DEFAULT_COMPONENT_DOUBLE_KEY));
        assertEquals(DEFAULT_COMPONENT_BOOLEAN_VALUE, testableMap.get(DEFAULT_COMPONENT_BOOLEAN_KEY));
        assertEquals(DEFAULT_COMPONENT_LONG_VALUE, testableMap.get(DEFAULT_COMPONENT_LONG_KEY));
        assertEquals(DEFAULT_COMPONENT_SHORT_VALUE, testableMap.get(DEFAULT_COMPONENT_SHORT_KEY));
        assertEquals(defaultWritableObject(), testableMap.get(DEFAULT_COMPONENT_WRITABLE_KEY));
        assertEquals(defaultComplexObject(), testableMap.get(DEFAULT_COMPONENT_COMPLEX_KEY));
    }

    private void assertPropCollectionWithGetForTestableComponent(ClientPropertyCollection propCollection)
    {
        assertEquals(1, propCollection.size());
        assertEquals(DEFAULT_COMPONENT_STRING_VALUE, propCollection.getValueForComponent(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_STRING_KEY, String.class));

        assertEquals(DEFAULT_COMPONENT_INTEGER_VALUE, propCollection.getValueForComponent(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_INTEGER_KEY, Integer.class));
        assertEquals(DEFAULT_COMPONENT_INTEGER_VALUE, propCollection.getValueForComponent(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_INTEGER_KEY, int.class));

        assertEquals(DEFAULT_COMPONENT_BOOLEAN_VALUE, propCollection.getValueForComponent(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_BOOLEAN_KEY, Boolean.class));
        assertEquals(DEFAULT_COMPONENT_BOOLEAN_VALUE_PRIMITIVE, propCollection.getValueForComponent(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_BOOLEAN_KEY, boolean.class));

        assertEquals(DEFAULT_COMPONENT_DOUBLE_VALUE, propCollection.getValueForComponent(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_DOUBLE_KEY, Double.class));
        assertEquals(DEFAULT_COMPONENT_DOUBLE_VALUE_PRIMITIVE, propCollection.getValueForComponent(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_DOUBLE_KEY, double.class), 0);

        assertEquals(DEFAULT_COMPONENT_LONG_VALUE, propCollection.getValueForComponent(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_LONG_KEY, Long.class));
        assertEquals(DEFAULT_COMPONENT_LONG_VALUE_PRIMITIVE, propCollection.getValueForComponent(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_LONG_KEY, long.class), 0);

        assertEquals(DEFAULT_COMPONENT_SHORT_VALUE, propCollection.getValueForComponent(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_SHORT_KEY, Short.class));
        assertEquals(DEFAULT_COMPONENT_SHORT_VALUE_PRIMITIVE, propCollection.getValueForComponent(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_SHORT_KEY, short.class), 0);

        assertEquals(defaultComplexObject(), propCollection.getValueForComponent(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_COMPLEX_KEY, complexObject.class));

        assertEquals(defaultWritableObject(), propCollection.getValueForComponent(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_WRITABLE_KEY, WritablePropertyResponse.class));
        assertEquals(defaultWritableObject(), propCollection.getValueForComponent(TESTABLE_COMPONENT_KEY, DEFAULT_COMPONENT_WRITABLE_KEY, GsonWritablePropertyResponse.class));
    }

    private void assertPropCollectionWithGetValue(ClientPropertyCollection propCollection)
    {
        assertEquals(8, propCollection.size());
        assertEquals(DEFAULT_COMPONENT_STRING_VALUE, propCollection.getValue(DEFAULT_COMPONENT_STRING_KEY, String.class));

        assertEquals(DEFAULT_COMPONENT_INTEGER_VALUE, propCollection.getValue(DEFAULT_COMPONENT_INTEGER_KEY, Integer.class));
        assertEquals(DEFAULT_COMPONENT_INTEGER_VALUE, propCollection.getValue(DEFAULT_COMPONENT_INTEGER_KEY, int.class));

        assertEquals(DEFAULT_COMPONENT_BOOLEAN_VALUE, propCollection.getValue(DEFAULT_COMPONENT_BOOLEAN_KEY, Boolean.class));
        assertEquals(DEFAULT_COMPONENT_BOOLEAN_VALUE_PRIMITIVE, propCollection.getValue(DEFAULT_COMPONENT_BOOLEAN_KEY, boolean.class));

        assertEquals(DEFAULT_COMPONENT_DOUBLE_VALUE, propCollection.getValue(DEFAULT_COMPONENT_DOUBLE_KEY, Double.class));
        assertEquals(DEFAULT_COMPONENT_DOUBLE_VALUE_PRIMITIVE, propCollection.getValue(DEFAULT_COMPONENT_DOUBLE_KEY, double.class), 0);

        assertEquals(DEFAULT_COMPONENT_LONG_VALUE, propCollection.getValue(DEFAULT_COMPONENT_LONG_KEY, Long.class));
        assertEquals(DEFAULT_COMPONENT_LONG_VALUE_PRIMITIVE, propCollection.getValue(DEFAULT_COMPONENT_LONG_KEY, long.class), 0);

        assertEquals(DEFAULT_COMPONENT_SHORT_VALUE, propCollection.getValue(DEFAULT_COMPONENT_SHORT_KEY, Short.class));
        assertEquals(DEFAULT_COMPONENT_SHORT_VALUE_PRIMITIVE, propCollection.getValue(DEFAULT_COMPONENT_SHORT_KEY, short.class), 0);

        assertEquals(defaultComplexObject(), propCollection.getValue(DEFAULT_COMPONENT_COMPLEX_KEY, complexObject.class));

        assertEquals(defaultWritableObject(), propCollection.getValue(DEFAULT_COMPONENT_WRITABLE_KEY, WritablePropertyResponse.class));
        assertEquals(defaultWritableObject(), propCollection.getValue(DEFAULT_COMPONENT_WRITABLE_KEY, GsonWritablePropertyResponse.class));
    }

    @Test
    public void putAll_FromMap()
    {
        // It's redundant but now we know

        // Test the constructor
        ClientPropertyCollection propCollectionFromConstructor = new ClientPropertyCollection(defaultComponentMap());
        assertPropCollectionWithGetForDefaultComponent(propCollectionFromConstructor);

        // Test the method
        ClientPropertyCollection propCollectionFromPutAll = new ClientPropertyCollection();
        propCollectionFromPutAll.putAll(defaultComponentMap());
        assertPropCollectionWithGetForDefaultComponent(propCollectionFromPutAll);
    }

    @Test
    public void put_FromMap()
    {
        ClientPropertyCollection propCollection = new ClientPropertyCollection();
        generateMap(propCollection);
        assertPropCollectionWithGetForDefaultComponent(propCollection);
    }

    @Test
    public void getCollectionAsSetOfProperty()
    {
    }

    @Test
    public void getValue_FromMap()
    {
        ClientPropertyCollection propCollection = new ClientPropertyCollection();
        generateMap(propCollection);
        assertPropCollectionWithGetValue(propCollection);
    }

    @Test
    public void getValueForComponent_FromMap()
    {
        ClientPropertyCollection propCollection = new ClientPropertyCollection();
        generateComponentMap(propCollection);
        assertPropCollectionWithGetForTestableComponent(propCollection);
    }

    @Test
    public void putComponentProperty_FromMap()
    {
        ClientPropertyCollection propCollection = new ClientPropertyCollection();
        generateComponentMapWithPutComponent(propCollection);
        assertPropCollectionWithGetForTestableComponent(propCollection);
    }

    @Test
    public void getValueForComponent_FromJSON()
    {
        ClientPropertyCollection propCollection = generateClientPropertyCollectionFromJSON(FULL_RESPONSE_WITH_COMPONENT);
        propCollection.setConvention(DefaultPayloadConvention.getInstance());
        assertPropCollectionWithGetForTestableComponent(propCollection);
    }

    @Test
    public void getValue_FromJSON()
    {
        ClientPropertyCollection propCollection = generateClientPropertyCollectionFromJSON(FULL_RESPONSE);
        propCollection.setConvention(DefaultPayloadConvention.getInstance());
        assertPropCollectionWithGetValue(propCollection);
    }

    @Test
    public void getVersion()
    {
        ClientPropertyCollection propCollection = generateClientPropertyCollectionFromJSON(FULL_RESPONSE);
        propCollection.setConvention(DefaultPayloadConvention.getInstance());
        assertEquals(Long.parseLong(VERSION), propCollection.getVersion(), 0);
    }

    @Test
    public void putAllAsWritableProperties_FromMessage()
    {
        // It's redundant but now we know

        Message msg = new Message(FULL_RESPONSE_WITH_COMPONENT);

        // Test the constructor
        ClientPropertyCollection propCollectionFromConstructor = new ClientPropertyCollection(msg, DefaultPayloadConvention.getInstance(), true);
        String s = "S";
        //assertPropCollectionWithGetForDefaultComponent(propCollectionFromPutAll);
    }
}