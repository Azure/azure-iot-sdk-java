# Copyright (c) Microsoft. All rights reserved.
# Licensed under the MIT license. See LICENSE file in the project root for full license information.

# This script generates javadocs for the currently checked out branch and creates a new git branch based off of the current
# gh-pages branch. This new branch will contain the generated javadocs. After running this script, you will manually
# need to create a pull request from the new branch ("gh-pages-<current date>") to "gh-pages".

# This script is designed to be used only by an Azure Devops release pipeline. Running it locally will not work
# due to some assumptions this script makes about working directory.

function TestLastExitCode {

    if ($LASTEXITCODE -ne 0) {
        throw "Last exit code is ($LASTEXITCODE)"
    }
}

function CreateJavadocReleaseBranch(
    $GitHubName,
    $GitHubEmail,
    $Sources,
    $FolderName,
    $UpdateDeviceClientDocs,
    $UpdateServiceClientDocs,    
    $UpdateProvisioningDeviceClientDocs,
    $UpdateProvisioningServiceClientDocs,    
    $UpdateTpmProviderEmulatorDocs,
    $UpdateTpmProviderDocs,
    $UpdateDiceProviderEmulatorDocs,
    $UpdateDiceProviderDocs,
    $UpdateSecurityProviderDocs,
    $UpdateX509ProviderDocs) {

    if ($([string]::IsNullOrWhiteSpace($GitHubName) -eq $true)) {
        throw "GitHubName is null or empty"
    }

    if ($([string]::IsNullOrWhiteSpace($GitHubEmail) -eq $true)) {
        throw "GitHubEmail is null or empty"
    }

    Set-Location $Sources -ErrorAction Stop

    #Some local samples rely on packages we don't publish publicly, so install everything locally before generating javadocs
    Write-Host "Installing jars locally"
    mvn `-Dmaven.javadoc.skip=true install `-DskipIntegrationTests `DskipUnitTests -T 2C

    Write-Host "Generating javadocs, placing them in apidocs folder at the root of the repository"
    mvn javadoc:javadoc -T 2C

    $date = Get-Date -UFormat '%Y-%m-%d'
    $newBranchName = "gh-pages-" + $date

    git.exe config user.email $GitHubEmail
    git.exe config user.name $GitHubName

    Write-Output "Create new branch '$newBranchName' based on gh-pages branch"
    git.exe checkout gh-pages

    # Create a new gh-pages merge branch based off of the original gh-pages branch
    git.exe checkout -b $newBranchName gh-pages
    TestLastExitCode  # stop if we can't create the branch

    Write-Host "Copying generated javadocs to replace current javadocs"
    Set-Location apidocs 

    if ($UpdateDeviceClientDocs -eq "True")
    {
        Remove-Item ..\$FolderName\device -Force -Recurse
        New-item -Path ..\$FolderName\device -ItemType Directory
        Copy-Item -Force -Path .\device\* -Destination ..\$FolderName\device
        Copy-Item -Recurse -Force -Path .\device\com -Destination ..\$FolderName\device
    }

    if ($UpdateServiceClientDocs -eq "True")
    {
        Remove-Item ..\$FolderName\service -Force -Recurse
        New-item -Path ..\$FolderName\service -ItemType Directory
        Copy-Item -Force -Path .\service\* -Destination ..\$FolderName\service
        Copy-Item -Recurse -Force -Path .\service\com -Destination ..\$FolderName\service
    }

    if ($UpdateProvisioningDeviceClientDocs -eq "True")
    {
        Remove-Item ..\$FolderName\provisioning\provisioning-device-client -Force -Recurse 
        New-Item -Path ..\$FolderName\provisioning\provisioning-device-client -ItemType Directory   
        Copy-Item -Force -Path .\provisioning\provisioning-device-client\* -Destination ..\$FolderName\provisioning\provisioning-device-client
        Copy-Item -Recurse -Force -Path .\provisioning\provisioning-device-client\com -Destination ..\$FolderName\provisioning\provisioning-device-client   
    }

    if ($UpdateProvisioningServiceClientDocs -eq "True")
    {
        Remove-Item ..\$FolderName\provisioning\provisioning-service-client -Force -Recurse
        New-Item -Path ..\$FolderName\provisioning\provisioning-service-client -ItemType Directory
        Copy-Item -Force -Path .\provisioning\provisioning-service-client\* -Destination ..\$FolderName\provisioning\provisioning-service-client
        Copy-Item -Recurse -Force -Path .\provisioning\provisioning-service-client\com -Destination ..\$FolderName\provisioning\provisioning-service-client
    }

    if ($UpdateTpmProviderEmulatorDocs -eq "True")
    {
        Remove-Item ..\$FolderName\provisioning\security\tpm-provider-emulator -Force -Recurse 
        New-Item -Path ..\$FolderName\provisioning\security\tpm-provider-emulator -ItemType Directory
        Copy-Item -Force -Path .\provisioning\security\tpm-provider-emulator\* -Destination ..\$FolderName\provisioning\security\tpm-provider-emulator
        Copy-Item -Recurse -Force -Path .\provisioning\security\tpm-provider-emulator\com -Destination ..\$FolderName\provisioning\security\tpm-provider-emulator
    }

    if ($UpdateTpmProviderDocs -eq "True")
    {
        Remove-Item ..\$FolderName\provisioning\security\tpm-provider -Force -Recurse
        New-Item -Path ..\$FolderName\provisioning\security\tpm-provider -ItemType Directory
        Copy-Item -Force -Path .\provisioning\security\tpm-provider\* -Destination ..\$FolderName\provisioning\security\tpm-provider
        Copy-Item -Recurse -Force -Path .\provisioning\security\tpm-provider\com -Destination ..\$FolderName\provisioning\security\tpm-provider
    }

    if ($UpdateDiceProviderEmulatorDocs -eq "True")
    {
        Remove-Item ..\$FolderName\provisioning\security\dice-provider-emulator -Force -Recurse
        New-Item -Path ..\$FolderName\provisioning\security\dice-provider-emulator -ItemType Directory
        Copy-Item -Force -Path .\provisioning\security\dice-provider-emulator\* -Destination ..\$FolderName\provisioning\security\dice-provider-emulator
        Copy-Item -Recurse -Force -Path .\provisioning\security\dice-provider-emulator\com -Destination ..\$FolderName\provisioning\security\dice-provider-emulator
    }

    if ($UpdateDiceProviderDocs -eq "True")
    {
        Remove-Item ..\$FolderName\provisioning\security\dice-provider -Force -Recurse
        New-Item -Path ..\$FolderName\provisioning\security\dice-provider -ItemType Directory
        Copy-Item -Force -Path .\provisioning\security\dice-provider\* -Destination ..\$FolderName\provisioning\security\dice-provider
        Copy-Item -Recurse -Force -Path .\provisioning\security\dice-provider\com -Destination ..\$FolderName\provisioning\security\dice-provider
    }

    if ($UpdateSecurityProviderDocs -eq "True")
    {
        Remove-Item ..\$FolderName\provisioning\security\security-provider -Force -Recurse
        New-Item -Path ..\$FolderName\provisioning\security\security-provider -ItemType Directory
        Copy-Item -Force -Path .\provisioning\security\security-provider\* -Destination ..\$FolderName\provisioning\security\security-provider
        Copy-Item -Recurse -Force -Path .\provisioning\security\security-provider\com -Destination ..\$FolderName\provisioning\security\security-provider
    }

    if ($UpdateX509ProviderDocs -eq "True")
    {
        Remove-Item ..\$FolderName\provisioning\security\x509-provider -Force -Recurse
        New-Item -Path ..\$FolderName\provisioning\security\x509-provider -ItemType Directory
        Copy-Item -Force -Path .\provisioning\security\x509-provider\* -Destination ..\$FolderName\provisioning\security\x509-provider
        Copy-Item -Recurse -Force -Path .\provisioning\security\x509-provider\com -Destination ..\$FolderName\provisioning\security\x509-provider
    }

    Remove-Item ..\device -Force -Recurse
    Remove-Item ..\service -Force -Recurse
    Remove-Item ..\provisioning -Force -Recurse

    Set-Location ..

    Write-Host "Removing temporary apidocs folder"
    Remove-Item ./apidocs -Recurse

    git.exe add .
    TestLastExitCode  # stop if we can't add the changes

    $commitMessage = "release($date): Update javadocs to latest"
    Write-Host "Committing to new javadocs release branch"
    git.exe commit -m $commitMessage
    TestLastExitCode  # stop if we can't commit the changes

    Write-Output "Push changes to origin"
    git.exe push --tags -u origin $newBranchName
    TestLastExitCode  # stop if push to remote fails
}
