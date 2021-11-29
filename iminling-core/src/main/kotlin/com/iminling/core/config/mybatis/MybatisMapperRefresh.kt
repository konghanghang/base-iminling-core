package com.iminling.core.config.mybatis

import com.google.common.collect.Lists
import org.apache.ibatis.binding.MapperRegistry
import org.apache.ibatis.builder.xml.XMLMapperBuilder
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver
import org.apache.ibatis.executor.ErrorContext
import org.apache.ibatis.executor.keygen.SelectKeyGenerator
import org.apache.ibatis.io.Resources
import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.parsing.XNode
import org.apache.ibatis.parsing.XPathParser
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSessionFactory
import org.slf4j.LoggerFactory
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.util.ResourceUtils
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * mybatis热加载mapper
 * @author  yslao@outlook.com
 * @since  2021/11/29
 */
class MybatisMapperRefresh {

    private val logger = LoggerFactory.getLogger(MybatisMapperRefresh::class.java)

    /**
     * 记录jar包存在的mapper
     */
    private val jarMapper = mutableMapOf<String, List<Resource>>()
    private val sqlSessionFactory: SqlSessionFactory
    private val mapperLocations: Array<Resource>
    private val configuration: Configuration

    /**
     * 记录文件最后更新时间
     */
    private val file2UpdateTime = mutableMapOf<String, Long>()

    /**
     * xml文件目录
     */
    private val fileSet: Set<String> = mutableSetOf()

    /**
     * 延迟加载时间
     */
    private val delaySeconds = 10

    /**
     * 刷新间隔时间
     */
    private var sleepSeconds = 20

    constructor(
        mapperLocations: Array<Resource>,
        sqlSessionFactory: SqlSessionFactory,
        sleepSeconds: Int
    ) {
        this.mapperLocations = mapperLocations.clone()
        this.sqlSessionFactory = sqlSessionFactory
        this.sleepSeconds = sleepSeconds
        configuration = sqlSessionFactory.configuration
        this.startRefresh()
    }

    constructor(mapperLocations: Array<Resource>, sqlSessionFactory: SqlSessionFactory) {
        this.mapperLocations = mapperLocations.clone()
        this.sqlSessionFactory = sqlSessionFactory
        configuration = sqlSessionFactory.configuration
        this.startRefresh()
    }

    /**
     * 启动刷新功能
     */
    private fun startRefresh() {
        logger.info("start refresh mapper.")
        // final GlobalConfig globalConfig = GlobalConfigUtils.getGlobalConfig(configuration);
        /*
         * 启动 XML 热加载
         */
        var beforeTime = System.currentTimeMillis()
        thread(name = "mybatis refreshMapper") {
            if (fileSet.isEmpty()) {
                mapperLocations.forEach {
                    if (ResourceUtils.isJarURL(it.url)) {
                        val key = UrlResource(ResourceUtils.extractJarFileURL(it.url))
                            .file.path
                        fileSet.plus(key)
                        file2UpdateTime[key] = beforeTime
                        if (jarMapper[key] != null) {
                            jarMapper[key]?.plus(it)
                        } else {
                            val resourcesList: MutableList<Resource> = ArrayList()
                            resourcesList.add(it)
                            jarMapper[key] = resourcesList
                        }
                    } else {
                        fileSet.plus(it.file.path)
                        file2UpdateTime[it.file.path] = beforeTime
                    }
                }
            }
            do {
                for (filePath in fileSet) {
                    val file = File(filePath)
                    val lastModified = file2UpdateTime[filePath]!!
                    if (file.isFile && file.lastModified() > lastModified) {
                        file2UpdateTime[filePath] = file.lastModified()
                        val removeList = jarMapper[filePath]
                        if (removeList != null && removeList.isNotEmpty()) {
                            for (resource in removeList) {
                                refresh(resource)
                            }
                        } else {
                            refresh(FileSystemResource(file))
                        }
                    }
                }
                TimeUnit.SECONDS.sleep(sleepSeconds.toLong())
            } while (true)
        }
    }

