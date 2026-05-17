package com.myforum.model.dto;

import lombok.Data;

@Data
public class PageQuery {
    private Integer page = 1;
    private Integer pageSize = 20;

    public int getOffset() {
        return (page - 1) * pageSize;
    }

    public void setPage(Integer page) {
        if (page == null || page < 1) this.page = 1;
        else this.page = page;
    }

    public void setPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) this.pageSize = 20;
        else if (pageSize > 50) this.pageSize = 50;
        else this.pageSize = pageSize;
    }
}
