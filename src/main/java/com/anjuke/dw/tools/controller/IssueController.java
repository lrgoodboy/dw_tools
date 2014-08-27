package com.anjuke.dw.tools.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.anjuke.dw.tools.dao.IssueRepository;
import com.anjuke.dw.tools.dao.UserRepository;
import com.anjuke.dw.tools.form.IssueFilterForm;
import com.anjuke.dw.tools.form.IssueForm;
import com.anjuke.dw.tools.model.Issue;
import com.anjuke.dw.tools.model.User;

@Controller
@RequestMapping("/issue")
public class IssueController {

    @Autowired
    private IssueRepository issueRepository;
    @Autowired
    private UserRepository userRepository;

    @RequestMapping({"", "list"})
    public String list(@ModelAttribute("issueFilter") IssueFilterForm issueFilter, Model model) {

        Pageable pageable = new PageRequest(0, Integer.MAX_VALUE, issueFilter.parseSort());
        List<Issue> issues = issueRepository.findByFilters(issueFilter.getStatus(), pageable);

        Set<Long> userIds = new HashSet<Long>();
        for (Issue issue : issues) {
            userIds.add(issue.getCreatorId());
            userIds.add(issue.getAsigneeId());
            userIds.add(issue.getReplierId());
        }

        Map<Long, User> users = new HashMap<Long, User>();
        for (User user : userRepository.findAll(userIds)) {
            users.put(user.getId(), user);
        }

        model.addAttribute("issues", issues);
        model.addAttribute("users", users);
        model.addAttribute("numOpened", issueRepository.countByStatus(Issue.STATUS_OPENED));
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
        issue.setReplyCount(0);
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
