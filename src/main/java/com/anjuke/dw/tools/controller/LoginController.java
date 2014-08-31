package com.anjuke.dw.tools.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import com.anjuke.dw.tools.dao.UserRepository;
import com.anjuke.dw.tools.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@SessionAttributes("user")
public class LoginController {

    public static final String DEFAULT_FROM = "/";
    private Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Value("${oauth.url}")
    private String oauthUrl;
    @Value("${oauth.client}")
    private String oauthClient;
    @Value("${oauth.secret}")
    private String oauthSecret;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper json;

    @RequestMapping("/login")
    public View login(@RequestParam(value = "access_token", required = false) String accessToken,
            @RequestParam(value = "from", defaultValue = DEFAULT_FROM) String from,
            @RequestParam(value = "custom", defaultValue = DEFAULT_FROM) String custom,
            Model model) throws Exception {

        if (accessToken != null) {
            model.addAttribute("user", loginUser(accessToken));
            return redirect(custom);
        }

        return redirect(getAuthUrl(from));
    }

    private RedirectView redirect(String url) {
        RedirectView redirect = new RedirectView();
        redirect.setStatusCode(HttpStatus.FOUND);
        redirect.setExposeModelAttributes(false);
        redirect.setUrl(url);
        logger.info("redirect: " + url);
        return redirect;
    }

    private JSONObject requestJson(String url, String... params) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpPost httpPost = new HttpPost(url);
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            for (int i = 0; i < params.length; i += 2) {
                nvps.add(new BasicNameValuePair(params[i], params[i + 1]));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            logger.info(String.format("request json, url: %s, params: %s", url, nvps));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                JSONObject info = json.readValue(EntityUtils.toString(response.getEntity()), JSONObject.class);
                logger.info("result: " + info.toString());
                return info;
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
    }

    private User loginUser(String accessToken) throws Exception {

        JSONObject info = requestJson(oauthUrl + "/resource.php",
                "oauth_token", accessToken,
                "getinfo", "true");

        String username = info.getString("username");
        User user = userRepository.findByUsername(username);
        if (user == null) {
            user = new User();
            user.setUsername(info.getString("username"));
            user.setTruename(info.getString("name"));
            user.setEmail(info.getString("email"));

            if (info.getString("function_id").equals("11")) {
                if (info.getString("department_name").contains("DW")) {
                    user.setRole(User.ROLE_DW);
                } else {
                    user.setRole(User.ROLE_BI);
                }
            } else {
                user.setRole(User.ROLE_ANJUKE);
            }

            user.setCreated(new Date());
            userRepository.save(user);
        }

        return user;
    }

    private String getTemporaryToken() throws Exception {

        JSONObject info = requestJson(oauthUrl + "/authorize.php",
                "client_id", oauthClient,
                "response_type", "code",
                "curl", "true");

        return info.getString("code");
    }

    private String getAuthUrl(String from) throws Exception {
        return UriComponentsBuilder.fromHttpUrl(oauthUrl).path("/token.php")
                .queryParam("client_id", oauthClient)
                .queryParam("client_secret", oauthSecret)
                .queryParam("grant_type", "authorization_code")
                .queryParam("code", getTemporaryToken())
                .queryParam("custom", from)
                .build().toUriString();
    }

}
