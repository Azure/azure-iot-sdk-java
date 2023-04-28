if (($env:JAVA_VERSION).equals("8"))
{
    $env:JAVA_HOME=$env:JAVA_HOME_8_X64
}
elseif (($env:JAVA_VERSION).equals("11"))
{
    $env:JAVA_HOME=$env:JAVA_HOME_11_X64
}

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

cd ./iot-e2e-tests/android
ls
gradle wrapper --gradle-version 7.3.1 --warning-mode all --stacktrace --scan
gradlew :clean :app:clean :app:assembleDebug

gradlew :app:assembleDebugAndroidTest -PIOTHUB_CONNECTION_STRING=$env:IOTHUB_CONNECTION_STRING -PIOT_DPS_CONNECTION_STRING=$env:IOT_DPS_CONNECTION_STRING -PIOT_DPS_ID_SCOPE=$env:DEVICE_PROVISIONING_SERVICE_ID_SCOPE -PDPS_GLOBALDEVICEENDPOINT_INVALIDCERT=$env:INVALID_DEVICE_PROVISIONING_SERVICE_GLOBAL_ENDPOINT -PPROVISIONING_CONNECTION_STRING_INVALIDCERT=$env:INVALID_DEVICE_PROVISIONING_SERVICE_CONNECTION_STRING -PIS_BASIC_TIER_HUB=$env:IS_BASIC_TIER_HUB -PIS_PULL_REQUEST=$env:isPullRequestBuild -PRECYCLE_TEST_IDENTITIES=true