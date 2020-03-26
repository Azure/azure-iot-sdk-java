//Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.MessageProperty;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Unit test for Message Property class.
 * 87% methods, 87% lines covered
 */
public class MessagePropertyTest
{
    // Tests_SRS_MESSAGEPROPERTY_11_001: [The constructor shall save the property name and value.]
    // Tests_SRS_MESSAGEPROPERTY_11_004: [The function shall return the property name.]
    @Test
    public void constructorSavesPropertyName()
    {
        final String name = "test-name";
        final String value = "test-value";

        MessageProperty property = new MessageProperty(name, value);
        String testName = property.getName();

        final String expectedName = name;
        assertThat(testName, is(expectedName));
    }

    // Tests_SRS_MESSAGEPROPERTY_11_001: [The constructor shall save the property name and value.]
    // Tests_SRS_MESSAGEPROPERTY_11_005: [The function shall return the property value.]
    @Test
    public void constructorSavesPropertyValue()
    {
        final String name = "test-name";
        final String value = "test-value";

        MessageProperty property = new MessageProperty(name, value);
        String testValue = property.getValue();

        final String expectedValue = value;
        assertThat(testValue, is(expectedValue));
		
    }

	 @Test
    public void constructorSavesPropertyValueSlash()
    {
        final String name = "topic";
        final String value = "/news/sports";

        MessageProperty property = new MessageProperty(name, value);
        String testValue = property.getValue();

        final String expectedValue = value;
        assertThat(testValue, is(expectedValue));
    }
    
    // Tests_SRS_MESSAGEPROPERTY_11_002: [If the name contains a character that is not in US-ASCII the function shall throw an IllegalArgumentException.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsInvalidPropertyName()
    {
        final String invalidName = "Price in £"; 
        final String value = "test-value";

        new MessageProperty(invalidName, value);
    }

    // Tests_SRS_MESSAGEPROPERTY_11_008: [If the name is a reserved property name, the function shall throw an IllegalArgumentException.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsReservedPropertyName()
    {
        final String invalidName = "iothub-ack";
        final String value = "test-value";

        new MessageProperty(invalidName, value);
    }

    // Tests_SRS_MESSAGEPROPERTY_11_003: [If the value contains a character that is not in US-ASCII, the function shall throw an IllegalArgumentException.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsInvalidPropertyValue()
    {
        final String name = "test-,name";
        final String invalidValue = "t\u0130est-value";

        new MessageProperty(name, invalidValue);
    }

    // Tests_SRS_MESSAGEPROPERTY_11_006: [The function shall return true if and only if the property has the given name, where the names are compared in a case-insensitive manner.]
    @Test
    public void hasNameMatchesNameInCaseInsensitiveManner()
    {
        final String name = "test-name";
        final String value = "test-value";

        MessageProperty property = new MessageProperty(name, value);
        boolean testHasName = property.hasSameName("Test-Name");

        final boolean expectedHasName = true;
        assertThat(testHasName, is(expectedHasName));
    }

    // Tests_SRS_MESSAGEPROPERTY_11_006: [The function shall return true if and only if the property has the given name, where the names are compared in a case-insensitive manner.]
    @Test
    public void hasNameFailsForNonMatchingName()
    {
        final String name = "test-name";
        final String value = "test-value";

        MessageProperty property = new MessageProperty(name, value);
        boolean testHasName = property.hasSameName("test-mame");

        final boolean expectedHasName = false;
        assertThat(testHasName, is(expectedHasName));
    }

    // Tests_SRS_MESSAGEPROPERTY_11_007: [The function shall return true if and only if the name and value only use characters in: US-ASCII and the name is not a reserved property name.]
    @Test
    public void isValidAppPropertyReturnsTrueForValidAppProperty()
    {
        final String name = "test-name";
        final String value = "test-value";

        boolean testIsValidAppProperty =
                MessageProperty.isValidAppProperty(name, value);

        final boolean expectedIsValidAppProperty = true;
        assertThat(testIsValidAppProperty, is(expectedIsValidAppProperty));
    }

    // Tests_SRS_MESSAGEPROPERTY_11_007: [The function shall return true if and only if the name and value only use characters in: US-ASCII and the name is not a reserved property name.]
    @Test
    public void isValidAppPropertyReturnsFalseForInvalidAppProperty()
    {
        final String name = "test-name";
        final String illegalValue = "परीक्षण"; // Unicode is not supported in MessageProperty value

        boolean testIsValidAppProperty =
                MessageProperty.isValidAppProperty(name, illegalValue);

        final boolean expectedIsValidAppProperty = false;
        assertThat(testIsValidAppProperty, is(expectedIsValidAppProperty));
    }

    // Tests_SRS_MESSAGEPROPERTY_11_007: [The function shall return true if and only if the name and value only use characters in: US-ASCII and the name is not a reserved property name.]
    @Test
    public void isValidAppPropertyReturnsFalseForReservedProperty()
    {
        final String reservedName = "iothub-to";
        final String value = "test-value";

        boolean testIsValidAppProperty =
                MessageProperty.isValidAppProperty(reservedName, value);

        final boolean expectedIsValidAppProperty = false;
        assertThat(testIsValidAppProperty, is(expectedIsValidAppProperty));
    }

    @Test
    public void isValidAppPropertyReturnsTrueForUnusualAppPropertyKey()
    {
        final String name = "Test1234!#$%&'*+-.^_`|~";
        final String value = "test-value";

        boolean isValidProperty = MessageProperty.isValidAppProperty(name, value);

        assertTrue(isValidProperty);
    }

    @Test
    public void isValidAppPropertyReturnsTrueForUnusualAppPropertyValue()
    {
        final String name = "test-key";
        final String value = "Test1234!#$%&'*+-.^_`|~";

        boolean isValidProperty = MessageProperty.isValidAppProperty(name, value);

        assertTrue(isValidProperty);
    }

    @Test
    public void isValidAppPropertyReturnsFalseForUnusualAppPropertyKey()
    {
        final String name = "Test1234 Test1234";
        final String value = "test-value";

        boolean isValidProperty = MessageProperty.isValidAppProperty(name, value);

        assertFalse(isValidProperty);
    }

    @Test
    public void isValidAppPropertyReturnsFalseForUnusualAppPropertyValue()
    {
        final String name = "test-key";
        final String value = "\"someQuotedValue\"";

        boolean isValidProperty = MessageProperty.isValidAppProperty(name, value);

        assertFalse(isValidProperty);
    }
}
