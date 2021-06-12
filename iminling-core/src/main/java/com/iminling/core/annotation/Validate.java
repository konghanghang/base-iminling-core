package com.iminling.core.annotation;

import com.iminling.core.validation.group.ValidateType;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Validate {

    Class<? extends ValidateType>[] value() default {};

}
