package com.aplana.sbrf.taxaccounting.service;

import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

/**
 * @author auldanov
 */
public class SchedulerInterceptor extends SpringBeanAutowiringInterceptor {
    @Override
    protected String getBeanFactoryLocatorKey(Object target) {
        return "schedulerBeanFactory";
    }
}
