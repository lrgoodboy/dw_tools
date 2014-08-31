package com.anjuke.dw.tools.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.UriComponentsBuilder;

import com.anjuke.dw.tools.model.User;

public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession();
        if (session.getAttribute("user") instanceof User) {
            return true;
        }

        response.sendRedirect(UriComponentsBuilder
                .fromPath(request.getContextPath()).path("/login")
                .queryParam("from", getUrl(request)).build().toUriString());
        return false;
    }

    private String getUrl(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder(request.getRequestURL());
        String queryString = request.getQueryString();
        if (queryString != null) {
            sb.append("?").append(queryString);
        }
        return sb.toString();
    }

}
