package com.iminling.common.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iminling.common.date.DateUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.core.JacksonException;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;
import tools.jackson.dataformat.xml.XmlMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;

/** Shared JSON conventions: formatted dates, string-valued longs and lenient unknown properties. */
public final class JsonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);
    private static final JsonMapper OBJECT_MAPPER =
            JsonMapper.builder()
                    .configureForJackson2()
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
                    .enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
                    .addModule(conventions())
                    .build();
    private static final XmlMapper XML_MAPPER = createXmlMapper();

    private JsonUtil() {}

    public static SimpleModule conventions() {
        DateTimeFormatter date = DateTimeFormatter.ofPattern(DateUtils.DEFAULT_DATE_FORMAT);
        DateTimeFormatter time = DateTimeFormatter.ofPattern(DateUtils.DEFAULT_TIME_FORMAT);
        DateTimeFormatter dateTime =
                DateTimeFormatter.ofPattern(DateUtils.DEFAULT_DATE_TIME_FORMAT);
        return new SimpleModule("iminling-json-conventions")
                .addSerializer(Long.class, ToStringSerializer.instance)
                .addSerializer(Long.TYPE, ToStringSerializer.instance)
                .addSerializer(LocalDate.class, new LocalDateSerializer(date))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(time))
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTime))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(date))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(time))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTime));
    }

    public static XmlMapper createXmlMapper() {
        return XmlMapper.builder()
                .configureForJackson2()
                .enable(
                        DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
                        DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .changeDefaultPropertyInclusion(
                        value ->
                                JsonInclude.Value.construct(
                                        JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL))
                .addModule(conventions())
                .build();
    }

    public static JsonMapper getInstant() {
        return OBJECT_MAPPER;
    }

    /**
     * Whether a bean has no non-null instance fields; scalars and containers do not require deep
     * reflection.
     */
    public static boolean objIsNull(Object value) {
        if (value == null) return true;
        if (value instanceof CharSequence
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Character
                || value instanceof Enum<?>
                || value instanceof Collection<?>
                || value instanceof Map<?, ?>
                || value.getClass().isArray()
                || value.getClass().getPackageName().startsWith("java.")) return false;
        for (Class<?> type = value.getClass(); type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) continue;
                try {
                    if (field.trySetAccessible() && field.get(value) != null) return false;
                } catch (IllegalAccessException e) {
                    LOGGER.debug("Cannot inspect {}", field.getName(), e);
                }
            }
        }
        return true;
    }

    public static String obj2Str(Object value) {
        if (objIsNull(value)) return null;
        try {
            return value instanceof String text ? text : OBJECT_MAPPER.writeValueAsString(value);
        } catch (JacksonException e) {
            LOGGER.warn("Parse Object to String error", e);
            return null;
        }
    }

    public static String obj2StrPretty(Object value) {
        if (value == null) return null;
        try {
            return value instanceof String text
                    ? text
                    : OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JacksonException e) {
            LOGGER.warn("Parse Object to String error", e);
            return null;
        }
    }

    public static <T> T str2Obj(String text, Class<T> type) {
        if (text == null || text.isEmpty() || type == null) return null;
        if (type == String.class) return type.cast(text);
        try {
            return OBJECT_MAPPER.readValue(text, type);
        } catch (JacksonException e) {
            LOGGER.warn("Parse String to Object error", e);
            return null;
        }
    }

    public static <T> T str2Obj(String text, Class<?> collectionType, Class<?>... elementTypes) {
        try {
            return OBJECT_MAPPER.readValue(
                    text,
                    OBJECT_MAPPER
                            .getTypeFactory()
                            .constructParametricType(collectionType, elementTypes));
        } catch (JacksonException e) {
            LOGGER.warn("Parse String to Object error", e);
            return null;
        }
    }

    public static <T> T str2Obj(String text, Type type) {
        try {
            return OBJECT_MAPPER.readValue(text, OBJECT_MAPPER.constructType(type));
        } catch (JacksonException e) {
            LOGGER.warn("Parse String to Object error", e);
            return null;
        }
    }

    public static <T> T str2Obj(String text, TypeReference<?> type) {
        try {
            return OBJECT_MAPPER.readValue(text, OBJECT_MAPPER.constructType(type));
        } catch (JacksonException e) {
            LOGGER.warn("Parse String to Object error", e);
            return null;
        }
    }

    public static <T> T convert(Object value, Class<T> type) {
        return value == null ? null : OBJECT_MAPPER.convertValue(value, type);
    }

    public static <T> T convert(Object value, Class<?> collectionType, Class<?>... elementTypes) {
        return value == null
                ? null
                : OBJECT_MAPPER.convertValue(
                        value,
                        OBJECT_MAPPER
                                .getTypeFactory()
                                .constructParametricType(collectionType, elementTypes));
    }

    public static <T> T convert(Object value, Type type) {
        return value == null
                ? null
                : OBJECT_MAPPER.convertValue(value, OBJECT_MAPPER.constructType(type));
    }

    public static <T> T xml2Obj(String text, Class<T> type) {
        try {
            return XML_MAPPER.readValue(text, type);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("序列化xml异常,xml:" + text, e);
        }
    }

    public static <T> String Obj2xml(T value) {
        try {
            return XML_MAPPER.writeValueAsString(value);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("对象转xml异常", e);
        }
    }

    public static <T> String map2xml(T value, String rootName) {
        try {
            return XML_MAPPER.writer().withRootName(rootName).writeValueAsString(value);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("对象转xml异常", e);
        }
    }
}
