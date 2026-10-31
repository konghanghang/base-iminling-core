package com.iminling.core.annotation;

import com.iminling.core.constant.ResolveStrategy;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableResolve {
    ResolveStrategy value() default ResolveStrategy.RETURN_VALUE;
}
