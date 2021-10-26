# For pull requests, this script checks the git diff between the merging branch and the upstream branch to see
#  if any code changes were made to iothub clients or provisioning clients. As a result, this script sets environment
#  variables to signal later vsts scripts to run or not to run certain e2e tests

# For all other builds (nightly builds, for instance), this script will set environment variables to run all e2e tests.

Write-Host "Determining tests to run..."
$Env:runIotHubTests = "false"
$Env:runProvisioningTests = "false"
$Env:runDigitalTwinTests = "false"