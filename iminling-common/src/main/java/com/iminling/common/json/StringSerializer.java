package com.iminling.common.json;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class StringSerializer extends ValueSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator generator, SerializationContext context) {
        generator.writeRawValue(value);
    }
}
