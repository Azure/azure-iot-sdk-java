# For pull requests, this script checks the git diff between the merging branch and the upstream branch to see
#  if any code changes were made to iothub clients or provisioning clients. As a result, this script sets environment
#  variables to signal later vsts scripts to run or not to run certain e2e tests

# For all other builds (nightly builds, for instance), this script will set environment variables to run all e2e tests.

Write-Host "Determining tests to run..."
$Env:runIotHubTests = "false"
$Env:runProvisioningTests = "false"

$targetBranch = ($env:TARGET_BRANCH)
if (($env:TARGET_BRANCH).toLower().Contains("system.pullrequest.targetbranch"))
{
    Write-Host "Assuming this build is not a pull request build, running all tests"

    $Env:runIotHubTests = "true"
    $Env:runProvisioningTests = "true"

    exit 0
}

$GitDiff = & git diff origin/$targetBranch --name-only
ForEach ($line in $($GitDiff -split "`r`n"))
{
	if ($line.EndsWith(".md", "CurrentCultureIgnoreCase") -or $line.EndsWith(".png", "CurrentCultureIgnoreCase"))
	{
        # These file types are ignored when determining if source code changes require running e2e tests
	}
	elseif ($line.toLower().Contains("sample"))
	{
	    # Sample changes don't necessitate running e2e tests
    }
	else
	{
	    # If code changes were made to vsts pipeline
		if ($line.toLower().Contains("vsts"))
		{
            $Env:runIotHubTests = "true"
            $Env:runProvisioningTests = "true"
		}

	    # If code changes were made to provisioning sdk code or to provisioning e2e tests
		if ($line.toLower().Contains("provisioning"))
		{
			$Env:runProvisioningTests = "true"
		}

        # If code changes were made to iot hub e2e tests
		if ($line.toLower().Contains("iothub"))
		{
            $Env:runIotHubTests = "true"
            $Env:runProvisioningTests = "true"
		}

		# If code changes were made to device client
		if ($line.toLower().Contains("iot-device-client/src/main") -or $line.toLower().Contains("iot-device-client/pom.xml"))
		{
		    $Env:runIotHubTests = "true"
		    $Env:runProvisioningTests = "true"
		}

        # If code changes were made to service client
        if ($line.toLower().Contains("iot-service-client/src/main") -or $line.toLower().Contains("iot-service-client/pom.xml"))
        {
            $Env:runIotHubTests = "true"
            $Env:runProvisioningTests = "true"
        }

        # Both provisioning and iot hub depend on deps package
		if ($line.toLower().Contains("deps/src/main") -or $line.toLower().Contains("deps/pom.xml"))
		{
			$Env:runIotHubTests = "true"
			$Env:runProvisioningTests = "true"
		}

        # E2E common code can be used in any test, so we must run every test
		if ($line.toLower().Contains("iot-e2e-tests/common/src/test/java/tests/integration/com/microsoft/azure/sdk/iot/helpers"))
		{
            $Env:runIotHubTests = "true"
            $Env:runProvisioningTests = "true"
        }

        # Android helpers can be used in any test, so we must run every test
		if ($line.toLower().Contains("android/helper"))
        {
            $Env:runIotHubTests = "true"
            $Env:runProvisioningTests = "true"
        }
	}
}

if ($Env:runIotHubTests -eq "true")
{
    Write-Host "Will run iot hub tests"
}
else
{
    Write-Host "Will not run iot hub tests"
}

if ($Env:runProvisioningTests -eq "true")
{
    Write-Host "Will run provisioning tests"
}
else
{
    Write-Host "Will not run provisioning tests"
}

