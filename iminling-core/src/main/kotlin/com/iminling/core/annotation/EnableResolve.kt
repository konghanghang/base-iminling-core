package com.iminling.core.annotation

import com.iminling.core.constant.ResolveStrategy

/**
 * @author  yslao@outlook.com
 * @since  2021/11/28
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@MustBeDocumented
annotation class EnableResolve(val value: ResolveStrategy = ResolveStrategy.RETURN_VALUE)
