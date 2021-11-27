package com.iminling.core.util

import com.iminling.core.log.ClientInfo
import java.util.*

/**
 * @author  yslao@outlook.com
 * @since  2021/11/26
 */
class ThreadContext {

    companion object {
        private val CLIENT_INFO = ThreadLocal<ClientInfo>()
        fun getClientInfo(): ClientInfo? {
            return CLIENT_INFO.get()
        }
        fun setClientInfo(clientInfo: ClientInfo) {
            CLIENT_INFO.set(clientInfo)
        }

        private val PROPERTIES = ThreadLocal<MutableMap<String, Any>>()
        fun getAttribute(key: String): Any? {
            val map = PROPERTIES.get()
            return if (Objects.isNull(map)) {
                null
            } else map[key]
        }

        fun setAttribute(key: String, value: Any) {
            var map = PROPERTIES.get()
            if (Objects.isNull(map)) {
                map = mutableMapOf()
            }
            map[key] = value
        }

        fun clear() {
            CLIENT_INFO.remove()
            PROPERTIES.remove()
        }
    }

}