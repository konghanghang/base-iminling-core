package com.iminling.common.id.jackson;

import com.iminling.common.id.annotation.IdHash;
import com.iminling.common.id.jackson.serialize.IdJsonDeserializer;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.*;

/**
 * @author yslao@outlook.com
 * @since 2020/11/13
 */
public class IdHashJsonDeserializerFactory extends ValueDeserializer<Object> {
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) {
        System.out.println("----------------");
        return null;
    }

    @Override
    public ValueDeserializer<?> createContextual(
            DeserializationContext ctxt, BeanProperty property) {
        IdHash annotation = property.getAnnotation(IdHash.class);
        JavaType type = property.getType();
        // string类型
        if (type.isTypeOrSubTypeOf(Long.class)) {
            return new IdJsonDeserializer();
        }
        return null;
    }
}
