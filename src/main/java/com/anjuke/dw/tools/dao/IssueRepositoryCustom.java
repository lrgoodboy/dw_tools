package com.anjuke.dw.tools.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.anjuke.dw.tools.model.Issue;

public interface IssueRepositoryCustom {
    List<Issue> findByFilters(Integer status, Pageable pageable);
}
