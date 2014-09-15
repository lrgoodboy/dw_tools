package com.anjuke.dw.tools.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.anjuke.dw.tools.model.MetricLog;

public interface MetricLogRepository extends CrudRepository<MetricLog, Long> {
    List<MetricLog> findByMetricIdOrderByCreatedDesc(long metricId, Pageable pageable);
}
