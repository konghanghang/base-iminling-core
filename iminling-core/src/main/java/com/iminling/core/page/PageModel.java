package com.iminling.core.page;

public class PageModel {

    /** 每页条数，默认为10 */
    private long pageSize = 10;

    /** 总记录数 */
    private long total = 0;

    /** 总页数 */
    private long pages = 0;

    /** 当前页码数，默认为1 */
    private long pageNum = 1;

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }

    public long getPageNum() {
        return pageNum;
    }

    public void setPageNum(long pageNum) {
        this.pageNum = pageNum;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public long getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
