package com.iminling.common.id.jackson.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.iminling.common.id.IdUtils;

import java.io.IOException;

/**
 * @author yslao@outlook.com
 * @since 2021/2/19
 */
public class IdJsonSerializer extends JsonSerializer<Long> {
    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            serializers.defaultSerializeNull(gen);
        } else {
            gen.writeString(IdUtils.encodeId(value));
        }
    }
}
