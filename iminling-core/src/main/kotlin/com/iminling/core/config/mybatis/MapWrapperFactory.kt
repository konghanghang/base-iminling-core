package com.iminling.core.config.mybatis

import org.apache.ibatis.reflection.MetaObject
import org.apache.ibatis.reflection.wrapper.ObjectWrapper
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory

/**
 * https://my.oschina.net/u/2278977/blog/1795969
 * @author  yslao@outlook.com
 * @since  2021/11/28
 */
class MapWrapperFactory : ObjectWrapperFactory {

    override fun hasWrapperFor(`object`: Any?): Boolean {
        return `object` != null && `object` is Map<*, *>?
    }

    override fun getWrapperFor(metaObject: MetaObject?, `object`: Any?): ObjectWrapper {
        return CustomizeWrapper(metaObject!!, `object` as Map<String, Any?>?)
    }
}