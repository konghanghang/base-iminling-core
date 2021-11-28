package com.iminling.core.annotation

import java.lang.annotation.*
import java.lang.annotation.Retention
import java.lang.annotation.Target

/**
 * @author  yslao@outlook.com
 * @since  2021/11/28
 */
@Target(ElementType.METHOD, ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
annotation class LoginRequired()
