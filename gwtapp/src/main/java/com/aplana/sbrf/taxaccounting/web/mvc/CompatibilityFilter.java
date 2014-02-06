package com.aplana.sbrf.taxaccounting.web.mvc;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CompatibilityFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		response.addHeader("X-UA-Compatible","IE=8");
		filterChain.doFilter(servletRequest, response);
	}

	@Override
	public void destroy() {
	}
}
