package com.anjuke.dw.tools.dao;

import java.util.List;

import javax.persistence.Tuple;

public interface IssueActionRepositoryCustom {
    List<Tuple> getReplyInfo(long issueId);
}
