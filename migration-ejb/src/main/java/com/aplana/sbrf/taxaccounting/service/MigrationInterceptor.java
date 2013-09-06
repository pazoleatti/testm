package com.aplana.sbrf.taxaccounting.service;

import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

public class MigrationInterceptor extends SpringBeanAutowiringInterceptor {
    @Override
    protected String getBeanFactoryLocatorKey(Object target) {
        return "migrationBeanFactory";
    }
}
