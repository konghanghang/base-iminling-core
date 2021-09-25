package com.iminling.model.page

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
    @ApiModelProperty(value = "总记录数,多余参数不用传")
    var total: Long = 0

    /**
     * 总页数
     */
    @ApiModelProperty(value = "总页数,多余参数不用传")
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
    fun getOffset(): Long {
        return (pageNum - 1) * pageSize
    }

}