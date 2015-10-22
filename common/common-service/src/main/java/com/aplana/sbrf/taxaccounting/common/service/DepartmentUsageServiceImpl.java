package com.aplana.sbrf.taxaccounting.common.service;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



/**
 * Спринговая реализация сервиса установки использования подразделения в модуле гарантий. Нужна для того, чтобы все работало в dev-моде.
 * Чтобы код не дублировался, он также вызывается из ejb-реалзации
 *
 * @author aivanov
 */
@Service
@Transactional
public class DepartmentUsageServiceImpl implements DepartmentUsageService {

    @Autowired
    private DepartmentService departmentService;

    private static final Log LOG = LogFactory.getLog(DepartmentUsageServiceImpl.class);

    @Override
    public void setDepartmentUsedByGarant(long depId, boolean used) throws CommonServiceException {
        try {
            LOG.info("Setting department usage by Garants: depId=" + depId + ", used=" + used);
            Department department = departmentService.getDepartment((int) depId);
            if (department == null) {
                LOG.info("Department not found: depId=" + depId);
                throw new CommonServiceException("Подразделения не существует!");
            } else {
                departmentService.setUsedByGarant((int) depId, used);
            }

        } catch (RuntimeException e) {
            LOG.info("Error setting department usage by Garants: " + e.getClass() + " - " + e.getMessage());
            throw new CommonServiceException(e. getMessage(), e.getCause());
        } catch (Exception e) {
            LOG.info("Error setting departmentusage by Garants: " + e.getClass() + " - " + e.getMessage());
            throw new CommonServiceException(e. getMessage(), e.getCause());
        }
    }
}
