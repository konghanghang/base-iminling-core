package com.iminling.core.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@LoginRequired
public @interface Authentication {
    String[] roles();

    String[] permissions();
}
