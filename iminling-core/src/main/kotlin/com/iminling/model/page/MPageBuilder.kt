package com.iminling.model.page

import com.baomidou.mybatisplus.core.metadata.IPage

/**
 * mybatis-plus构建pagination
 * @author yslao@outlook.com
 * @since 2021/9/25
 */
class MPageBuilder {

    companion object {
        fun <T> builder(page: IPage<T>): Pagination<T> {
            var pagination = Pagination<T>()
            pagination.pageModel.pageNum = page.current
            pagination.pageModel.pages = page.pages
            pagination.pageModel.pageSize = page.size
            pagination.pageModel.total = page.total
            pagination.list = page.records
            return pagination
        }
    }

}