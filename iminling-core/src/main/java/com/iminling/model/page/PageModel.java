package com.iminling.model.page;

import lombok.Data;

/**
 * 分页参数
 * @author yslao@outlook.com
 */
@Data
public class PageModel {

    /**
     * 页大小
     */
    private long pageSize = 10;

    /**
     * 总记录数
     */
    private long total = 0;

    /**
     * 总页数
     */
    private long pages = 0;

    /**
     * 当前页码数
     */
    private long pageNum = 1;
}
