package com.anjuke.dw.tools.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Issue {

    public static final int STATUS_OPENED = 1;
    public static final int STATUS_CLOSED = 2;

    @Id @GeneratedValue
    private Long id;
    private String title;
    private String content;
    private Integer status;
    private Long creatorId;
    private Long asigneeId;
    private Long replierId;
    private Integer replyCount;
    private Date replied;
    @Column(updatable = false)
    private Date created;
    @Column(insertable = false, updatable = false)
    private Date updated;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public Long getCreatorId() {
        return creatorId;
    }
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
    public Long getAsigneeId() {
        return asigneeId;
    }
    public void setAsigneeId(Long asigneeId) {
        this.asigneeId = asigneeId;
    }
    public Long getReplierId() {
        return replierId;
    }
    public void setReplierId(Long replierId) {
        this.replierId = replierId;
    }
    public Integer getReplyCount() {
        return replyCount;
    }
    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }
    public Date getReplied() {
        return replied;
    }
    public void setReplied(Date replied) {
        this.replied = replied;
    }
    public Date getCreated() {
        return created;
    }
    public void setCreated(Date created) {
        this.created = created;
    }
    public Date getUpdated() {
        return updated;
    }
    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}
