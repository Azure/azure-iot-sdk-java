package samples.com.microsoft.azure.sdk.iot;

public enum ConnectionStatus
{
    // Either the connection is closed or is in a state where the ClientManager will not attempt to reconnect.
    DISCONNECTED,

    // The client manager is attempting to reconnect.
    CONNECTING,

    // The connection is established successfully.
    CONNECTED
}
