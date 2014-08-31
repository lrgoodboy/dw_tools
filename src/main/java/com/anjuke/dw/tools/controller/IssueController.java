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
import org.pegdown.PegDownProcessor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.anjuke.dw.tools.dao.IssueActionRepository;
import com.anjuke.dw.tools.dao.IssueRepository;
import com.anjuke.dw.tools.dao.UserRepository;
import com.anjuke.dw.tools.form.IssueFilterForm;
import com.anjuke.dw.tools.form.IssueForm;
import com.anjuke.dw.tools.form.IssueReplyForm;
import com.anjuke.dw.tools.model.Issue;
import com.anjuke.dw.tools.model.IssueAction;
import com.anjuke.dw.tools.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/issue")
@SessionAttributes("user")
public class IssueController {

    @Autowired
    private IssueRepository issueRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IssueActionRepository issueActionRepository;
    @Autowired
    private ObjectMapper json;

    @RequestMapping({"", "list"})
    public String list(@ModelAttribute IssueFilterForm issueFilterForm, Model model) {

        Pageable pageable = new PageRequest(0, Integer.MAX_VALUE, issueFilterForm.parseSort());
        List<Issue> issues = issueRepository.findByFilters(issueFilterForm.getStatus(), pageable);

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
    public String add(@ModelAttribute IssueForm issueForm, Model model) {
        return "edit";
    }

    @RequestMapping(value = "add", method = RequestMethod.POST)
    public String addSubmit(@Valid @ModelAttribute IssueForm issueForm,
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

        IssueAction action = new IssueAction();
        action.setIssueId(issue.getId());
        action.setOperatorId(1L);
        action.setAction(IssueAction.ACTION_OPEN);
        action.setDetails("{}");
        action.setCreated(now);
        issueActionRepository.save(action);

        return "redirect:list";
    }

    @RequestMapping(value = "view/{id}", method = RequestMethod.GET)
    public String view(@PathVariable("id") Issue issue,
            @ModelAttribute IssueReplyForm issueReplyForm,
            Model model) {

        renderIssueView(issue, model);
        return "issue/view";
    }

    @RequestMapping(value = "view/{id}", method = RequestMethod.POST)
    public String viewSubmit(@PathVariable("id") Issue issue,
            @ModelAttribute IssueReplyForm issueReplyForm,
            BindingResult result, Model model) {

        do {

            if (result.hasErrors()) {
                break;
            }

            Date now = new Date();
            boolean hasReply = !StringUtils.isEmpty(issueReplyForm.getContent());

            if (hasReply) {

                IssueAction action = new IssueAction();
                action.setIssueId(issue.getId());
                action.setOperatorId(1L);
                action.setAction(IssueAction.ACTION_REPLY);
                action.setCreated(now);

                try {
                    JSONObject details = new JSONObject();
                    details.put("content", issueReplyForm.getContent());
                    action.setDetails(json.writeValueAsString(details));
                } catch (Exception e) {
                    result.addError(new ObjectError("content", "Fail to set content."));
                    break;
                }

                issueActionRepository.save(action);

                issue.setReplierId(action.getOperatorId());
                issue.setReplied(action.getCreated());
                issue.setReplyCount(issue.getReplyCount() + 1);
                issueRepository.save(issue);
            }

            if (issueReplyForm.getStatus()) {

                Integer newStatus, newAction;
                if (issue.getStatus() == Issue.STATUS_OPENED) {
                    newStatus = Issue.STATUS_CLOSED;
                    newAction = IssueAction.ACTION_CLOSE;
                } else {
                    newStatus = Issue.STATUS_OPENED;
                    newAction = IssueAction.ACTION_REOPEN;
                }

                issue.setStatus(newStatus);
                issueRepository.save(issue);

                IssueAction action = new IssueAction();
                action.setIssueId(issue.getId());
                action.setOperatorId(1L);
                action.setAction(newAction);
                action.setCreated(now);
                action.setDetails("{}");
                issueActionRepository.save(action);

            } else if (!hasReply) {
                result.addError(new ObjectError("global", "Invalid request."));
                break;
            }

            issueReplyForm.setContent(null);

        } while (false);

        renderIssueView(issue, model);
        return "issue/view";
    }

    private void renderIssueView(Issue issue, Model model) {

        Set<Long> userIds = new HashSet<Long>();
        userIds.add(issue.getCreatorId());

        PegDownProcessor md = new PegDownProcessor();
        String issueMd = md.markdownToHtml(issue.getContent());

        List<IssueAction> actions = new ArrayList<IssueAction>();
        Map<Long, JSONObject> details = new HashMap<Long, JSONObject>();
        Map<Long, String> actionMds = new HashMap<Long, String>();
        for (IssueAction action : issueActionRepository.findByIssueIdOrderByCreatedAsc(issue.getId())) {
            actions.add(action);

            JSONObject detail;
            try {
                detail = json.readValue(action.getDetails(), JSONObject.class);
            } catch (Exception e) {
                detail = new JSONObject();
            }
            details.put(action.getId(), detail);

            userIds.add(action.getOperatorId());
            actionMds.put(action.getId(), md.markdownToHtml(detail.optString("content")));
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
        model.addAttribute("issueMd", issueMd);
        model.addAttribute("actionMds", actionMds);
    }

    @ModelAttribute("user")
    public User getUser() {
        return userRepository.findOne(1L);
    }

    @ModelAttribute("navbar")
    public String getNavBar() {
        return "issue";
    }

}
