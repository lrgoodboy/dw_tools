package com.anjuke.dw.tools.form;

import com.anjuke.dw.tools.model.Issue;

public class IssueFilterForm {
    private Integer status = Issue.STATUS_OPENED;
    private String sort = "updated,desc";
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public String getSort() {
        return sort;
    }
    public void setSort(String sort) {
        this.sort = sort;
    }
}
