package com.anjuke.dw.tools.form;

import org.springframework.data.domain.Sort;

import com.anjuke.dw.tools.model.Issue;

public class IssueFilterForm {

    private Integer status = Issue.STATUS_OPENED;
    private String sort = "updated,desc";

    public Sort parseSort() {
        if (sort.equals("created,asc")) {
            return new Sort(Sort.Direction.ASC, "created");
        } else if (sort.equals("created,desc")) {
            return new Sort(Sort.Direction.DESC, "created");
        } else if (sort.equals("updated,asc")) {
            return new Sort(Sort.Direction.ASC, "updated");
        } else {
            sort = "updated,desc";
            return new Sort(Sort.Direction.DESC, "updated");
        }
    }

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
