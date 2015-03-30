package com.anjuke.dw.tools.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;
import javax.persistence.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.anjuke.dw.tools.dao.IssueActionRepository;
import com.anjuke.dw.tools.dao.IssueParticipantRepository;
import com.anjuke.dw.tools.dao.IssueRepository;
import com.anjuke.dw.tools.dao.UserRepository;
import com.anjuke.dw.tools.model.Issue;
import com.anjuke.dw.tools.model.IssueParticipant;
import com.anjuke.dw.tools.model.User;


@Service
public class IssueService {

    private Logger logger = LoggerFactory.getLogger(IssueService.class);

    @Autowired
    private IssueRepository issueRepository;
    @Autowired
    private IssueActionRepository issueActionRepository;
    @Autowired
    private IssueParticipantRepository issueParticipantRepository;
    @Autowired
    private UserRepository userRepository;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @PreDestroy
    public void preDestroy() {
        executor.shutdown();
    }

    public void buildAsync(long issueId) {
        executor.submit(new BuildTask(issueId));
    }

    private class BuildTask implements Runnable {

        private long issueId;

        public BuildTask(long issueId) {
            this.issueId = issueId;
        }

        @Override
        public void run() {

            Issue issue = issueRepository.findOne(issueId);
            List<Tuple> replyInfos = issueActionRepository.getReplyInfo(issueId);

            issue.setReplyCount(replyInfos.size());
            if (replyInfos.size() > 0) {
                issue.setReplierId(replyInfos.get(0).get("replierId", Long.class));
                issue.setReplied(replyInfos.get(0).get("replied", Date.class));
            } else {
                issue.setReplierId(0L);
            }

            issueRepository.save(issue);
            logger.info("Build issue id: " + issueId);

        }

    }

    public static class ParticipantInfo {
        private List<User> to;
        private List<User> cc;
        public List<User> getTo() {
            return to;
        }
        public void setTo(List<User> to) {
            this.to = to;
        }
        public List<User> getCc() {
            return cc;
        }
        public void setCc(List<User> cc) {
            this.cc = cc;
        }
    }

    public ParticipantInfo processParticipants(String content, Long issueId, Long currentUserId) {

        ParticipantInfo result = new ParticipantInfo();
        result.to = new ArrayList<User>();
        result.cc = new ArrayList<User>();

        // find AT users
        Pattern ptrn = Pattern.compile("@(\\S+)");
        Matcher matcher = ptrn.matcher(content);

        Set<String> toTruenames = new LinkedHashSet<String>();
        while (matcher.find()) {
            toTruenames.add(matcher.group(1));
        }

        Set<Long> toSet = new HashSet<Long>();
        if (toTruenames.size() > 0) {

            Map<String, User> toUsers = new HashMap<String, User>();
            for (User user : userRepository.findByTruenameIn(toTruenames)) {
                toUsers.put(user.getTruename(), user);
            }

            // generate ordered TO list
            for (String truename : toTruenames) {
                User toUser = toUsers.get(truename);
                if (toUser != null) {
                    result.to.add(toUser);
                    toSet.add(toUser.getId());
                }
            }

        }

        // get participants
        Set<Long> participants = new LinkedHashSet<Long>();
        for (IssueParticipant participant : issueParticipantRepository.findByIssueIdOrderByCreatedAsc(issueId)) {
            participants.add(participant.getUserId());
        }

        Set<Long> missingSet = new HashSet<Long>(toSet);
        missingSet.removeAll(participants);

        if (!participants.contains(currentUserId)) {
            missingSet.add(currentUserId);
            participants.add(currentUserId);
        }

        // generate ordered CC users
        Set<Long> ccSet = new LinkedHashSet<Long>();
        for (Long userId : participants) {
            if (!toSet.contains(userId)) {
                ccSet.add(userId);
            }
        }

        if (ccSet.size() > 0) {

            Map<Long, User> ccUsers = new HashMap<Long, User>();
            for (User user : userRepository.findAll(ccSet)) {
                ccUsers.put(user.getId(), user);
            }

            for (Long userId : ccSet) {
                User ccUser = ccUsers.get(userId);
                if (ccUser != null) {
                    result.cc.add(ccUser);
                }
            }
        }

        // insert missing users
        if (missingSet.size() > 0) {
            List<IssueParticipant> missingParticipants = new ArrayList<IssueParticipant>();
            for (Long userId : missingSet) {
                IssueParticipant participant = new IssueParticipant();
                participant.setIssueId(issueId);
                participant.setUserId(userId);
                participant.setCreated(new Date());
                missingParticipants.add(participant);
            }
            issueParticipantRepository.save(missingParticipants);
        }

        if (result.to.isEmpty() && result.cc.size() > 0) {
            List<User> tmp = result.to;
            result.to = result.cc;
            result.cc = tmp;
        }

        return result;
    }

}
