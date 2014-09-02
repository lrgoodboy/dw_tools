package com.anjuke.dw.tools.service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;
import javax.persistence.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.anjuke.dw.tools.dao.IssueActionRepository;
import com.anjuke.dw.tools.dao.IssueRepository;
import com.anjuke.dw.tools.model.Issue;


@Service
public class IssueService {

    private Logger logger = LoggerFactory.getLogger(IssueService.class);

    @Autowired
    private IssueRepository issueRepository;
    @Autowired
    private IssueActionRepository issueActionRepository;

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

}
