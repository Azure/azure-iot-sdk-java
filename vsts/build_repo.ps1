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

# Set the Java home equal to the Java version under test. Currently, Azure Devops hosted agents only support Java 8, 11 and 17
# Since they are the current Java LTS versions
if (($env:JAVA_VERSION).equals("8"))
{
    $env:JAVA_HOME=$env:JAVA_HOME_8_X64

    if ($isPullRequestBuild.equals("true"))
    {
        Write-Host "Running just unit tests in Java 8"
        #mvn install -T 2C -DskipIntegrationTests=true
    }
    else
    {
        Write-Host "Running all tests in Java 8"
        #mvn -DRUN_PROVISIONING_TESTS="$Env:runProvisioningTests" -DRUN_DIGITAL_TESTS="$Env:runDigitalTwinTests" -DRUN_IOTHUB_TESTS="$Env:runIotHubTests" -DIS_PULL_REQUEST="$isPullRequestBuild" install -T 2C
    }
}
elseif (($env:JAVA_VERSION).equals("11"))
{
    Write-Host "Running all tests in Java 11"
    $env:JAVA_HOME=$env:JAVA_HOME_11_X64
    #mvn -DRUN_PROVISIONING_TESTS="$Env:runProvisioningTests" -DRUN_DIGITAL_TESTS="$Env:runDigitalTwinTests" -DRUN_IOTHUB_TESTS="$Env:runIotHubTests" -DIS_PULL_REQUEST="$isPullRequestBuild" install -T 2C -DskipUnitTests=true
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
