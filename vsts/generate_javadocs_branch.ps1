# Copyright (c) Microsoft. All rights reserved.
# Licensed under the MIT license. See LICENSE file in the project root for full license information.

# This script generates javadocs for the currently checked out branch and creates a new git branch based off of the current
# gh-pages branch. This new branch will contain the generated javadocs. After running this script, you will manually
# need to create a pull request from the new branch ("gh-pages-<current date>") to "gh-pages".

# This script is designed to be used only by an Azure Devops release pipeline. Running it locally will not work
# due to some assumptions this script makes about working directory.

# Folder to place the generated content. 
# Note: This is an input to the script and will be different for master and preview branches.
[Parameter(Mandatory=$true)]
[string] $folderName

function TestLastExitCode {

    if ($LASTEXITCODE -ne 0) {
        throw "Last exit code is ($LASTEXITCODE)"
    }
}

function CreateJavadocReleaseBranch($GitHubName, $GitHubEmail, $Sources) {
    if ($([string]::IsNullOrWhiteSpace($GitHubName) -eq $true)) {
        throw "GitHubName is null or empty"
    }

    if ($([string]::IsNullOrWhiteSpace($GitHubEmail) -eq $true)) {
        throw "GitHubEmail is null or empty"
    }

    Set-Location $Sources -ErrorAction Stop

    #Some local samples rely on packages we don't publish publicly, so install everything locally before generating javadocs
    Write-Host "Installing jars locally"
    mvn `-DskipTests `-Dmaven.javadoc.skip=true install -T 2C

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
    Remove-Item ..\$folderName\deps -Force -Recurse
    Copy-Item -Force -Path .\deps\* -Destination ..\$folderName\deps
    Copy-Item -Recurse -Force -Path .\deps\com -Destination ..\$folderName\deps

    Remove-Item ..\$folderName\device -Force -Recurse
    Copy-Item -Force -Path .\device\* -Destination ..\$folderName\device
    Copy-Item -Recurse -Force -Path .\device\com -Destination ..\$folderName\device

    Remove-Item ..\$folderName\service -Force -Recurse
    Copy-Item -Force -Path .\service\* -Destination ..\$folderName\service
    Copy-Item -Recurse -Force -Path .\service\com -Destination ..\$folderName\service

    Remove-Item ..\$folderName\provisioning -Force -Recurse
    New-Item -Path '../$folderName/provisioning' -ItemType Directory
    New-Item -Path '../$folderName/provisioning/provisioning-device-client' -ItemType Directory
    New-Item -Path '../$folderName/provisioning/provisioning-service-client' -ItemType Directory
    New-Item -Path '../$folderName/provisioning/security' -ItemType Directory
    New-Item -Path '../$folderName/provisioning/security/dice-provider' -ItemType Directory
    New-Item -Path '../$folderName/provisioning/security/dice-provider-emulator' -ItemType Directory
    New-Item -Path '../$folderName/provisioning/security/security-provider' -ItemType Directory
    New-Item -Path '../$folderName/provisioning/security/tpm-provider' -ItemType Directory
    New-Item -Path '../$folderName/provisioning/security/tpm-provider-emulator' -ItemType Directory
    New-Item -Path '../$folderName/provisioning/security/x509-provider' -ItemType Directory

    Copy-Item -Force -Path .\provisioning\provisioning-device-client\* -Destination ..\$folderName\provisioning\provisioning-device-client
    Copy-Item -Recurse -Force -Path .\provisioning\provisioning-device-client\com -Destination ..\$folderName\provisioning\provisioning-device-client

    Copy-Item -Force -Path .\provisioning\provisioning-service-client\* -Destination ..\$folderName\provisioning\provisioning-service-client
    Copy-Item -Recurse -Force -Path .\provisioning\provisioning-service-client\com -Destination ..\$folderName\provisioning\provisioning-service-client

    Copy-Item -Force -Path .\provisioning\security\tpm-provider-emulator\* -Destination ..\$folderName\provisioning\security\tpm-provider-emulator
    Copy-Item -Recurse -Force -Path .\provisioning\security\tpm-provider-emulator\com -Destination ..\$folderName\provisioning\security\tpm-provider-emulator

    Copy-Item -Force -Path .\provisioning\security\tpm-provider\* -Destination ..\$folderName\provisioning\security\tpm-provider
    Copy-Item -Recurse -Force -Path .\provisioning\security\tpm-provider\com -Destination ..\$folderName\provisioning\security\tpm-provider

    Copy-Item -Force -Path .\provisioning\security\dice-provider-emulator\* -Destination ..\$folderName\provisioning\security\dice-provider-emulator
    Copy-Item -Recurse -Force -Path .\provisioning\security\dice-provider-emulator\com -Destination ..\$folderName\provisioning\security\dice-provider-emulator

    Copy-Item -Force -Path .\provisioning\security\dice-provider\* -Destination ..\$folderName\provisioning\security\dice-provider
    Copy-Item -Recurse -Force -Path .\provisioning\security\dice-provider\com -Destination ..\$folderName\provisioning\security\dice-provider

    Copy-Item -Force -Path .\provisioning\security\security-provider\* -Destination ..\$folderName\provisioning\security\security-provider
    Copy-Item -Recurse -Force -Path .\provisioning\security\security-provider\com -Destination ..\$folderName\provisioning\security\security-provider

    Copy-Item -Force -Path .\provisioning\security\x509-provider\* -Destination ..\$folderName\provisioning\security\x509-provider
    Copy-Item -Recurse -Force -Path .\provisioning\security\x509-provider\com -Destination ..\$folderName\provisioning\security\x509-provider

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
