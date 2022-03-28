https://azure.github.io/azure-sdk/java_introduction.html

```java
public class IotHubClientException extends Exception 
{
    @Getter
    boolean isRetryable;

    @Getter
    IotHubStatusCode iotHubStatusCode;
}
```

```java
// current sync pattern
public IotHubStatusCode sendEvent(Message message) 
        throws InterruptedException, TimeoutException, IllegalStateException

// current async pattern
public void sendEventAsync(Message message, IotHubEventCallback messagesSentCallbackImpl, Object context) 
        throws IllegalStateException

interface IotHubEventCallback
{
    void execute(IotHubStatusCode responseStatus, Object context);
}
```

```java
// proposed sync pattern
public void sendEvent(Message message) 
        throws InterruptedException, IllegalStateException, IotHubClientException

// proposed async pattern
public void sendEventAsync(Message message, IotHubEventCallback messagesSentCallbackImpl, Object context) 
        throws IllegalStateException

interface MessageSentCallback
{
    void onMessageSent(IotHubClientException e, Object context);
}
```

```java
// sync demonstration code 
try
{
    deviceClient.sendEvent(message);
}
catch (IotHubClientException e)
{
    if (e.isRetryable())
    {
        // requeue the work    
    }
    else
    {
        // abandon the work
    }
}
catch (IllegalStateException e)
{
    deviceClient.open(true);
    // requeue the work
}
catch (InterruptedException e)
{
    // user wanted this process to stop, so close the client and return
    deviceClient.close();
    return;
}
```

```java
// async demonstration code 
try
{
    deviceClient.sendEventAsync(message, this, null);
}
catch (IllegalStateException e)
{
    deviceClient.open(true);
    // requeue the work
}

void execute(IotHubClientException e, Object context)
{
    if (e == null)
    {
        // message sent successfully. log it or do whatever.
        return;
    }

    if (e.isRetryable())
    {
        // requeue the work    
    }
    else
    {
        // abandon the work
    }
}

```