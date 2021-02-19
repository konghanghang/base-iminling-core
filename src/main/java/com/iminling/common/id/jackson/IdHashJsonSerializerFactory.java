package com.iminling.common.id.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.iminling.common.id.IdUtils;
import com.iminling.common.id.annotation.IdHash;
import com.iminling.common.id.jackson.serialize.IdJsonSerializer;

import java.io.IOException;

/**
 * @author yslao@outlook.com
 * @since 2020/11/13
 */
public class IdHashJsonSerializerFactory extends JsonSerializer<Object> implements ContextualSerializer {
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        System.out.println("------------");
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        IdHash annotation = property.getAnnotation(IdHash.class);
        JavaType type = property.getType();
        // string类型
        if (type.isTypeOrSubTypeOf(Long.class)) {
            return new IdJsonSerializer();
        }
        return null;
    }
}
