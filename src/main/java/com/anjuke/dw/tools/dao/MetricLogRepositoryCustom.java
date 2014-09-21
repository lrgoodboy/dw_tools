package com.anjuke.dw.tools.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.anjuke.dw.tools.model.MetricLog;

public interface MetricLogRepositoryCustom {
    List<MetricLog> findRange(long metricId, Date begin, Date end, Pageable pageable);
}
