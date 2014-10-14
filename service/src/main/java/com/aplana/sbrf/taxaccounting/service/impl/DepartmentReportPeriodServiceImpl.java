package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class DepartmentReportPeriodServiceImpl implements DepartmentReportPeriodService {

    private final static String ERROR_BATCH_MESSAGE = "Пустой список отчетных периодов";

    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;

    @Override
    public DepartmentReportPeriod get(int id) {
        return departmentReportPeriodDao.get(id);
    }

    @Override
    public List<DepartmentReportPeriod> getListByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter) {
        return departmentReportPeriodDao.getListByFilter(departmentReportPeriodFilter);
    }

    @Override
    public List<Integer> getListIdsByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter) {
        return departmentReportPeriodDao.getListIdsByFilter(departmentReportPeriodFilter);
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
    public void updateActive(List<Integer> ids, boolean active) {
        if (ids == null || ids.isEmpty())
            throw new ServiceException(ERROR_BATCH_MESSAGE);

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
    public void updateBalance(List<Integer> ids, boolean isBalance) {
        if (ids == null || ids.isEmpty())
            throw new ServiceException(ERROR_BATCH_MESSAGE);
        departmentReportPeriodDao.updateBalance(ids, isBalance);
    }

    @Override
    public void delete(int id) {
        departmentReportPeriodDao.delete(id);
    }

    @Override
    public void delete(List<Integer> ids) {
        departmentReportPeriodDao.delete(ids);
    }

    @Override
    public boolean existForDepartment(int departmentId, int reportPeriodId) {
        return departmentReportPeriodDao.existForDepartment(departmentId, reportPeriodId);
    }

    @Override
    public Integer getCorrectionNumber(int id) {
        return departmentReportPeriodDao.getCorrectionNumber(id);
    }

    @Override
    public DepartmentReportPeriod getLast(int departmentId, int reportPeriodId) {
        return departmentReportPeriodDao.getLast(departmentId, reportPeriodId);
    }

    @Override
    public boolean existLargeCorrection(int departmentId, int reportPeriodId, Date correctionDate) {
        return departmentReportPeriodDao.existLargeCorrection(departmentId, reportPeriodId, correctionDate);
    }
}
