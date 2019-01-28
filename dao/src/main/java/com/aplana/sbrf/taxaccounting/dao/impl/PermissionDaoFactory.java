package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.model.refbook.PermissivePerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookSimple;
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
    @Autowired
    private DeclarationTemplateDao declarationTemplateDao;
    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;
    @Autowired
    private DepartmentConfigDao departmentConfigDao;
    @Autowired
    private RefBookPersonDao refBookPersonDao;

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
        if (permissionClass.isAssignableFrom(DeclarationTemplate.class)) {
            return declarationTemplateDao;
        }
        if (permissionClass.isAssignableFrom(DepartmentReportPeriod.class)) {
            return departmentReportPeriodDao;
        }
        if (permissionClass.isAssignableFrom(DepartmentConfig.class)) {
            return departmentConfigDao;
        }
        if (PermissivePerson.class.isAssignableFrom(permissionClass)) {
            return refBookPersonDao;
        }
        throw new ServiceException("Не удалось получить дао, соответствующее защищенной сущности: " + permissionClass);
    }
}
