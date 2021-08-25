// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.fileupload;

import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUploadInProgress;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/**
 * Unit tests for file upload in progress class.
 * 100% methods, 100% lines covered
 */
public class FileUploadInProgressTest
{
    @Mocked
    private IotHubEventCallback mockIotHubEventCallback;

    @Mocked
    private Future mockFuture;

    /* Codes_SRS_FILEUPLOADINPROGRESS_21_001: [The constructor shall sore the content of the `statusCallback`, and `statusCallbackContext`.] */
    @Test
    public void constructorSuccess()
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();

        // act
        FileUploadInProgress fileUploadInProgress = Deencapsulation.newInstance(FileUploadInProgress.class,
                new Class[] {IotHubEventCallback.class, Object.class},
                mockIotHubEventCallback, context);

        // assert
        assertEquals(mockIotHubEventCallback, Deencapsulation.getField(fileUploadInProgress, "statusCallback"));
        assertEquals(context, Deencapsulation.getField(fileUploadInProgress, "statusCallbackContext"));
    }

    /* Codes_SRS_FILEUPLOADINPROGRESS_21_002: [If the `statusCallback` is null, the constructor shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrows()
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();

        // act
        Deencapsulation.newInstance(FileUploadInProgress.class,
                new Class[] {IotHubEventCallback.class, Object.class},
                (IotHubEventCallback)null, context);
    }

    /* Codes_SRS_FILEUPLOADINPROGRESS_21_003: [The setTask shall sore the content of the `task`.] */
    @Test
    public void setTaskSuccess()
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        FileUploadInProgress fileUploadInProgress = Deencapsulation.newInstance(FileUploadInProgress.class,
                new Class[] {IotHubEventCallback.class, Object.class},
                mockIotHubEventCallback, context);

        // act
        Deencapsulation.invoke(fileUploadInProgress, "setTask", new Class[] {Future.class}, mockFuture);

        // assert
        assertEquals(mockFuture, Deencapsulation.getField(fileUploadInProgress, "task"));
    }

    /* Codes_SRS_FILEUPLOADINPROGRESS_21_004: [If the `task` is null, the setTask shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void setTaskThrows()
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        FileUploadInProgress fileUploadInProgress = Deencapsulation.newInstance(FileUploadInProgress.class,
                new Class[] {IotHubEventCallback.class, Object.class},
                mockIotHubEventCallback, context);

        // act
        Deencapsulation.invoke(fileUploadInProgress, "setTask", new Class[] {Future.class}, (Future)null);
    }

    /* Codes_SRS_FILEUPLOADINPROGRESS_21_005: [The triggerCallback shall call the execute in `statusCallback` with the provided `iotHubStatusCode` and `statusCallbackContext`.] */
    @Test
    public void triggerCallbackSuccess()
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        FileUploadInProgress fileUploadInProgress = Deencapsulation.newInstance(FileUploadInProgress.class,
                new Class[] {IotHubEventCallback.class, Object.class},
                mockIotHubEventCallback, context);

        // act
        Deencapsulation.invoke(fileUploadInProgress, "triggerCallback", new Class[] {IotHubStatusCode.class}, IotHubStatusCode.OK);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockIotHubEventCallback, "execute", new Class[] {IotHubStatusCode.class, Object.class}, IotHubStatusCode.OK, context);
                times = 1;
            }
        };
    }

    /* Codes_SRS_FILEUPLOADINPROGRESS_21_006: [The isCancelled shall return the value of isCancelled on the `task`.] */
    @Test
    public void isCancelledTrueSuccess()
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        FileUploadInProgress fileUploadInProgress = Deencapsulation.newInstance(FileUploadInProgress.class,
                new Class[] {IotHubEventCallback.class, Object.class},
                mockIotHubEventCallback, context);
        Deencapsulation.invoke(fileUploadInProgress, "setTask", new Class[] {Future.class}, mockFuture);
        new NonStrictExpectations()
        {
            {
                mockFuture.isCancelled();
                result = true;
                times = 1;
            }
        };

        // act
        boolean result = (boolean)Deencapsulation.invoke(fileUploadInProgress, "isCancelled");

        // assert
        assertTrue(result);
    }

    /* Codes_SRS_FILEUPLOADINPROGRESS_21_006: [The isCancelled shall return the value of isCancelled on the `task`.] */
    @Test
    public void isCancelledFalseSuccess()
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        FileUploadInProgress fileUploadInProgress = Deencapsulation.newInstance(FileUploadInProgress.class,
                new Class[] {IotHubEventCallback.class, Object.class},
                mockIotHubEventCallback, context);
        Deencapsulation.invoke(fileUploadInProgress, "setTask", new Class[] {Future.class}, mockFuture);
        new NonStrictExpectations()
        {
            {
                mockFuture.isCancelled();
                result = false;
                times = 1;
            }
        };

        // act
        boolean result = (boolean)Deencapsulation.invoke(fileUploadInProgress, "isCancelled");

        // assert
        assertFalse(result);
    }

    /* Codes_SRS_FILEUPLOADINPROGRESS_21_007: [If the `task` is null, the isCancelled shall throws IOException.] */
    @Test (expected = IOException.class)
    public void isCancelledThrows()
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        FileUploadInProgress fileUploadInProgress = Deencapsulation.newInstance(FileUploadInProgress.class,
                new Class[] {IotHubEventCallback.class, Object.class},
                mockIotHubEventCallback, context);

        // act
        Deencapsulation.invoke(fileUploadInProgress, "isCancelled");
    }

}
