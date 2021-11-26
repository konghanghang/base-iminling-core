package com.iminling.model.page

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.annotations.ApiModelProperty

/**
 * 分页参数
 * @author yslao@outlook.com
 * @since 2021/9/25
 */
class PageModel {

    /**
     * 每页条数
     */
    @ApiModelProperty(value = "每页条数,默认为10")
    var pageSize: Long = 10

    /**
     * 总记录数
     */
    @ApiModelProperty(value = "总记录数")
    var total: Long = 0

    /**
     * 总页数
     */
    @ApiModelProperty(value = "总页数")
    var pages: Long = 0

    /**
     * 当前页码数
     */
    @ApiModelProperty(value = "当前页码数,默认为1", notes = "默认为1")
    var pageNum: Long = 1

    /**
     * 获取offset, limit offset,pageSize
     * @return offset
     */
    @ApiModelProperty(hidden = true)
    @JsonIgnore
    var offset: Long = 0
        get() = (pageNum - 1) * pageSize

}