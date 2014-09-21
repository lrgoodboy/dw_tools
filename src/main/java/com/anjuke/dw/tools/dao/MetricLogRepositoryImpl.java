package com.anjuke.dw.tools.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Pageable;

import com.anjuke.dw.tools.model.MetricLog;

public class MetricLogRepositoryImpl implements MetricLogRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<MetricLog> findRange(long metricId, Date begin, Date end, Pageable pageable) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<MetricLog> query = builder.createQuery(MetricLog.class);
        Root<MetricLog> metricLog = query.from(MetricLog.class);

        query.where(
            builder.equal(metricLog.get("metricId"), metricId),
            builder.greaterThanOrEqualTo(metricLog.<Date>get("created"), begin),
            builder.lessThanOrEqualTo(metricLog.<Date>get("created"), end)
        );

        query.orderBy(builder.asc(metricLog.get("created")));

        TypedQuery<MetricLog> q = entityManager.createQuery(query);
        q.setFirstResult(pageable.getOffset());
        q.setMaxResults(pageable.getPageSize());

        return q.getResultList();
    }

}
