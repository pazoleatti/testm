package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.cache.CacheManagerDecorator;
import com.aplana.sbrf.taxaccounting.model.UuidEnum;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.aplana.sbrf.taxaccounting.web.handlers.SUDIRRedirectLogoutSuccessHandler.WEBSEAL_HEADER_NAME;

/**
 * Контроллер для работы с кэшем
 */
@Controller
@RequestMapping(value = "/actions/")
public class CacheController {

	@Autowired
	CacheManagerDecorator cacheManagerDecorator;

	/**
	 * Сбрасывает кэш
	 * @param resp ответ
	 * @throws IOException
	 */
	@RequestMapping(value = "/cache/clear-cache",method = RequestMethod.GET)
	public void clearCache(HttpServletResponse resp) throws IOException {
		cacheManagerDecorator.clearAll();
		resp.setContentType("text/plain");
		resp.setCharacterEncoding("UTF-8");
        resp.getWriter().printf("Кэш сброшен");
	}

	/**
	 * Сброс кэша аутентификации
	 * @param request запрос
	 * @param response ответ
	 * @throws IOException
	 */
	@RequestMapping(value = "/clearAuthenticationCache", method = RequestMethod.GET)
	@ResponseBody
	public void logout401(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setHeader("WWW-Authenticate", "Basic realm=\"defaultWIMFileBasedRealm\"");
		response.setStatus(401);
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(UuidEnum.IS_WEBSEAL.name(), isWebsealUsed(request));
			response.getWriter().printf(jsonObject.toString());
		} catch (JSONException e) {

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
