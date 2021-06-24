**The Azure IoT Java SDK teams coding guidelines**

Following is a definition of a minimum set of rulers that defines the Java SDK coding style.

- [Programming Guidelines](#Programming-Guidelines)
- [Space and Braces](#Space-and-Braces)
- [Annotations](#Annotations)
- [Comment Style](#Comment-Style)
- [Naming Conventions](#Naming-Conventions)
- [JavaDoc Guidelines](#JavaDoc-Guidelines)
- [Commit Guidelines](#Commit-Guidelines)

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

# .editorconfig
We have included an editor config to allow sharing of the code styles. This should be picked up by IntelliJ automatically. You will see a notification in File > Settings > Editor > Code Styles > Java that specifies the settings are being overridden by the config.

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
    private final int f1 = 1;
    private final String field2 = "";
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

# Commit Guidelines
We have very precise rules over how our git commit messages can be formatted. This leads to more readable messages that are easy to follow when looking through the project history.

**Commit Message Format**

Each commit message consists of a header, a body (optional) and a footer (optional). The header has a special format that includes a type, a scope and a subject:

```java

<type>(<scope>): <subject>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>

```
The header is mandatory and the body and footer are optional.

Any line of the commit message cannot be longer 100 characters! This allows the message to be easier to read on GitHub as well as in various git tools.

Footer should contain a [closing reference](https://help.github.com/articles/closing-issues-using-keywords/) to an issue if any.

**Revert**

If the commit reverts a previous commit, it should begin with revert:, followed by the header of the reverted commit. In the body it should say: This reverts commit <hash>., where the hash is the SHA of the commit being reverted.

**Rebase and Squash**

* Its manadatory to squash all your commits per scope (i.e package). It is also important to rebase your commits on master.
* Optionally you can split your commits on the basis of the package you are providing code to.

**Type**

Must be one of the following:

* build: Changes that affect the build system (eg POM changes)
* docs: Documentation only changes (eg ReadMe)
* feat: A new feature
* fix: A bug fix
* perf: A code change that improves performance
* refactor: A code change that neither fixes a bug nor adds a feature

Optionally you could also use the following scope:

* style: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
* test: Adding missing tests or correcting existing tests

**Scope**

The scope should be the name of the maven package affected as perceived by person reading changelog generated from commit messages.

The following is the list of supported scopes:

* iot-dev
* iot-service
* deps
* prov-dev
* prov-service
* sec-provider
* tpm-provider
* tpm-emulator
* x509-provider
* dice-emulator
* dice-provider
* samples

**Subject**

The subject contains succinct description of the change:

* use the imperative, present tense: "change" not "changed" nor "changes"
* no dot (.) at the end

**Body**

Just as in the subject, use the imperative, present tense: 

"change" not "changed" nor "changes". The body should include the motivation for the change and contrast this with previous behavior.

**Footer**

The footer should contain any information about Breaking Changes and is also the place to reference GitHub issues that this commit Closes.

Breaking Changes should start with the word BREAKING CHANGE: with a space or two newlines. The rest of the commit message is then used for this.

***Example commit messages***

Good commit messages look like below:

* fix(iot-dev, deps): Fix failure in reconnection

    Fix failure in MQTT reconnection when token expires.

    Github issue (fix#123)
* feat(iot-service): Add continuation token to Query
* docs: Update readme to reflect provisiong client

Bad commit messages look like below:

* fix(iot-dev): small fix

    I was trying to reconnect and network dropped. Fixing such random failures

* feat(iot-service): add test
* docs: update readme 

References for commit guidelines 
* https://udacity.github.io/git-styleguide/
* https://github.com/angular/angular/blob/master/CONTRIBUTING.md#-commit-message-guidelines
* https://github.com/googlesamples/android-architecture/issues/300
