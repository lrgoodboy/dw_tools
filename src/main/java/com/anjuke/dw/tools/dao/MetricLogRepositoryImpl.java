package com.anjuke.dw.tools.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.time.DateUtils;

import com.anjuke.dw.tools.model.MetricLog;

public class MetricLogRepositoryImpl implements MetricLogRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<MetricLog> findToday(long metricId, boolean asc, int limit) {

        Date begin = DateUtils.truncate(new Date(), Calendar.DATE);
        Date end = DateUtils.addSeconds(DateUtils.addDays(begin, 1), -1);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<MetricLog> query = builder.createQuery(MetricLog.class);
        Root<MetricLog> metricLog = query.from(MetricLog.class);

        query.where(
            builder.equal(metricLog.get("metricId"), metricId),
            builder.greaterThanOrEqualTo(metricLog.<Date>get("created"), begin),
            builder.lessThanOrEqualTo(metricLog.<Date>get("created"), end)
        );

        if (asc) {
            query.orderBy(builder.asc(metricLog.get("created")));
        } else {
            query.orderBy(builder.desc(metricLog.get("created")));
        }

        TypedQuery<MetricLog> q = entityManager.createQuery(query);
        q.setMaxResults(limit);

        return q.getResultList();
    }

}
