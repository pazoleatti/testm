package com.aplana.sbrf.taxaccounting.common.service;

import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

/**
 * Интерцептор для пробрасывания спринговых бинов в ejb
 * @author aivanov
 */
public class DepartmentUsageInterceptor extends SpringBeanAutowiringInterceptor {
    @Override
    protected String getBeanFactoryLocatorKey(Object target) {
        return "dapartmentBeanFactory";
    }
}
