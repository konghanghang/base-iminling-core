package com.iminling.core.page

import com.baomidou.mybatisplus.core.metadata.IPage

/**
 * mybatis-plus构建pagination
 * @author yslao@outlook.com
 * @since 2021/9/25
 */
class MPageBuilder {

    companion object {
        fun <T> builder(page: IPage<T>): PageResp<T> {
            var pageResp = PageResp<T>()
            pageResp.pageModel.pageNum = page.current
            pageResp.pageModel.pages = page.pages
            pageResp.pageModel.pageSize = page.size
            pageResp.pageModel.total = page.total
            pageResp.list = page.records
            return pageResp
        }
    }

}