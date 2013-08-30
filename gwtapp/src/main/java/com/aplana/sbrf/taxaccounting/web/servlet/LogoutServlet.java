package com.aplana.sbrf.taxaccounting.web.servlet;

import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Сервлет для выхода из системы. Очищает сессию и редиректит на страницу авторизации.
 *
 * @author Vitalii Samolovskikh
 */
public class LogoutServlet extends HttpServlet {
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String contextPath = request.getContextPath();
		/*WebApplicationContext springContext =
				WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		SecurityService securityService = (SecurityService)springContext.getBean("securityService");
		if (securityService.currentUserInfo() != null) {
			FormDataService unlockFormData =(FormDataService)springContext.getBean("unlockFormData");
			unlockFormData.unlockAllByUser(securityService.currentUserInfo());
		}*/
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        if (Pattern.compile("Web\\s*Sphere", Pattern.CASE_INSENSITIVE).matcher(getServletContext().getServerInfo()).find()) {
            response.sendRedirect("/pkmslogout");
		} else {
			response.sendRedirect(contextPath + "/login");
		}
	}
}
