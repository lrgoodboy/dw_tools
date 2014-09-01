package com.anjuke.dw.tools.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.anjuke.dw.tools.dao.IssueActionRepository;
import com.anjuke.dw.tools.dao.IssueRepository;
import com.anjuke.dw.tools.model.Issue;
import com.anjuke.dw.tools.model.IssueAction;


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

            List<IssueAction> issueActions = issueActionRepository.findByIssueIdAndActionOrderByCreatedDesc(issueId, IssueAction.ACTION_REPLY, new PageRequest(0, 1));
            if (issueActions.size() > 0) {
                issue.setReplierId(issueActions.get(0).getOperatorId());
                issue.setReplied(issueActions.get(0).getCreated());
                issue.setReplyCount(issueActionRepository.countByIssueIdAndAction(issueId, IssueAction.ACTION_REPLY));
            } else {
                issue.setReplyCount(0);
                issue.setReplierId(0L);
            }

            issueRepository.save(issue);
            logger.info("Build issue id: " + issueId);
        }

    }

}
