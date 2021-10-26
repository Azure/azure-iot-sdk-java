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

mvn -pl :provisioning-device-client,:provisioning-service-client,:iot-deps,:iot-service-client,:iot-device-client -D"maven.test.skip=true" -D"maven.javadoc.skip=true" install -T 2C