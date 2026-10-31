package com.iminling.core.config.mybatis;

import com.google.common.base.CaseFormat;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.MapWrapper;

import java.util.Map;

public class CustomizeWrapper extends MapWrapper {
    public CustomizeWrapper(MetaObject metaObject, Map<String, Object> map) {
        super(metaObject, map);
    }

    @Override
    public String findProperty(String name, boolean camelCase) {
        return camelCase ? CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name) : name;
    }
}
