package com.aplana.sbrf.taxaccounting.async.service;

import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

/**
 * Интерцептор для пробрасывания спринговых бинов в ejb
 * @author dloshkarev
 */
public class AsyncTaskInterceptor extends SpringBeanAutowiringInterceptor {
    @Override
    protected String getBeanFactoryLocatorKey(Object target) {
        return "asyncBeanFactory";
    }
}
