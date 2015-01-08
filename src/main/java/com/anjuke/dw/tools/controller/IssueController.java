package com.anjuke.dw.tools.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.json.JSONObject;
import org.markdown4j.Markdown4jProcessor;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.anjuke.dw.tools.AisProperties;
import com.anjuke.dw.tools.dao.IssueActionRepository;
import com.anjuke.dw.tools.dao.IssueRepository;
import com.anjuke.dw.tools.dao.UserRepository;
import com.anjuke.dw.tools.form.IssueFilterForm;
import com.anjuke.dw.tools.form.IssueForm;
import com.anjuke.dw.tools.form.IssueReplyForm;
import com.anjuke.dw.tools.model.Issue;
import com.anjuke.dw.tools.model.IssueAction;
import com.anjuke.dw.tools.model.User;
import com.anjuke.dw.tools.service.EmailService;
import com.anjuke.dw.tools.service.EmailService.Email;
import com.anjuke.dw.tools.service.IssueService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/issue")
@SessionAttributes("user")
public class IssueController {

    @SuppressWarnings("unused")
    private Logger logger = LoggerFactory.getLogger(IssueController.class);

    @Autowired
    private IssueRepository issueRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IssueActionRepository issueActionRepository;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private EmailService emailService;
    @Autowired
    private IssueService issueService;

    @Value("${email.receiver}")
    private String emailReceiver;
    @Value("${email.baseurl}")
    private String emailBaseUrl;

    @Autowired
    private AisProperties aisProperties;

