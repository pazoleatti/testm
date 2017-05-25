package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.cache.CacheManagerDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping(value = "/actions/")
public class CacheController {

	@Autowired
	CacheManagerDecorator cacheManagerDecorator;

	@RequestMapping(value = "/cache/clear-cache",method = RequestMethod.GET)
	public void clearCache(HttpServletResponse resp) throws IOException {
		cacheManagerDecorator.clearAll();
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().printf("Кэш сброшен");
	}

}
