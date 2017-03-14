package com.aplana.sbrf.taxaccounting.web.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

/**
 * User: avanteev
 */
public class SUDIRRedirectLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final Log LOG = LogFactory.getLog(SUDIRRedirectLogoutSuccessHandler.class);

	// Один из заголовков, которые вебсил добавляет к запросам идущим от него к нашему приложениию
	private final static String WEBSEAL_HEADER_NAME = "iv_server_name";
	// Путь к pkmslogout нужно указывать так, чтобы нивелировать наш корневой url,
	// иначе получим 404 и логаут не сработает
	private final static String WEBSEAL_LOGOUT_URL = "../../pkmslogout";
	// Адрес для входа в приложение по умолчанию
	private final static String REDIRECT_URL = "/login";

	@Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String contextPath = request.getContextPath();
        SecurityContextHolder.clearContext();
        if (Pattern.compile("Web\\s*Sphere", Pattern.CASE_INSENSITIVE).matcher(request.getSession().getServletContext().getServerInfo()).find()) {
			if (isWebsealUsed(request)) {
				LOG.info("LOGOUT: WEBSEAL");
				// При выходе нужно посетить и страницу логаута сферы и страницу логаута вебсила
				// Через logoutExitPage выстраиваем их в цепочку редиректов
				response.sendRedirect(contextPath + "/ibm_security_logout?logoutExitPage=" + URLEncoder.encode(WEBSEAL_LOGOUT_URL, "UTF-8"));
			} else {
				LOG.info("LOGOUT: WEBSPHERE");
				response.sendRedirect(contextPath + "/ibm_security_logout?logoutExitPage=" + REDIRECT_URL);
			}
        } else {
			LOG.info("LOGOUT: OTHER");
            response.sendRedirect(contextPath + REDIRECT_URL);
        }
    }

	/**
	 * Определяет был ли сделан запрос через WebSeal или напрямую к серверу
	 *
	 * @param request запрос
	 * @return факт использования WebSeal
	 */
	private boolean isWebsealUsed(HttpServletRequest request) {
		return request.getHeader(WEBSEAL_HEADER_NAME) != null;
	}
}