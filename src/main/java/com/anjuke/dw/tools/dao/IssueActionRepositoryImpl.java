package com.anjuke.dw.tools.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.anjuke.dw.tools.model.IssueAction;

public class IssueActionRepositoryImpl implements IssueActionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Tuple> getReplyInfo(long issueId) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<IssueAction> issueAction = query.from(IssueAction.class);

        query.select(builder.tuple(
            issueAction.get("operatorId").alias("replierId"),
            issueAction.get("created").alias("replied")
        ));
        query.where(
            builder.equal(issueAction.get("issueId"), issueId),
            builder.equal(issueAction.get("action"), IssueAction.ACTION_REPLY)
        );
        query.orderBy(builder.desc(issueAction.get("created")));

        return entityManager.createQuery(query).getResultList();
    }

}
