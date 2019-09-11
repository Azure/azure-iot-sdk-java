package com.microsoft.azure.sdk.iot.digitaltwin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.microsoft.rest.CollectionFormat;
import com.microsoft.rest.protocol.SerializerAdapter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class CustomJsonAdapter implements SerializerAdapter<ObjectMapper> {

    private final ObjectMapper mapper;

    public CustomJsonAdapter() {
        mapper = new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ObjectMapper serializer() {
        return this.mapper;
    }

    public Converter.Factory converterFactory() {
        return new Converter.Factory() {
            public Converter<ResponseBody, ?> responseBodyConverter(final Type type, Annotation[] annotations, Retrofit retrofit) {
                return new Converter<ResponseBody, Object>() {
                    @Override
                    public Object convert(ResponseBody responseBody) throws IOException {
                        JavaType javaType = mapper.getTypeFactory().constructType(type);
                        return deserialize(responseBody.string(), javaType);
                    }
                };
            }

            public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
                return new Converter<Object, RequestBody>() {
                    @Override
                    public RequestBody convert(Object object) throws JsonProcessingException {
                        return RequestBody.create(MediaType.get("application/json"), serialize(object));
                    }
                };
            }

            public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                return new Converter<Object, String>() {
                    @Override
                    public String convert(Object object) throws JsonProcessingException {
                        return serialize(object);
                    }
                };
            }

        };
    }

    public String serialize(Object object) throws JsonProcessingException {
        if (object == null || object instanceof String) {
            return String.valueOf(object);
        } else {
            return mapper.writeValueAsString(object);
        }
    }

    public String serializeRaw(Object object) {
        throw new RuntimeException("Shouldn't be called");
    }

    public String serializeList(List<?> list, CollectionFormat format) {
        throw new RuntimeException("Shouldn't be called");
    }

    private JavaType constructJavaType(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return this.mapper.getTypeFactory().constructType(type);
        } else {
            JavaType[] javaTypeArgs = new JavaType[((ParameterizedType)type).getActualTypeArguments().length];

            for(int i = 0; i != ((ParameterizedType)type).getActualTypeArguments().length; ++i) {
                javaTypeArgs[i] = this.constructJavaType(((ParameterizedType)type).getActualTypeArguments()[i]);
            }

            return this.mapper.getTypeFactory().constructType(type, TypeBindings.create((Class)((ParameterizedType)type).getRawType(), javaTypeArgs));
        }
    }

    public Object deserialize(String value, Type type) throws IOException {
        if (isNotBlank(value)) {
            return mapper.readValue(value, this.constructJavaType(type));
        } else {
            return null;
        }
    }

}
