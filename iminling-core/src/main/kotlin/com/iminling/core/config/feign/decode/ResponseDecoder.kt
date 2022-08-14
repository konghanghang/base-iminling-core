package com.iminling.core.config.feign.decode

import com.google.common.base.Strings
import com.iminling.common.json.JsonUtil
import com.iminling.core.config.value.ResultModel
import feign.Response
import feign.Util
import feign.codec.Decoder
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets

/**
 * 解码feign请求返回的resultModel对象
 * @author yslao@outlook.com
 * @since 2022/5/21
 */
class ResponseDecoder: Decoder {

    private val delegate: Decoder

    constructor(decoder: Decoder) {
        this.delegate = decoder
    }

    override fun decode(response: Response, type: Type): Any? {
        var rawType: Type = type
        if (type is ParameterizedType) {
            rawType = type.rawType
        }
        // 判断返回类型是否是ResultModel，如果是直接返回
        if (ResultModel::class.java.isAssignableFrom(rawType.javaClass)) {
            return delegate.decode(response, type)
        }
        if (response.body() == null) return null
        val body = Util.toString(response.body().asReader(StandardCharsets.UTF_8))
        if (Strings.isNullOrEmpty(body)) {
            return null
        }
        var result = JsonUtil.str2Obj(body, ResultModel::class.java)
        if (result.data == null) return null
        if (result.data is String) {
            return result.data
        }
        // 将返回结果拆分，获取data返回，方便转成方法中的返回对象类型
        var bytes = JsonUtil.getInstant().writeValueAsBytes(result.data)
        val wrapResponse = Response.builder()
            .headers(response.headers())
            .reason(response.reason())
            .request(response.request())
            .status(response.status())
            .body(bytes)
            .build()
        return delegate.decode(wrapResponse, type)
    }
}