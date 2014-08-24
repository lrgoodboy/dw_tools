package com.anjuke.dw.tools.dao;

import org.springframework.data.repository.CrudRepository;

import com.anjuke.dw.tools.model.Issue;

public interface IssueRepository extends CrudRepository<Issue, Long> {

}
