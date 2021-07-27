package com.microsoft.azure.sdk.iot.deps.convention;

import java.util.HashMap;
import java.util.Map;

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

    public TypeToReflect canCastPrimitive(Class<?> type1)
    {
        if (javaToPrimitiveTypeMap.containsKey(type1))
        {
            return javaToPrimitiveTypeMap.get(type1);
        }
        else if (primitiveToJavaTypeMap.containsKey(type1))
        {
            return primitiveToJavaTypeMap.get(type1);
        }
        return null;
    }
}
