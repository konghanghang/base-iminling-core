package com.iminling.common.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.iminling.common.date.DateUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final XmlMapper XML_MAPPER = createXmlMapper();

    private JsonUtil(){}

    static {
        // 设置日期对象的输出格式
        // objectMapper.setDateFormat(new SimpleDateFormat(DateUtils.DEFAULT_DATE_TIME_FORMAT, Locale.CHINESE));
        // 设置输入时忽略在json字符串中存在 但在java对象实际没有的属性
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 允许备注
        OBJECT_MAPPER.enable(JsonParser.Feature.ALLOW_COMMENTS);
        // objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DateUtils.DEFAULT_DATE_TIME_FORMAT)));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DateUtils.DEFAULT_DATE_FORMAT)));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DateUtils.DEFAULT_TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DateUtils.DEFAULT_DATE_TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DateUtils.DEFAULT_DATE_FORMAT)));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DateUtils.DEFAULT_TIME_FORMAT)));
        OBJECT_MAPPER.registerModule(javaTimeModule);
    }

    public static XmlMapper createXmlMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        xmlMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        xmlMapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        xmlMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        xmlMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.enable(MapperFeature.USE_STD_BEAN_NAMING);
        // 是否添加<?xml version='1.0' encoding='UTF-8'?>
        // xmlMapper.enable(Feature.WRITE_XML_DECLARATION);
        return xmlMapper;
    }

    /**
     * 获取objectMapper
     * @return ObjectMapper
     */
    public static ObjectMapper getInstant(){
        return OBJECT_MAPPER;
    }

    /**
     * 判断 对象是否为null
     * https://blog.csdn.net/qq_35566813/article/details/90914062
     * @param object 对象
     * @return 是否为null
     */
    public static boolean objIsNull(Object object) {
        if (object == null) {
            return true;
        }
        // 得到类对象
        Class clazz = object.getClass();
        // 得到所有属性
        Field[] fields = clazz.getDeclaredFields();
        //定义返回结果，默认为true
        boolean flag = true;
        for (Field field : fields) {
            //设置权限
            field.setAccessible(true);
            Object fieldValue = null;
            String fieldName = null;
            try {
                //得到属性值
                fieldValue = field.get(object);
                //得到属性类型
                Type fieldType = field.getGenericType();
                //得到属性名
                fieldName = field.getName();
                //打印输出(调试用可忽略)
                /*if (objectNullSystemOutFlag == 1) {
                    System.out.println("属性类型：" + fieldType + ",属性名：" + fieldName + ",属性值：" + fieldValue);
                }*/
            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOGGER.error("get field value error!");
            }
            //只要有一个属性值不为null 就返回false 表示对象不为null
            if (fieldValue != null && !"serialVersionUID".equals(fieldName)) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    /**
     * 对象转string
     * @param obj 对象
     * @param <T> 泛型
     * @return 字符串
     */
    public static <T> String obj2Str(T obj){
        if(objIsNull(obj)){
            return null;
        }
        try {
            return obj instanceof String ? (String)obj :  OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            LOGGER.warn("Parse Object to String error",e);
            return null;
        }
    }

    /**
     * 对象转string 格式化输出
     * @param obj 对象
     * @param <T> 泛型
     * @return 字符串
     */
    public static <T> String obj2StrPretty(T obj){
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String)obj :  OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            LOGGER.warn("Parse Object to String error",e);
            return null;
        }
    }

    /**
     * 字符串转对象
     * @param str 字符串
     * @param clazz 对象
     * @param <T> 泛型
     * @return 对象
     */
    public static <T> T str2Obj(String str, Class<T> clazz){
        if(StringUtils.isEmpty(str) || clazz == null){
            return null;
        }
        try {
            return clazz.equals(String.class)? (T)str : OBJECT_MAPPER.readValue(str, clazz);
        } catch (Exception e) {
            LOGGER.warn("Parse String to Object error",e);
            return null;
        }
    }

    /**
     * 字符串转List对象
     * @param str json字符串
     * @param collectionClass 集合类
     * @param elementClasses 集合元素类
     * @param <T> 泛型
     * @return 集合对象
     */
    public static <T> T str2Obj(String str, Class<?> collectionClass, Class<?>... elementClasses){
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(collectionClass,elementClasses);
        try {
            return OBJECT_MAPPER.readValue(str,javaType);
        } catch (Exception e) {
            LOGGER.warn("Parse String to Object error",e);
            return null;
        }
    }

    /**
     * 把一个对象转成另一个对象
     * @param pojo 对象
     * @param target 目标对象
     * @param <T> 泛型
     * @return 目标对象
     */
    public static <T> T convert(Object pojo, Class<T> target) {
        if (Objects.isNull(pojo)) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(pojo, target);
    }

    /**
     * 把数组对象转成另一个类型的数组对象
     * @param pojo  对象
     * @param collectionClass 集合类
     * @param elementClass 集合元素类
     * @param <T> 泛型
     * @return 集合对象
     */
    public static <T> T convert(Object pojo, Class<?> collectionClass, Class<?>... elementClass) {
        if (Objects.isNull(pojo)) {
            return null;
        }
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(collectionClass, elementClass);
        return OBJECT_MAPPER.convertValue(pojo, javaType);
    }

    /**
     * 把一个对象转成Type对应的对象
     * @param pojo 对象
     * @param type 目标对象类型
     * @param <T> 泛型
     * @return 目标对象
     */
    public static <T> T convert(Object pojo, Type type) {
        if (Objects.isNull(pojo)) {
            return null;
        }
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructType(type);
        return OBJECT_MAPPER.convertValue(pojo, javaType);
    }

    public static <T> T xml2Obj(String xmlStr, Class<T> clazz) {
        try {
            return XML_MAPPER.readValue(xmlStr, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化xml异常,xml:" + xmlStr, e);
        }
    }

    public static <T> String Obj2xml(T obj) {
        try {
            return XML_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象转xml异常,xml。", e);
        }
    }

}
