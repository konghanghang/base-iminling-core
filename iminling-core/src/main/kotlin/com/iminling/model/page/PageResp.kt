package com.iminling.model.page

/**
 * 返回前端的分页对象
 * @author yslao@outlook.com
 * @since 2021/9/25
 */
class PageResp<T> constructor() {

    constructor(pageModel: PageModel) : this() {
        this.pageModel = pageModel
    }

    /**
     * 分页信息
     */
    var pageModel: PageModel = PageModel()

    /**
     * 查询数据列表
     */
    var list: List<T> = emptyList()

}