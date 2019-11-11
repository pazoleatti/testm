package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
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
    @Transactional(readOnly = true)
    public DepartmentReportPeriod get(int id) {
        return departmentReportPeriodDao.fetchOne(id);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentReportPeriod getFirst(int departmentId, int reportPeriodId) {
        return departmentReportPeriodDao.fetchFirst(departmentId, reportPeriodId);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentReportPeriod getLast(int departmentId, int reportPeriodId) {
        return departmentReportPeriodDao.fetchLast(departmentId, reportPeriodId);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentReportPeriod getPrevLast(int departmentId, int reportPeriodId) {
        return departmentReportPeriodDao.fetchPrevLast(departmentId, reportPeriodId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getIdsByDepartmentTypeAndReportPeriod(int departmentTypeCode, int departmentReportPeriodId) {
        return departmentReportPeriodDao.fetchIdsByDepartmentTypeAndReportPeriod(departmentTypeCode, departmentReportPeriodId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentReportPeriod> getPeriodsSortedByFormTypePriority(long departmentId, String periodCode,
                                                                           int year, boolean correctivePeriods) {
        return departmentReportPeriodDao.getPeriodsSortedByFormTypePriority(departmentId, periodCode, year, correctivePeriods);
    }

    @Override
    public String formatPeriodName(DepartmentReportPeriod departmentReportPeriod, String formatExp) {
        return departmentReportPeriodFormatter.formatPeriodName(departmentReportPeriod, formatExp);
    }
}
