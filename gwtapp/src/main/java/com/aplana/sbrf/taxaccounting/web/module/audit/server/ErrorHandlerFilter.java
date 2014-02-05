package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;

import javax.servlet.*;
import java.io.IOException;

/**
 * Фильтр должен отлавливать эксепшины com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException
 * и выкидывать org.springframework.security.access.AccessDeniedException
 * см. http://jira.aplana.com/browse/SBRFACCTAX-5434
 * User: fmukhametdinov
 * Date: 03.02.14
 * Time: 17:43
 */
public class ErrorHandlerFilter implements Filter {

    public void init(FilterConfig config) throws ServletException {

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        try{
            chain.doFilter(request, response);
        }catch(AccessDeniedException e){
            throw new org.springframework.security.access.AccessDeniedException("Access is denied!", e);
        }
    }

    public void destroy() {
    }

}
