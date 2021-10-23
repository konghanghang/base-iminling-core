package com.iminling.common.id.jackson.serialize;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.iminling.common.id.IdUtils;
import java.io.IOException;

/**
 * @author yslao@outlook.com
 * @since 2021/2/19
 */
public class IdJsonDeserializer extends JsonDeserializer<Long> {
    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (StrUtil.isNotBlank(text)) {
            return IdUtils.decodeId(text);
        }
        return null;
    }
}
