# Default RetryPolicy

The default retry policy is defined by the following:
`new ExponentialBackoffWithJitter(Integer.MAX_VALUE, 100, 10*1000, 100);`

This retry policy will have a fast first retry by default as well.

The retry mechanism will stop after `DefaultOperationTimeoutInMilliseconds` which is currently set at 4 minutes. 
This timeout can be altered through the DeviceClient API `public void setOperationTimeout(long timeout)`


# Custom RetryPolicy

This SDK allows you to set your own custom retry policy through the DeviceClient or TransportClient API

`public void setRetryPolicy(RetryPolicy retryPolicy)`

Your custom retry policy will always be consulted before retrying any operation. However, your custom retry policy may not
be consulted about a particular retry scenario if our SDK has already determined that retrying would be pointless or 
cause more harm than good.