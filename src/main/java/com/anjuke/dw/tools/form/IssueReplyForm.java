package com.anjuke.dw.tools.form;

import javax.validation.constraints.NotNull;


public class IssueReplyForm {
    private String content;
    @NotNull
    private Boolean status;
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Boolean getStatus() {
        return status;
    }
    public void setStatus(Boolean status) {
        this.status = status;
    }
}
