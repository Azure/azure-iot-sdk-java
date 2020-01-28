# Device Reconnection Sample

This sample code demonstrates the various connection status changes and connection status change reasons the Device Client can return, and how to handle them.

The device client exhibits the following connection status changes with reason:

<table>
  <tr>
    <th> Connection Status </th>
    <th> Change Reason </th>
    <th> Ownership of connectivity </th>
    <th> Comments </th>
  </tr>
  <tr>
    <td> CONNECTED </td>
    <td> CONNECTED_OK </td>
    <td> SDK </td>
    <td> SDK tries to remain connected to the service and can carry out all operation as normal </td>
  </tr>
  <tr>
    <td rowspan="2"> DISCONNECTED_RETRYING </td>
    <td> NO_NETWORK </td>
    <td rowspan="2"> SDK </td>
    <td rowspan="2"> When disconnection happens because of any reason (network failures, transient loss of connectivity etc.), SDK makes best attempt to connect back to IotHub. The RetryPolicy applied on the DeviceClient will be used to determine the count of reconnection attempts for <em>retriable</em> errors </td>
  </tr>
  <tr>
    <td> COMMUNICATION_ERROR </td>
  </tr>
  <tr>
    <td rowspan="5"> DISCONNECTED </td>
    <td> CLIENT_CLOSE </td>
    <td rowspan="5"> Application </td>
    <td> This is state when SDK was asked to close the connection by application </td>
  </tr>
  <tr>
    <td> BAD_CREDENTIAL </td>
    <td> Supplied credential isn’t good for device to connect to service - Fix the supplied credentials before attempting to reconnect again </td>
  </tr>
  <tr>
    <td> EXPIRED_SAS_TOKEN </td>
    <td> Supplied SAS Token has expired - Fix the supplied SAS token before attempting to reconnect again </td>
  </tr>
  <tr>
    <td> COMMUNICATION_ERROR </td>
    <td> This is the state when SDK landed up in a non-retriable error during communication - Inspect the <em>throwable</em> supplied in the <code>IotHubConnectionStatusChangeCallback</code> to get the stacktrace of the exception </td>
  </tr>
  <tr>
    <td> RETRY_EXPIRED </td>
    <td> This is the state when SDK attempted maximum retries set by the application </td>
  </tr>
</table>

For status `CLIENT_CLOSE`, `COMMUNICATION_ERROR` and `RETRY_EXPIRED`, the application can attempt to reconnect by closing and opening the client again eg. calling `deviceClient.closeNow();` `deviceClient.open();` in a separate thread.

We would like to highlight here that a separate thread should be used to call `open()`/ `closeNow()`. These calls should never be made from the same thread in the `IotHubConnectionStatusChangeCallback`. 

