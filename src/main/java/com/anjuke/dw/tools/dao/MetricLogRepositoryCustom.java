package com.anjuke.dw.tools.dao;

import java.util.List;

import com.anjuke.dw.tools.model.MetricLog;

public interface MetricLogRepositoryCustom {
    List<MetricLog> findToday(long metricId, boolean desc, int limit);
}
