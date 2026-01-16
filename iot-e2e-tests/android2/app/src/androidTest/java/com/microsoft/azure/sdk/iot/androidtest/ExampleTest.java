package com.microsoft.azure.sdk.iot.androidtest;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleTest {
    @Test
    public void shouldPass() {

    }

    @Test
    public void shouldFail() {
        Assert.fail("lolololol");
    }

}