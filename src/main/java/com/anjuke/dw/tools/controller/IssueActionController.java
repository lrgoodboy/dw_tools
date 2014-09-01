package com.anjuke.dw.tools.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.anjuke.dw.tools.dao.IssueActionRepository;
import com.anjuke.dw.tools.model.IssueAction;


@RestController
@RequestMapping("/issue/action")
@SessionAttributes("user")
public class IssueActionController {

    @Autowired
    private IssueActionRepository issueActionRepository;

    @RequestMapping("delete/{actionId}")
    public void delete(@PathVariable("actionId") IssueAction action) {
        issueActionRepository.delete(action);
    }

}
