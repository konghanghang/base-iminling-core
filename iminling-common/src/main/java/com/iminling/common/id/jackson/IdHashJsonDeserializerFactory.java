package com.iminling.common.id.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.iminling.common.id.annotation.IdHash;
import com.iminling.common.id.jackson.serialize.IdJsonDeserializer;

import java.io.IOException;

/**
 * @author yslao@outlook.com
 * @since 2020/11/13
 */
public class IdHashJsonDeserializerFactory extends JsonDeserializer<Object> implements ContextualDeserializer {
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        System.out.println("----------------");
        return null;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        IdHash annotation = property.getAnnotation(IdHash.class);
        JavaType type = property.getType();
        // string类型
        if (type.isTypeOrSubTypeOf(Long.class)) {
            return new IdJsonDeserializer();
        }
        return null;
    }
}
