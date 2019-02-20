package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.script.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.utils.DepartmentReportPeriodFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Component("departmentReportPeriodService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DepartmentReportPeriodServiceImpl implements DepartmentReportPeriodService {

    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;

    @Autowired
    private DepartmentReportPeriodFormatter departmentReportPeriodFormatter;

    @Override
    public DepartmentReportPeriod get(int id) {
        return departmentReportPeriodDao.fetchOne(id);
    }

    @Override
    public DepartmentReportPeriod getFirst(int departmentId, int reportPeriodId) {
        return departmentReportPeriodDao.fetchFirst(departmentId, reportPeriodId);
    }

    @Override
    public DepartmentReportPeriod getLast(int departmentId, int reportPeriodId) {
        return departmentReportPeriodDao.fetchLast(departmentId, reportPeriodId);
    }

    @Override
    public DepartmentReportPeriod getPrevLast(int departmentId, int reportPeriodId) {
        return departmentReportPeriodDao.fetchPrevLast(departmentId, reportPeriodId);
    }

    @Override
    public List<Integer> getIdsByDepartmentTypeAndReportPeriod(int departmentTypeCode, int departmentReportPeriodId) {
        return departmentReportPeriodDao.fetchIdsByDepartmentTypeAndReportPeriod(departmentTypeCode, departmentReportPeriodId);
    }

    @Override
    public String formatPeriodName(DepartmentReportPeriod departmentReportPeriod, String formatExp) {
        return departmentReportPeriodFormatter.formatPeriodName(departmentReportPeriod, formatExp);
    }
}
