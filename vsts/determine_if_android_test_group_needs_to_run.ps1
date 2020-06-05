# This script determines, for the provided android test group, if any tests need to be run

#Sets env vars for
./vsts/determine_tests_to_run.ps1

Write-Host "##vso[task.setvariable variable=task.android.needToRunTestGroup]no"

$targetBranch = ($env:TARGET_BRANCH)
if (!($env:TARGET_BRANCH).toLower().Contains("system.pullrequest.targetbranch"))
{
    if (($env:IS_BASIC_TIER_HUB))
    {
        #Nightly builds cover this scenario, since it has so little coverage
        Write-Host "This build is a pull request build and is for basic tier hub, so all android tests will be skipped"
        Write-Host "##vso[task.setvariable variable=task.android.needToRunTestGroup]no"
        exit 0
    }
}

# "TestGroup12", for instance
$testGroupString = $env:TEST_GROUP_ID

$testGroupImportPattern = $testGroupString + ";"
$testGroupAnnotationPattern = "@" + $testGroupString

$testGroupContainsIotHubTests = "false"
$testGroupContainsProvisioningTests = "false"

# List of files that import the TestGroupX
$testRunnerFilePaths = @()

# List of files that import TestGroupX and contain "@TestGroupX"
# Files have to be cross-referenced like this because "@TestGroup1" shows up in "@TestGroup12" and "@TestGroup13", but the same doesn't happen in import statements
# Import statements alone cannot be used because there may files with import statements for a test group that don't actually use it,
# so we have to confirm the file in question has both the import statement, and the test group annotation in use
$confirmedTestRunnerFilePaths = @()

$paths = & Get-ChildItem -recurse | Select-String -pattern $testGroupImportPattern | group path | select name
Write-Host "Searching for instances of " $testGroupImportPattern
ForEach ($line in $($paths -split "`r`n"))
{
	# ignore dex, class, etc files.
	if ($line.toLower().Contains("androidrunner.java"))
	{
		if ($line.toLower().Contains("dummyandroidrunner.java"))
		{
			# Ignore this file, it has no bearing on which tests should be run
		}
		else
		{
			$testRunnerFilePaths += $line
		}
	}
}

Write-Host "Files that import this test group:"
Write-Host $testRunnerFilePaths

$paths = & Get-ChildItem -recurse | Select-String -pattern $testGroupAnnotationPattern | group path | select name
Write-Host "Searching for instances of " $testGroupAnnotationPattern
ForEach ($line in $($paths -split "`r`n"))
{
	# ignore dex, class, etc files.
	if ($line.toLower().Contains("androidrunner.java"))
	{
		if ($testRunnerFilePaths.Contains($line))
		{
			if ($line.toLower().Contains("dummyandroidrunner.java"))
			{
				# Ignore this file, it has no bearing on which tests should be run
			}
			else
			{
				$confirmedTestRunnerFilePaths += $line

				if ($line.toLower().Contains("iothub") -and ($Env:runIotHubTests -eq "true"))
				{
				    Write-Host "This test group includes iot hub tests, and iot hub tests have to be run, so this test group will run"
				    Write-Host "##vso[task.setvariable variable=task.android.needToRunTestGroup]yes"

                    # No more need to process results, the test runner definitely needs to run
                    exit 0
				}

				if ($line.toLower().Contains("provisioning") -and ($Env:runProvisioningTests -eq "true"))
				{
				    Write-Host "This test group includes provisioning tests, and provisioning tests have to be run, so this test group will run"
				    Write-Host "##vso[task.setvariable variable=task.android.needToRunTestGroup]yes"

                    # No more need to process results, the test runner definitely needs to run
                    exit 0
				}
			}
		}
	}
}

Write-Host "This test runner has no need to run"