    // https://github.com/jch/html-pipeline/blob/master/lib/html/pipeline/sanitization_filter.rb
    private PolicyFactory sanitizer = new HtmlPolicyBuilder()
            .allowAttributes("src").onElements("img")
            .allowAttributes("href").onElements("a")
            .allowAttributes("itemscope", "itemtype").onElements("div")
            .allowStandardUrlProtocols()
            .allowElements((
                "h1 h2 h3 h4 h5 h6 h7 h8 br b i strong em a pre code img tt"
                + " div ins del sup sub p ol ul table thead tbody tfoot blockquote"
                + " dl dt dd kbd q samp var hr ruby rt rp li tr td th s strike"
            ).split(" "))
            .toFactory();

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
        return "issue/list";
    }

    @RequestMapping(value = "add", method = RequestMethod.GET)
    public String add(@ModelAttribute IssueForm issueForm, Model model) {
        return "issue/edit";
    }

    @RequestMapping(value = "add", method = RequestMethod.POST)
    public String addSubmit(@ModelAttribute User currentUser,
            @Valid @ModelAttribute IssueForm issueForm,
            BindingResult result, Model model) {

        if (result.hasErrors()) {
            return "issue/edit";
        }

        Issue issue = new Issue();
        BeanUtils.copyProperties(issueForm, issue);
        Date now = new Date();
        issue.setStatus(Issue.STATUS_OPENED);
        issue.setCreatorId(currentUser.getId());
        issue.setAsigneeId(0L);
        issue.setReplierId(0L);
        issue.setReplyCount(0);
        issue.setReplied(now);
        issue.setCreated(now);
        issueRepository.save(issue);

        IssueAction action = new IssueAction();
        action.setIssueId(issue.getId());
        action.setOperatorId(currentUser.getId());
        action.setAction(IssueAction.ACTION_OPEN);
        action.setDetails("{}");
        action.setCreated(now);
        issueActionRepository.save(action);

        sendEmail(String.format("Issue#%d %s", issue.getId(), issue.getTitle()), issue.getContent(),
                currentUser.getTruename(), issue.getCreated(), issue.getId());

        return "redirect:/issue/view/" + issue.getId();
    }

    @RequestMapping(value = "view/{issueId}", method = RequestMethod.GET)
    public String view(@ModelAttribute User currentUser,
            @PathVariable("issueId") Issue issue,
            @ModelAttribute IssueReplyForm issueReplyForm,
            Model model) {

        renderIssueView(issue, model, currentUser);
        return "issue/view";
    }

    @RequestMapping(value = "view/{issueId}", method = RequestMethod.POST)
    public String viewSubmit(@ModelAttribute User currentUser,
            @PathVariable("issueId") Issue issue,
            @ModelAttribute IssueReplyForm issueReplyForm,
            BindingResult result, Model model) {

        boolean needBuild = false;

        do {

            if (result.hasErrors()) {
                break;
            }

            Date now = new Date();
            boolean hasReply = !StringUtils.isEmpty(issueReplyForm.getContent());

            if (hasReply) {

                IssueAction action = new IssueAction();
                action.setIssueId(issue.getId());
                action.setOperatorId(currentUser.getId());
                action.setAction(IssueAction.ACTION_REPLY);
                action.setCreated(now);

                try {
                    JSONObject details = new JSONObject();
                    details.put("content", issueReplyForm.getContent());
                    action.setDetails(json.writeValueAsString(details));
                } catch (Exception e) {
                    result.rejectValue("content", "issueReplyForm.content", "Fail to set content.");
                    break;
                }

                issueActionRepository.save(action);
                needBuild = true;

                sendEmail(String.format("Re: Issue#%d %s", issue.getId(), issue.getTitle()), issueReplyForm.getContent(),
                        currentUser.getTruename(), action.getCreated(), action.getIssueId());
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
                action.setOperatorId(currentUser.getId());
                action.setAction(newAction);
                action.setCreated(now);
                action.setDetails("{}");
                issueActionRepository.save(action);

            } else if (!hasReply) {
                result.rejectValue("content", "issueReplyForm.content", "Comment cannot be empty.");
                break;
            }

            issueReplyForm.setContent(null);

        } while (false);

        if (needBuild) {
            issueService.buildAsync(issue.getId());
        }

        renderIssueView(issue, model, currentUser);
        return "issue/view";
    }

    @RequestMapping(value = "edit/{issueId}", method = RequestMethod.GET)
    public String edit(@PathVariable("issueId") Issue issue,
            @ModelAttribute IssueForm issueForm,
            Model model) {

        BeanUtils.copyProperties(issue, issueForm);
        return "issue/edit";
    }

    @RequestMapping(value = "edit/{issueId}", method = RequestMethod.POST)
    public String editSubmit(@PathVariable("issueId") Issue issue,
            @Valid @ModelAttribute IssueForm issueForm,
            BindingResult result, Model model) {

        if (result.hasErrors()) {
            return "issue/edit";
        }

        BeanUtils.copyProperties(issueForm, issue, "id");
        issueRepository.save(issue);

        return "redirect:/issue/view/" + issue.getId();
    }

    @RequestMapping(value = "preview", method = RequestMethod.POST, produces = "text/html;charset=utf-8")
    @ResponseBody
    public String preview(@RequestParam("content") String content) {
        return renderMarkdown(content);
    }

    @RequestMapping(value = "upload-callback")
    @ResponseBody
    public String uploadCallback(@RequestParam("q") String q) {
        return q;
    }

    private void renderIssueView(Issue issue, Model model, User currentUser) {

        Set<Long> userIds = new HashSet<Long>();
        userIds.add(currentUser.getId());
        userIds.add(issue.getCreatorId());

        String issueMd = renderMarkdown(issue.getContent());

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
            actionMds.put(action.getId(), renderMarkdown(detail.optString("content")));
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

    private void sendEmail(String subject, String content, String truename, Date created, Long issueId) {

        content = renderMarkdown(content);

        String signature = String.format("<p style=\"color: gray; margin-top: 30px;\">--<br>%s %s <a href=\"%s/issue/view/%d\">查看详情</a></p>",
                truename, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(created),
                emailBaseUrl, issueId);

        Email email = new Email();
        email.setSubject(subject);
        email.setHtml(content + signature);
        email.setSender(truename);
        email.setReceivers(Arrays.asList(emailReceiver));
        emailService.sendAsync(email);
    }

    private String renderMarkdown(String input) {
        String html;
        try {
            html = new Markdown4jProcessor().process(input);
        } catch (IOException e) {
            html = "<pre>" + input + "</pre>";
        }
        return sanitizer.sanitize(html);
    }

    @ModelAttribute("navbar")
    public String getNavBar() {
        return "issue";
    }

    @ModelAttribute
    public AisProperties getAisProperties() {
        return aisProperties;
    }

}
