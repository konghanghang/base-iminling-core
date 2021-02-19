package com.iminling.common.id.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.iminling.common.id.jackson.IdHashJsonDeserializerFactory;
import com.iminling.common.id.jackson.IdHashJsonSerializerFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yslao@outlook.com
 * @since 2020/11/13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@JacksonAnnotationsInside
// 指定序列化
@JsonSerialize(using = IdHashJsonSerializerFactory.class)
// 指定反序列化
@JsonDeserialize(using = IdHashJsonDeserializerFactory.class)
public @interface IdHash {
}
