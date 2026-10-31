package com.iminling.core.page;

import com.baomidou.mybatisplus.core.metadata.IPage;

public final class MPageBuilder {
    private MPageBuilder() {}

    public static <T> PageResp<T> builder(IPage<T> page) {
        PageResp<T> result = new PageResp<>();
        PageModel model = result.getPageModel();
        model.setPageNum(page.getCurrent());
        model.setPages(page.getPages());
        model.setPageSize(page.getSize());
        model.setTotal(page.getTotal());
        result.setList(page.getRecords());
        return result;
    }
}
