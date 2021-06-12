package com.iminling.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface EnableResolve {

    ResolveStrategy value() default ResolveStrategy.ALL;

    enum ResolveStrategy{
        NONE,
        ARGUMENTS,
        RETURN_VALUE,
        ALL
    }

}
