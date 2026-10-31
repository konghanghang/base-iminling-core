package com.iminling.core.page;

public class PageResp<T> {
    public PageResp() {}

    public PageResp(PageModel model) {
        this.pageModel = model;
    }

    private PageModel pageModel = new PageModel();
    private java.util.List<T> list = java.util.List.of();

    public PageModel getPageModel() {
        return pageModel;
    }

    public void setPageModel(PageModel pageModel) {
        this.pageModel = pageModel;
    }

    public java.util.List<T> getList() {
        return list;
    }

    public void setList(java.util.List<T> list) {
        this.list = list;
    }
}
