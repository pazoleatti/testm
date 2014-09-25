package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.service.script.DepartmentReportPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Component("departmentReportPeriodService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DepartmentReportPeriodServiceImpl implements DepartmentReportPeriodService {

    @Autowired
    DepartmentReportPeriodDao departmentReportPeriodDao;

    @Override
    public DepartmentReportPeriod get(int id) {
        return departmentReportPeriodDao.get(id);
    }
}
