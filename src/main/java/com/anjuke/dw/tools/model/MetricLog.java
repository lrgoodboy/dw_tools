package com.anjuke.dw.tools.model;

import java.util.Date;

public class MetricLog {
    private Long id;
    private Long metricId;
    private Long data;
    private Date created;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getMetricId() {
        return metricId;
    }
    public void setMetricId(Long metricId) {
        this.metricId = metricId;
    }
    public Long getData() {
        return data;
    }
    public void setData(Long data) {
        this.data = data;
    }
    public Date getCreated() {
        return created;
    }
    public void setCreated(Date created) {
        this.created = created;
    }
}
