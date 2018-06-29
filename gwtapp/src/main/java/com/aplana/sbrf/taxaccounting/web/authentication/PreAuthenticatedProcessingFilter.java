package com.aplana.sbrf.taxaccounting.web.authentication;

import org.springframework.security.web.authentication.preauth.j2ee.J2eePreAuthenticatedProcessingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class PreAuthenticatedProcessingFilter extends J2eePreAuthenticatedProcessingFilter {
    private static final String WEBSPHERE_LOGOUT_PAGE = "/ibm_security_logout";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        httpServletRequest.getSession();

        if (!httpServletRequest.getRequestURI().contains(WEBSPHERE_LOGOUT_PAGE)) {
            super.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }
}
