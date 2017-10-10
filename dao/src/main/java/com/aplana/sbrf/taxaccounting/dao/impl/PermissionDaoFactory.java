package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Фабрика для получения дао, соответствующих определенным защищенным сущностям
 * @author dloshkarev
 */
@Component
public class PermissionDaoFactory {

    @Autowired
    private DeclarationDataDao declarationDataDao;
    @Autowired
    private DeclarationDataFileDao declarationDataFileDao;
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private LockDataDao lockDataDao;
    @Autowired
    private ReportPeriodDao reportPeriodDao;
    @Autowired
    private TAUserDao userDao;

    /**
     * Возвращает дао, которое занимается обработкой указанной защищенной сущности
     * @param permissionClass класс защищенной сущности
     * @return дао
     */
    public <T extends SecuredEntity> PermissionDao getPermissionDao(Class<T> permissionClass) {
        if (permissionClass.isAssignableFrom(DeclarationDataFile.class)) {
            return declarationDataFileDao;
        }
        if (permissionClass.isAssignableFrom(DeclarationData.class)) {
            return declarationDataDao;
        }
        if (permissionClass.isAssignableFrom(Department.class)) {
            return departmentDao;
        }
        if (permissionClass.isAssignableFrom(LockData.class)) {
            return lockDataDao;
        }
        if (permissionClass.isAssignableFrom(ReportPeriod.class)) {
            return reportPeriodDao;
        }
        if (permissionClass.isAssignableFrom(TAUser.class)) {
            return userDao;
        }
        throw new ServiceException("Не удалось получить дао, соответствующее защищенной сущности: " + permissionClass);
    }
}