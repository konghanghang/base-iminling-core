package com.iminling.core.annotation

import com.iminling.core.constant.ResolveStrategy
import java.lang.annotation.*
import java.lang.annotation.Retention
import java.lang.annotation.Target

/**
 * @author  yslao@outlook.com
 * @since  2021/11/28
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE, ElementType.METHOD)
@Documented
annotation class EnableResolve(val value: ResolveStrategy = ResolveStrategy.RETURN_VALUE)
