package com.iminling.core.config.mybatis

import org.apache.ibatis.cache.CacheKey
import org.apache.ibatis.executor.Executor
import org.apache.ibatis.mapping.BoundSql
import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.plugin.Interceptor
import org.apache.ibatis.plugin.Intercepts
import org.apache.ibatis.plugin.Invocation
import org.apache.ibatis.plugin.Signature
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.ResultHandler
import org.apache.ibatis.session.RowBounds
import org.slf4j.LoggerFactory


/**
 *
 * @author yslao@outlook.com
 * @since 2021/12/4
 */
@Intercepts(
    Signature(type = Executor::class, method = "update", args = [MappedStatement::class, Object::class]),
    Signature(type = Executor::class, method = "query", args = [MappedStatement::class, Object::class,
        RowBounds::class, ResultHandler::class, CacheKey::class, BoundSql::class]),
    Signature(type = Executor::class, method = "query", args = [MappedStatement::class, Object::class, RowBounds::class,
        ResultHandler::class])
)
class SqlInterceptor: Interceptor {

    private val logger = LoggerFactory.getLogger(SqlInterceptor::class.java)

    override fun intercept(invocation: Invocation): Any {
        // 耗时开始时间
        val startTime = System.currentTimeMillis()
        var mappedStatement = invocation.args[0] as MappedStatement
        var parameter: Any? = null;
        if (invocation.args.size > 1) {
            //获得查询方法的参数，比如selectById(Integer id,String name)，那么就可以获取到四个参数分别是：
            //{id:1,name:"user1",param1:1,param2:"user1"}
            parameter = invocation.args[1];
        }
        // 获取sql
        var boundSql = mappedStatement.getBoundSql(parameter)
        var sql = boundSql.sql.replace("[\\s\n ]+".toRegex(), " ")
        // val sql: String = showSql(mappedStatement.configuration, mappedStatement.getBoundSql(parameterObject))
        var params = getParams(mappedStatement.configuration, boundSql)
        // 获取执行sql方法
        val sqlId = mappedStatement.id
        // 执行sql
        val result = invocation.proceed()
        // 计算总耗时
        val cost = System.currentTimeMillis() - startTime
        logger.info(" ======> SQL方法 : {} , SQL语句 : {}, 总耗时 : {}毫秒,  参数 ：{}", sqlId, sql, cost, params)
        return result

    }

    private fun getParams(configuration: Configuration, boundSql: BoundSql): String {
        // 获取参数
        val parameterObject = boundSql.parameterObject ?: return ""
        val parameterMappings = boundSql.parameterMappings
        // 获取类型处理器注册器，类型处理器的功能是进行java类型和数据库类型的转换
        val typeHandlerRegistry = configuration.typeHandlerRegistry
        var sb = StringBuilder()
        if (typeHandlerRegistry.hasTypeHandler(parameterObject.javaClass)) {
            sb.append(parameterObject).append(",")
        } else {
            // MetaObject主要是封装了originalObject对象，提供了get和set的方法用于获取和设置originalObject的属性值,主要支持对JavaBean、Collection、Map三种类型对象的操作
            val metaObject = configuration.newMetaObject(parameterObject)
            for (parameterMapping in parameterMappings) {
                val propertyName = parameterMapping.property
                if (metaObject.hasGetter(propertyName)) {
                    val obj: Any? = metaObject.getValue(propertyName)
                    sb.append(obj).append(",")
                } else if (boundSql.hasAdditionalParameter(propertyName)) {
                    // 该分支是动态sql
                    val obj = boundSql.getAdditionalParameter(propertyName)
                    sb.append(obj).append(",")
                } else {
                    // 打印出缺失，提醒该参数缺失并防止错位
                    sb.append("缺失").append(",")
                }
            }
        }
        if (sb.length > 1) {
            sb.deleteCharAt(sb.length - 1)
        }
        return sb.toString()
    }

    /*private static String showSql(Configuration configuration, BoundSql boundSql) {
        // 获取参数
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        // sql语句中多个空格都用一个空格代替
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (CollectionUtils.isNotEmpty(parameterMappings) && parameterObject != null) {
            // 获取类型处理器注册器，类型处理器的功能是进行java类型和数据库类型的转换
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            // 如果根据parameterObject.getClass(）可以找到对应的类型，则替换
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(parameterObject)));
            } else {
                // MetaObject主要是封装了originalObject对象，提供了get和set的方法用于获取和设置originalObject的属性值,主要支持对JavaBean、Collection、Map三种类型对象的操作
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        // 该分支是动态sql
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                    } else {
                        // 打印出缺失，提醒该参数缺失并防止错位
                        sql = sql.replaceFirst("\\?", "缺失");
                    }
                }
            }
        }

        return sql;
    }


    private static String getParameterValue(Object obj) {
        String value;

        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        }
        else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(new Date()) + "'";
        }
        else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }
        }

        return value;
    }*/

}