package com.aplana.sbrf.taxaccounting.web.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Сервлет для выхода из системы. Очищает сессию и редиректит на страницу авторизации.
 *
 * @author Vitalii Samolovskikh
 */
public class LogoutServlet extends HttpServlet {
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		if (session != null) {
			session.invalidate();
		}
		response.sendRedirect(request.getContextPath() + "/login");
	}
}
