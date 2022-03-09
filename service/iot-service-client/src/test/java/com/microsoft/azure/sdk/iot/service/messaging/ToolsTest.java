/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.messaging;

import mockit.Expectations;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Utility function collection
 */
public class ToolsTest
{
    // Tests_SRS_SERVICE_SDK_JAVA_TOOLS_12_001: [The function shall return true if the input is null]      @Test
    @Test
    public void isNullOrEmptyInputNull_called_with_null()
    {
        // Arrange
        String value = null;
        Boolean expResult = true;
        // Act
        Boolean result = Tools.isNullOrEmpty(value);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_TOOLS_12_002: [The function shall return true if the input string’s length is zero]
    @Test
    public void isNullOrEmpty_called_with_empty()
    {
        // Arrange
        String value = "";
        Boolean expResult = true;
        // Act
        Boolean result = Tools.isNullOrEmpty(value);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_TOOLS_12_003: [The function shall return false otherwise]
    @Test
    public void isNullOrEmpty_input_not_empty()
    {
        // Arrange
        String value = "XXX";
        Boolean expResult = false;
        // Act
        Boolean result = Tools.isNullOrEmpty(value);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_TOOLS_12_004: [The function shall return true if the input is null]
    @Test
    public void isNullOrWhiteSpace_input_null()
    {
        // Arrange
        String value = null;
        Boolean expResult = true;
        // Act
        Boolean result = Tools.isNullOrWhiteSpace(value);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_TOOLS_12_005: [The function shall call the isNullOrEmpty function and return with it’s return value]
    @Test
    public void isNullOrWhiteSpace_input_white_space()
    {
        // Arrange
        String value = " ";
        Boolean expResult = true;
        new Expectations()
        {
            Tools tools;
            {
                Tools.isNullOrEmpty(anyString);
            }
        };
        // Act
        Boolean result = Tools.isNullOrWhiteSpace(value);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_TOOLS_12_005: [The function shall call the isNullOrEmpty function and return with it’s return value]
    @Test
    public void isNullOrWhiteSpace_input_white_spaces()
    {
        // Arrange
        String value = "   ";
        Boolean expResult = true;
        new Expectations()
        {
            Tools tools;
            {
                Tools.isNullOrEmpty(anyString);
            }
        };
        // Act
        Boolean result = Tools.isNullOrWhiteSpace(value);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_TOOLS_12_005: [The function shall call the isNullOrEmpty function and return with it’s return value]
    @Test
    public void isNullOrWhiteSpace_not_empty()
    {
        // Arrange
        String value = "XXX";
        Boolean expResult = false;
        new Expectations()
        {
            Tools tools;
            {
                Tools.isNullOrEmpty(anyString);
            }
        };
        // Act
        Boolean result = Tools.isNullOrWhiteSpace(value);
        // Assert
        assertEquals(expResult, result);
    }
}
