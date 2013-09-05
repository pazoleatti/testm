package com.aplana.sbrf.taxaccounting.web.handlers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * User: avanteev
 */
public class SUDIRRedirectLogoutSuccessHandler implements LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String contextPath = request.getContextPath();
        SecurityContextHolder.clearContext();
        if (Pattern.compile("Web\\s*Sphere", Pattern.CASE_INSENSITIVE).matcher(request.getSession().getServletContext().getServerInfo()).find()) {
            response.sendRedirect("/pkmslogout");
        } else {
            response.sendRedirect(contextPath + "/login");
        }
    }
}
