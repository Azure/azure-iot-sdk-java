**The Azure IoT Java SDK teams coding guidelines**

Following is a definition of a minimum set of rulers that defines the Java SDK coding style.

- [Programming Guidelines](#Programming-Guidelines)
- [Space and Braces](#Space-and-Braces)
- [Annotations](#Annotations)
- [Comment Style](#Comment-Style)
- [Naming Conventions](#Naming-Conventions)
- [JavaDoc Guidelines](#JavaDoc-Guidelines)

# Programming Guidelines
* Switch statements
    1. Must have default
    2. Must specify intended fall through as comment
    
    Eg : 
    ```java
    switch (a)
    {
        case FIRST:
            doCaseFirst();
            break;
        case SECOND:
            // Intended fall through
        case THIRD:    
            // Intended fall through
        default:
            doDefault();
    }
    ```

* When a reference to a static class member is made, it should be referenced with that class's name, not with a reference or expression of that class's type.
    ```java
    Foo foo = ...;
    Foo.someStaticMethod(); // good
    foo.someStaticMethod(); // bad
    somethingThatYieldsAFoo().someStaticMethod(); // very bad
    ```
* The square brackets form a part of the type, not the variable: String[] args, not String args[].

* Avoid dangling pointers by explicitly marking them null once you are done using them. This will let garbage collector pick it up.

* Class and member modifiers, when present, appear in the order recommended by the Java Language Specification.

# Space and Braces
* Tabs should be replaced by 4 spaces

* Space must be set 
    1. before parenthesis for - if, for, while, switch, try, catch, synchronized
    2. around operators
    3. before left braces
    4. before keywords like else, while, catch, finally
    5. after comma
    6. after semi colon
    7. after typecast

* Braces should always fall on the new line after class declaration, method declarion, if(), else, else if(), for, while, do, switch, try-with-resources, try, catch, finally.

* Code must always start on new line after `{`

* Maximum blank line spacing should be 1 everywhere in the code.

* Extra white space after { or } or after defining a class, member, enum, interface or ; is not allowed.

* Throws should be defined on the same line as method definition when possible
    
    Eg : 
    ```java
    public void methodDoesSomething() throws IOException
    {

    }
    ```

These guidelines can be achieved very easily by setting your editor with the above mentioned specifications. In order to make it easy to follow, the above guidelines are also mentioned in the order in which they occur in IntelliJ.

# Annotations
* Must be present on separate lines when not used in params.

Eg : 
```java
@Override
@Nullable
public String someMethod() 
{ 
    ... 
}
```

# Comment Style

```java
// This is Ok 
/*
**This is also ok
*/
```

# Naming Conventions
* Class names must be UpperCamelCase
* Method, parameters and local variables must be lowerCamelCase
* Constants should be capitalized eg CAPS_ALWAYS. They must always be static final fields   

    Eg :
```java
    static final long WAIT_IN_SECS = 100L;
``` 
* Package names must be lowercasenounderscores eg : For example, com.microsoft.packagename, not com.microsoft.packageName or com.microsoft.package_name
* Test classes always starts with name of class followed by "Test" Eg: ClassNameTest
* Test Methods starts with test followed by a camelCase description of test eg testGetLengthSucceeds, testGetLengthThrowsOnNullInput
* Requirement documents must always be named in lower case for class name followed by an underscore and requirements eg SomeClassName should have corresponding someclassname_requirements.md

# JavaDoc Guidelines
* Must start with description.
* Must have @params, @return, @throws tags in that order if contained in the code.

Eg :

```java
    /**
     * Short description here.
     *
     * @param param1 description for param1.
     * @param param2 description for param2. Can be {@code null}.
     *
     * @return descrption for return value
     *
     * @throws Exception1 reason for throwing exception.
     * @throws Exception2 reason for throwing exception.
     */
```

# Sample 
The following sample is generated from Intellij when set to follow the above mentioned guidelines.

```java
public class ThisIsASampleClass extends C1 implements I1, I2, I3, I4, I5
{
    private int f1 = 1;
    private String field2 = "";
    private static final long WAIT_TIME_IN_SECS = 10L;

    /**
     * Short description of foo1 here.
     *
     * @param param1 description for param1.
     * @param param2 description for param2. Can be {@code null}.
     *
     * @throws Exception1 Reason for throwing exception.
     * @throws Exception2 Reason for throwing exception.
     */
    @Override
    public void foo1(int i1, int i2, int i3, int i4, int i5, int i6, int i7)
    {
    }

    public static void longerMethod() throws Exception1, Exception2, Exception3
    {
        // todo something
        int i = 0;
        int[] a = new int[] {1, 2, 0x0052, 0x0053, 0x0054};
        int[] empty = new int[] {};
        int var1 = 1;
        int var2 = 2;
        foo1(0x0051, 0x0052, 0x0053, 0x0054, 0x0055, 0x0056, 0x0057);
        int x = (3 + 4 + 5 + 6) * (7 + 8 + 9 + 10) * (11 + 12 + 13 + 14 + 0xFFFFFFFF);
        String s1, s2, s3;
        s1 = s2 = s3 = "012345678901456";
        assert i + j + k + l + n + m <= 2 : "assert description";
        int y = 2 > 3 ? 7 + 8 + 9 : 11 + 12 + 13;
        super.getFoo().foo().getBar().bar();

        if (2 < 3) 
        {
            return;
        }
        else if (2 > 3)
        {
            return;
        } 
        else 
        {
            return;
        }
        
        for (int i = 0; i < 0xFFFFFF; i += 2)
        {
            System.out.println(i);
        }
       
        while (x < 50000) 
        {
            x++;
        }
       
        do 
        {
            x++;
        }
        while (x < 10000);

        switch (a)
        {
            case 0:
                doCase0();
                break;
            case 1:
                // Intended fall through
            case 2:    
                // Intended fall through
            default:
                doDefault();
        }

        try (MyResource r1 = getResource();
             MyResource r2 = null)
        {
            doSomething();
        }
        catch (Exception e)
        {
            processException(e);
        }
        finally
        {
            processFinally();
        }

        try (MyResource r1 = getResource();
             MyResource r2 = null)
        {
            doSomething();
        }

        Runnable r = () ->
        {
        };
    }   
}

public class ThisIsASampleClassTest()
{
    public void testLongerMethodSucceeds()
    {
        .....
    }

    public void testFoo1Succeeds()
    {
        .....
    }
}
```
