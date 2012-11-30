package com.aplana.sbrf.taxaccounting.web.servlet;

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
				session.invalidate();
			}
			response.sendRedirect(contextPath + "/login");
		}
	}
}
