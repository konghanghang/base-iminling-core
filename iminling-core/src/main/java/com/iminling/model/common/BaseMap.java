package com.iminling.model.common;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;

/**
 * 增强HashMap,理论上和FastJson的JSONObject功能相同
 * 可以使用getInteger();getLong()方法.
 * @author yslao@outlook.com
 */
public class BaseMap extends HashMap<String, Object> {

    public BaseMap() {
        super();
    }

    public BaseMap(String key, Object value) {
        super();
        this.put(key, value);
    }

    public Boolean getBoolean(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return Boolean.valueOf(value.toString());
    }

    public Short getShort(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return Short.valueOf(value.toString());
    }

    public Integer getInteger(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return Integer.valueOf(value.toString());
    }

    public Long getLong(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return Long.valueOf(value.toString());
    }

    public Double getDouble(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return Double.valueOf(value.toString());
    }

    public BigDecimal getBigDecimal(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return new BigDecimal(value.toString());
    }

    public BigInteger getBigInteger(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return new BigInteger(value.toString());
    }

    public String getString(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public Byte getByte(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return Byte.valueOf(value.toString());
    }

}
