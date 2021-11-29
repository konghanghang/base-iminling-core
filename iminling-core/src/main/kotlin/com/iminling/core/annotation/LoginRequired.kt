package com.iminling.core.annotation

import java.lang.annotation.*

/**
 * @author  yslao@outlook.com
 * @since  2021/11/28
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Inherited
annotation class LoginRequired()
