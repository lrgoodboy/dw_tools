package com.anjuke.dw.tools.form;

import javax.validation.constraints.NotNull;


public class IssueReplyForm {
    private String content;
    @NotNull
    private Boolean close;
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Boolean getClose() {
        return close;
    }
    public void setClose(Boolean close) {
        this.close = close;
    }
}
