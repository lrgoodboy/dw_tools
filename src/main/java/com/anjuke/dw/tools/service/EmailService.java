package com.anjuke.dw.tools.service;

import java.util.Arrays;

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

}
