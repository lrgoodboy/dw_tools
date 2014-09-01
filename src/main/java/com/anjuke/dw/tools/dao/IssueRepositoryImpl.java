package com.anjuke.dw.tools.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.anjuke.dw.tools.model.Issue;

public class IssueRepositoryImpl implements IssueRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Issue> findByFilters(Integer status, Pageable pageable) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Issue> query = builder.createQuery(Issue.class);
        Root<Issue> issue = query.from(Issue.class);

        if (status != null) {
            query.where(builder.equal(issue.get("status"), status));
        }

        List<Order> orders = new ArrayList<Order>();
        for (Sort.Order order : pageable.getSort()) {
            if (order.isAscending()) {
                orders.add(builder.asc(issue.get(order.getProperty())));
            } else {
                orders.add(builder.desc(issue.get(order.getProperty())));
            }
        }
        query.orderBy(orders);

        TypedQuery<Issue> q = entityManager.createQuery(query);
        q.setFirstResult(pageable.getOffset());
        q.setMaxResults(pageable.getPageSize());

        return q.getResultList();
    }

}
