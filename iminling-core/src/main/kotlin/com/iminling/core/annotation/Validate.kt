package com.iminling.core.annotation

import com.iminling.core.validation.group.ValidateType
import java.lang.annotation.*
import java.lang.annotation.Retention
import java.lang.annotation.Target
import kotlin.reflect.KClass

/**
 * @author  yslao@outlook.com
 * @since  2021/11/28
 */
@Target(ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
annotation class Validate(val value: Array<KClass<out ValidateType>> = [])
