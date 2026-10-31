package com.iminling.common.id.jackson.serialize;

import com.iminling.common.id.IdUtils;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * @author yslao@outlook.com
 * @since 2021/2/19
 */
public class IdJsonSerializer extends ValueSerializer<Long> {
    @Override
    public void serialize(Long value, JsonGenerator gen, SerializationContext serializers) {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(IdUtils.encodeId(value));
        }
    }
}
