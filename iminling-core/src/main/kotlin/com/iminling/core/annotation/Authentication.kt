package com.iminling.core.annotation

/**
 * @author  yslao@outlook.com
 * @since  2021/11/28
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
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
