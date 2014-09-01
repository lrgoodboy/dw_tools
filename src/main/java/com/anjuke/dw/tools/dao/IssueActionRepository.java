package com.anjuke.dw.tools.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import com.anjuke.dw.tools.model.IssueAction;

public interface IssueActionRepository extends CrudRepository<IssueAction, Long>{
    List<IssueAction> findByIssueIdOrderByCreatedAsc(long issueId);
    List<IssueAction> findByIssueIdAndActionOrderByCreatedDesc(long issueId, int action, Pageable pageable);
    int countByIssueIdAndAction(long issueId, int action);
}
