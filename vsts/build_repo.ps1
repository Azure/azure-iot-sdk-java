./vsts/determine_tests_to_run.ps1

$targetBranch = ($env:TARGET_BRANCH)
$isPullRequestBuild = "true";
if (($env:TARGET_BRANCH).toLower().Contains("system.pullrequest.targetbranch"))
{
    Write-Host "Not a pull request build, will run all tests"
    $isPullRequestBuild = "false";
}
else
{
    Write-Host "Pull request build detected"
}

# This repo can only run unit tests when using Java 8 currently due to outdated unit test code packages. Because of that,
# we can only run e2e tests for Java versions other than Java 8.
if (!($env:JAVA_VERSION).equals("8"))
{
    #Build the repo with Java 8 as configured in the project itself
    mvn install -DskipTests -T 2C

    # go into the e2e tests folder so the next mvn install only runs the e2e tests instead of running e2e tests + unit tests.
    cd "./iot-e2e-tests"
}

# Set the Java home equal to the Java version under test. Currently, Azure Devops hosted agents only support Java 8, 11 and 17 currently
# Since they are the current Java LTS versions
if (($env:JAVA_VERSION).equals("8"))
{
    $env:JAVA_HOME=$env:JAVA_HOME_8_X64
}
elseif (($env:JAVA_VERSION).equals("11"))
{
    $env:JAVA_HOME=$env:JAVA_HOME_11_X64
}
# Leaving this commented out to make it easy to add Java 17 support later
#elseif (($env:JAVA_VERSION).equals("17"))
#{
#    $env:JAVA_HOME=$env:JAVA_HOME_17_X64
#}
else
{
    Write-Host "Unrecognized or unsupported JDK version: " $env:JAVA_VERSION
    exit -1
}

# Run the e2e tests with the Java version under test
mvn -DRUN_PROVISIONING_TESTS="$Env:runProvisioningTests" -DRUN_DIGITAL_TESTS="$Env:runDigitalTwinTests" -DRUN_IOTHUB_TESTS="$Env:runIotHubTests" -DIS_PULL_REQUEST="$isPullRequestBuild" install -T 2C

