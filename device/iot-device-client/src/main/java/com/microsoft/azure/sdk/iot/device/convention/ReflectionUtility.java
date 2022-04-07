// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.convention;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that allows you to check if one data type can be converted to another safely.
 * <p>
 *     When trying to cast from a nullable type to a primitive you should always use the to xxxValue().
 *     This utility will help make the descision if you're unsure of the type being checked.
 *
 *     Example:
 *     {@code
 *      Integer cast = 56;
 *      int i = cast.intValue();
 *     }
 * </p>
 */
public class ReflectionUtility
{
    public enum TypeToReflect {
        BOOL,
        BYTE,
        CHAR,
        DOUBLE,
        FLOAT,
        INT,
        LONG,
        SHORT
    }

    private final Map<Class<?>, TypeToReflect> primitiveToJavaTypeMap = new HashMap<>();
    private final Map<Class<?>, TypeToReflect> javaToPrimitiveTypeMap = new HashMap<>();

    public static final ReflectionUtility INSTANCE = new ReflectionUtility();

    private ReflectionUtility()
    {
        primitiveToJavaTypeMap.put(boolean.class, TypeToReflect.BOOL);
        primitiveToJavaTypeMap.put(byte.class, TypeToReflect.BYTE);
        primitiveToJavaTypeMap.put(char.class, TypeToReflect.CHAR);
        primitiveToJavaTypeMap.put(double.class, TypeToReflect.DOUBLE);
        primitiveToJavaTypeMap.put(float.class, TypeToReflect.FLOAT);
        primitiveToJavaTypeMap.put(int.class, TypeToReflect.INT);
        primitiveToJavaTypeMap.put(long.class, TypeToReflect.LONG);
        primitiveToJavaTypeMap.put(short.class, TypeToReflect.SHORT);

        javaToPrimitiveTypeMap.put(Boolean.class, TypeToReflect.BOOL);
        javaToPrimitiveTypeMap.put(Byte.class, TypeToReflect.BYTE);
        javaToPrimitiveTypeMap.put(Character.class, TypeToReflect.CHAR);
        javaToPrimitiveTypeMap.put(Double.class, TypeToReflect.DOUBLE);
        javaToPrimitiveTypeMap.put(Float.class, TypeToReflect.FLOAT);
        javaToPrimitiveTypeMap.put(Integer.class, TypeToReflect.INT);
        javaToPrimitiveTypeMap.put(Long.class, TypeToReflect.LONG);
        javaToPrimitiveTypeMap.put(Short.class, TypeToReflect.SHORT);
    }

    /**
     * Checks to see if you can cat the primitive type to another.
     * @param typeToCheck
     * @return
     */
    public TypeToReflect canCastPrimitive(Class<?> typeToCheck)
    {
        if (javaToPrimitiveTypeMap.containsKey(typeToCheck))
        {
            return javaToPrimitiveTypeMap.get(typeToCheck);
        }
        else if (primitiveToJavaTypeMap.containsKey(typeToCheck))
        {
            return primitiveToJavaTypeMap.get(typeToCheck);
        }
        return null;
    }
}
