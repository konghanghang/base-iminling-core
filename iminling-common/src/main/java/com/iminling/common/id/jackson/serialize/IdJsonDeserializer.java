package com.iminling.common.id.jackson.serialize;

import cn.hutool.core.util.StrUtil;

import com.iminling.common.id.IdUtils;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * @author yslao@outlook.com
 * @since 2021/2/19
 */
public class IdJsonDeserializer extends ValueDeserializer<Long> {
    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) {
        String text = p.getString();
        if (StrUtil.isNotBlank(text)) {
            return IdUtils.decodeId(text);
        }
        return null;
    }
}
