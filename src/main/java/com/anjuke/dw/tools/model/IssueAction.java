package com.anjuke.dw.tools.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class IssueAction {

    public static final int ACTION_OPEN = 1;
    public static final int ACTION_CLOSE = 2;
    public static final int ACTION_REOPEN = 3;
    public static final int ACTION_REPLY = 4;
    public static final int ACTION_ASIGN = 5;

    @Id @GeneratedValue
    private Long id;
    private Long issueId;
    private Long operatorId;
    private Integer action;
    private String details;
    @Column(updatable = false)
    private Date created;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getIssueId() {
        return issueId;
    }
    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }
    public Long getOperatorId() {
        return operatorId;
    }
    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }
    public Integer getAction() {
        return action;
    }
    public void setAction(Integer action) {
        this.action = action;
    }
    public String getDetails() {
        return details;
    }
    public void setDetails(String details) {
        this.details = details;
    }
    public Date getCreated() {
        return created;
    }
    public void setCreated(Date created) {
        this.created = created;
    }

}
