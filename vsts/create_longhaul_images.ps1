# options include
#  "MMSWindows2016" to use the 2016 VS Hosted agent image
#  "MMSWindows2019" to use the 2019 VS Hosted agent image
#  "MMSWindows2022" to use the 2022 VS Hosted agent image
#  "MMSUbuntu16.04" to use the Ubuntu 16.04 image
#  "MMSUbuntu18.04" to use the Ubuntu 18.04 image
#  "MMSUbuntu20.04" to use the Ubuntu 20.04 image

param ($subscriptionId, $resourceGroup, $location="eastus", $imageName, $poolName, [int] $maxPoolSize = 1, $vmImage = 'MMSUbuntu18.04')

if (!$subscriptionId -or !$resourceGroup -or !$location -or !$imageName -or !$poolName -or !$maxPoolSize -or !$vmImage)
{
    Write-Host "Missing command line arguments."
    Write-Host "Example usage:"
    Write-Host ".\create_longhaul_images.ps1 -subscriptionId 00000000-0000-0000-0000-000000000000 -resourceGroup myResourceGroup -location eastus -imageName myHostedAgentImage -poolName myHostedAgentPool -maxPoolSize 3 -vmImage MMSUbuntu18.04"
    Write-Host "vmImage parameter possible values: MMSWindows2016, MMSWindows2019, MMSWindows2022, MMSUbuntu16.04, MMSUbuntu18.04, MMSUbuntu20.04"
    return
}

az login
az account set --subscription $subscriptionId

$resourceGroupExistsAlready = az group exists  --resource-group $resourceGroup
if ([System.Convert]::ToBoolean($resourceGroupExistsAlready))
{
	Write-Host "Resource group" $resourceGroup "already exists. No new resource group will be created."
}
else
{
	Write-Host "Creating resource group" $resourceGroup
    az group create --resource-group $resourceGroup --location $location
}

Write-Host "Creating custom vm image to use for the pool"
$properties = '{\"resourceId\":\"/subscriptions/723b64f0-884d-4994-b6de-8960d049cb7e/resourceGroups/HostedPools/providers/Microsoft.Compute/galleries/MMSGallery/images/' + $vmImage + '/versions/latest\",\"imageType\":\"SharedGallery\"}'
Write-Host $properties
az resource create --resource-group $resourceGroup --resource-type Microsoft.CloudTest/Images --name $imageName --location $location --properties $properties



Write-Host "Creating custom pool with the created vm image. This may take several minutes to finish."
$properties = '{\"organization\":\"https://dev.azure.com/azure-iot-sdks\",\"sku\":{\"name\":\"Standard_DS2_V2\",\"tier\":\"Standard\"},\"images\":[{\"imageName\":\"' + $imageName + '\",\"poolbufferpercentage\":\"100\"}],\"maxPoolSize\":' + $maxPoolSize + ',\"agentProfile\":{\"type\":\"Stateful\"}}'
Write-Host $properties
az resource create --resource-group $resourceGroup --resource-type Microsoft.CloudTest/hostedpools --name $poolName --location $location --properties $properties	



