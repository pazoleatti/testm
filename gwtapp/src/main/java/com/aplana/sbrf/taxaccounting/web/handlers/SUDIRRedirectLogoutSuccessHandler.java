package com.aplana.sbrf.taxaccounting.web.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * User: avanteev
 */
public class SUDIRRedirectLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final Log LOG = LogFactory.getLog(SUDIRRedirectLogoutSuccessHandler.class);

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String contextPath = request.getContextPath();
        String redirectPage = "/login";

        try {
            LOG.info("Получение информации по logout");
        } catch (Exception e) {}

        try {
            LOG.info("ServerInfo: " + request.getSession().getServletContext().getServerInfo().toString());
        } catch (Exception e) {}

        SecurityContextHolder.clearContext();
        if (Pattern.compile("Web\\s*Sphere", Pattern.CASE_INSENSITIVE).matcher(request.getSession().getServletContext().getServerInfo()).find()) {
            boolean isSudir = false;
            Cookie cookies[] = request.getCookies();
            if (cookies != null && cookies.length != 0) {
                for (Cookie cookie : cookies) {

                    try {
                        LOG.info("Cookie name: " + cookie.getName());
                    } catch (Exception e) {}

                    if (cookie.getName().equals("IV_JCT"))
                        isSudir = true;
                }
            }
            if (isSudir) {
                response.sendRedirect("/pkmslogout");
            } else {
                response.sendRedirect(contextPath + "/ibm_security_logout?logoutExitPage=" + redirectPage);
            }
        } else {
            response.sendRedirect(contextPath + redirectPage);
        }
    }
}
