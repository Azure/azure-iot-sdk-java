package com.microsoft.azure.sdk.iot.device.fileupload;

import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import lombok.extern.slf4j.Slf4j;

import static com.microsoft.azure.sdk.iot.device.fileupload.FileUpload.fileUploadInProgressesSet;

@Slf4j
public final class FileUploadStatusCallBack implements IotHubEventCallback
{
    @Override
    public synchronized void execute(IotHubStatusCode status, Object context)
    {
        if(context instanceof FileUploadInProgress)
        {
            FileUploadInProgress uploadInProgress = (FileUploadInProgress) context;
            uploadInProgress.triggerCallback(status);
            try
            {
                fileUploadInProgressesSet.remove(context);
            }
            catch (ClassCastException | NullPointerException | UnsupportedOperationException e)
            {
                log.error("FileUploadStatusCallBack received callback for unknown FileUpload", e);
            }
        }
        else
        {
            log.error("FileUploadStatusCallBack received callback for unknown FileUpload");
        }
    }
}
