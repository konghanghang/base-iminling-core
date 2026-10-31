package com.iminling.core.config.mybatis;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.*;

import java.util.Map;

public class MapWrapperFactory implements ObjectWrapperFactory {
    @Override
    public boolean hasWrapperFor(Object value) {
        return value instanceof Map<?, ?>;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object value) {
        return new CustomizeWrapper(metaObject, (Map<String, Object>) value);
    }
}
