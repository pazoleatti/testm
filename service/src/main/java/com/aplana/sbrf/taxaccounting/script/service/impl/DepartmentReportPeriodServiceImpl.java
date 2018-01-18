package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.script.service.DepartmentReportPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Transactional(readOnly = true)
@Component("departmentReportPeriodService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DepartmentReportPeriodServiceImpl implements DepartmentReportPeriodService {

    @Autowired
    DepartmentReportPeriodDao departmentReportPeriodDao;

    @Override
    public DepartmentReportPeriod get(int id) {
        return departmentReportPeriodDao.fetchOne(id);
    }

    @Override
    @Deprecated
    public Map<Integer, List<Date>> getCorrectionDateListByReportPeriod(Collection<Integer> reportPeriodIds) {
        return departmentReportPeriodDao.fetchCorrectionDateListByReportPeriod(reportPeriodIds);
    }

    @Override
    public List<DepartmentReportPeriod> getListByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter) {
        return departmentReportPeriodDao.fetchAllByFilter(departmentReportPeriodFilter);
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
}
