package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.cache.CacheManagerDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class CacheController {

	@Autowired
	CacheManagerDecorator cacheManagerDecorator;

	@RequestMapping(value = "/clear-cache",method = RequestMethod.GET)
	public void clearCache(HttpServletResponse resp) throws IOException {
		cacheManagerDecorator.clearAll();
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().printf("Кэш сброшен");
	}

	@RequestMapping(value = "/clearAuthenticationCache", method = RequestMethod.GET)
	@ResponseBody
	public void logout401(HttpServletResponse response) {
		response.setHeader("WWW-Authenticate", "Basic realm=\"defaultWIMFileBasedRealm\"");
		response.setStatus(401);
	}
}
