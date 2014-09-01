package com.anjuke.dw.tools.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.buf.HexUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import com.anjuke.dw.tools.dao.UserRepository;
import com.anjuke.dw.tools.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@SessionAttributes("user")
public class LoginController {

    private static final String DEFAULT_FROM = "/";
    private static final String COOKIE_NAME = "dw_tools_auth";
    private static final Integer COOKIE_EXPIRE = 7 * 24 * 3600;
    private static final String HMAC_KEY = "dwrocks";
    private static final String HMAC_ALGORITHM = "HmacMD5";

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
    public View login(@CookieValue(value = COOKIE_NAME, required = false) String cookieToken,
            @RequestParam(value = "access_token", required = false) String accessToken,
            @RequestParam(value = "from", defaultValue = DEFAULT_FROM) String from,
            @RequestParam(value = "custom", defaultValue = DEFAULT_FROM) String custom,
            Model model, HttpServletResponse response) throws Exception {

        User user = null;

        if (cookieToken != null) {
            try {
                user = loginByCookie(cookieToken);
            } catch (Exception e) {
                logger.info("Login by cookie failed, remove the cookie.", e);
                response.addCookie(new Cookie(COOKIE_NAME, null));
            }

            if (user != null) {
                logger.info("Login by cookie: " + user.getUsername());
                model.addAttribute("user", user);
                return redirect(from);
            }
        }

        if (accessToken != null) {
            try {
                user = loginByAuth(accessToken);
            } catch (Exception e) {
                logger.info("Login by auth failed.", e);
            }

            if (user != null) {
                logger.info("Login by auth: " + user.getUsername());
                model.addAttribute("user", user);

                Cookie cookie = new Cookie(COOKIE_NAME, getCookieToken(user.getId()));
                cookie.setMaxAge(COOKIE_EXPIRE);
                response.addCookie(cookie);

                return redirect(custom);
            }
        }

        return redirect(getAuthUrl(from));
    }

    @RequestMapping("/logout")
    public View logout(SessionStatus status, HttpServletResponse response) {
        status.setComplete();
        response.addCookie(new Cookie(COOKIE_NAME, null));
        return redirect(UriComponentsBuilder.fromHttpUrl(oauthUrl).path("/logout.php")
                .queryParam("client_id", oauthClient)
                .queryParam("client_secret", oauthSecret)
                .build().toUriString());
    }

    private RedirectView redirect(String url) {
        RedirectView redirect = new RedirectView();
        redirect.setStatusCode(HttpStatus.FOUND);
        redirect.setExposeModelAttributes(false);
        redirect.setUrl(url);
        logger.info("Redirect: " + url);
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
            logger.info(String.format("Request json, url: %s, params: %s", url, nvps));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                JSONObject info = json.readValue(EntityUtils.toString(response.getEntity()), JSONObject.class);
                logger.info("Result: " + info.toString());
                return info;
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
    }

    private User loginByAuth(String accessToken) throws Exception {

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

    private User loginByCookie(String cookieToken) throws Exception {

        Pattern ptrn = Pattern.compile("^([0-9]+)_(.*)$");
        Matcher matcher = ptrn.matcher(cookieToken);
        if (!matcher.matches()) {
            throw new Exception("Invalid token format.");
        }

        if (!matcher.group(2).equals(hmac(matcher.group(1)))) {
            throw new Exception("Invalid token.");
        }

        return userRepository.findOne(Long.parseLong(matcher.group(1)));
    }

    private String getCookieToken(long userId) throws Exception {
        return String.format("%d_%s", userId, hmac(String.valueOf(userId)));
    }

    public String hmac(String input) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(HMAC_KEY.getBytes(), HMAC_ALGORITHM));
        return HexUtils.toHexString(mac.doFinal(input.getBytes()));
    }

}
