package com.iminling.core.config.mybatis;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * mybatis-plus参数填充
 * @author yslao@outlook.com
 * @since 2021/8/14
 */
public class CustomizeMetaObjectHandler implements MetaObjectHandler {

    private final String CREATE_TIME_FIELD = "createTime";
    private final String UPDATE_TIME_FIELD = "updateTime";
    private final String ID_FIELD = "id";

    @Override
    public void insertFill(MetaObject metaObject) {
        Object idVal = getFieldValByName(ID_FIELD, metaObject);
        if (Objects.isNull(idVal) || "0".equals(idVal)) {
            TableInfo tableInfo = findTableInfo(metaObject);
            IdType idType = tableInfo.getIdType();
            if (idType.equals(IdType.NONE)) {
                // 后续处理
            }
        }
        Object createTimeVal = getFieldValByName(CREATE_TIME_FIELD, metaObject);
        if (Objects.isNull(createTimeVal)) {
            this.strictInsertFill(metaObject, CREATE_TIME_FIELD, LocalDateTime.class, LocalDateTime.now());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Object updateTimeVal = getFieldValByName(UPDATE_TIME_FIELD, metaObject);
        if (Objects.isNull(updateTimeVal)) {
            this.strictUpdateFill(metaObject, UPDATE_TIME_FIELD, LocalDateTime.class, LocalDateTime.now());
        }
    }
}
