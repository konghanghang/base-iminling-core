package com.iminling.core.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author yslao@outlook.com
 * @since 2021/2/19
 */
@ConfigurationProperties(prefix = "knife4j.info")
class Knife4jApiInfoProperties {

    var description:String = ""

    var serviceUrl:String = ""

    var contactName:String = ""

    var contactUrl:String = ""

    var contactEmail:String = ""

    var version:String = "1.0"

    var groupName:String = ""

    var enable:Boolean = true

    var basePackage:String = ""

    var packages:MutableList<String> = mutableListOf()

    var excludePackages:MutableList<String> = mutableListOf()

}