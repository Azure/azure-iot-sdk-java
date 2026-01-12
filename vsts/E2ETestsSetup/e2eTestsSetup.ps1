# NOTE: This script needs to be run using admin mode

param(
    [Parameter(Mandatory)]
    [string] $Region,

    [Parameter(Mandatory)]
    [string] $ResourceGroup,
    
    [Parameter(Mandatory)]
    [string] $SubscriptionId
)

$startTime = (Get-Date)

########################################################################################################
# Set error and warning preferences for the script to run.
########################################################################################################

$ErrorActionPreference = "Stop"
$WarningActionPreference = "Continue"

########################################################################################################
# Check PowerShell version
########################################################################################################
if ($PSversiontable.PSVersion -lt "7.0.0")
{
    Write-Error "This script requires PowerShell v7. Please install it and rerun."
    exit
}

#################################################################################################
# Set required parameters.
#################################################################################################

$Region = $Region.Replace(' ', '')
$iothubUnitsToBeCreated = 1

## remove any characters that aren't letters or numbers, and then validate
$storageAccountName = "$($ResourceGroup.ToLower())sa-"
$storageAccountName = [regex]::Replace($storageAccountName, "[^a-z0-9]", "")
if (-not ($storageAccountName -match "^[a-z0-9][a-z0-9]{1,22}[a-z0-9]$"))
{
    throw "Storage account name derived from resource group has illegal characters: $storageAccountName"
}

$iothubUnitsToBeCreated = 5;

######################################################################################################
# Get-ResourceGroup - Finds or creates the resource group to be used by the
# deployment.
######################################################################################################

$rgExists = az group exists --name $ResourceGroup
if ($rgExists -eq "False")
{
    Write-Host "`nCreating resource group $ResourceGroup in $Region"
    az group create --name $ResourceGroup --location $Region --output none
}

$resourceGroupId = az group show -n $ResourceGroup --query id --out tsv

#######################################################################################################
# Invoke-Deployment - Uses the .\.json template to create the necessary resources to run E2E tests.
#######################################################################################################

# Create a unique deployment name
$randomSuffix = -join ((65..90) + (97..122) | Get-Random -Count 5 | ForEach-Object { [char]$_ })
$deploymentName = "IotE2eInfra-$randomSuffix"

# Deploy
Write-Host @"
    `nStarting deployment which may take a while.
    1. Progress can be monitored from the Azure Portal (http://portal.azure.com); go to resource group | deployments | deployment name.
    2. Info to track: subscription ($SubscriptionId), resource group ($ResourceGroup), deployment name ($deploymentName).
"@

az deployment group create `
    --resource-group $ResourceGroup `
    --name $deploymentName `
    --output none `
    --only-show-errors `
    --template-file "$PSScriptRoot\test-resources.json" `
    --parameters `
    StorageAccountName=$storageAccountName `
    HubUnitsCount=$iothubUnitsToBeCreated

if ($LastExitCode -ne 0)
{
    throw "Error running resource group deployment."
}

Write-Host "`nYour infrastructure is ready in subscription ($SubscriptionId), resource group ($ResourceGroup)."

#########################################################################################################
# Get properties to setup the config file for environment variables.
#########################################################################################################

Write-Host "`nGetting generated names and secrets from ARM template output."
$iotHubConnectionString = az deployment group show -g $ResourceGroup -n $deploymentName --query 'properties.outputs.hubConnectionString.value' --output tsv
$dpsName = az deployment group show -g $ResourceGroup -n $deploymentName --query 'properties.outputs.dpsName.value' --output tsv
$dpsConnectionString = az deployment group show -g $ResourceGroup -n $deploymentName  --query 'properties.outputs.dpsConnectionString.value' --output tsv
$storageAccountConnectionString = az deployment group show -g $ResourceGroup -n $deploymentName  --query 'properties.outputs.storageAccountConnectionString.value' --output tsv
$workspaceId = az deployment group show -g $ResourceGroup -n $deploymentName --query 'properties.outputs.workspaceId.value' --output tsv
$iotHubName = az deployment group show -g $ResourceGroup -n $deploymentName --query 'properties.outputs.hubName.value' --output tsv

##################################################################################################################################
# Fetch the iothubowner policy details.
##################################################################################################################################

$iothubownerSasPolicy = "iothubowner"
$iothubownerSasPrimaryKey = az iot hub policy show --hub-name $iotHubName --name $iothubownerSasPolicy --query 'primaryKey'

###################################################################################################################################
# Configure environment variables with secret values.
###################################################################################################################################

$dpsEndpoint = "global.azure-devices-provisioning.net"
if ($Region.EndsWith('euap', 'CurrentCultureIgnoreCase'))
{
    $dpsEndpoint = "global-canary.azure-devices-provisioning.net"
}

$dpsIdScope = az iot dps show -g $ResourceGroup --name $dpsName --query 'properties.idScope' --output tsv

# Environment variables for IoT Hub E2E tests
Write-Host "##vso[task.setvariable variable=IOTHUB_CONNECTION_STRING;isOutput=true]$iotHubConnectionString"
Write-Host "##vso[task.setvariable variable=IOTHUB_X509_DEVICE_PFX_CERTIFICATE;isOutput=true]$iothubX509DevicePfxBase64"
Write-Host "##vso[task.setvariable variable=IOTHUB_X509_CHAIN_DEVICE_NAME;isOutput=true]$iotHubX509CertChainDeviceCommonName";
Write-Host "##vso[task.setvariable variable=IOTHUB_X509_CHAIN_DEVICE_PFX_CERTIFICATE;isOutput=true]$iothubX509ChainDevicePfxBase64"
Write-Host "##vso[task.setvariable variable=IOTHUB_USER_ASSIGNED_MSI_RESOURCE_ID;isOutput=true]$msiResourceId"

# Environment variables for DPS E2E tests
Write-Host "##vso[task.setvariable variable=DPS_IDSCOPE;isOutput=true]$dpsIdScope"
Write-Host "##vso[task.setvariable variable=PROVISIONING_CONNECTION_STRING;isOutput=true]$dpsConnectionString"
Write-Host "##vso[task.setvariable variable=DPS_GLOBALDEVICEENDPOINT;isOutput=true]$dpsEndpoint"

Write-Host "Done!"