package com.anjuke.dw.tools.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.anjuke.dw.tools.model.IssueParticipant;

public interface IssueParticipantRepository extends CrudRepository<IssueParticipant, Long> {
    List<IssueParticipant> findByIssueIdOrderByCreatedAsc(long issueId);
}
