package com.anjuke.dw.tools.service;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${email.host}")
    private String emailHost;
    @Value("${email.username}")
    private String emailUsername;
    @Value("${email.password}")
    private String emailPassword;
    @Value("${email.sender.email}")
    private String senderEmail;
    @Value("${email.sender.name}")
    private String senderName;

    private ExecutorService executor = Executors.newFixedThreadPool(5);

    @PreDestroy
    public void preDestroy() {
        executor.shutdown();
    }

    public void send(String subject, String html, String... receivers) throws Exception {

        HtmlEmail email = new HtmlEmail();
        email.setHostName(emailHost);
        email.setAuthentication(emailUsername, emailPassword);
        email.setFrom(senderEmail, senderName);
        email.setSubject(subject);

        for (String to : receivers) {
            email.addTo(to);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head>");
        sb.append("<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\">");
        sb.append("</head><body>");
        sb.append(html);
        sb.append("</body></html>");
        email.setHtmlMsg(sb.toString());
        email.setCharset("UTF-8");

        logger.info(String.format("Sending email, subject: %s, receivers: %s", subject, Arrays.toString(receivers)));

        email.send();
    }

    public void sendAsync(String subject, String html, String... receivers) {
        executor.submit(new AsyncTask(subject, html, receivers));
    }

    private class AsyncTask implements Runnable {

        private String subject;
        private String html;
        private String[] receivers;

        public AsyncTask(String subject, String html, String... receivers) {
            this.subject = subject;
            this.html = html;
            this.receivers = receivers;
        }

        @Override
        public void run() {
            try {
                send(subject, html, receivers);
            } catch (Exception e) {
                logger.error("Fail to send email.", e);
            }
        }

    }

}
