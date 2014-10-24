package com.aplana.sbrf.taxaccounting.common.service;

import org.springframework.beans.factory.annotation.Autowired;

import javax.ejb.*;
import javax.interceptor.Interceptors;

/**
 * EJB-реализация сервиса установки использования подразделения в модуле гарантий
 * @author aivanov
 */
@Local(DepartmentUsageServiceLocal.class)
@Remote(DepartmentUsageServiceRemote.class)
@Stateless
@Interceptors(DepartmentUsageInterceptor.class)
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class DepartmentUsageServiceBean implements DepartmentUsageService {

    @Autowired
    private DepartmentUsageService departmentUsageService;

    @Override
    public void setDepartmentUsedByGarant(long id, boolean used) throws CommonServiceException {
        departmentUsageService.setDepartmentUsedByGarant(id, used);
    }
}
