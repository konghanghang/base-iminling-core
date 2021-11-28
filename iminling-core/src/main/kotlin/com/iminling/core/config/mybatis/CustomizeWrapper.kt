package com.iminling.core.config.mybatis

import com.google.common.base.CaseFormat
import org.apache.ibatis.reflection.MetaObject
import org.apache.ibatis.reflection.wrapper.MapWrapper

/**
 * 查询结果为map时，key转为驼峰
 * @author  yslao@outlook.com
 * @since  2021/11/28
 */
class CustomizeWrapper(metaObject: MetaObject, map: Map<String, Any?>?) :
    MapWrapper(metaObject, map) {

    override fun findProperty(name: String, useCamelCaseMapping: Boolean): String {
        return if (useCamelCaseMapping) {
            //CaseFormat是引用的 guava库,里面有转换驼峰的,免得自己重复造轮子,pom添加
            CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name)
        } else name
    }

}