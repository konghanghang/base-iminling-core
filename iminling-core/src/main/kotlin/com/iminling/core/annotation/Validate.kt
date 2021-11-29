package com.iminling.core.annotation

import com.iminling.core.validation.group.ValidateType
import kotlin.reflect.KClass

/**
 * @author  yslao@outlook.com
 * @since  2021/11/28
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Validate(val value: Array<KClass<out ValidateType>> = [])
