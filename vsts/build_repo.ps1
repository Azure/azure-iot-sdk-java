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

if ($env:JAVA_VERSION == 8)
{
    mvn -DRUN_PROVISIONING_TESTS="$Env:runProvisioningTests" -DRUN_DIGITAL_TESTS="$Env:runDigitalTwinTests" -DRUN_IOTHUB_TESTS="$Env:runIotHubTests" -DIS_PULL_REQUEST="$isPullRequestBuild" install -T 2C
}
elseif ($env:JAVA_VERSION == 11)
{
    mvn install -DskipTests -T 2C

    cd "azure-iot-sdk-java/iot-e2e-tests"

    $env:JAVA_HOME=$env:JAVA_HOME_11_X64

    mvn -DRUN_PROVISIONING_TESTS="$Env:runProvisioningTests" -DRUN_DIGITAL_TESTS="$Env:runDigitalTwinTests" -DRUN_IOTHUB_TESTS="$Env:runIotHubTests" -DIS_PULL_REQUEST="$isPullRequestBuild" install -T 2C
}
else
{
    Write-Host "Unrecognized or unsupported JDK version: " $env:JAVA_VERSION
    exit -1
}