    private fun refresh(resource: Resource) {
        val isSupper = configuration.javaClass.superclass == Configuration::class.java
        try {
            val loadedResourcesField =
                if (isSupper) configuration.javaClass.superclass.getDeclaredField("loadedResources") else configuration.javaClass.getDeclaredField(
                    "loadedResources"
                )
            loadedResourcesField.isAccessible = true
            val loadedResourcesSet = loadedResourcesField[configuration] as MutableSet<*>
            val xPathParser = XPathParser(
                resource.inputStream, true, configuration.variables,
                XMLMapperEntityResolver()
            )
            val context = xPathParser.evalNode("/mapper")
            val namespace = context.getStringAttribute("namespace")
            val field = MapperRegistry::class.java.getDeclaredField("knownMappers")
            field.isAccessible = true
            val mapConfig = field[configuration.mapperRegistry] as MutableMap<*, *>
            // Collection<String> mappedStatementNames = configuration.getMappedStatementNames();
            mapConfig.remove(Resources.classForName(namespace))
            loadedResourcesSet.remove(resource.toString())
            configuration.cacheNames.remove(namespace)
            cleanParameterMap(context.evalNodes("/mapper/parameterMap"), namespace)
            cleanResultMap(context.evalNodes("/mapper/resultMap"), namespace)
            cleanKeyGenerators(context.evalNodes("insert|update|select"), namespace)
            cleanSqlElement(context.evalNodes("/mapper/sql"), namespace)
            val xmlMapperBuilder = XMLMapperBuilder(
                resource.inputStream,
                sqlSessionFactory.configuration,
                resource.toString(), sqlSessionFactory.configuration.sqlFragments
            )
            xmlMapperBuilder.parse()
            logger.info("refresh: '$resource', success!")
        } catch (e: Exception) {
            logger.error("Refresh Exception :" + e.message)
        } finally {
            ErrorContext.instance().reset()
        }
    }

    /**
     * 清理parameterMap
     *
     * @param list
     * @param namespace
     */
    private fun cleanParameterMap(list: List<XNode>, namespace: String) {
        for (parameterMapNode in list) {
            val id = parameterMapNode.getStringAttribute("id")
            var parameterMap = configuration.getParameterMap("$namespace.$id")
            configuration.parameterMaps.remove(parameterMap)
        }
    }

    /**
     * 清理resultMap
     *
     * @param list
     * @param namespace
     */
    private fun cleanResultMap(list: List<XNode>, namespace: String) {
        for (resultMapNode in list) {
            val id = resultMapNode.getStringAttribute("id", resultMapNode.valueBasedIdentifier)
            configuration.resultMapNames.remove(id)
            configuration.resultMapNames.remove("$namespace.$id")
            clearResultMap(resultMapNode, namespace)
        }
    }

    private fun clearResultMap(xNode: XNode, namespace: String) {
        for (resultChild in xNode.children) {
            if ("association" == resultChild.name || "collection" == resultChild.name || "case" == resultChild.name) {
                if (resultChild.getStringAttribute("select") == null) {
                    configuration.resultMapNames.remove(
                        resultChild.getStringAttribute("id", resultChild.valueBasedIdentifier)
                    )
                    configuration.resultMapNames.remove(
                        namespace + "." + resultChild.getStringAttribute("id", resultChild.valueBasedIdentifier)
                    )
                    if (resultChild.children != null && resultChild.children.isNotEmpty()) {
                        clearResultMap(resultChild, namespace)
                    }
                }
            }
        }
    }

    /**
     * 清理selectKey
     *
     * @param list
     * @param namespace
     */
    private fun cleanKeyGenerators(list: List<XNode>, namespace: String) {
        for (context in list) {
            val id = context.getStringAttribute("id")
            configuration.keyGeneratorNames.remove(id + SelectKeyGenerator.SELECT_KEY_SUFFIX)
            configuration.keyGeneratorNames.remove(namespace + "." + id + SelectKeyGenerator.SELECT_KEY_SUFFIX)
            val mappedStatements = configuration.mappedStatements
            val objects: MutableList<MappedStatement> = Lists.newArrayList()
            val iterator: Iterator<MappedStatement> = mappedStatements.iterator()
            while (iterator.hasNext()) {
                val next: Any = iterator.next()
                // 跳过mybatis-plus自带的那些查询
                if (next is MappedStatement) {
                    if (next.id == "$namespace.$id") {
                        objects.add(next)
                    }
                }
            }
            mappedStatements.removeAll(objects.toSet())
        }
    }

    /**
     * 清理sql节点缓存
     *
     * @param list
     * @param namespace
     */
    private fun cleanSqlElement(list: List<XNode>, namespace: String) {
        for (context in list) {
            val id = context.getStringAttribute("id")
            configuration.sqlFragments.remove(id)
            configuration.sqlFragments.remove("$namespace.$id")
        }
    }
}