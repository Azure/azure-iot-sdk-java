// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.Template;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/*
    1. The goal of the test is to provide as much as class, method and line coverage as possible. Our SDK
       is currently targeting above 95% coverage.
    2. Every requirement specified in the template_requirement.md should have a test case associated with it.
    3. We follow the AAA principle of testing i.e Arrange, Act and Assert. [http://wiki.c2.com/?ArrangeActAssert]
*/

// Unit tests for Template.
public class TemplateTest
{
    // Mock global objects which you do not want to test for this class
    /*
        Some common objects to be considered for mocking :
        1. Usually you mock objects that are input or output to the class under test
        2. You can also mock objects belonging to JDK which you do not want to test
        3. You can mock objects to control the expected behavior for the method under test
     */

    // Tests_SRS_TEMPLATE_99_001: [The constructor shall save the input parameters.]
    // Tests_SRS_TEMPLATE_99_003: [The constructor shall create a new instance of the public and private objects.]
    @Test                           /* Mock objects specific for this test */
    public void testConstructor(final @Mocked Set<String> mockedSet)
    {
        //arrange
        final String testString = "testString";

        /* Set any expectations on mocked object */
        new NonStrictExpectations()
        {
            {

            }
        };

        //act
        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        Template testObject = Deencapsulation.newInstance(Template.class, testString);

        //assert

        /* Verify that constructor initializes */
        assertNotNull(testObject.templateTestPublic);

        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        final String actualString = Deencapsulation.getField(testObject, "templateTestPrivate");

        /* Verify that constructor sets as expected */
        assertEquals(actualString, testString);

        /* Verify any call flow on mocked objects that is expected on act */
        new Verifications()
        {
            {

            }
        };
    }

    // Tests_SRS_TEMPLATE_99_002: [If the input parameter is null, the constructor shall throw an IllegalArgumentException.]
            /* Verify if the method throws expected exception */
    @Test (expected = IllegalArgumentException.class)
    public void testConstructorThrowsOnNullArguments()
    {
        //act
        Template testObject = Deencapsulation.newInstance(Template.class, String.class);
    }

    // Tests_SRS_TEMPLATE_99_004: [The method shall create a new instance of the unionSet .]
    @Test
    public void testOpenCreatesNewSet(final @Mocked Set<String> mockedSet)
    {
        //arrange
        final String testString = "testString";

        /* Set any expectations on mocked object */
        new NonStrictExpectations()
        {
            {

            }
        };

        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        Template testObject = Deencapsulation.newInstance(Template.class, testString);

        //act
        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        Deencapsulation.invoke(testObject, "open");

        //assert

        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        final Set actualSet = Deencapsulation.getField(testObject, "unionSet");

        assertNotNull(actualSet);
        assertEquals(actualSet, mockedSet);

        /* Verify any call flow on mocked objects that is expected on act */
        new Verifications()
        {
            {
                new HashSet<>();
            }
        };
    }

    // Tests_SRS_TEMPLATE_99_005: [If open is already called then this method shall do nothing and return.]
    @Test
    public void testOpenCreatesNewSetOnlyOnce(final @Mocked Set<String> mockedSet)
    {
        //arrange
        final String testString = "testString";

        /* Set any expectations on mocked object */
        new NonStrictExpectations()
        {
            {

            }
        };

        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        Template testObject = Deencapsulation.newInstance(Template.class, testString);

        //act
        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        Deencapsulation.invoke(testObject, "open");
        Deencapsulation.invoke(testObject, "open");

        //assert

        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        final Set actualSet = Deencapsulation.getField(testObject, "unionSet");

        assertNotNull(actualSet);
        assertEquals(actualSet, mockedSet);

        /* Verify any call flow on mocked objects that is expected on act */
        new Verifications()
        {
            {
                new HashSet<>();
            }
        };
    }

    // Tests_SRS_TEMPLATE_99_006: [This method shall clear the unionSet and set all the members ready for garbage collection.]
    @Test
    public void testCloseDestroysSet(final @Mocked Set<String> mockedSet)
    {
        //arrange
        final String testString = "testString";

        /* Set any expectations on mocked object */
        new NonStrictExpectations()
        {
            {

            }
        };

        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        Template testObject = Deencapsulation.newInstance(Template.class, testString);

        /*Use Dencapsulation to control the expected value of private fields */
        Deencapsulation.setField(testObject, "unionSet", mockedSet);

        //act
        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        Deencapsulation.invoke(testObject, "close");

        //assert

        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        final Set actualSet = Deencapsulation.getField(testObject, "unionSet");

        /* Verify that close indeed set the value to null */
        assertNull(actualSet);

        /* Verify any call flow on mocked objects that is expected on close */
        new Verifications()
        {
            {
                mockedSet.clear();
                times = 1;
            }
        };
    }

    // Tests_SRS_TEMPLATE_99_007: [If close is already called then this method shall do nothing and return.]
    @Test
    public void testCloseWhenCalledTwiceReturnsSuccessfully(final @Mocked Set<String> mockedSet)
    {
        //arrange

        final String testString = "testString";

        /* Set any expectations on mocked object */
        new NonStrictExpectations()
        {
            {

            }
        };

        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        Template testObject = Deencapsulation.newInstance(Template.class, testString);

        /*Use Dencapsulation to control the expected value of private fields */
        Deencapsulation.setField(testObject, "unionSet", mockedSet);

        //act

        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        Deencapsulation.invoke(testObject, "close");
        Deencapsulation.invoke(testObject, "close");

        //assert

        /* Verify any call flow and number of times the call is expected on mocked objects close is invoked */
        new Verifications()
        {
            {
                mockedSet.clear();
                times = 1;
            }
        };
    }

    // Tests_SRS_TEMPLATE_99_009: [The method shall return the current instance of the union set.]
    @Test
    public void testGetUnionSetGets(final @Mocked Set<String> mockedSet)
    {
        //arrange
        final String testString = "testString";

        /* Set any expectations on mocked object */
        new NonStrictExpectations()
        {
            {

            }
        };

        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        Template testObject = Deencapsulation.newInstance(Template.class, testString);
        Deencapsulation.invoke(testObject, "open");

        //act
        final Set<String> actualSet = (Set<String>) testObject.getUnionSet();

        //assert

        /* Verify that getter gets as expected */
        assertEquals(actualSet, mockedSet);

        /* Verify any call flow on mocked objects that is expected on act */
        new Verifications()
        {
            {

            }
        };

    }

    // Tests_SRS_TEMPLATE_99_008: [The method shall return the private member object.]
    @Test
    public void testGetTemplateTestPrivateGets()
    {
        //arrange
        final String testString = "testString";

        /* Set any expectations on mocked object */
        new NonStrictExpectations()
        {
            {

            }
        };

        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        Template testObject = Deencapsulation.newInstance(Template.class, testString);

        //act
        final String actualString = (String) testObject.getTemplateTestPrivate();

        //assert

        /* Verify that getter gets as expected */
        assertEquals(actualString, testString);

        /* Verify any call flow on mocked objects that is expected on act */
        new Verifications()
        {
            {

            }
        };

    }

    // Tests_SRS_TEMPLATE_99_010: [The method shall add the collection to the union set .]
    @Test
    public void testAddToSetSucceeds()
    {
        //arrange
        final String testString = "testString";
        final Set<String> newSet = new HashSet<>();
        newSet.add("test1");
        newSet.add("test2");

        /* Set any expectations on mocked object */
        new NonStrictExpectations()
        {
            {

            }
        };


        /* Put the class in expected state before testing it */
        Template testObject = Deencapsulation.newInstance(Template.class, testString);
        Deencapsulation.invoke(testObject, "open");

        //act
        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        testObject.addToSet(newSet);

        //assert

        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        final Set actualSet = Deencapsulation.getField(testObject, "unionSet");

        assertNotNull(actualSet);
        assertEquals(2, actualSet.size());

        /* Verify any call flow on mocked objects that is expected on act */
        new Verifications()
        {
            {

            }
        };
    }

    // Tests_SRS_TEMPLATE_99_011: [The method shall throw IllegalArgumentException if the collection to be added was either empty or null .]
    @Test (expected = IllegalArgumentException.class)
    public void testAddToSetThrowsIfNullInputSet()
    {
        //arrange
        final String testString = "testString";
        final Set<String> newSet = null;

        /* Set any expectations on mocked object */
        new NonStrictExpectations()
        {
            {

            }
        };

        /* Put the class in expected state before testing it */
        Template testObject = Deencapsulation.newInstance(Template.class, testString);
        Deencapsulation.invoke(testObject, "open");

        //act
        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        testObject.addToSet(newSet);

        //assert
        /* Throws exception */
    }

    // Tests_SRS_TEMPLATE_99_011: [The method shall throw IllegalArgumentException if the collection to be added was either empty or null .]
    @Test (expected = IllegalArgumentException.class)
    public void testAddToSetThrowsIfEmptyInputSet(final @Mocked Set<String> mockedSet)
    {
        //arrange
        final String testString = "testString";

        /* Set any expectations on mocked object */
        new NonStrictExpectations()
        {
            {
                mockedSet.size();
                result = 0;

            }
        };

        /* Put the class in expected state before testing it */
        Template testObject = Deencapsulation.newInstance(Template.class, testString);
        Deencapsulation.invoke(testObject, "open");

        //act
        testObject.addToSet(mockedSet);

        //assert
        /* Throws exception */
    }

    // Tests_SRS_TEMPLATE_99_012: [The method shall throw IllegalStateException if it is called before calling open. ]
    @Test (expected = IllegalArgumentException.class)
    public void testAddToSetThrowsIfCalledWithoutOpen()
    {
        //arrange
        final String testString = "testString";
        final Set<String> newSet = new HashSet<>();

        /* Set any expectations on mocked object */
        new NonStrictExpectations()
        {
            {

            }
        };

        /* Put the class in expected state before testing it */
        Template testObject = Deencapsulation.newInstance(Template.class, testString);

        //act
        /* Use Deencapsulation or reflection to access objects that are not scoped for test */
        testObject.addToSet(newSet);

        //assert
        /* Throws exception */
    }

}
