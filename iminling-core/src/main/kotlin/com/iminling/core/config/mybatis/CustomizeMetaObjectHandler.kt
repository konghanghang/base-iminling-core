package com.iminling.core.config.mybatis

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler
import org.apache.ibatis.reflection.MetaObject
import java.time.LocalDateTime
import java.util.*

/**
 * mybatis-plus参数填充
 * @author yslao@outlook.com
 * @since 2021/8/14
 */
class CustomizeMetaObjectHandler : MetaObjectHandler {
    private val CREATE_TIME_FIELD = "createTime"
    private val UPDATE_TIME_FIELD = "updateTime"
    private val ID_FIELD = "id"

    override fun insertFill(metaObject: MetaObject) {
        val idVal = getFieldValByName(ID_FIELD, metaObject)
        if (Objects.isNull(idVal) || "0" == idVal) {
            val tableInfo = findTableInfo(metaObject)
            val idType = tableInfo.idType
            if (idType == IdType.NONE) {
                // 后续处理
            }
        }
        val createTimeVal = getFieldValByName(CREATE_TIME_FIELD, metaObject)
        if (Objects.isNull(createTimeVal)) {
            this.strictInsertFill(
                metaObject, CREATE_TIME_FIELD,
                LocalDateTime::class.java, LocalDateTime.now()
            )
        }
    }

    override fun updateFill(metaObject: MetaObject) {
        val updateTimeVal = getFieldValByName(UPDATE_TIME_FIELD, metaObject)
        if (Objects.isNull(updateTimeVal)) {
            this.strictUpdateFill(
                metaObject, UPDATE_TIME_FIELD,
                LocalDateTime::class.java, LocalDateTime.now()
            )
        }
    }
}