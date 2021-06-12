package com.iminling.model.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class Pagination<T> {

    /**
     * 分页信息
     */
    private PageModel pageModel;

    /**
     * 查询数据列表
     */
    private List<T> list = Collections.emptyList();


    private Pagination(){
        this.pageModel = new PageModel();
    }

    /**
     * 分页构造函数
     *
     * @param page page接口对象
     */
    public Pagination(IPage<T> page) {
        this();
        this.list = page.getRecords();
        this.pageModel.setPageNum(page.getCurrent());
        this.pageModel.setPages(page.getPages());
        this.pageModel.setPageSize(page.getSize());
        this.pageModel.setTotal(page.getTotal());
    }
}
