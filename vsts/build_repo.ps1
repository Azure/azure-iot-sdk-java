./vsts/determine_tests_to_run.ps1

$targetBranch = ($env:TARGET_BRANCH)
$isPullRequestBuild = "true";
if (($env:TARGET_BRANCH).toLower().Contains("system.pullrequest.targetbranch"))
{
    $isPullRequestBuild = "false";
}

mvn -DRUN_PROVISIONING_TESTS="$Env:runProvisioningTests" -DRUN_IOTHUB_TESTS="$Env:runIotHubTests" -DIS_PULL_REQUEST="$isPullRequestBuild" install -DskipITs=false -T 2C `-Dmaven.javadoc.skip
