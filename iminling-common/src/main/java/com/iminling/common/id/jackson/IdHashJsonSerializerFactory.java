package com.iminling.common.id.jackson;

import com.iminling.common.id.annotation.IdHash;
import com.iminling.common.id.jackson.serialize.IdJsonSerializer;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.*;

/**
 * @author yslao@outlook.com
 * @since 2020/11/13
 */
public class IdHashJsonSerializerFactory extends ValueSerializer<Object> {
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext serializers) {
        System.out.println("------------");
    }

    @Override
    public ValueSerializer<?> createContextual(SerializationContext prov, BeanProperty property) {
        IdHash annotation = property.getAnnotation(IdHash.class);
        JavaType type = property.getType();
        // string类型
        if (type.isTypeOrSubTypeOf(Long.class)) {
            return new IdJsonSerializer();
        }
        return null;
    }
}
