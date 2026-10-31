package com.iminling.core.config.mybatis;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Intercepts({
    @Signature(
            type = Executor.class,
            method = "update",
            args = {MappedStatement.class, Object.class}),
    @Signature(
            type = Executor.class,
            method = "query",
            args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(
            type = Executor.class,
            method = "query",
            args = {
                MappedStatement.class,
                Object.class,
                RowBounds.class,
                ResultHandler.class,
                CacheKey.class,
                BoundSql.class
            })
})
public class SqlInterceptor implements Interceptor {
    private static final Logger LOG = LoggerFactory.getLogger(SqlInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        MappedStatement statement = (MappedStatement) invocation.getArgs()[0];
        BoundSql sql =
                invocation.getArgs().length == 6
                        ? (BoundSql) invocation.getArgs()[5]
                        : statement.getBoundSql(invocation.getArgs()[1]);
        try {
            return invocation.proceed();
        } finally {
            LOG.info(
                    "SQL方法:{}, SQL语句:{}, 总耗时:{}毫秒, 参数:{}",
                    statement.getId(),
                    sql.getSql().replaceAll("\\s+", " "),
                    System.currentTimeMillis() - start,
                    getParams(statement.getConfiguration(), sql));
        }
    }

    private String getParams(Configuration configuration, BoundSql sql) {
        Object parameter = sql.getParameterObject();
        if (parameter == null) return "";
        if (configuration.getTypeHandlerRegistry().hasTypeHandler(parameter.getClass()))
            return parameter.toString();
        var meta = configuration.newMetaObject(parameter);
        var values = new java.util.ArrayList<String>();
        for (var mapping : sql.getParameterMappings()) {
            String name = mapping.getProperty();
            Object value =
                    sql.hasAdditionalParameter(name)
                            ? sql.getAdditionalParameter(name)
                            : meta.hasGetter(name) ? meta.getValue(name) : "缺失";
            values.add(String.valueOf(value));
        }
        return String.join(",", values);
    }
}
