package com.iminling.common.map;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapUtils {

    /**
     * 对象转map
     * @param obj 对象
     * @return map
     * @throws IllegalAccessException 非法访问异常
     */
    public static Map<String, Object> objToMap(Object obj) throws IllegalAccessException {
        Map<String, Object> map = new LinkedHashMap<>();
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object value = field.get(obj);
            map.put(fieldName, value);
        }
        return map;
    }

}
