package com.anjuke.dw.tools.model;

public class MetricLogPoint {
    private String id;
    private Long x;
    private Long y = 0L;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public Long getX() {
        return x;
    }
    public void setX(Long x) {
        this.x = x;
    }
    public Long getY() {
        return y;
    }
    public void setY(Long y) {
        this.y = y;
    }
}
