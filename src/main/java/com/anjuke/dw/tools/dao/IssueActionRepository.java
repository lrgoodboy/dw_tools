package com.anjuke.dw.tools.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.anjuke.dw.tools.model.IssueAction;

public interface IssueActionRepository extends CrudRepository<IssueAction, Long>{
    List<IssueAction> findByIssueIdOrderByCreatedAsc(long issueId);
}
