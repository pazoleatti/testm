package com.aplana.sbrf.taxaccounting.web.mvc;

import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

//TODO https://jira.aplana.com/browse/SBRFNDFL-1687

public class CompatibilityFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.addHeader("X-UA-Compatible", "IE=edge,chrome=1");
        HttpSession session = httpServletRequest.getSession();
        try {
            MDC.put("processId", "SID=" + session.getId() + " ");
            if (httpServletRequest.getUserPrincipal() != null) {
                MDC.put("userInfo", String.format("%s ", httpServletRequest.getUserPrincipal().getName()));
            }
            filterChain.doFilter(servletRequest, response);
        } finally {
            MDC.clear();
        }
    }

    @Override
    public void destroy() {
    }
}
