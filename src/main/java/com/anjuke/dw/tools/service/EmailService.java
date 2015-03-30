package com.anjuke.dw.tools.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

@Service
public class EmailService {

    private Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${email.host}")
    private String emailHost;
    @Value("${email.username}")
    private String emailUsername;
    @Value("${email.password}")
    private String emailPassword;

    private ExecutorService executor = Executors.newFixedThreadPool(5);

    @PreDestroy
    public void preDestroy() {
        executor.shutdown();
    }

    public void send(Email em) throws Exception {

        HtmlEmail email = new HtmlEmail();
        email.setHostName(emailHost);
        email.setAuthentication(emailUsername, emailPassword);
        email.setFrom(emailUsername, em.getSender());
        email.setSubject(em.getSubject());

        for (EmailReceiver to : em.getTo()) {
            email.addTo(to.getEmail(), to.getName());
        }

        for (EmailReceiver cc : em.getCc()) {
            email.addCc(cc.getEmail(), cc.getName());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head>");
        sb.append("<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\">");
        sb.append("</head><body>");
        sb.append(em.getHtml());
        sb.append("</body></html>");
        email.setHtmlMsg(sb.toString());
        email.setCharset("UTF-8");

        logger.info(String.format("Sending email, subject: %s, sender: %s, to: %s, cc: %s",
                em.getSubject(), em.getSender(),
                Iterables.toString(email.getToAddresses()),
                Iterables.toString(email.getCcAddresses())));

        email.send();
    }

    public void sendAsync(Email email) {
        executor.submit(new AsyncTask(email));
    }

    private class AsyncTask implements Runnable {

        private Email email;

        public AsyncTask(Email email) {
            this.email = email;
        }

        @Override
        public void run() {
            try {
                send(email);
            } catch (Exception e) {
                logger.error("Fail to send email - " + e.getMessage());
            }
        }

    }

    public static class EmailReceiver {
        private String email;
        private String name;
        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Email {
        private String subject;
        private String html;
        private String sender;
        private List<EmailReceiver> to;
        private List<EmailReceiver> cc;
        public String getSubject() {
            return subject;
        }
        public void setSubject(String subject) {
            this.subject = subject;
        }
        public String getHtml() {
            return html;
        }
        public void setHtml(String html) {
            this.html = html;
        }
        public String getSender() {
            return sender;
        }
        public void setSender(String sender) {
            this.sender = sender;
        }
        public List<EmailReceiver> getTo() {
            return to;
        }
        public void setTo(List<EmailReceiver> to) {
            this.to = to;
        }
        public List<EmailReceiver> getCc() {
            return cc;
        }
        public void setCc(List<EmailReceiver> cc) {
            this.cc = cc;
        }
    }

}
