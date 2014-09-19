package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class DepartmentReportPeriodServiceImpl implements DepartmentReportPeriodService {

    @Autowired
    DepartmentReportPeriodDao departmentReportPeriodDao;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public DepartmentReportPeriod get(int id) {
        return departmentReportPeriodDao.get(id);
    }

    @Override
    public List<DepartmentReportPeriod> getListByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter) {
        return departmentReportPeriodDao.getListByFilter(departmentReportPeriodFilter);
    }

    @Override
    public int save(DepartmentReportPeriod departmentReportPeriod) {
        return departmentReportPeriodDao.save(departmentReportPeriod);
    }

    @Override
    public void updateActive(int id, boolean active) {
        departmentReportPeriodDao.updateActive(id, active);
    }

    @Override
    public void updateActive(List<DepartmentReportPeriod> drps, boolean active, List<LogEntry> logs) {
        if (drps == null || drps.isEmpty())
            throw new ServiceException("Пустой список отчетных периодов.");
        ArrayList<Integer> ids = new ArrayList<Integer>(drps.size());
        if (logs != null){
            for (DepartmentReportPeriod drp : drps){
                if (drp.isActive())
                    continue;
                int year = drp.getReportPeriod().getTaxPeriod().getYear();
                logs.add(new LogEntry(LogLevel.INFO, "Период" + " \"" + drp.getReportPeriod().getName() + "\" " +
                        year + " " +
                        "закрыт для \"" +
                        departmentService.getDepartment(drp.getDepartmentId()).getName() +
                        "\""));

            }
        }
        departmentReportPeriodDao.updateActive(ids, active);
    }

    @Override
    public void updateCorrectionDate(int id, Date correctionDate) {
        departmentReportPeriodDao.updateCorrectionDate(id, correctionDate);
    }

    @Override
    public void updateBalance(int id, boolean isBalance) {
        departmentReportPeriodDao.updateBalance(id, isBalance);
    }

    @Override
    public void delete(int id) {
        departmentReportPeriodDao.delete(id);
    }

    @Override
    public boolean existForDepartment(int departmentId, int reportPeriodId) {
        return departmentReportPeriodDao.existForDepartment(departmentId, reportPeriodId);
    }

    @Override
    public Integer getCorrectionNumber(int id) {
        return departmentReportPeriodDao.getCorrectionNumber(id);
    }
}
