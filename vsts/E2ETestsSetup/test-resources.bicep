@description('The name of the main IoT hub used by tests.')
param HubName string = '${resourceGroup().name}-hub'

@description('The number of IoT hub units to be deployed.')
param HubUnitsCount int = 1

@description('The IoT hub consumer group name.')
param ConsumerGroupName string = 'e2e-tests'

@description('The name of DPS used by tests.')
param DpsName string ='${resourceGroup().name}-dps'

var hubKeysId = resourceId('Microsoft.Devices/IotHubs/Iothubkeys', HubName, 'iothubowner')
var dpsKeysId = resourceId('Microsoft.Devices/ProvisioningServices/keys', DpsName, 'provisioningserviceowner')


resource iotHub 'Microsoft.Devices/IotHubs@2021-03-03-preview' = {
  name: HubName
  location: resourceGroup().location
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    eventHubEndpoints: {
      events: {
        retentionTimeInDays: 1
        partitionCount: 4
      }
    }
    cloudToDevice: {
      defaultTtlAsIso8601: 'PT1H'
      maxDeliveryCount: 100
      feedback: {
        ttlAsIso8601: 'PT1H'
        lockDurationAsIso8601: 'PT5S'
        maxDeliveryCount: 100
      }
    }
    messagingEndpoints: {
      fileNotifications: {
        ttlAsIso8601: 'PT1H'
        lockDurationAsIso8601: 'PT5S'
        maxDeliveryCount: 100
      }
    }
    enableFileUploadNotifications: true
  }
  sku: {
    name: 'S1'
    tier: 'Standard'
    capacity: HubUnitsCount
  }
  dependsOn: [
    container
  ]
}

resource consumerGroups 'Microsoft.Devices/IotHubs/eventHubEndpoints/ConsumerGroups@2018-04-01' = {
  name: '${iotHub.name}/events/${ConsumerGroupName}'
}

resource provisioningService 'Microsoft.Devices/provisioningServices@2017-11-15' = {
  name: DpsName
  location: resourceGroup().location
  sku: {
    name: 'S1'
    capacity: 1
  }
  properties: {
    iotHubs: [
      {
        location: resourceGroup().location
        connectionString: 'HostName=${iotHub.name}.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=${listkeys(hubKeysId, '2020-01-01').primaryKey}'
      }
    ]
  }
}

output hubName string = HubName
output hubConnectionString string = 'HostName=${HubName}.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=${listkeys(hubKeysId, '2019-11-04').primaryKey}'
output dpsName string = DpsName
output dpsConnectionString string = 'HostName=${DpsName}.azure-devices-provisioning.net;SharedAccessKeyName=provisioningserviceowner;SharedAccessKey=${listkeys(dpsKeysId, '2017-11-15').primaryKey}'