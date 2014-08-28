package com.anjuke.dw.tools.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.anjuke.dw.tools.dao.IssueActionRepository;
import com.anjuke.dw.tools.dao.IssueRepository;
import com.anjuke.dw.tools.dao.UserRepository;
import com.anjuke.dw.tools.form.IssueFilterForm;
import com.anjuke.dw.tools.form.IssueForm;
import com.anjuke.dw.tools.model.Issue;
import com.anjuke.dw.tools.model.IssueAction;
import com.anjuke.dw.tools.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;

@Controller
@RequestMapping("/issue")
public class IssueController {

    @Autowired
    private IssueRepository issueRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IssueActionRepository issueActionRepository;

    private static ObjectMapper json = new ObjectMapper();
    static {
        json.registerModule(new JsonOrgModule());
    }

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

    @RequestMapping("view/{id}")
    public String view(@PathVariable("id") Issue issue, Model model) {

        Set<Long> userIds = new HashSet<Long>();
        userIds.add(issue.getCreatorId());

        List<IssueAction> actions = new ArrayList<IssueAction>();
        Map<Long, JSONObject> details = new HashMap<Long, JSONObject>();
        for (IssueAction action : issueActionRepository.findByIssueIdOrderByCreatedDesc(issue.getId())) {
            actions.add(action);

            JSONObject detail;
            try {
                detail = json.readValue(action.getDetails(), JSONObject.class);
            } catch (Exception e) {
                detail = new JSONObject();
            }
            details.put(action.getId(), detail);

            userIds.add(action.getOperatorId());
        }

        Map<Long, User> users = new HashMap<Long, User>();
        Map<Long, String> hashes = new HashMap<Long, String>();
        for (User user : userRepository.findAll(userIds)) {
            users.put(user.getId(), user);
            hashes.put(user.getId(), DigestUtils.md5DigestAsHex(user.getEmail().trim().toLowerCase().getBytes()));
        }

        model.addAttribute("issue", issue);
        model.addAttribute("actions", actions);
        model.addAttribute("details", details);
        model.addAttribute("users", users);
        model.addAttribute("hashes", hashes);
        return "issue/view";
    }

    @ModelAttribute("navbar")
    public String getNavBar() {
        return "issue";
    }

}
