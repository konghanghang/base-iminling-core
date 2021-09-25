package com.iminling.model.page

import com.querydsl.core.QueryResults
import kotlin.math.ceil

/**
 * queryDsl-jpa构建pagination
 * @author yslao@outlook.com
 * @since 2021/9/25
 */
class JPageBuilder {

    companion object {
        fun <T> build(fetchResults: QueryResults<T>, pageModel: PageModel): Pagination<T> {
            var page = Pagination<T>(pageModel)
            pageModel.total = fetchResults.total
            pageModel.pages = ceil(pageModel.total.toDouble()/pageModel.pageSize).toLong()
            page.list = fetchResults.results
            return page
        }
    }

}