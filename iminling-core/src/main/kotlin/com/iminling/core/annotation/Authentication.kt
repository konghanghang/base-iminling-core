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
@LoginRequired
annotation class Authentication(

    /**
     * 角色
     */
    val roles: Array<String>,

    /**
     * 权限
     */
    val permissions: Array<String>
)
