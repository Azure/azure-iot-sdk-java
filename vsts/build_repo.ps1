#./vsts/determine_tests_to_run.ps1

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

mvn -DRUN_PROVISIONING_TESTS="true" -DRUN_DIGITAL_TESTS="true" -DRUN_IOTHUB_TESTS="true" -DIS_PULL_REQUEST="$isPullRequestBuild" install -T 2C