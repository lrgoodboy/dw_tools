package com.anjuke.dw.tools.controller;

import java.util.Date;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.anjuke.dw.tools.dao.IssueRepository;
import com.anjuke.dw.tools.form.IssueForm;
import com.anjuke.dw.tools.model.Issue;

@Controller
@RequestMapping("/issue")
public class IssueController {

    @Autowired
    private IssueRepository issueRepository;

    @RequestMapping({"", "list"})
    public String list(Model model) {
        return "issue";
    }

    @RequestMapping(value = "add", method = RequestMethod.GET)
    public String add(Model model) {
        model.addAttribute("issue", new IssueForm());
        return "edit";
    }

    @RequestMapping(value = "add", method = RequestMethod.POST)
    public String addSubmit(@Valid @ModelAttribute("issue") IssueForm issueForm,
            BindingResult result, Model model) {

        if (result.hasErrors()) {
            return "edit";
        }

        Issue issue = new Issue();
        BeanUtils.copyProperties(issueForm, issue);
        Date now = new Date();
        issue.setStatus(Issue.STATUS_OPENED);
        issue.setCreatorId(1L);
        issue.setAsigneeId(0L);
        issue.setReplierId(0L);
        issue.setReplied(now);
        issue.setCreated(now);
        issueRepository.save(issue);

        return "redirect:list";
    }

    @ModelAttribute("navbar")
    public String getNavBar() {
        return "issue";
    }

}
