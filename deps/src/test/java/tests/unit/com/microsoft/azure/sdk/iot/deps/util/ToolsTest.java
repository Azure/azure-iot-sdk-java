/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.deps.util;


import com.microsoft.azure.sdk.iot.deps.util.Tools;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Utility function collection
 */
public class ToolsTest
{
    // Tests_SRS_SDK_JAVA_TOOLS_12_001: [The function shall return true if the input is null]      @Test
    @Test
    public void isNullOrEmptyInputNullCalledWithNull()
    {
        // Arrange
        String value = null;
        Boolean expResult = true;
        // Act
        Boolean result = Tools.isNullOrEmpty(value);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_002: [The function shall return true if the input string’s length is zero]
    @Test
    public void isNullOrEmptyCalledWithEmpty()
    {
        // Arrange
        String value = "";
        Boolean expResult = true;
        // Act
        Boolean result = Tools.isNullOrEmpty(value);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_003: [The function shall return false otherwise]
    @Test
    public void isNullOrEmptyInputNotEmpty()
    {
        // Arrange
        String value = "XXX";
        Boolean expResult = false;
        // Act
        Boolean result = Tools.isNullOrEmpty(value);
        // Assert
        assertEquals(expResult, result);
    }
}
