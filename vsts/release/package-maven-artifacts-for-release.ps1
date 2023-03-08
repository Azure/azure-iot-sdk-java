# This script builds all of the maven artifacts needed to release a java package

param (
 [string]$securityProvider,
 [string]$tpmProvider,
 [string]$x509Provider,
 [string]$provisioningDeviceClient,
 [string]$provisioningServiceClient,
 [string]$iotDeviceClient,
 [string]$defaultWorkingDirectory
 )
 
function GetVersion($Sources, $artifactId) {
    # reads a version number from a pom.xml file
    $pomPath = Join-Path $env:sources "pom.xml"
    $propertyName = $artifactId
    $previewTag = "-preview";
    if ($artifactId.Contains($previewTag))
    {
        $propertyName = $artifactId.subString(0, ($artifactId.length - $previewTag.length))
    }

    $propertyName = $propertyName + "-version"

    $xml = [xml]$(Get-Content $pomPath -ErrorAction Stop)
    $version = $xml.project.properties.$propertyName

    if ([System.String]::IsNullOrWhiteSpace($version)) {
        $exception = "Version not found for property name " + $propertyName
        throw $exception
    }

    return $version
}

function GetJobs($Sources, [Hashtable]$Clients) {
    # validate and collect parameters for each 'build job'

    $jobs = @()

    foreach ($artifactId in $Clients.Keys) {

        $clientSource = Join-Path $Sources $Clients[$artifactId]

        if ($(Test-Path $clientSource -PathType Container) -eq $false) {
            throw "Folder not found: $($clientSource)"
        }

        # This folder will contain a "pom.xml" as well as a ".flattened-pom.xml".
        # The former should not be published because it contains a reference to a parent pom.xml that we do not publish
        # The latter contains a flattened version of the normal pom. This flattened pom has all property values filled in from its parent, as well as all dependencies declared that were declared in the parent.
        $pomFilePath = Join-Path $clientSource "pom.xml"
        $flattenedPomFilePath = Join-Path $clientSource ".flattened-pom.xml"
        $exists = Test-Path $pomFilePath -PathType Leaf

        if ($exists -eq $false) {
            throw "File not found: $pomFilePath"
        }

        $version = GetVersion $Sources $artifactId

        $clientFolder = Join-Path $Sources $artifactId
        $resourcesFolder = Join-Path $clientFolder "/src/main/resources"  # license file location

        $jobs += [PSCustomObject]@{
            ArtifactId 		= $artifactId
            PomFile    		= $pomFilePath  # full path
            FlattenedPomFile= $flattenedPomFilePath  # full path
            Resources  		= $resourcesFolder # license file location
            Source     		= $clientSource # sdk repo
            Version    		= $version # for pom xml file name
        }
    }

    return $jobs
}

<#
.SYNOPSIS
Maven package Azure IoT Java SDK.
.PARAMETER Sources
Root folder of the Azure IoT Java SDK repositroy.
.PARAMETER Tools
Root folder path of Internals build tools.
.PARAMETER Output
Location for the compiled JAR files to ship.
#>
function PackageArtifacts($Sources, $Tools, $Output) {
    New-Item $Output -ItemType Directory # output folder should be new on the agent

    $location = Get-Location
    $jobs = GetJobs $Sources $Clients

    try {

        Set-Location $Sources

        mvn clean install -DskipTests -T 2C  --batch-mode -q # Attempt to build
        TestLastExitCode

        foreach ($job in $jobs) {

            New-Item $job.Resources -ItemType Directory  # make folder to include license files in package

            # copy notice file to temp folder

            Set-Location $job.Source  # set current directory to the folder mvn expects for build and package

            $licensePath = Join-Path $Sources "LICENSE.txt"
            $thirdPartyNoticePath = Join-Path $Sources "thirdpartynotice.txt"

            Copy-Item $licensePath $job.Resources
            Copy-Item $thirdPartyNoticePath $job.Resources

            mvn package -DskipTests -T 2C # Attempt to package
            TestLastExitCode

            # if successful mvn will produce a local 'target' folder with the .jar files

            $pomFile = "$($job.ArtifactId)-$($job.Version).pom"
            $targetPomFile = Join-Path $Output $pomFile  # publish name for .pom file

            # post artifacts to publish location

            Copy-Item "$($job.Source)/target/*.jar" $Output

            $flattenedPomExists = Test-Path $job.FlattenedPomFile -PathType Leaf
            if ($flattenedPomExists -eq $false) {
                throw "Flattened pom could not be found. Was the flatten plugin removed?"
            }

            Copy-Item $job.FlattenedPomFile $targetPomFile
        }
    }
    finally {
        Set-Location $location
    }
}

function TestLastExitCode {
    if ($LASTEXITCODE -ne 0) {
        throw "Last exit code is ($LASTEXITCODE)"
    }
}

function ValidateInputParameter($parameter, $parameterName, $packageName, $path) {
    if ($parameter -eq "True")
    {
        $Clients.Add($packageName, $path)
        echo "Will package $packageName"
    }
    elseif ($parameter -eq "False")
    {
        echo "Will not package $packageName"
    }
    else
    {
        echo "Must set parameter `"$parameterName`" to either `"True`" or `"False`""
        exit 1
    }
}

$iotHubBasePomPath = Join-Path $env:sources "pom.xml"

$Clients = @{ }
ValidateInputParameter $securityProvider "securityProvider" "security-provider" "provisioning/security/security-provider"
ValidateInputParameter $tpmProvider "tpmProvider" "tpm-provider" "provisioning/security/tpm-provider"
ValidateInputParameter $x509Provider "x509Provider" "x509-provider" "provisioning/security/x509-provider"
ValidateInputParameter $provisioningDeviceClient "provisioningDeviceClient" "provisioning-device-client" "provisioning/provisioning-device-client"
ValidateInputParameter $provisioningServiceClient "provisioningServiceClient" "provisioning-service-client" "provisioning/provisioning-service-client"
ValidateInputParameter $iotDeviceClient "iotDeviceClient" "iot-device-client" "device/iot-device-client"

if (($securityProvider -eq "False") -and ($tpmProvider -eq "False") -and ($x509Provider -eq "False") -and ($provisioningDeviceClient -eq "False") -and ($provisioningServiceClient -eq "False") -and ($iotDeviceClient -eq "False"))
{
    echo "No packages were configured to be released, so this pipeline would do nothing. Please schedule a new run of this pipeline, and configure at least one package to be released."
    exit 1
}

Write-Host "`n`n`n`n"
Write-Host "\/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/"
Write-Host "\/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/"
$date = Get-Date -UFormat "%Y_%m_%d_%A_%H_%M_%S"
Write-Host "The below string is used in the partner release pipeline to identify which folder has the jar files to publish, note it for later"
Write-Host ""
Write-Host "azure-iot-sdk/java/$date"
Write-Host ""
Write-Host "/\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\"
Write-Host "/\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\"
Write-Host "`n`n`n`n" 

$outputFolder = $env:output + "/" + $date

PackageArtifacts -Sources $env:sources -Tools "$defaultWorkingDirectory/_azure-iot-sdk-java" -Output $outputFolder
