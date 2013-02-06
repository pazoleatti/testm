package com.aplana.sbrf.taxaccounting.web.servlet;

import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

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
		if (Pattern.compile("Web\\s*Sphere", Pattern.CASE_INSENSITIVE).matcher(getServletContext().getServerInfo()).find()) {
			response.sendRedirect(contextPath + "/ibm_security_logout?logoutExitPage=login");
		} else {
			HttpSession session = request.getSession();
			if (session != null) {
				WebApplicationContext springContext =
						WebApplicationContextUtils.getWebApplicationContext(getServletContext());
				FormDataService unlockFormData =(FormDataService)springContext.getBean("unlockFormData");
				SecurityService securityService = (SecurityService)springContext.getBean("securityService");
				unlockFormData.unlockAllByUserId(securityService.currentUser().getId());
				session.invalidate();
			}
			response.sendRedirect(contextPath + "/login");
		}
	}
}
