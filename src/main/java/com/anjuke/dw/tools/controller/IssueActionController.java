package com.anjuke.dw.tools.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.anjuke.dw.tools.dao.IssueActionRepository;
import com.anjuke.dw.tools.dao.IssueRepository;
import com.anjuke.dw.tools.model.Issue;
import com.anjuke.dw.tools.model.IssueAction;


@RestController
@RequestMapping("/issue/action")
@SessionAttributes("user")
public class IssueActionController {

    @Autowired
    private IssueActionRepository issueActionRepository;
    @Autowired
    private IssueRepository issueRepository;

    @RequestMapping("delete/{actionId}")
    public void delete(@PathVariable("actionId") IssueAction action) {

        Issue issue = issueRepository.findOne(action.getIssueId());
        issue.setReplyCount(issue.getReplyCount() - 1);
        issueRepository.save(issue);

        issueActionRepository.delete(action);
    }

}
